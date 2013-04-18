package edu.berkeley.wtchoi.instrument.DexProcessor.printingVisitor;

import edu.berkeley.wtchoi.instrument.util.IndentingPrintWriter;
import org.ow2.asmdex.AnnotationVisitor;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/19/12
 * Time: 6:25 PM
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
public class AnnotationPrintingVisitor extends AnnotationVisitor {

    IndentingPrintWriter pw;
    boolean inArray = false;

    AnnotationPrintingVisitor(int api, AnnotationVisitor av, IndentingPrintWriter pw){
        super(api, av);
        this.pw = pw;
    }

    AnnotationPrintingVisitor(int api, IndentingPrintWriter pw){
        super(api);
        this.pw = pw;
    }


    @Override
    public void visit(String name, Object value){
        tryExitArray();
        pw.printnln(name + " = " + value);
        super.visit(name, value);
    }


    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc){
        tryExitArray();
        pw.printnln(name + " : " + desc);
        pw.incrementIndent();

        AnnotationVisitor chained = super.visitAnnotation(name, desc);
        return new AnnotationPrintingVisitor(api, chained, pw);
    }

    @Override
    public AnnotationVisitor visitArray(String name){
        tryExitArray();
        pw.printnln(name + " { ");
        //pw.incrementIndent();

        AnnotationVisitor chained = super.visitArray(name);
        return new AnnotationPrintingVisitor(api, chained, pw);
    }

    @Override
    public void visitClass(String aName, String cName){
        tryExitArray();
        pw.printnln("class@" + aName + " " + cName);
        super.visitClass(aName, cName);
    }

    @Override
    public void visitEnum(String name, String desc, String val){
        tryExitArray();
        pw.printnln("enum@" + name + " " + desc + " " + val);
        super.visitEnum(name, desc, val);
    }

    @Override
    public void visitEnd(){
        tryExitArray();
        pw.decrementIndent();
        super.visitEnd();
    }

    public void tryExitArray(){
        if(inArray){
            pw.printnln("}");
            pw.decrementIndent();
            inArray = false;
        }
    }

    public void setInArray(){
        inArray = true;
    }
}
