**experimental**
================

attempts to implement a different formatting strategy where not every tag is in a new line, but some are kept inline (see https://github.com/eclipse/lemminx/issues/594).

XML Language Server (LemMinX)
===========================
[![Build Status](https://ci.eclipse.org/lemminx/buildStatus/icon?job=lemminx%2Fmaster)](https://ci.eclipse.org/lemminx/job/lemminx/job/master/)

**LemMinX** is a XML language specific implementation of the [Language Server Protocol](https://github.com/Microsoft/language-server-protocol)
and can be used with any editor that supports the protocol, to offer good support for the **XML Language**. The server is based on:

 * [Eclipse LSP4J](https://github.com/eclipse/lsp4j), the Java binding for the Language Server Protocol.
 * Xerces to manage XML Schema validation, completion and hover

Features
--------------

* [textDocument/codeAction](https://microsoft.github.io/language-server-protocol/specification#textDocument_codeAction).
* [textDocument/completion](https://microsoft.github.io/language-server-protocol/specification#textDocument_completion).
* [textDocument/documentHighlight](https://microsoft.github.io/language-server-protocol/specification#textDocument_documentHighlight).
* [textDocument/documentLink](https://microsoft.github.io/language-server-protocol/specification#textDocument_documentLink).
* [textDocument/documentSymbol](https://microsoft.github.io/language-server-protocol/specification#textDocument_documentSymbol).
* [textDocument/foldingRanges](https://microsoft.github.io/language-server-protocol/specification#textDocument_foldingRange).
* [textDocument/formatting](https://microsoft.github.io/language-server-protocol/specification#textDocument_formatting).
* [textDocument/hover](https://microsoft.github.io/language-server-protocol/specification#textDocument_hover).
* [textDocument/rangeFormatting](https://microsoft.github.io/language-server-protocol/specification#textDocument_rangeFormatting)
* [textDocument/rename](https://microsoft.github.io/language-server-protocol/specification#textDocument_rename).

See screenshots in the [wiki](https://github.com/eclipse/lemminx/wiki/Features).

See the [changelog](CHANGELOG.md) for the latest release.


Demo
--------------

![XML Language Server Demo](demos/XMLLanguageServerDemo.gif)

Get started
--------------
* Clone this repository
* Open the folder in your terminal / command line
* Run `./mvnw clean verify` (OSX, Linux) or `mvnw.cmd clean verify` (Windows)
* After successful compilation you can find the resulting `org.eclipse.lemminx-uber.jar` in the folder `org.eclipse.lemminx/target`

Developer
--------------

To debug the XML LS you can use XMLServerSocketLauncher:

1. Run the XMLServerSocketLauncher in debug mode (e.g. in eclipse)
2. Connect your client via socket port. Default port is 5008, but you can change it with start argument `--port` in step 1

Client connection example using Theia and TypeScript:

```js
let socketPort = '5008'
console.log(`Connecting via port ${socketPort}`)
const socket = new net.Socket()
const serverConnection = createSocketConnection(socket,
    socket, () => {
        socket.destroy()
    });
this.forward(clientConnection, serverConnection)
socket.connect(socketPort)
```

Maven coordinates:
------------------

Here are the Maven coordinates for lemminx (replace the `X.Y.Z` version with the [latest release](https://repo.eclipse.org/content/repositories/lemminx-releases)):
```xml
<dependency>
    <groupId>org.eclipse.lemminx</groupId>
    <artifactId>org.eclipse.lemminx</artifactId>
    <version>X.Y.Z</version>
    <!-- classifier:uber includes all dependencies -->
    <classifier>uber</classifier>
</dependency>
```

for Gradle:
```
compile(group: 'org.lemminx', name: 'org.eclipse.lemminx', version: 'X.Y.Z', classifier: 'uber')
```

You will have to reference the Maven repository hosting the dependency you need. E.g. for Maven, add this repository to your pom.xml or settings.xml :
```xml
<repository>
  <id>lemminx-releases</id>
  <url>https://repo.eclipse.org/content/repositories/lemminx-releases/</url>
  <snapshots>
    <enabled>false</enabled>
  </snapshots>
  <releases>
    <enabled>true</enabled>
  </releases>
</repository>
```

And if you want to consume the SNAPSHOT builds instead:
```xml
<repository>
  <id>lemminx-snapshots</id>
  <url>https://repo.eclipse.org/content/repositories/lemminx-snapshots/</url>
  <releases>
    <enabled>false</enabled>
  </releases>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>
```

Clients
-------

Here are some clients consuming this XML Language Server:

 * Eclipse IDE with [Wild Web Developer](https://github.com/eclipse/wildwebdeveloper) and [m2e](https://www.eclipse.org/m2e/)
 * VSCode with [vscode-xml](https://github.com/redhat-developer/vscode-xml)
 * Theia with [theia-xml](https://github.com/theia-ide/theia-xml-extension)
 * [Spring Tools 4](https://github.com/spring-projects/sts4) - re-using the XML parser for Spring-specific analysis and content-assist
 * Vim/Neovim with [coc-xml](https://github.com/fannheyward/coc-xml)
 * Emacs with [lsp-mode](https://github.com/emacs-lsp/lsp-mode)
 
 
Extensions
----------

The XML Language Server is extensible with plugin kind (with SPI). Additionally to XSD-based validation and assistance, those extensiosn allow to enrich the validation and assistance, typically for specific files or contexts.

Example of extensions include:

 * Built-in content model to provide completion, validation, hover based on XML Schema.
 * Built-in completion based on Emmet
 * [See all built-in extensions](https://github.com/eclipse/lemminx/tree/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/extensions)
 * Eclipse [LemMinX Maven extension](https://github.com/eclipse/lemminx-maven/) provides extra assistance for Maven pom files, adding some pom validation (as diagnostics), hover for documentation and properties evaluation, completion for configuration element (not part of XSD), constrained node, file path, GAVs..., go to definition for properties and GAVs...
