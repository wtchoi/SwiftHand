package edu.berkeley.wtchoi.instrument.DexProcessor.printingVisitor;

import edu.berkeley.wtchoi.instrument.util.IndentingPrintWriter;
import org.ow2.asmdex.ApplicationVisitor;
import org.ow2.asmdex.ClassVisitor;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 7/27/12
 * Time: 2:25 PM
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
public class ApplicationPrintingVisitor extends ApplicationVisitor {

    protected IndentingPrintWriter pw;

    public IndentingPrintWriterWrapper getPrintWriter(){
        return new IndentingPrintWriterWrapper(pw);
    }

    public static class IndentingPrintWriterWrapper{
        private IndentingPrintWriter ipw;
        private List<String> buffer;

        public IndentingPrintWriterWrapper(IndentingPrintWriter ipw){
            this.ipw = ipw;
            this.buffer = new LinkedList<String>();
        }

        public void println(String s){
            ipw.printIndent();
            ipw.println(s);
        }
    }

    public ApplicationPrintingVisitor(int api, ApplicationVisitor av, Writer w){
        super(api, av);
        pw = new IndentingPrintWriter(w);
    }

    public ApplicationPrintingVisitor(int api, Writer w){
        super(api);
        pw = new IndentingPrintWriter(w);
    }

    @Override
    public ClassVisitor visitClass(int access, String name, String[] signature, String superName, String[] interfaces){
        pw.printnln("Class = " + name);
        pw.incrementIndent();

        ClassVisitor chained = super.visitClass(access, name, signature, superName, interfaces);
        return new ClassPrintingVisitor(api, chained, name, pw);
    }

    @Override
    public void visitEnd(){
        pw.decrementIndent();
        pw.flush();
        super.visitEnd();
    }
}
