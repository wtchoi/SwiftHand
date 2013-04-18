package edu.berkeley.wtchoi.swift.testing.android.random;

import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CVector;
import edu.berkeley.wtchoi.gv.GraphViz;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.testing.android.AppRequest;
import edu.berkeley.wtchoi.swift.testing.android.AppResult;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.swift.testing.android.AppTestingGuide;
import edu.berkeley.wtchoi.swift.util.RandomUtil;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/25/12
 * Time: 8:25 PM
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
public class RandomGuide implements AppTestingGuide {

    private int nodeCount = 0;
    private static String leafPrefix = "1567834223";
    private double averageChild = 0;
    private long totalChild;


    class Node implements Comparable<Node>, RandomUtil.Weighted {
        public TreeMap<ICommand, Node> children;
        public Set<ICommand> palette;
        private ICommand inputFromParent;
        private Node parent;
        int depth;
        boolean visited = false;
        boolean stop = false;


        final int id = nodeCount++;

        public double weight(){
            double e = 1.0 / averageChild;
            double w = e;
            for(int i = 0 ; i<depth;i++){
                w = w * e;
            }

            return w;
        }

        public int compareTo(Node n){
            return new Integer(id).compareTo(n.id);
        }

        public Node(){
            frontierSet.add(this);
            depth = 0;
        }

        private void visit(Set<ICommand> palette){
            frontierSet.remove(this);
            this.visited = true;
            this.palette = palette;
            this.children = new TreeMap<ICommand, Node>();
            for(ICommand cmd:palette){
                Node child = new Node();
                child.inputFromParent = cmd;
                child.parent = this;
                child.depth = this.depth + 1;
                this.children.put(cmd, child);
            }
            totalChild += children.size();
            averageChild = ((double)totalChild) / ((double)(nodeCount - frontierSet.size() - taboo.size()));
        }

        private CList<ICommand> getInputSequence(){
            LinkedList<ICommand> list = new LinkedList<ICommand>();
            Node cur = this;
            while(cur.parent != null){
                list.addFirst(cur.inputFromParent);
                cur = cur.parent;
            }
            return new CVector<ICommand>(list);
        }

        public void encodeToDot(GraphViz gv){
            if(this.visited){
                gv.addln(id + " [shape=circle];");
                int leafChildCount = 0;
                for(ICommand cmd:children.keySet()){
                    Node child = children.get(cmd);
                    if(child.stop) continue;
                    if(child.visited){
                        gv.addln(id + " -> " + child.id + "[fontsize=11, label=\"" + cmd.toString()+ "\"];");
                        child.encodeToDot(gv);
                    }
                    else
                        leafChildCount++;
                }
                if(leafChildCount > 0){
                    gv.addln(id + " -> " + id + leafPrefix + " [label=" + leafChildCount + ", fontsize=10, fontcolor=gray, color=gray];");
                    gv.addln(id + leafPrefix + " [shape=point, color=gray]");
                }
            }
        }
    }

    private TreeSet<Node> taboo = new TreeSet<Node>();

    Node root;
    private RandomUtil random = RandomUtil.getSharedInstance();
    private double restartRate = 0.1;
    private Set<Node> frontierSet = new TreeSet<Node>();
    private RandomTestingObserver observer;

    private boolean flagUseTree = false;
    private boolean flagTabooSet = false;

    public void setObserver(RandomTestingObserver observer){
        this.observer = observer;
    }

    public long getSeed(){
        return random.getSeed();
    }

    private void update(CList<ICommand> inputs, List<AppState> states){
        Node cur = previousState;
        Iterator<AppState> stateIterator = states.iterator();

        for(ICommand cmd: inputs){
            if(!stateIterator.hasNext()){
                int x = 1;
            }

            cur = cur.children.get(cmd);
            AppState s = stateIterator.next();
            if(s.isStop()){
                cur.visited = true;
                cur.stop = true;
                frontierSet.remove(cur);
                break;
            }
            if(cur == null){
                throw new RuntimeException("WTF?");
            }
            if(cur.visited == false){
                Set<ICommand> palette = s.getPalette();
                cur.visit(palette);
            }
        }
        previousState = cur;
    }

    @Override
    public void setInitialState(AppState s){;
        root = new Node();
        observer.setGuide(this);

        Set<ICommand> palette = s.getPalette();
        root.visit(palette);
        previousState = root;
    }

    private Node previousState;

    @Override
    public AppRequest getRequest(AppState s){
        if(previousState.stop) return randomRequest();

        Set<ICommand> palette = previousState.palette;
        if(palette.size() > 0 && random.nextDouble() > restartRate){
            CList<ICommand> inputs = new CVector<ICommand>();
            inputs.add(random.pick(palette));
            return new AppRequest(inputs);
        }
        else{
            return randomRequest();
        }
    }

    private AppRequest randomRequest(){
        if(flagUseTree && flagTabooSet && previousState.children != null){
            frontierSet.addAll(taboo);
            taboo.clear();
            taboo.addAll(previousState.children.values());
            frontierSet.removeAll(previousState.children.values());

            if(frontierSet.size() == 0){
                frontierSet.addAll(taboo);
                taboo.clear();
            }
        }

        previousState = root;
        AppRequest request;

        if(flagUseTree){
            Node target = random.weightedPick(frontierSet);
            request = new AppRequest(target.getInputSequence());
        }
        else{
            request = new AppRequest(new CVector<ICommand>());
        }

        request.setRestartRequest();
        return request;
    }

    @Override
    public void receiveResult(AppRequest r, AppResult rr){
        List<AppState> stateSequence = rr.getResultingStateSequence();
        update(r.getInputSequence(), stateSequence);
    }

    public void useTabooSet(){
        flagTabooSet = true;
    }

    public void useTree(){
        flagUseTree = true;
    }

    public void setRestartRate(double rate){
        restartRate = rate;
    }

    @Override
    public List<String> getOptionString(){
        return null;
    }

    @Override
    public void finish(){}
}

