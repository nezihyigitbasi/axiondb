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
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Comparator;

import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.DataTypeFactory;
import org.axiondb.util.ComparableComparator;

/**
 * Abstract base implemention of {@link DataType}.
 *
 * @version  
 * @author Rodney Waldhoff
 * @author Rob Oxspring
 * @author Chuck Burdick
 */
public abstract class BaseDataType implements DataType, DataTypeFactory {
    public abstract DataType makeNewInstance();
    public abstract boolean accepts(Object value);
    public abstract Object convert(Object value) throws AxionException;
    public abstract Object read(DataInput in) throws IOException;
    public abstract void write(Object value, DataOutput out) throws IOException;
    public abstract int getJdbcType();
    
    /** @see org.axiondb.jdbc.AxionResultSetMetaData#getColumnClassName */
    public String getPreferredValueClassName() {
        return "java.lang.Object";
    }

    public int compare(Object a, Object b) {
        return getComparator().compare(a,b);
    }
    
    /**
     * This base implementation simply returns a
     * {@link ComparableComparator}.
     */
    protected Comparator getComparator() {
        return ComparableComparator.getInstance();
    }

    public int getColumnDisplaySize() {
        return 0;
    }

    public int getPrecision() {
        return 0;
    }

    public int getScale() {
        return 0;    
    }
    
    public boolean isCaseSensitive() {
        return false;
    }

    public boolean isCurrency() {
        return false;
    }
    
    public String getLiteralPrefix() {
        return null;
    }

    public String getLiteralSuffix() {
        return null;
    }

    public int getNullableCode() {
        return DatabaseMetaData.typeNullable;
    }

    public short getSearchableCode() {
        return DatabaseMetaData.typePredBasic;
    }

    public boolean isUnsigned() {
        return false;
    }

    public boolean supportsSuccessor() {
        return false;
    }

    public Object successor(Object value) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }
    
    public int getPrecisionRadix() {
        return 10;
    }
    
    protected Number toNumber(Object value) throws AxionException {
        if(null == value) {
            return null;
        } else if(value instanceof Number) {
            return (Number)value;
        } else {
            throw new AxionException("Can't convert " + toString() + " to Number.");
        }
    }

    public BigDecimal toBigDecimal(Object value) throws AxionException {
        Number num = toNumber(value);
        if(num instanceof BigDecimal) {
            return (BigDecimal)num;
        } else if(null == num) {
            return null;
        } else {
            return new BigDecimal(String.valueOf(num));
        }
    }

    public BigInteger toBigInteger(Object value) throws AxionException {        
        Number num = toNumber(value);
        if(num instanceof BigInteger) {
            return (BigInteger)num;
        } else if(null == num) {
            return null;
        } else {
            return new BigInteger(String.valueOf(num.longValue()));
        }
    }

    public boolean toBoolean(Object value) throws AxionException {
        throw new AxionException("Can't convert " + toString() + " to boolean.");
    }

    public byte toByte(Object value) throws AxionException {
        return toNumber(value).byteValue();
    }
    
    public byte[] toByteArray(Object value) throws AxionException {
        throw new AxionException("Can't convert " + toString() + " to byte[].");
    }

    public Date toDate(Object value) throws AxionException {
        throw new AxionException("Can't convert " + toString() + " to Date.");
    }

    public double toDouble(Object value) throws AxionException {
        return toNumber(value).doubleValue();
    }

    public float toFloat(Object value) throws AxionException {
        return toNumber(value).floatValue();
    }

    public int toInt(Object value) throws AxionException {
        return toNumber(value).intValue();
    }

    public long toLong(Object value) throws AxionException {
        return toNumber(value).longValue();
    }

    public short toShort(Object value) throws AxionException {
        return toNumber(value).shortValue();
    }

    public String toString(Object value) throws AxionException {
        Object val = convert(value);
        if (val == null) {
            return null;
        }
        return val.toString();
    }

    public URL toURL(Object value) throws AxionException {
        String str = toString(value);
        if(null == str) {
            return null;
        }
        
        try {
            return new URL(str);
        } catch (MalformedURLException e) {
            throw new AxionException("Can't convert " + value + " to URL.");
        }                            
    }

    public Time toTime(Object value) throws AxionException {
        throw new AxionException("Can't convert " + toString() + " to Time.");
    }

    public Timestamp toTimestamp(Object value) throws AxionException {
        throw new AxionException("Can't convert " + toString() + " to Timestamp.");
    }

    public Clob toClob(Object value) throws AxionException {
        return new StringClob(toString(value));
    }

    public Blob toBlob(Object value) throws AxionException {
        if(null == value) {
            return null;
        } else if (value instanceof byte[]){
            return new ByteArrayBlob((byte[])value);
        }
        throw new AxionException("Can't convert " + toString() + " to Blob.");
    }
    private static final long serialVersionUID = -6298442558736380407L;
}

