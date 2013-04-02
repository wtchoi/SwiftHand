package edu.berkeley.wtchoi.instrument.DexProcessor.il.types

import collection.immutable.Queue

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/2/12
 * Time: 9:34 AM
 * To change this template use File | Settings | File Templates.
 */
sealed abstract class Type{
  def descriptor:String
  def size:Int //word size
}

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/2/12
 * Time: 5:42 PM
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
trait TD{
  def d:Char
}

object Type{
  def typeSequenceOf(desc:String):Queue[Type] = {
    var typeSequence= Queue.empty[Type]
    var remainder:String = desc
    while(remainder.length != 0){
      val (ty, _remainder) = getTypeToken(remainder)
      typeSequence += ty
      remainder = _remainder
    }
    return typeSequence
  }

  private def getTypeToken(desc:String):(Type, String) = {
    val c:Char = desc.charAt(0)
    val sub:String = desc.substring(1)
    c match{
      case _ if c == TypeVoid.d => (TypeVoid, sub)
      case _ if c == TypeBoolean.d  => (TypeBoolean, sub)
      case _ if c == TypeChar.d     => (TypeChar, sub)
      case _ if c == TypeByte.d     => (TypeByte, sub)
      case _ if c == TypeShort.d    => (TypeShort, sub)
      case _ if c == TypeInt.d      => (TypeInt, sub)
      case _ if c == TypeFloat.d    => (TypeFloat, sub)
      case _ if c == TypeLong.d     => (TypeLong, sub)
      case _ if c == TypeDouble.d   => (TypeDouble, sub)
      case _ if c == TypeObject.d   => {
        val (cname, remainder) = getQualifiedName(sub)
        (new TypeObject(cname), remainder)
      }
      case _ if c == TypeArray.d  => {
        val (contentType, remainder) = getTypeToken(sub)
        (new TypeArray(contentType), remainder)
      }
    }
  }

  private def getQualifiedName(desc:String):(String, String) = {
    val pivot = desc.indexOf(';') + 1
    (desc.substring(0, pivot), desc.substring(pivot))
  }
}



case object TypeVoid extends Type with TD{
  def descriptor = "V"
  def d = 'V'
  def size = 0
}

case object TypeBoolean extends Type with TD{
  def descriptor:String = "Z"
  def d = 'Z'
  def size = 1
}

case object TypeChar extends Type with TD{
  def descriptor:String = "C"
  def d = 'C'
  def size = 1
}

case object TypeByte extends Type with TD{
  def descriptor:String = "B"
  def d = 'B'
  def size = 1
}

case object TypeShort extends Type with TD{
  def descriptor:String = "S"
  def d = 'S'
  def size = 1
}

case object TypeInt extends Type with TD{
  def descriptor:String = "I"
  def d = 'I'
  def size = 1
}

case object TypeFloat extends Type with TD{
  def descriptor:String = "F"
  def d = 'F'
  def size = 1
}

case object TypeLong extends Type with TD{
  def descriptor:String = "J"
  def d = 'J'
  def size = 2
}

case object TypeDouble extends Type with TD{
  def descriptor:String = "D"
  def d = 'D'
  def size = 2
}

object TypeObject extends TD{
  def d = 'L'
}

case class TypeObject(cName:String) extends Type{
  def descriptor:String = cName //expect qualified name
  def size = 1
}

object TypeArray extends TD{
  def d = '['
}

case class TypeArray(content:Type) extends Type{
  def descriptor:String = "[" + content.descriptor
  def size = 1
}