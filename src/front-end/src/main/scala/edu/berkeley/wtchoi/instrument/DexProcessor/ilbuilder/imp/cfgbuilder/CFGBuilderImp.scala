package edu.berkeley.wtchoi.instrument.DexProcessor.ilbuilder.imp.cfgbuilder

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 8/16/12
 * Time: 12:29 AM
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

import edu.berkeley.wtchoi.instrument.DexProcessor.common.MultiMap
import edu.berkeley.wtchoi.instrument.util.Debug
import edu.berkeley.wtchoi.instrument.DexProcessor.il._
import org.ow2.asmdex.structureCommon.Label
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{MethodInfo, CFG, BasicBlock}
import edu.berkeley.wtchoi.instrument.DexProcessor.ilbuilder.imp.CFGBuilder
import collection.immutable.Queue


class CFGBuilderImp(managerFactory: StateManagerFactory, mi:MethodInfo) extends CFGBuilder{
  var mCurrentOffset: Int = 0
  var mPrevOffset: Int = 0
  var mCurrentTryEndOffset:Int = -1

  def setOffset(o: Int) {
    /*
    if (o == mCurrentTryEndOffset){
      connectFlow(mCurrentOffset, o)
    } */

    mPrevOffset = mCurrentOffset
    mCurrentOffset = o
  }

  def installTryCatchBlock(startOffset:Int, endOffset:Int, handlerOffset:Int, desc:String){
    if (mCurrentOffset != startOffset) throw new RuntimeException("Offset Mismatch!")

    mHandlerOffsets += handlerOffset
    mHandlerTable += (startOffset -> (endOffset, handlerOffset, desc))

    //add start, end offsets of try block to head offset
    mHeadOffsets += startOffset
    mHeadOffsets += endOffset

    if (mPrevOffset != mCurrentOffset //prevent self loop of the try block at the very beginning
      && !mState.isInstanceOf[EscapeState]) // prevent escaping state to be connected with next state)
      connectFlow(mPrevOffset, mCurrentOffset)
    mCurrentTryEndOffset = endOffset
  }

  var mStartOffset: Int = 0
  var mHeadOffsets: Set[Int] = Set.empty[Int]

  var mHandlerOffsets: Set[Int] = Set.empty[Int]
  val mHandlerTable: MultiMap[Int, (Int, Int, String)] = MultiMap.empty[Int, (Int, Int, String)]

  val mPred: MultiMap[Int, Int] = MultiMap.empty[Int, Int]
  val mSucc: MultiMap[Int, Int] = MultiMap.empty[Int, Int]
  var mInstructions: Queue[Instruction] = Queue.empty[Instruction]
  var mOffsetToLine: Map[Int, Int] = Map.empty[Int, Int]

  var mStateManager: StateManager = managerFactory.getStateManager(this)
  var mState: State = mStateManager.getInitState

  def connectFlow(from: Int, to: Int) {
    mSucc += (from -> to)
    mPred += (to -> from)
    mHeadOffsets += to
  }

  override def acceptLabel(label: Label) {
    if (mCurrentOffset != label.getOffset)
      Debug.Warning("\n " + mi.getQuantifiedName() + "\n offset calculation mismatch!" + mCurrentOffset + " vs " + label.getOffset)
    mState = mState.acceptLabel(label)
  }

  override def acceptInstruction(inst: Instruction) ={
    mState = mState.acceptInstruction(inst)
  }


  override def acceptTryCatchBlock(start: Label, end: Label, handler: Label, typ: String) =
    mState = mState.acceptTryCatchBlock(start, end, handler, typ)


  override def acceptEnd(): CFG = {
    Debug.println("START")
    Debug.println("=====")
    val cfg = construct()
    mi.registerCFG()
    //mi.exportToDot("/tmp/construct/")

    //optimize(cfg)
    reverseTopologicalOrdering(cfg)
    return cfg
  }

  def construct(): CFG = {
    //val tryExitOffset = mTryExitOffsets.filter(offset => offset <= mPrevOffset)
    val compare = (a: Int, b: Int) => a < b
    val headList = (mHeadOffsets ++ mHandlerOffsets).filter(_ <= mPrevOffset).toList.sortWith(compare)

    var offset = 0
    var block: BasicBlock = null
    var heads = headList
    var head = 0

    var cfg: CFG = mi.getUninitializedCFG()
    cfg.initialize(mStartOffset, headList, mHandlerTable)

    //constructing and connecting blocks by scanning instructions
    val f1 = (inst: Instruction) => {
      if (heads != Nil && heads.head == offset) {
        block = cfg.getOrCreateBB(heads.head)
        head = heads.head
        heads = heads.drop(1)
      }

      if (mSucc.contains(offset))
        mSucc(offset).foreach(target => block.connectTo(cfg.getOrCreateBB(target)))
      block.add(inst)
      offset += (inst.opcode().size())
    }
    mInstructions.foreach(f1)

    mHandlerTable.foreach((tryEntry, x) => {
      val (tryExit, handlerIdx, desc) = x
      val tb:TryBlock = cfg.getOrCreateTryBlock(tryEntry, tryExit)
      val handlerBlock = cfg.getBB(handlerIdx).get
      val ty = if (desc == null) None else Some(desc)
      tb.addHandler(ty, handlerBlock)
      handlerBlock.connectTryBlock(tb)

      cfg.foreachBB(bb => {
        if (bb.id() >= tryEntry && bb.id() < tryExit){
          bb.setIncludingTryBlock(tb)
          tb.addBB(bb)
        }
      })
    })

    Debug.println("Initial")
    Debug.println("-------")
    Debug.println("heads:")
    headList.foreach((offset: Int) => {
      Debug.print("  ");
      Debug.print(offset.toString)
    })
    Debug.println("")
    //Debug.println ("jump  :" + succ.toString())
    Debug.println ("succ  :" + cfg.succ.toString())
    //Debug.println ("blocks:" + cfgbuilder.blocks.toString())
    return cfg
  }


  //Optimize: merging basic blocks
  def optimize(cfg:CFG) {
    Debug.println("Optimize Start")
    Debug.println("--------------")

    var headL= try{cfg.mHeadList.map((offset) => cfg.getBB(offset).get)}
    catch{
      case _ => null}

    var flag: Boolean = false
    while (true) {
      Debug.println(headL.toString)
      flag = false
      headL match {
        case head :: tail => {
          if (head.succ().size == 1 && !(head.isSignificantBlock())) {
            var next = head.succ.head

            if (next.pred.size == 1 && !next.isSignificantBlock() && !next.isFirstBlock) {
              cfg.merge(head, next)

              //setting flag (since there is no continue in SCALA)
              if (tail != null) flag = true
            }
          }
          //handling header list w.r.t flag
          if (flag) headL = head :: tail.drop(1)
          else headL = tail
        }
        case _ => {
          Debug.println("RESULT succ" + mSucc.toString())
          return
        }
      }
    }
  }


  //ordering calculation
  def reverseTopologicalOrdering(cfg:CFG) {

    var visitStack: List[(BasicBlock, List[BasicBlock])] = Nil
    var count = 1
    var markedSet: Set[BasicBlock] = Set.empty[BasicBlock]

    var orderToBlock: Map[Int, BasicBlock] = Map.empty[Int, BasicBlock]
    var blockToOrder: Map[BasicBlock, Int] = Map.empty[BasicBlock, Int]

    val ordering = (block: BasicBlock) => {
      block.setReachable()
      orderToBlock += (count -> block)
      blockToOrder += (block -> count)
      count = count + 1
    }

    var handlerEntries: Set[BasicBlock] = Set.empty[BasicBlock]

    val f: (Int, (Int, Int, String)) => Unit =
      (_: Int, a: (Int, Int, String)) => handlerEntries += cfg.getOrCreateBB(a._2)

    mHandlerTable.foreach(f)

    //TODO:this code should be modified w.r.t proper handling of exception handler!
    val startings: List[BasicBlock] = cfg.getFirstBlock() :: (handlerEntries.toList)
    visitStack = (cfg.getEmptyBlock(), startings) :: visitStack

    Debug.println("--------------")
    Debug.println("Ordering Start")
    Debug.println("--------------")

    //Debug.println ("input succ:" + succ.toString())

    while (!visitStack.isEmpty) {
      Debug.println(visitStack.toString())
      visitStack match {
        case (block, Nil) :: remainders => {
          ordering(block)
          visitStack = remainders
        }
        case ((block, child :: otherChildren) :: remainders) => {
          if (markedSet.contains(child))
            visitStack = (block, otherChildren) :: remainders
          else {
            var handlers = List.empty[BasicBlock]
            if (child.includingTryBlock.isDefined){
              val tb:TryBlock = child.includingTryBlock.get
              tb.foreachHandler(handler => {
                handlers = handler._2::handlers
              })
            }

            visitStack = (child, child.succ ++ handlers) ::(block, otherChildren) :: remainders
            markedSet += child
          }
        }
      }
    }

    cfg.setOrdering(orderToBlock, blockToOrder)
  }
}


