package edu.berkeley.wtchoi.instrument.DexProcessor.ilbuilder.imp.cfgbuilder

import edu.berkeley.wtchoi.instrument.util.Debug
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import edu.berkeley.wtchoi.instrument.DexProcessor.il._
import org.ow2.asmdex.structureCommon.Label
/*
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

abstract class SingletonState(_manager: SingletonStateManager, _builder: CFGBuilderImp) extends State {
  val manager = _manager
  val builder = _builder

  override def acceptInstruction(inst: Instruction): SingletonState = {
    Debug.notImplemented(this)
    return null
  }

  override def acceptLabel(label: Label): SingletonState = {
    Debug.notImplemented(this)
    return null
  }

  override def acceptTryCatchBlock(start: Label, end: Label, handler: Label, typ: String): SingletonState = {
    Debug.notImplemented(this)
    return null
  }

  override def hasType(ty:Int):Boolean = {
    Debug.notImplemented(this)
    return false
  }
}

class InitState(m: SingletonStateManager, b: CFGBuilderImp) extends SingletonState(m, b) {
  override def acceptLabel(label: Label): SingletonState = {
    //ASSUMPTION: Starting offset is alwasy 0
    assert(label.getOffset == 0)

    builder.mHeadOffsets += 0
    builder.mOffsetToLine += (0 -> label.getLine)

    builder.mStartOffset = 0
    builder.setOffset(label.getOffset)
    return manager.getBlockState
  }

  override def acceptInstruction(inst: Instruction): SingletonState = {
    builder.mHandlerOffsets += 0

    builder.mStartOffset = 0
    builder.setOffset(0)
    return manager.getBlockState().acceptInstruction(inst)
  }
}


class BlockState(m: SingletonStateManager, b: CFGBuilderImp) extends SingletonState(m, b) {
  override def acceptLabel(label: Label): SingletonState = {
    builder.connectFlow(builder.mPrevOffset, builder.mCurrentOffset)
    builder.mOffsetToLine += (label.getOffset -> label.getLine)
    return manager.getBlockState
  }

  override def acceptInstruction(inst: Instruction): SingletonState = {
    //Debug.println(inst.opcode() + " offset="+ builder.currentOffset + ", instSize=" + inst.size())
    val instructionSize: Int = inst.opcode.size
    val newOffset: Int = builder.mCurrentOffset + instructionSize

    var next: SingletonState = manager.getBlockState



    inst match {
      //making class accessing instruction a separate block
      case TypeInsn(Opcode.NEW_ARRAY, _, _ , _, _) => {
        builder.connectFlow(builder.mPrevOffset, builder.mCurrentOffset)
        builder.connectFlow(builder.mCurrentOffset, newOffset)
        next = manager.getBlockState()
      }

      case FieldInsn(op, _, _, _, _, _) =>{
        builder.connectFlow(builder.mPrevOffset, builder.mCurrentOffset)
        builder.connectFlow(builder.mCurrentOffset, newOffset)
        next = manager.getBlockState()
      }

      //GOTO
      case JumpInsn(op, label, _, _) if op.isGOTO() => {
        builder.connectFlow(builder.mCurrentOffset, label.getOffset)
        next = manager.getEscapeState
      }

      //conditional jumps
      case JumpInsn(op, label, _, _) => {
        builder.connectFlow(builder.mCurrentOffset, label.getOffset)
        builder.connectFlow(builder.mCurrentOffset, newOffset)
      }

      case MethodInsn(op, _, name, _, _) => {
        if (builder.mCurrentOffset != 0)
          builder.connectFlow(builder.mPrevOffset, builder.mCurrentOffset)

        next = manager.getAfterInvokeState()
        //builder.connectFlow(builder.currentOffset, newOffset)
      }

      case LookupSwitchInsn(_, dflt, _, labels) => {
        builder.connectFlow(builder.mCurrentOffset, dflt.getOffset)
        labels.foreach((l) => builder.connectFlow(builder.mCurrentOffset, l.getOffset))
        next = manager.getEscapeState
      }

      case TableSwitchInsn(_, _, _, dflt, labels) => {
        builder.connectFlow(builder.mCurrentOffset, dflt.getOffset)
        labels.foreach((l) => builder.connectFlow(builder.mCurrentOffset, l.getOffset))
        next = manager.getEscapeState
      }

      case Insn(Opcode.RETURN_VOID) => {
        if(builder.mCurrentOffset != 0)
          builder.connectFlow(builder.mPrevOffset, builder.mCurrentOffset)

        next = manager.getEscapeState
      }

      case IntInsn(Opcode.RETURN_OBJECT, _)
           | IntInsn(Opcode.RETURN, _)
           | IntInsn(Opcode.RETURN_WIDE, _) => {
        if (builder.mCurrentOffset != 0)
          builder.connectFlow(builder.mPrevOffset, builder.mCurrentOffset)

        next = manager.getEscapeState
      }

      case IntInsn(Opcode.MOVE_EXCEPTION, _) => {
        //Move exception is the first command of an event handler block
        //and event handler blocks are usually connected from other blocks
        //by jump. Therefore, we don't have to connect with previous block.

        builder.connectFlow(builder.mCurrentOffset, newOffset)
        next = manager.getBlockState()
      }

      case IntInsn(Opcode.THROW, _) =>{
        if (builder.mCurrentOffset != 0)
          builder.connectFlow(builder.mPrevOffset, builder.mCurrentOffset)

        next = manager.getEscapeState()
      }


      case _ => {
        //Do nothing
      }
    }

    builder.mInstructions += inst
    builder.setOffset(newOffset)
    return next
  }

  override def acceptTryCatchBlock(start: Label, end: Label, handler: Label, typ: String): SingletonState = {
    builder.installTryCatchBlock(start.getOffset, end.getOffset, handler.getOffset, typ)
    return manager.getBlockState
  }
}


class EscapeState(m: SingletonStateManager, b: CFGBuilderImp) extends SingletonState(m, b) {
  override def acceptLabel(l: Label): SingletonState = {
    builder.mOffsetToLine += (l.getOffset -> l.getLine)
    return manager.getEscapeState()
  }

  override def acceptInstruction(inst:Instruction): SingletonState = {
    var next:SingletonState = manager.getEscapeState()

    val newOffset = builder.mCurrentOffset + inst.size()
    inst match{
      case Insn(x) if x == Opcode.NOP => ()

      case MethodInsn(op, _, name, _, _) =>
        next = manager.getAfterInvokeState()

      case TypeInsn(Opcode.NEW_ARRAY, _, _ , _, _) => {
        next = manager.getBlockState()
        builder.connectFlow(builder.mCurrentOffset, newOffset)
      }

      case FieldInsn(op, _, _, _, _, _) =>{
        next = manager.getBlockState()
        builder.connectFlow(builder.mCurrentOffset, newOffset)
      }

      case _ =>
        return manager.getBlockState.acceptInstruction(inst)
        //{Debug.notImplemented(this);()}
    }

    builder.mInstructions += inst
    builder.setOffset(newOffset)
    return next
  }

  override def acceptTryCatchBlock(start: Label, end: Label, handler: Label, typ: String): SingletonState = {
    builder.installTryCatchBlock(start.getOffset, end.getOffset, handler.getOffset, typ)
    return manager.getEscapeState()
  }
}


class AfterInvokeState(m:SingletonStateManager, b:CFGBuilderImp) extends SingletonState(m,b){
  override def acceptTryCatchBlock(start: Label, end: Label, handler: Label, typ: String): SingletonState = {
    builder.installTryCatchBlock(start.getOffset, end.getOffset, handler.getOffset, typ)
    return manager.getAfterInvokeState()
  }

  override def acceptLabel(label:Label):SingletonState = {
    builder.mOffsetToLine += (label.getOffset -> label.getLine)
    return manager.getAfterInvokeState()
  }

  override def acceptInstruction(inst:Instruction):SingletonState = {

    val newOffset = builder.mCurrentOffset + inst.size()

    //Group invoke and move-result into a single invoke block
    if(inst.opcode.isMOVE_RESULT){
      builder.connectFlow(builder.mPrevOffset, builder.mCurrentOffset)
      builder.connectFlow(builder.mCurrentOffset, newOffset)
      builder.mInstructions += inst
      builder.setOffset(newOffset)
      return manager.getBlockState()
    }

    builder.connectFlow(builder.mPrevOffset, builder.mCurrentOffset)
    return manager.getBlockState().acceptInstruction(inst)
  }
}
