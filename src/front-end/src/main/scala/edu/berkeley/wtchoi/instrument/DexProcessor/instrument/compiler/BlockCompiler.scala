package edu.berkeley.wtchoi.instrument.DexProcessor.instrument.compiler

import edu.berkeley.wtchoi.instrument.DexProcessor.il._
import collection.immutable.Queue
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{IL, BasicBlock}
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.{Command, RegisterRemapping}

//BlockCompiler translate code fragment written in InstLanguage into IntermediateLanguage
class BlockCompiler(rr:RegisterRemapping, mi:MethodInfo)
{
  def compile(cb:Queue[Command], bb:BasicBlock, isEntry:Boolean, isInNestedTryBlock:Boolean)
  : Queue[Instruction] = {
    val context: CompilingContext = new CompilingContext(bb, mi, rr, isEntry, isInNestedTryBlock)
    compileImp(cb, context)
  }

  def compileSuperMode(cb:Queue[Command]): Queue[Instruction] = {
    val ctx = new CompilingContext(mi, rr)
    compileImp(cb, ctx)
  }

  private def compileImp(cb:Queue[Command], ctx:CompilingContext) : Queue[Instruction] ={
    val compiler = new CmdListCompiler(ctx)
    cb.foreach(compiler.accept _)
    compiler.getResult()
  }
}