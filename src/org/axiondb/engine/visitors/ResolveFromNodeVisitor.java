/*
 * 
 * =======================================================================
 * Copyright (c) 2004 Axion Development Team.  All rights reserved.
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
package org.axiondb.engine.visitors;

import java.util.List;

import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.FromNode;
import org.axiondb.Table;
import org.axiondb.TableIdentifier;
import org.axiondb.engine.commands.SelectCommand;
import org.axiondb.engine.commands.SubSelectCommand;

/**
 * Resolves a (@link FromNode) for a given (@link Database)
 * 
 * @author Ahimanikya Satapathy
 * @author Ritesh Adval
 */
public class ResolveFromNodeVisitor {

    public void resolveFromNode(FromNode from, Database db) throws AxionException {
        if (from == null) {
            return;
        }
        resolveFromNode(from, from.getLeft(), db, FromNode.TYPE_LEFT);
        resolveFromNode(from, from.getRight(), db, FromNode.TYPE_RIGHT);
    }

    private void resolveFromNode(FromNode from, Object child, Database db, int type)
            throws AxionException {
        if (child instanceof SelectCommand) {
            SubSelectCommand selCmd = ((SubSelectCommand) child);
            Table view = selCmd.getTableView(db, null, true);
            TableIdentifier tid = new TableIdentifier(view.getName(), selCmd.getAlias());
            if (type == FromNode.TYPE_LEFT) {
                from.setLeft(tid);
            } else {
                from.setRight(tid);
            }
        } else if (child instanceof FromNode) {
            resolveFromNode((FromNode) child, db);
        }
    }

    public void resolveFromNode(FromNode node, Database db, List selected) throws AxionException {
        if (node == null) {
            return;
        }

        ResolveSelectableVisitor resolveSel = new ResolveSelectableVisitor(db);
        node.setCondition(resolveSel.visit(node.getCondition(), selected, node
            .toTableArray()));
        MaskSelectablesForTablesVisitor filter = new MaskSelectablesForTablesVisitor();

        Object left = node.getLeft();
        if (left instanceof FromNode) {
            FromNode childNode = (FromNode) left;
            resolveFromNode(childNode, db, filter.maskAliasListForTables(childNode, selected));
        }

        Object right = node.getRight();
        if (right instanceof FromNode) {
            FromNode childNode = (FromNode) right;
            resolveFromNode(childNode, db, filter.maskAliasListForTables(childNode, selected));
        }
    }

}
