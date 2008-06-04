/*
 * $Id: TestDeleteCommand.java,v 1.1 2007/11/28 10:01:23 jawed Exp $
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.axiondb.AxionCommand;
import org.axiondb.Row;
import org.axiondb.Table;
import org.axiondb.engine.rows.SimpleRow;

/**
 * @version $Revision: 1.1 $ $Date: 2007/11/28 10:01:23 $
 * @author Ahimanikya Satapathy
 */
public class TestDeleteCommand extends BaseAxionCommandTest {

    //------------------------------------------------------------ Conventional

    public TestDeleteCommand(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestDeleteCommand.class);
    }

    //--------------------------------------------------------------- Lifecycle

    public void setUp() throws Exception {
        super.setUp();
        {
            CreateTableCommand cmd = new CreateTableCommand("FOO");
            cmd.addColumn("ID","integer");
            cmd.addColumn("NAME","varchar", "10");
            cmd.execute(getDatabase());
        }
        populateTable();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected AxionCommand makeCommand() {
        return new DeleteCommand("FOO", null);
    }
    
    protected void populateTable() throws Exception{
        Table table = getDatabase().getTable("FOO");
        {
            Row row = new SimpleRow(2);
            row.set(0,new Integer(1));
            row.set(1,"one");
            table.addRow(row);
        }
        {
            Row row = new SimpleRow(2);
            row.set(0,new Integer(2));
            row.set(1,"two");
            table.addRow(row);
        }
    }
    
    //------------------------------------------------------------------- Tests

    public void testExecute() throws Exception {
        AxionCommand cmd = makeCommand();
        cmd.execute(getDatabase());
    }

    public void testExecuteQuery() throws Exception {
        assertExecuteQueryIsNotSupported(makeCommand());
    }
    
    public void testBadTableName() throws Exception {
        DeleteCommand cmd = new DeleteCommand("FOO1", null);
        try {
            cmd.execute(getDatabase());
            fail("Expected table not found exception");
        } catch(Exception e) {
            // expected
        }
    }

}
