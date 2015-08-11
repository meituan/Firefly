package com.meituan.firefly

import java.io.File

import scopt.OptionParser

/**
 * Application's entrance
 */
object Main {
  def main(args: Array[String]): Unit = {
    parser.parse(args, Compiler()).foreach(_.run())
  }

  /**
   * a parser that parse command line arguments
   */
  val parser = new OptionParser[Compiler]("Firefly") {
    help("help") text ("prints this usage text")
    arg[File]("<file>...") unbounded() action { (file, c) =>
      c.copy(thriftFiles = file :: c.thriftFiles)
    } text ("thrift files ")
    opt[File]("output") valueName ("<path>") action { (file, c) =>
      assert(file.isDirectory)
      c.copy(output = file)
    } text ("gen code output dir")
  }
}
