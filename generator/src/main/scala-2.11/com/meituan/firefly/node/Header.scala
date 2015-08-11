package com.meituan.firefly.node

/**
 * A header is either a Thrift include, a C++ include, or a namespace declaration.
 * <pre>
 * Header          ::=  Include | CppInclude | Namespace
 * </pre>
 */
sealed abstract class Header

/**
 * An include makes all the symbols from another file visible (with a prefix) and
 * adds corresponding include statements into the code generated for this Thrift document.
 * <pre>
 *   Include         ::=  'include' Literal
 * </pre>
 * @param file the include thrift file
 * @param document the document resolved from the thrift file
 */
case class Include(file: String, document: Document) extends Header

/**
 * A namespace declares which namespaces/package/module/etc.
 * the type definitions in this file will be declared in for the target languages.
 * The namespace scope indicates which language the namespace applies to;
 * a scope of '*' indicates that the namespace applies to all target languages.
 *
 * <pre>
 *   Namespace       ::=  ( 'namespace' ( NamespaceScope Identifier ) |
 *                                      ( 'smalltalk.category' STIdentifier ) |
 *                                      ( 'smalltalk.prefix' Identifier ) ) |
 *                        ( 'php_namespace' Literal ) |
 *                        ( 'xsd_namespace' Literal )
 *
 *  NamespaceScope  ::=  '*' | 'cpp' | 'java' | 'py' | 'perl' | 'rb' | 'cocoa' | 'csharp' | 'as3'
 * </pre>
 */
case class NameSpace(scope: String, id: Identifier) extends Header
