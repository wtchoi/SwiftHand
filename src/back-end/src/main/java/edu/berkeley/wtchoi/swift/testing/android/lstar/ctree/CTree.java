package edu.berkeley.wtchoi.swift.testing.android.lstar.ctree;

import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.CVector;
import edu.berkeley.wtchoi.collection.Pair;
import edu.berkeley.wtchoi.gv.GraphViz;
import edu.berkeley.wtchoi.logger.Logger;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.testing.android.lstar.Observation;
import edu.berkeley.wtchoi.swift.util.RandomUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/13/12
 * Time: 7:42 PM
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
public class CTree{
    //protected CSet<ICommand> defaultPalette;

    CNode root;
    Set<CNode> leafSet;
    private int treeDepth;
    
    private long parentCount = 0;
    private long childCount = 0;
    private double averageChildrenCount = 0;

    public CTree(CSet<ICommand> initialPalette){//}, CSet<ICommand> defaultPalette){
        leafSet = new TreeSet<CNode>();
        //this.defaultPalette = defaultPalette;

        root = new CNode(this);
        root.palette = initialPalette;
        root.depth = 0;
        leafSet.add(root);
        extend(root);        
    }

    public CNode makeNode(CNode p, ICommand i){
        CNode n = new CNode(this);

        n.parent = p;
        n.inputFromParent = i;

        n.depth = p.depth+1;
        if(n.depth > treeDepth) treeDepth = n.depth;

        leafSet.add(n);

        return n;
    }

    private CNode getNode(CNode startingNode, List<ICommand> ilst){
        CNode cur = startingNode;
        for(ICommand i: ilst){
            if(!cur.children.containsKey(i)) return null;
            cur = cur.children.get(i).fst;
            if(cur.isMerged() && cur.permanentlyMerged)
                cur = cur.mergeTo;
        }
        return cur;
    }

    public void addPath(CState startingState, List<ICommand> ilst, List<Observation> olst){
        addPathImp(startingState.node, ilst, olst);
    }

    private void addPathImp(CNode startingNode, List<ICommand> ilst, List<Observation> olst){
        CNode cur = startingNode;
        Iterator<Observation> oiter = olst.iterator();
        Observation o;

        for(ICommand i : ilst){
            o = oiter.next();
            Pair<CNode,Observation> child = cur.children.get(i);
            if(o.isStopObservation())
                child.fst.isStopNode = true;
            if(child.snd == null){
                child.setSecond(o);
                child.fst.palette = o.getPalette();
                child.fst.tiFromParent = o.getAugmentation();
                System.out.println("CL!:" + o.getAugmentation().didNothing());
                extend(child.fst);
            }
            if(o.isStopObservation()) break;
            cur = child.fst;
        }
    }

    boolean buildInputPath(CNode from, CNode target, CList<ICommand> lst){
        if(target == from) return true;
        if(target == root) return false;
        if(buildInputPath(from, target.parent, lst)){
            lst.add(target.inputFromParent);
            return true;
        }
        else{
            return false;
        }
    }

    void buildInputPath(CNode target, CList<ICommand> lst){
        buildInputPath(root,target,lst);
    }

    private void extend(CNode target){
        leafSet.remove(target);
        if(target.isStopNode) return;

        for(ICommand i : target.palette){
            CNode temp = makeNode(target, i);
            target.children.put(i, new Pair<CNode,Observation>(temp,null));
        }

        parentCount++;
        childCount += target.palette.size();
        averageChildrenCount = ((double)childCount / ((double) parentCount));
    }

    private final Set<CNode> getLeafSet(){
        return leafSet;
    }

    public CList<ICommand> tryPruning(CState startingState, List<ICommand> ilst){
        return tryPruningImp(getNode(startingState.node, ilst));
    }

    private CList<ICommand> tryPruningImp(CNode target){
        CList<ICommand> rlst = new CVector<ICommand>();
        try{
            if(target.isStopNode) return null;
            if(!target.tiFromParent.didNothing()) return null;
        }
        catch(Exception e){
            int x = 1;
        }

        CSet<CNode> candidates = new CSet<CNode>();
        candidates.add(target);

        //initialize
        //if(! target.tiFromParent.didNothing()) return null;
        //CSet<CNode> candidates = new CSet<CNode>();
        //candidates.add(target.parent);

        CSet<CNode> candidates2 = new CSet<CNode>();
        //for(Pair<CNode,Observation> ch: target.parent.children.values()){
        //    if(ch.fst.id == target.id || leafSet.contains(ch.fst)) continue;
        //    if(ch.fst.tiFromParent.didNothing() && ! (ch.fst.isMerged()))
        //        candidates2.add(ch.fst);
        //}


        //check trough ancestor
        while(!candidates.isEmpty()){
            CNode candidate = candidates.pollFirst();
            if(candidate.compareTo(target) != 0 &&  candidate.palette.compareTo(target.palette) == 0){
                doMerge(target, candidate,true);
                buildInputPath(candidate, rlst);
                return rlst;
            }

            try{
            if(candidate.parent != null
                    && candidate.tiFromParent.didNothing()){
                candidates.add(candidate.parent);
                //add other descendant of ancestors
                for(Pair<CNode,Observation> ch: candidate.parent.children.values()){
                    if(ch.fst.id == candidate.id || leafSet.contains(ch.fst)) continue;
                    if(ch.fst.tiFromParent.didNothing() && ! (ch.fst.isMerged()))
                        candidates2.add(ch.fst);
                }
            }
            }catch(Exception e){
                int x  = 1;
            }
        }

        while(!candidates2.isEmpty()){
            CNode candidate = candidates2.pollFirst();
            if(candidate.palette.compareTo(target.palette) == 0){
                doMerge(target, candidate, true);
                buildInputPath(candidate, rlst);
                return rlst;
            }

            for(Pair<CNode,Observation> ch: candidate.children.values()){
                if(leafSet.contains(ch.fst)) continue;
                if(ch.fst.tiFromParent.didNothing() && ! (ch.fst.isMerged()))
                    candidates2.add(ch.fst);
            }
        }
        return null;
    }

    //merge for internal purpose
    void doMerge(CNode target, CNode to, boolean temporalFlag){
        target.mergeTo(to,temporalFlag);
        if(temporalFlag) remove(target);
    }

    void split(CNode node){
        node.mergeTo = null;
        leafSet.addAll(node.descendantLeaves);
        node.descendantLeaves = null;
    }

    private void remove(CNode n){
        if(n.isMerged()) return;
        leafSet.remove(n);
        for(Pair<CNode,Observation> ch : n.children.values())
            remove(ch.getFirst());
    }

    //CState generators
    public CState getInitState(){
        return new CState(root, null,this);
    }

    public CState getState(CList<ICommand> i){
        return new CState(null, i, this);
    }

    public CState getState(CState s, CList<ICommand> input){
        CVector<ICommand> tmp = new CVector<ICommand>();
        tmp.addAll(s.input);
        tmp.addAll(input);
        return new CState(s.node,tmp,this);
    }

    public CState getState(CState s, ICommand cmd){
        CVector<ICommand> input = new CVector<ICommand>();
        input.add(cmd);
        return getState(s,input);
    }

    //Utility Functions
    public CSet<ICommand> getPalette(CState state){
        state.normalize();
        if(state.input.isEmpty()) return state.node.palette;
        return null;
    }

    public Observation getTransition(CState state, ICommand cmd){
        state.normalize();
        CNode n = state.node;

        if(leafSet.contains(n)) return null;
        if(!n.children.containsKey(cmd)) return null;
        return n.children.get(cmd).snd;

    }

    public CList<Observation> getTransition(CState state, CList<ICommand> input){
        state.normalize();
        if(leafSet.contains(state.node) && !input.isEmpty()) return null;

        CNode cur = state.node;
        CList<Observation> output = new CVector<Observation>();
        for(ICommand i: input){
            if(!cur.children.containsKey(i)) return null;
            output.add(cur.children.get(i).snd);
            cur = cur.children.get(i).fst;
        }
        return output;
    }

    public boolean checkPossible(CState state, CList<ICommand> input){
        state.normalize();
        if(leafSet.contains(state.node) && !input.isEmpty()) return false;

        CNode cur = state.node;
        for(ICommand i: input){
            if(!cur.children.containsKey(i)) return false;
            cur = cur.children.get(i).fst;
        }
        return true;
    }

    public boolean visited(CState state, CList<ICommand> input){
        state.normalize();
        if(leafSet.contains(state.node)) return false;
        if(input.isEmpty()) return true;
        return !leafSet.contains(getNode(state.node,input));
    }

    //Assume input state is visited
    private static final int recommandThreshold = 3;
    public CList<ICommand> recommendNext(CState state){
        state.normalize();
        if(leafSet.contains(state.node)) return null;

        CList<ICommand> inputVector = new CVector<ICommand>();
        for(CNode n : leafSet){
            if(buildInputPath(state.node, n, inputVector))
                if(n.depth - state.getDepth() < recommandThreshold)
                    return inputVector;
            inputVector.clear();
        }
        return null;
    }

    public void setRecommendRate(double rate){
        recommendRate = rate;
    }

    private double recommendRate = 0.8;
    private RandomUtil random = RandomUtil.getSharedInstance();

    public CList<ICommand> recommendNextRandomly(CState state){
        state.normalize();
        if(leafSet.contains(state.node)) return null;
        if(random.nextDouble() > recommendRate) return null;

        CList<ICommand> inputVector = new CVector<ICommand>();
        List<CNode> candidates = new LinkedList<CNode>();

        for(CNode n : leafSet){
            if(buildInputPath(state.node, n, inputVector)){
                candidates.add(n);
            }
            inputVector.clear();
        }

        if(candidates.size() == 0) return null;

        CNode selected =  random.weightedPick(candidates, weightCalculator);
        buildInputPath(state.node, selected, inputVector);
        return inputVector;
    }

    private RandomUtil.WeighCalculator<CNode> weightCalculator =
            new RandomUtil.WeighCalculator<CNode>(){
                @Override
                public double weight(CNode node){
                    int n = node.depth;
                    double w = 1;
                    for(int i =0;i<n;i++){
                        w *= averageChildrenCount;
                    }
                    return (1.0/w);
                }
            };

    public int depth(){
        return treeDepth;
    }


    //For Model Visualization Part
    //----------------------------
    private GraphViz encodeToDot(){
        DotEncoder encoder = new DotEncoder();
        return encoder.encode(this);
    }

    public void drawTree(String path){
        GraphViz gv = encodeToDot();
        java.io.File out = new java.io.File(path);
        gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), "gif"), out);
    }

    CTreeViewer viewer;

    public void startViewer(){
        viewer = new CTreeViewer(this);
        SwingUtilities.invokeLater(viewer);
    }

    private String dotpath = "/tmp";

    public void setDotPath(String path){
        dotpath = path;
    }

    public void updateView(){
        if(viewer != null)
            viewer.reload();

        if(dotpath != null){
            dumpToDot(dotpath + "/state.dot");
        }
    }

    public void dumpToDot(String filename){
        try{
            File file = new File(filename);
            file.deleteOnExit();
            file.createNewFile();

            GraphViz gv = encodeToDot();
            FileWriter writer = new FileWriter(file);
            writer.write(gv.getDotSource());
            writer.flush();
            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
            Logger.log("CTree.dumpToDot() : Cannot create dot file : " + filename);
        }
    }

    public long getChildCount(){
        return childCount;
    }

    public long getParentCount(){
        return parentCount;
    }

    public double getAverageChildren(){
        return averageChildrenCount;
    }
}

