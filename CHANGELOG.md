# Change Log

-------

## 0.2.3
* Bugfix: If enum value is > 1k, comma is added in the integer in the generated Java file
* Bugfix: Single quotation mark is not accepted during Java files generation
* Gradle plugin use projectDir instead relative path

## 0.2.2
* Provide a gradle plugin that generates code from thrift files automatically.
* Support Serializable.
* Support Android's Parcelable.
* Bugfix: binary field type was generated to byte array but processed as ByteBuffer.
* Bugfix: can not specify a default value to enum type field.
* Bugfix: crash when call none argument method on Android 5.0.

## 0.2.0

* RxJava support. 
* Better exception handle to method with `oneway void` or `void` return type.

## 0.1.4

Initial release.
