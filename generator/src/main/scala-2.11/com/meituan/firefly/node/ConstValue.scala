package com.meituan.firefly.node

sealed abstract class ConstValue

case class Literal(value: String) extends ConstValue

case class IntConstant(value: Long) extends ConstValue

case class DoubleConstant(value: Double) extends ConstValue

case class IdConstant(id: Identifier) extends ConstValue

case class ConstList(elems: Seq[ConstValue]) extends ConstValue

case class ConstMap(elems: Seq[(ConstValue, ConstValue)]) extends ConstValue
