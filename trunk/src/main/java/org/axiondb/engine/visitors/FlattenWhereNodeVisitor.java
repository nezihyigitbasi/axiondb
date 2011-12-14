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

package org.axiondb.engine.visitors;

import java.util.LinkedHashSet;
import java.util.Set;

import org.axiondb.Selectable;
import org.axiondb.SelectableVisitor;
import org.axiondb.functions.AndFunction;

/**
 * Decomposes a {@link WhereNode}tree into a {@link LinkedHashSet}of {@link WhereNode}s that
 * were originally ANDed together in the source tree.
 * 
 * @author Morgan Delagrange
 * @author Chuck Burdick
 * @author Ahimanikya Satapathy
 */
public class FlattenWhereNodeVisitor implements SelectableVisitor {

    protected Set _nodes;

    public Set getNodes(Selectable node) {
        _nodes = new LinkedHashSet();
        visit(node);
        return _nodes;
    }
    
    public void visit(Selectable sel) {
        if (sel instanceof AndFunction) {
            AndFunction fn = (AndFunction) sel;
            for (int i = 0, I = fn.getArgumentCount(); i < I; i++) {
                visit(fn.getArgument(i));
            }
        } else if (sel != null) {
            _nodes.add(sel);
        }
    }

}