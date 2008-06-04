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
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.util.Comparator;

import org.axiondb.AxionException;
import org.axiondb.DataType;

/**
 * A {@link DataType} representing a {@link String} value.
 *
 * @version  
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 * @author Ahimanikya Satapathy
 */
public class StringType extends BaseDataType {

    public StringType() {
    }

    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    public String getPreferredValueClassName() {
        return "java.lang.String";
    }

    public int getColumnDisplaySize() {
        return 32766;
    }
    
    public int getPrecision() {
        return Integer.MAX_VALUE;
    }
    
    public boolean isCaseSensitive() {
        return true;
    }

    /**
     * Returns <code>"string"</code>
     * 
     * @return <code>"string"</code>
     */
    public String toString() {
        return "string";
    }

    public byte[] toByteArray(Object value) throws AxionException {
        return toString(value).getBytes();
    }

    /**
     * Returns <code>true</code> iff <i>value </i> is <code>null</code> or a
     * {@link String String}.
     */
    public boolean accepts(Object value) {
        return true;
    }

    /**
     * Returns a {@link String}converted from the given <i>value </i>, or throws
     * {@link IllegalArgumentException}if the given <i>value </i> isn't
     * {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws AxionException {
        if (null == value) {
            return null;
        } if(value instanceof Clob) {
            return new CLOBType().toString(value);
        } if(value instanceof Blob) {
            try {
                return new String(((Blob)value).getBytes(1, (int)((Blob)value).length()));
            } catch (Exception e) {
                throw new AxionException(e);
            }
        } if (value instanceof byte[]) { 
            return new String((byte[]) value);
        } else {
            return value.toString();
        }
    }

    public boolean supportsSuccessor() {
        return true;
    }

    public Object successor(Object value) throws IllegalArgumentException {
        String v = ((String)value);
        return v + "\0";
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        String val = in.readUTF();
        if("null".equals(val)) {
            if(!in.readBoolean()) {
                return null;
            }
        }
        return val;
    }

    /**
     * Writes the given <i>value </i> to the given <code>DataOutput</code>.
     * <code>Null</code> values are written as <code>"null", false</code>, the
     * non-null String <code>"null"</code> is written as <code>"null", true</code>,
     * all other values are written directly.
     * 
     * @param value the value to write, which must be {@link #accepts acceptable}
     */
    public void write(Object value, DataOutput out) throws IOException {
        if(null == value) {
            out.writeUTF("null");
            out.writeBoolean(false);
        } else if("null".equals(value)) {
            out.writeUTF("null");
            out.writeBoolean(true);
        } else {
            out.writeUTF(value.toString());
        }
    }

    public DataType makeNewInstance() {
        return new StringType();
    }

    public String getLiteralPrefix() {
        return "'";
    }

    public String getLiteralSuffix() {
        return "'";
    }

    public short getSearchableCode() {
        return DatabaseMetaData.typeSearchable;
    }

    public int compare(Object a, Object b) {
        return ((String)a).compareTo((String)b);
    }

    protected Comparator getComparator() {
        return this;
    }
    
    public static final StringType instance() {
        return INSTANCE;
    }
    
    private static final StringType INSTANCE = new StringType();
    
    private static final long serialVersionUID = -2663968706274343425L;
}

