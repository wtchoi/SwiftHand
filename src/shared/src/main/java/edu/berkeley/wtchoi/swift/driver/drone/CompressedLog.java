package edu.berkeley.wtchoi.swift.driver.drone;

import java.io.*;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/14/12
 * Time: 11:54 PM
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
public class CompressedLog implements Iterable<SLog>, Externalizable {
    final static int BLOCKSIZE = 10000;
    final static int BUFFERSIZE = BLOCKSIZE * SLog.LOGSIZE;

    private static final long serialVersionUID = -5186309675577891457L;

    private class Block implements Serializable{
        private static final long serialVersionUID = -5186309675577891457L;

        byte[] buffer;
        Block next;
        Block prev;

        Block(){
            buffer = new byte[BUFFERSIZE];
            next = null;
            prev = null;
        }
    }

    private Block mHeadBlock;
    private Block mTailBlock;
    private int mHeadOffset;
    private int mTailOffset;
    private int mSize;

    public CompressedLog(){
        clear();
    }


    @Override
    public void writeExternal(ObjectOutput o) throws IOException{
        o.writeLong(serialVersionUID);
        o.writeInt(mHeadOffset);
        o.writeInt(mTailOffset);
        o.writeInt(mSize);
        int blokcNumber = countBlocks();
        o.writeInt(blokcNumber);

        Block temp = mHeadBlock;
        for(int i = 0;i<blokcNumber;i++){
            o.writeObject(temp.buffer);
            temp = temp.next;
        }
    }

    private int countBlocks(){
        Block temp = mHeadBlock;
        int i = 0;

        while(temp != null){
            i = i + 1;
            temp = temp.next;
        }

        return i;
    }

    @Override
    public void readExternal(ObjectInput o) throws IOException, ClassNotFoundException
    {
        if(o.readLong() != serialVersionUID)
            throw new RuntimeException("Version does not match!");

        mHeadOffset = o.readInt();
        mTailOffset = o.readInt();
        mSize = o.readInt();
        int blokcNumber = o.readInt();

        for(int i = 0 ; i<blokcNumber ; i++){
            Block block = new Block();
            block.buffer = (byte[]) o.readObject();

            if(i == 0){
                mHeadBlock = block;
                mTailBlock = block;
            }
            else{
                mTailBlock.next = block;
                block.prev = mTailBlock;
                mTailBlock = block;
            }
        }
    }

    @Override
    public Iterator<SLog> iterator(){
        return new Iterator<SLog>(){
            private Block curBlock = mHeadBlock;
            private int curOffset = mHeadOffset;
            private int handledCount = 0;

            @Override
            public boolean hasNext(){
                return handledCount < mSize;
            }

            @Override
            public SLog next(){
                if(!hasNext()) return null;
                SLog val = SLog.readFrom(curBlock.buffer, curOffset);

                curOffset += SLog.LOGSIZE;
                if(curOffset == BUFFERSIZE){
                    curOffset = 0;
                    curBlock = curBlock.next;
                }

                handledCount++;
                //Logger.log("iteration: " + handledCount + ", " + mSize + " " + val.toString());
                return val;
            }

            @Override
            public void remove(){
                throw new RuntimeException("Remove during iteration is not supported");
            }
        };
    }


    public void add(SLog slog){
        if(mSize != 0){
            mTailOffset += SLog.LOGSIZE;
            if(mTailOffset == BUFFERSIZE){
                mTailBlock.next = new Block();
                mTailBlock.next.prev = mTailBlock;
                mTailBlock = mTailBlock.next;
                mTailOffset = 0;
            }
        }
        slog.writeTo(mTailBlock.buffer, mTailOffset);
        //Logger.log("add log to " + mTailBlock.buffer.toString() +"," + mTailOffset);
        mSize++;
    }


    public int size(){
        return mSize;
    }


    public void clear(){
        if(mHeadBlock == null){
            mHeadBlock = new Block();
        }
        else{
            mHeadBlock.next = null;
            mHeadBlock.prev = null;
        }
        mHeadOffset= 0;
        mTailBlock = mHeadBlock;
        mTailOffset = mHeadOffset;
        mSize = 0;
    }

    public SLog removeFirst(){
        if(mSize == 0) return null;

        SLog val = SLog.readFrom(mHeadBlock.buffer, mHeadOffset);
        if(mSize != 1){
            mHeadOffset += SLog.LOGSIZE;
            if(mHeadOffset == BUFFERSIZE){
                mHeadOffset = 0;
                if(mHeadBlock.next == null){
                    mTailOffset = 0;
                }
                else{
                    mHeadBlock = mHeadBlock.next;
                    mHeadBlock.prev = null;
                }
            }
        }
        mSize--;
        return val;
    }

    public SLog removeLast(){
        if(mSize == 0) return null;
        SLog val = SLog.readFrom(mTailBlock.buffer, mTailOffset);
        if(mSize != 1){
            if(mTailOffset == 0){
                mTailOffset = BUFFERSIZE - SLog.LOGSIZE;
                mTailBlock = mTailBlock.prev;
                mTailBlock.next = null;
            }
            else{
                mTailOffset -= SLog.LOGSIZE;
            }
        }
        mSize --;
        return val;
    }

    public SLog peekLast(){
        if(mSize == 0) return null;
        return SLog.readFrom(mTailBlock.buffer, mTailOffset);
    }
}
