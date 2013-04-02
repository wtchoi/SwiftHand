package edu.berkeley.wtchoi.instrument.DexProcessor.spec

import edu.berkeley.wtchoi.instrument.DexProcessor.instrument._
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{MethodInsn, BasicBlock, MethodInfo}
import collection.immutable.Queue

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/12/12
 * Time: 1:58 PM
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
abstract class MethodSpecification{

  private var cEntry = Map.empty[BasicBlock, Queue[Command]]
  private var cExit = Map.empty[BasicBlock, Queue[Command]]
  protected var cAdditionalHandler = Map.empty[BasicBlock, Queue[Command]]

  protected var entryCodeBlock:Queue[Command] = null
  protected var exitCodeBlock:Queue[Command] = null
  protected var handlerCodeBlock:Queue[Command] = null

  def getCodeAssignment(mi:MethodInfo):CodeAssignment = {

    val handleBB : BasicBlock => Unit = (bb) => {
      entryCodeBlock = Queue.empty
      exitCodeBlock = Queue.empty
      handlerCodeBlock = Queue.empty

      if (bb.isEntryBlock()){
        atBegin(bb, mi)
        if (!mi.isConstructor() || !mi.includingClass().isInherited())
          atEntry(bb, mi)
      }
      else if (mi.isConstructor() && mi.includingClass().isInherited() && bb.pred().length == 1){
        val predBB = bb.pred().head
        if ((predBB.isInvokeOnlyBlock() && predBB.isInvokeSuper) || (predBB.isInvokeLowerBlock() && predBB.pred().head.isInvokeSuper())){
          atEntry(bb, mi)
        }
      }

      if (!bb.isExceptionHandlerEntry() && !bb.isInvokeLowerBlock())
        atBasicBlock(bb, mi)

      if (bb.isInvokeOnlyBlock()){
        atBeforeInvoke(bb, mi)
        atAfterInvoke(bb, mi)
      }
      else if (bb.isInvokeUpperBlock()){
        atBeforeInvoke(bb, mi)
      }
      else if (bb.isInvokeLowerBlock()){
        atAfterInvoke(bb, mi)
      }

      if (bb.isExitBlock()){
        atExit(bb, mi)
      }
      else if (bb.isThrowBlock()){
        atThrow(bb, mi)
      }

      //wrap basic block with exception handler if necessary
      installHandler(bb, mi)

      if (!entryCodeBlock.isEmpty)
        cEntry += (bb -> entryCodeBlock)
      if (!exitCodeBlock.isEmpty)
        cExit += (bb -> exitCodeBlock)
      if (!handlerCodeBlock.isEmpty)
        cAdditionalHandler += (bb -> handlerCodeBlock)
    }

    mi.cfg().foreachBB(bb => if(bb.isReachable()) handleBB(bb))
    new CodeAssignment(cEntry, cExit, cAdditionalHandler);
  }

  def atBegin(bb:BasicBlock, mi:MethodInfo) {}
  def atEntry(bb:BasicBlock, mi:MethodInfo) {}
  def atBasicBlock(bb:BasicBlock, mi:MethodInfo) {}
  def atBeforeInvoke(bb:BasicBlock, mi:MethodInfo) {}
  def atAfterInvoke(bb:BasicBlock, mi:MethodInfo) {}
  def atExit(bb:BasicBlock, mi:MethodInfo) {}
  def atThrow(bb:BasicBlock, mi:MethodInfo) {}
  def installHandler(bb:BasicBlock, mi:MethodInfo) {}
}