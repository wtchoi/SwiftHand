package edu.berkeley.wtchoi.instrument.DexProcessor.il.types

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/2/12
 * Time: 4:48 PM
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

import edu.berkeley.wtchoi.instrument.DexProcessor.analysisFramework.intraProcedural.Domain
import collection.immutable.Queue

abstract sealed class RegisterType extends Domain[RegisterType]{
  def optBaseType():RegisterType =
    throw new RuntimeException(this.toString + " is not an array type")

  def assertArrayType():RegisterType =
    throw new RuntimeException(this.toString + " is not an array type")

  def assertWideLowType():RegisterType =
    throw new RuntimeException(this.toString + " is not a wide low type")

  def assertWideHighType():RegisterType =
    throw new RuntimeException(this.toString + " is not a wide high type")

  def assertObjectType():RegisterType =
    throw new RuntimeException(this.toString + " is not a object type")

  def assertNumericType():RegisterType =
    throw new RuntimeException(this.toString + " is not a numeric type")

  def assertEqual(ty:RegisterType):RegisterType =
    if (RegisterType.equal(this,ty)) this
    else throw new RuntimeException(this.toString + " and " + ty.toString + " are not equal")

  def assertLE(ty:RegisterType):RegisterType =
    if (RegisterType.le(this,ty)) this
    else throw new RuntimeException(this.toString + " is bigger then " + ty.toString)

  def isWideLowType():Boolean = false
  def isWideHighType():Boolean = false
  def isNumericType():Boolean = false
  def isObjectType():Boolean = false

  override def join(ty:RegisterType) = RegisterType.join(this, ty)
  override def le(ty:RegisterType) = RegisterType.le(this, ty)
  override def bot() = RegisterTypeBottom
  override def top() = RegisterTypeTop
}

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/28/12
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */

object RegisterType{
  def equal(t1:RegisterType, t2:RegisterType):Boolean = {
    (t1, t2) match {
      case (RegisterTypeBottom, RegisterTypeBottom) => true
      case (RegisterTypeZero, RegisterTypeZero) => true
      case (RegisterTypeBoolean, RegisterTypeBoolean) => true
      case (RegisterTypeByte, RegisterTypeByte) => true
      case (RegisterTypeChar, RegisterTypeChar) => true
      case (RegisterTypeShort, RegisterTypeShort) => true
      case (RegisterTypeNumeric, RegisterTypeNumeric) => true
      case (RegisterTypeWideLow, RegisterTypeWideLow) => true
      case (RegisterTypeWideHigh, RegisterTypeWideHigh) => true
      case (RegisterTypeObject,RegisterTypeObject) => true
      case (RegisterTypeArray(base1), RegisterTypeArray(base2)) => equal(base1, base2)
      case (RegisterTypeTop, RegisterTypeTop) => true
      case _ => false
    }
  }

  def le(t1:RegisterType , t2:RegisterType):Boolean = {
    if (equal(t1,t2)) return true

    (t1, t2) match{
      case (RegisterTypeBottom, _) => true

      case (RegisterTypeZero, RegisterTypeBoolean) => true
      case (RegisterTypeZero, RegisterTypeByte) => true
      case (RegisterTypeZero, RegisterTypeChar) => true
      case (RegisterTypeZero, RegisterTypeShort) => true
      case (RegisterTypeZero, RegisterTypeNumeric) => true
      case (RegisterTypeZero, RegisterTypeObject) => true
      case (RegisterTypeZero, RegisterTypeArray(_)) => true


      case (RegisterTypeBoolean, RegisterTypeByte) => true
      case (RegisterTypeBoolean, RegisterTypeChar) => true
      case (RegisterTypeBoolean, RegisterTypeShort) => true
      case (RegisterTypeBoolean, RegisterTypeNumeric) => true

      case (RegisterTypeByte, RegisterTypeChar) => true
      case (RegisterTypeByte, RegisterTypeShort) => true
      case (RegisterTypeByte, RegisterTypeNumeric) => true

      case (RegisterTypeChar, RegisterTypeShort) => true
      case (RegisterTypeChar, RegisterTypeNumeric) => true

      case (RegisterTypeShort, RegisterTypeNumeric) => true

      case (_, RegisterTypeTop) => true

      //case (RegisterTypeClass(cName1), RegisterTypeClass(cName2)) => ???
      case (RegisterTypeArray(_), RegisterTypeObject) => true
      case _ => false
    }
  }

  def join(t1:RegisterType, t2:RegisterType):RegisterType = {
    if (equal(t1,t2)) return t1
    if (le(t1,t2)) return t2
    if (le(t2,t1)) return t1

    (t1,t2) match{
      case (RegisterTypeArray(_), RegisterTypeArray(_)) => RegisterTypeObject //strict!
      case (RegisterTypeArray(_), RegisterTypeObject) => RegisterTypeObject
      case (RegisterTypeObject, RegisterTypeArray(_)) => RegisterTypeObject
      case _ => RegisterTypeTop
    }
  }

  def getRegisterTypeFromString(desc:String): RegisterType = {
    val tySequence:Queue[Type] = Type.typeSequenceOf(desc)
    getRegisterTypeFromType(tySequence.head)
  }

  def getRegisterTypeFromType(ty:Type):RegisterType = {
    ty match{
      case TypeBoolean => RegisterTypeBoolean
      case TypeByte => RegisterTypeByte
      case TypeChar => RegisterTypeChar
      case TypeShort => RegisterTypeShort
      case TypeInt => RegisterTypeNumeric
      case TypeLong => RegisterTypeWideLow
      case TypeFloat => RegisterTypeNumeric
      case TypeDouble => RegisterTypeWideLow
      case TypeObject(cName) => RegisterTypeObject
      case TypeArray(baseTy) => RegisterTypeArray(getRegisterTypeFromType(baseTy))
    }
  }

  //I am not sure this is correct. Should I consider signed version?
  def getRegisterTypeFromInt(i:Int): RegisterType = {
    if (i == 0) return RegisterTypeZero
    //if (i >= 0 && i <= 1) return RegisterTypeBoolean
    //if (i >= 0 && i <= 15) return RegisterTypeByte
    //if (i >= 0 && i <= 255) return RegisterTypeChar
    //if (i >= 0 && i <= 65536) return RegisterTypeShort
    return RegisterTypeNumeric
  }
}

case object RegisterTypeBottom extends RegisterType{
  override def toString() = "-"
  override def optBaseType():RegisterType = this
}

case object RegisterTypeZero extends RegisterType{
  override def toString() = "0"

  override def optBaseType():RegisterType = RegisterTypeBottom

  override def isNumericType() = true
  override def assertNumericType() = this
  override def assertObjectType() = this
  override def assertArrayType() = this
}

case object RegisterTypeBoolean extends RegisterType{
  override def toString() = "Z"
  override def isNumericType() = true
  override def assertNumericType() = this
}

case object RegisterTypeByte extends RegisterType{
  override def toString() = "B"
  override def isNumericType() = true
  override def assertNumericType() = this
}

case object RegisterTypeChar extends RegisterType{
  override def toString() = "C"
  override def isNumericType() = true
  override def assertNumericType() = this
}

case object RegisterTypeShort extends RegisterType{
  override def toString() = "S"
  override def isNumericType() = true
  override def assertNumericType() = this
}

case object RegisterTypeNumeric extends RegisterType{
  override def toString() = "I"
  override def isNumericType() = true
  override def assertNumericType() = this
}

case object RegisterTypeWideLow extends RegisterType{
  override  def toString() = "Jl"
  override  def isWideLowType() = true
  override def assertWideLowType() = this
}

case object RegisterTypeWideHigh extends RegisterType{
  override  def toString() = "Jh"
  override  def isWideHighType() = true
  override def assertWideHighType() = this
}

case class RegisterTypeArray(baseType:RegisterType) extends RegisterType{
  override def optBaseType():RegisterType = baseType
  override def toString() = "["
  override def isObjectType() = true
  override def assertObjectType() = this
  override def assertArrayType() = this
}

case object RegisterTypeObject extends RegisterType{
  override def toString() = "L"
  override def isObjectType() = true
  override def assertObjectType() = this
}

case object RegisterTypeTop extends RegisterType{
  override def toString() = "T"
}
