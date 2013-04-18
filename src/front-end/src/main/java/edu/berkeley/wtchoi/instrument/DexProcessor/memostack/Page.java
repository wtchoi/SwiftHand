package edu.berkeley.wtchoi.instrument.DexProcessor.memostack;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/3/12
 * Time: 3:07 PM
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
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

class Page{
    private static int STACk_SIZE = 8;

    private boolean[] booleanStack;
    private byte booleanStackTop = -1;

    private byte[] byteStack;
    private byte byteStackTop = -1;

    private char[] charStack;
    private byte charStackTop = -1;

    private short[] shortStack;
    private byte shortStackTop = -1;

    private int[] intStack;
    private byte intStackTop = -1;

    private long[] longStack;
    private byte longStackTop = -1;

    private float[] floatStack;
    private byte floatStackTop = -1;

    private double[] doubleStack;
    private byte doubleStackTop = -1;

    private Object[] objectStack;
    private byte objectStackTop = -1;

    public void pushValue(boolean b){
        if(booleanStack == null){
            booleanStack = new boolean[STACk_SIZE];
        }
        booleanStack[++booleanStackTop] = b;
    }

    public void pushValue(byte b){
        if(byteStack == null){
            byteStack = new byte[STACk_SIZE];
        }
        byteStack[++byteStackTop] = b;
    }

    public void pushValue(char b){
        if(charStack == null){
            charStack = new char[STACk_SIZE];
        }
        charStack[++charStackTop] = b;
    }

    public void pushValue(short s){
        if(shortStack == null){
            shortStack = new short[STACk_SIZE];
        }
        shortStack[++shortStackTop] = s;
    }

    public void pushValue(int i){
        if(intStack == null){
            intStack = new int[STACk_SIZE];
        }
        intStack[++intStackTop] = i;
    }

    public void pushValue(long l){
        if(longStack == null){
            longStack = new long[STACk_SIZE];
        }
        longStack[++longStackTop] = l;
    }

    public void pushValue(float f){
        if(floatStack == null){
            floatStack = new float[STACk_SIZE];
        }
        floatStack[++floatStackTop] = f;
    }

    public void pushValue(double d){
        if(doubleStack == null){
            doubleStack = new double[STACk_SIZE];
        }
        doubleStack[++doubleStackTop] = d;
    }

    public void pushValue(Object o){
        if(objectStack == null){
            objectStack = new Object[STACk_SIZE];
        }
        objectStack[++objectStackTop] = o;
    }

    public boolean popValueBoolean(){
        return booleanStack[booleanStackTop--];
    }

    public byte popValueByte(){
        return byteStack[byteStackTop--];
    }

    public char popValueChar(){
        return charStack[charStackTop--];
    }

    public short popValueShort(){
        return shortStack[shortStackTop--];
    }

    public int popValueInt(){
        return intStack[intStackTop--];
    }

    public long popValueLong(){
        return longStack[longStackTop--];
    }

    public float popValueFloat(){
        return floatStack[floatStackTop--];
    }

    public double popValueDouble(){
        return doubleStack[doubleStackTop--];
    }

    public Object popValueObject(){
        return objectStack[objectStackTop--];
    }
}
