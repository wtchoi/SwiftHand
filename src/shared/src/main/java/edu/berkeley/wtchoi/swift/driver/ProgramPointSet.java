package edu.berkeley.wtchoi.swift.driver;

import edu.berkeley.wtchoi.logger.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/21/12
 * Time: 11:47 PM
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
public class ProgramPointSet implements Serializable {

    private static final long serialVersionUID = -5186309675577891457L;

    private TreeSet<Long> set = new TreeSet<Long>();

    public void add(int pp, short fid){
        long v = encode(pp, fid);
        set.add(v);
    }

    private long encode(int pp, short fid){
        long v = pp;
        v <<= 16;
        v |= fid;
        return v;
    }

    public boolean contains(int pp, short fid){
        long v = encode(pp, fid);
        return set.contains(v);
    }

    public void clear(){
        set.clear();
    }

    public void addAll(ProgramPointSet s){
        set.addAll(s.set);
    }

    public int size(){
        return set.size();
    }

    public void dump(String filename){
        try{
            File file = new File(filename);
            PrintWriter writer = new PrintWriter(file);

            TreeSet<Long> pps = new TreeSet<Long>();
            pps.addAll(this.set);

            while(pps.size() != 0){
                long value = pps.first();
                pps.remove(value);
                long pp = getPP(value);
                long fid = getFid(value);
                writer.println("(" + pp + ", " + fid + ")");
            }

            writer.flush();
            writer.close();
        }
        catch(Exception e){
            Logger.log("ProgramPointSets : Cannot dump to " + filename);
        }
    }

    private int getPP(long encoding){
        return (int) encoding >> 16;
    }

    private short getFid(long encoding){
        long pp = encoding >> 16;
        return (short) (encoding ^ (encoding & (pp << 16)));
    }

    public void foreach(ProgramPointVisitor visitor){
        for(Long point:set){
            int pp = getPP(point);
            short fid = getFid(point);
            visitor.visit(pp, fid);
        }
    }

    public static interface ProgramPointVisitor{
        public void visit(int bid, short mid);
    }

    /*
    @Override                                   tr
    public void readExternal(ObjectInput o) throws IOException, ClassNotFoundException{
        o.readLong();
        set = (TreeSet<Long>) o.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput o) throws IOException{
        o.writeLong(238428434);
        o.writeObject(set);
    }
    */
}
