package edu.berkeley.wtchoi.instrument.util;

import java.io.*;
/**
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
//PrintWriter class which simultaneously relaying inputs to superclass and printing it to standard out.
public class EchoingWriter extends MirroringWriter {
    private static final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(System.out)));


    public static EchoingWriter create(String filename) throws IOException{
        File file = new File(filename);
        if(file.exists()) file.delete();
        file.createNewFile();
        FileWriter fw = new FileWriter(file);
        return new EchoingWriter(fw);
    }

    public static EchoingWriter create(String filename, boolean flagMain, boolean flagEcho) throws IOException {
        EchoingWriter ew = create(filename);
        if(!flagMain) ew.disableMain();
        if(!flagEcho) ew.disableEcho();
        return ew;
    }

    public static EchoingWriter create(Writer w){
        return new EchoingWriter(w);
    }

    private EchoingWriter(Writer w){
        super(w, bw);
    }

    public void disableEcho(){
        super.disableMirror();
    }

    public void enableEcho(){
        super.enableMirror();
    }

    public void disableMain(){
        super.disableMain();
    }

    public void enableMain(){
        super.enableMain();
    }
}
