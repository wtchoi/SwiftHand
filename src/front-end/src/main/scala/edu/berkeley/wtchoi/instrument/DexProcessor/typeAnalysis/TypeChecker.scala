package edu.berkeley.wtchoi.instrument.DexProcessor.typeAnalysis

import edu.berkeley.wtchoi.instrument.DexProcessor.il._
import types._

import edu.berkeley.wtchoi.instrument.DexProcessor.analysisFramework.intraProcedural.AbstractEnvironment
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/3/13
 * Time: 12:50 AM
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
object TypeChecker{
  type TyEnv = AbstractEnvironment[IL.Register,RegisterType]

  def assert(inst:Instruction, tyEnv:TyEnv){
    inst match{
      case ArrayLengthInsn(destinationRegister: IL.Register, arrayReferenceBearing: Int) => {
        tyEnv(arrayReferenceBearing).assertArrayType()
      }

      case ArrayOperationInsn(op: Opcode, valueRegister: IL.Register , arrayRegister: IL.Register, indexRegister: IL.Register) => {
        tyEnv(arrayRegister).assertArrayType()
        if (op.isAGET_WIDE || op.isAPUT_WIDE) tyEnv(arrayRegister).optBaseType().assertWideLowType()

        if(op.isAPUT_WIDE) tyEnv(valueRegister).assertWideLowType()
        else if(op.isAPUT) tyEnv(arrayRegister).optBaseType() match {
          case RegisterTypeObject => tyEnv(valueRegister).assertObjectType()
          case RegisterTypeArray(_) => tyEnv(valueRegister).assertArrayType()  //TODO:imprecise
          case ty => tyEnv(valueRegister).assertLE(RegisterTypeNumeric)
        }
      }

      case FieldInsn(op, owner, name, desc: String, valueRegister: IL.Register, objReg: IL.Register) if op.isIGET || op.isIPUT => {
        val ty = Type.typeSequenceOf(desc).head
        tyEnv(objReg).assertObjectType() //Imprecise.
      }

      case IntInsn(op, register) if op.isRETURN_WIDE => {
        tyEnv(register).assertWideLowType()
        tyEnv(register + 1).assertWideHighType()
      }

      case IntInsn(op, register) if op.isRETURN_OBJECT => tyEnv(register).assertObjectType()
      case IntInsn(op, register) if op.isRETURN => tyEnv(register).assertNumericType()

      //case JumpInsn(op, label, reg1, reg2) if op.isTEST => tyEnv(reg1).assertNumericType();tyEnv(reg2).assertNumericType()
      //case JumpInsn(op, label, reg1, _) if op.isTESTZ => RegisterTypeZero.assertLE(tyEnv(reg1))

      case LookupSwitchInsn(reg, _, _, _) => tyEnv(reg).assertNumericType()

      case MethodInsn(op: Opcode, owner: String, name: String, desc: String, arguments: Array[IL.Register]) => {

        val types = Type.typeSequenceOf(desc)
        //TODO:check arguments type
      }

      case OperationInsn(op:Opcode, destinationReg: IL.Register, fst:IL.Register, snd:IL.Register, _) if op.isCMP => {
        if(op.isCMP_WIDE){
          tyEnv(fst).assertWideLowType()
          tyEnv(fst+1).assertWideHighType()
          tyEnv(snd).assertWideLowType()
          tyEnv(snd+1).assertWideHighType()
        }
        else{
          tyEnv(fst).assertNumericType()
          tyEnv(snd).assertNumericType()
        }
      }


      case OperationInsn(op: Opcode, destinationReg: IL.Register, firstSourceReg: IL.Register, secondSourceReg: IL.Register, value: Int) if op.isUNOP=> {
        if (op.isUNOP_GET_WIDE) {
          tyEnv(firstSourceReg).assertWideLowType()
          tyEnv(firstSourceReg+1).assertWideHighType()
        }
        else if (op.isUNOP_PUT_BOOLEAN) tyEnv(firstSourceReg).assertNumericType()
        else if (op.isUNOP_PUT_BYTE) tyEnv(firstSourceReg).assertNumericType()
        else if (op.isUNOP_PUT_CHAR) tyEnv(firstSourceReg).assertNumericType()
        else if (op.isUNOP_PUT_SHORT) tyEnv(firstSourceReg).assertNumericType()
        else tyEnv(firstSourceReg).assertNumericType()
      }

      case OperationInsn(op: Opcode, destinationReg: IL.Register, firstSourceReg: IL.Register, secondSourceReg: IL.Register, value: Int) if op.isBINOP_INT || op.isBINOP_FLOAT => {
        tyEnv(firstSourceReg).assertNumericType()
        tyEnv(secondSourceReg).assertNumericType()
      }

      case OperationInsn(op: Opcode, destinationReg: IL.Register, firstSourceReg: IL.Register, secondSourceReg: IL.Register, value: Int) if op.isBINOP_DOUBLE || op.isBINOP_LONG => {
        tyEnv(firstSourceReg).assertWideLowType()
        tyEnv(firstSourceReg+1).assertWideHighType()

        if (op.isBINOP_LONG_SH){
          tyEnv(secondSourceReg).assertNumericType()
        }
        else{
          tyEnv(secondSourceReg).assertWideLowType()
          tyEnv(secondSourceReg+1).assertWideHighType()
        }
      }

      case OperationInsn(op: Opcode, destinationReg: IL.Register, firstSourceReg: IL.Register, secondSourceReg: IL.Register, value: Int) if op.isBINOP_2ADDR_INT || op.isBINOP_2ADDR_FLOAT => {
        tyEnv(firstSourceReg).assertNumericType()
        tyEnv(destinationReg).assertNumericType()
      }

      case OperationInsn(op: Opcode, destinationReg: IL.Register, firstSourceReg: IL.Register, secondSourceReg: IL.Register, value: Int) if op.isBINOP_2ADDR_DOUBLE || op.isBINOP_2ADDR_LONG => {
        tyEnv(destinationReg).assertWideLowType()
        tyEnv(destinationReg+1).assertWideHighType()

        if (op.isBINOP_2ADDR_LONG_SH){
          tyEnv(secondSourceReg).assertNumericType()
        }
        else{
          tyEnv(secondSourceReg).assertWideLowType()
          tyEnv(secondSourceReg+1).assertWideHighType()
        }
      }

      case OperationInsn(op: Opcode, destinationReg: IL.Register, firstSourceReg: IL.Register, secondSourceReg: IL.Register, value: Int) if op.isBINOP_LIT8 || op.isBINOP_LIT16=> {
        tyEnv(firstSourceReg).assertNumericType()
      }

      case TypeInsn(op, destinationReg, referenceBearingRegister, sizeReg, string) => {
        op match{
          case Opcode.INSTANCE_OF => tyEnv(referenceBearingRegister).assertObjectType()
          case Opcode.CHECK_CAST => tyEnv(referenceBearingRegister).assertObjectType()
          case Opcode.NEW_ARRAY => tyEnv(sizeReg).assertNumericType()
          case _ => {}
        }
      }

      case VarInsn(op: Opcode, destReg, value:Int) if op.isMOVE_REG_WIDE => {
        tyEnv(value).assertWideLowType()
        tyEnv(value+1).assertWideHighType()
      }

      case VarInsn(op: Opcode, destinationReg: IL.Register, value: Int) if op.isMOVE_REG => {
        if(op.isMOVE_REG_OBJECT) tyEnv(value).assertObjectType();
        else tyEnv(value).assertNumericType();
      }

      case FillArrayDataInsn(reg: IL.Register, arrayData:Array[Object]) => {
        tyEnv(reg).assertArrayType();
      }

      case _ => {}
    }
  }
}
