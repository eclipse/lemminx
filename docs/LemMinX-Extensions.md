# LemMinX Extensions

The LemMinX XML Language Server can be extended with custom plugins to provide additional validation and assistance. Typically this is done for specific files or contexts.

Many of the XML language features provided by LemMinX are implemented using built-in LemMinX extensions. For example you can look at the:

- Built-in [content model plugin](https://github.com/eclipse/lemminx/tree/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/extensions/contentmodel) to provide completion, validation, hover based on XML Schema.
- Built-in [XSL Plugin](https://github.com/eclipse/lemminx/tree/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/extensions/xsl) that registers an XSD schema for XSL.
- See all the [built-in extensions](https://github.com/eclipse/lemminx/tree/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/extensions) for more examples.

External extensions are not built into LemMinX but instead are contributed via an external JAR. For example the:

- [LemMinX Maven extension](https://github.com/eclipse/lemminx-maven/) provides extra assistance for the Maven pom.xml files.
- [LemMinX Liquibase extension](https://github.com/Treehopper/liquibase-lsp/) provides extra assistance for Liquibase XML migration scripts, adding database validation (as diagnostics) using an in-memory database.
- [LemMinX Liberty extension](https://github.com/OpenLiberty/liberty-language-server/tree/master/lemminx-liberty) provides extra assistance for the Open Liberty runtime server.xml file. Adding completion options and diagnostics for Liberty features.

## Creating a LemMinX extension.

LemMinX is extended using the [Java Service Provider Interface (SPI)](https://www.baeldung.com/java-spi). You can extend LemMinX to provide custom completion, hover, diagnostics, renaming etc.
You can find the complete [LemMinX extension API here](https://github.com/eclipse/lemminx/tree/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions).

To start developing a LemMinX extension, create a new Java Project that includes LemMinX as a provided dependency. Also make sure to include the `lemminx-releases` repository.

```xml
    <dependency>
      <groupId>org.eclipse.lemminx</groupId>
      <artifactId>org.eclipse.lemminx</artifactId>
      <version>0.12.0</version>
      <scope>provided</scope>
    </dependency>
```

```xml
  <repositories>
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
  </repositories>
```

Create the entry point for your LemMinX extension by creating a class that implements [IXMLExtension](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/IXMLExtension.java). 

```java
package org.samples.lemminx.extensions.foo;

import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.eclipse.lsp4j.InitializeParams;

public class FooPlugin implements IXMLExtension {

	@Override
	public void doSave(ISaveContext context) {
		// Called when settings or XML document are saved.
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		// Register here completion, hover, etc participants
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		// Unregister here completion, hover, etc participants
	}
}
```

This class should register language feature participants (Classes that implement `ICompletionParticipant`, `IHoverParticipant`, `IDiagnosticsParticipant`, etc). For example the [MavenDiagnosticParticipant](https://github.com/eclipse/lemminx-maven/blob/master/lemminx-maven/src/main/java/org/eclipse/lemminx/extensions/maven/MavenDiagnosticParticipant.java). These participants should be registered in the [start method](https://github.com/eclipse/lemminx-maven/blob/f9b38316599d62ad944c3042127c99f674c30079/lemminx-maven/src/main/java/org/eclipse/lemminx/extensions/maven/MavenLemminxExtension.java#L101) of your XMLExtension.

To register your extension with LemMinX using Java SPI you need to create a [/META-INF/services/org.eclipse.lemminx.services.extensions.IXMLExtension](https://github.com/eclipse/lemminx-maven/blob/master/lemminx-maven/src/main/resources/META-INF/services/org.eclipse.lemminx.services.extensions.IXMLExtension) file that declares your implementation of IXMLExtension. 

```
org.samples.lemminx.extensions.foo.FooPlugin
```
When a JAR of your extension is contributed to the classpath of LemMinX, LemMinX is able to discover and use your implementation of IXMLExtension.

## Adding language features

The [LemMinx Extensions API](https://github.com/eclipse/lemminx/tree/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions) supports many of the language features defined in the [Language server Protocol](https://microsoft.github.io/language-server-protocol/). You can add these features to your extension by implementing the feature participant and registering the participant in your implementation of IXMLExtension. This includes:

- Diagnostics with [IDiagnosticsParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/diagnostics/IDiagnosticsParticipant.java)
- Code actions with [ICodeActionParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/ICodeActionParticipant.java)
- Code Completion with [ICompletionParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/ICompletionParticipant.java)
- Go to Definition with [IDefinitionParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/IDefinitionParticipant.java)
- Adding document Links with [IDocumentLinkParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/IDocumentLinkParticipant.java)
- Highlighting with [IHighlightingParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/IHighlightingParticipant.java)
- Hover information with [IHoverParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/IHoverParticipant.java)
- Find references with [IReferenceParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/IReferenceParticipant.java)
- Rename symbols with [IRenameParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/IRenameParticipant.java)
- Type Definitions with [ITypeDefinitionParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/ITypeDefinitionParticipant.java)
- CodeLens with [ICodeLensParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/codelens/ICodeLensParticipant.java)
- Formatter with [IFormatterParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/format/IFormatterParticipant.java)
- Symbols with [ISymbolsProviderParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/ISymbolsProviderParticipant.java)
- Monitoring workspace folders with [IWorkspaceServiceParticipant](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/IWorkspaceServiceParticipant.java)

## XML Language Server services available for extensions
XML Language Server extension may need to use standard Language Server features such as commands, documents and ability to manipulate documents. These are available to extensions indirectly via specialized service API.

#### Command Service
The command service is a wrapper around Language Server client functionality allowing to register/unregister/execute commands via Language Server Protocol. The service can be accessed like the following:

```java
public class FooPlugin implements IXMLExtension {

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		// Register here custom server command
		IXMLCommandService commandService = registry.getCommandService();
		commandService.registerCommand("my-cmd", (params, cancelChecker) -> "executed");		
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		// Unregister here custom server command
		IXMLCommandService commandService = registry.getCommandService();
		commandService.unregisterCommand("my-cmd");		
	}
}
```

There are two types of commands XML LS allows extender to work with via `IXMLCommandService`:
 - Server command that can be executed from the client (via `workspace/executeCommand` request message from the LSP spec)
 - Client command that can be executed from the server (via `xml/executeClientCommand` request message - XML extension of the LSP spec)
 
A server command should implement the [IDelegateCommandHandler](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/IXMLCommandService.java).

```java
	public interface IDelegateCommandHandler {
		
		/**
		 * Executes a command
		 * @param params command execution parameters
		 * @param cancelChecker check if cancel has been requested
		 * @return the result of the command
		 * @throws Exception the unhandled exception will be wrapped in
		 *                   <code>org.eclipse.lsp4j.jsonrpc.ResponseErrorException</code>
		 *                   and be wired back to the JSON-RPC protocol caller
		 */
	    Object executeCommand(ExecuteCommandParams params, CancelChecker cancelChecker) throws Exception;	    
	}
```

A server command can be registered/unregistered like follows:

```java
	// Command registration
	commandService.registerCommand("my-cmd", (params, cancelChecker) -> "executed");
		
	//Unregister command
	commandService.unregisterCommand("my-cmd");
```

A client command can be executed like follows:

```java
	// Opens up terminal view in VSCode client
	commandService.executeClientCommand(new ExecuteCommandParams("workbench.action.terminal.toggleTerminal", Collections.emptyList()))
```

Note that XML LS client (VSCode in particular) registers a client command `xml.workspace.executeCommand` to allow other extensions to execute XML LS commands. Thus XML LS extension can execute `"my-cmd"` command registered above with the following snippet:

```java
	// Execute command. The result should be "executed" string
	Object result = commandService.executeClientCommand(new ExecuteCommandParams("xml.workspace.executeCommand", Arrays.asList("my-cmd"))).get();
```

See definition of [IXMLCommandService](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/extensions/commands/IXMLCommandService.java)

#### Document Provider
The document provider allows for finding the document from the document URI and listing all XML documents. Note that the document provider is only aware of the XML documents it  is working with (opened XML documents).

See definition of [IXMLDocumentProvider](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/IXMLDocumentProvider.java)

#### Validation Service
The validation service allows for triggering validation of all opened XML documents on server side.

See definition of [IXMLValidationService](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/services/IXMLValidationService.java)

## Adding custom settings for your extension

Your extension can have its own custom settings to control its behavior. Your new setting must start with the prefix `xml`, so for example `xml.myextension.mycustomsetting`. Reading the settings can be done from the `doSave` method in your XMLExtension. The `doSave` method is called with a `Settings` context on extension startup and also whenever the settings are updated. For an example you can look at the [Content Model Settings](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/extensions/contentmodel/settings/ContentModelSettings.java) and how they are updated in the [doSave method](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/main/java/org/eclipse/lemminx/extensions/contentmodel/ContentModelPlugin.java#L73).

## Testing your custom LemMinX extension

If you are using lemminx version >= `0.14.0` to build your extension you can use the lemminx [XMLAssert](https://github.com/eclipse/lemminx/blob/master/org.eclipse.lemminx/src/test/java/org/eclipse/lemminx/XMLAssert.java) class to test the functionality of your extension.

XMLAssert provides many static helper methods for building lsp4j structures such as CompletionItem, TextEdit, Hover etc. 

For example to build a CompletionItem that uses a TextEdit you can use the `c` and `te` methods.
```java
CompletionItem testCompletionItem = c("xsl:template", te(2, 0, 2, 0, "<xsl:template></xsl:template>"), "xsl:template");
```

Along with building lsp4j structures, XMLAssert contains static methods that test xml language functionality.

For example to test that completion options exist, we build an XML String that contains a `|` character. The `|` is used to represent where the completion has been triggered in the XML file.

```java
String xml = "<?xml version=\"1.0\"?>\r\n" +
		"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n" +
		"|";

testCompletionFor(xml,
		c("xsl:template", te(2, 0, 2, 0, "<xsl:template></xsl:template>"), "xsl:template"), 
		c("xsl:import", te(2, 0, 2, 0, "<xsl:import href=\"\" />"), "xsl:import")
);
```

For more examples on how to use XMLAssert you can look at the [tests for the built-in lemminx extensions](https://github.com/eclipse/lemminx/tree/master/org.eclipse.lemminx/src/test/java/org/eclipse/lemminx/extensions).

## Integrating your extension with Editors/IDEs

VSCode with [vscode-xml](https://github.com/redhat-developer/vscode-xml)

- See documentation on [contributing your extension to vscode-xml](https://github.com/redhat-developer/vscode-xml/blob/master/docs/Extensions.md)

Eclipse with [Wild Web Developer](https://github.com/eclipse/wildwebdeveloper)

- Contribute your extension to the classpath of LemMinX using the [org.eclipse.wildwebdeveloper.xml.lemminxExtension](https://github.com/eclipse/wildwebdeveloper/blob/27fc7b619c5a8683ad919cb7ca3e6bdbfcdc35c9/org.eclipse.wildwebdeveloper.xml/plugin.xml#L4) extension point.
