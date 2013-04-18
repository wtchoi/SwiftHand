package edu.berkeley.wtchoi.swift.testing.android.graph.tree;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.ViewInfo;
import edu.berkeley.wtchoi.swift.driver.drone.CompressedLog;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.swift.util.dot.GraphPrinter;
import edu.berkeley.wtchoi.collection.CList;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/8/13
 * Time: 8:11 PM
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
public class Tree {
    //CLASS INVARIANTS
    //  1. Subclass TreeState is immutable. All different instance have distinctive identifier.

    final private TreeStateFactory mStateFactory;
    final private TreeMap<TreeState, TreeMap<ICommand, TransitionInfo>> succesorTable = new TreeMap<TreeState, TreeMap<ICommand, TransitionInfo>>();
    final private TreeMap<TreeState, TransitionInfo> predecessorTable = new TreeMap<TreeState, TransitionInfo>();

    final public TreeState initState;



    public Tree(final AppState initAppState){
        mStateFactory = new TreeStateFactory(this);

        initState = createLiveState(initAppState.getViewInfo());
        visitState(initState);
    }

    public TreeState getCorrespondingTreeState(AppState state){
        List<ICommand> inputSequence = state.getHistory();
        return getCorrespondingTreeState(inputSequence);
    }

    public TreeState getCorrespondingTreeState(List<ICommand> sequence){
        TreeState cursor = initState;
        for(ICommand cmd : sequence){
            cursor = cursor.getSuccessor(cmd);
        }

        return cursor;
    }

    public void update(AppState start, CList<ICommand> inputSeq, List<CompressedLog> observationSeq, List<AppState> stateSeq, boolean isConflict){
        TreeState stateCursor = getCorrespondingTreeState(start);

        Iterator<ICommand>      inputIter = inputSeq.iterator();
        Iterator<CompressedLog> obsvIter  = observationSeq.iterator();
        Iterator<AppState>      stateIter = stateSeq.iterator();

        while(inputIter.hasNext()){
            if(!stateIter.hasNext()){
                if(isConflict) break;
                else throw new RuntimeException("Something is Wrong!");
            }

            ICommand input = inputIter.next();
            AppState state = stateIter.next();
            TreeMap<ICommand, TransitionInfo> map = succesorTable.get(stateCursor);
            TransitionInfo tinfo = map.get(input);

            if(tinfo.nextState.palette == null){
                remove(tinfo.nextState);
                if(state.isStop()){
                    if(!tinfo.isStop){
                        tinfo.isStop = true;
                        tinfo.nextState = createStopState();
                        predecessorTable.put(tinfo.nextState, tinfo);
                    }
                    break;
                }
                else{
                    CompressedLog observation = obsvIter.next();

                    TreeState newState = createLiveState(state.getViewInfo());
                    tinfo.log = observation;
                    tinfo.nextState = newState;
                    visitState(tinfo.nextState);
                    predecessorTable.put(tinfo.nextState, tinfo);
                }
            }
            else if(state.isStop()){
                throw new RuntimeException("Something is wrong");
            }

            stateCursor = tinfo.nextState;
        }
    }

    private TreeState createLiveState(ViewInfo viewInfo){
        TreeState newState = mStateFactory.createLiveState(viewInfo);
        succesorTable.put(newState, new TreeMap<ICommand, TransitionInfo>());

        return newState;
    }

    private TreeState createStopState(){
        TreeState newState = mStateFactory.createStopState();
        succesorTable.put(newState, new TreeMap<ICommand, TransitionInfo>());

        return newState;
    }

    private void remove(TreeState state){
        succesorTable.remove(state);
        predecessorTable.remove(state);
    }

    TreeState getNextState(TreeState state, ICommand cmd){
        return succesorTable.get(state).get(cmd).nextState;
    }

    TreeState getPredState(TreeState state){
        return predecessorTable.get(state).baseState;
    }

    TransitionInfo getTransitionFromPred(TreeState state){
        return predecessorTable.get(state);
    }

    private void visitState(TreeState state){
        for(ICommand cmd : state.palette){
            TreeState next = mStateFactory.createEmptyState();
            TransitionInfo ninfo = new TransitionInfo(state, next, cmd);

            succesorTable.put(next, new TreeMap<ICommand, TransitionInfo>());

            succesorTable.get(state).put(cmd, ninfo);
            predecessorTable.put(next, ninfo);
        }
    }

    public void writeTo(Writer writer){
        GraphPrinter<TreeState> printer = new GraphPrinter<TreeState>(initState);
        printer.printTo(writer);
    }
}


class TransitionInfo{
    TreeState nextState;
    TreeState baseState;
    ICommand input;
    CompressedLog log;

    boolean isStop;

    public TransitionInfo(TreeState b, TreeState n, ICommand input){
        this.nextState = n;
        this.baseState = b;
        this.input = input;
    }

    public String toString(){
        return "(" + baseState + ", " + input + ", " + nextState + ")";
    }
}


