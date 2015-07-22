package com.meituan.firefly.node

sealed abstract class Identifier {
  def fullName: String
}

case class SimpleId(name: String) extends Identifier {
  override def fullName: String = name
}

case class QualifiedId(ids: Seq[String]) extends Identifier {
  assert(ids.size >= 2)
  assert(!ids.exists(_.isEmpty))

  def qualifier = ids.dropRight(1).mkString(".")

  def name = ids.last

  override def fullName: String = ids.mkString(".")
}

object Identifier {
  def apply(s: String): Identifier = {
    assert(!s.isEmpty)
    val ids = s.split("\\.")
    if (ids.size == 1) SimpleId(ids.head)
    else QualifiedId(ids)
  }
}

