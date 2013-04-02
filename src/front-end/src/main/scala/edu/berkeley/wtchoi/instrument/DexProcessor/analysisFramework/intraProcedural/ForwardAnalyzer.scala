package edu.berkeley.wtchoi.instrument.DexProcessor.analysisFramework.intraProcedural

import edu.berkeley.wtchoi.instrument.DexProcessor.il.{BasicBlock, MethodInfo}
import collection.immutable.TreeSet
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

class ForwardAnalyzer[D <:Domain[D], T<:ForwardTransformer[D]](transformer:T, listener:FowardAnalysisListener[D]) {

  //copied from live register analysis
  implicit object BBOrdering extends Ordering[BasicBlock]{
    override def compare(x:BasicBlock, y:BasicBlock):Int ={
      if (x.order < y.order()) return 1
      else if (x.order() == y.order()) return 0
      else -1//why not using ordering?
    }
  }


  class Analysis(mi:MethodInfo, entryInput:D){
    var In:Map[BasicBlock,D] = Map.empty
    var Out:Map[BasicBlock,D] = Map.empty

    var worklist:TreeSet[BasicBlock] = TreeSet.empty[BasicBlock]

    def run(){
      mi.cfg().foreachBB((bb) => {
        if (bb.isEntryBlock()){
          In += (bb -> entryInput)
          worklist += bb
        }
        /*
        else if (bb.isExceptionHandlerEntry()){
          In += (bb -> entryInput.top) //boundary condition
          worklist += bb
        }
        */
        else{
          In += (bb -> entryInput.bot) //initial condition
        }
        Out += (bb -> entryInput.bot) //initial condition
      })

      if (listener != null)
        listener.onPreparation(In, Out, mi)

      var work: BasicBlock = null
        try{
        while(! worklist.isEmpty){
          if (listener != null)
            listener.onIterationHead(In, Out, worklist)


          work = worklist.firstKey
          worklist = worklist - work

          var newIn:D = In(work)
          work.pred().foreach(bb => {newIn = newIn.join(Out(bb))})

          In += (work -> newIn)
          val newOut = analyzeBB(work, newIn)
          val out = Out(work)
          if (!newOut.le(out)){
            Out += (work -> newOut)
            work.succ().foreach(bb => {worklist += bb})
          }
        }

        if (listener != null)
          listener.onFinish(In, Out)
      }
      catch{
        case e:Exception => {
          if (listener != null)
            listener.onError(In, Out, work)

          e.printStackTrace()
          throw e
        }
      }
    }

    def analyzeBB(bb:BasicBlock, preB:D):D = {
      bb.foldLeft(preB)((preI, inst) =>{
        if (bb.includingTryBlock.isDefined){
          bb.includingTryBlock.get.foreachHandler(handler => {
            tryPushToIn(handler._2, preI)
          })
        }
        val postI = transformer.forward(inst, preI)
        postI
      })
    }

    def tryPushToIn(bb:BasicBlock, d:D){
      val in = In(bb)
      if (!d.le(in)){
        worklist += bb
        In += bb -> d.join(in)
      }
    }
  }

  def analyze(mi:MethodInfo, entryInput:D) : (Map[BasicBlock,D], Map[BasicBlock,D]) = {
    val analysis = new Analysis(mi, entryInput)
    analysis.run()
    return (analysis.In, analysis.Out)
  }
}
