package edu.berkeley.wtchoi.instrument.DexProcessor.instrument

import compiler.BlockCompiler
import org.ow2.asmdex.MethodVisitor
import collection.immutable.Queue
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import java.util.{NoSuchElementException, Random}

import scala.collection.immutable.Map
import edu.berkeley.wtchoi.instrument.DexProcessor.il.helper.MethodVisitorHelper
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{_}
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
class InstrumentingMethodVisitor(methodInfo:MethodInfo, api: Int, mv: MethodVisitor, ca:CodeAssignment)
  extends MethodVisitorHelper(api, mv){

  private var rr:RegisterRemapping = null

  private var instructionsForEntry = Map.empty[BasicBlock,Queue[Instruction]]
  private var instructionsForExit = Map.empty[BasicBlock,Queue[Instruction]]
  private var instructionsForHandler = Map.empty[BasicBlock, Queue[Instruction]]

  private var handlersToInstall:Queue[(Label, Handler)] = Queue.empty
  private var baseHandlerEnd : Label = null

  abstract class Handler
  case class IntentionalHandler(body:Queue[Instruction]) extends Handler
  case class InstrumentHandler() extends Handler

  init()

  var offset:Int = 0
  var labelCount = 0

  private def getNewLabel(): Label = {
    labelCount = labelCount + 1
    val l =  new Label()
    //l.setLine(0)
    //l.setOffset(-1 * labelCount)
    return l
  }


  private var currentInstruction:Instruction = null
  private var currentBasicBlock:BasicBlock = null

  override def handleInst(inst:Instruction){
    currentInstruction = inst

    tryUpdateCurrentBasicBlock()
    tryEntryInstrumentation()

    tryOpenTryBlock()
    modifyAndVisitCurrentInstruction()

    tryCloseTryBlock()
    tryExitInstrumentation()
    progress()
  }


  override def visitTryCatchBlock(start:Label, end:Label, handler:Label, typ:String){
    mv.visitTryCatchBlock(start, end, handler, typ)
    //userHandlerEndLabels += end
    baseHandlerEnd = end
  }

  override def visitLabel(label:Label){
    mv.visitLabel(label)
    if(label == baseHandlerEnd){
      baseHandlerEnd = null
    }
  }

  override def visitEnd(){
    if(baseHandlerEnd != null){
      mv.visitLabel(baseHandlerEnd)
      baseHandlerEnd = null
    }

    handlersToInstall.foreach(x => {
      val (label,handler) = x
      mv.visitLabel(label)

      handler match{
        case InstrumentHandler() => {
          mv.visitIntInsn(Opcode.MOVE_EXCEPTION.encode(), 0)
          mv.visitMethodInsn(Opcode.INVOKE_VIRTUAL.encode(), "Ljava/lang/Exception;", "printStackTrace", "V", Array(0))
          mv.visitTypeInsn(Opcode.NEW_INSTANCE.encode(), 0, 0, 0, "Ljava/lang/RuntimeException;")
          mv.visitStringInsn(Opcode.CONST_STRING.encode(), 1, "Instrumentation failed!")
          mv.visitMethodInsn(Opcode.INVOKE_DIRECT.encode(), "Ljava/lang/RuntimeException;", "<init>", "VLjava/lang/String;", Array(0, 1))
          mv.visitIntInsn(Opcode.THROW.encode(), 0)
        }
        case IntentionalHandler(body) => {
          body.foreach(_.forceVisit(mv))
        }
      }
    })
    mv.visitEnd()
  }

  private def isEnteringBasicBlock(): Boolean = {
    val bbOpt = methodInfo.cfg.getBB(offset)
    return bbOpt.isDefined
  }

  private def isExitingBasicBlock(): Boolean = {
    val bbOpt = methodInfo.cfg.getBB(offset + currentInstruction.size())
    val isEndOfLastBlock = (bbOpt.isEmpty && currentBasicBlock.isLastBlock)
    val isEndOfUsualBlock = (bbOpt.isDefined && currentBasicBlock != bbOpt.get)
    return isEndOfUsualBlock || isEndOfLastBlock
  }


  private def getCurrentBB(): BasicBlock = methodInfo.cfg.getBB(offset).get

  private def progress(){
    offset += currentInstruction.size()
  }

  private def modifyAndVisitCurrentInstruction(){
    val modifiedInstruction = modifyInstruction(currentInstruction)
    modifiedInstruction.forceVisit(mv)
  }

  private def modifyInstruction(inst:Instruction):Instruction = {
    inst match{
      case ArrayLengthInsn(destReg, arrayReg) =>
        ArrayLengthInsn(rr(destReg), rr(arrayReg))

      case ArrayOperationInsn(op, valueReg, arrayReg, indexReg) =>
        ArrayOperationInsn(op, rr(valueReg), rr(arrayReg), rr(indexReg))

      case FieldInsn(op, owner, name, desc, valueReg, objectReg) =>
        FieldInsn(op, owner, name, desc, rr(valueReg), rr(objectReg))

      //TODO: check whether this is the right way
      case FillArrayDataInsn(arrayReference, arrayData) =>
        FillArrayDataInsn(rr(arrayReference), arrayData)


      //Skiped because there is no register
      //case Insn(op)

      case IntInsn(op, valueReg) =>
        IntInsn(op, rr(valueReg))


      case JumpInsn(op, label, regA, regB) =>
        JumpInsn(op, label, rr(regA), rr(regB))

      case LookupSwitchInsn(reg, dflt, keys, labels) =>
        LookupSwitchInsn(rr(reg), dflt, keys, labels)

      case MethodInsn(op, owner, name, desc, arguments) =>
        MethodInsn(op, owner, name, desc, arguments.map(rr(_)))

      case OperationInsn(op, destReg, regA, regB, value) =>
        OperationInsn(op, rr(destReg), rr(regA), rr(regB), value)

      case StringInsn(op, destReg, string) =>
        StringInsn(op, rr(destReg), string)

      case TableSwitchInsn(reg, min, max, dflt, labels) =>
        TableSwitchInsn(rr(reg), min, max, dflt, labels)

      case TypeInsn(op, destReg, referenceBearingReg, sizeReg, string) =>
        TypeInsn(op, rr(destReg), rr(referenceBearingReg), rr(sizeReg), string)

      case VarInsn(op, destReg, value) if op.isCONST_VAL || op.isCONST_VAL_WIDE16 =>
        VarInsn(op, rr(destReg), value)

      case VarInsn(op, destReg, value) if op.isMOVE_REG =>{
        VarInsn(op, rr(destReg), rr(value))
      }


      case VarInsnL(op, destReg, value) =>
        VarInsnL(op, rr(destReg), value)

      case default =>
        inst
    }
  }

  //fix random key to help debug
  val rand:Random = new Random(10)

  val putRandomNops = () => {
    val x = rand.nextInt()%4 + 1
    for (i <- 0 until x)
      mv.visitInsn(Opcode.NOP.encode())
  }

  private def tryUpdateCurrentBasicBlock(){
    if(isEnteringBasicBlock()){
      currentBasicBlock = getCurrentBB()
    }
  }

  private def tryEntryInstrumentation(){
    if(isEnteringBasicBlock()){
      try {
        if (currentBasicBlock.isEntryBlock()) swapParameters()

        val instrument:Queue[Instruction] = instructionsForEntry(currentBasicBlock)
        doInstrument(instrument)
      }
      catch{
        case e:NoSuchElementException=> ()
      }
    }
  }

  private var endLabel:Label = null
  private var additionalHandlerLabel:Label = null

  private def tryOpenTryBlock(){
    if (instructionsForHandler.isDefinedAt(currentBasicBlock)){
      //Entring user provided try-block
      if (isEnteringBasicBlock()){
        val startLabel =  new Label()
        endLabel = new Label()
        if (additionalHandlerLabel == null){
          additionalHandlerLabel = new Label()
          val body = instructionsForHandler(currentBasicBlock)
          handlersToInstall += (additionalHandlerLabel, new IntentionalHandler(body))
        }

        mv.visitLabel(startLabel)
        mv.visitTryCatchBlock(startLabel, endLabel, additionalHandlerLabel, TyException.descriptor())
      }
    }
  }

  private def tryCloseTryBlock(){
    if (isExitingBasicBlock()){
      if (endLabel != null){
        mv.visitLabel(endLabel)
        endLabel = null
      }
    }
  }


  private def tryExitInstrumentation(){
    if (isExitingBasicBlock()){
      try {
        val instrument:Queue[Instruction] = instructionsForExit(currentBasicBlock)
        doInstrument(instrument)
      }
      catch{
        case e:NoSuchElementException => ()
      }
    }
  }


  //Parameter swapping:
  //At the entry of a method, the index of parameter registers are restored to the original index
  //to nullify effect of register pool resizing.
  private def swapParameters(){
    rr.getSwaps.toSeq.sortBy(x => x._1).foreach(x => {
      val (currentR,targetR) = x
      val rtype = rr.getOriginalRegisterTypeAtEntry(currentBasicBlock, targetR)
      if (rtype.isNumericType()){
        mv.visitVarInsn(Opcode.MOVE_16.encode(), targetR, currentR)
      }
      else if (rtype.isObjectType){
        mv.visitVarInsn(Opcode.MOVE_OBJECT_16.encode(), targetR, currentR)
      }
      else if (rtype.isWideLowType()){
        mv.visitVarInsn(Opcode.MOVE_WIDE_FROM16.encode(), targetR, currentR)
      }
      else if (rtype.isWideHighType()){
        //do nothing
      }
      else{
        throw new RuntimeException("Parameter should have concrete type but have type " + rtype)
      }
    })
  }


  //Instrument code:
  //Note that instrumented code is wrapped with a new try-catch block
  //This serves two purpose:
  //1. Failure of instrumented code should not interleave with the original program.
  //   Therefore, program should die, or continue without generating any side effect
  //   when instrumented code failed. In the current implementation, we take the first
  //   alternative.
  //2. To prevent register spilling inside instrumented code to affect execution of
  //   enclosing exception handler which is in the original program.
  private def doInstrument(instrument:Queue[Instruction]){
    if(instrument.length > 0){
      if (false){//(baseHandlerEnd == null){
        instrument.foreach(inst => {inst.forceVisit(mv)})
      }
      else{
        val labelOfTryEntry = getNewLabel()
        val labelOfTryExit = getNewLabel()
        val labelOfHandlerEntry = if (handlerAdded) handlerLabel else getNewLabel()


        mv.visitLabel(labelOfTryEntry)
        mv.visitTryCatchBlock(labelOfTryEntry, labelOfTryExit, labelOfHandlerEntry, null)

        instrument.foreach(inst => {inst.forceVisit(mv)})

        mv.visitLabel(labelOfTryExit)
        mv.visitInsn(Opcode.NOP.encode())

        if(!handlerAdded){
          handlersToInstall = handlersToInstall + Pair(labelOfHandlerEntry, new InstrumentHandler)
          handlerAdded = true
          handlerLabel = labelOfHandlerEntry
        }
      }
    }
  }

  private var handlerAdded:Boolean = false
  private var handlerLabel:Label = null

  override def visitMaxs(maxStack:Int, maxLocals:Int){
    mv.visitMaxs(maxStack + rr.getDemand(), maxLocals)
  }

  private def analyzeDemand(): RegisterCounter = {
    val localRegisterCount:Int = methodInfo.getLocalRegisterCount

    val f:(BasicBlock => IL.LiveSet) => ((BasicBlock, Queue[Command])) => RegisterCounter
    = tbl => x => {
      val (bb, cb) = x
      val liveRegisters:IL.LiveSet = tbl(bb)
      DemandAnalysis(cb, localRegisterCount, liveRegisters)
    }

    val ff :(Map[BasicBlock, Instrumentation.CodeBlock], BasicBlock => IL.LiveSet) => RegisterCounter
    = (ca, tbl) => {
      if (ca == null) new RegisterCounter
      else if (ca.isEmpty) new RegisterCounter
      else ca.map(f(tbl)).foldLeft(new RegisterCounter)(Demand.accumulate)
    }

    type ltbl = BasicBlock => IL.LiveSet
    val t1:ltbl = (bb) => bb.optExtra[IL.LiveSet](IL.BB.LiveRegisterIn)
    val t2:ltbl = (bb) => bb.optExtra[IL.LiveSet](IL.BB.LiveRegisterOut)
    val t3:ltbl = (bb) => Set.empty[IL.Register]

    val demandEntry = ff(ca.getCodeForEntry, t1)
    val demandExit = ff(ca.getCodeForExit, t2)
    val demandHandler = ff(ca.getAdditionalHandler, t3)

    return Demand.accumulate(Demand.accumulate(demandEntry, demandExit), demandHandler)
  }

  private def calculateDelta(mi:MethodInfo, demand:RegisterCounter):Int = {
    demand.counterLow() + demand.counterMid() + demand.counterHigh()
  }

  private def compile(rr: RegisterRemapping) = {
    val handleEntry:((BasicBlock, Queue[Command])) => Unit = x => {
      val (bb,cb) = x
      val compiler = new BlockCompiler(rr, methodInfo)
      instructionsForEntry += (bb -> compiler.compile(cb, bb, true, bb.includingTryBlock.isDefined))
    }

    val handleExit:((BasicBlock, Queue[Command])) => Unit = x => {
      val (bb,cb) = x
      val compiler = new BlockCompiler(rr, methodInfo)
      instructionsForExit += (bb -> compiler.compile(cb, bb, false, bb.includingTryBlock.isDefined))
    }

    val handleHandler:((BasicBlock, Queue[Command])) => Unit = x => {
      val (bb, cb) = x
      val compiler = new BlockCompiler(rr, methodInfo)
      instructionsForHandler += (bb -> compiler.compileSuperMode(cb))
    }

    if(ca.getCodeForEntry() != null)
      ca.getCodeForEntry.foreach(handleEntry)

    if(ca.getCodeForExit() != null)
      ca.getCodeForExit.foreach(handleExit)

    if (ca.getAdditionalHandler() != null)
      ca.getAdditionalHandler().foreach(handleHandler)
  }

  private def init(){
    val demand = analyzeDemand()
    val delta:Int = calculateDelta(methodInfo, demand)
    rr = new RegisterRemapping(methodInfo, delta)
    compile(rr)
  }
}