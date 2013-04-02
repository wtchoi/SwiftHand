package edu.berkeley.wtchoi.instrument.DexProcessor.instrument

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 9/20/12
 * Time: 5:55 PM
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
import collection.immutable.Queue
import edu.berkeley.wtchoi.instrument.DexProcessor.il.IL

//DemandAnalysis class calculate number of necessary ADDITIONAL registers
object DemandAnalysis{
  def apply(cb:Queue[Command], localRegisterCount:Int, liveRegister:IL.LiveSet): RegisterCounter =
    analyzeBlock(cb, localRegisterCount, liveRegister)

  private def analyzeBlock(cb:Queue[Command], localRegisterCount:Int, liveRegisters:IL.LiveSet): RegisterCounter = {
    val analyzer = new CommandSequenceAnalyzer(localRegisterCount, liveRegisters)
    val demands = cb.map(analyzer.analyzeCommand _)
    return demands.foldLeft(new RegisterCounter)(Demand.accumulate)
  }


  class CommandSequenceAnalyzer(localRegisterCount:Int, liveRegisters:IL.LiveSet){
    private var varSet:Set[String] = Set.empty
    private val required = new RegisterCounter

    def analyzeCommand(cmd:Command):RegisterCounter = {
      cmd match {
        case CmdNop => {}
        case CmdInvokeStatic(_,_,args,_) => required.increaseCounterLow(args.length)
        case CmdInvokeVirtual(_,_,args,_) => required.increaseCounterLow(args.length)
        case CmdDeclareVariable(x,_) => {varSet += x}
        case CmdAssign(_,_) => {}
        case CmdLoadResult(_) => {}
        case CmdLoadException(_) => {}
        case CmdThrowException(_) => {}
      }

      required.increaseCounterLow(varSet.size)

      var available = new RegisterCounter
      for (i <- 0 until localRegisterCount){
        if(!liveRegisters.contains(i)){
          available.countRegister(i)
        }
      }

      val demand = Demand.cancelOut(available, required)
      return demand
    }
  }
}