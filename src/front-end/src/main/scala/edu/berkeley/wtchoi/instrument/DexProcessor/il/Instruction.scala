package edu.berkeley.wtchoi.instrument.DexProcessor.il

import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import org.ow2.asmdex.structureCommon.Label
import org.ow2.asmdex.MethodVisitor
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

//Instruction class derived from ASM-DEX1.0 MethodVisitor class
abstract sealed class Instruction{
  def opcode():Opcode
  def size():Int
  def forceVisit(mv:MethodVisitor):Unit

  implicit def op2int(op:Opcode):Int = op.encode()
}


case class ArrayLengthInsn(destinationRegister: Int, arrayReferenceBearing: Int) extends Instruction{
  override def opcode() = Opcode.ARRAY_LENGTH
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitArrayLengthInsn(destinationRegister, arrayReferenceBearing)
}


case class ArrayOperationInsn(op: Opcode, valueRegister: Int, arrayRegister: Int, indexRegister: Int) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitArrayOperationInsn(op, valueRegister, arrayRegister, indexRegister)
}


case class FieldInsn(op: Opcode, owner: String, name: String, desc: String, valueRegister: Int, objectRegister: Int) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitFieldInsn(op, owner, name, desc, valueRegister, objectRegister)
}


case class FillArrayDataInsn(arrayReference: Int, arrayData: Array[Object]) extends Instruction{
  override def opcode() = Opcode.FILL_ARRAY_DATA
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitFillArrayDataInsn(arrayReference, arrayData)
}


case class Insn(op: Opcode) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitInsn(op)
}


case class IntInsn(op: Opcode, register: Int) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()
  override def forceVisit(mv:MethodVisitor)
  = mv.visitIntInsn(op, register)
}


case class JumpInsn(op: Opcode, label: Label, registerA: Int, registerB: Int) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitJumpInsn(op, label, registerA, registerB)
}


case class LookupSwitchInsn(register: Int, dflt: Label, keys: Array[Int], labels: Array[Label]) extends Instruction{
  override def opcode() = Opcode.SPARSE_SWITCH
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitLookupSwitchInsn(register, dflt, keys, labels)
}


case class MethodInsn(op: Opcode, owner: String, name: String, desc: String, arguments: Array[Int]) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitMethodInsn(op, owner, name, desc, arguments)
}

//??? desc:Opcode
case class MultiANewArrayInsn(desc: String, registers: Array[Int]) extends Instruction{
  override def opcode() = Opcode.FILLED_NEW_ARRAY //XX
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitMultiANewArrayInsn(desc, registers)
}


case class OperationInsn(op: Opcode, destinationReg: Int, firstSourceReg: Int, secondSourceReg: Int, value: Int) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitOperationInsn(op, destinationReg, firstSourceReg, secondSourceReg, value)
}


case class StringInsn(op: Opcode, destinationReg: Int, string: String) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitStringInsn(op, destinationReg, string)
}


case class TableSwitchInsn(register: Int, min: Int, max: Int, dflt: Label, labels: Array[Label]) extends Instruction{
  override def opcode() = Opcode.PACKED_SWITCH
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitTableSwitchInsn(register, min, max, dflt, labels)
}


case class TypeInsn(op: Opcode, destinationReg: Int, referenceBearingReg: Int, sizeReg: Int, string: String) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitTypeInsn(op, destinationReg, referenceBearingReg, sizeReg, string)
}


case class VarInsn(op: Opcode, destinationReg: Int, value: Int) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitVarInsn(op, destinationReg, value)
}


case class VarInsnL(op: Opcode, destinationReg: Int, value: Long) extends Instruction{
  override def opcode() = op
  override def size():Int = opcode.size()

  override def forceVisit(mv:MethodVisitor)
  = mv.visitVarInsn(op, destinationReg, value)
}
