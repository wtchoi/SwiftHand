package edu.berkeley.wtchoi.swift.testing.android.lstar.ctree;

import edu.berkeley.wtchoi.gv.GraphViz;
import edu.berkeley.wtchoi.swift.driver.ICommand;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/26/12
 * Time: 4:18 PM
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
class DotEncoder{
    private GraphViz gv;
    private TreeMap<CNode, TreeSet<ICommand>> remainedInputs;
    private final static String remainderPostfix = "913867345824";

    public GraphViz encode(CTree tree){
        gv = new GraphViz();
        remainedInputs = new TreeMap<CNode, TreeSet<ICommand>>();

        gv.addln(gv.start_graph());
        drawTree(tree.root);
        drawRemainedInputs();
        gv.addln(gv.end_graph());
        return gv;
    }

    protected void drawNode(CNode n){
        String id1 = String.valueOf(n.id);

        if(!n.children.isEmpty()){
            gv.addln(id1+" [style = bold, shape = circle, color="+n.color+"];");
        }

        //if(!n.children.isEmpty())
        //    gv.addln(id1+" [style = bold, shape = circle, color="+n.color+"];");
        ////else if (!n.isStopNode){
        ////   gv.addln(id1+" [shape = point, color=gray];");
        ////}
    }

    protected void drawEdgeToChild(CNode n, ICommand i, CNode child){
        String id1 = String.valueOf(n.id);
        String id2 = String.valueOf(child.id);
        if(child.isLeafNode()){
            if(!remainedInputs.containsKey(n)){
                remainedInputs.put(n, new TreeSet<ICommand>());
            }
            remainedInputs.get(n).add(i);
        }
        else{

            if(child.isMerged()){
                resolveMerge(id1, i, child, !child.permanentlyMerged);
            }
            else{
                if(child.isStopNode) return;
                if(child.tiFromParent.didNothing()){
                    gv.addln(id1 + "->" + id2 + "[style=bold, color=blue, label=\""+ i+"\"];");
                }
                else{
                    gv.addln(id1 + "->" + id2 + "[style=bold, label=\""+ i+"\"];");
                }
            }
            /*
            if(child.isStopNode) return;
            if(child.tiFromParent != null && child.tiFromParent.didNothing()){
                gv.addln(id1 + "->" + id2 + "[style=bold, color=blue, label=\""+ i+"\"];");
            }
            else{
                gv.addln(id1 + "->" + id2 + "[style=bold, label=\""+ i+"\"];");
            }
            if(child.isMerged()){
                resolveMerge2(id2, child.mergeTo, i);
            }
            */
        }
    }

    private void resolveMerge2(String id, CNode target, ICommand i){
        if(target.isMerged()) resolveMerge2(id, target.mergeTo, i);
        else
            gv.addln(id + "->" + target.id + "[color = green, fontsize=12, label=\""+ i+"\"];");
    }

    private void resolveMerge(String parentID, ICommand i, CNode target, boolean isTemporalMerge){
        if(target.mergeTo.isMerged())
            resolveMerge(parentID, i, target.mergeTo, isTemporalMerge && target.mergeTo.permanentlyMerged);
        else{
            if(isTemporalMerge){
                gv.addln(parentID + "->" + target.mergeTo.id + "[color = green, fontsize=12, label=\""+ i+"\"];");
            }
            else if(target.tiFromParent.didNothing())
                gv.addln(parentID + "->" + target.mergeTo.id + "[color = blue, fontsize=12, label=\""+ i+"\"];");
            else
                gv.addln(parentID + "->" + target.mergeTo.id + "[label=\""+ i+"\"];");

        }
    }

    protected void drawTree(CNode n){
        //String id1 = String.valueOf(n.id);
        drawNode(n);

        for(ICommand i: n.children.keySet()){
            CNode child =n.children.get(i).fst;
            drawEdgeToChild(n,i,child);
            if(child.isMerged()) continue;
            drawTree(child);
        }
    }

    protected void drawRemainedInputs(){
        for(CNode node:remainedInputs.keySet()){
            String dummyID = node.id + remainderPostfix;
            if(node.color.equals("blue")){
                gv.addln(node.id + "->" + dummyID + " [color=gray];");
                StringBuilder label = new StringBuilder();
                boolean flag = false;
                for(ICommand cmd : remainedInputs.get(node)){
                    if(flag) label.append("\\n");
                    else flag = true;
                    label.append(cmd);
                }
                gv.addln(dummyID + " [shape=rectangle, color=gray, fontsize=11, fontcolor=gray, label = \"" + label + "\"];");
            }
            else{
                int size = node.children.size();
                gv.addln(node.id + "->" + dummyID + " [color=gray, fontcolor=gray, fontsize=11, label = <" + size + ">];");
                gv.addln(dummyID + " [shape=point, color=gray];");
            }
        }
    }
}
