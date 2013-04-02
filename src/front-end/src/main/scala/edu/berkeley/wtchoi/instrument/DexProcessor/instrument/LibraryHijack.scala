package edu.berkeley.wtchoi.instrument.DexProcessor.instrument

import org.ow2.asmdex.{ClassVisitor, ApplicationVisitor, ApplicationReader}

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/2/12
 * Time: 9:16 PM
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
object LibraryHijack{
  def run(api:Int, libraryReader:ApplicationReader, av:ApplicationVisitor){
    val hijacker = new ClassHijackingApplicationVisitor(api, av)
    libraryReader.accept(hijacker,0)
    1==1
  }

  private var classes:Set[String] = Set.empty
  class ClassHijackingApplicationVisitor(api:Int, av:ApplicationVisitor) extends ApplicationVisitor(api, av){
    override def visit(){}

    override def visitEnd(){}

    override def visitClass(access:Int, name:String, signature:Array[String], superName:String, interface:Array[String])
    : ClassVisitor
    ={
      val cv:ClassVisitor = av.visitClass(access, name, signature, superName, interface)
      new ClassHijackClassVisitor(api, cv)
    }
  }

  class ClassHijackClassVisitor(api:Int, cv:ClassVisitor) extends ClassVisitor(api, cv){
    override def visit(version:Int, access:Int, name:String, signature:Array[String], superName:String, interfaces:Array[String]){
      classes += name
      cv.visit(version, access, name, signature, superName, interfaces)
    }
  }
}

