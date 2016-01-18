organization := "com.meituan.firefly"

name := "generator"

version := "0.2.2"

scalaVersion := "2.11.6"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"

libraryDependencies += "org.scalatra.scalate" %% "scalate-core" % "1.7.0"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.12"

publishMavenStyle := true

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

bintrayPackage := "com.meituan.firefly:generator_2.11"

bintrayPackageLabels := Seq("thrift", "android")

javacOptions in (Compile, compile) ++= Seq("-source", "1.7", "-target", "1.7")
