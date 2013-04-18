package edu.berkeley.wtchoi.instrument.DexProcessor.nestedTryResolvingVisitor;

import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode;
import edu.berkeley.wtchoi.instrument.DexProcessor.asmdexHelper.ExtendedMethodVisitor;
import org.ow2.asmdex.MethodVisitor;
import org.ow2.asmdex.structureCommon.Label;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/10/12
 * Time: 9:39 PM
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
public class NestedTryResolvingMethodVisitor extends ExtendedMethodVisitor {

    public final static int FREE_STATE = 1;
    public final static int IN_TRY_STATE = 2;
    public final static int MAYBE_NESTED_TRY_STATE = 3;
    public final static int ENTERING_TRY_STATE = 4;
    public final static int NESTED_TRY_STATE = 5;
    public final static int EXITING_NESTED_TRY_STATE = 6;


    private class HandlerDescriptor{
        public Label handler;
        public String type;

        public HandlerDescriptor(Label handler, String type){
            this.handler = handler;
            this.type = type;
        }
    }

    private int state;
    private Label baseTryBlockStart;    //Assume it is resolved
    private Label baseTryBlockEnd;      //Assume it is resolved
    private Label nestedTryBlockStart;  //Assume it is resolved
    private Label nestedTryBlockEnd;    //Assume it it not resolved
    private Queue<HandlerDescriptor> baseHandlers;
    private boolean facedNestedTryBlock;
    private boolean tryAllExists;

    private Label endLabel;             //Assume it is not resolved


    public NestedTryResolvingMethodVisitor(int API, MethodVisitor mv){
        super(API, mv);
        state = FREE_STATE;
    }

    @Override
    public void visitLabel(Label label){
        switch(state){
            case IN_TRY_STATE:
                //mv.visitLabel(label);
                //if(baseTryBlockEnd.isResolved()){
                if(baseTryBlockEnd == label){
                    state = FREE_STATE;
                    mv.visitLabel(label);
                    //mv.visitInsn(Opcode.NOP.encode());
                    mv.visitLabel(endLabel);
                    endLabel = null;
                    baseTryBlockStart = null;
                    baseTryBlockEnd = null;
                }
                else{
                    state = MAYBE_NESTED_TRY_STATE;
                    nestedTryBlockStart = label;
                }
                break;
            case MAYBE_NESTED_TRY_STATE:
                mv.visitLabel(nestedTryBlockStart);
                nestedTryBlockStart = label;
                break;
            case NESTED_TRY_STATE:
                mv.visitLabel(label);
                //If current label is the end label of the current nested try block,
                //nested try block will become resolved. Also, we are going to change
                //state once visit that label. Thus, we can simply call isResolved
                //to check whether we reached the end of the nested try block.
                if(nestedTryBlockEnd.isResolved()){
                    state = EXITING_NESTED_TRY_STATE;
                }
                break;
            default:
                mv.visitLabel(label);
        }
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type){
        switch(state){
            case FREE_STATE:
                state = IN_TRY_STATE;
                baseTryBlockStart = start;
                baseTryBlockEnd = end;
                baseHandlers = new LinkedList<HandlerDescriptor>();
                baseHandlers.add(new HandlerDescriptor(handler, type));
                endLabel = new Label();
                mv.visitTryCatchBlock(start, endLabel, handler, type);
                break;
            case IN_TRY_STATE:
                baseHandlers.add(new HandlerDescriptor(handler, type));
                mv.visitTryCatchBlock(start, endLabel, handler, type);
                break;
            case MAYBE_NESTED_TRY_STATE:
                nestedTryBlockEnd = end;
                state = ENTERING_TRY_STATE;
                tryAllExists = type == null;
                mv.visitInsn(Opcode.NOP.encode()); //TODO: print if this is directly after base try
                mv.visitLabel(endLabel);
                mv.visitInsn(Opcode.NOP.encode());
                mv.visitLabel(nestedTryBlockStart);
                mv.visitTryCatchBlock(start, end, handler, type);

                endLabel = new Label();
                break;
            default:
                mv.visitTryCatchBlock(start, end, handler, type);
        }
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index){
        switch(state){
            case MAYBE_NESTED_TRY_STATE:
                mv.visitLabel(nestedTryBlockStart);
                nestedTryBlockStart = null;
                state = IN_TRY_STATE;
                mv.visitLocalVariable(name, desc, signature, start, end, index);
                break;
            default:
                mv.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, List<Label> ends, List<Label> restarts, int index){
        switch(state){
            case MAYBE_NESTED_TRY_STATE:
                mv.visitLabel(nestedTryBlockStart);
                nestedTryBlockStart = null;
                state = IN_TRY_STATE;
                mv.visitLocalVariable(name, desc, signature, start, ends, restarts, index);
                break;
            default:
                mv.visitLocalVariable(name, desc, signature, start, ends, restarts, index);
        }
    }

    @Override
    public void visitLineNumber(int line, Label start){
        switch(state){
            case MAYBE_NESTED_TRY_STATE:
                mv.visitLabel(nestedTryBlockStart);
                nestedTryBlockStart = null;
                state = IN_TRY_STATE;
                mv.visitLineNumber(line, start);
                break;
            default:
                mv.visitLineNumber(line, start);
        }
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack){
        switch(state){
            case MAYBE_NESTED_TRY_STATE:
                mv.visitLabel(nestedTryBlockStart);
                nestedTryBlockStart = null;
                state = IN_TRY_STATE;
                mv.visitFrame(type, nLocal, local, nStack, stack);
                break;
            default:
                mv.visitFrame(type, nLocal, local, nStack, stack);
        }
    }

    @Override
    public void visitArrayLengthInsn(int destRegister, int referenceBearingRegister){
        final int d = destRegister;
        final int r = referenceBearingRegister;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitArrayLengthInsn(d, r);
            }
        });
    }

    @Override
    public void visitArrayOperationInsn(int opcode, int valueRegister, int arrayRegister, int indexRegister){
        final int op = opcode;
        final int v = valueRegister;
        final int a = arrayRegister;
        final int i = indexRegister;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitArrayOperationInsn(op, v, a, i);
            }
        });
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc, int valueRegister, int objRegister){
        final int op = opcode;
        final String o = owner;
        final String n = name;
        final String d = desc;
        final int v = valueRegister;
        final int or = objRegister;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitFieldInsn(op, o, n, d, v, or);
            }
        });
    }

    @Override
    public void visitFillArrayDataInsn(int arrayReference, Object[] arrayData){
        final int a = arrayReference;
        final Object[] ad = arrayData;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitFillArrayDataInsn(a, ad);
            }
        });
    }

    @Override
    public void visitInsn(int op){
        final int o = op;
        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitInsn(o);
            }
        });
    }

    @Override
    public void visitIntInsn(int op, int reg){
        final int o = op;
        final int r = reg;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitIntInsn(o, r);
            }
        });
    }

    @Override
    public void visitLookupSwitchInsn(int reg, Label dflt, int[] keys, Label[] labels){
        final int r = reg;
        final Label d = dflt;
        final int[] k = keys;
        final Label[] l = labels;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitLookupSwitchInsn(r, d, k , l);
            }
        });
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, int[] args){
        final int op = opcode;
        final String o = owner;
        final String n = name;
        final String d = desc;
        final int[] a = args;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitMethodInsn(op, o, n, d, a);
            }
        });
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int[] regs){
        final String d = desc;
        final int[] r = regs;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitMultiANewArrayInsn(d, r);
            }
        });
    }

    @Override
    public void visitOperationInsn(int opcode, int destReg, int firstSourceReg, int secondSourceReg, int value){
        final int op = opcode;
        final int d = destReg;
        final int f = firstSourceReg;
        final int s = secondSourceReg;
        final int v = value;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitOperationInsn(op, d, f, s, v);
            }
        });
    }

    @Override
    public void visitStringInsn(int opcode, int destReg, String string){
        final int op = opcode;
        final int d = destReg;
        final String s = string;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitStringInsn(op, d, s);
            }
        });
    }

    @Override
    public void visitTableSwitchInsn(int reg, int min, int max, Label dflt, Label[] labels){
        final int r = reg;
        final int m = min;
        final int M = max;
        final Label d = dflt;
        final Label[] l = labels;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitTableSwitchInsn(r, m, M, d, l);
            }
        });
    }

    @Override
    public void visitTypeInsn(int opcode, int destReg, int refBearingReg, int sizeRegister, String type){
        final int op = opcode;
        final int d = destReg;
        final int r = refBearingReg;
        final int s = sizeRegister;
        final String t = type;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitTypeInsn(op, d, r, s, t);
            }
        });
    }

    @Override
    public void visitVarInsn(int opcode, int destReg, int var){
        final int op = opcode;
        final int d = destReg;
        final int v = var;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitVarInsn(op, d, v);
            }
        });
    }

    @Override
    public void visitVarInsn(int opcode, int destReg, long var){
        final int op = opcode;
        final int d = destReg;
        final long v = var;

        visitInsn(new InsnCallBack() {
            @Override
            public void visitInsn() {
                mv.visitVarInsn(op, d, v);
            }
        });
    }

    private void visitTries(Label start, Label end){
        for(HandlerDescriptor desc:baseHandlers){
            mv.visitTryCatchBlock(start, end, desc.handler, desc.type);
        }
    }

    private void visitInsn(InsnCallBack callback){
        switch(state){
            case MAYBE_NESTED_TRY_STATE:
                mv.visitLabel(nestedTryBlockStart);
                nestedTryBlockStart = null;
                state = IN_TRY_STATE;
                callback.visitInsn();
                break;
            case ENTERING_TRY_STATE:
                state = NESTED_TRY_STATE;
                if(!tryAllExists)
                    visitTries(nestedTryBlockStart, nestedTryBlockEnd);
                callback.visitInsn();
                break;
            case EXITING_NESTED_TRY_STATE:
                state = IN_TRY_STATE;
                callback.visitInsn();
                Label startLabel = new Label();
                mv.visitLabel(startLabel);
                visitTries(startLabel, endLabel);
                mv.visitInsn(Opcode.NOP.encode());
                break;
            default:
                callback.visitInsn();
        }
    }

    abstract private class InsnCallBack{
        abstract public void visitInsn();
    }
}
