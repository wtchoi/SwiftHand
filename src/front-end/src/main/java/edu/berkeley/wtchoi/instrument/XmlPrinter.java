package edu.berkeley.wtchoi.instrument;

import edu.berkeley.wtchoi.instrument.ApkProcessor.ApkProcessor;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.DumpAdapter;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/18/12
 * Time: 1:59 AM
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
public class XmlPrinter extends ApkProcessor.FileTransformer {
    @Override
    public void transform(InputStream is, OutputStream os){
        try{
            AxmlReader ar = AxmlReader.create(is);
            Writer w = new OutputStreamWriter(System.out);
            DumpAdapter da = new DumpAdapter(w);
            ar.accept(da);

            w.flush();
        }
        catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("Cannot modify XML");
        }
    }
}
