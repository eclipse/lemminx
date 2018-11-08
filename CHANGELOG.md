# Change Log


## [0.0.2](https://github.com/angelozerr/lsp4xml/milestone/1?closed=1) (November 8, 2018)

### Enhancements

* Add support for `textDocument/documentLink` . See [#56](https://github.com/angelozerr/lsp4xml/issues/56).
* No completion nor validation when editing an xsd schema. See [#178](https://github.com/angelozerr/lsp4xml/issues/178).
* Cache on the file system, XML Schema from http, ftp before loading it. See [#159](https://github.com/angelozerr/lsp4xml/issues/159).
* Support for XSL. See [#189](https://github.com/angelozerr/lsp4xml/issues/189).
* Change 'resource downloading' diagnostic severity to Information. See [#187](https://github.com/angelozerr/lsp4xml/pull/187).
* XSL support to resolve XML Schema of xsl. See [#91](https://github.com/angelozerr/lsp4xml/issues/91).
* Add support for completion requests from empty character. See [#112](https://github.com/angelozerr/lsp4xml/issues/112).
* Provide documentation on hover for attributes. See [#146](https://github.com/angelozerr/lsp4xml/issues/146).

### Bug Fixes

* Formatting deletes document's body when there's a DTD declaration. See [#198](https://github.com/angelozerr/lsp4xml/issues/198).
* Completion from local xsd was cached too aggressively. See [#194](https://github.com/angelozerr/lsp4xml/issues/194).
* "format.splitAttributes:true" adds excessive indentation. See [#188](https://github.com/angelozerr/lsp4xml/issues/188).
* No validation or code completion on nested elements. See [#177](https://github.com/angelozerr/lsp4xml/issues/177).
* XSD files can only be edited if useCache is enabled. See [#186](https://github.com/angelozerr/lsp4xml/issues/186).
* No autocompletion when writing XSDs. See [#111](https://github.com/angelozerr/lsp4xml/issues/111).
* Insert required attribute code action inserts bad placeholder. See [#185](https://github.com/angelozerr/lsp4xml/issues/185).
* No validation when referencing a schema in the same directory. See [#144](https://github.com/angelozerr/lsp4xml/issues/144).
* Hover doesn't work when xs:annotation is declared in type and not element. See [#182](https://github.com/angelozerr/lsp4xml/issues/182).
* Incomplete autocompletion for xsl documents. See [#165](https://github.com/angelozerr/lsp4xml/issues/165).
* Auto Complete/ Completion for XML Prolog. See [#85](https://github.com/angelozerr/lsp4xml/issues/85).
* `xml.format.splitAttributes` keeps first attribute on same line. See [#161](https://github.com/angelozerr/lsp4xml/pull/161).
* File association should support relative path for systemId. See [#142](https://github.com/angelozerr/lsp4xml/issues/142).
* Validation of non-empty nodes required to be empty shows misplaced diagnostics. See [#147](https://github.com/angelozerr/lsp4xml/issues/147).
* Validation of empty required node shows misplaced diagnostics. See [#145](https://github.com/angelozerr/lsp4xml/issues/145).