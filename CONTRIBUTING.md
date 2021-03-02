# How to Contribute

Contributions are essential for keeping this language server great. We try to keep it as easy as possible to contribute changes and we are open to suggestions for making it even easier. There are only a few guidelines that we need contributors to follow.

## Development

### Installation Prerequisites:

  * [JDK 8+](https://adoptopenjdk.net/)
  * [Maven](https://maven.apache.org/)

### Steps
1. Fork and clone this repository

2. Build/Test LemMinx on Mac/Linux:
	```bash
	$ ./mvnw verify
	```
	or for Windows:
	```bash
	$ mvnw.cmd verify
	```

### Debug

The LemMinx language server must be debugged remotely as it's most useful when connected to a client. In order to debug, one needs to look at whether the specific language client provides such a capability. For example :

* [Eclipse Wild Web Developer](https://github.com/eclipse/wildwebdeveloper) can set LemMinx in debug mode by passing the system property `org.eclipse.wildwebdeveloper.xml.internal=${port}` to have the language server listening to debug connections on ${port}

https://github.com/eclipse/wildwebdeveloper/blob/master/org.eclipse.wildwebdeveloper.xml/src/org/eclipse/wildwebdeveloper/xml/internal/XMLLanguageServer.java#L75

* [VSCode XML](https://github.com/redhat-developer/vscode-xml) can set LemMinx in debug mode by either debugging the extension itself, or by adding `-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=${port}` to the `"xml.server.vmargs"` client setting.

https://github.com/redhat-developer/vscode-xml/blob/master/src/server/java/javaServerStarter.ts#L45

### Pull Requests

In order to submit contributions for review, please make sure you have signed the [Eclipse Contributor Agreement](https://www.eclipse.org/legal/ecafaq.php) (ECA) with your account.
