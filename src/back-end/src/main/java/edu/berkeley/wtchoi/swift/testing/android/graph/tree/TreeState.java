package edu.berkeley.wtchoi.swift.testing.android.graph.tree;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.util.dot.DGVertex;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/11/13
 * Time: 10:34 PM
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
public class TreeState implements DGVertex<ICommand, TreeState>{
    public final Set<ICommand> palette;
    public final int id;
    public final boolean isStop;

    private final Tree tree;

    public TreeState(int id, Tree tree){
        this.id = id;
        this.isStop = false;
        this.palette = null;
        this.tree = tree;
    }

    public TreeState(int id, boolean isStop, Tree tree){
        this.id = id;
        this.isStop = isStop;
        this.palette = null;
        this.tree = tree;
    }

    public TreeState(int id, Set<ICommand> palette, Tree tree){
        this.id = id;
        this.palette = palette;
        this.isStop = false;
        this.tree = tree;
    }

    public int compareTo(TreeState s){
        return Integer.valueOf(id).compareTo(s.id);
    }

    public TreeState getPredecessor(){
        return tree.getPredState(this);
    }

    public TreeState getSuccessor(ICommand cmd){
        return tree.getNextState(this, cmd);
    }

    public boolean isRootState(){
        return this.compareTo(tree.initState) == 0;
    }

    public boolean isVisited(){
        return ((palette != null) || isStop);
    }

    public boolean isVeryInteresting(){
        return false;
    }

    public String toString(){
        return "T" + id;
    }

    public LinkedList<ICommand> getPath(){
        return getPath(LinkedList.class);
    }

    public <T extends List<ICommand>> T getPath(Class<T> cls){
        try{
            if(this.compareTo(tree.initState) == 0) return cls.newInstance();

            T pathToPredecessor = this.getPredecessor().getPath(cls);
            pathToPredecessor.add(tree.getTransitionFromPred(this).input);
            return pathToPredecessor;
        }
        catch(Exception e){
            return null;
        }
    }

    //DGVertex methods
    @Override
    public int getId(){
        return id;
    }

    @Override
    public Collection<Pair<ICommand, TreeState>> getOutgoingEdges(){
        LinkedList<Pair<ICommand, TreeState>> edges = new LinkedList<Pair<ICommand, TreeState>>();
        if(palette == null) return edges;

        for(ICommand cmd:palette){
            edges.add(new Pair<ICommand, TreeState>(cmd, this.getSuccessor(cmd)));
        }
        return edges;
    }

    @Override
    public boolean isVisible(){
        return !isStop;
    }

    @Override
    public int getObservationDegree(){
        if(palette == null && !isStop) return UNVISITED;
        return OBSERVED;
    }

    @Override
    public boolean isInteresting(){
        return false;
    }
}
