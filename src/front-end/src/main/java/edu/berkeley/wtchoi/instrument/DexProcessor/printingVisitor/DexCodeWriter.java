package edu.berkeley.wtchoi.instrument.DexProcessor.printingVisitor;

import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode;
import edu.berkeley.wtchoi.instrument.util.IndentingPrintWriter;
import org.ow2.asmdex.structureCommon.Label;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 9/6/12
 * Time: 1:01 PM
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
class DexCodeWriter{
    private final IndentingPrintWriter pw;
    public DexCodeWriter(IndentingPrintWriter _pw){
        pw = _pw;
    }

    public void print(String s){
        pw.print(s);
    }

    public void maxStack(int maxStackSize){
        pw.println(String.valueOf(maxStackSize));
        pw.flush();
    }

    public void opLabel(Label label){
        pw.println("\t" + DexDecode.decodeLabel(label) + ":");
        pw.flush();
    }

    public void op(int opcode){
        pw.print("\t\t" + DexDecode.decodeOp(opcode) + "(" + String.format("%x", opcode) + "," + Opcode.decode(opcode).size() + ")");
        pw.flush();
    }

    public void op(int opcode, int offset){
        pw.print("\t\t" + DexDecode.decodeOp(opcode) + "(" + String.format("%x", opcode) + "," + Opcode.decode(opcode).size() + "," +  offset+ ")");
        pw.flush();
    }

    public void opstr(String str){
        pw.print("\t\t" + str);
        pw.flush();
    }

    public void label(Label label){
        if(label != null)
            pw.print("\t"+DexDecode.decodeLabel(label));
        else
            pw.print("\tnull");
    }

    public void labels(List<Label> labels){
        if(labels == null){
            pw.print("\tnull");
            return;
        }

        pw.print("\t{");
        boolean flag = false;
        for(Label label: labels){
            if(flag) pw.print(", ");
            else flag = true;

            pw.print(DexDecode.decodeLabel(label));
        }
        pw.print("}");
    }

    public void labels(Label[] labels){
        if(labels == null){
            pw.print("\tnull");
            return;
        }

        pw.print("\t{");
        boolean flag = false;
        for(Label label: labels){
            if(flag) pw.print(", ");
            else flag = true;

            pw.print(DexDecode.decodeLabel(label));
        }
        pw.print("}");
    }

    public void values(int[] values){
        if(values == null){
            pw.print("\tnull");
            return;
        }

        pw.print("\t{");
        boolean flag = false;
        for(int value: values){
            if(flag) pw.print(", ");
            else flag = true;

            pw.print(value);
        }
        pw.print("}");
    }

    public void reg(int register){
        pw.print("\t" + DexDecode.decodeReg(register));
    }

    public void var(int var){
        pw.print("\t" + var);
    }

    public void var(long var){
        pw.print("\t" + var);
    }

    public void str(String str){
        String s = null!=str ? str.replaceAll("\n","\\\\n") : null;
        pw.print("\t" + s);
    }

    public void done(){
        pw.println();
        pw.flush();
    }
}


