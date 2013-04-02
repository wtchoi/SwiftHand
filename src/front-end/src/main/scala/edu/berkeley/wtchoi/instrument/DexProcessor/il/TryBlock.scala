package edu.berkeley.wtchoi.instrument.DexProcessor.il

import scala.collection.immutable.Queue

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/19/13
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
class TryBlock(_id:Int, _begin:Int, _end:Int){
  var mContainedBBs : Set[BasicBlock] = Set.empty
  var mHandlers : Queue[(Option[String], BasicBlock)] = Queue.empty
  var mFirstBB : BasicBlock = null

  def id():Int = _id
  def begin():Int = _begin
  def end():Int = _end

  def addBB(bb:BasicBlock){
    mContainedBBs += bb
    if (mFirstBB == null){
      mFirstBB = bb
    }
    else if (bb.id < mFirstBB.id){
      mFirstBB = bb
    }
  }

  def addHandler(ty:Option[String], h:BasicBlock) = mHandlers += (ty, h)

  def foreachBB(f:(BasicBlock) => Unit) = mContainedBBs.foreach(f)
  def foreachHandler(f:((Option[String], BasicBlock)) => Unit)  = mHandlers.foreach(f)

  def getFirstBB():BasicBlock = mFirstBB
}
