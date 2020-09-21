# Configuration

## Client

TODO...

```
	java -classpath ... org.eclipse.lsp4xml.XMLServerLauncher
```

## Existing Implementations

To see how current implementations of this language server send configurations,
refer to [VSCode XML](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-xml).

1. Go to preferences and set `xml.trace.server` to `verbose`
2. With an XML file open, go to View -> Output -> XML Support (top right drop down menu)
3. Ctrl + f, and search for 'workspace/didChangeConfiguration'

## Settings

Settings for **didChangeConfiguration** and **initializationOptions** must follow this JSON format:

```json
"settings": {
  "xml": {
    "trace": {
      "server": "verbose"
    },
    "catalogs": [],
    "logs": {
      "client": true,
      "file": "/home/.../lsp4xml.log"
    },
    "format": {
      "splitAttributes": true,
      "joinCDATALines": false,
      "joinContentLines": false,
      "joinCommentLines": false,
      "spaceBeforeEmptyCloseTag": false,
      "enabled": true
    }
  }
}  
```
  
  
Here a sample of JSON request message for "initialize": 

```json
{
  "jsonrpc":"2.0",
  "id":"1",
  "method":"initialize",
  "params":{
    "processId":17880,
    "rootPath":"test/",
    "rootUri":"/test/",
    "initializationOptions":{
      "settings":{
        "xml":{
          "trace":{
            "server":"verbose"
          },
          "catalogs":[

          ],
          "logs":{
            "client":true,
            "file":"/home/.../lsp4xml.log"
          },
          "format":{
            "splitAttributes":true,
            "joinCDATALines":false,
            "joinContentLines":false,
            "joinCommentLines":false,
            "spaceBeforeEmptyCloseTag":false,
            "enabled":true
          }
        }
      }
    }
  }
}
```

### Logging

* Only send 'file' on initialization

```json
{
  ...
  "logs": {
    "client": true,
    "file": "path/to/file"
  }
}
```

 * **client**: Should LSP message be sent to the client when logging.
 * **file**: Path to the file for log messages to be output.

### Catalog

```json
{
  "catalogs": [
      "catalog.xml",
      "catalog2.xml"
  ]
}
```
### File Associations

You can associate an XML file pattern with a XML Schema to use. It gives you the capability to benefit with validation and completion based on XML Schema without declaring it in your XML file.

Here a sample which associate the Eclipse file `.project` file with the XML Schema [projectDescription.xsd](https://github.com/angelozerr/lsp4xml/blob/master/org.eclipse.lsp4xml/src/test/resources/xsd/projectDescription.xsd)

```json
{
    "fileAssociations": [{
        "systemId": "C:\\org.eclipse.lsp4xml\\src\\test\\resources\\xsd\\projectDescription.xsd",
        "pattern": ".project"
    }]
}
```
fileAssociations  is an array of association:

 * systemId: file path of the XML Schema, DTD
 * pattern: file name pattern where you can use '*', '?' (it uses Java [glob](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob) NIO)
 

With this association, you benefit with validation and completion for file `.project` 

![XML File Associations](images/XMLFileAssociations.png)

although it doesn't declare DTD or XMl Schema:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>org.eclipse.lsp4xml</name>
	<comment></comment>
	<projects>
	</projects>
	<buildSpec>
		<buildCommand>
			<name>org.eclipse.jdt.core.javabuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
		<buildCommand>
			<name>org.eclipse.m2e.core.maven2Builder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>org.eclipse.jdt.core.javanature</nature>
		<nature>org.eclipse.m2e.core.maven2Nature</nature>
	</natures>
</projectDescription>
```

### Formatting


#### Excluded
In the LSP, the following options are given by [FormattingOptions](https://microsoft.github.io/language-server-protocol/specification#textDocument_formatting) so it is not necessary to pass these as configurations
* **insertSpaces**: indent using spaces
* **tabSize**: amount of spaces to indent by if insertSpaces == true



#### All Formatting Options

*It is not necessary to include each option, missing options while be set
to a default value shown below.

```json
{
  ...
  "format": {
    "enabled": true,
    "splitAttributes": false,
    "joinCDATALines": false,
    "joinCommentLines": false,
    "formatComments": true,
    "joinContentLines": false,
    "spaceBeforeEmptyCloseTag": true
  }
}
```
 * **enabled**: is able to format document
 * **splitAttributes**: each attribute is formatted onto new line
 * **joinCDATALines**: normalize content inside CDATA
 * **joinCommentLines**: normalize content inside comments
 * **formatComments**: keep comment in relative position 
 * **joinContentLines**: normalize content inside elements
 * **spaceBeforeEmptyCloseTag**: insert whitespace before self closing tag end bracket

### Capabilities

If a capability is sent from the client on 'initialize' indicating it  
supports 'dynamicRegistration' then the server can support enabling/disabling  
of the capability by the following:

 * **Note:** all dynamicRegistration capabilities are by default, true.

```json
{
  ...
  "capabilities": {
    "formatting": true
  }
}
```
Supported enabling/disabling:

 * **formatting**

### Completion

 * **Note:** all completion settings are by default, true.

```json
{
  ...
  "completion": {
    "autoCloseTags": true
  }
}
```
* **autoCloseTags:** When a start tag is typed the closing tag is automatically inserted as well,  
                    also schema completion will auto close tags if this is enabled.     

### Caching

Determines if schema's are cached on the system

```json
{
  ...
  "useCache": true
}
```                

### Validation

Ability to enable/disable validation.

```json
{
  ...
  "validation": {
    "noGrammar": "hint",
    "enabled": true,
    "schema": true
  }
}
```

* **noGrammar:** The message severity when a document has no associated grammar.  
  Values: ["ignore", "hint", "info", "warning", "error"] 
* **enabled:** True to enable all validation/diagnostics. Else false.
* **schema:** True to enable all schema validation/diagnostics. Else false.  

