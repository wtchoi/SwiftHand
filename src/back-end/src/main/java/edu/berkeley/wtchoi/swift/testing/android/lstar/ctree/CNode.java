package edu.berkeley.wtchoi.swift.testing.android.lstar.ctree;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.testing.android.lstar.Observation;
import edu.berkeley.wtchoi.swift.testing.android.lstar.TransitionInfo;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.ExtendedComparable;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/20/12
 * Time: 10:16 PM
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
class CNode implements ExtendedComparable<CNode> {
    protected static Integer nidset = 0;

    final Integer id;
    CTree tree;

    CNode parent;
    ICommand inputFromParent;
    TransitionInfo tiFromParent;
    Map<ICommand,Pair<CNode,Observation>> children;

    String color = "gray";
    Integer depth;
    CSet<ICommand> palette;
    boolean isStopNode = false;

    CNode mergeTo;
    boolean permanentlyMerged = false;
    Collection<CNode> descendantLeaves;


    public CNode(CTree tree){
        children = new TreeMap<ICommand,Pair<CNode,Observation>>();
        id = nidset++;
        this.tree = tree;
    }

    public int compareTo(CNode target){
        int f = depth.compareTo(target.depth);
        if(f != 0) return f;

        return id.compareTo(target.id);
    }

    public boolean equalsTo(CNode target){
        if (depth != target.depth) return false;
        return id == target.id;
    }

    public boolean isAncestorOf(CNode n){
        CNode cur = n;
        while(cur.parent != null){
            if(cur.id == this.id) return true;
            cur = cur.parent;
        }
        return false;
    }

    public void mergeTo(CNode target,  boolean temporalFlag){
        mergeTo = target;
        permanentlyMerged = temporalFlag;
        descendantLeaves = calcDescendantLeaves();
        tree.leafSet.removeAll(descendantLeaves);
    }

    public boolean isMerged(){
        return (mergeTo != null);
    }

    Collection<CNode> calcDescendantLeaves(){
        Collection<CNode> leaves = new TreeSet<CNode>();
        for(Pair<CNode,Observation> elt:children.values()){
            CNode child = elt.fst;
            if(child.isLeafNode()) leaves.add(child);
            else{
                Collection<CNode> cleaves = child.calcDescendantLeaves();
                leaves.addAll(cleaves);
            }
        }
        return leaves;
    }

    public boolean isLeafNode(){
        return tree.leafSet.contains(this);
    }
}
