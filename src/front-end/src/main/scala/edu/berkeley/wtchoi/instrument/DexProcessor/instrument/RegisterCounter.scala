package edu.berkeley.wtchoi.instrument.DexProcessor.instrument

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/12/13
 * Time: 12:54 AM
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
class RegisterCounter{
  private var l : Int = 0 //0-15
  private var m : Int = 0 //16-255
  private var h : Int = 0 //256-65535

  def increaseCounterLow(delta:Int) = l = l + delta
  def increaseCounterMid(delta:Int) = m = m + delta
  def increaseCounterHigh(delta:Int) = h = h + delta

  def counterLow():Int = l
  def counterMid():Int = m
  def counterHigh():Int = h

  def setCounterLow(l:Int) = this.l = l
  def setCounterMid(m:Int) = this.m = m
  def setCounterHigh(h:Int) = this.h = h

  def countRegister(register:Int){
    if (register < 16) increaseCounterLow(1)
    else if (register < 256) increaseCounterMid(1)
    else increaseCounterHigh(1)
  }
}
