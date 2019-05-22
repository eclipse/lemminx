# Change Log

## [0.6.0](https://github.com/angelozerr/lsp4xml/milestone/6?closed=1) (May 22, 2019)

### Enhancements

* Attribute completion for both `xsi:schemaLocation` and `xsi:noNamespaceSchemaLocation` are independent of each other. See [#382](https://github.com/angelozerr/lsp4xml/pull/382).
* Upgraded to lsp4j version 0.7.1. See [#370](https://github.com/angelozerr/lsp4xml/issues/370).
* Preference `xml.format.preservedNewLines` to preserve new lines on format. See [#350](https://github.com/angelozerr/lsp4xml/issues/350).

### Bug Fixes

* Fixed error range for `cvc-complex-type.2.4.f`. See [#368](https://github.com/angelozerr/lsp4xml/issues/368).
* Fixed error range for `SchemaLocation` warning. See [#343](https://github.com/angelozerr/lsp4xml/issues/343).
* Fixed error range for `MarkupEntityMismatch`. See [#367](https://github.com/angelozerr/lsp4xml/issues/367).
* Missing schema would generate too many/redundant warnings. See [#336](https://github.com/angelozerr/lsp4xml/issues/336).
* Self-closing tag did not remove end tag if tag name contained uppercase characters. See [#354](https://github.com/angelozerr/lsp4xml/issues/354).
* Placing a `/` in an attribute value triggered autoclosing. See [vscode-xml#126](https://github.com/redhat-developer/vscode-xml/issues/126).
* New Maven POM attribute was breaking tests. See [#356](https://github.com/angelozerr/lsp4xml/pull/356).
* Removed unused settings for testing. See [#356](https://github.com/angelozerr/lsp4xml/pull/378).


## [0.5.1](https://github.com/angelozerr/lsp4xml/milestone/7?closed=1) (April 08, 2019)

### Bug Fixes

* Fixed incorrect expansion of the `~` directory on Windows, for `xml.server.workDir`. See [#348](https://github.com/angelozerr/lsp4xml/pull/348).


## [0.5.0](https://github.com/angelozerr/lsp4xml/milestone/5?closed=1) (April 05, 2019)

### Enhancements

* More detailed completion for Prolog. See [#155](https://github.com/angelozerr/lsp4xml/issues/155).
* Added completion for xmlns attribute. See [#208](https://github.com/angelozerr/lsp4xml/issues/208).
* Have value completion for `xmlns:xsi`. See [#326](https://github.com/angelozerr/lsp4xml/issues/326).
* Make ParentProcessWatcher optional. See [#328](https://github.com/angelozerr/lsp4xml/issues/328).
* Autoclose self-closing tags. See [#239](https://github.com/angelozerr/lsp4xml/issues/239).
* Don't autoclose tag if the closing tag already exists. See [#314](https://github.com/angelozerr/lsp4xml/issues/314).
* Changing the content of an XML Schema triggers validation. See [#213](https://github.com/angelozerr/lsp4xml/issues/213).
* Preference `xml.server.workDir` to set schema cache folder. See [#222](https://github.com/angelozerr/lsp4xml/issues/222).
* Code action to close missing quotes for attributes. See [#137](https://github.com/angelozerr/lsp4xml/issues/137).
* Hover for attribute value documentation from XSD's. See [#12](https://github.com/angelozerr/lsp4xml/issues/12).
* Autocompletion for `xsi:nil` values. See [#247](https://github.com/angelozerr/lsp4xml/issues/247).

### Bug Fixes

* `textDocument/publishDiagnostics` failed with message: Illegal argument: line must be non-negative. See [#157](https://github.com/angelozerr/lsp4xml/pull/157).
* XSI completion item messages were incorrect. See [#296](https://github.com/angelozerr/lsp4xml/issues/296).
* Removed trailing whitespace from normalized strings on format. See [#300](https://github.com/angelozerr/lsp4xml/pull/300).
* Format of attribute without value loses data. See [#294](https://github.com/angelozerr/lsp4xml/issues/294).
* Cleaned up skipped unit tests. See [#319](https://github.com/angelozerr/lsp4xml/issues/319).
* Verified that logger settings were actually set on startup before updating settings. See [#81](https://github.com/angelozerr/lsp4xml/issues/81).
* Fixed error range of cvc-type.3.1.2. See [#318](https://github.com/angelozerr/lsp4xml/issues/318).
* Fixed error range of ETagUnterminated. See [#317](https://github.com/angelozerr/lsp4xml/issues/317).
* Fixed error range of cvc-elt.3.2.1. See [#321](https://github.com/angelozerr/lsp4xml/issues/321).
* Multiple `'insert required attribute'` code actions shown when multiple attributes are missing. See [#209](https://github.com/angelozerr/lsp4xml/issues/209).
* Self closing tag causes NPE in `cvc_complex_type_2_1CodeAction.doCodeAction`. See [#339](https://github.com/angelozerr/lsp4xml/issues/339).
* Closing CDATA tag throws exception. See [#341](https://github.com/angelozerr/lsp4xml/issues/341).
* Fix formatting issue with processing instruction attributes. See [#331](https://github.com/angelozerr/lsp4xml/issues/331).
  
## [0.4.0](https://github.com/angelozerr/lsp4xml/milestone/4?closed=1) (March 07, 2019)

### Enhancements

* Modified schema validation messages. See [#181](https://github.com/angelozerr/lsp4xml/issues/181).
* Preference `xml.format.quotations` to set single vs double quotes for attribute values on format. See [#263](https://github.com/angelozerr/lsp4xml/issues/263).
* Preference `xml.format.preserveEmptyContent` to preserve a whitespace value in an element's content. See [#273](https://github.com/angelozerr/lsp4xml/issues/273).
* Compatibility with OSGi and p2. See [#288](https://github.com/angelozerr/lsp4xml/issues/288).

### Bug Fixes

* Fixed memory leak of file handles. See [#303](https://github.com/angelozerr/lsp4xml/pull/303).
* XSI completion item messages were incorrect. See [#296](https://github.com/angelozerr/lsp4xml/issues/296).
* Removed trailing whitespace from normalized strings on format. See [#300](https://github.com/angelozerr/lsp4xml/pull/300).
* Format of attribute without value loses data. See [#294](https://github.com/angelozerr/lsp4xml/issues/294).

## [0.3.0](https://github.com/angelozerr/lsp4xml/milestone/3?closed=1) (January 28, 2019)

### Enhancements

* Addded root element 'xml' to preferences JSON. See [#257](https://github.com/angelozerr/lsp4xml/issues/257).
* Added ability to format DTD/DOCTYPE content. See [#268](https://github.com/angelozerr/lsp4xml/issues/268).
* Added outline for DTD elements. See [#226](https://github.com/angelozerr/lsp4xml/issues/226).
* Ability to start the server in socket mode. See [#259](https://github.com/angelozerr/lsp4xml/pull/259).
* XML completion based on internal DTD. See [#251](https://github.com/angelozerr/lsp4xml/issues/251).
* XML completion based on external DTD. See [#106](https://github.com/angelozerr/lsp4xml/issues/106).
* Completion for DTD <!ELEMENT, <!ATTRIBUTE, ... . See [#232](https://github.com/angelozerr/lsp4xml/issues/232).
* Provide automatic completion/validation in catalog files. See [#204](https://github.com/angelozerr/lsp4xml/issues/204).
* Hover for XSI attributes. See [#164](https://github.com/angelozerr/lsp4xml/issues/164).
* Show attribute value completion based on XML Schema/DTD. See [#242](https://github.com/angelozerr/lsp4xml/issues/242).
* Added `xml.format.spaceBeforeEmptyCloseTag` preference to insert whitespace before closing empty end-tag. See [#197](https://github.com/angelozerr/lsp4xml/issues/197).
* Completion for XSI attributes. See [#163](https://github.com/angelozerr/lsp4xml/issues/163).
* Changing the content of catalog.xml refreshes the catalogs and triggers validation. See [#212](https://github.com/angelozerr/lsp4xml/issues/212).
* Switched to lsp4j 0.6.0 release. See [#254](https://github.com/angelozerr/lsp4xml/issues/254).
* Added `xml.validation.noGrammar` preference, to indicate document won't be validated. See [#218](https://github.com/angelozerr/lsp4xml/issues/218).
* Added preference to enable/disable validation `xml.validation.enabled` and `xml.validation.schema`. See [#260](https://github.com/angelozerr/lsp4xml/issues/260).
* Deploy lsp4xml to a public Maven repository. [#229](https://github.com/angelozerr/lsp4xml/issues/229).

### Bug Fixes

* Formatting unclosed tag would be in wrong location. See [#269](https://github.com/angelozerr/lsp4xml/issues/269).
* Formatting removes DOCTYPE's public declaration. See [#250](https://github.com/angelozerr/lsp4xml/issues/250).
* Infinite loop when `<` was typed into an empty DTD file. See [#266](https://github.com/angelozerr/lsp4xml/issues/266).
* Formatting malformed xml removed content. See [#227](https://github.com/angelozerr/lsp4xml/issues/227).
* Misplace diagnostic for cvc-elt.3.1. See [#241](https://github.com/angelozerr/lsp4xml/issues/241).
* javax.xml.soap.Node is not available in Java 11. See [#238](https://github.com/angelozerr/lsp4xml/issues/238).
* Adjust range for DTD validation errors. See [#107](https://github.com/angelozerr/lsp4xml/issues/107).
* Adjust range error for internal DTD declaration. See [#225](https://github.com/angelozerr/lsp4xml/issues/225).
* Don't add sibling element when completion items is filled with grammar. See [#211](https://github.com/angelozerr/lsp4xml/issues/211).
* Validation needs additional `<uri>` catalog entry. See [#217](https://github.com/angelozerr/lsp4xml/issues/217).
* XML Schema completion prefix did not work in some cases. See [#214](https://github.com/angelozerr/lsp4xml/issues/214).
* Support rootUri for XML catalog configuration. See [#206](https://github.com/angelozerr/lsp4xml/issues/206).
* CacheResourcesManager keeps trying to download unavailable resources. See [#201](https://github.com/angelozerr/lsp4xml/issues/201).
* Support rootUri for XML catalog configuration. See [#206](https://github.com/angelozerr/lsp4xml/issues/206).
* CacheResourcesManager keeps trying to download unavailable resources. See [#201](https://github.com/angelozerr/lsp4xml/issues/201).
* Fix license headers according to project's declared EPL v2.0. See [#256](https://github.com/angelozerr/lsp4xml/issues/256).

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