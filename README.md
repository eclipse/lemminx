[![Build Status](https://secure.travis-ci.org/angelozerr/xml-languageserver.png)](http://travis-ci.org/angelozerr/xml-languageserver)

XML Language Server
===========================

The XML Language Server is a XML language specific implementation of the [Language Server Protocol](https://github.com/Microsoft/language-server-protocol)
and can be used with any editor that supports the protocol, to offer good support for the XML Language. The server is based on:

 * [Eclipse LSP4J](https://github.com/eclipse/lsp4j), the Java binding for the Language Server Protocol, 

Features
--------------

* [textDocument/documentSymbol](https://microsoft.github.io/language-server-protocol/specification#textDocument_documentSymbol).
* [textDocument/documentHighlight](https://microsoft.github.io/language-server-protocol/specification#textDocument_documentHighlight).

TODO: completion extensible with plugin kind (with SPI) to provide custom completion (like camel, Java class compeltion, etc)

Demo
--------------

![XML Language Server Demo](demos/XMLLanguageServerDemo.gif)

Clients
-------

TODO (Eclipse BlueSky?)


