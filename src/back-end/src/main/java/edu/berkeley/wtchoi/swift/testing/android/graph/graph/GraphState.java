package edu.berkeley.wtchoi.swift.testing.android.graph.graph;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.util.dot.DGVertex;
import edu.berkeley.wtchoi.swift.testing.android.graph.tree.TreeState;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/11/13
 * Time: 10:44 PM
 *
 * SwiftHand Project follows BSD License
 *
 * [The "BSD license"]
 * Copyright (c) 2013 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class GraphState implements DGVertex<ICommand, GraphState> {
    public final int id;
    public final Set<ICommand> palette;
    final Graph graph;

    boolean isRefined = false;

    public int depth=-1;

    public GraphState(int id, Graph g){
        this.id = id;
        palette = null;
        graph = g;
    }

    public GraphState(int id, Set<ICommand> palette, Graph g){
        this.id = id;
        this.palette = palette;
        graph = g;
    }

    public int compareTo(GraphState gs){
        return Integer.valueOf(this.id).compareTo(gs.id);
    }

    public GraphState getSuccessor(ICommand cmd){
        return graph.getSuccessor(this, cmd);
    }

    public boolean isStopState(){
        return palette != null && palette.size() == 0;
    }

    public String toString(){
        return ("G"+id);
    }

    public void setRefined(){
        isRefined = true;
    }

    public boolean isVisited(){
        return palette != null;
    }

    public TreeState getRepresentativeState(){
        return graph.abstraction.concreteMap.get(this);
    }

    //DGVector methods
    @Override
    public int getId(){
        return id;
    }

    @Override
    public Collection<Pair<ICommand, GraphState>> getOutgoingEdges(){
        LinkedList<Pair<ICommand, GraphState>> edges = new LinkedList<Pair<ICommand, GraphState>>();
        if(palette == null) return edges;

        for(ICommand cmd:palette){
            edges.add(new Pair<ICommand, GraphState>(cmd, this.getSuccessor(cmd)));
        }
        return edges;
    }

    @Override
    public boolean isVisible(){
        return !isStopState();
    }

    @Override
    public int getObservationDegree(){
        if(palette == null && !isStopState()) return UNVISITED;
        return OBSERVED;
    }

    @Override
    public boolean isInteresting(){
        return isRefined;
    }

    @Override
    public boolean isVeryInteresting(){
        return false;
    }
}


