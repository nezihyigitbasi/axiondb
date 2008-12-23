/*
 * 
 * =======================================================================
 * Copyright (c) 2002 Axion Development Team.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The names "Tigris", "Axion", nor the names of its contributors may
 *    not be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * 4. Products derived from this software may not be called "Axion", nor
 *    may "Tigris" or "Axion" appear in their names without specific prior
 *    written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =======================================================================
 */

package org.axiondb.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.Transaction;
import org.axiondb.TransactionConflictException;
import org.axiondb.TransactionManager;
import org.axiondb.util.Utils;

/**
 * An implemenation of {@link TransactionManager}currently only supportd
 * {@link SnapshotIsolationTransaction}s.
 * 
 * @version  
 * @author Rodney Waldhoff
 */
public class TransactionManagerImpl implements TransactionManager {
    public TransactionManagerImpl(Database db) {
        _database = db;
    }

    @SuppressWarnings("unchecked")
    public synchronized Transaction createTransaction() throws AxionException {
        assertNotShutdown();
        Transaction t = new SnapshotIsolationTransaction(getLastCommittedTransaction());
        _openTransactions.add(t);
        return t;
    }

    @SuppressWarnings("unchecked")
    public synchronized void commitTransaction(Transaction t) throws AxionException {
        assertNotShutdown();
        if (t.getModifiedTables().isEmpty()) {
            t.commit();
            t.apply();
            _openTransactions.remove(t);
        } else {
            // check for conflicts
            Iterator iter = null;
            {
                int index = _committedTransactions.indexOf(t.getOpenOnTransaction());
                if (-1 == index) {
                    iter = _committedTransactions.iterator();
                } else {
                    iter = _committedTransactions.subList(index + 1, _committedTransactions.size()).iterator();
                }
            }

            while (iter.hasNext()) {
                Transaction c = (Transaction) (iter.next());
                if (inConflict(t, c)) {
                    _log.log(Level.FINE,"commitTransaction(): conflict found.");
                    throw new TransactionConflictException("Transaction conflict.");
                }
            }

            t.commit();
            _committedTransactions.add(t);
            _openTransactions.remove(t);
        }
        tryToApply();
    }

    public synchronized void abortTransaction(Transaction t) throws AxionException {
        assertNotShutdown();
        t.rollback();
        _openTransactions.remove(t);

        try {
            tryToApply();
        } catch (AxionException e) {
            _log.log(Level.FINE,e.getMessage(),e);
        }
    }

    public synchronized void shutdown() throws AxionException {
        for (int i = _openTransactions.size() - 1; i >= 0; i--) {
            Transaction t = (Transaction) (_openTransactions.get(i));
            t.rollback();
        }
        _openTransactions.clear();

        try {
            tryToApply();
        } catch (AxionException e) {
        }
        _database.shutdown();
        _database = null;
    }

    public synchronized boolean isShutdown() {
        return null == _database;
    }

    private boolean inConflict(Transaction newT, Transaction oldT) {
        // if the new transaction changed anything
        if (!newT.getModifiedTables().isEmpty()) {
            // then check that none of the tables read were changed
            // by the already committed transaction
            // (note that this is much too strong of a conflict detection
            // alogrithm, it'll give a lot of false conflicts).
            if (Utils.containsAny(newT.getReadTables(), oldT.getModifiedTables())) {
                return true;
            }
        }
        return false;
    }

    private void assertNotShutdown() throws AxionException {
        if (isShutdown()) {
            throw new AxionException("Already shutdown");
        }
    }

    private void tryToApply() throws AxionException {
        if ((!NEVER_APPLY) && _openTransactions.isEmpty()) {
            Transaction last = null;
            try {
                for (Iterator iter = _committedTransactions.iterator(); iter.hasNext();) {
                    last = (Transaction) (iter.next());
                    last.apply();
                    iter.remove();
                }
            } catch (Exception e) {
                _committedTransactions.clear();
                throw new AxionException("Fail to apply transction", e);
            }
            if (null != last) {
                last.checkpoint();
            }
        }
    }

    private Database getLastCommittedTransaction() {
        if (_committedTransactions.isEmpty()) {
            return _database;
        }
        return (Database) (_committedTransactions.get(_committedTransactions.size() - 1));
    }

    private List _committedTransactions = new ArrayList();
    private List _openTransactions = new ArrayList();
    private Database _database = null;

    // allow a System property to indicate that we should never apply a transaction
    public static final boolean NEVER_APPLY;
    static {
        boolean neverApply = false;
        try {
            neverApply = Boolean.getBoolean("org.axiondb.engine.TransactionManagerImpl.NEVER_APPLY");
        } catch (Throwable t) {
            neverApply = false;
        }
        NEVER_APPLY = neverApply;
    }

    private static Logger _log = Logger.getLogger(TransactionManagerImpl.class.getName());

}
