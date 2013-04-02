package edu.berkeley.wtchoi.instrument.DexProcessor.spec

import collection.immutable.Queue
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.Command
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{MethodInfo, BasicBlock}

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
abstract class MethodSpecDSL extends MethodSpecification with DSL{
  abstract sealed class Mode
  case object EntryMode extends Mode
  case object ExitMode extends Mode
  case object HandlerMode extends Mode

  private var mode:Mode = EntryMode

  def setMode(mode:Mode) = this.mode = mode

  override protected final def code():Queue[Command] = {
    mode match {
      case EntryMode => entryCodeBlock
      case ExitMode => exitCodeBlock
      case HandlerMode => handlerCodeBlock
    }
  }

  override protected final def addToCode(c:Command) = {
    mode match{
      case EntryMode => entryCodeBlock += c
      case ExitMode => exitCodeBlock += c
      case HandlerMode => handlerCodeBlock += c
    }
  }

  final override def atBegin(bb:BasicBlock, mi:MethodInfo){
    mode = EntryMode
    atBeginImp(bb, mi)
  }

  final override def atEntry(bb:BasicBlock, mi:MethodInfo){
    mode = EntryMode
    atEntryImp(bb, mi)
  }

  final override def atExit(bb:BasicBlock, mi:MethodInfo){
    mode = EntryMode
    atExitImp(bb,mi)
  }

  final override def atThrow(bb:BasicBlock, mi:MethodInfo){
    mode = EntryMode
    atThrowImp(bb,mi)
  }

  final override def atBasicBlock(bb:BasicBlock, mi:MethodInfo){
    mode = EntryMode
    atBasicBlockImp(bb, mi)
  }

  final override def atBeforeInvoke(bb:BasicBlock, mi:MethodInfo){
    mode = EntryMode
    atBeforeInvokeImp(bb, mi)
  }

  final override def atAfterInvoke(bb:BasicBlock, mi:MethodInfo){
    mode = ExitMode
    atAfterInvokeImp(bb,mi)
  }

  final override def installHandler(bb:BasicBlock, mi:MethodInfo){
    mode = HandlerMode
    installHandlerImp(bb, mi)
  }

  def atBeginImp(bb:BasicBlock, mi:MethodInfo) {}
  def atEntryImp(bb:BasicBlock, mi:MethodInfo) {}
  def atExitImp(bb:BasicBlock, mi:MethodInfo) {}
  def atThrowImp(bb:BasicBlock, mi:MethodInfo) {}
  def atBasicBlockImp(bb:BasicBlock, mi:MethodInfo) {}
  def atBeforeInvokeImp(bb:BasicBlock, mi:MethodInfo) {}
  def atAfterInvokeImp(bb:BasicBlock, mi:MethodInfo){}
  def installHandlerImp(bb:BasicBlock, mi:MethodInfo){}
}
