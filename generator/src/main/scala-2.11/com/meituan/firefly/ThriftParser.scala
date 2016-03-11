package com.meituan.firefly

import java.io.{File, FileNotFoundException}

import com.meituan.firefly.node._
import jdk.nashorn.internal.runtime.ParserException

import scala.io.Source
import scala.util.parsing.combinator._

class ThriftParser(dir: File) extends RegexParsers {
  assert(dir == null || dir.isDirectory)
  //                            1    2           3                     4         4a    4b    4c       4d
  override val whiteSpace = """(\s+|(//.*\r?\n)|(#([^@\r\n].*)?\r?\n)|(/\*[^\*]([^\*]+|\r?\n|\*(?!/))*\*/))+""".r
  // 1: whitespace, 1 or more
  // 2: leading // followed by anything 0 or more, until newline
  // 3: leading #  then NOT a @ followed by anything 0 or more, until newline
  // 4: leading /* then NOT a *, then...
  // 4a:  not a *, 1 or more times
  // 4b:  OR a newline
  // 4c:  OR a * followed by a 0-width lookahead / (not sure why we have this -KO)
  //   (0 or more of 4b/4c/4d)
  // 4d: ending */


  //Document
  lazy val document: Parser[Document] = rep(header) ~ rep(definition) <~ opt(comments) ^^ {
    case headers ~ defs => Document(headers, defs)
  }

  //Header
  lazy val header: Parser[Header] = include | namespace

  lazy val include: Parser[Include] = opt(comments) ~> "include" ~> literal ^^ {
    s => Include(s.value, parseFile(new File(dir, s.value)))
  }

  lazy val namespace: Parser[NameSpace] = opt(comments) ~> "namespace" ~> namespaceScope ~ identifier ^^ {
    case scope ~ id => NameSpace(scope, id)
  }
  lazy val namespaceScope = "*" | "cpp" | "java" | "py" | "perl" | "rb" | "cocoa" | "csharp" | "php" | "as3"

  //Definition
  lazy val definition: Parser[Definition] = const | typedef | enum | struct | union | exception | service

  lazy val const: Parser[Const] = opt(comments) ~ ("const" ~> fieldType) ~ simpleId ~ ("=" ~> constValue) <~ opt(listSeparator) ^^ {
    case cm ~ ft ~ name ~ value => Const(ft, name, value, cm)
  }
  lazy val typedef: Parser[Typedef] = (opt(comments) <~ "typedef") ~ definitionType ~ simpleId ^^ {
    case cm ~ dt ~ name => Typedef(dt, name, cm)
  }
  lazy val enum: Parser[Enum] = (opt(comments) <~ "enum") ~ simpleId ~ ("{" ~> rep(simpleId ~ opt("=" ~> intConstant) <~ opt(listSeparator)) <~ "}") ^^ {
    case cm ~ name ~ elems => {
      val valuedElems = (elems.filter(_._2.isEmpty).zipWithIndex.map(t => (t._1._1, t._2)) ++ elems.filter(_._2.isDefined).map(t => (t._1, t._2.get.value.toInt))).sortBy(_._2)
      findDuplicate(valuedElems)(_._2).foreach(e => throw new RepeatingEnumValueException(name.name, e._2))
      Enum(name, valuedElems, cm)
    }
  }
  lazy val struct: Parser[Struct] = structlike("struct") ^^ {
    case cm ~ id ~ fields => {
      checkFieldIds(fields)
      Struct(id, fields, cm)
    }
  }
  lazy val union: Parser[Union] = structlike("union") ^^ {
    case cm ~ name ~ fields => {
      fields.find(_.requiredness.isDefined).foreach(f => throw UnionFieldRequirednessException(name.name, f.identifier.name, f.requiredness.get.toString))
      checkFieldIds(fields)
      Union(name, fields, cm)
    }
  }
  lazy val exception: Parser[ExceptionDef] = structlike("exception") ^^ {
    case cm ~ name ~ fields => {
      checkFieldIds(fields)
      ExceptionDef(name, fields, cm)
    }
  }
  lazy val service: Parser[Service] = opt(comments) ~ ("service" ~> simpleId) ~ opt("extends" ~> identifier) ~ ("{" ~> rep(function) <~ "}") ^^ {
    case cm ~ name ~ parent ~ functions => Service(name, parent, functions, cm)
  }

  def structlike(keyword: String) = opt(comments) ~ (keyword ~> simpleId) ~ ("{" ~> rep(field) <~ "}")

  def findDuplicate[A, B](l: List[A])(f: A => B): Option[A] = {
    def findDuplicate[A, B](l: List[A], s: Set[B], f: A => B): Option[A] = l match {
      case Nil => None
      case (h :: t) =>
        val b = f(h)
        if (s(b)) Some(h) else findDuplicate(t, s + b, f)
    }
    findDuplicate(l, Set(), f)
  }

  def checkFieldIds(fields: List[Field]) = {
    fields.find(field => field.id.isDefined && field.id.get < 0).foreach(field => throw new NegativeFieldIdException(field.identifier.name))
    findDuplicate(fields.filter(_.id.isDefined))(_.id).foreach(field => throw new DuplicateFieldIdException(field.identifier.name))
  }

  //Field
  lazy val field = opt(comments) ~ opt(intConstant <~ ":") ~ opt(requiredness) ~ fieldType ~ simpleId ~ opt("=" ~> constValue) <~ opt(listSeparator) ^^ {
    case cm ~ id ~ req ~ ft ~ name ~ value => Field(id.map(_.value.toInt), req, ft, name, value, cm)
  }

  lazy val requiredness: Parser[Requiredness] = ("required" | "optional") ^^ {
    case "required" => Requiredness.Required
    case _ => Requiredness.Optional
  }

  //Functions
  lazy val function = opt(comments) ~ opt("oneway") ~ functionType ~ simpleId ~ ("(" ~> rep(field) <~ ")") ~ opt(throws) <~ opt(listSeparator) ^^ {
    case cm ~ oneway ~ funcType ~ name ~ params ~ throws =>
      checkFieldIds(params)
      checkFieldIds(throws.getOrElse(List()))
      Function(if (oneway.isDefined) OnewayVoid else funcType, name, params, throws, cm)
  }
  lazy val functionType = "void" ^^^ Void | fieldType
  lazy val throws = "throws" ~> "(" ~> rep(field) <~ ")"
  //Types
  lazy val fieldType: Parser[Type] = baseType | containerType | identifierType

  lazy val definitionType = baseType | containerType

  lazy val baseType = {
    "bool" ^^^ TBool |
      "byte" ^^^ TByte |
      "i16" ^^^ TI16 |
      "i32" ^^^ TI32 |
      "i64" ^^^ TI64 |
      "double" ^^^ TDouble |
      "string" ^^^ TString |
      "binary" ^^^ TBinary
  }
  lazy val identifierType = identifier ^^ { case id => IdentifierType(id) }
  lazy val containerType = mapType | setType | listType
  lazy val mapType = "map" ~> "<" ~> ((fieldType <~ ",") ~ (fieldType <~ ">")) ^^ {
    case keyType ~ valueType => MapType(keyType, valueType)
  }
  lazy val setType = "set" ~> "<" ~> fieldType <~ ">" ^^ { case tp => SetType(tp) }
  lazy val listType = "list" ~> "<" ~> fieldType <~ ">" ^^ { case tp => ListType(tp) }
  //Constant Values
  lazy val constValue = numberConstant | boolConstant | literal | constIdentifier | constList | constMap


  lazy val intConstant = """[-+]?\d+(?!\.)""".r ^^ { x => IntConstant(x.toLong) }

  lazy val numberConstant = """[+-]?\d+(\.\d+)?([Ee]\d+)?""".r ^^ {
    case x =>
      if (x exists ("eE." contains _)) DoubleConstant(x.toDouble)
      else IntConstant(x.toLong)
  }

  lazy val boolConstant = {
    "true" ^^^ BoolConstant(true) |
      "false" ^^^ BoolConstant(false)
  }

  lazy val constIdentifier = identifier ^^ { case id => IdConstant(id) }

  lazy val constList: Parser[ConstList] = "[" ~> repsep(constValue, listSeparator) <~ opt(listSeparator) <~ "]" ^^ {
    case list => ConstList(list)
  }
  lazy val keyvalue: Parser[(ConstValue, ConstValue)] = constValue ~ (":" ~> constValue) ^^ {
    case k ~ v => k -> v
  }
  lazy val constMap: Parser[ConstMap] = "{" ~> repsep(keyvalue, listSeparator) <~ opt(listSeparator) <~ "}" ^^ {
    case elems => ConstMap(elems)
  }

  //Basic Definitions

  // use a single regex to match string quote-to-quote, so that whitespace parser doesn"t
  // get executed inside the quotes
  lazy val doubleQuotedString = """(")(\.|[^\"])*(")""".r
  lazy val singleQuotedString = """'(\\.|[^\\'])*'""".r

  lazy val literal = (doubleQuotedString | singleQuotedString) ^^ {
    // strip off quotes
    x => Literal(x.substring(1, x.length - 1))
  }

  val identifierRegex = """[A-Za-z_][A-Za-z0-9\._]*""".r
  lazy val identifier = identifierRegex ^^ {
    x => Identifier(x)
  }

  private[this] val thriftKeywords = Set[String](
    "async",
    "const",
    "enum",
    "exception",
    "extends",
    "include",
    "namespace",
    "optional",
    "required",
    "service",
    "struct",
    "throws",
    "typedef",
    "union",
    "void",
    // Built-in types are also keywords.
    "binary",
    "bool",
    "byte",
    "double",
    "i16",
    "i32",
    "i64",
    "list",
    "map",
    "set",
    "string"
  )

  lazy val simpleIdRegex = "[A-Za-z_][A-Za-z0-9_]*".r
  lazy val simpleId = simpleIdRegex ^^ { x =>
    if (thriftKeywords.contains(x))
      throw new KeywordException(x)

    SimpleId(x)
  }
  lazy val listSeparator = "[,|;]".r

  /**
   * Matches scaladoc/javadoc style comments.
   */
  lazy val comments: Parser[String] = {
    rep1( """(?s)/\*\*.+?\*/""".r) ^^ {
      case cs =>
        cs.mkString("\n")
    }
  }

  def parseFile(file: File): Document = {
    if (!file.canRead)
      throw new FileNotFoundException(file.getCanonicalPath + " not Found")
    parseAll(document, Source.fromFile(file).mkString) match {
      case Success(result, _) => result
      case x@Failure(_, _) => throw new ParseException(x toString)
      case x@Error(_, _) => throw new ParserException(x toString)
    }
  }
}

