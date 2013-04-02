package edu.berkeley.wtchoi.instrument.DexProcessor.instrument.compiler

import collection.immutable.Queue
import edu.berkeley.wtchoi.instrument.DexProcessor.il._
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument._
import edu.berkeley.wtchoi.instrument.DexProcessor.il.types.RegisterType


/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/12/12
 * Time: 7:42 PM
 * To change this template use File | Settings | File Templates.
 */

class CmdListCompiler(context:CompilingContext){
  type Register = IL.Register

  private var instructionSeq:Queue[Instruction] = Queue.empty
  private var availableRegs:Set[Register] =  context.getAvailableRegsAtBeginning()
  private var environment:Map[String,Register] = Map.empty

  private var tyEnvironment:Map[String,Ty] = Map("this" -> TyClass(context.handlingMethod().className()))


  //resultTy is null if the content of the result register is not valid
  //resultTy contains value only right after invoking method
  //To keep this invariant, destroyResult should be called after every byte code insertion
  private var resultTy:Ty = null
  private def destroyResult() = resultTy = null
  private def setResult(ty:Ty) = resultTy = ty


  private def flatten(strs:Queue[String]):String = {
    var str = ""
    strs.foreach(str += _)
    return str
  }

  def accept(cmd:Command){
    //Debug.println(cmd)
    //Debug.flush()

    val newSeq:Queue[Instruction] = cmd match {
      case CmdNop => Queue(Insn(Opcode.NOP))

      case CmdDeclareVariable(x,ty) => {
        //if (environment.contains(x))
        //Debug.WTF("Instrumentation varialbe cannot be redeclared!")

        if (!environment.contains(x)){
          val reg = availableRegs.head
          availableRegs = availableRegs.tail
          environment += (x -> reg)
        }
        tyEnvironment += (x -> ty)
        Queue.empty[Instruction]
      }

      case CmdAssign(varName:String, exp:Expression) =>{
        Ty.assertEqual(tyEnvironment(varName), exp.ty(tyEnvironment))
        Queue(ExpressionCompiler.compileLoadExpression(environment, tyEnvironment, exp, environment(varName), context))
      }

      case CmdInvokeStatic(cName,mName,args,returnTy) =>{
        //if (!availableRegs.contains(RESULT_REGISTER))
        //  Debug.notImplemented(this)

        if (returnTy != TyVoid) setResult(returnTy)

        val ac = new ArgListCompiler(availableRegs, environment, tyEnvironment, context)
        ac.compile(args)
        var methodDescriptor = ac.getDescriptor()

        val seq = ac.getInstructions() + MethodInsn(Opcode.INVOKE_STATIC, cName, mName, returnTy.descriptor() + flatten(methodDescriptor), ac.getArgumentRegisterArray())
        seq ++ restoreVictims(ac.getVictims())
      }

      case CmdInvokeVirtual(cName,mName,args,returnTy) =>{
        //if (!availableRegs.contains(RESULT_REGISTER))
        //  Debug.notImplemented(this)

        if (returnTy != TyVoid) setResult(returnTy)

        val ac = new ArgListCompiler(availableRegs, environment, tyEnvironment, context)
        ac.compile(args)
        var methodDescriptor = ac.getDescriptor()
        if(methodDescriptor.length > 0) methodDescriptor = methodDescriptor.tail //removing this type from descriptor

        val seq =  ac.getInstructions() + MethodInsn(Opcode.INVOKE_VIRTUAL, cName, mName, returnTy.descriptor() + flatten(methodDescriptor), ac.getArgumentRegisterArray())
        seq ++ restoreVictims(ac.getVictims())
      }

      case CmdLoadResult(varName:String) => {
        Ty.assertEqual(tyEnvironment(varName), resultTy)


        if(resultTy.isObjectTy)
          Queue(IntInsn(Opcode.MOVE_RESULT_OBJECT, environment(varName)))
        else
          Queue(IntInsn(Opcode.MOVE_RESULT, environment(varName)))
        //we ignore wide case, since instrumentation does not allow using wide type
      }

      case CmdLoadException(varName:String) => {
        Ty.assertEqual(tyEnvironment(varName), TyException)
        Queue(IntInsn(Opcode.MOVE_EXCEPTION, environment(varName)))
      }

      case CmdThrowException(varName:String) => {
        Ty.assertEqual(tyEnvironment(varName), TyException)
        Queue(IntInsn(Opcode.THROW, environment(varName)))
      }
    }

    //update resultTy status
    cmd match{
      case CmdDeclareVariable(_,_) => () //do nothing for variable declaration
      case CmdInvokeStatic (_,_,_,returnTy) => setResult(returnTy)
      case CmdInvokeVirtual(_,_,_,returnTy) => setResult(returnTy)
      case _ => destroyResult()
    }

    instructionSeq ++= newSeq
  }

  private def restoreVictims(victims:Set[(Register, Register, RegisterType)]): Queue[Instruction] = {
    var queue = Queue.empty[Instruction]
    victims.foreach(x => {
      val (origin, target, ty) = x
      if (ty.isNumericType())
        queue += VarInsn(Opcode.MOVE_FROM16, origin, target)
      else if (ty.isObjectType())
        queue += VarInsn(Opcode.MOVE_OBJECT_FROM16, origin, target)
      else{
        throw new RuntimeException("Cannot restore victim register")
      }
    })
    return queue
  }

  def getResult():Queue[Instruction] = instructionSeq
}