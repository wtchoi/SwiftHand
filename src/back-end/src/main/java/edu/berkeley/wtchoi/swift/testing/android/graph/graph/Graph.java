package edu.berkeley.wtchoi.swift.testing.android.graph.graph;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.util.dot.GraphPrinter;
import edu.berkeley.wtchoi.swift.testing.android.graph.tree.Tree;
import edu.berkeley.wtchoi.swift.testing.android.graph.tree.TreeState;
import edu.berkeley.wtchoi.collection.CList;

import java.io.Writer;
import java.util.*;

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
public class Graph {

    public Graph(Tree tree){
        this.mTree = tree;
        mStateFactory = new GraphStateFactory(this);

        abstraction = new Abstraction(this, mStateFactory);
        registerInitState();
    }

    private void registerInitState(){
        this.initState = abstraction.abst(mTree.initState, mTree);
        this.initState.depth = 0;
        visitState(this.initState);
    }

    public boolean getTransition(GraphState from, GraphState to, CList<ICommand> inputVector){
        if(from.compareTo(to) == 0) return true;

        Set<GraphState> currentIteration = new TreeSet<GraphState>();
        Map<GraphState, List<ICommand>> pathTo = new TreeMap<GraphState, List<ICommand>>();

        currentIteration.add(from);
        pathTo.put(from, new LinkedList<ICommand>());

        while(true){
            Set<GraphState> nextIteration = new TreeSet<GraphState>();

            MID_LOOP:
            for(GraphState state: currentIteration){
                TreeMap<ICommand, GraphState> children = succTable.get(state);
                if(children == null) continue MID_LOOP;

                INNER_LOOP:
                for(Map.Entry<ICommand, GraphState> entry:children.entrySet()){
                    GraphState child = entry.getValue();

                    //If child is already visited, skip
                    if(pathTo.containsKey(child)) continue INNER_LOOP;

                    //Construct path to the child
                    LinkedList<ICommand> path = new LinkedList<ICommand>();
                    path.addAll(pathTo.get(state));
                    path.add(entry.getKey());


                    //If child is the target, return path to the child.
                    if(child.compareTo(to) == 0){
                        if(inputVector != null) inputVector.addAll(path);
                        return true;
                    }

                    //If child is not the target, add child to a next iteration set
                    nextIteration.add(child);
                    pathTo.put(child, path);
                }
            }

            if(nextIteration.isEmpty()) return false;

            //switch to next iteration
            currentIteration = nextIteration;
        }
    }

    public boolean checkTransitivity(GraphState from, GraphState to){
        //TODO: switch to efficient implementation
        return getTransition(from, to, null);
    }

    public GraphState getInitState(){
        return initState;
    }

    public GraphState getCorrespondingGraphState(TreeState treeState){

        return abstraction.abst(treeState, mTree);
    }

    public Set<GraphState> getFrontiers(){
        return new TreeSet<GraphState>(frontiers);
    }

    public boolean hasFrontier(){
        return !frontiers.isEmpty();
    }

    public void update(TreeState baseTreeState, List<ICommand> inputSeq, boolean isConflict){
        if(isConflict || checkConflict(baseTreeState, inputSeq)){
            updateConflict(baseTreeState, inputSeq);
        }
        else{
            updateNormal(baseTreeState, inputSeq);
        }
    }

    private boolean checkConflict(TreeState baseTreeState, List<ICommand> inputSeq){
        TreeState treeCursor = baseTreeState;
        GraphState graphCursor = abstraction.abst(treeCursor, mTree);

        for(ICommand cmd: inputSeq){
            treeCursor = treeCursor.getSuccessor(cmd);
            graphCursor = graphCursor.getSuccessor(cmd);
            if(abstraction.abst(treeCursor, mTree).compareTo(graphCursor) != 0 && graphCursor.isVisited())
                return true;
        }

        return false;
    }

    private void updateNormal(TreeState baseTreeState, List<ICommand> inputSeq){
        GraphState baseGraphState = abstraction.abst(baseTreeState, mTree);

        for(ICommand cmd:inputSeq){
            TreeState nextTreeState = baseTreeState.getSuccessor(cmd);
            GraphState nextGraphState = abstraction.abst(nextTreeState, mTree);

            connect(baseGraphState, nextGraphState, nextTreeState, cmd);
            baseGraphState = nextGraphState;
            baseTreeState = nextTreeState;

            if(nextGraphState.id == 109){
                int x = 1;
            }
        }
    }

    private void updateConflict(TreeState baseTreeState, List<ICommand> inputSeq){
        List<ICommand> fullpath = baseTreeState.getPath();
        fullpath.addAll(inputSeq);
        TreeState finalState = mTree.getCorrespondingTreeState(fullpath);

        abstraction.refineByCounterExample(fullpath, finalState, mTree);
        frontiers.clear();
        succTable.clear();
        predTable.clear();

        registerInitState();

        LinkedList<TreeState> frontier = new LinkedList<TreeState>();
        frontier.add(mTree.initState);

        while(true){
            if(frontier.isEmpty()) break;
            TreeState state = frontier.pollFirst();
            if(state.palette == null){
                int x = 1;
            }
            GraphState gState = abstraction.abst(state, mTree);

            for(ICommand cmd : state.palette){
                TreeState child = state.getSuccessor(cmd);
                if(child.palette != null){
                    GraphState gChild = abstraction.abst(child, mTree);
                    connect(gState, gChild, child, cmd);
                    frontier.addLast(child);
                }
            }
        }

        int x = 1;
    }

    private void connect(GraphState base, GraphState next, TreeState nextTreeState, ICommand cmd){
        GraphState cur = null;
        TreeMap<ICommand, GraphState> map = succTable.get(base);
        cur = map.get(cmd);

        if(cur == null){
            int x = 1;
        }

        if(cur.palette == null){
            //TODO: exact depth calculation
            if (next.depth == -1){
                next.depth = base.depth + 1;
            }
            visitState(next);

            succTable.get(base).put(cmd, next);
            predTable.get(next).put(cmd, base);

            predTable.get(cur).remove(cmd);
            predTable.remove(cur);
            frontiers.remove(cur);
        }
        else if(cur.compareTo(next) != 0){
            throw new RuntimeException("something is wrong!");
        }


    }

    private void visitState(GraphState state){
        if(!succTable.containsKey(state)){
            succTable.put(state, new TreeMap<ICommand, GraphState>());
            predTable.put(state, new TreeMap<ICommand, GraphState>());

            for(ICommand cmd:state.palette){
                GraphState n = mStateFactory.createEmptyState();
                n.depth = state.depth + 1;
                succTable.get(state).put(cmd, n);
                predTable.put(n, new TreeMap<ICommand, GraphState>());
                predTable.get(n).put(cmd, state);
                frontiers.add(n);
            }
        }
    }

    public GraphState getSuccessor(GraphState state, ICommand cmd){
        return succTable.get(state).get(cmd);
    }

    public void writeTo(Writer writer){
        GraphPrinter<GraphState> printer = new GraphPrinter<GraphState>(initState);
        //printer.setEnumerateUnvisitedStates(true);
        printer.setEnumerateUnvisitedStateEdges(true);

        printer.printTo(writer);
    }

    final Tree mTree;
    GraphState initState;

    final Set<GraphState> frontiers = new TreeSet<GraphState>();
    final Map<GraphState, TreeMap<ICommand, GraphState>> succTable = new TreeMap<GraphState, TreeMap<ICommand, GraphState>>();
    final Map<GraphState, TreeMap<ICommand, GraphState>> predTable = new TreeMap<GraphState, TreeMap<ICommand, GraphState>>();

    final private GraphStateFactory mStateFactory;

    Abstraction abstraction;
}

