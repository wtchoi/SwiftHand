package edu.berkeley.wtchoi.instrument.DexProcessor.analysisFramework.intraProcedural

import edu.berkeley.wtchoi.instrument.DexProcessor.il.{MethodInfo, BasicBlock}
import collection.immutable.TreeSet

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/4/12
 * Time: 9:33 PM
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
abstract class FowardAnalysisListener[D]{
  def onPreparation(in:Map[BasicBlock,D], out:Map[BasicBlock,D], mi:MethodInfo)
  def onIterationHead(in:Map[BasicBlock,D], out:Map[BasicBlock,D], worklist:TreeSet[BasicBlock])
  def onFinish(in:Map[BasicBlock,D], out:Map[BasicBlock,D])
  def onError(in:Map[BasicBlock,D], out:Map[BasicBlock,D], work:BasicBlock)
}
