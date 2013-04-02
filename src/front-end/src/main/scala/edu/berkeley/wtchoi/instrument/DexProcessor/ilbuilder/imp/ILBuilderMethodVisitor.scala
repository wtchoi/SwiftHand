package edu.berkeley.wtchoi.instrument.DexProcessor.ilbuilder.imp

import edu.berkeley.wtchoi.instrument.DexProcessor.ilbuilder.imp.cfgbuilder.{SingletonStateManagerFactory, CFGBuilderImp}
import org.ow2.asmdex.structureCommon.{Label => LabelASMDEX}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.helper.MethodVisitorHelper
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{Instruction, MethodInfo}
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
//import edu.berkeley.wtchoi.DexProcessor.scala.il.{Label => LabelIL}

/*
Method Visitor for ApplicationInfo Building : THE CORE!
------------------------------------------
The main obligation of ths class is:
  A. creating CFGBuilderImp, a FSM which actually performing CFG construction
  B. feeding a CFGBuilderImp FSM with "instruction events"
  C. registering constructed CFG
 */
class ILBuilderMethodVisitor(api: Int, access: Int, methodInfo:MethodInfo) extends MethodVisitorHelper(api, null) {

  //Pick a single implementation
  private val CFGbuilder: CFGBuilder = new CFGBuilderImp(SingletonStateManagerFactory, methodInfo)

  override def handleInst(inst:Instruction) ={
    methodInfo.includingApp().increaseInstructionCount();
    methodInfo.increaseInstructionCount();
    CFGbuilder.acceptInstruction(inst)
  }

  override def visitLabel(label: LabelASMDEX) {
    CFGbuilder.acceptLabel(label)
  }

  override def visitTryCatchBlock(start: LabelASMDEX, end: LabelASMDEX, handler: LabelASMDEX, typ: String) {
    CFGbuilder.acceptTryCatchBlock(start, end, handler, typ)
  }

  /*NOT implemented:
    visitAnnotation
    visitAnnotationDefault
    visitAttribute

    visitFrame
    visitLineNumber
    visitLocalVariable
    visitParameterAnnotation
  */

  //According to ASMDEX API 1.0, parameter maxLocals should be ignored
  override def visitMaxs(maxStacks: Int, maxLocals: Int) {
    methodInfo.setRegisterCount(maxStacks)
  }

/*
  override def visitParameters(parameters: Array[String]) {
    //var actualParameterCount = parameters.length
    //methodInfo.setActualParameterCount(actualParameterCount)
    //This function is not be called if function doesn't have parameters.
    //So, it will be better to handle TotalParameterCount in different place.
  }
*/

  override def visitEnd() {
    if (!methodInfo.isAbstractMethod && !methodInfo.isNative()){
      try{
        CFGbuilder.acceptEnd()
      }
      catch{
        case e => throw new RuntimeException("Error while handling method:" + methodInfo.getQuantifiedName(), e);
      }
    }
  }
}
