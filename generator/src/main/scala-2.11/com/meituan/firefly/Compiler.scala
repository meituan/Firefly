package com.meituan.firefly

import java.io.File

/**
 * Created by ponyets on 15/6/2.
 */
case class Compiler(thriftFiles: List[File] = List()) {
  val generator = new Generator()

  def run(): Unit = {
    thriftFiles.foreach {
      file =>
        val document = new ThriftParser().parseFile(file)
        println(document)
        generator(document)
    }
  }
}
