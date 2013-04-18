package edu.berkeley.wtchoi.instrument.DexProcessor.printingVisitor; /**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 7/27/12
 * Time: 3:43 PM
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

import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode;
import edu.berkeley.wtchoi.instrument.util.IndentingPrintWriter;
import edu.berkeley.wtchoi.instrument.DexProcessor.asmdexHelper.ExtendedMethodVisitor;
import org.ow2.asmdex.MethodVisitor;
import org.ow2.asmdex.structureCommon.Label;

import java.util.List;

public class MethodPrintingVisitor extends ExtendedMethodVisitor {

    protected final DexCodeWriter writer;
    protected IndentingPrintWriter pw;

    protected int offset = 0;

    public int getOffset(){
        return offset;
    }

    public MethodPrintingVisitor(int api, MethodVisitor mv, IndentingPrintWriter pw) {
        super(api, mv);
        writer = new DexCodeWriter(pw);
        this.pw = pw;
    }

    public MethodPrintingVisitor(int api, IndentingPrintWriter pw){
        super(api, null);
        writer = new DexCodeWriter(pw);
        this.pw = pw;
    }

    @Override
    public void visitComment(String comment){
        pw.printIndent();
        writer.print("//\t" + comment);
        writer.done();
        super.visitComment(comment);
    }

    @Override
    public void visitInsn(int opcode){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.done();

        super.visitInsn(opcode);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitIntInsn(int opcode, int register){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.reg(register);
        writer.done();

        super.visitIntInsn(opcode, register);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitVarInsn(int opcode, int destinationRegister, int var){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.reg(destinationRegister);
        writer.var(var);
        writer.done();

        super.visitVarInsn(opcode, destinationRegister, var);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitVarInsn(int opcode, int destinationRegister, long var){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.reg(destinationRegister);
        writer.var(var);
        writer.done();

        super.visitVarInsn(opcode, destinationRegister, var);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitTypeInsn(int opcode,
                              int destinationRegister,
                              int referenceBearingRegister,
                              int sizeRegister,
                              String type){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.reg(destinationRegister);
        writer.reg(referenceBearingRegister);
        writer.reg(sizeRegister);
        writer.str(type);
        writer.done();

        super.visitTypeInsn(opcode, destinationRegister, referenceBearingRegister, sizeRegister, type);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitFieldInsn(int opcode,
                               String owner,
                               String name,
                               String desc,
                               int valueRegister,
                               int objectRegister){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.str(owner);
        writer.str(name);
        writer.str(desc);
        writer.reg(valueRegister);

        Opcode op = Opcode.decode(opcode);
        if(!(op.isSGET() || op.isSPUT())){
            writer.reg(objectRegister);
        }
        writer.done();

        super.visitFieldInsn(opcode, owner, name, desc, valueRegister, objectRegister);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitMethodInsn(int opcode, java.lang.String owner, java.lang.String name, java.lang.String desc, int[] arguments) {
        pw.printIndent();
        writer.op(opcode, offset);
        writer.str(owner);
        writer.str(name);
        writer.str(desc);
        for(int arg: arguments){
            writer.reg(arg);
        }
        writer.done();

        super.visitMethodInsn(opcode, owner, name, desc, arguments);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitStringInsn(int opcode,
                                int destinationRegister,
                                String string){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.reg(destinationRegister);
        writer.str(string);
        writer.done();

        super.visitStringInsn(opcode, destinationRegister, string);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitOperationInsn(int opcode, int destinationRegister, int firstSourceRegister, int secondSourceRegister, int value){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.reg(destinationRegister);
        writer.reg(firstSourceRegister);
        writer.reg(secondSourceRegister);
        writer.var(value);
        writer.done();

        super.visitOperationInsn(opcode, destinationRegister, firstSourceRegister, secondSourceRegister, value);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitArrayOperationInsn(int opcode, int valueRegister, int arrayRegister, int indexRegister){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.reg(valueRegister);
        writer.reg(arrayRegister);
        writer.reg(indexRegister);
        writer.done();

        super.visitArrayOperationInsn(opcode, valueRegister, arrayRegister, indexRegister);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitFillArrayDataInsn(int arrayReference,
                                       Object[] arrayData){
        pw.printIndent();
        writer.op(Opcode.FILL_ARRAY_DATA.encode(), offset);
        writer.reg(arrayReference);
        writer.done();

        super.visitFillArrayDataInsn(arrayReference, arrayData);
        offset += Opcode.FILL_ARRAY_DATA.size();
    }

    @Override
    public void visitJumpInsn(int opcode,
                              Label label,
                              int registerA,
                              int registerB){
        pw.printIndent();
        writer.op(opcode, offset);
        writer.label(label);
        writer.reg(registerA);
        writer.reg(registerB);
        writer.done();

        super.visitJumpInsn(opcode, label, registerA, registerB);
        offset += Opcode.decode(opcode).size();
    }

    @Override
    public void visitLabel(Label label){
        pw.printIndent();
        writer.opLabel(label);
        super.visitLabel(label);
    }

    @Override
    public void visitTableSwitchInsn(int register,
                                    int min,
                                    int max,
                                    Label dflt,
                                    Label[] labels){
        pw.printIndent();
        writer.op(Opcode.PACKED_SWITCH.encode(), offset);
        writer.reg(register);
        writer.var(min);
        writer.var(max);
        writer.label(dflt);
        writer.labels(labels);
        writer.done();

        super.visitTableSwitchInsn(register, min, max, dflt, labels);
        offset += Opcode.PACKED_SWITCH.size();
    }

    @Override
    public void visitLookupSwitchInsn(int register,
                                      Label dflt,
                                      int[] keys,
                                      Label[] labels){
        pw.printIndent();
        writer.op(Opcode.SPARSE_SWITCH.encode(), offset);
        writer.label(dflt);
        writer.values(keys);
        writer.labels(labels);
        writer.done();

        super.visitLookupSwitchInsn(register, dflt, keys, labels);
        offset += Opcode.SPARSE_SWITCH.size();
    }

    @Override
    public void visitMultiANewArrayInsn(String desc,
                                        int[] register){
        pw.printIndent();
        writer.op(Opcode.FILLED_NEW_ARRAY.encode(), offset);
        writer.str(desc);
        //XXX
        writer.done();

        super.visitMultiANewArrayInsn(desc,register);
        offset += Opcode.FILLED_NEW_ARRAY.size();
    }

    @Override
    public void visitArrayLengthInsn(int destinationRegister,
                                     int arrayReferenceBearing){
        pw.printIndent();
        writer.op(Opcode.ARRAY_LENGTH.encode(), offset);
        writer.reg(destinationRegister);
        writer.reg(arrayReferenceBearing);
        writer.done();

        super.visitArrayLengthInsn(destinationRegister, arrayReferenceBearing);
        offset += Opcode.ARRAY_LENGTH.size();
    }

    @Override
    public void visitFrame(int type, int nLocal, java.lang.Object[] local, int nStack, java.lang.Object[] stack){
        pw.printIndent();
        writer.opstr("FRAME");
        writer.done();
        super.visitFrame(type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitTryCatchBlock(Label start,
                                   Label end,
                                   Label handler,
                                   String type){
        pw.printIndent();
        writer.opstr("TRY_CATCH_BLOCK");
        writer.label(start);
        writer.label(end);
        writer.label(handler);
        writer.str(type);
        writer.done();

        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitLocalVariable(String name,
                                   String desc,
                                   String signature,
                                   Label start,
                                   Label end,
                                   int index){
        pw.printIndent();
        writer.opstr("LOCAL_VARIABLE");
        writer.str(name);
        writer.str(desc);
        writer.str(signature);
        writer.label(start);
        writer.label(end);
        writer.done();

        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitLocalVariable(String name,
                                   String desc,
                                   String signature,
                                   Label start,
                                   List<Label> ends,
                                   List<Label> restarts,
                                   int index){
        pw.printIndent();
        writer.opstr("LOCAL_VARIABLE");
        writer.str(name);
        writer.str(desc);
        writer.str(signature);
        writer.label(start);
        writer.labels(ends);
        writer.labels(restarts);
        writer.done();

        super.visitLocalVariable(name, desc, signature, start, ends, restarts, index);
    }

    @Override
    public void visitLineNumber(int line, Label start){
        pw.printIndent();
        writer.opstr("LINE_NUMBER");
        writer.var(line);
        writer.label(start);
        writer.done();

        super.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocal){
        pw.printIndent();
        writer.maxStack(maxStack);

        super.visitMaxs(maxStack, maxLocal);
    }

    @Override
    public void visitParameters(String[] parameters){
        pw.printIndent();
        writer.print("parameters{");
        boolean flag = false;
        for(String p: parameters){
            if(flag) writer.print(", ");
            else flag = true;
            writer.print(p);
        }
        writer.print("}");
        writer.done();

        super.visitParameters(parameters);
    }

    @Override
    public void visitEnd(){
        pw.decrementIndent();
        super.visitEnd();
    }
}


