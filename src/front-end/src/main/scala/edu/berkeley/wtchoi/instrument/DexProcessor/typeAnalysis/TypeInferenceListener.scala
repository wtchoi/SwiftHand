package edu.berkeley.wtchoi.instrument.DexProcessor.typeAnalysis

import edu.berkeley.wtchoi.instrument.DexProcessor.analysisFramework.intraProcedural.{AbstractEnvironment, FowardAnalysisListener}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.types.RegisterType
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{MethodInfo, BasicBlock}
import edu.berkeley.wtchoi.instrument.util.Debug
import collection.immutable.TreeSet

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/17/12
 * Time: 7:15 PM
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
object TypeInferenceListener extends FowardAnalysisListener[AbstractEnvironment[Int,RegisterType]]{
  type D = AbstractEnvironment[Int,RegisterType]

  var iterationCount = 0

  override def onPreparation(in:Map[BasicBlock, D], out:Map[BasicBlock, D], mi:MethodInfo){
    iterationCount = 0
    Debug.println("Analyzing " + mi.className() + "::" + mi.methodName() + "\t" + mi.descriptor())
  }

  override def onIterationHead(in:Map[BasicBlock, D], out:Map[BasicBlock, D], worklist:TreeSet[BasicBlock]){
    iterationCount = iterationCount + 1
    Debug.println("Iter #" + iterationCount + "\t" + worklist.toSeq.sortBy(bb => bb.order))
  }

  override def onFinish(in:Map[BasicBlock, D], out:Map[BasicBlock, D]) = print(in, out, null)
  override def onError(in:Map[BasicBlock, D], out:Map[BasicBlock, D], work:BasicBlock) = print(in, out, work)

  def print(in:Map[BasicBlock,D], out:Map[BasicBlock,D], work:BasicBlock){
    if (work != null) Debug.println("Error occrued during type analysis, at BB(" + work + ")")

    Debug.println("[IN]")
    in.toSeq.sortBy(x => x._1.id).foreach(x => {
      val (bb, tyEnv) = x
      if (!tyEnv.le(tyEnv.bot()))
        Debug.println("\t" + bb.id().toString + " \t" + tyEnv.toString)
    })

    Debug.println(" ")
    Debug.println("[OUT]")
    out.toSeq.sortBy(x => x._1.id).foreach(x => {
      val (bb, tyEnv) = x
      if (!tyEnv.le(tyEnv.bot()))
        Debug.println("\t" + bb.id().toString + "\t" + tyEnv.toString)
    })
  }
}
