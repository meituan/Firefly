package com.meituan.firefly.node

sealed abstract class Header

case class Include(file: String, document: Document) extends Header

case class NameSpace(scope: String, id: Identifier) extends Header
