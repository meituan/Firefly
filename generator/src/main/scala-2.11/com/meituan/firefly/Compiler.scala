package com.meituan.firefly

import java.io.File

/**
 * A compiler stores configurations of command line.
 * It processes thrift files one by one, parse thrift file into Document than generate code from Document.
 * @param thriftFiles target thrift files to compile
 * @param output output dir of generated code, specified by --output argument
 */
case class Compiler(thriftFiles: List[File] = List(), output: File = new File("gen"), rxStyle: Boolean = false, androidSupport: Boolean = false) {

  /**
   * Generates java code from thrift files
   */
  def run(): Unit = {
    val generator = new Generator(output = output, rxStyle = rxStyle, androidSupport = androidSupport)
    thriftFiles.foreach {
      file =>
        val document = new ThriftParser(file.getParentFile).parseFile(file)
        generator(document, file.getName)
    }
  }
}

object Compiler {
}
