package edu.berkeley.wtchoi.instrument.DexProcessor.memostack;

import java.util.LinkedList;
/*
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

//Thread Local Memo Stack
public class MemoStack {

    private static ThreadLocal<LinkedList<Page>> threadLocalStacks = new ThreadLocal<LinkedList<Page>>();
    private static ThreadLocal<Page> threadLocalTopPages = new ThreadLocal<Page>();

    private static LinkedList<Page> getStack(){
        LinkedList<Page> stack = threadLocalStacks.get();
        if(stack == null){
            stack = new LinkedList<Page>();
            threadLocalStacks.set(stack);
        }
        return stack;
    }

    private static Page getTopPage(){
        return threadLocalTopPages.get();
    }

    //push and pop pages
    public static void pushNewPage(){
        Page newPage = new Page();
        getStack().push(newPage);
        threadLocalTopPages.set(newPage);
    }

    public static void popPage(){
        LinkedList<Page> stack = getStack();
        stack.pop();
        //should check whether getLast return null if there is no element left
        threadLocalTopPages.set(stack.getLast());
    }

    //push values in page
    public static void pushValue(boolean b){
        getTopPage().pushValue(b);
    }

    public static void pushValue(byte b){
        getTopPage().pushValue(b);
    }

    public static void pushValue(char c){
        getTopPage().pushValue(c);
    }

    public static void pushValue(short s){
        getTopPage().pushValue(s);
    }

    public static void pushValue(int v){
        getTopPage().pushValue(v);
    }

    public static void pushValue(long l){
        getTopPage().pushValue(l);
    }

    public static void pushValue(float f){
        getTopPage().pushValue(f);
    }

    public static void pushValue(double d){
        getTopPage().pushValue(d);
    }

    public static void pushValue(Object o){
        getTopPage().pushValue(o);
    }

    public static boolean popValueBoolean(){
        return getTopPage().popValueBoolean();
    }

    public static byte popValueByte(){
        return getTopPage().popValueByte();
    }

    public static char popValueChar(){
        return getTopPage().popValueChar();
    }

    public static short popValueShort(){
        return getTopPage().popValueShort();
    }

    public static int popValueInt(){
        return getTopPage().popValueInt();
    }

    public static long popValueLong(){
        return getTopPage().popValueLong();
    }

    public static float popValueFloat(){
        return getTopPage().popValueFloat();
    }

    public static double popValueDouble(){
        return getTopPage().popValueDouble();
    }

    public static Object popValueObject(){
        return getTopPage().popValueObject();
    }
}
