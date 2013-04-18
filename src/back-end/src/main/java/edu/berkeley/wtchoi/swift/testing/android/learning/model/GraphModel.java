package edu.berkeley.wtchoi.swift.testing.android.learning.model;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.collection.CPair;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/26/13
 * Time: 9:28 PM
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
public class GraphModel extends Model{

    public State createState(){
        return new GraphState();
    }

    public GraphModel(Model model){
        final LinkedList<CPair<State, State>> queue = new LinkedList<CPair<State, State>>();

        root = createState();
        states.add(root);
        statesYetToVisit.add(root);
        parentTable.put(root, new TreeSet<CPair<State, ICommand>>());

        //new model state * source model state
        queue.add(new CPair<State,State>(root, model.root));

        //Invariant: items in queue are not visited
        while(!queue.isEmpty()){
            CPair<State,State> pair = queue.removeFirst();
            GraphState state  = (GraphState) pair.getFirst();
            State mstate = pair.getSecond();

            try{
                visitState(state, mstate.output);
                state.addSource(mstate);
            }
            catch(InconsistencyException ignore){}
            //Inconsistency cannot happen this case

            for(ICommand cmd: mstate.output){
                if(mstate.getNext(cmd).isYetToVisit) continue;
                State child = state.transition.get(cmd);
                State mchild = mstate.getNext(cmd);
                queue.add(new CPair<State,State>(child, mchild));
            }
        }

        lastUpdate = System.currentTimeMillis();
    }

    public void merge(State red, State blue){
        merge(red, blue, null);
    }


    //We assume either (1)blueRoot isYetToVisit or (2)blue fringe invariant: sub-graph rooted from blueRoot is a lstar
    public void merge(State redRoot, State blueRoot, LinkedList<Pair<Integer, Integer>> record){

        for(CPair<State,ICommand> pair : parentTable.get(blueRoot)){
            State parent = pair.getFirst();
            ICommand cmd = pair.getSecond();
            parent.transition.put(cmd, redRoot);
            tryClose(parent);
        }

        try{
            parentTable.get(redRoot).addAll(parentTable.get(blueRoot));
        }
        catch(Exception e){
            e.printStackTrace();
            int x = 1;
        }

        if(blueRoot.isYetToVisit){
            removeYetToVisited(blueRoot);
            return;
        }

        mergeDFS(redRoot, blueRoot, record);
        lastUpdate = System.currentTimeMillis();
    }

    private void mergeDFS(State redRoot, State blueRoot, LinkedList<Pair<Integer, Integer>> record){
        LinkedList<CPair<State,State>> mergeStack = new LinkedList<CPair<State, State>>();
        mergeStack.add(new CPair(redRoot,blueRoot));

        while(!mergeStack.isEmpty()){
            CPair<State, State> sp = mergeStack.removeLast();
            State red = sp.getFirst();
            State blue = sp.getSecond();

            if(record != null)
                record.add(new Pair<Integer, Integer>(red.id, blue.id));

            LinkedList<CPair<State,State>> nextLevel = new LinkedList<CPair<State,State>>();
            for(ICommand cmd: blue.output){
                State blueChild = blue.getNext(cmd);
                State redChild = red.getNext(cmd);
                if(!blueChild.isYetToVisit){
                    if(redChild.isYetToVisit){
                        removeYetToVisited(redChild);
                        red.transition.put(cmd, blueChild);
                        parentTable.get(blueChild).clear();
                        parentTable.get(blueChild).add(new CPair<State, ICommand>(red, cmd));
                    }
                    else{
                        nextLevel.addFirst(new CPair<State, State>(redChild, blueChild));
                    }
                }
                else
                    removeYetToVisited(blueChild);
            }
            mergeStack.addAll(nextLevel);


            removeVisited(blue);
            tryClose(red);

            ((GraphState) red).addAllSources(((GraphState) blue).sourceStates);
        }
    }

    private void mergeBFS(State redRoot, State blueRoot){
        LinkedList<CPair<State,State>> mergeQueue = new LinkedList<CPair<State, State>>();
        mergeQueue.add(new CPair(redRoot,blueRoot));


        while(!mergeQueue.isEmpty()){
            CPair<State,State> sp = mergeQueue.removeFirst();
            State red = sp.getFirst();
            State blue = sp.getSecond();

            for(ICommand cmd: blue.output){
                State blueChild = blue.getNext(cmd);
                State redChild = red.getNext(cmd);
                if(!blueChild.isYetToVisit){
                    if(redChild.isYetToVisit){
                        removeYetToVisited(redChild);
                        red.transition.put(cmd, blueChild);
                        parentTable.get(blueChild).clear();
                        parentTable.get(blueChild).add(new CPair<State, ICommand>(red, cmd));
                    }
                    else{
                        //Assume : blue child has only one parent
                        //red.transition.put(cmd, redChild);
                        mergeQueue.add(new CPair<State,State>(redChild, blueChild));
                    }
                }
                else
                    removeYetToVisited(blueChild);
            }

            removeVisited(blue);
            tryClose(red);

            ((GraphState) red).addAllSources(((GraphState) blue).sourceStates);
        }
    }

    protected void removeVisited(State state){
        states.remove(state);
        parentTable.remove(state);

        screenToStates.get(state.output).remove(state);
        if(frontiers.contains(state))
            frontiers.remove(state);
    }

    protected void removeYetToVisited(State state){
        states.remove(state);
        statesYetToVisit.remove(state);
        state.isYetToVisit = false;
        parentTable.remove(state);
    }

    int stateIdentifier = 1;

    public class GraphState extends State{
        public HashSet<State> sourceStates;

        public GraphState(){
            id = stateIdentifier++;
            model = GraphModel.this;
        }

        public void addSource(State state){
            if(sourceStates == null)
                sourceStates = new HashSet<State>();

            sourceStates.add(state);
        }

        public void addAllSources(Collection<State> s){
            sourceStates.addAll(s);
        }
    }
}
