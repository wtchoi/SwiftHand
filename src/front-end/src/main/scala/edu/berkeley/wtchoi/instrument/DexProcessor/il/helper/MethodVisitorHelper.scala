package edu.berkeley.wtchoi.instrument.DexProcessor.il.helper

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 9/21/12
 * Time: 3:37 PM
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
import org.ow2.asmdex.MethodVisitor
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import org.ow2.asmdex.structureCommon.Label
import edu.berkeley.wtchoi.instrument.DexProcessor.il._

abstract class MethodVisitorHelper(api:Int, mv:MethodVisitor) extends MethodVisitor(api,mv){

  //Instead of defining number of functions, you can define a single handleInst function.
  def handleInst(inst:Instruction):Unit

  //implicit type conversion
  implicit def Int2Opcode(op:Int):Opcode = Opcode.decode(op)

  //overriding some visitor methods
  override def visitArrayLengthInsn(destinationReg:Int, arrayBearingReferenceReg:Int)
  = handleInst(ArrayLengthInsn(destinationReg, arrayBearingReferenceReg))

  override def visitArrayOperationInsn(op:Int, valueRegister:Int, arrayRegister:Int, indexRegister:Int)
  = handleInst(ArrayOperationInsn(op, valueRegister, arrayRegister, indexRegister))

  override def visitFieldInsn(opcode:Int, owner:String, name:String, desc:String, valueRegister:Int, objectRegister:Int)
  = handleInst(FieldInsn(opcode, owner, name, desc, valueRegister, objectRegister))

  //TODO: Looks like filled-new-array and filled-new-array/range are handled equivalently
  override def visitFillArrayDataInsn(arrayRef:Int, arrayData:Array[Object])
  = handleInst(FillArrayDataInsn(arrayRef, arrayData))

  override def visitInsn(opcode:Int)
  = handleInst(Insn(opcode))

  override def visitIntInsn(opcode:Int, register:Int)
  = handleInst(IntInsn(opcode, register))

  override def visitJumpInsn(opcode:Int, label:Label, regA:Int, regB:Int)
  = handleInst(JumpInsn(opcode, label, regA, regB))

  override def visitLookupSwitchInsn(register:Int, dflt:Label, keys:Array[Int], labels:Array[Label])
  = handleInst(LookupSwitchInsn(register, dflt, keys, labels))

  override def visitMethodInsn(opcode:Int, owner:String, name:String, desc:String, args:Array[Int])
  = handleInst(MethodInsn(opcode, owner, name, desc, args))

  override def visitMultiANewArrayInsn(desc:String, registers:Array[Int])
  = handleInst(MultiANewArrayInsn(desc, registers))

  override def visitOperationInsn(opcode:Int, destReg:Int, reg1:Int, reg2:Int, value:Int)
  = handleInst(OperationInsn(opcode, destReg, reg1, reg2, value))

  override def visitStringInsn(opcode:Int, destReg:Int, string:String)
  = handleInst(StringInsn(opcode, destReg, string))

  override def visitTableSwitchInsn(register:Int, min:Int, max:Int, dflt:Label, labels:Array[Label])
  = handleInst(TableSwitchInsn(register, min, max, dflt, labels))

  override def visitTypeInsn(opcode:Int, destReg:Int, referenceBearingReg:Int, sizeReg:Int, typ:String)
  = handleInst(TypeInsn(opcode, destReg, referenceBearingReg, sizeReg, typ))

  override def visitVarInsn(opcode:Int, destinationReg:Int, variable:Int)
  = handleInst(VarInsn(opcode, destinationReg, variable))

  override def visitVarInsn(opcode:Int, destinationReg:Int, variable:Long)
  = handleInst(VarInsnL(opcode, destinationReg, variable))
}
