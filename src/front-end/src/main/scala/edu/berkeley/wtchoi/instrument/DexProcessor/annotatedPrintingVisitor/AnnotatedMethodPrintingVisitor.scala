package edu.berkeley.wtchoi.instrument.DexProcessor.annotatedPrintingVisitor

import org.ow2.asmdex.MethodVisitor
import edu.berkeley.wtchoi.instrument.DexProcessor.printingVisitor.MethodPrintingVisitor
import edu.berkeley.wtchoi.instrument.util.IndentingPrintWriter
import edu.berkeley.wtchoi.instrument.DexProcessor.il.MethodInfo
import org.ow2.asmdex.structureCommon.Label
import edu.berkeley.wtchoi.instrument.DexProcessor.printingVisitor.ApplicationPrintingVisitor.IndentingPrintWriterWrapper

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/18/13
 * Time: 10:02 PM
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
class AnnotatedMethodPrintingVisitor(_api:Int, _mv:MethodVisitor, pw:IndentingPrintWriterWrapper, mi:MethodInfo) extends MethodVisitor(_api, _mv){

  val mpv = mv.asInstanceOf[MethodPrintingVisitor]

  def printAnnotation(){
    val offset = mpv.getOffset
    val bbw = mi.cfg.getBB(mpv.getOffset)
    if (bbw.isDefined && bbw.get.isDecisionPointHead()){
      pw.println("// BasicBlock[" + offset + "]")
    }
  }

  override def visitInsn(opcode:Int){
    printAnnotation()
    super.visitInsn(opcode)
  }

  override def visitIntInsn(opcode:Int, register:Int){
    printAnnotation()
    super.visitIntInsn(opcode, register)
  }

  override def visitVarInsn(opcode:Int, destReg:Int, varReg:Int){
    printAnnotation()
    super.visitVarInsn(opcode, destReg, varReg)
  }

  override def visitVarInsn(opcode:Int, destReg:Int, varReg:Long){
    printAnnotation()
    super.visitVarInsn(opcode, destReg, varReg)
  }

  override def visitTypeInsn(opcode:Int, destReg:Int, refBearingReg:Int, sizeReg:Int, typ:String){
    printAnnotation()
    super.visitTypeInsn(opcode, destReg, refBearingReg, sizeReg, typ)
  }

  override def visitFieldInsn(opcode:Int, owner:String, name:String, desc:String, valueReg:Int, objReg:Int){
    printAnnotation()
    super.visitFieldInsn(opcode, owner, name, desc, valueReg, objReg)
  }

  override def visitMethodInsn(opcode:Int, owner:String, name:String, desc:String, arguments:Array[Int]){
    printAnnotation()
    super.visitMethodInsn(opcode, owner, name, desc, arguments)
  }

  override def visitStringInsn(opcode:Int, destReg:Int, string:String){
    printAnnotation()
    super.visitStringInsn(opcode, destReg, string)
  }

  override def visitOperationInsn(opcode:Int, destReg:Int, firstSourceReg:Int, secondSourceReg:Int, value:Int){
    printAnnotation()
    super.visitOperationInsn(opcode, destReg, firstSourceReg, secondSourceReg, value)
  }

  override def visitArrayOperationInsn(opcode:Int, valueReg:Int, arrayReg:Int, indexReg:Int){
    printAnnotation()
    super.visitArrayOperationInsn(opcode, valueReg, arrayReg, indexReg)
  }

  override def visitFillArrayDataInsn(arrayReg:Int, arrayDay:Array[Object]){
    printAnnotation()
    super.visitFillArrayDataInsn(arrayReg, arrayDay)
  }

  override def visitJumpInsn(opcode:Int, label:Label, regA:Int, regB:Int){
    printAnnotation()
    super.visitJumpInsn(opcode, label, regA, regB)
  }

  override def visitTableSwitchInsn(reg:Int, min:Int, max:Int, dflt:Label, labels:Array[Label]){
    printAnnotation()
    super.visitTableSwitchInsn(reg, min, max, dflt, labels)
  }

  override def visitLookupSwitchInsn(register:Int, dflt:Label, keys:Array[Int], labels:Array[Label]){
    printAnnotation()
    super.visitLookupSwitchInsn(register, dflt, keys, labels)
  }

  override def visitMultiANewArrayInsn(desc:String, register:Array[Int]){
    printAnnotation()
    super.visitMultiANewArrayInsn(desc, register)
  }

  override def visitArrayLengthInsn(destReg:Int, arrayRefBearingReg:Int){
    printAnnotation()
    super.visitArrayLengthInsn(destReg, arrayRefBearingReg)
  }
}
