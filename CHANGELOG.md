# Change Log

## [0.18.1](https://github.com/eclipse/lemminx/milestone/27?closed=1) (October 29, 2021)

### Bug Fixes

 * Disable external entities when using SAX parser. See [#1104](https://github.com/eclipse/lemminx/pull/1104).
 * Aggregate errors in xsd:import|include@schemaLocation for referenced grammar which have errors. See [#1117](https://github.com/eclipse/lemminx/pull/1117).
 * LemMinX doesn't declare workspaceFolders capability. See [#1110](https://github.com/eclipse/lemminx/issues/1110).

### Build

 * Bump jsoup from 1.9.2 to 1.14.2 in org.eclipse.lemminx. See [#1115](https://github.com/eclipse/lemminx/pull/1115).
 * Update maven wrapper to 3.8.3. See [#1118](https://github.com/eclipse/lemminx/pull/1118).
 * Fix typo in readme. See [#1120](https://github.com/eclipse/lemminx/pull/1120).
 * Cache ~/.m2/ to speed up GH actions. [#1109](https://github.com/eclipse/lemminx/pull/1109)

### Others

 * Register a command "xml.check.file.pattern". See [#1112](https://github.com/eclipse/lemminx/pull/1112).
 * Report schema identifier of XML document through telemetry event. See [#1105](https://github.com/eclipse/lemminx/issues/1105).

## [0.18.0](https://github.com/eclipse/lemminx/milestone/26?closed=1) (August 10, 2021)

### Enhancements

 * Added CodeLens that displays referenced grammars at the top of an XML file. See [#1092](https://github.com/eclipse/lemminx/issues/1092).
 * Added CodeAction to bind an XML document to an existing schema. See [#1088](https://github.com/eclipse/lemminx/pull/1088).
 * Added request 'canBindGrammar' to help implement binding an XML document to an existing schema. See [#1084](https://github.com/eclipse/lemminx/pull/1084).
 * Let scanner check for whitespace. See [#1077](https://github.com/eclipse/lemminx/pull/1077).
 * Provide Document Lifecycle Participant for tracking `didOpen`, `did*`. See [#603](https://github.com/eclipse/lemminx/issues/603).
 * Report telemetry events about text documents that are opened: file extension, grammar binding strategy, grammar resolving strategy. See [#1066](https://github.com/eclipse/lemminx/issues/1066).

### Bug Fixes

 * Fixed stackoverflow and resource leak when calculating folding ranges. See [#1074](https://github.com/eclipse/lemminx/pull/1074).
 * Aligned tag closing bracket with attribute when `splitAttributes` and `closingBracketNewLine` are enabled. See [#1085](https://github.com/eclipse/lemminx/pull/1085).
 * Fixed `src-import.3.1` error range. See [#1075](https://github.com/eclipse/lemminx/issues/1075).
 * Fixed `src-import.3.2` error range. See [#1069](https://github.com/eclipse/lemminx/issues/1069).
 * Adding closing bracket (`>`) in attribute quotation marks causes repeated auto-complete of the closing tag. See [#1083](https://github.com/redhat-developer/vscode-xml/issues/547).

### Build

 * Update jarsigner plugin to fix Jenkins builds. See [#1095](https://github.com/eclipse/lemminx/issues/1095).

## [0.17.1](https://github.com/eclipse/lemminx/milestone/25?closed=1) (June 25, 2021)

### Bug Fixes

 * Add support for exiting immediately from a shutdown() request. See [#1070](https://github.com/eclipse/lemminx/pull/1070).

## [0.17.0](https://github.com/eclipse/lemminx/milestone/23?closed=1) (June 22, 2021)

### Enhancements

 * Generate CodeLens to associate a grammar/schema. See [#1049](https://github.com/eclipse/lemminx/pull/1049).
 * Support for `textDocument/selectionRange`. See [#1021](https://github.com/eclipse/lemminx/issues/1021).
 * Add support for linked editing. See [#987](https://github.com/eclipse/lemminx/issues/987).
 * Added `closingBracketNewLine` formatting option. See [#1051](https://github.com/eclipse/lemminx/pull/1051).

### Bug Fixes

 * Fixed 'Go To References' in binary. See [#1059](https://github.com/eclipse/lemminx/pull/1059).
 * CodeLens does not work in binary. See [#1046](https://github.com/eclipse/lemminx/issues/1046).
 * Error while saving file to cache on Windows OS (PosixFileAttributeView not supported). See [#734](https://github.com/eclipse/lemminx/issues/734).

## [0.16.2](https://github.com/eclipse/lemminx/milestone/24?closed=1) (May 18, 2021)

### Bug Fixes

 * Fix range formatting in the binary server. See [#1035](https://github.com/eclipse/lemminx/issues/1035)
 * Mitigate Billion Laughs vulnerability. See [#1038](https://github.com/eclipse/lemminx/pull/1038)

## [0.16.1](https://github.com/eclipse/lemminx/milestone/22?closed=1) (May 17, 2021)

### Enhancements

 * Improve the error range for unterminated elements, and use `relatedInformation` to show the expected close tag placement. See [#963](https://github.com/eclipse/lemminx/issues/963).
 * Add setting `xml.completion.autoCloseRemovesContent` to prevent auto self-closing feature from deleting content. See [#1009](https://github.com/eclipse/lemminx/pull/1009).
 * Change name of telemetry events to use `.`. See [#1017](https://github.com/eclipse/lemminx/pull/1017).
 * Output the language server error stream during development. See [#1019](https://github.com/eclipse/lemminx/issues/1019).
 * Update Guava to 30.1.1. See [#1025](https://github.com/eclipse/lemminx/issues/1025)
 * Update Xerces to 2.12.1. See [#1013](https://github.com/eclipse/lemminx/issues/1013).

### Bug Fixes

 * Fix `xml.validation.noGrammar` setting. See [#1024](https://github.com/eclipse/lemminx/pull/1024).
 * Fix XML 1.1 support in the binary server. See [#1027](https://github.com/eclipse/lemminx/issues/1027).
 * Fix revalidation commands in the binary server. See [#1031](https://github.com/eclipse/lemminx/issues/1031).
 * Return `null` when receiving a request on a document before it has been opened. See [#957](https://github.com/eclipse/lemminx/issues/957).
 * Update the binary configuration so that the server works with an LSP 3.15 client. See [#1022](https://github.com/eclipse/lemminx/pull/1022).

## [0.16.0](https://github.com/eclipse/lemminx/milestone/21?closed=1) (April 13, 2021)

### Enhancements

 * Add `telemetry/event` support. See [#430](https://github.com/eclipse/lemminx/issues/430).
 * Add `CancelChecker` as parameter to `ICompletionParticipant` methods. See [#992](https://github.com/eclipse/lemminx/pull/992).
 * Add ability to read proxy configuration from environment variables. See [#1012](https://github.com/eclipse/lemminx/pull/1012)

### Bug Fixes

 * Fix rename not completing when using the binary. See [#990](https://github.com/eclipse/lemminx/pull/990).
 * Fix a test failure. See [#1003](https://github.com/eclipse/lemminx/issues/1003).
 * Fix NPE when hovering on a malformed document. See [#984](https://github.com/eclipse/lemminx/issues/984).
 * `trimTrailingWhitespace` option is not respected by `textDocument/formatting`. See [#827](https://github.com/eclipse/lemminx/issues/827).
 * Add a `CONTRIBUTING.md`. See [#998](https://github.com/eclipse/lemminx/issues/998).
 * Use `User-Agent: LemMinX` when downloading schemas to prevent HTTP 403 when using Java 8. See [#994](https://github.com/eclipse/lemminx/pull/994).

## [0.15.0](https://github.com/eclipse/lemminx/milestone/20?closed=1) (February 2, 2021)

### Enhancements
 * Generate a native binary using GraalVM. See [#860](https://github.com/eclipse/lemminx/pull/860).
 * Indicate if the server is a binary in the startup message. See [#949](https://github.com/eclipse/lemminx/issues/949).
 * Allow LemMinX extensions to contribute to `WorkspaceService`. See [#966](https://github.com/eclipse/lemminx/pull/966).
 * Add new formatting setting `xml.format.splitAttributesIndentSize`. See [#952](https://github.com/eclipse/lemminx/pull/952).
 * Disable XSD validation when `xsi:schemaLocation` doesn't declare the hint for the document element root. See [#953](https://github.com/eclipse/lemminx/pull/953).
 * Manage namespaces / prefix validation with a setting. See [#960](https://github.com/eclipse/lemminx/issues/960).

### Bug Fixes
 * Avoid trailing space in processing instructions. See [redhat-developer/vscode-xml#372](https://github.com/redhat-developer/vscode-xml/issues/372).
 * LemMinX no longer crashes if a LemMinX extension class cannot be created. See [#967](https://github.com/eclipse/lemminx/issues/967).
 * Single `<` no longer has code action to close with `/>`. See [redhat-developer/vscode-xml#373](https://github.com/redhat-developer/vscode-xml/issues/373).
 * Catch errors from any participants. See [#946](https://github.com/eclipse/lemminx/issues/946).
 * Avoid sending duplicate `client/registerCapability` for `workspace/executeCommand`. See [#937](https://github.com/eclipse/lemminx/issues/937).
 * Use `kill -0` instead of `ps -p` in `ParentProcessWatcher`. See [#936](https://github.com/eclipse/lemminx/issues/936).
 * Prevent `ClassCastException` when generating document links for XML catalogs. See [#932](https://github.com/eclipse/lemminx/issues/932).
 * Register `org.eclipse.lsp4j.FileEvent` for reflection. See [#979](https://github.com/eclipse/lemminx/issues/979).
 * Prevent URLs in `uri` attributes in catalogs from raising exceptions. See [#977](https://github.com/eclipse/lemminx/issues/977).

## [0.14.1](https://github.com/eclipse/lemminx/milestone/19?closed=1) (November 10, 2020)

### Bug Fixes

 * NPE When there's a validation error. See [#927](https://github.com/eclipse/lemminx/issues/927).
 * Symbols are not computed when ResultLimitExceededException is thrown. See [#928](https://github.com/eclipse/lemminx/issues/928).

## [0.14.0](https://github.com/eclipse/lemminx/milestone/18?closed=1) (November 6, 2020)

### Enhancements

 * Outline should display referenced DTD / XSD from the current XML. See [#892](https://github.com/eclipse/lemminx/issues/892).
 * XML catalog `nextCatalog/@catalog` documentLink support. See [#845](https://github.com/eclipse/lemminx/issues/845).
 * Format for `xsi:schemaLocation`. See [#825](https://github.com/eclipse/lemminx/issues/825).
 * Customize documentSymbols (Outline) with participant. See [#824](https://github.com/eclipse/lemminx/issues/824).
 * Support for `xml/executeClientCommand` access to server from extension. See [#596](https://github.com/eclipse/lemminx/issues/596).
 * Document links in catalog's `<system uri="..." />`. See [#220](https://github.com/eclipse/lemminx/issues/220).
 * Command to reload remote schema. See [vscode-xml#284](https://github.com/redhat-developer/vscode-xml/issues/284).
 * Customize symbols in the outline. See [vscode-xml#220](https://github.com/redhat-developer/vscode-xml/issues/220).

### Bug Fixes

 * When associating a DTD through `<?xml-model...?>`, DTD-related errors should be aggregated. See [#918](https://github.com/eclipse/lemminx/issues/918).
 * Can't use XML catalog with XSD files that have `<xs:include />`. See [#914](https://github.com/eclipse/lemminx/issues/914).
 * Empty log file string crashes the server. See [#904](https://github.com/eclipse/lemminx/issues/904).
 * Incorrect diagnostic error range for `MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL`. See [#902](https://github.com/eclipse/lemminx/issues/902).
 * CodeAction which raises an Exception prevents other CodeActions from being generated. See [#900](https://github.com/eclipse/lemminx/issues/900).
 * Symbols Max Items Computed doesn't work for 0. See [#898](https://github.com/eclipse/lemminx/issues/898).
 * Code Action for `</` with no matching open tag doesn't fix content. See [#889](https://github.com/eclipse/lemminx/issues/889).
 * Incorrect error range for cvc-complex-type.2.3. See [#885](https://github.com/eclipse/lemminx/issues/885).
 * Code Action to close root element closing tag inserts wrong closing tag. See [#878](https://github.com/eclipse/lemminx/issues/878).
 * Improve ETagRequired error range. See [#876](https://github.com/eclipse/lemminx/issues/876).
 * Improve error range for ETagUnterminated . See [#875](https://github.com/eclipse/lemminx/issues/875).
 * Error range for empty element cvc-datatype-valid.1.2.3. See [#871](https://github.com/eclipse/lemminx/issues/871).
 * Incorrect error range for cvc-datatype-valid.1.2.3. See [#864](https://github.com/eclipse/lemminx/issues/864).
 * `StringIndexOutOfBoundsException` in `EntityNotDeclaredCodeAction.getEntityName`. See [#862](https://github.com/eclipse/lemminx/issues/862).
 * Infinite loop inside `LSPMessageFormatter` for some cases. See [#856](https://github.com/eclipse/lemminx/issues/856).
 * XML validation should aggregate DTD errors in doctype. See [#853](https://github.com/eclipse/lemminx/issues/853).
 * DTD hyperlink with XML catalog and `PUBLIC` declaration doesn't work. See [#850](https://github.com/eclipse/lemminx/issues/850).
 * XML completion based on DTD with XML catalog and `PUBLIC` declaration doesn't work. See [#849](https://github.com/eclipse/lemminx/issues/849).
 * DTD validation doesn't work with XML catalog and `PUBLIC` declaration. See [#847](https://github.com/eclipse/lemminx/issues/847).
 * Null Pointer Exception in catalog extension. See [#833](https://github.com/eclipse/lemminx/issues/833).
 * XML validation should aggregate XSD errors where is referenced. See [#768](https://github.com/eclipse/lemminx/issues/768).
 * `CacheResourcesManagerTest.testAvailableCache` fails sometimes. See [#753](https://github.com/eclipse/lemminx/issues/753).
 * `completionRequest.getReplaceRange()` is erroneous in text that contains `/`. See [#723](https://github.com/eclipse/lemminx/issues/723).
 * Formatting comments which have no end should not generate `-->`. See [vscode-xml#347](https://github.com/redhat-developer/vscode-xml/issues/347).
 * Don't send invalid catalog notifications for paths with file schemes. See [vscode-xml#289](https://github.com/redhat-developer/vscode-xml/issues/289).
 * EntityNotDeclared quick fix doesn't use the proper indentation settings. See [vscode-xml#267](https://github.com/redhat-developer/vscode-xml/issues/267).
 * XSD with `targetNamespace` cannot be used with `xml.fileAssociations`. See [vscode-xml#223](https://github.com/redhat-developer/vscode-xml/issues/223).
 * `xml.fileAssociations` does not work with DTD files. See [vscode-xml#184](https://github.com/redhat-developer/vscode-xml/issues/184).

## [0.13.1](https://github.com/eclipse/lemminx/milestone/17?closed=1) (July 6, 2020)

### Bug Fixes

 * Fix generate schema code action when file name contains a single quote. See [#820](https://github.com/eclipse/lemminx/pull/820).

## [0.13.0](https://github.com/eclipse/lemminx/milestone/16?closed=1) (July 6, 2020)

### Enhancements

 * Grammar generator: generate a grammar from an XML document. See [#778](https://github.com/eclipse/lemminx/issues/778).
 * Bind XML document with no grammar constraints to generated XSD / DTD. See [#151](https://github.com/eclipse/lemminx/issues/151).
 * Quick fix to create missing `xsi:noNamespaceSchemaLocation` and generate XSD that adheres to current XML document. See [#702](https://github.com/eclipse/lemminx/issues/702).
 * Highlight the XSD file name in `xsi:schemaLocation` when reporting an invalid or missing XSD file. See [#782](https://github.com/eclipse/lemminx/issues/782).
 * Add support for `textDocument/documentLink` for `xsi:schemaLocation`. See [#666](https://github.com/eclipse/lemminx/issues/666).
 * Sort snippets. See [#692](https://github.com/eclipse/lemminx/issues/692).
 * Formatting support for trim trailing whitespace. See [#784](https://github.com/eclipse/lemminx/pull/784)
 * Warning message when one of the `xml.catalogs` paths cannot be found. See [#757](https://github.com/eclipse/lemminx/pull/757).
 * New snippet to generate a catalog. See [#708](https://github.com/eclipse/lemminx/issues/708).
 * New snippets for `xml-stylesheet`. See [#728](https://github.com/eclipse/lemminx/issues/728).

### Bug Fixes

 * Missing `xml-model` reference generates multiple similar warnings. See [#795](https://github.com/eclipse/lemminx/issues/795).
 * Fix line break being incorrectly added when `preserveAttrLineBreaks` is `true`. See [#780](https://github.com/eclipse/lemminx/pull/780).
 * Fix cases where spaces in file paths weren't accounted for. See [#749](https://github.com/eclipse/lemminx/issues/749).
 * Fix documentation "information" typo. See [#812](https://github.com/eclipse/lemminx/pull/812).

## [0.12.0](https://github.com/eclipse/lemminx/milestone/15?closed=1) (June 10, 2020)

### Enhancements

 * Preserve attribute line breaks. See [#772](https://github.com/eclipse/lemminx/pull/772)
 * Provide more server/build info on startup. See [#755](https://github.com/eclipse/lemminx/pull/755)
 * Display no hover if there is no documentation. See [#743](https://github.com/eclipse/lemminx/pull/743)
 * Add support for `textDocument/documentLink` for xs:import/schemaLocation. See [#733](https://github.com/eclipse/lemminx/issues/733)
 * Add support for `textDocument/documentLink` for xml-model/href. See [#712](https://github.com/eclipse/lemminx/issues/712)
 * Find definition for external declared entity. See [#706](https://github.com/eclipse/lemminx/issues/706)
 * Snippet to generate xml-model. See [#699](https://github.com/eclipse/lemminx/issues/699)
 * XML Completion based on DTD/XML Schema by using xml-model . See [#698](https://github.com/eclipse/lemminx/issues/698)
 * Validate XML with DTD/XML Schema by using xml-model. See [#697](https://github.com/eclipse/lemminx/issues/697)
 * Create hyperlink to DTD source on hover. See [#693](https://github.com/eclipse/lemminx/issues/693)
 * Add support for `textDocument/documentLink` for xs:include/schemaLocation. See [#689](https://github.com/eclipse/lemminx/issues/689)
 * Remove spacing when formatting processing instruction. See [#670](https://github.com/eclipse/lemminx/pull/670)
 * Hover for referenced entities. See [#663](https://github.com/eclipse/lemminx/issues/663)
 * Completion for external declared entity. See [#660](https://github.com/eclipse/lemminx/issues/660)
 * Insert final newline depending on lsp4j formatting settings. See [#649](https://github.com/eclipse/lemminx/pull/649)
 * Formatter expand/collapse/ignore empty XML tags. See [#644](https://github.com/eclipse/lemminx/pull/644)
 * Hyperlink to open declared DTD files. See [#641](https://github.com/eclipse/lemminx/issues/641)
 * Manage snippet registry to write snippet in JSON. See [#640](https://github.com/eclipse/lemminx/issues/640)
 * Configure limit for `textDocument/documentSymbol` with `xml.symbols.maxItemsComputed`. See [#637](https://github.com/eclipse/lemminx/pull/637)
 * Completion for `xsd:enumeration` inside of text node. See [#632](https://github.com/eclipse/lemminx/pull/632)
 * Separate xsd:documentation and xsd:appinfo contents on hover and completion. See [#630](https://github.com/eclipse/lemminx/issues/630)
 * Consume LSP4J 0.9.0. See [#628](https://github.com/eclipse/lemminx/issues/628)
 * Find definition for locally declared entity. See [#625](https://github.com/eclipse/lemminx/issues/625)
 * CodeActions for RootElementTypeMustMatchDoctypedecl. See [#561](https://github.com/eclipse/lemminx/issues/561)
 * CodeAction for EntityNotDeclared. See [#532](https://github.com/eclipse/lemminx/issues/532)
 * Completion for locally declared entity. See [#520](https://github.com/eclipse/lemminx/issues/520)

### Bug Fixes

 * Too many logs after completion, hover with XML that contains DTD subset. See [#750](https://github.com/eclipse/lemminx/issues/750)
 * Fix collection of external entities depending on line ending. See [#744](https://github.com/eclipse/lemminx/pull/744)
 * No entity completion for externally declared SYSTEM and PUBLIC entities. See [#742](https://github.com/eclipse/lemminx/issues/742)
 * Entity documentation has no value for entities declared with SYSTEM OR PUBLIC. See [#741](https://github.com/eclipse/lemminx/issues/741)
 * Error while loading DOCTYPE subset : java.lang.NullPointerException. See [#739](https://github.com/eclipse/lemminx/issues/739)
 * NPE in ContentModelCompletionParticipant.addCompletionItem. See [#720](https://github.com/eclipse/lemminx/issues/720)
 * NPE in XMLCompletions collectAttributeNameSuggestions(). See [#719](https://github.com/eclipse/lemminx/issues/719)
 * Support advanced characters for entity name. See [#718](https://github.com/eclipse/lemminx/pull/718)
 * Fix error range TargetNamespace.1. See [#704](https://github.com/eclipse/lemminx/issues/704)
 * Fix error range TargetNamespace.2. See [#703](https://github.com/eclipse/lemminx/issues/703)
 * Fix cache result of external grammar info. See [#696](https://github.com/eclipse/lemminx/pull/696)
 * Read the cached XSD, DTD grammar file with lazy mode. See [#687](https://github.com/eclipse/lemminx/pull/687)
 * NPE with Codelens in empty XSD file. See [#684](https://github.com/eclipse/lemminx/issues/684)
 * Range formatting inserts `<null>` when formatting inside DOCTYPE element. See [#682](https://github.com/eclipse/lemminx/issues/682)
 * NPE in ContentModelCodeActionParticipant.doCodeAction#L47. See [#671](https://github.com/eclipse/lemminx/issues/671)
 * Fix error range for `SemicolonRequiredInReference`. See [#664](https://github.com/eclipse/lemminx/issues/664)
 * Don't generate end element on apply completion if it exists. See [#651](https://github.com/eclipse/lemminx/issues/651)
 * Quickfix to close open tag doesn't deal with attributes. See [#646](https://github.com/eclipse/lemminx/issues/646)
 * MSG_ATTRIBUTE_NOT_DECLARED must highlight attribute name instead of attribute value. See [#634](https://github.com/eclipse/lemminx/pull/634)
 * NPE with TypeDefinition. See [#629](https://github.com/eclipse/lemminx/issues/629)

### Build

 * Display test names in a more user-friendly way. See [#647](https://github.com/eclipse/lemminx/pull/647)
 * Migrate Tests to JUnit 5 Jupiter. See [#627](https://github.com/eclipse/lemminx/pull/627)

## [0.11.1](https://github.com/eclipse/lemminx/milestone/14?closed=1) (March 25, 2020)

### Bug Fixes

 * Enumeration documentation is not displayed. See [#623](https://github.com/eclipse/lemminx/pull/623)

## [0.11.0](https://github.com/eclipse/lemminx/milestone/13?closed=1) (March 19, 2020)

### Enhancements

 * Contribute lsp4xml to the Eclipse foundation. See [#283](https://github.com/eclipse/lemminx/issues/283)
 * Add onTagText to IHoverParticipant (or extend IHoverParticipant). See [#609](https://github.com/eclipse/lemminx/issues/609)

### Bug Fixes

 * NPE when typing <?. See [#614](https://github.com/eclipse/lemminx/issues/614)
 * NPE when document contains an empty tag. See [#613](https://github.com/eclipse/lemminx/issues/613)
 * In Maven <configuration>, all known XML elements from schema are suggested as completion. See [#612](https://github.com/eclipse/lemminx/issues/612)
 * UTF-16 not supported. See [#611](https://github.com/eclipse/lemminx/issues/611)
 * Unregister language server extension on LS shutdown. See [#605](https://github.com/eclipse/lemminx/issues/605)

## [0.10.0](https://github.com/eclipse/lemminx/milestone/11?closed=1) (December 13, 2019)

### Enhancements

* (Experimental) Ability to edit start/end tag simultaneously under `xml.mirrorCursorOnMatchingTag` preference. See [#597](https://github.com/eclipse/lemminx/pull/597).
* Allows File Associations to be used without Workspace. See [#598](https://github.com/eclipse/lemminx/pull/598).
* CodeAction for missing root end tag. See [#595](https://github.com/eclipse/lemminx/pull/595).
* DTD hover/completion support for documentation. See [#592](https://github.com/eclipse/lemminx/pull/592).
* CodeAction for similar looking element names if it doesn't match the schema. See [#591](https://github.com/eclipse/lemminx/pull/591).
* Navigation and intellisense for xs:include-ed types. See [#579](https://github.com/eclipse/lemminx/pull/579).

### Bug Fixes

* xs:import code action was inserting inside the tag name. See [#593](https://github.com/eclipse/lemminx/pull/593).
* Prolog attribute completion was providing invalid values. See [#587](https://github.com/eclipse/lemminx/pull/587).
* getCurrentAttribute method was not returning the correct attribute name. See [#584](https://github.com/eclipse/lemminx/pull/584).
* Hover was not returning all hover responses. See [#582](https://github.com/eclipse/lemminx/pull/582).
* cvc-pattern error range fix. See [#580](https://github.com/eclipse/lemminx/pull/580).


## [0.9.1](https://github.com/eclipse/lemminx/milestone/11?closed=1) (October 17, 2019)

### Bug Fixes

 * XSD: IntelliSense and element substitutions. See [#568](https://github.com/eclipse/lemminx/pull/568)
 * Completion doesn't use file cache for included XML schema. See [#570](https://github.com/eclipse/lemminx/pull/570)
 * Prevent from NPE validation with schemaLocaton and "schema.reference.4" error. See [#569](https://github.com/eclipse/lemminx/pull/569)

### Performance

 * Improve performance and memory for validation by caching XML Schema / DTD. See [#534](https://github.com/eclipse/lemminx/issues/534)

### Others

 * Update lsp4j version to 0.8.1. See [#571](https://github.com/eclipse/lemminx/pull/571)
 * Reject download of resource which are not in the cache folder. Fixes [CVE-2019-18212](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2019-18212). See [#567](https://github.com/eclipse/lemminx/pull/567)
 * Add disallowDocTypeDecl & resolveExternalEntities validation settings. Fixes [CVE-2019-18213](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2019-18213). See [#566](https://github.com/eclipse/lemminx/pull/566)

## [0.9.0](https://github.com/eclipse/lemminx/milestone/10?closed=1) (September 10, 2019)

### Enhancements

 * Add support for `textDocument/documentHighlight` for DTD. See [#545](https://github.com/eclipse/lemminx/issues/545)
 * Ability to `rename` a `complexType/@name` inside XML Schema. See [#454](https://github.com/eclipse/lemminx/issues/454)
 * Add support for `textDocument/codeLens` for XML DTD. See [#252](https://github.com/eclipse/lemminx/issues/252)
 * Add support for `textDocument/references` for DTD. See [#234](https://github.com/eclipse/lemminx/issues/234)
 * Add support for `textDocument/definition` for DTD. See [#233](https://github.com/eclipse/lemminx/issues/233)

### Bug Fixes

 * Cache completion based on XML Schema/DTD. See [#547](https://github.com/eclipse/lemminx/issues/547)
 * Fix error range for `cvc-datatype-valid-1-2-1`. See [#323](https://github.com/eclipse/lemminx/issues/323)
 * Support completion with `xs:any`. See [#177](https://github.com/eclipse/lemminx/pull/563)
 * Fixes issue with error messages not showing. See [#557](https://github.com/eclipse/lemminx/pull/557)
 * Validation Error Message Fails on Certain Cases. See [#553](https://github.com/eclipse/lemminx/issues/553)
 * Error range for `RootElementTypeMustMatchDoctypedecl`. See [#537](https://github.com/eclipse/lemminx/issues/537)

# Change Log

## [0.8.0](https://github.com/eclipse/lemminx/milestone/9?closed=1) (July 23, 2019)

### Enhancements

 * `Markdown` support for `hover` documentation. See [#24](https://github.com/eclipse/lemminx/issues/245)
 * `Markdown` support for `completion` documentation. See [#526](https://github.com/eclipse/lemminx/issues/526)
 * Add completion for `comment` and `#region`. See [#54](https://github.com/eclipse/lemminx/issues/54)
 * Add completion for `CDATA` block. See [#168](https://github.com/redhat-developer/vscode-xml/issues/168)
 * Find definition for start/end tag element. See [#535](https://github.com/eclipse/lemminx/issues/535)
 * Show `relevant XML` completion options based on XML Schema. See [#347](https://github.com/eclipse/lemminx/issues/347)
 * Improve `XSD source` information for XML completion. See [#529](https://github.com/eclipse/lemminx/issues/529)
 * Add support for `textDocument/documentHighlight` for XML Schema types. See [#470](https://github.com/eclipse/lemminx/issues/470)
 * Add support for `textDocument/completion` for xs:element/@name / xs:extension/@base. See [#451](https://github.com/eclipse/lemminx/issues/451)
 * Add support for selective outline enablement per file. See [#427](https://github.com/eclipse/lemminx/issues/427)
 * Parse `.ent` and `.mod` files as DTD files. See [#380](https://github.com/eclipse/lemminx/issues/380)
 * Add support for `textDocument/typeDefinition` from XML to XMLSchema/DTD. See [#371](https://github.com/eclipse/lemminx/issues/371)
 * Add support for `textDocument/definition` for XML Schema. See [#148](https://github.com/eclipse/lemminx/issues/148)
 * Add support for `textDocument/references` for XML Schema types. See [#58](https://github.com/eclipse/lemminx/issues/58)
 * Add support for `textDocument/codelens` for XML Schema types. See [#55](https://github.com/eclipse/lemminx/issues/55)
 * Add support for clickable`XSD CodeLens`. See [#490](https://github.com/eclipse/lemminx/issues/490)
 * Improved XML validation when XSD files are saved. See [#506](https://github.com/eclipse/lemminx/issues/506)

### Bug Fixes

 * Hover markup response ignored the hover client capability. See [#525](https://github.com/eclipse/lemminx/issues/525)
 * Completion capability was lost in specific scenarios. See [#522](https://github.com/eclipse/lemminx/issues/522)
 * Fixed NPE in `textDocument/definition` in XSD files. See [#488](https://github.com/eclipse/lemminx/issues/488)
 * Fixed case sensitivity problems for element and attribute names. See [#433](https://github.com/eclipse/lemminx/issues/433)
 * Selection formatting ignores attribute indentation preference. See [#429](https://github.com/eclipse/lemminx/issues/429)
 * Fixed error range for `EntityNotDeclared`. See [#518](https://github.com/eclipse/lemminx/issues/518)
 * Fixed error range for `src-import.1.2`. See [#499](https://github.com/eclipse/lemminx/issues/499)
 * Fixed error range for `s4s-elt-invalid-content.3`. See [#496](https://github.com/eclipse/lemminx/issues/496)
 * Fixed error range for `cvc-pattern-valid`. See [#477](https://github.com/eclipse/lemminx/issues/477)
 * Fixed error range for `AttributePrefixUnbound`. See [#476](https://github.com/eclipse/lemminx/issues/476)
 * Fixed error range for `EmptyTargetNamespace`. See [#472](https://github.com/eclipse/lemminx/issues/472)
 * Fixed error range for `ct-props-correct.3`. See [#467](https://github.com/eclipse/lemminx/issues/467)
 * Fixed error range for `sch-props-correct.2`. See [#462](https://github.com/eclipse/lemminx/issues/462)
 * Fixed error range for `s4s-elt-must-match.2`. See [#458](https://github.com/eclipse/lemminx/issues/458)
 * Fixed error range for `ct-props-correct.3`. See [#455](https://github.com/eclipse/lemminx/issues/455)
 * Fixed error range for `src-ct.1`. See [#453](https://github.com/eclipse/lemminx/issues/453)
 * Fixed error range for `duplicate attribute`.. See [#452](https://github.com/eclipse/lemminx/issues/452)
 * Fixed error range for `p-props-correct.2.1`. See [#436](https://github.com/eclipse/lemminx/issues/436)
 * Fixed error range for `cos-all-limited.2`. See [#428](https://github.com/eclipse/lemminx/issues/428)
 * Fixed error range for `src-element.3`. See [#420](https://github.com/eclipse/lemminx/issues/420)
 * Documents with an Internal Subset DOCTYPE had stopped trying to bind. See [#379](https://github.com/eclipse/lemminx/issues/379)
 * Fixed discrepancy in completion between prefixed and default namespaces. See [#311](https://github.com/eclipse/lemminx/issues/311)
 * XML did not validate when bounded DTD file was not found. See [#167](https://github.com/redhat-developer/vscode-xml/issues/167)
 * Formatter inserts spaces in empty lines. See [#157](https://github.com/redhat-developer/vscode-xml/issues/157)
 * VSCode was not revalidating XML files when relevant XSD files were modified outside VSCode. See [#131](https://github.com/redhat-developer/vscode-xml/issues/131)

### Performance

 * Improve XML Scanner performance. See [#444](https://github.com/eclipse/lemminx/issues/444)
 * Use CompletableFuture to load DOMDocument. See [#439](https://github.com/eclipse/lemminx/issues/439)
 * Examined memory usage. See [#438](https://github.com/eclipse/lemminx/issues/438)
 * Improved `TextDocument` update (in async) performance with `TreeLineTracker`. See [#426](https://github.com/eclipse/lemminx/issues/426)
 * Tested large files for performance. See [#48](https://github.com/eclipse/lemminx/issues/48)

## [0.7.0](https://github.com/eclipse/lemminx/milestone/8?closed=1) (June 11, 2019)

### Enhancements

* Display Java runtime used to launch the server. See [#415](https://github.com/eclipse/lemminx/pull/415).
* Added `xml.symbols.enabled` preference, to enable/disable Document Symbols. See [#413](https://github.com/eclipse/lemminx/issues/413).
* File completion in attribute value. See [#345](https://github.com/eclipse/lemminx/issues/345).
* Validation for an XML Schema. See [#190](https://github.com/eclipse/lemminx/issues/190).
* Ability for XML Prolog completion in DTD files. See [#267](https://github.com/eclipse/lemminx/issues/267).
* Ability to rename a namespace/namespace renaming improvements. See [#366](https://github.com/eclipse/lemminx/issues/366).
* Startup time for SVG DTD file completion was too slow. See [#397](https://github.com/eclipse/lemminx/issues/397).
* Mark element source coming from XML Schema/DTD for completion. See [#210](https://github.com/eclipse/lemminx/issues/210).


### Bug Fixes

* Memory usage improvements. See [#389](https://github.com/eclipse/lemminx/issues/389).
* Fix completion source crash on Windows OS. See [#408](https://github.com/eclipse/lemminx/pull/408).
* Fix error range for `ETagRequired`. See [#387](https://github.com/eclipse/lemminx/issues/387).
* Fix error range for `cos-all-limited.2`. See [#407](https://github.com/eclipse/lemminx/issues/407).
* Fix `normalizePath` test for Windows OS. See [#399](https://github.com/eclipse/lemminx/pull/399).
* Document Symbols only returns the 1st `ATTLIST` value. See [#265](https://github.com/eclipse/lemminx/issues/265).
* Completion in SVG DTD file proposed duplicate completions. See [#386](https://github.com/eclipse/lemminx/issues/386).
* Fixed formatting range issues. See [#76](https://github.com/eclipse/lemminx/issues/76).

## [0.6.0](https://github.com/eclipse/lemminx/milestone/6?closed=1) (May 22, 2019)

### Enhancements

* Attribute completion for both `xsi:schemaLocation` and `xsi:noNamespaceSchemaLocation` are independent of each other. See [#382](https://github.com/eclipse/lemminx/pull/382).
* Upgraded to lsp4j version 0.7.1. See [#370](https://github.com/eclipse/lemminx/issues/370).
* Preference `xml.format.preservedNewLines` to preserve new lines on format. See [#350](https://github.com/eclipse/lemminx/issues/350).

### Bug Fixes

* Fixed error range for `cvc-complex-type.2.4.f`. See [#368](https://github.com/eclipse/lemminx/issues/368).
* Fixed error range for `SchemaLocation` warning. See [#343](https://github.com/eclipse/lemminx/issues/343).
* Fixed error range for `MarkupEntityMismatch`. See [#367](https://github.com/eclipse/lemminx/issues/367).
* Missing schema would generate too many/redundant warnings. See [#336](https://github.com/eclipse/lemminx/issues/336).
* Self-closing tag did not remove end tag if tag name contained uppercase characters. See [#354](https://github.com/eclipse/lemminx/issues/354).
* Placing a `/` in an attribute value triggered autoclosing. See [vscode-xml#126](https://github.com/redhat-developer/vscode-xml/issues/126).
* New Maven POM attribute was breaking tests. See [#356](https://github.com/eclipse/lemminx/pull/356).
* Removed unused settings for testing. See [#356](https://github.com/eclipse/lemminx/pull/378).


## [0.5.1](https://github.com/eclipse/lemminx/milestone/7?closed=1) (April 08, 2019)

### Bug Fixes

* Fixed incorrect expansion of the `~` directory on Windows, for `xml.server.workDir`. See [#348](https://github.com/eclipse/lemminx/pull/348).


## [0.5.0](https://github.com/eclipse/lemminx/milestone/5?closed=1) (April 05, 2019)

### Enhancements

* More detailed completion for Prolog. See [#155](https://github.com/eclipse/lemminx/issues/155).
* Added completion for xmlns attribute. See [#208](https://github.com/eclipse/lemminx/issues/208).
* Have value completion for `xmlns:xsi`. See [#326](https://github.com/eclipse/lemminx/issues/326).
* Make ParentProcessWatcher optional. See [#328](https://github.com/eclipse/lemminx/issues/328).
* Autoclose self-closing tags. See [#239](https://github.com/eclipse/lemminx/issues/239).
* Don't autoclose tag if the closing tag already exists. See [#314](https://github.com/eclipse/lemminx/issues/314).
* Changing the content of an XML Schema triggers validation. See [#213](https://github.com/eclipse/lemminx/issues/213).
* Preference `xml.server.workDir` to set schema cache folder. See [#222](https://github.com/eclipse/lemminx/issues/222).
* Code action to close missing quotes for attributes. See [#137](https://github.com/eclipse/lemminx/issues/137).
* Hover for attribute value documentation from XSD's. See [#12](https://github.com/eclipse/lemminx/issues/12).
* Autocompletion for `xsi:nil` values. See [#247](https://github.com/eclipse/lemminx/issues/247).

### Bug Fixes

* `textDocument/publishDiagnostics` failed with message: Illegal argument: line must be non-negative. See [#157](https://github.com/eclipse/lemminx/pull/157).
* XSI completion item messages were incorrect. See [#296](https://github.com/eclipse/lemminx/issues/296).
* Removed trailing whitespace from normalized strings on format. See [#300](https://github.com/eclipse/lemminx/pull/300).
* Format of attribute without value loses data. See [#294](https://github.com/eclipse/lemminx/issues/294).
* Cleaned up skipped unit tests. See [#319](https://github.com/eclipse/lemminx/issues/319).
* Verified that logger settings were actually set on startup before updating settings. See [#81](https://github.com/eclipse/lemminx/issues/81).
* Fixed error range of cvc-type.3.1.2. See [#318](https://github.com/eclipse/lemminx/issues/318).
* Fixed error range of ETagUnterminated. See [#317](https://github.com/eclipse/lemminx/issues/317).
* Fixed error range of cvc-elt.3.2.1. See [#321](https://github.com/eclipse/lemminx/issues/321).
* Multiple `'insert required attribute'` code actions shown when multiple attributes are missing. See [#209](https://github.com/eclipse/lemminx/issues/209).
* Self closing tag causes NPE in `cvc_complex_type_2_1CodeAction.doCodeAction`. See [#339](https://github.com/eclipse/lemminx/issues/339).
* Closing CDATA tag throws exception. See [#341](https://github.com/eclipse/lemminx/issues/341).
* Fix formatting issue with processing instruction attributes. See [#331](https://github.com/eclipse/lemminx/issues/331).

## [0.4.0](https://github.com/eclipse/lemminx/milestone/4?closed=1) (March 07, 2019)

### Enhancements

* Modified schema validation messages. See [#181](https://github.com/eclipse/lemminx/issues/181).
* Preference `xml.format.quotations` to set single vs double quotes for attribute values on format. See [#263](https://github.com/eclipse/lemminx/issues/263).
* Preference `xml.format.preserveEmptyContent` to preserve a whitespace value in an element's content. See [#273](https://github.com/eclipse/lemminx/issues/273).
* Compatibility with OSGi and p2. See [#288](https://github.com/eclipse/lemminx/issues/288).

### Bug Fixes

* Fixed memory leak of file handles. See [#303](https://github.com/eclipse/lemminx/pull/303).
* XSI completion item messages were incorrect. See [#296](https://github.com/eclipse/lemminx/issues/296).
* Removed trailing whitespace from normalized strings on format. See [#300](https://github.com/eclipse/lemminx/pull/300).
* Format of attribute without value loses data. See [#294](https://github.com/eclipse/lemminx/issues/294).

## [0.3.0](https://github.com/eclipse/lemminx/milestone/3?closed=1) (January 28, 2019)

### Enhancements

* Addded root element 'xml' to preferences JSON. See [#257](https://github.com/eclipse/lemminx/issues/257).
* Added ability to format DTD/DOCTYPE content. See [#268](https://github.com/eclipse/lemminx/issues/268).
* Added outline for DTD elements. See [#226](https://github.com/eclipse/lemminx/issues/226).
* Ability to start the server in socket mode. See [#259](https://github.com/eclipse/lemminx/pull/259).
* XML completion based on internal DTD. See [#251](https://github.com/eclipse/lemminx/issues/251).
* XML completion based on external DTD. See [#106](https://github.com/eclipse/lemminx/issues/106).
* Completion for DTD <!ELEMENT, <!ATTRIBUTE, ... . See [#232](https://github.com/eclipse/lemminx/issues/232).
* Provide automatic completion/validation in catalog files. See [#204](https://github.com/eclipse/lemminx/issues/204).
* Hover for XSI attributes. See [#164](https://github.com/eclipse/lemminx/issues/164).
* Show attribute value completion based on XML Schema/DTD. See [#242](https://github.com/eclipse/lemminx/issues/242).
* Added `xml.format.spaceBeforeEmptyCloseTag` preference to insert whitespace before closing empty end-tag. See [#197](https://github.com/eclipse/lemminx/issues/197).
* Completion for XSI attributes. See [#163](https://github.com/eclipse/lemminx/issues/163).
* Changing the content of catalog.xml refreshes the catalogs and triggers validation. See [#212](https://github.com/eclipse/lemminx/issues/212).
* Switched to lsp4j 0.6.0 release. See [#254](https://github.com/eclipse/lemminx/issues/254).
* Added `xml.validation.noGrammar` preference, to indicate document won't be validated. See [#218](https://github.com/eclipse/lemminx/issues/218).
* Added preference to enable/disable validation `xml.validation.enabled` and `xml.validation.schema`. See [#260](https://github.com/eclipse/lemminx/issues/260).
* Deploy lsp4xml to a public Maven repository. [#229](https://github.com/eclipse/lemminx/issues/229).

### Bug Fixes

* Formatting unclosed tag would be in wrong location. See [#269](https://github.com/eclipse/lemminx/issues/269).
* Formatting removes DOCTYPE's public declaration. See [#250](https://github.com/eclipse/lemminx/issues/250).
* Infinite loop when `<` was typed into an empty DTD file. See [#266](https://github.com/eclipse/lemminx/issues/266).
* Formatting malformed xml removed content. See [#227](https://github.com/eclipse/lemminx/issues/227).
* Misplace diagnostic for cvc-elt.3.1. See [#241](https://github.com/eclipse/lemminx/issues/241).
* javax.xml.soap.Node is not available in Java 11. See [#238](https://github.com/eclipse/lemminx/issues/238).
* Adjust range for DTD validation errors. See [#107](https://github.com/eclipse/lemminx/issues/107).
* Adjust range error for internal DTD declaration. See [#225](https://github.com/eclipse/lemminx/issues/225).
* Don't add sibling element when completion items is filled with grammar. See [#211](https://github.com/eclipse/lemminx/issues/211).
* Validation needs additional `<uri>` catalog entry. See [#217](https://github.com/eclipse/lemminx/issues/217).
* XML Schema completion prefix did not work in some cases. See [#214](https://github.com/eclipse/lemminx/issues/214).
* Support rootUri for XML catalog configuration. See [#206](https://github.com/eclipse/lemminx/issues/206).
* CacheResourcesManager keeps trying to download unavailable resources. See [#201](https://github.com/eclipse/lemminx/issues/201).
* Support rootUri for XML catalog configuration. See [#206](https://github.com/eclipse/lemminx/issues/206).
* CacheResourcesManager keeps trying to download unavailable resources. See [#201](https://github.com/eclipse/lemminx/issues/201).
* Fix license headers according to project's declared EPL v2.0. See [#256](https://github.com/eclipse/lemminx/issues/256).

## [0.0.2](https://github.com/eclipse/lemminx/milestone/1?closed=1) (November 8, 2018)

### Enhancements

* Add support for `textDocument/documentLink` . See [#56](https://github.com/eclipse/lemminx/issues/56).
* No completion nor validation when editing an xsd schema. See [#178](https://github.com/eclipse/lemminx/issues/178).
* Cache on the file system, XML Schema from http, ftp before loading it. See [#159](https://github.com/eclipse/lemminx/issues/159).
* Support for XSL. See [#189](https://github.com/eclipse/lemminx/issues/189).
* Change 'resource downloading' diagnostic severity to Information. See [#187](https://github.com/eclipse/lemminx/pull/187).
* XSL support to resolve XML Schema of xsl. See [#91](https://github.com/eclipse/lemminx/issues/91).
* Add support for completion requests from empty character. See [#112](https://github.com/eclipse/lemminx/issues/112).
* Provide documentation on hover for attributes. See [#146](https://github.com/eclipse/lemminx/issues/146).

### Bug Fixes

* Formatting deletes document's body when there's a DTD declaration. See [#198](https://github.com/eclipse/lemminx/issues/198).
* Completion from local xsd was cached too aggressively. See [#194](https://github.com/eclipse/lemminx/issues/194).
* "format.splitAttributes:true" adds excessive indentation. See [#188](https://github.com/eclipse/lemminx/issues/188).
* No validation or code completion on nested elements. See [#177](https://github.com/eclipse/lemminx/issues/177).
* XSD files can only be edited if useCache is enabled. See [#186](https://github.com/eclipse/lemminx/issues/186).
* No autocompletion when writing XSDs. See [#111](https://github.com/eclipse/lemminx/issues/111).
* Insert required attribute code action inserts bad placeholder. See [#185](https://github.com/eclipse/lemminx/issues/185).
* No validation when referencing a schema in the same directory. See [#144](https://github.com/eclipse/lemminx/issues/144).
* Hover doesn't work when xs:annotation is declared in type and not element. See [#182](https://github.com/eclipse/lemminx/issues/182).
* Incomplete autocompletion for xsl documents. See [#165](https://github.com/eclipse/lemminx/issues/165).
* Auto Complete/ Completion for XML Prolog. See [#85](https://github.com/eclipse/lemminx/issues/85).
* `xml.format.splitAttributes` keeps first attribute on same line. See [#161](https://github.com/eclipse/lemminx/pull/161).
* File association should support relative path for systemId. See [#142](https://github.com/eclipse/lemminx/issues/142).
* Validation of non-empty nodes required to be empty shows misplaced diagnostics. See [#147](https://github.com/eclipse/lemminx/issues/147).
* Validation of empty required node shows misplaced diagnostics. See [#145](https://github.com/eclipse/lemminx/issues/145).
