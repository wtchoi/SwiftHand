package edu.berkeley.wtchoi.instrument.DexProcessor.liveRegisterAnalysis

import collection.mutable.{Set => MSet, Map => MMap}
import collection.immutable.{Set => ISet, Map => IMap}

import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import edu.berkeley.wtchoi.instrument.util.Debug
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{_}
import collection.mutable


/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 8/17/12
 * Time: 5:37 PM
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


object LiveRegisterAnalysis{
  type LiveSet = IL.LiveSet
  type LiveRegisterTable = IL.LiveRegisterTable

  type DU = (LiveSet, LiveSet)
  type DUTbl = IMap[BasicBlock,DU]

  def analyzeApp(il:ApplicationInfo){

    val analyzeMethod = (method:MethodInfo) =>{
      if(!method.isAbstractMethod() && !method.isNative()){
        val (i,o) = analyzeMethodImp(method)

        if (!method.isStaticMethod()){
          val thisReg = method.getThisRegister().get
          i.foreach((ix) => {
            val (bb, tbl) = ix
            val tbl2 = tbl ++ liveAtHandlerEntry(bb, i, true)
            bb.putExtra(IL.BB.LiveRegisterIn, tbl2 + thisReg)})

          o.foreach((ox) => {
            val (bb, tbl) = ox
            val tbl2 = tbl ++ liveAtHandlerEntry(bb, i, false)
            bb.putExtra(IL.BB.LiveRegisterOut, tbl2 + thisReg)})
        }
        else{
          i.foreach((ix) => {
            val (bb, tbl) = ix
            val tbl2 = tbl ++ liveAtHandlerEntry(bb, i, true)
            bb.putExtra(IL.BB.LiveRegisterIn, tbl2)})

          o.foreach((ox) => {
            val (bb, tbl) = ox
            val tbl2 = tbl ++ liveAtHandlerEntry(bb, i, false)
            bb.putExtra(IL.BB.LiveRegisterOut, tbl2)})
        }
      }
    }

    il.foreachMethod(analyzeMethod)
  }

  private def liveAtHandlerEntry(bb:BasicBlock, liveIN:Map[BasicBlock, LiveSet], isHandlingEntry:Boolean) : LiveSet = {
    var live:LiveSet = Set.empty[IL.Register]

    if (bb.includingTryBlock.isDefined){
      val tryBlock = bb.includingTryBlock.get
      if (true){//(isHandlingEntry || tryBlock.end() > bb.getEndOffset()){
        tryBlock.mHandlers.foreach(x => {
          val handlerEntry = x._2
          live ++= liveIN(handlerEntry)
        })
      }
    }

    return live
  }

  //return (USE, DEF) pair of instruction
  private val summarizeInstruction: ((LiveSet, LiveSet), Instruction) => (LiveSet, LiveSet)
  = (du:(LiveSet, LiveSet), inst:Instruction) => {

    var (d:LiveSet, u:LiveSet) = du
    val tryUse = (reg:Int) => if(!d.contains(reg)) u += reg

    inst match{
      case MethodInsn(_,_,_,_,argRegs) => argRegs.foreach(tryUse)

      //constant loading (int, long) case
      case VarInsn(op,destReg,value) if op.isCONST_VAL => d += destReg
      case VarInsn(op, destReg, value) if op.isCONST_VAL_WIDE16 => d += (destReg, destReg + 1)

      //move between register


      case VarInsn(op,destReg, value) if op.isMOVE_REG_WIDE => {
        tryUse(value); tryUse(value+1)
        d+= (destReg, destReg+1)
      }

      case VarInsn(op,destReg, value) if op.isMOVE_REG
      => {
        tryUse(value)
        d+= destReg
      }


      //constant loading cases
      case VarInsnL(_,destReg,_) => d += (destReg, destReg + 1)

      //jump instructions
      case JumpInsn(op,_,_,_) if op.isGOTO => {}
      case JumpInsn(op,_,reg,_) if op.isTESTZ => tryUse(reg)
      case JumpInsn(op,_,reg1,reg2) if op.isTEST => {tryUse(reg1); tryUse(reg2)}

      // No-argument instructions NOP, RETURN-VOID
      case Insn(op) => {}

      case IntInsn(op,reg) if op.isRETURN || op.isTHROW => {
        tryUse(reg)
        if(op.isRETURN_WIDE) tryUse(reg + 1)
      }

      case IntInsn(op,reg) if op.isMOVE_RESULT || op.isMOVE_EXCEPTION =>{
        d+=reg
        if(op.isMOVE_RESULT_WIDE) d += (reg + 1)
      }

      case IntInsn(op,reg) if op.isMONITOR => {tryUse(reg)}


      case TypeInsn(op,destReg,refBearReg,sizeReg,_) =>{
        op match{
          case Opcode.CHECK_CAST => tryUse(refBearReg)
          case Opcode.INSTANCE_OF => {tryUse(refBearReg); d+= destReg}
          case Opcode.NEW_INSTANCE => d+= destReg
          case Opcode.CONST_CLASS => d+= destReg
          case Opcode.NEW_ARRAY => {tryUse(sizeReg); d+= destReg}
          case _ => Debug.WTF("TypeInstruction should not have Opcode:" + op.toString)
        }
      }

      //IGET
      case FieldInsn(op,_,_,_,valueReg,objectReg) if op.isIGET()
      =>{
        tryUse(objectReg); d+=valueReg
        if (op.isIGET_WIDE) d+= (valueReg + 1)
      }

      //IPUT
      case FieldInsn(op,_,_,_,valueReg,objectReg) if op.isIPUT()
      =>{
        tryUse(objectReg); tryUse(valueReg)
        if (op.isIPUT_WIDE)  tryUse(valueReg+1)
      }


      // SGET
      case FieldInsn(op,_,_,_,valueReg,_) if op.isSGET
      =>{
        d += valueReg
        if (op.isSGET_WIDE) d += (valueReg+1)
      }

      //SPUT
      case FieldInsn(op,_,_,_,valueReg,_) if op.isSPUT
      =>{
        tryUse(valueReg)
        if (op.isSPUT_WIDE) tryUse(valueReg+1)
      }


      //String loadig instruction
      case StringInsn(_,destReg,_) => d+= destReg


      //CMP kind
      case OperationInsn(op, destReg, arg1, arg2, _) if op.isCMP_WIDE
      =>{
        tryUse(arg1); tryUse(arg2);
        tryUse(arg1+1); tryUse(arg2+1)
        d += destReg
      }

      case OperationInsn(op,destReg,arg1,arg2,_) if op.isCMP
      => tryUse(arg1); tryUse(arg2); d += destReg


      //UNOP
      case OperationInsn(op, destReg, arg1, _,_) if op.isUNOP_GET_WIDE
      => tryUse(arg1); tryUse(arg1 + 1); d+= destReg

      case OperationInsn(op, destReg, arg1, _,_) if op.isUNOP_PUT_WIDE
      => tryUse(arg1); d+= (destReg, destReg + 1)

      case OperationInsn(op, destReg, arg1, _,_) if op.isUNOP()
      => tryUse(arg1); d+= destReg

      //BINOP_LIT kind
      case OperationInsn(op,destReg,arg1,_,_) if (op.isBINOP_LIT8 || op.isBINOP_LIT16)
      => tryUse(arg1); d+= destReg

      //BINOP
      case OperationInsn(op, dest, a1, a2, _) if (op.isBINOP_DOUBLE || op.isBINOP_LONG)
      =>{
        tryUse(a1); tryUse(a2);
        tryUse(a1+1); tryUse(a2+1);
        d+= (dest, dest+1)
      }

      case OperationInsn(op,destReg,arg1,arg2,_) if op.isBINOP
      => {tryUse(arg1); tryUse(arg2); d+= destReg}


      //BINOP_2ADDR
      case OperationInsn(op, dest, a1, a2, _) if (op.isBINOP_2ADDR_LONG || op.isBINOP_2ADDR_DOUBLE)
      => {
        tryUse(a1); tryUse(a2)
        tryUse(a1+1); tryUse(a2+1);
        d+= (dest, dest+1)
      }

      case OperationInsn(op,destReg,arg1,arg2,_) if op.isBINOP_2ADDR
      => { tryUse(arg1); tryUse(arg2); d += destReg }

      //AGET
      case ArrayOperationInsn(op,valueReg,arrayReg,indexReg) if op.isAGET
      => {
        tryUse(arrayReg); tryUse(indexReg); d+= valueReg
        if (op.isAGET_WIDE) d+= (valueReg + 1)
      }

      //APUT
      case ArrayOperationInsn(op,valueReg,arrayReg,indexReg) if op.isAPUT
      => {
        tryUse(arrayReg); tryUse(indexReg); tryUse(valueReg)
        if (op.isAPUT_WIDE) tryUse(valueReg + 1)
      }

      //TODO:check whether this is right way
      case FillArrayDataInsn(arrayReference,arrayData) => tryUse(arrayReference)

      case TableSwitchInsn(register,_,_,_,_) => tryUse(register)

      case LookupSwitchInsn(register,_,_,_) => tryUse(register)

      //TODO:check whether this is right way
      case MultiANewArrayInsn(_,args) => args.foreach(tryUse)

      case ArrayLengthInsn(destReg,arrayReferenceReg)
      => {tryUse(arrayReferenceReg); d+=destReg}
    }

    (d,u)
  }

  // return (DEF,USE) summay of basic block
  private val summarizeBlock: (DUTbl,BasicBlock) => DUTbl
  = (duTbl:DUTbl, bb: BasicBlock) => {
    val du = bb.foldLeft(Set.empty[Int], Set.empty[Int])(summarizeInstruction)
    duTbl + (bb -> du)
  }

  implicit object BBOrdering extends Ordering[BasicBlock]{
    override def compare(x:BasicBlock, y:BasicBlock):Int ={
      return y.order().compare(x.order) //why not using ordering?
      //if (x.id == y.id) return 0
      //if (x.id > y.id) return 1
      //return -1
    }
  }

  //return DU Table for given method
  private val analyzeMethodImp: (MethodInfo) => LiveRegisterTable = (method:MethodInfo) => {
    val cfg = method.cfg
    val duTbl:DUTbl = cfg.foldLeftBB(IMap.empty[BasicBlock,DU])(summarizeBlock)

    //start worklist algorithm
    var worklist = new collection.immutable.TreeSet[BasicBlock]()(BBOrdering)
    var In = IMap.empty[BasicBlock, LiveSet]
    var Out = IMap.empty[BasicBlock, LiveSet]

    val f = (bb:BasicBlock) =>{
      if (bb.isReachable()){
        worklist += bb
        Out += (bb -> Set.empty[Int])
      }
    }

    cfg.foreachBB(f)

    Debug.println ("Liveness start :" + method.getQuantifiedName() + "\t" + method.descriptor())
    Debug.println ("--------------")

    var iterationCount = 1

    while(! worklist.isEmpty){
      Debug.println ("Iter #" + (iterationCount) + "\t" + worklist.toSeq.sortBy(bb => bb.order()))
      iterationCount + iterationCount + 1

      val work:BasicBlock = worklist.last
      worklist -=  work

      val liveIn: Set[Int] = (Out(work) -- duTbl(work)._1) ++ duTbl(work)._2

      val updatePred:BasicBlock => Unit = (bb) =>{
        if(! liveIn.subsetOf(Out(bb))){
          Out += bb -> (Out(bb) ++ liveIn)
          worklist = worklist + bb
        }
      }

      In += work -> liveIn
      work.pred().foreach(updatePred)

      //Handling exceptional control flow.
      //Note that current implementation under-approximate exceptional control flow.
      //Proper implementation would be the one that push liveIn of exception handler to
      //all liveOut of all instructions of all connected try blocks.
      if (work.isExceptionHandlerEntry()){
        work.foreachExceptionalPredecessor(updatePred)
      }
    }

    Debug.println ("Liveness RESULT")
    Debug.println ("---------------")
    Debug.println ("DU  : " + duTbl.toString())
    Debug.println ("In  : " + In.toString())
    Debug.println ("Out : " + Out.toString())

    (In,Out)
  }
}
