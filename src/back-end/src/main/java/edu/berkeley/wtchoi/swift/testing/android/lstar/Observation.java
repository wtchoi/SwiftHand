package edu.berkeley.wtchoi.swift.testing.android.lstar;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.ExtendedComparable;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/13/12
 * Time: 7:46 PM
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
public class Observation implements ExtendedComparable<Observation> {
    private CSet<ICommand> palette;
    private TransitionInfo augmentation;

    private static TransitionInfo stopTransition = new TransitionInfo(){
        public boolean didNothing(){ return false; }
    };


    private static Observation stopObservation = new Observation(new CSet<ICommand>(),stopTransition){
        @Override
        public boolean isStopObservation(){
            return true;
        }
    };

    public Observation(CSet<ICommand> k, TransitionInfo a){
        palette = k;
        augmentation = a;
    }

    public int compareTo(Observation o){
        boolean f1 = (this == stopObservation);
        boolean f2 = (o == stopObservation);

        if(f1 && f2) return 0;
        if(f1 && !f2) return 1;
        if(f2 && !f1) return -1;


        if(palette == null || o == null){
            int x = 1;
        }
        return this.palette.compareTo(o.palette);
    }

    public CSet<ICommand> getPalette(){return palette;}
    public TransitionInfo getAugmentation(){ return augmentation;}

    public String toString(){
        return palette.toString()+augmentation.didNothing();
    }

    public static Observation getStopObservation(){
        return stopObservation;
    }

    public boolean isStopObservation(){
        return false;
    }

    public boolean equalsTo(Observation target){
        if(this.compareTo(target) == 0){
            return augmentation.equalsTo(target.augmentation);
        }
        return false;
    }
}
