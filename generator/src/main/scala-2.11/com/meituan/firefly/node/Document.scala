package com.meituan.firefly.node

/**
 * Every Thrift document contains 0 or more headers followed by 0 or more definitions.
 * <pre>
 * Document        ::=  Header* Definition*
 * </pre>
 *
 */
case class Document(headers: Seq[Header], defs: Seq[Definition])
