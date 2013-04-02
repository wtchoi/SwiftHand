package edu.berkeley.wtchoi.instrument.DexProcessor.typeAnalysis

import edu.berkeley.wtchoi.instrument.DexProcessor.analysisFramework.intraProcedural._
import edu.berkeley.wtchoi.instrument.DexProcessor.il._
import edu.berkeley.wtchoi.instrument.DexProcessor.il.types._
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode


/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/2/13
 * Time: 4:34 AM
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
object TypeTransformer extends ForwardTransformer[AbstractEnvironment[Int,RegisterType]]{
  type Register = IL.Register

  val RESULT_REGISTER = IL.RESULT_REGISTER
  val RESULT_REGISTER_HIGH = IL.RESULT_REGISTER_HIGH

  //Register Type Environment
  type TyEnv = AbstractEnvironment[Register,RegisterType]

  override def forward(inst:Instruction, tyEnv:TyEnv): TyEnv = {
    inst match{
      case ArrayLengthInsn(destinationRegister: Register, arrayReferenceBearing: Int) => {
        tyEnv + (destinationRegister -> RegisterTypeNumeric)
      }

      case ArrayOperationInsn(op: Opcode, valueRegister: Register , arrayRegister: Register, indexRegister: Register) if op.isAGET => {
        if (op.isAGET_WIDE){
          tyEnv + (valueRegister -> RegisterTypeWideLow) + ((valueRegister + 1) -> RegisterTypeWideHigh)
        }
        else{
          val ty = tyEnv(arrayRegister)
          val baseTy = ty.optBaseType()
          tyEnv + (valueRegister -> baseTy)
        }
      }

      case FieldInsn(op, owner, name, desc: String, valueRegister: Int, _) if op.isIGET || op.isSGET=> {
        val ty = Type.typeSequenceOf(desc).head
        ty match{
          case TypeDouble | TypeLong => tyEnv + (valueRegister -> RegisterTypeWideLow) + ((valueRegister+1) -> RegisterTypeWideHigh)
          case _ => tyEnv + (valueRegister -> RegisterType.getRegisterTypeFromType(ty))
        }
      }

      case IntInsn(op: Opcode, register: Register) if op.isMOVE_EXCEPTION => {
        tyEnv + (register-> RegisterTypeObject) //Exception is abstracted
      }

      case IntInsn(op, register) if op.isMOVE_RESULT_WIDE => {
        tyEnv + (register -> tyEnv(RESULT_REGISTER)) + (register + 1 -> tyEnv(RESULT_REGISTER_HIGH)) + (RESULT_REGISTER -> RegisterTypeBottom) + (RESULT_REGISTER_HIGH -> RegisterTypeBottom)
      }

      case IntInsn(op, register) if op.isMOVE_RESULT => {
        tyEnv + (register -> tyEnv(RESULT_REGISTER)) + (RESULT_REGISTER -> RegisterTypeBottom) + (RESULT_REGISTER_HIGH -> RegisterTypeBottom)
      }

      case MethodInsn(op: Opcode, owner: String, name: String, desc: String, arguments: Array[Register]) => {
        val ty = Type.typeSequenceOf(desc).head
        ty match{
          case TypeDouble | TypeLong => tyEnv + (RESULT_REGISTER -> RegisterTypeWideLow) + (RESULT_REGISTER_HIGH -> RegisterTypeWideHigh)
          case TypeVoid => tyEnv + (RESULT_REGISTER -> RegisterTypeBottom) + (RESULT_REGISTER_HIGH -> RegisterTypeBottom)
          case _ => tyEnv + (RESULT_REGISTER -> RegisterType.getRegisterTypeFromType(ty))
        }
      }

      case OperationInsn(op:Opcode, destinationReg: Register, fst:Register, snd:Register, _) if op.isCMP => {
        tyEnv + (destinationReg -> RegisterTypeNumeric)
      }


      case OperationInsn(op: Opcode, destinationReg: Register, firstSourceReg: Register, secondSourceReg: Register, value: Int) if op.isUNOP=> {
        if (op.isUNOP_PUT_WIDE) {
          tyEnv + (destinationReg -> RegisterTypeWideLow) + (destinationReg+1 -> RegisterTypeWideHigh)
        }
        else if (op.isUNOP_PUT_BOOLEAN){ tyEnv + (destinationReg -> RegisterTypeBoolean) }
        else if (op.isUNOP_PUT_BYTE) tyEnv + (destinationReg -> RegisterTypeByte)
        else if (op.isUNOP_PUT_CHAR) tyEnv + (destinationReg -> RegisterTypeChar)
        else if (op.isUNOP_PUT_SHORT) tyEnv + (destinationReg -> RegisterTypeShort)
        else tyEnv + (destinationReg -> RegisterTypeNumeric)
      }

      case OperationInsn(op: Opcode, destinationReg: Register, firstSourceReg: Register, secondSourceReg: Register, value: Int) if op.isBINOP_INT || op.isBINOP_FLOAT => {
        tyEnv + (destinationReg -> RegisterTypeNumeric)
      }

      case OperationInsn(op: Opcode, destinationReg: Register, firstSourceReg: Register, secondSourceReg: Register, value: Int) if op.isBINOP_DOUBLE || op.isBINOP_LONG => {
        tyEnv + (destinationReg -> RegisterTypeWideLow) + (destinationReg+1 -> RegisterTypeWideHigh)
      }

      case OperationInsn(op: Opcode, destinationReg: Register, firstSourceReg: Register, secondSourceReg: Register, value: Int) if op.isBINOP_2ADDR_INT || op.isBINOP_2ADDR_FLOAT => {
        tyEnv + (destinationReg -> RegisterTypeNumeric)
      }

      case OperationInsn(op: Opcode, destinationReg: Register, firstSourceReg: Register, secondSourceReg: Register, value: Int) if op.isBINOP_2ADDR_DOUBLE || op.isBINOP_2ADDR_LONG => {
        tyEnv + (destinationReg -> RegisterTypeWideLow) + (destinationReg+1 -> RegisterTypeWideHigh)
      }

      case OperationInsn(op: Opcode, destinationReg: Register, firstSourceReg: Register, secondSourceReg: Register, value: Int) if op.isBINOP_LIT8 || op.isBINOP_LIT16=> {
        tyEnv + (destinationReg -> RegisterTypeNumeric)
      }

      case StringInsn(op: Opcode, destinationReg: Register, string: String) => {
        tyEnv + (destinationReg -> RegisterTypeObject)
      }

      case TypeInsn(op, destinationReg, referenceBearingRegister, _, string) => {
        op match{
          case Opcode.CONST_CLASS => tyEnv + (destinationReg -> RegisterType.getRegisterTypeFromString(string))
          case Opcode.NEW_ARRAY => tyEnv + (destinationReg -> RegisterType.getRegisterTypeFromString(string))
          case Opcode.NEW_INSTANCE => tyEnv + (destinationReg -> RegisterTypeObject)
          case Opcode.INSTANCE_OF => tyEnv + (destinationReg -> RegisterTypeBoolean)
          case Opcode.CHECK_CAST => tyEnv + (referenceBearingRegister -> RegisterType.getRegisterTypeFromString(string))
        }
      }

      case VarInsn(op: Opcode, destReg, value:Int) if op.isMOVE_REG_WIDE => {
        tyEnv + (destReg -> tyEnv(value)) + (destReg+1 -> tyEnv(value+1))
      }

      case VarInsn(op: Opcode, destinationReg: Register, value: Int) if op.isMOVE_REG => {
        tyEnv + (destinationReg -> tyEnv(value))
      }

      case VarInsn(op, destReg, value) if op.isCONST_VAL => {
        tyEnv + (destReg -> RegisterType.getRegisterTypeFromInt(value))
      }

      case VarInsn(op, destReg, value) if op.isCONST_VAL_WIDE16 => {
        tyEnv + (destReg -> RegisterTypeWideLow) + (destReg + 1 -> RegisterTypeWideHigh)
      }

      case VarInsnL(op, destReg, value) if op.isCONST_VAL_WIDE32 => {
        tyEnv + (destReg -> RegisterTypeWideLow) + (destReg + 1 -> RegisterTypeWideHigh)
      }

      case MultiANewArrayInsn(desc:String, register) => {
        //TODO: check whether this is correct or not
        tyEnv + (RESULT_REGISTER -> RegisterType.getRegisterTypeFromString(desc)) + (RESULT_REGISTER_HIGH -> RegisterTypeBottom)
      }

      case _ => tyEnv
    }
  }
}
