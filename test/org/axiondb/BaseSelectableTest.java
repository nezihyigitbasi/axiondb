/*
 * $Id: BaseSelectableTest.java,v 1.1 2007/11/28 10:01:21 jawed Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
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

package org.axiondb;

import java.io.Serializable;

/**
 * @version $Revision: 1.1 $ $Date: 2007/11/28 10:01:21 $
 * @author Chuck Burdick
 * @author Rod Waldhoff
 */
public abstract class BaseSelectableTest extends BaseSerializableTest {

    //------------------------------------------------------------ Conventional

    protected BaseSelectableTest(String testName) {
        super(testName);
    }

    //--------------------------------------------------------------- Framework
    
    protected abstract Selectable makeSelectable();

    protected final Serializable makeSerializable() {
        return makeSelectable();
    }
    
    //--------------------------------------------------------------- Lifecycle

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //------------------------------------------------------------------- Tests

    public final void testSelectableEvaluateNull() {
        Selectable sel = makeSelectable();
        try {
            sel.evaluate(null);
        } catch(AxionException e) {
            // axionexception is acceptable, npe or simliar isn't
        }
    }

    public final void testSelectableLabelIsNotNull() {
        Selectable sel = makeSelectable();
        assertNotNull(sel.getLabel());
    }

    public final void testSelectableNameIsNotNull() {
        Selectable sel = makeSelectable();
        assertNotNull(sel.getName());
    }

    public final void testSelectableDataTypeIsNotNull() {
        Selectable sel = makeSelectable();
        assertNotNull(sel.getDataType());
    }

    public final void testSerializeDeserialize() throws Exception {
        Selectable sel1 = makeSelectable();
        Selectable sel2 = (Selectable)(cloneViaSerialization(sel1));
        assertEquals(sel1.getName(),sel2.getName());
        assertEquals(sel1.getLabel(),sel2.getLabel());
        // TODO: Why is the following assertion disabled?
        //assertEquals(sel1.getDataType(),sel2.getDataType());
    }
}
