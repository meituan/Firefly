package com.meituan.firefly.node

/**
 * Created by ponyets on 15/5/20.
 */
sealed abstract class Header

case class Include(file: String, document: Document) extends Header

case class NameSpace(scope: String, id: Identifier) extends Header
