package edu.berkeley.wtchoi.swift.testing.android.lstar;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.Driver;
import edu.berkeley.wtchoi.swift.util.IdentifierPool;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.CVector;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/21/12
 * Time: 12:54 AM
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
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
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
public class ExpandToCommand extends ICommand{
    private static int typeint = IdentifierPool.getFreshInteger();


    public Integer typeint(){
        return typeint;
    }

    private Integer degree;

    public int compareSameType(ICommand t){
        ExpandToCommand target = (ExpandToCommand) t;
        return degree.compareTo(target.degree);
    }

    protected boolean equalsToSameType(ICommand target){
        ExpandToCommand cmd = (ExpandToCommand) target;
        return degree == cmd.degree;
    }

    @Override
    protected void sendCommandImp(Driver drive){
        throw new RuntimeException("ExpandToCommand should not escape it's boundary!");
    }

    public ExpandToCommand(int i){
        degree = i;
    }

    public CSet<CList<ICommand>> expand(CList<ICommand> prefix, CSet<ICommand> palette){//}, CSet<ICommand> defaultPalette){
        CSet<CList<ICommand>> set = new CSet<CList<ICommand>>();
        if(degree == 0) return set;
        for(ICommand cmd: palette){
            CList<ICommand> input = new CVector<ICommand>();
            input.addAll(prefix);
            input.add(cmd);
            set.add(input);
            if(degree != 1){
                CList<ICommand> input2 = new CVector<ICommand>();
                input2.addAll(input);
                input2.add(new ExpandToCommand(degree - 1));
                set.add(input2);
            }
        }
        /*
        for(ICommand cmd: defaultPalette){
            CList<ICommand> input = new CVector<ICommand>();
            input.addAll(prefix);
            input.add(cmd);
            set.add(input);
            if(degree != 1){
                CList<ICommand> input2 = new CVector<ICommand>();
                input2.add(new ExpandToCommand(degree - 1));
                input2.addAll(input);
                set.add(input2);
            }
        }
        */
        return set;
    }

    public static CList<ICommand> getVector(int degree){
        CList<ICommand> list = new CVector<ICommand>();
        list.add(new ExpandToCommand(degree));
        return list;
    }
}
