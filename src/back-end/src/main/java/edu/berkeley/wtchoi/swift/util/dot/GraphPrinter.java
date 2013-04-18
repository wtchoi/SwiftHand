package edu.berkeley.wtchoi.swift.util.dot;

import edu.berkeley.wtchoi.collection.Pair;
import edu.berkeley.wtchoi.gv.GraphViz;
import edu.berkeley.wtchoi.swift.driver.ICommand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/13/13
 * Time: 1:13 AM
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
public class GraphPrinter<T extends DGVertex<ICommand,T>>{

    T root;

    boolean enumerateUnvisitedStates = false;
    boolean enumerateUnvisitedStateEdges = false;
    boolean mergeEdges = true;

    static String leafPrefix = "1567834223";

    public GraphPrinter(T root){
        this.root = root;
    }

    public void setEnumerateUnvisitedStates(boolean flag){
        enumerateUnvisitedStates = flag;
    }

    public void setEnumerateUnvisitedStateEdges(boolean flag){
        enumerateUnvisitedStateEdges = flag;
    }

    public void printTo(String filename){
        try{
            File file = new File(filename);
            FileWriter fw = new FileWriter(file);
            printTo(fw);
        }
        catch(IOException e){
            throw new RuntimeException("Something is wrong", e);
        }
    }

    public void printTo(final Writer writer){
        final GraphViz gv = new GraphViz();

        dumpDot(gv);

        try{
            writer.write(gv.start_graph());
            writer.write("\n");
            writer.write(gv.getDotSource());
            writer.write("\n");
            writer.write(gv.end_graph());
            writer.flush();
        }
        catch(IOException e){
            throw new RuntimeException("something is wrong", e);
        }
    }

    private void dumpDot(GraphViz gv){
        LinkedList<T> statesToVisit = new LinkedList<T>();
        Set<T> markedStates = new TreeSet<T>();

        statesToVisit.add(root);
        markedStates.add(root);

        while(!statesToVisit.isEmpty()){
            T cursor = statesToVisit.pollFirst();
            encodeStateToDot(cursor, gv, statesToVisit, markedStates);
        }
    }

    private void encodeStateToDot(T state, GraphViz gv, LinkedList<T> statesToVisit, Set<T> markedStates){

        String stateColor = null;
        switch(state.getObservationDegree()){
            case DGVertex.OBSERVED:
                stateColor = "black";
                break;
            case DGVertex.OBSERVING:
                stateColor = "blue";
                break;
            case DGVertex.VISITED:
                stateColor = "cyan";
                break;
            case DGVertex.UNVISITED:
                stateColor = "gray";
                break;
            case DGVertex.TERMINAL:
                stateColor = "black";
                break;
        }


        int frontierChildCount = 0;
        Set<ICommand> edgeToFrontier = new TreeSet<ICommand>();

        TreeMap<T, TreeMap<String, TreeSet<ICommand>>> edgeGroups = new TreeMap<T, TreeMap<String, TreeSet<ICommand>>>();

        for(Pair<ICommand, T> edge : state.getOutgoingEdges()){

            ICommand cmd = edge.fst;
            T child = edge.snd;

            if(child.isVisible()){
                String edgeColor = null;
                switch (child.getObservationDegree()){
                    case DGVertex.OBSERVED:
                        edgeColor = "black";
                        break;
                    case DGVertex.OBSERVING:
                        edgeColor = "black";
                        break;
                    case DGVertex.VISITED:
                        edgeColor = "black";
                        break;
                    case DGVertex.UNVISITED:
                        if(child.isVeryInteresting()){
                            edgeColor = "gray";
                        }
                        else if(enumerateUnvisitedStates){
                            edgeColor = "gray";
                        }
                        else{
                            frontierChildCount++;
                            edgeToFrontier.add(cmd);
                            continue;
                        }
                        break;
                    case DGVertex.TERMINAL:
                        edgeColor = "black";
                        break;
                }

                if(mergeEdges){
                    if(!edgeGroups.containsKey(child)) edgeGroups.put(child, new TreeMap<String, TreeSet<ICommand>>());
                    TreeMap<String, TreeSet<ICommand>> innerGroups = edgeGroups.get(child);

                    if(!innerGroups.containsKey(edgeColor)) innerGroups.put(edgeColor, new TreeSet<ICommand>());
                    TreeSet<ICommand> group = innerGroups.get(edgeColor);
                    group.add(cmd);
                }
                else{
                    gv.addln(state.getId() + " -> " + child.getId() + " [label=\"" + cmd + "\", color="+ edgeColor + "];");
                }

                if(!markedStates.contains(child)){
                    statesToVisit.addLast(child);
                    markedStates.add(child);
                }
            }
        }

        if(mergeEdges){
            for(Map.Entry<T,TreeMap<String,TreeSet<ICommand>>> innerGroupEntry : edgeGroups.entrySet()){
                for(Map.Entry<String, TreeSet<ICommand>> groupEntry : innerGroupEntry.getValue().entrySet()){
                    StringBuilder cmds = new StringBuilder();
                    boolean flag = false;
                    for(ICommand cmd : groupEntry.getValue()){
                        if(flag) cmds.append("\\n");
                        else flag = true;
                        cmds.append(cmd);
                    }
                    gv.addln(state.getId() + " -> " + innerGroupEntry.getKey().getId() + " [label=\"" + cmds + "\", color="+ groupEntry.getKey() + ", fontsize=10];");
                }
            }
        }


        if(!enumerateUnvisitedStates){
            if(frontierChildCount > 0){
                if(enumerateUnvisitedStateEdges){
                    StringBuilder content = new StringBuilder();

                    boolean flag = false;
                    for(ICommand cmd:edgeToFrontier){
                        if(flag) content.append("\\n");
                        else flag = true;
                        content.append(cmd);
                    }
                    gv.addln(state.getId()+ leafPrefix +  " [label=\"" + content.toString() + "\", fontsize=10, fontcolor=gray, color=gray, shape=rectangle];");
                    gv.addln(state.getId() + " -> " +  state.getId() + leafPrefix + " [color=gray];");
                }
            }
        }

        String id = String.valueOf(state.getId());
        if(frontierChildCount > 0 && !enumerateUnvisitedStateEdges && !enumerateUnvisitedStates){
            id = id + " [" + frontierChildCount + "]";
        }

        String shape = state.getObservationDegree() == DGVertex.TERMINAL ? ", shape=square":", shape=circle";
        if(state.isInteresting())
            gv.addln(id + "[color="+ stateColor + shape + ", style=bold, peripheries=2];");
        else if(state.isVeryInteresting())
            gv.addln(id + "[color="+ stateColor + shape + ", style=bold, peripheries=4];");
        else
            gv.addln(id + "[color=" + stateColor + shape + "];");
    }
}
