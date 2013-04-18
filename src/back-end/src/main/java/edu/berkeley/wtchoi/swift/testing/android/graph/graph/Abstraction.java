package edu.berkeley.wtchoi.swift.testing.android.graph.graph;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.testing.android.graph.tree.Tree;
import edu.berkeley.wtchoi.swift.testing.android.graph.tree.TreeState;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.CVector;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/15/13
 * Time: 12:41 AM
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
class Abstraction {
    public Abstraction(Graph g, GraphStateFactory factory){
        mGraph = g;
        mStateFactory = factory;

        stopState = mStateFactory.createStopState();
    }

    public GraphState abst(TreeState state, Tree tree){
        if(state.isStop) return stopState;

        CSet<ICommand> key = new CSet<ICommand>();
        try{
            key.addAll(state.palette);
        }
        catch(Exception e){
            int x = 1;
        }

        CList<ICommand> path = state.getPath(CVector.class);
        if(uniqueStateMap.containsKey(path)){
            return uniqueStateMap.get(path);
        }
        else if(abstractionMap.containsKey(key)){
            return abstractionMap.get(key);
        }

        GraphState gState = mStateFactory.createLiveState(state.palette);

        if(path.size() == 0) uniqueStateMap.put(path, gState);
        else abstractionMap.put(key, gState);
        concreteMap.put(gState, state);

        return gState;
    }

    final TreeMap<CSet<ICommand>, GraphState> abstractionMap = new TreeMap<CSet<ICommand>, GraphState>();
    final TreeMap<GraphState, TreeState> concreteMap = new TreeMap<GraphState, TreeState>();//debugging purpose
    final TreeMap<CList<ICommand>, GraphState> uniqueStateMap = new TreeMap<CList<ICommand>, GraphState>();
    final Graph mGraph;
    final GraphStateFactory mStateFactory;

    GraphState stopState;

    //refinement
    //Precondition: only the last state is conflicting state, although all states are going to be refined.
    public void refineByCounterExample(List<ICommand> sequence, TreeState state, Tree tree){
        List<ICommand> path = new LinkedList<ICommand>();

        TreeState cursor = tree.initState;
        for(ICommand cmd:sequence){
            path.add(cmd);
            CList<ICommand> key = new CVector<ICommand>();
            key.addAll(path);
            cursor = cursor.getSuccessor(cmd);

            if(!uniqueStateMap.containsKey(key)){
                if(!cursor.isStop){
                    GraphState gState = mStateFactory.createLiveState(cursor.palette);
                    gState.setRefined();
                    uniqueStateMap.put(key, gState);
                    concreteMap.put(gState, cursor);
                }
            }
        }
    }
}
