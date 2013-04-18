package edu.berkeley.wtchoi.instrument.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 8/15/12
 * Time: 12:21 AM
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
public class Debug {

    public interface AbortListener{
        public void acceptAbort();
    }

    private static Vector<AbortListener> listeners = new Vector<AbortListener>();
    public static synchronized void registerAbortListener(AbortListener listener){
        listeners.add(listener);
    }

    private static void abort(){
        for(AbortListener l:listeners){
            l.acceptAbort();
        }
        for(Writer w:redirection){
            try{
                w.flush();
            }
            catch(Exception e){
                e.printStackTrace();
                throw new RuntimeException("Redirection flush failed in Abort situation");
            }
        }
    }

    public static void notImplemented(Object obj){
        abort();
        throw new RuntimeException("NotImplemented!:" + obj.toString());
    }


    public static void WTF(String message){
        abort();
        throw new RuntimeException("WTF: " + message);
    }

    public static void Warning(String message){
        println("Warning:" + message);
    }


    private static LinkedList<PrintWriter> redirection = new LinkedList<PrintWriter>();
    public static void pushRedirection(Writer writer){
        redirection.add(new PrintWriter(writer));
    }

    public static void popRedirection(){
        redirection.removeLast();
    }

    public static void println(String s){
        if(redirection.isEmpty()){
            System.out.println(s);
        }
        else{
            redirection.getLast().println(s);
        }
    }

    public static void print(String s){
        if(redirection.isEmpty()){
            System.out.print(s);
        }
        else{
            redirection.getLast().print(s);
        }
    }
}
