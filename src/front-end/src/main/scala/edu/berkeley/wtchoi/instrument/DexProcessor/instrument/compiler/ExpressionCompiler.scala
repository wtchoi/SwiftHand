package edu.berkeley.wtchoi.instrument.DexProcessor.instrument.compiler

import edu.berkeley.wtchoi.instrument.DexProcessor.il._
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import edu.berkeley.wtchoi.instrument.util.Debug
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument._

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/12/12
 * Time: 7:47 PM
 * To change this template use File | Settings | File Templates.
 */
object ExpressionCompiler {
  def compileLoadExpression(env:Map[String,IL.Register], tyEnv:Map[String,Ty], exp:Expression, targetReg:IL.Register, context:CompilingContext):Instruction = {
    exp match {
      //case ExpThisAsObject => VarInsn(Opcode.MOVE_OBJECT, targetReg, thisReg.get)

      case ExpThis() => VarInsn(Opcode.MOVE_OBJECT, targetReg, context.getThisRegister().get)

      case ExpInt(i) =>
        if(i < 16) VarInsn(Opcode.CONST_4, targetReg, i)
        else if(i < 65536) VarInsn(Opcode.CONST_16, targetReg, i)
        else VarInsn(Opcode.CONST, targetReg, i)

      case ExpShort(i) =>
        if(i < 16) VarInsn(Opcode.CONST_4, targetReg, i)
        else if(i < 65536) VarInsn(Opcode.CONST_16, targetReg, i)
        else throw new RuntimeException("Unreachable code reached!")

      case ExpStr(s) =>  StringInsn(Opcode.CONST_STRING, targetReg, s)

      case ExpVar(x) =>
        if (targetReg == env(x)) Insn(Opcode.NOP)
        else{
          val ty = exp.ty(tyEnv)
          if (ty.isObjectTy) VarInsn(Opcode.MOVE_OBJECT, targetReg, env(x))
          else VarInsn(Opcode.MOVE,targetReg, env(x))
        }

      case ExpMethodName => StringInsn(Opcode.CONST_STRING, targetReg, context.handlingMethod().getQuantifiedName())

      case ExpOffset => {
        if (context.isSuperMode){
          throw new RuntimeException("Cannot compile ExpOffset in super mode")
        }
        VarInsn(Opcode.CONST_16, targetReg, context.targetBB.getOffset())
      }

      case ExpStaticField(c,f,typ) => {
        val (op, tyDesc) = typ match{
          case TyInt => (Opcode.SGET, typ.descriptor())
          case TyBoolean => (Opcode.SGET_BOOLEAN, typ.descriptor())
          case TyString => (Opcode.SGET_OBJECT, typ.descriptor())
          case TyObject => (Opcode.SGET_OBJECT, typ.descriptor())
          case TyClass(desc) => (Opcode.SGET_OBJECT, desc)
          case TyVoid => Debug.WTF("Expression should not have void type"); null
        }
        FieldInsn(op, c, f, tyDesc, targetReg, 0)
      }
    }
  }

}
