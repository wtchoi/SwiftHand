package edu.berkeley.wtchoi.swift.util.statistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/26/12
 * Time: 5:02 PM
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
public class SimpleTracker implements Tracker{
    private long startTime;
    private long wrapTime;
    private double average;
    private long total;


    private Vector<Record> records;

    public SimpleTracker(){
        reset();
    }

    public void start(){
        startTime = System.currentTimeMillis();
    }

    public void stop(){
        wrapTime = System.currentTimeMillis() - startTime;
        add();
    }

    public void attach(Object o){
        records.lastElement().extra = o;
    }

    public void abort(){
        wrapTime = 0;
        add();
    }

    public void undoLast(){
        int lastIndex = records.size() - 1;
        long lastRecord  = records.get(lastIndex).time;
        records.remove(lastIndex);
        total -= lastRecord;
        average = total / records.size();
    }

    private void add(){
        records.add(new Record(wrapTime));
        total += wrapTime;
        average = ((double)(total))/((double)records.size());
    }

    public String getCurrentRecordWithFormat(){
        return "#" + records.size() + "\t" + wrapTime + "\t" + average;
    }

    public long getCurrentRecord(){
        return wrapTime;
    }

    public Vector<Long> getRecords(){
        return (Vector<Long>) records.clone();
    }


    public void reset(){
        startTime = 0;
        wrapTime = 0;
        average = 0;
        total = 0;
        records = new Vector<Record>();
    }

    public long total(){
        return total;
    }

    public int count(){
        return records.size();
    }

    public double average(){
        return average;
    }

    public void dump(String filename) throws IOException{
        File file = new File(filename);
        FileWriter fw = new FileWriter(file);
        PrintWriter writer = new PrintWriter(fw);

        int i = 1;
        double sum = 0;
        for(Record r:records){
            writer.print(i++);
            writer.print("\t");
            writer.print(sum);
            writer.print("\t");
            writer.print(r.time);
            writer.print("\t");
            writer.println(r.extra.toString());
            sum += r.time;
        }

        writer.flush();
        writer.close();
    }
}

