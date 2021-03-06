/*
 * 
 * =======================================================================
 * Copyright (c) 2002-2005 Axion Development Team.  All rights reserved.
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

package org.axiondb.engine.rowiterators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.axiondb.AxionException;
import org.axiondb.Row;
import org.axiondb.RowDecorator;
import org.axiondb.RowIterator;
import org.axiondb.Selectable;
import org.axiondb.util.ValuePool;

/**
 * A {@link DelegatingRowIterator}implementing DISTINCT.
 * 
 * @version  
 * @author Rodney Waldhoff
 * @author Ahimanikya Satapathy
 */
public class DistinctRowIterator extends AbstractAcceptingRowIterator {

    public DistinctRowIterator(RowIterator iter, Map selectableMap, Selectable[] selectables) {
        super(iter);
        _decorator = new RowDecorator(selectableMap);
        _selectables = selectables;
        _timesEncountered = new HashMap();
    }

    /** Not supported in the base implementation. */
    public void set(Row row) throws AxionException {
        throw new UnsupportedOperationException();
    }

    /** Not supported in the base implementation. */
    public void remove() throws AxionException {
        throw new UnsupportedOperationException();
    }

    public void reset() throws AxionException {
        super.reset();
        _timesEncountered.clear();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(20);
        buf.append("Distinct (");
        for (int i = 0; i < _selectables.length; i++) {
            buf.append(_selectables[i] + (i != 0 ? "," : ""));
        }
        buf.append(")");
        return buf.toString();
    }

    protected boolean acceptable(int rowindex, Row row) throws AxionException {
        List values = populateValueList(row);
        Integer count = (Integer) (_timesEncountered.get(values));
        if (null == count) {
            count = ValuePool.getInt(0);
        }

        // next
        if (rowindex > previousIndex()) {
            _timesEncountered.put(values, ValuePool.getInt(count.intValue() + 1));
            return (0 == count.intValue());
        }

        // previous
        if (1 == count.intValue()) {
            _timesEncountered.remove(values);
        } else {
            _timesEncountered.put(values, ValuePool.getInt(count.intValue() - 1));
        }
        return (1 == count.intValue());
    }

    private List populateValueList(Row row) throws AxionException {
        _decorator.setRow(row);
        List values = new ArrayList(_selectables.length);
        for (int i = 0; i < _selectables.length; i++) {
            values.add(_selectables[i].evaluate(_decorator));
        }
        return values;
    }

    private RowDecorator _decorator = null;
    private Selectable[] _selectables = null;
    private Map _timesEncountered = null;
}
