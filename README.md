XML Language Server (lsp4xml)
===========================

[![Build Status](https://travis-ci.org/angelozerr/lsp4xml.svg?branch=master)](http://travis-ci.org/angelozerr/lsp4xml)

The **lsp4xml** is a XML language specific implementation of the [Language Server Protocol](https://github.com/Microsoft/language-server-protocol)
and can be used with any editor that supports the protocol, to offer good support for the **XML Language**. The server is based on:

 * [Eclipse LSP4J](https://github.com/eclipse/lsp4j), the Java binding for the Language Server Protocol.
 * Xerces to manage XML Schema validation, completion and hover

Features
--------------

* [textDocument/documentSymbol](https://microsoft.github.io/language-server-protocol/specification#textDocument_documentSymbol).
* [textDocument/documentHighlight](https://microsoft.github.io/language-server-protocol/specification#textDocument_documentHighlight).
* [textDocument/completion](https://microsoft.github.io/language-server-protocol/specification#textDocument_completion).
* [textDocument/hover](https://microsoft.github.io/language-server-protocol/specification#textDocument_hover).
* [textDocument/rename](https://microsoft.github.io/language-server-protocol/specification#textDocument_rename).
* [textDocument/foldingRanges](https://microsoft.github.io/language-server-protocol/specification#textDocument_foldingRange).
* [textDocument/codeAction](https://microsoft.github.io/language-server-protocol/specification#textDocument_codeAction).

See screenshots in the [wiki](https://github.com/angelozerr/lsp4xml/wiki/Features)

Extension
--------------

The XML Language Server is extensible with plugin kind (with SPI). Here existings extensions:

 * content model to provide completion, validation, hover based on XML Schema.
 * emmet to provide completion based on Emmet.

Demo
--------------

![XML Language Server Demo](demos/XMLLanguageServerDemo.gif)

Clients
-------

Here client which consumes this XML Language Server:

 * Eclipse with [lsp4e-xml](https://github.com/angelozerr/lsp4e-xml)
 * VSCode with [vscode-xml](https://github.com/gorkem/vscode-xml)
