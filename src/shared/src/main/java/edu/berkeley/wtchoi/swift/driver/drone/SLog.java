package edu.berkeley.wtchoi.swift.driver.drone;


import edu.berkeley.wtchoi.collection.ExtendedComparable;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 6:08 PM
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
public class SLog implements ExtendedComparable<SLog>, Serializable {
    private static final long serialVersionUID = -5186309675577891457L;

    public static final int LOGSIZE = 7;

    public static final byte CALL = 1;
    public static final byte RETURN = 2;
    public static final byte ENTER = 3;
    public static final byte EXIT = 4;
    public static final byte UNROLL_CATCH = 5;
    public static final byte PP = 6;
    public static final byte RECEIVER = 7;
    public static final byte CATCH = 8;
    public static final byte THROW = 9;
    public static final byte PP_EXTRA = 10;
    public static final byte THROW_UNROLL = 11;
    public static final byte ENTER_CLINIT = 12;


    public byte type;
	public short fid;
	public int aux;

    public SLog(byte lt, short fid, int aux){
        this.type = lt;
        this.fid = fid;
        this.aux = aux;
    }


    private SLog(byte lt, short fid){
		this.type = lt;
		this.fid = fid;
	}

    public static SLog readFrom(byte[] buffer, int offset){
        ByteBuffer b = ByteBuffer.wrap(buffer);

        byte type = buffer[offset];
        short fid = b.getShort(offset+1);
        int aux = b.getInt(offset+3);

        return new SLog(type, fid, aux);
    }

    public void writeTo(byte[] buffer, int offset){
        ByteBuffer b = ByteBuffer.wrap(buffer);

        b.put(offset, this.type);
        b.putShort(offset+1, this.fid);
        b.putInt(offset+3, this.aux);
    }


    public static SLog getEnter(short fid){
        return new SLog(ENTER, fid);
    }

    public static SLog getEnterCLINIT(short fid){
        return new SLog(ENTER_CLINIT, fid);
    }

    public static SLog getExit(short fid){
        return new SLog(EXIT,  fid);
    }

    public static SLog getUnrollCatch(short fid){
        return new SLog(UNROLL_CATCH, fid);
    }


    public static SLog getCatch(short fid){
        return new SLog(CATCH, fid);
    }

    public static SLog getThrow(short fid){
        return new SLog(THROW, fid);
    }

    public static SLog getThrowUnroll(short fid){
        return new SLog(THROW_UNROLL, fid);
    }

    public static SLog getCall(short fid){
        return new SLog(CALL, fid, 0);
    }

    public static SLog getReturn(short fid){
        return new SLog(RETURN, fid);
    }

    public static SLog getReceiver(int oid, short fid){
        return new SLog(RECEIVER, fid, oid);
    }

    public static SLog getPP(int pid, short fid){
        return new SLog(PP, fid, pid);
    }

    public static SLog getPPExtra(int pid, short fid){
        return new SLog(PP_EXTRA, fid, pid);
    }

	public String toString(){
		String typ = null;
		switch(type){
			case CALL:
				typ = "CALL";
				break;
			case RETURN:
				typ = "RETURN";
				break;
            case ENTER:
                typ = "ENTER";
                break;
            case ENTER_CLINIT:
                typ = "ENTER_CLINIT";
                break;
            case EXIT:
                typ = "EXIT";
                break;
            case UNROLL_CATCH:
                typ = "UNROLL_CATCH";
                break;
            case PP:
                typ = "PP";
                break;
            case RECEIVER:
                typ = "RECEIVER";
                break;
            case THROW:
                typ = "THROW";
                break;
            case THROW_UNROLL:
                typ = "THROW_UNROLL";
                break;
            case CATCH:
                typ = "CATCH";
                break;
			default:
				break;
		}
        if(type == PP || type == PP_EXTRA){
            return typ + "(fid = "+ fid +", aux=" + aux + ")";
        }
        else if(type == CALL){
            return typ + "(fid = "+ fid +", count=" + aux + ")";
        }
		return typ+"(fid = "+ fid +")";
	}

    @Override
    public int compareTo(SLog t){
        int c1 = compareInt(type, t.type);
        if(c1 != 0) return c1;

        int c2 = compareInt(fid, t.fid);
        if(c2 != 0) return c2;

        return compareInt(aux, t.aux);
    }

    public boolean equalsTo(SLog t){
        if(type != t.type) return false;
        if(fid != t.fid) return false;
        return aux == t.aux;
    }

    public int pseudoCompareTo(SLog t){
        int c1 = compareInt(type, t.type);
        if(c1 != 0) return c1;

        int c2 = compareInt(fid, t.fid);
        if(c2 != 0) return c2;

        if(type == PP) return compareInt(aux, t.aux);
        else return 0;
    }

    private int compareInt(int i1, int i2){
        if(i1 > i2) return 1;
        if(i1 < i2) return -1;
        return 0;
    }
}
