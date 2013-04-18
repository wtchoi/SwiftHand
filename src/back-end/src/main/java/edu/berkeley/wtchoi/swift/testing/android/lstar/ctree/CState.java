package edu.berkeley.wtchoi.swift.testing.android.lstar.ctree;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CVector;
import edu.berkeley.wtchoi.collection.ExtendedComparable;

import java.util.Iterator;


/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/20/12
 * Time: 10:12 PM
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
public class CState implements ExtendedComparable<CState> {
    CNode node;
    CList<ICommand> input;

    CTree ctree;

    CState(CNode n, CList<ICommand> i, CTree t){
        node = (n == null) ? t.root  : n;
        input = (i == null) ? new CVector<ICommand>() : i;
        ctree = t;
        this.normalize();
    }

    public int compareTo(CState st){  //TODO
        this.normalize();
        st.normalize();

        int f = Integer.valueOf(this.getDepth()).compareTo(st.getDepth());
        if(f!=0) return f;

        f = this.node.compareTo(st.node);
        if(f != 0) return f;

        return this.input.compareTo(st.input);
    }

    public boolean equalsTo(CState st){
        this.normalize();
        st.normalize();

        if(this.getDepth() != st.getDepth()) return false;
        if(!this.node.equalsTo(st.node)) return false;
        return this.input.equalsTo(st.input);
    }

    void normalize(){
        if(input.isEmpty()) return;

        CNode n = node;
        Iterator<ICommand> iter = input.iterator();
        while(iter.hasNext() && !ctree.leafSet.contains(n)){
            n = n.children.get(iter.next()).fst;
            if(n.isMerged() && n.permanentlyMerged){
                n = n.mergeTo;
            }
        }
        CList<ICommand> tmp = new CVector<ICommand>();
        while(iter.hasNext()) tmp.add(iter.next());

        this.node = n;
        this.input = tmp;
    }

    public void mergeTo(CState target){
        ctree.doMerge(node, target.node, false);
    }

    public void split(){
        ctree.split(node);
    }

    public CList<ICommand> getInput(){
        CList<ICommand> temp = new CVector<ICommand>();
        ctree.buildInputPath(this.node, temp);
        temp.addAll((this.input));
        return temp;
    }

    public boolean isStopNode(){
        return node.isStopNode;
    }

    public String toString(){
        this.normalize();
        return String.valueOf(node.id);
    }

    public boolean isPrefixOf(CList<ICommand> input){
        Iterator<ICommand> iter = input.iterator();

        CNode cur = ctree.root;
        CNode target = this.node;
        while(iter.hasNext()){
            if(cur.equals(target)) return true;

            ICommand i = iter.next();
            if(!cur.children.containsKey(i)) break;
            cur = cur.children.get(i).fst;
        }
        return false;
    }

    public boolean isPrefixOf(CState target){
        return node.isAncestorOf(target.node);
    }

    public void removePrefixFrom(CList<ICommand> input){
        Iterator<ICommand> iter = input.iterator();
        CVector<ICommand> temp = new CVector<ICommand>();

        CNode cur = ctree.root;
        CNode target = this.node;
        while(iter.hasNext()){
            if(cur.compareTo(target) == 0) break;

            ICommand i = iter.next();
            if(!cur.children.containsKey(i)) return;
            cur = cur.children.get(i).fst;
        }
        if(!iter.hasNext()) return;

        while(iter.hasNext()){
            ICommand i = iter.next();
            temp.add(i);
            cur = cur.children.get(i).fst;
        }

        input.clear();
        input.addAll(temp);
    }

    public void setColor(String c){
        node.color = c;
    }

    public int getDepth(){
        return node.depth + input.size();
    }

    public CState getMergeTo(){
        return new CState(node.mergeTo, null, ctree);
    }
}
