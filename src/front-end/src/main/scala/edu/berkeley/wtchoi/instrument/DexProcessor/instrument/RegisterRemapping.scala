package edu.berkeley.wtchoi.instrument.DexProcessor.instrument

import edu.berkeley.wtchoi.instrument.DexProcessor.il.{IL, MethodInfo, BasicBlock}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.types.RegisterType
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
class RegisterRemapping(method:MethodInfo, increase:Int){
  type LiveSet = IL.LiveSet
  type Register = IL.Register

  private val originalLocalRegisterCount = method.getLocalRegisterCount()
  private val originalRegisterCount = method.getRegisterCount()

  private var originalRegisterMap:Map[Register, Register] = Map.empty[Register, Register]
  private var newRegisterSet:Set[Register] = Set.empty[Register]
  private var parametersToSwap:Set[(Register,Register)] = Set.empty[(Register, Register)]

  {
    for (i <- 0 until increase){
      newRegisterSet += (i + originalLocalRegisterCount)
    }

    for(i <- 0 until originalLocalRegisterCount){
      originalRegisterMap += (i -> i)
    }

    //first check whether swapping is required or not
    var flag = false
    for(i <- originalLocalRegisterCount until originalRegisterCount){
      if (registerClass(i) != registerClass(i + increase)) flag = true
    }

    //then, do remapping
    if (flag == false){
      for(i <- originalLocalRegisterCount until originalRegisterCount){
        originalRegisterMap += i -> (i + increase)
      }
    }
    else{
      for(i <- originalLocalRegisterCount until originalRegisterCount){
        originalRegisterMap += i -> i
        newRegisterSet -= i
        newRegisterSet += (i + increase)
        parametersToSwap += Pair(i+increase, i) //current modified position, position to be relocated
      }
    }
  }

  private def registerClass(r:Register): Int = {
    if (r < 16) return 0
    if (r < 256) return 1
    return 2
  }

  private def getFreeRegisters(liveRegisters:Set[Register]):Set[Register] = {
    var availableRegs = Set.empty[Register]

    for (i <- 0 until originalRegisterCount)
      if (!liveRegisters.contains(i)){
        availableRegs += this.map(i)
      }

    availableRegs ++ newRegisterSet
  }

  def getFreeRegistersAtEntry(bb: BasicBlock): Set[Register] = getFreeRegisters(bb.optExtra(IL.BB.LiveRegisterIn))
  def getFreeRegistersAtExit(bb: BasicBlock): Set[Register] = getFreeRegisters(bb.optExtra(IL.BB.LiveRegisterOut))


  def getOriginalRegisterTypeAtEntry(bb:BasicBlock, r:Register) = bb.optExtra(IL.BB.RegisterTypeIn).asInstanceOf[IL.RegisterTypes](r)
  def getOriginalRegisterTypeAtExit(bb:BasicBlock, r:Register) = bb.optExtra(IL.BB.RegisterTypeOut).asInstanceOf[IL.RegisterTypes](r)

  def getRegisterTypeAtEntry(bb:BasicBlock, r:Register): RegisterType = {
    originalRegisterMap.foreach(x => {
      if (x._2 == r) return getOriginalRegisterTypeAtEntry(bb, x._1)
    })
    throw new RuntimeException("Cannot resolve original register index from new register index ")
  }

  def getRegisterTypeAtExit(bb:BasicBlock, r:Register): RegisterType = {
    originalRegisterMap.foreach(x => {
      if (x._2 == r) return getOriginalRegisterTypeAtExit(bb, x._1)
    })
    throw new RuntimeException("Cannot resolve original register index from new register index ")
  }

  def map(reg: Register): Register = originalRegisterMap(reg)


  def getThisRegister(): Option[Register] = {
    val originalThis:Option[Register] = method.getThisRegister()
    if (originalThis.isEmpty) None
    else Some(map(method.getThisRegister().get))
  }

  def getDemand():Int = increase
  def getSwaps():Set[(Register,Register)] = parametersToSwap
  def getRegisterCount():Int = increase + originalRegisterCount

  def apply(reg:Register):Register = map(reg)
}
