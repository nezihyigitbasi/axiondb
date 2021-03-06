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

package org.axiondb.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;

import org.axiondb.AxionException;
import org.axiondb.DataType;

/**
 * A {@link DataType}representing a short value.
 * 
 * @version  
 * @author Rodney Waldhoff
 * @author Jonathan Giron
 */
public class ShortType extends BaseNumberDataType {

    public ShortType() {
    }

    public int getJdbcType() {
        return java.sql.Types.SMALLINT;
    }

    public String getPreferredValueClassName() {
        return "java.lang.Short";
    }
    
    public String getTypeName() {
        return "SHORT";
    }    

    public int getPrecision() {
        return String.valueOf(Short.MAX_VALUE).length();
    }

    public int getColumnDisplaySize() {
        return String.valueOf(Short.MIN_VALUE).length();
    }

    public String toString() {
        return "short";
    }

    /**
     * Returns an <tt>Short</tt> converted from the given <i>value </i>, or throws
     * {@link IllegalArgumentException}if the given <i>value </i> isn't
     * {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws AxionException {
        int rawValue = 0; 
        
        if (value instanceof Short) {
            return value;
        } else if (value instanceof BigDecimal) {
            rawValue = ((BigDecimal) value).intValue();
        } else if (value instanceof Number) {
            rawValue = ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                rawValue = new BigDecimal(value.toString().trim()).intValue();
            } catch (NumberFormatException e) {
                throw new AxionException(22018);
            }
        } else {
            return super.convert(value);
        }
        
        assertValueInRange(rawValue);
        return new Short((short) rawValue);
    }

    public Object successor(Object value) throws IllegalArgumentException {
        short v = ((Short) value).shortValue();
        if (v == Short.MAX_VALUE) {
            return value;
        }
        return new Short(++v);
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        short value = in.readShort();
        if (Short.MIN_VALUE == value) {
            if (!in.readBoolean()) {
                return null;
            }
        }
        return new Short(value);
    }

    /**
     * Writes the given <i>value </i> to the given <code>DataOutput</code>.
     * <code>Null</code> values are written as <code>Short.MIN_VALUE</code>,
     * <code>false</code>.<code>Short.MIN_VALUE</code> values are written as
     * <code>Short.MIN_VALUE</code>,<code>true</code>. All other values are written
     * directly.
     * 
     * @param value the value to write, which must be {@link #accepts acceptable}
     */
    public void write(Object value, DataOutput out) throws IOException {
        if (null == value) {
            out.writeShort(Short.MIN_VALUE);
            out.writeBoolean(false);
        } else {
            short val;
            try {
                val = ((Short) (convert(value))).shortValue();
                out.writeShort(val);
                if (Short.MIN_VALUE == val) {
                    out.writeBoolean(true);
                }
            } catch (AxionException e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    public DataType makeNewInstance() {
        return new ShortType();
    }

    public int compare(Object a, Object b) {
        short pa = ((Number) a).shortValue();
        short pb = ((Number) b).shortValue();
        return (pa < pb) ? -1 : ((pa == pb) ? 0 : 1);
    }

    protected Comparator getComparator() {
        return this;
    }

    private void assertValueInRange(int value) throws AxionException {
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new AxionException(22003);
        }
    }

    private static final long serialVersionUID = -8598189286242089718L;
}
