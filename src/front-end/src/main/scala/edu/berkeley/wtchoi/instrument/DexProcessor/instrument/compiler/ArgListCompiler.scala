package edu.berkeley.wtchoi.instrument.DexProcessor.instrument.compiler

import collection.immutable.Queue
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{TryBlock, IL, Instruction, VarInsn}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.types.RegisterType
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument._
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.ExpThis
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.ExpVar
import scala.Tuple3
import sun.nio.cs.ISO_8859_2

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/12/12
 * Time: 6:58 PM
 * To change this template use File | Settings | File Templates.
 */
class ArgListCompiler( _availableRegisters:Set[IL.Register],
                       env:Map[String,IL.Register],
                       tyEnv:Map[String,Ty],
                       context:CompilingContext)
{
  type Register = IL.Register

  private var mAvailableRegisters:Set[Register] = _availableRegisters
  private var preparingInstructionSeq:Queue[Instruction] = Queue.empty
  private var argumentRegSeq:Queue[Register] = Queue.empty
  private var argsDescriptor:Queue[String] = Queue.empty
  private var protectedSet:Set[Register] = Set.empty
  private var victims:Set[(Register, Register, RegisterType)] = Set.empty

  def getInstructions() = preparingInstructionSeq
  def getDescriptor() = argsDescriptor
  def getArgumentRegisterArray() = argumentRegSeq.toArray
  def getVictims() = victims

  def compile(args:Queue[Expression]){
    args.foreach(compileArg _)

  }

  private def getFreshReg():Register = {
    val reg = mAvailableRegisters.min
    mAvailableRegisters -= reg
    return reg
  }

  private def compileArg(exp:Expression){
    //TODO: current protection mechanism is not good enough
    exp match {
      case ExpVar(x) if env(x) < 16 =>{
        argumentRegSeq += env(x)
        protectedSet += env(x)
      }
      case ExpThis() if context.getThisRegister().get < 16 =>{
        argumentRegSeq += context.getThisRegister().get
        protectedSet += context.getThisRegister().get
      }
      case _ =>{
        var targetReg = getFreshReg()
        if (targetReg >= 16){
          if (context.isSuperMode()){
            throw new RuntimeException("Unsupported action")
          }
          val (victim, victimType) = findVictim(protectedSet)

          if (victimType.isNumericType){
            preparingInstructionSeq += VarInsn(Opcode.MOVE_16, targetReg, victim)
          }
          else if (victimType.isObjectType){
            preparingInstructionSeq += VarInsn(Opcode.MOVE_OBJECT_16, targetReg, victim)
          }
          else{
            throw new RuntimeException("Cannot evict victim register")
          }

          victims += Tuple3(victim, targetReg, victimType)
          protectedSet += victim

          targetReg = victim
        }
        else{
          protectedSet += targetReg
        }
        preparingInstructionSeq += ExpressionCompiler.compileLoadExpression(env, tyEnv, exp, targetReg, context)
        argumentRegSeq += targetReg
      }
    }
    argsDescriptor += exp.ty(tyEnv).descriptor()
  }

  private def findVictim(protectedSet: Set[Register]):(Register, RegisterType) = {
    for (r <- 0 until 16){
      if (!protectedSet.contains(r)){
        val ty =
          if (context.isEntry())
            context.getRegisterTypeAtEntry(r)
          else
            context.getRegisterTypeAtExit(r)

        if (ty.isNumericType() || ty.isObjectType()) return (r,ty)
      }
    }
    throw new RuntimeException("Cannot find register to spill!")
  }
}
