package com.meituan.firefly.node

/**
 * Created by ponyets on 15/5/20.
 */
sealed abstract class Definition

case class Const(fieldType: Type, name: SimpleId, value: ConstValue, comment: Option[String]) extends Definition

case class Typedef(definitionType: Type, name: SimpleId, comment: Option[String]) extends Definition

case class Enum(name: SimpleId, elems: Seq[(SimpleId, Int)], comment: Option[String]) extends Definition

sealed abstract class StructLike extends Definition{
  val name : SimpleId
  val fields: Seq[Field]
  val comment: Option[String]
}

case class Struct(name: SimpleId, fields: Seq[Field], comment: Option[String]) extends StructLike

case class Union(name: SimpleId, fields: Seq[Field], comment: Option[String]) extends StructLike

case class ExceptionDef(name: SimpleId, fields: Seq[Field], comment: Option[String]) extends StructLike

case class Service(name:SimpleId, parent: Option[Identifier], functions: Seq[Function], comment: Option[String]) extends Definition