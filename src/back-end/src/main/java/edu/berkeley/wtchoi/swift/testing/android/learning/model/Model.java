package edu.berkeley.wtchoi.swift.testing.android.learning.model;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.util.dot.GraphPrinter;
import edu.berkeley.wtchoi.collection.CPair;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.Pair;

import java.io.Writer;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/28/13
 * Time: 11:14 PM
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

public abstract class Model {
    protected State root;
    //final protected Map<State, Map<ICommand, State>> transitionTable = new TreeMap<State, Map<ICommand, State>>();
    final protected Map<State, Set<CPair<State, ICommand>>> parentTable = new TreeMap<State, Set<CPair<State, ICommand>>>();
    final protected Map<CSet<ICommand>, TreeSet<State>> screenToStates = new TreeMap<CSet<ICommand>, TreeSet<State>>();
    final protected Set<State> statesYetToVisit = new TreeSet<State>();
    final protected Set<State> frontiers = new TreeSet<State>();
    final protected Set<State> states = new TreeSet<State>();

    long lastUpdate;
    long lastDump;


    public abstract State createState();


    public boolean isClosed(){
        return frontiers.isEmpty();
    }

    public void forceVisitState(State state, CSet<ICommand> enabledInputs){
        assert(state.getModel() == this);

        try{
            visitState(state, enabledInputs);
        }
        catch(InconsistencyException e){
            throw new RuntimeException("WHAT!!!??!", e);
        }
    }

    protected void visitState(State state, CSet<ICommand> enabledInputs) throws InconsistencyException{
        if(state == null){
            int x = 1;
        }

        if(!state.isYetToVisit){
            if(enabledInputs.compareTo(state.output) != 0){
                CSet<ICommand> stateOutput = state.output;
                throw new InconsistencyException(state.path(),  stateOutput, enabledInputs);
            }
            else
                return;
        }

        state.setOutput(enabledInputs);

        state.transition = new TreeMap<ICommand, State>();
        statesYetToVisit.remove(state);
        state.isYetToVisit = false;
        frontiers.add(state);

        if(!screenToStates.containsKey(enabledInputs))
            screenToStates.put(enabledInputs, new TreeSet<State>());
        screenToStates.get(enabledInputs).add(state);

        for(ICommand cmd:enabledInputs){
            State child = createState();
            parentTable.put(child, new TreeSet<CPair<State, ICommand>>());
            parentTable.get(child).add(new CPair<State, ICommand>(state, cmd));
            state.transition.put(cmd, child);
            states.add(child);
            statesYetToVisit.add(child);
        }

        tryClose(state);
        for(Pair<State,ICommand> pair :state.getParents()){
            tryClose(pair.fst);
        }
    }
    
    protected void tryClose(State state){
        if(state.isFrontier()){
            for(State child: state.getChildren()){
                if(child.isYetToVisit) return;
            }
            frontiers.remove(state);
        }
    }

    public Set<State> getStatesYetToVisit(){
        TreeSet<State> set = new TreeSet<State>();
        set.addAll(statesYetToVisit);
        return set;
    }

    public Set<State> getStatesWithScreen(CSet<ICommand> enabledInputs){
        TreeSet<State> set = new TreeSet<State>();
        if(screenToStates.containsKey(enabledInputs))
            set.addAll(screenToStates.get(enabledInputs));

        return set;
    }

    public Set<State> getFrontiers(){
        TreeSet<State> set = new TreeSet<State>();
        set.addAll(frontiers);
        return set;
    }

    public State getState(List<ICommand> inputSequence){
        State cursor = root;
        for(ICommand cmd: inputSequence){
            try{
                cursor = cursor.getNext(cmd);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }

            if(cursor == null)
                throw new NoSuchElementException();
        }
        return cursor;
    }

    public State getRoot(){
        return root;
    }

    public Set<State> getStates(){
        Set<State> states = new TreeSet<State>();
        states.addAll(this.states);
        return states;
    }

    public void writeTo(Writer writer, boolean treemode){
        if(lastUpdate <= lastDump) return;
        forceWriteTo(writer, treemode);
        lastDump = System.currentTimeMillis();
    }

    public void forceWriteTo(Writer writer, boolean treemode){
        GraphPrinter<State> printer = new GraphPrinter<State>(root);
        if(treemode){
            printer.setEnumerateUnvisitedStates(false);
        }
        else{
            printer.setEnumerateUnvisitedStateEdges(true);
        }

        printer.printTo(writer);
    }
}

