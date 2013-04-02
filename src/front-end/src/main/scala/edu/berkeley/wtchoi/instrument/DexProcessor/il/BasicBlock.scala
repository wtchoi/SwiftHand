package edu.berkeley.wtchoi.instrument.DexProcessor.il

import collection.immutable.Queue
import helper.ExtraInfo
import types.RegisterType

import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
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
class BasicBlock(cfg:CFG, _id:Int){

  def this() = this(null, -1)

  private var mContents: Queue[Instruction] = Queue.empty
  private var mExtraInfo: ExtraInfo = ExtraInfo.create(IL.BB.name)
  private var mIncludingTryBlock:Option[TryBlock] = None
  private var mConnectedTryBlock:Set[TryBlock] = Set.empty
  private var mReachable:Boolean = false

  //Add instruction to the end of block
  def add(inst:Instruction) = mContents = mContents.enqueue(inst)

  //Append instructions from the argument block to the receiver block
  def append(bb:BasicBlock) = mContents ++= bb.mContents

  //foldLeft on Instruction
  def foldLeft[A](a:A)(f:(A,Instruction) => A):A = mContents.foldLeft(a)(f)

  //accessing content of basic block
  def apply(index:Int):Instruction = mContents(index)

  def setIncludingTryBlock(tb:TryBlock){
    mIncludingTryBlock = Some(tb)
  }

  def connectTryBlock(tb:TryBlock){
    mConnectedTryBlock += tb
  }


  def foreachExceptionalPredecessor(f:(BasicBlock) => Unit){
    mConnectedTryBlock.foreach(tb => {
      tb.foreachBB(f)
    })
  }

  def includingTryBlock:Option[TryBlock] = mIncludingTryBlock

  //=================================================
  // Functions required from cfg optimization phase
  //=================================================

  def isReachable():Boolean = mReachable

  def setReachable() = mReachable = true

  //Check whether this block is invoker block
  def isInvokeBlock(): Boolean = {
    return (mContents.size == 1 && mContents(0).opcode().isINVOKE)
  }

  def isInvokeDirect(): Boolean = {
    return (isInvokeBlock() && mContents(0).opcode.isINVOKE_DIRECT)
  }

  def isInvokeStatic(): Boolean = {
    return (isInvokeBlock() && mContents(0).opcode.isINVOKE_STATIC)
  }

  def isInvokeOnlyBlock(): Boolean =
    (isInvokeBlock() && this.succ().length == 1 && !this.succ().last.isInvokeLowerBlock())

  def isInvokeUpperBlock(): Boolean =
    (isInvokeBlock() && this.succ().length == 1 && this.succ().last.isInvokeLowerBlock())

  def isInvokeLowerBlock(): Boolean =
    (mContents.size == 1 && mContents(0).opcode().isMOVE_RESULT)

  def isInvokeDirectLowerBlock():Boolean =
    isInvokeLowerBlock() && this.pred.last.isInvokeDirect()

  def isInvokeStaticLowerBlock():Boolean =
    isInvokeLowerBlock() && this.pred.last.isInvokeStatic()

  //Check whether this block is return block
  def isReturnBlock():Boolean = {
    try{
      return (mContents(0).opcode().isRETURN)
    }
    catch{
      case _ => 1 == 1
    }
    return false
  }

  //Check whether this block is exception handler entry
  def isExceptionHandlerEntry():Boolean = return (mContents.size == 1 && mContents(0).opcode().isMOVE_EXCEPTION)

  //Check whether this block is throw block
  def isThrowBlock(): Boolean = return (mContents.size == 1 && mContents(0).opcode().isTHROW)

  def isClassAcessingBlock():Boolean = {
    if (mContents.size == 1){
      val op = mContents(0).opcode();
      return op.encode() == Opcode.NEW_INSTANCE.encode() || op.isSPUT || op.isSGET || op.isIGET || op.isIPUT
    }
    return false;
  }

  def isSignificantBlock():Boolean = return (isInvokeBlock() || isReturnBlock() || isExceptionHandlerEntry() || isThrowBlock())


  //==================================================
  // Functions required from instrumentation phase
  //==================================================

  def isExceptionHandlerSuccesor():Boolean = {
    val b = pred().length == 1 && pred().head.isExceptionHandlerEntry
    return b
  }

  //This is predecessor of join point
  def isJoinPredecessor():Boolean = {
    return (succ().exists(_.isJoinPoint))
  }

  def isBranchSuccesor():Boolean = {
    return (pred().exists(_.isBranchingPoint()))
  }

  def isEntryBlock():Boolean = cfg.getFirstBlock().id() == id()

  def isExitBlock():Boolean = isReturnBlock()

  //This is join point
  def isJoinPoint():Boolean = return pred().length > 1

  def isBranchingPoint():Boolean = return succ().length > 1

  def isInvokeSuper(): Boolean = {
    if (isInvokeBlock()){
      val MethodInsn(_, owner, name, _, _) = mContents(0)
      return (name == "<init>" && includingClass().isInheritedFrom(owner));
    }
    return false
  }


  def id() = _id
  def getOffset() = _id //TODO:separate id and offset
  def getEndOffset() = getOffset() + mContents.size

  def order() = cfg.blockToOrder(this)
  def size() = mContents.size

  override def equals(block:Any): Boolean = block match {
    case block:BasicBlock => _id == block.id()
    case _ => false
  }

  override def hashCode(): Int  = id()

  def pred():List[BasicBlock] =
    if(cfg.pred.contains(this)) cfg.pred(this).toList
    else{ Nil }

  def succ():List[BasicBlock] =
    if(cfg.succ.contains(this)) cfg.succ(this).toList
    else{ Nil }

  def connectTo(target:BasicBlock){
    cfg.succ += this -> target
    cfg.pred += target -> this
  }

  def isFirstBlock():Boolean = this.id() == cfg.getFirstBlock().id()
  def isLastBlock():Boolean = this.id() == cfg.getLastBlock().id()


  def toDotNodeString():String = {
    var a:String =
      if(cfg.blockToOrder.size != 0 && isReachable())
        id().toString + "[" + mContents.size + "](" + order() + ")"
      else
        id().toString + "[" + mContents.size + "]"

    val live:(Option[IL.LiveSet], Option[IL.LiveSet]) = (getExtra(IL.BB.LiveRegisterIn), getExtra(IL.BB.LiveRegisterOut))
    val types:(Option[IL.RegisterTypes], Option[IL.RegisterTypes]) = (getExtra(IL.BB.RegisterTypeIn), getExtra(IL.BB.RegisterTypeOut))

    (live._1, live._2, types._1, types._2) match {
      case (None, None , _, _) => return a
      case (Some(in), Some(out), None, None) =>
        return a + ("\\nIN:" + in.toString()) + ("\\nOUT:" + out.toString())
      case (Some(in), Some(out), Some(inT), Some(outT)) =>{

        val f:(Map[IL.Register, RegisterType], Set[IL.Register]) => String
          = (tys, lives) => {
          var str = "{"
          tys.toSeq.sortBy(x => x._1).foreach(x => {
            if (lives.contains(x._1)){
              val (reg, typ) = x
              var s = reg.toString + typ.toString
              str += s + " "
            }
            else if (x._1 == IL.RESULT_REGISTER){
              val (_, typ) = x
              var s = "L" + typ.toString
              str += s + " "
            }
            else if (x._1 == IL.RESULT_REGISTER_HIGH){
              val (_, typ) = x
              var s = "H" + typ.toString
              str += s + " "
            }
          })
          str + "}"
        }

        return a + ("\\nIN:" + f(inT, in)) + ("\\nOUT:" + f(outT, out))
      }
    }
  }

  def includingCFG() = cfg
  def includingMethod() = cfg.includingMethod()
  def includingClass() = cfg.includingClass()


  def putExtra(key:String, entry:Object) = mExtraInfo.putExtra(key, entry)
  def getExtra[T](key:String): Option[T] = mExtraInfo.getExtra(key)
  def optExtra[T](key:String): T = mExtraInfo.optExtra(key)

  def setDecisionPointHead(){
    includingCFG().decisionBlockHeads += this;
  }

  def isDecisionPointHead():Boolean = {
    return includingCFG().decisionBlockHeads.contains(this);
  }

  override def toString():String = String.valueOf(_id)
}
