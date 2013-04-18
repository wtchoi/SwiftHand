package edu.berkeley.wtchoi.swift.testing.android.learning.model;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.util.Predicate;
import edu.berkeley.wtchoi.swift.util.dot.DGVertex;
import edu.berkeley.wtchoi.collection.CPair;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.ExtendedComparable;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/26/13
 * Time: 9:36 PM
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
public abstract class State implements ExtendedComparable<State>, DGVertex<ICommand, State> {
    public boolean isYetToVisit = true;
    public boolean isVisible = true;
    public boolean isInteresting = false;
    public boolean isVeryInteresting = false;

    public CSet<ICommand> output = null;
    public TreeMap<ICommand, State> transition;

    public int id;

    Model model;

    protected State(){}

    public List<ICommand> path(){
        return pathFrom(model.root);
    }

    public <T extends Collection<ICommand>> T path(T collection){
        collection.addAll(path());
        return collection;
    }

    public List<ICommand> pathFrom(State start){
        assert(model == start.model);

        LinkedList<State> queue = new LinkedList<State>();
        TreeMap<State, CPair<State, ICommand>> parent = new TreeMap<State, CPair<State, ICommand>>();
        TreeSet<State> considered = new TreeSet<State>();
        queue.add(start);
        considered.add(start);

        State cursor;
        while(true){
            cursor = queue.removeFirst();
            if(cursor.id == this.id) break;
            if(cursor.isYetToVisit) continue;

            InnerLoop:
            for(ICommand cmd:cursor.output){
                State child = cursor.transition.get(cmd);
                if(considered.contains(child)) continue InnerLoop;
                considered.add(child);
                parent.put(child, new CPair<State,ICommand>(cursor, cmd));
                queue.add(child);
            }
        }

        LinkedList<ICommand> list = new LinkedList<ICommand>();
        while(cursor.id != start.id){
            CPair<State,ICommand> pair = parent.get(cursor);
            list.addFirst(pair.getSecond());
            cursor = pair.getFirst();
        }
        return list;
    }

    public <T extends Collection<ICommand>> T pathFrom(State start, T collection){
        assert(start.model == this.model);

        collection.addAll(pathFrom(start));
        return collection;
    }


    public int compareTo(State state){
        assert(state.model == model);
        return (new Integer(id)).compareTo(new Integer(state.id));
    }

    public boolean equalsTo(State state){
        assert(state.model == model);
        return id == state.id;
    }

    public boolean isRoot(Model context){
        return context.getRoot() == this;
    }

    public Set<State> getChildren(){
        TreeSet<State> children = new TreeSet<State>();
        if(transition != null){
            for(Map.Entry<ICommand, State> entry : transition.entrySet()){
                children.add(entry.getValue());
            }
        }
        return children;
    }

    public State getNext(ICommand cmd){
        if(transition == null)
            throw new NoSuchElementException();

        State state = transition.get(cmd);
        if(state == null)
            throw new NoSuchElementException();

        return state;
    }

    public boolean hasNext(ICommand cmd){
        if(transition == null) return false;
        if(!transition.containsKey(cmd)) return false;
        return true;
    }

    public boolean isYetToVisit(){
        return isYetToVisit;
    }

    public boolean isFrontier(){
        return getModel().frontiers.contains(this);
    }

    public Model getModel(){
        return model;
    }

    public void setOutput(CSet<ICommand> enabledInputs){
        output = enabledInputs;
        isVisible = enabledInputs.size() > 0;
    }

    //DGVertex interfaces
    public int getId(){
        return id;
    }

    public Collection<Pair<ICommand,State>> getOutgoingEdges(){
        LinkedList<Pair<ICommand, State>> collection = new LinkedList<Pair<ICommand, State>>();
        if(!this.isYetToVisit){
            for(Map.Entry<ICommand,State> entry: transition.entrySet()){
                collection.add(new Pair<ICommand, State>(entry.getKey(), entry.getValue()));
            }
        }
        return collection;
    }

    public boolean isVisible(){
        return isVisible;
    }

    public int getObservationDegree(){
        if(output == null)
            return UNVISITED;

        if(isFrontier())
            return OBSERVING;

        return OBSERVED;
    }

    @Override
    public boolean isInteresting(){
        return isInteresting;
    }

    @Override
    public boolean isVeryInteresting(){
        return isVeryInteresting;
    }

    public String toString(){
        return "S" + String.valueOf(id);
    }

    public Set<CPair<State, ICommand>> getParents(){
        return model.parentTable.get(this);
    }

    public void setInteresting(boolean f){
        isInteresting = f;
    }

    public void setVeryInteresting(boolean f){
        isVeryInteresting = f;
    }

    public boolean isReachableTo(Predicate<State> predicate){
        LinkedList<State> queue = new LinkedList<State>();
        TreeSet<State> considered = new TreeSet<State>();
        queue.add(this);
        considered.add(this);

        while(!queue.isEmpty()){
            State state = queue.removeFirst();

            for(State child: state.getChildren()){
                if(considered.contains(child)) continue;
                if(predicate.check(child)) return true;
                considered.add(child);
                queue.add(child);
            }
        }

        return false;
    }
}
