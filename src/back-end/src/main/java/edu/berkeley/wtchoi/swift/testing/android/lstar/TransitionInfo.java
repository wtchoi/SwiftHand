package edu.berkeley.wtchoi.swift.testing.android.lstar;

import edu.berkeley.wtchoi.swift.driver.drone.CompressedLog;
import edu.berkeley.wtchoi.swift.driver.drone.SLog;
import edu.berkeley.wtchoi.collection.CVector;
import edu.berkeley.wtchoi.collection.CollectionUtil;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/13/12
 * Time: 7:35 PM
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

public class TransitionInfo implements Serializable
{
    private static final long serialVersionUID = -5186309675577891457L;

    public CVector<SLog> trace;

    public TransitionInfo(){}

    public TransitionInfo(CompressedLog cl){
        trace = new CVector<SLog>(cl);
    }


    public TransitionInfo(int[] buffer, int size){
        trace = new CVector<SLog>();
        for(int i =0;i<size;i++){
            trace.add(new SLog((byte)buffer[i*3], (short)buffer[i*3+1], buffer[i*3+2]));
        }
    }

    public boolean didNothing(){
        return trace.isEmpty();
    }

    public boolean equalsTo(TransitionInfo target){
        if(trace == null) return target.trace == null;
        else if(target.trace == null) return false;

        return 0 == CollectionUtil.compare(trace, target.trace, comparator);
    }

    private static Comparator<SLog> comparator = new Comparator<SLog>(){
        public int compare(SLog s1, SLog s2){
            return s1.pseudoCompareTo(s2);
        }
    };

    public void concat(TransitionInfo ti){
        trace.addAll(ti.trace);
    }
}
