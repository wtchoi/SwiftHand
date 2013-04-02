package edu.berkeley.wtchoi.instrument.DexProcessor.il

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 8/15/12
 * Time: 12:00 PM
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
import edu.berkeley.wtchoi.instrument.DexProcessor.common.MultiMap
import java.util.NoSuchElementException
import edu.berkeley.wtchoi.gv.GraphViz

class CFG(_method:MethodInfo){

  //basic blocks
  var mBlocks = Map.empty[Int, BasicBlock]

  var mStartOffset:Int = 0
  var mLastOffset :Int = 0
  var mHeadList:List[Int] = null
  var mHandlerTable:MultiMap[Int,(Int,Int,String)] = null

  //Try blocks
  var mTryBlocks:Set[TryBlock] = Set.empty
  var mTryBlockCounter = 0


  //connectivity
  val succ = MultiMap.empty[BasicBlock, BasicBlock]
  val pred = MultiMap.empty[BasicBlock, BasicBlock]
  var orderToBlock: Map[Int, BasicBlock] = Map.empty[Int, BasicBlock]
  var blockToOrder: Map[BasicBlock, Int] = Map.empty[BasicBlock, Int]

  //
  var decisionBlockHeads:Set[BasicBlock] = Set.empty[BasicBlock]

  def getOrCreateTryBlock(begin:Int, end:Int): TryBlock = {
    val optTB = mTryBlocks.find(tb => tb.begin() == begin && tb.end() == end)
    if (optTB.isEmpty){
      mTryBlockCounter += 1
      val tb = new TryBlock(mTryBlockCounter, begin, end)
      mTryBlocks += tb
      return tb
    }
    return optTB.get
  }

  def initialize(s:Int,hl:List[Int],ht:MultiMap[Int,(Int,Int,String)]){
    mStartOffset = s
    mHeadList = hl
    mHandlerTable = ht
  }


  def getOrCreateBB(id: Int): BasicBlock = {
    if (mBlocks.contains(id)) return mBlocks(id)
    val block = new BasicBlock(this, id)
    mBlocks += (id -> block)

    if (id > mLastOffset) mLastOffset = id

    return block
  }

  def getBB(id: Int): Option[BasicBlock] = {
    if (mBlocks.contains(id)) return Some(mBlocks(id))
    return None
  }

  def getEmptyBlock(): BasicBlock = {
    new BasicBlock(this, -1)
  }

  def getFirstBlock() = mBlocks(mStartOffset)
  def getLastBlock() = mBlocks(mLastOffset)


  def getAvailableRegisters(block: BasicBlock): List[Int] = {
    var availRegs: Queue[Int] = Queue.empty
    val liveRegs:IL.LiveSet = block.getExtra(IL.BB.LiveRegisterIn).get

    var upperBound = _method.getRegisterCount - _method.getParameterCount;
    if (_method.isStaticMethod) upperBound -= 1

    for (reg <- 0 until upperBound) {
      if (!liveRegs.contains(reg))
        availRegs += reg
    }

    return availRegs.toList
  }

  def merge(head: BasicBlock, next: BasicBlock) {
    //merging instruction
    head.append(next)
    mBlocks -= next.id

    //Remove flow from HEAD to NEXT
    succ(head) -= next
    pred -= next

    //Connect HEAD and successors of NEXT
    if (succ.contains(next)) {
      succ(head) ++= succ(next)
      succ(next).foreach((ns) => {
        pred(ns) -= next
        pred(ns) += head
      })
      succ -= next
    }
  }

  def setOrdering(o2b: Map[Int, BasicBlock], b2o: Map[BasicBlock, Int]){
    orderToBlock ++= o2b
    blockToOrder ++= b2o
  }

  def exportToDot():GraphViz = {
    val dot: GraphViz = new GraphViz()

    dot.addln(dot.start_graph())
    dot.addln("compound=true;")
    dot.addln("bgcolor=\"white\";")
    dot.addln("entry[shape=box, style=rounded, label=\"" + includingClass.name + "::" + includingMethod.name + "(r" + includingMethod.getRegisterCount + ", p" + includingMethod.getParameterCount + ")\"];")

    val registerNode = (offset: Int) => {
      if (mBlocks.contains(offset)) {
        var block = mBlocks(offset)
        if (block.isInvokeBlock())
          dot.addln(offset + " [shape=square, color=blue, label=\"" + block.toDotNodeString() + "\"];")
        else if (block.isExceptionHandlerEntry())
          dot.addln(offset + " [shape=square, color=green, label=\"" + block.toDotNodeString() + "\"];")
        else if (block.isExitBlock())
          dot.addln(offset + " [sahpe=square, style=bold, label=\"" + block.toDotNodeString() + "\"];")
        else if (block.isThrowBlock())
          dot.addln(offset + " [sahpe=square, style=bold, color=green, label=\"" + block.toDotNodeString() + "\"];")
        else
          dot.addln(offset + " [shape=square, label=\"" + block.toDotNodeString() + "\"];")
      }
      else
        dot.addln(offset + "[shape=square, style=dashed];")
    }

    var registeredNodes:Set[Int] = Set.empty
    mTryBlocks.foreach(tb => {
      dot.addln("subgraph cluster" + tb.id() + " {")
      dot.addln("style = dashed;")
      dot.addln("color = green;")
      tb.foreachBB(bb => {
        registerNode(bb.id)
        registeredNodes += bb.id
      })
      dot.addln("}")
      tb.foreachHandler(handler => {
        dot.addln(tb.getFirstBB.id  + " -> " + handler._2.id + "[style=dashed, color=green, ltail=cluster" + tb.id+ "];")
      })
    })

    (mHeadList.toSet -- registeredNodes).foreach(registerNode)

    dot.addln("entry -> " + getFirstBlock() + ";")

    val registerEdge = (source: BasicBlock, target: BasicBlock) => {
      if (source.isInvokeUpperBlock()) dot.addln(source.id() + " -> " + target.id() + "[color=blue];")
      else dot.addln(source.id + " -> " + target.id + ";")
    }
    succ.foreach(registerEdge)

    dot.addln(dot.end_graph())
    //println(dot.getDotSource)

    return dot
  }

  def includingMethod():MethodInfo = _method
  def includingClass():ClassInfo = _method.includingClass()
  def includingApp():ApplicationInfo = _method.includingApp()

  //def getLiveRegisterTable(): Option[LiveRegisterTable] = includingApp.getLiveRegisterTableOf(_method)
  //def getLiveRegistersAtEntryOf(bb:BasicBlock) = includingApp.getLiveRegistersAtEntryOf(bb)
  //def getLiveRegistersAtExitOf(bb:BasicBlock) = includingApp.getLiveRegistersAtExitOf(bb)

  def localRegisterCount() = _method.getLocalRegisterCount


  //=============================
  // InstrumentationOldf:
  // We assume instrumentation is tarnsparent. I.e., added code does not change semantics of target application.
  // Therefore, we just link added code to it's position, without modifying control flow information.
  //=============================
  //code added in front of a basic block
  private var instrumentedHead:collection.immutable.Map[BasicBlock,Queue[Instruction]] = Map.empty
  def instrumentBasicBlockHead(target:BasicBlock, content:Queue[Instruction]) = instrumentedHead += (target -> content)

  def foreachBB(f:(BasicBlock) => Unit) = mBlocks.values.foreach(f)
  def foldLeftBB[A](a:A)(f:(A,BasicBlock) => A):A = mBlocks.values.foldLeft(a)(f)
}

