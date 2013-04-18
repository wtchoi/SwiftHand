package edu.berkeley.wtchoi.swift.util;

import java.util.Collection;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/29/12
 * Time: 8:51 PM
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
public class RandomUtil{
    private Random rand;
    private long seed;

    public RandomUtil(){
        rand = new Random(0);
        seed = 0;
    }

    public RandomUtil(long s){
        rand = new Random(s);
        seed = s;
    }

    public void setSeed(long s){
        seed = s;
        rand.setSeed(s);
    }

    public long getSeed(){
        return seed;
    }

    public <T> T pick(Collection<T> collection){
        int targetIndex = rand.nextInt(collection.size());
        int index = 0;
        for(T candidate:collection){
            if(index++ == targetIndex){
                return candidate;
            }
        }
        throw new RuntimeException("Cannot pick from set????: " +  targetIndex + " vs " + collection.size());
    }

    public <T> T pickWithTaboo(Collection<T> collection, Collection<T> tabooSet){
        int targetIndex = rand.nextInt(collection.size());
        int index = 0;
        for(T candidate:collection){
            if(tabooSet.contains(candidate)) continue;
            if(index++ == targetIndex){
                return candidate;
            }
        }
        if(index == 0) return null;
        return pickWithTaboo(collection, tabooSet);
    }

    public <T extends RandomUtil.Weighted> T weightedPick(Collection<T> collection){
        if(collection.size() == 0) return null;

        double total = 0;
        for(T elt:collection){
            total += elt.weight();
        }

        double targetValue = rand.nextDouble() * total;
        double upperBound = 0;
        int i = 1;
        for(T candidate:collection){
            upperBound += candidate.weight();
            if(upperBound > targetValue)
                return candidate;
        }
        return weightedPick(collection);
    }

    public <T> T weightedPick(Collection<T> collection, WeighCalculator<T> calculator){
        if(collection.size() == 0) return null;

        double total = 0;
        for(T elt:collection){
            total += calculator.weight(elt);
        }

        double targetValue = rand.nextDouble() * total;
        double upperBound = 0;
        int i = 1;
        for(T candidate:collection){
            upperBound += calculator.weight(candidate);
            if(upperBound > targetValue)
                return candidate;
        }
        return weightedPick(collection, calculator);
    }


    public int nextInt(int limit){
        return rand.nextInt(limit);
    }

    public double nextDouble(){
        return rand.nextDouble();
    }

    public boolean nextBoolean(){
        return rand.nextBoolean();
    }

    public static interface Weighted{
        public double weight();
    }

    public static interface WeighCalculator<T>{
        public double weight(T elt);
    }

    private static RandomUtil sharedInstance;
    public static RandomUtil getSharedInstance(){
        return sharedInstance;
    }

    public static void initSharedInstance(int key){
        sharedInstance = new RandomUtil(key);
    }
}
