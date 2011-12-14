/*
 * 
 * =======================================================================
 * Copyright (c) 2002-2004 Axion Development Team.  All rights reserved.
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
package org.axiondb.engine.commands;

import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.Table;
import org.axiondb.TransactableTable;
import org.axiondb.engine.tables.BaseDiskTable;
import org.axiondb.engine.tables.ExternalDatabaseTable;
import org.axiondb.jdbc.AxionResultSet;

/**
 * A <tt>DEFRAG TABLE tableName</tt> command, to compact the a disk resident table
 *
 * @version  
 * @author Sudhendra Seshachala
 * @author Ahimanikya Satapathy
 */
public class DefragCommand extends BaseAxionCommand {

    public DefragCommand() {
    }

    public int executeUpdate(Database db) throws AxionException {
        if(_tableName == null) {
            throw new AxionException("Table name can't be null");
        }

        if (isDiskTable(db, _tableName)){
            return db.defragTable(_tableName);
        }

        return 0;
    }

    public boolean execute(Database db) throws AxionException {
        executeUpdate(db);
        return false;
    }

    /** Unsupported */
    public AxionResultSet executeQuery(Database database) throws AxionException {
        throw new UnsupportedOperationException("Use execute.");
    }

    public void setObjectName(String tableName) {
        _tableName = tableName;
    }

    private boolean isDiskTable(Database db, String tableName) throws AxionException {
        boolean ret = false;

        Table table = db.getTable(tableName);
        if (table == null){
            throw new AxionException("Table not found." + tableName);
        }

        if (!(table instanceof ExternalDatabaseTable)){
            while (table instanceof TransactableTable){
                table = ((TransactableTable)table).getTable();
            }

            if (table instanceof BaseDiskTable){
                ret = true;
            }
        }
        return ret;
    }

    private String _tableName;
}