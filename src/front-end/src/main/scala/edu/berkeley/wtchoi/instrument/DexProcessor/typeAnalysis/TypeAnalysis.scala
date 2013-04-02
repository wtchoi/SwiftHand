package edu.berkeley.wtchoi.instrument.DexProcessor.typeAnalysis

import edu.berkeley.wtchoi.instrument.util.Debug
import edu.berkeley.wtchoi.instrument.DexProcessor.analysisFramework.intraProcedural.{FowardAnalysisListener, ForwardAnalyzer, AbstractEnvironment}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{MethodInfo, BasicBlock, IL, ApplicationInfo}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.types._
import collection.immutable.TreeSet

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/31/12
 * Time: 1:21 AM
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

object TypeAnalysis{

  def analyzeApp(appInfo:ApplicationInfo) = {
    val typeInference = new TypeInference

    appInfo.foreachMethod((mi) => {
      if (!mi.isAbstractMethod() && !mi.isNative()){
        var domain = Set.empty[IL.Register]
        for (i <- 0 to (mi.getRegisterCount() - 1)){
          domain += i
        }
        domain += IL.RESULT_REGISTER
        domain += IL.RESULT_REGISTER_HIGH

        var entryInput = new AbstractEnvironment[IL.Register, RegisterType](RegisterTypeBottom, domain).bot()

        mi.getActualParametersInfo().foreach(x => {
          entryInput += x._1 -> RegisterType.getRegisterTypeFromType(x._2)
          if(x._2.size == 2){ entryInput += (x._1 + 1) -> RegisterTypeWideHigh }
        })
        if (!mi.isStaticMethod()){
          entryInput += mi.getThisRegister().get -> RegisterType.getRegisterTypeFromString(mi.includingClass().name())
        }

        val (i,o) = typeInference.analyze(mi, entryInput)
        i.foreach(ix => ix._1.putExtra(IL.BB.RegisterTypeIn, ix._2.toMap))
        o.foreach(ox => ox._1.putExtra(IL.BB.RegisterTypeOut, ox._2.toMap))

        mi.cfg().foreachBB(typecheckBB(domain))
      }
    })
  }

  def typecheckBB(tyEnvDomain:Set[IL.Register])(bb:BasicBlock):Unit = {
    val inputTyEnvMap:Map[IL.Register,RegisterType] = bb.getExtra(IL.BB.RegisterTypeIn).get
    var inputTyEnv:TypeChecker.TyEnv = (new AbstractEnvironment[IL.Register, RegisterType](RegisterTypeBottom, tyEnvDomain)).addAll(inputTyEnvMap)
    var instCount = 0
    try{
      bb.foldLeft(inputTyEnv)((tyEnv,inst) => {
        instCount = instCount + 1
        TypeChecker.assert(inst,tyEnv)
        TypeTransformer.forward(inst, tyEnv)
      })
    }
    catch{
      case e => {
        e.printStackTrace()
        Debug.WTF("Error occrued during type checking, at " + bb.includingMethod().getQuantifiedName() + " BB(" + bb + "), Instruction #" + instCount)
        throw e
      }
    }
  }
}

