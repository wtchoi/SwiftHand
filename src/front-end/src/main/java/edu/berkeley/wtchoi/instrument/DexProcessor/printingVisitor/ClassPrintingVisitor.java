package edu.berkeley.wtchoi.instrument.DexProcessor.printingVisitor;
/*
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 7/27/12
 * Time: 3:39 PM
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

import edu.berkeley.wtchoi.instrument.util.IndentingPrintWriter;
import org.ow2.asmdex.ClassVisitor;
import org.ow2.asmdex.FieldVisitor;
import org.ow2.asmdex.MethodVisitor;
import org.ow2.asmdex.Opcodes;

public class ClassPrintingVisitor extends ClassVisitor{
    private IndentingPrintWriter pw;
    private String cName;

    public ClassPrintingVisitor(int api, ClassVisitor cv, String cName, IndentingPrintWriter _pw){
        super(api, cv);
        this.cName = cName;
        pw = _pw;
    }

    public ClassPrintingVisitor(int api, String cName, IndentingPrintWriter _pw){
        super(api);
        this.cName = cName;
        pw = _pw;
    }


    private String decodeAccess(int access){
        String s = "";

        boolean flag = false;

        if((Opcodes.ACC_ABSTRACT & access) != 0){
            s = "abstract";
            flag = true;
        }

        if((Opcodes.ACC_STATIC & access) != 0){
            if(flag) s+= "\t";
            s += "static";
            flag = true;
        }
        return (s.length() != 0) ? s + "\t" : s;
    }

    protected void visitMethodPrint(int access, String name, String desc, String[] signature,
                               String[] exceptions) {
        pw.printnln(decodeAccess(access) + cName + " :: " + name);
        pw.printnln(desc + "\t" + desc.hashCode());
        pw.incrementIndent();
    }

    protected MethodVisitor visitMethodCreate(int access, String name, String desc, String[] signature,
                            String[] exceptions){
        MethodVisitor chained = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodPrintingVisitor(api, chained, pw);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String[] signature,
                                     String[] exceptions) {
        visitMethodPrint(access, name, desc, signature, exceptions);
        return visitMethodCreate(access, name, desc, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String[]signature, Object value){
        pw.printnln(decodeAccess(access) + name + "\t" + desc);
        pw.incrementIndent();

        FieldVisitor chained = super.visitField(access, name, desc, signature, value);
        return new FieldPrintingVisitor(api,chained, pw);
    }

    @Override
    public void visitEnd(){
        pw.decrementIndent();
        super.visitEnd();
    }

}
