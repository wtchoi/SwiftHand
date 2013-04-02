package edu.berkeley.wtchoi.instrument.DexProcessor.spec

import edu.berkeley.wtchoi.instrument.DexProcessor.il.{MethodInfo, BasicBlock}
import collection.immutable.Queue
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.{ExpVar, ExpShort}


/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/16/12
 * Time: 10:54 AM
 *
 * SwiftHand Project follows BSD License
 *
 * [The "BSD license"]
 * Copyright (c) 2013 Wontae Choi.
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
class MethodSpecVirtual extends MethodSpecDSL{

  override def atBeginImp(bb:BasicBlock, mi:MethodInfo){
    invokeSV (C.supervisor, "logEnter", Queue(ExpShort(mi.id.toShort)));
    invokeSV (C.supervisor, "logReceiver", Queue('thisObject, ExpShort(mi.id.toShort)))
  }

  override def atBasicBlockImp(bb:BasicBlock, mi:MethodInfo){
    if (bb.isExceptionHandlerSuccesor()){
      //exception handling part1
      invokeSV (C.supervisor, "logCatch", Queue(ExpShort(mi.id.toShort)))
    }

    if (bb.isExceptionHandlerSuccesor() || bb.isBranchSuccesor() || bb.isEntryBlock() || bb.isExitBlock()){
      InstrumentationCounter.addDecisionPoint(bb.id, mi.id)
      bb.setDecisionPointHead()
      invokeSV (C.supervisor, "logDecisionPoint", Queue(bb.id(), ExpShort(mi.id.toShort)))
    }
    else if (bb.isInvokeBlock()){
      invokeSV (C.supervisor, "logProgramPoint", Queue(bb.id(), ExpShort(mi.id.toShort)))
    }
    else{
      invokeSV (C.supervisor, "logProgramPointExtra", Queue(bb.id(), ExpShort(mi.id.toShort)))
      //invokeSV (C.supervisor, "logProgramPoint", Queue(bb.id(), ExpShort(mi.id.toShort)))
    }
  }

  override def atExitImp(bb:BasicBlock, mi:MethodInfo){
    invokeSV (C.supervisor, "logExit", Queue(ExpShort(mi.id.toShort)))
  }

  override def atThrowImp(bb:BasicBlock, mi:MethodInfo){
    invokeSV (C.supervisor, "logThrow", Queue(ExpShort(mi.id.toShort)))
  }

  override def atBeforeInvokeImp(bb:BasicBlock, mi:MethodInfo){
    //if (! (bb.isInvokeDirect() || bb.isInvokeStatic()))
    invokeSV (C.supervisor, "logCall", Queue(ExpShort(mi.id.toShort)))
  }

  override def atAfterInvokeImp(bb:BasicBlock, mi:MethodInfo){
    //if (bb.isInvokeLowerBlock() && !(bb.isInvokeDirectLowerBlock() || bb.isInvokeStaticLowerBlock()))
      invokeSV (C.supervisor, "logReturn", Queue(ExpShort(mi.id.toShort)))
    //else if (bb.isInvokeBlock() && !(bb.isInvokeDirect() || bb.isInvokeStatic()))
    //  invokeSV (C.supervisor, "logReturn", Queue(ExpShort(mi.id.toShort)))
  }

  //exception handling part
  override def installHandlerImp(bb:BasicBlock, mi:MethodInfo){
    if (!bb.includingTryBlock.isDefined && !bb.isExceptionHandlerEntry() && !bb.isExitBlock() && !bb.isThrowBlock()){
      variable("x", 'Exception)
      loadExceptionTo("x")
      invokeSV (C.supervisor, "logUnroll", Queue(ExpShort(mi.id.toShort), ExpVar("x")))
      throwException("x")
    }
  }
}
