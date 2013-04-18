package edu.berkeley.wtchoi.swift.util.statistics;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/26/12
 * Time: 5:09 PM
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
public class SequentialTracker implements Tracker{

    private boolean fixed = false;
    private boolean stable = true;
    private Vector<String> names;
    private Vector<SimpleTracker> trackers;
    final private SimpleTracker selfTracker = new SimpleTracker();

    public SequentialTracker(){
        clear();
    }

    public void clear(){
        selfTracker.reset();
        names = new Vector<String>();
        trackers = new Vector<SimpleTracker>();
        fixed = false;
    }

    public void addTracker(String name, SimpleTracker tracker){
        if(!fixed){
            names.add(name);
            trackers.add(tracker);
        }
        else{
            throw new RuntimeException("Cannot add tracker to already fixed SequenceTracker");
        }
    }

    public void fix(){
        fixed = true;
    }

    private Iterator<SimpleTracker> cursor;
    public void reset(){
        for(SimpleTracker t:trackers){
            t.reset();
        }
        cursor = trackers.iterator();
        selfTracker.reset();
    }

    private SimpleTracker currentTracker;
    public void start(){
        stable = false;
        cursor = trackers.iterator();
        currentTracker = cursor.next();
        selfTracker.start();
    }

    public void startSub(){
        currentTracker.start();
    }

    public void stopSub(){
        currentTracker.stop();
        currentTracker = cursor.next();
    }

    public void check(){
        currentTracker.stop();
        currentTracker = cursor.next();
        currentTracker.start();
    }

    public void stop(){
        currentTracker.stop();
        while(cursor.hasNext()){
            currentTracker = cursor.next();
            currentTracker.abort();
        }
        stable = true;
        cursor = null;
        currentTracker = null;
        selfTracker.stop();
    }

    public void abort(){
        if(!stable){
            cursor = trackers.iterator();
            while(cursor.hasNext()){
                SimpleTracker undoTarget = cursor.next();
                if(undoTarget == currentTracker) break;
                undoTarget.undoLast();
            }
        }
        for(SimpleTracker t:trackers) t.abort();
        stable = true;
        cursor = null;
        currentTracker = null;
        selfTracker.abort();
    }

    public void undoLast(){
        assertStable();
        for(SimpleTracker t:trackers) t.undoLast();
        selfTracker.undoLast();
    }

    public long getCurrentRecord(){
        assertStable();
        return selfTracker.getCurrentRecord();
    }

    public Vector<Long> getRecords(){
        assertStable();
        return selfTracker.getRecords();
    }

    public Vector<Long> getCurrentRecordDetail(){
        assertStable();
        Vector<Long> record = new Vector<Long>();
        for(SimpleTracker t:trackers) record.add(t.getCurrentRecord());
        return record;
    }

    public Vector<String> getNames(){
        return (Vector<String>) names.clone();
    }

    public SimpleTracker getSubTracker(String name){
        int index = names.indexOf(name);
        return trackers.get(index);
    }

    private void assertStable(){
        if(!stable) throw new RuntimeException("Cannot do this action while tracker is active");
    }
}
