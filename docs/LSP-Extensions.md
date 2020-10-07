The XML Language Server implements the following extensions that are not part of the standard LSP 

| Method                   | Kind              | Notes                                                                |
|--------------------------|-------------------|----------------------------------------------------------------------|
| xml/closeTag             | Request to server | Retrieves the close Tag to insert in a given position of a document. |
| xml/executeClientCommand | Request to client | Executes command on the client.                                      |
