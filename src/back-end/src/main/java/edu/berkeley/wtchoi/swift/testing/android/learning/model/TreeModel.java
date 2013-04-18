package edu.berkeley.wtchoi.swift.testing.android.learning.model;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.collection.CPair;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.swift.util.dot.DGVertex;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/26/13
 * Time: 6:49 PM
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
public class TreeModel extends Model{

    public State createState(){
        return new TreeState();
    }

    public TreeModel(CSet<ICommand> enabledInputs){
        root = createState();
        states.add(root);
        statesYetToVisit.add(root);
        parentTable.put(root, new TreeSet<CPair<State, ICommand>>());
        try{
            visitState(root, enabledInputs);
        }
        catch(InconsistencyException ignore){}
        //Inconsistency Cannot happen this case

        lastUpdate = System.currentTimeMillis();
    }


    public void addTrace(List<ICommand> pathToBase, List<CPair<ICommand, CSet<ICommand>>> trace) throws InconsistencyException{
        State cursor = root;
        for(ICommand cmd : pathToBase)
            cursor = cursor.getNext(cmd);


        for(CPair<ICommand, CSet<ICommand>> pair:trace){
            ICommand cmd = pair.getFirst();
            CSet<ICommand> palette = pair.getSecond();
            cursor = cursor.transition.get(cmd);
            visitState(cursor, palette);
            if(cursor.id == 311){
                int x = 1;
            }
            else if (cursor.id == 1043){
                int x = 1;
            }
        }

        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void visitState(State state, CSet<ICommand> enabledInputs) throws InconsistencyException{
        super.visitState(state, enabledInputs);

        for(State child:state.getChildren()){
            ((TreeState) child).depth = ((TreeState) state).depth + 1;
            ((TreeState) child).parent = (TreeState) state;
        }

        TreeState cursor = ((TreeState) state).parent;
        while(cursor != null){
            if(cursor.isFrontier()) break;

            int minRadius = 100000;
            for(State child : cursor.getChildren()){
                if(((TreeState)child).observationRadius < minRadius)
                    minRadius = ((TreeState) child).observationRadius + 1;
            }
            if(cursor.observationRadius == minRadius) break;
            cursor.observationRadius = minRadius;
            cursor = cursor.parent;
        }
    }

    protected static LinkedList<ICommand> dummyPath = new LinkedList<ICommand>();
    public void addTrace(List<CPair<ICommand, CSet<ICommand>>> trace) throws InconsistencyException{
        addTrace(dummyPath, trace);
    }

    int stateIdentifier = 1;
    public class TreeState extends State{
        public int depth = 0;
        public int observationRadius = 0;
        public TreeState parent;

        public TreeState(){
            id = stateIdentifier++;
            model = TreeModel.this;
        }

        @Override
        public int getObservationDegree(){
            if(isYetToVisit)
                return DGVertex.UNVISITED;

            if(output.size() == 0)
                return DGVertex.TERMINAL;

            if(isFrontier())
                return DGVertex.OBSERVING;

            return DGVertex.OBSERVED;
        }

        @Override
        public void setOutput(CSet<ICommand> enabledInputs){
            super.setOutput(enabledInputs);
            isVisible = true;
        }
    }


}



