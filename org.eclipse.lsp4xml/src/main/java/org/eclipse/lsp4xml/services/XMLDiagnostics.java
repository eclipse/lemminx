/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/

/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.internal.parser.Scanner;
import org.eclipse.lsp4xml.internal.parser.Token;
import org.eclipse.lsp4xml.internal.parser.TokenType;
import org.eclipse.lsp4xml.internal.parser.XMLScanner;
import org.eclipse.lsp4xml.services.extensions.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * XML diagnostics support.
 *
 */
class XMLDiagnostics {
  private static final Logger LOGGER = Logger.getLogger(XMLDiagnostics.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;
  private TextDocument xmlDocument;
  private static final int ERROR_THRESHOLD = 25;

  /** A stack used for finding missing start- and end-tag pairs */
  private Stack tagStack;

	public XMLDiagnostics(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<Diagnostic> doDiagnostics(TextDocument document, CancelChecker monitor) {
		this.xmlDocument = document;
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		try {
			return doBasicDiagnostics(document, diagnostics, monitor);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		doExtensionsDiagnostics(document, diagnostics, monitor);
		return diagnostics;
	}

	/**
	 * Do basic validation to check the no XML valid.
	 * 
	 * @param document
	 * @param diagnostics
	 * @param monitor
	 * @throws BadLocationException
	 */
	private List<Diagnostic> doBasicDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor)
			throws BadLocationException {
    tagStack = new Stack();
		Scanner scanner = XMLScanner.createScanner(document.getText());
		List previousRegion = null;
    List region = null;
    List startRootTagRegion = null;
    List endRootTagRegion = null;
    int endRootTagOffset = 0;
    boolean isClosed = true;
    boolean alreadyReported = false; //tag after root tag was already reported
    TokenType tokenType = scanner.scan();
    boolean wasEndRootTagFound = false;
    boolean wasStartRootTagFound = false;
    boolean attributesUsed = false;
		while (tokenType != TokenType.EOS) {
			monitor.checkCanceled();
			Token token = new Token(tokenType, scanner.getTokenText(), scanner.getTokenOffset(), scanner.getTokenEnd());
      isClosed = false;
      if ((tokenType == TokenType.Content) 
          || (tokenType == TokenType.StartPrologOrPI)
          || (tokenType == TokenType.StartTagOpen) || (tokenType == TokenType.EndTagOpen)
          || (tokenType == TokenType.StartCommentTag) || (tokenType == TokenType.CDATATagOpen)
          ) {
        // Validate the previous
        // Create a new Region
        previousRegion = region;
        region = new ArrayList(0);
        region.add(token);
        if (tokenType == TokenType.StartPrologOrPI) {
          checkContentBeforeProcessingInstruction(previousRegion, diagnostics);
        } 
      
        checkForTagClose(previousRegion, diagnostics);

        if(wasEndRootTagFound) { // Sets errors for illegal content outside of root
          Token startToken = (Token) region.get(0);
          if (!(startToken.type == TokenType.Content && tokenTextIsWhitespace(startToken.tokenText))) {
            Token endToken = (Token) region.get(region.size() - 1);
            
            int startOffset = startToken.startOffset;
            
            if (startOffset >= endRootTagOffset && tokenIsNotComment((Token) region.get(0))) {

              //Finds offset at beginning of line
              int start = startToken.startOffset;
              int end = startToken.endOffset;

              String messageText = XMLDiagnosticMessages.TAGS_OUTSIDE_OF_ROOT_TAG
                  + ((Token) endRootTagRegion.get(1)).tokenText;
                  createAndSetCustomDiagnostic(messageText, start, end, diagnostics);
              
              return diagnostics;
            }
          }
          
        }

      } else if ((tokenType == TokenType.StartTag) || (tokenType == TokenType.EndTag) || (tokenType == TokenType.AttributeName)
          || (tokenType == TokenType.DelimiterAssign) || (tokenType == TokenType.AttributeValue)
          || (tokenType == TokenType.Comment) || (tokenType == TokenType.Doctype)) {
        region.add(token);
        
        if (tokenType == TokenType.StartTag || tokenType == TokenType.EndTag) {
          checkForSpaceBeforeName(token, region, diagnostics);
        }

        if(wasStartRootTagFound == false && tokenType == TokenType.StartTag) {
          wasStartRootTagFound = true;
          startRootTagRegion = region;
        }
      } else if ((tokenType == TokenType.PrologEnd) || (tokenType == TokenType.StartTagClose) || (tokenType == TokenType.EndTagClose)
          || (tokenType == TokenType.StartTagSelfClose) || (tokenType == TokenType.EndCommentTag)
          || (tokenType == TokenType.CDATATagClose)) {
        region.add(token);
        
        checkNoSelfClosingBeforeRoot(region, diagnostics, wasStartRootTagFound, tokenType);
        
        if (tokenType == TokenType.PrologEnd) {
          //checkNamespacesInProcessingInstruction(region, diagnostics);
        } else if (tokenType == TokenType.StartTagClose || tokenType == TokenType.EndTagClose || tokenType == TokenType.StartTagSelfClose) {
          if(checkEmptyTag(region, diagnostics)) {
            continue;
          }
          final int regionLength = region.size();
          if (regionLength > 0) {
            Token first = (Token) region.get(0);
            if (first.type == TokenType.EndTagOpen) {
              checkAttributsInEndTag(first, region, diagnostics);
              if (first.type == TokenType.EndTagOpen && tagStack != null) {
                if (regionLength > 1) {
                  Token name = (Token) region.get(1);
                  if (tagStack.isEmpty()) {
                    // We have an end tag without a start tag
                    createMissingTagError(name, false, diagnostics);
                  } else {
                    if (!((Token) tagStack.peek()).tokenText.equals(name.tokenText)) {
                      boolean wasFound = false;
                      final int stackSize = tagStack.size();
                      for (int i = stackSize - 1; i >= 0; i--) {
                        Token pointer = (Token) tagStack.get(i);
                        if (pointer.tokenText.equals(name.tokenText)) {
                          wasFound = true;
                          Token top = null;
                          // Found the opening tag - everything in between that was unclosed should be
                          // flagged
                          while (!tagStack.isEmpty() && !(top = (Token) tagStack.pop()).tokenText.equals(pointer.tokenText)) {
                            createMissingTagError(top, true, diagnostics);
                          }
                          break;
                        }
                      }
                      if (!wasFound) {
                        // End tag doesn't have a matching start
                        createMissingTagError(name, false, diagnostics);
                      }
                    } else {
                      // We've got a match
                      tagStack.pop();
                    }
                  }
                }
              }
            } else if (first.type == TokenType.StartTagOpen) {
              checkAttributes(region, diagnostics);
              if ((tokenType == TokenType.StartTagClose) && tagStack != null && regionLength > 1) {
                Token name = (Token) region.get(1);
                if (name.type == TokenType.StartTag) {
                  tagStack.push(name);
                }
              }
            }
          }
        }
        if (wasEndRootTagFound == false && tokenType == TokenType.EndTagClose && endTagBelongsToRoot(startRootTagRegion,region)) {
          endRootTagRegion = region;
          wasEndRootTagFound = true;
          endRootTagOffset = ((Token) region.get(endRootTagRegion.size() - 1)).endOffset;
        }

        isClosed = true;
      }
      
      
			tokenType = scanner.scan();
    }
    if (!isClosed && region != null) {
      // Check some things about the last region, just in case it wasn't properly
      // closed
      final int regionLength = region.size();
      if (regionLength > 0) {
        final Token first = (Token) region.get(0);
        if (first.type == TokenType.StartPrologOrPI) {
          checkNamespacesInProcessingInstruction(region, diagnostics);
        }
        if (first.type == TokenType.StartTagOpen || first.type == TokenType.EndTagOpen) {
          checkForTagClose(region, diagnostics);
        }
        if (first.type == TokenType.StartTagOpen) {
          checkAttributes(region, diagnostics);
        }
      }
    }

    if (tagStack != null) {
      while (!tagStack.isEmpty()) {
        createMissingTagError((Token) tagStack.pop(), true, diagnostics);
      }
    }
    return diagnostics;
  }

  private void createOutOfBoundsDiagnostic(String messageText, int start, int length, List<Diagnostic> diagnostics) {
    
    Range range = createOutOfBoundsRange(start, length);
    Diagnostic tempDiagnostic = new Diagnostic(range, messageText);
    diagnostics.add(tempDiagnostic);
	}

private void checkNoSelfClosingBeforeRoot(List region, List<Diagnostic> diagnostics, boolean wasStartRootTagFound, TokenType tokenType) {
    if(!wasStartRootTagFound && tokenType == TokenType.StartTagSelfClose) {
      int start = ((Token) region.get(0)).startOffset;
      int end = ((Token) region.get(region.size() - 1)).endOffset;
      createAndSetCustomDiagnostic(XMLDiagnosticMessages.TAGS_OUTSIDE_OF_ROOT_TAG, start, end, diagnostics);
    }
	}

private boolean tokenIsNotComment(Token token) {
    return token.type != TokenType.CDATATagOpen;
  }
  
  private boolean endTagBelongsToRoot(List startRoot, List endRoot) {
    if(startRoot == null || endRoot == null) {
      return false;
    }
    return ((Token)startRoot.get(1)).tokenText.equals(((Token)endRoot.get(1)).tokenText) && tagStack.size() == 0;
  }

  private boolean tokenTextIsWhitespace(String tokenText) {
    return tokenText.trim().length() == 0;
  }

  /**
   * Checks that there is no content before the XML declaration
   * 
   * @param previousRegion the region prior to the processing instruction
   * @param reporter       the reporter
   */
	private void checkContentBeforeProcessingInstruction(List previousRegion, List<Diagnostic> diagnostics) {
		if (previousRegion != null && previousRegion.size() > 0) {
      Token first = (Token) previousRegion.get(0);
      if (first.type == TokenType.Content && first.startOffset == 0) {
        // XML declaration only allowed at the start of the document
				String messageText = XMLDiagnosticMessages.CONTENT_BEFORE_OPEN_TAG;
				createAndSetDiagnostic(first, messageText, diagnostics);
      }
    }
  }
  
    /**
   * Checks that the processing instruction name doesn't contain a namespace
   * 
   * @param region   the processing instruction region
   * @param reporter the reporter
   */
  private void checkNamespacesInProcessingInstruction(List region, List<Diagnostic> diagnostics) {
    final int regionLength = region.size();
    for (int i = 0; i < regionLength; i++) {
      Token t = (Token) region.get(i);
      if (t.type == TokenType.StartTag || t.type == TokenType.EndTag) {
        int index = t.tokenText.indexOf(":"); //$NON-NLS-1$
        if (index != -1) {
          String messageText = XMLDiagnosticMessages.PROLOG_CONTAINS_NAMESPACE;
          int start = t.startOffset + index;
          int length = t.tokenText.trim().length() - index;

          createAndSetCustomDiagnostic(messageText, start, start + length, diagnostics);
          break;
        }
      }
    }
  }

	private void checkForSpaceBeforeName(Token token, List region, List<Diagnostic> diagnostics) {
    if (region != null && region.size() == 2) {
      // Check that the start tag's name comes right after the <
      Token startBracket = (Token) region.get(0);
      Token tagName = (Token) region.get(region.size() - 1);
      if ((TokenType.StartTagOpen.equals(startBracket.type) || TokenType.EndTagOpen.equals(startBracket.type)) && tagName.startOffset - startBracket.endOffset != 0) {
        final String messageText = XMLDiagnosticMessages.SPACE_BEFORE_TAG_NAME;
        createAndSetCustomDiagnostic(messageText, startBracket.endOffset, tagName.startOffset, diagnostics);
      }
    }
    else {
      LOGGER.severe("Shouldn't reach here. Most likely Scanner tokens were off");
    }
    
	}
  
  /**
   * Check that when a start- or end-tag has been opened it is properly closed
   * with there being a '>' at the end
   * 
   * @param previousRegion the previous region
   * @param reporter       the reporter
   */
	private void checkForTagClose(List previousRegion, List<Diagnostic> diagnostics) {
    if (previousRegion != null && previousRegion.size() > 0) {
      final Token first = (Token) previousRegion.get(0);
      // If the previous region was a start- or end-tag, look for the tag close
      if (first.type == TokenType.StartTagOpen || first.type == TokenType.EndTagOpen) {
        final int length = previousRegion.size();
        boolean isClosed = false;
        int textLength = first.length;
        for (int i = 1; i < length; i++) {
          Token t = (Token) previousRegion.get(i);
          // Valid tag closings, EMPTY_TAG_CLOSE only works for a start tag, though
          if(first.type == TokenType.StartTagOpen) {
            if(isClosed = (t.type == TokenType.StartTagClose || t.type == TokenType.StartTagSelfClose) ? true : false) {
              break;
            }
          }
          if(first.type == TokenType.EndTagOpen) {
            if(isClosed = t.type == TokenType.EndTagClose ? true : false) {
              break;
            }
          }
          if (t.type == TokenType.StartTag || t.type == TokenType.EndTag) {
            textLength += t.length;
          }
        }
        if (!isClosed) {
          String messageText = XMLDiagnosticMessages.TAG_NOT_CLOSED;
          
          createAndSetCustomDiagnostic(messageText, first.startOffset, first.startOffset + textLength, diagnostics);
          
        }
      }
    }
  }

  /**
   * Check that a tag has a name (<> is invalid)
   * 
   * @param token    The xml tag close token
   * @param region   the tag region
   * @param reporter the reporter
   */
  private boolean checkEmptyTag(List region, List<Diagnostic> diagnostics) {
    if (region.size() == 2) {
      // Check that the tag is not empty
      Token first = (Token) region.get(0);
      if (first.type == TokenType.StartTagOpen || first.type == TokenType.EndTagOpen) {
        String messageText = XMLDiagnosticMessages.TAG_MISSING_NAME;
        Token second = (Token) region.get(1);
        createAndSetCustomDiagnostic(messageText, first.startOffset, second.endOffset, diagnostics);
        return true;
      }
    }
    return false;
  }

  /**
   * Checks the end-tag region for attributes. There should be no attributes in
   * the end tag
   * 
   * @param first    the first token in the region
   * @param region   the end-tag region
   * @param reporter the reporter
   */
  private void checkAttributsInEndTag(Token first, List region, List<Diagnostic> diagnostics) {
    int errors = 0;
    int start = first.startOffset, end = first.startOffset;
    final int regionLength = region.size();

    // Start at one, since we know the first token is an tag-open
    for (int i = 1; (i < regionLength) && (errors < ERROR_THRESHOLD); i++) {
      Token t = (Token) region.get(i);
      if ((t.type == TokenType.AttributeName) || (t.type == TokenType.DelimiterAssign)
          || (t.type == TokenType.AttributeValue)) {
        if (start == first.startOffset) {
          start = t.startOffset;
        }
        end = t.startOffset + t.length;
        errors++;
      }
    }

    // create one error for all attributes in the end tag
    if (errors > 0) {
      // Position p = new Position(start, end - start);
      String messageText = XMLDiagnosticMessages.END_TAG_HAD_ATTRIBUTES;
      createAndSetDiagnostic(first, messageText, diagnostics);

    }
  }

    /**
   * Creates a missing tag error for the token
   * 
   * @param token      the token that's missing its pair tag
   * @param isStartTag is the token a start tag
   * @param reporter   the reporter
   */
  private void createMissingTagError(Token token, boolean isStartTag, List<Diagnostic> diagnostics) {
    
    String messageText = (isStartTag ? XMLDiagnosticMessages.MISSING_END_TAG : XMLDiagnosticMessages.MISSING_START_TAG);
    createAndSetDiagnostic(token, messageText, diagnostics);
  }

   /**
   * Checks that all the attribute in the start-tag have values and that those
   * values are properly quoted
   * 
   * @param region   the start-tag region
   * @param reporter the reporter
   */
  private void checkAttributes(List region, List<Diagnostic> diagnostics ) {
    int attrState = 0;
    int errorCount = 0;
    final int regionLength = region.size();

    //The region should have been checked to be closed before
    // < 4 will imply no attribute values
    if(regionLength < 4) {
      return;
    }

    // Start at one, since we know the first token is an tag-open
    for (int i = 1; i < regionLength && errorCount < ERROR_THRESHOLD; i++) {
      Token t = (Token) region.get(i);
      if (t.type == TokenType.AttributeName || t.type == TokenType.StartTagClose || t.type == TokenType.EndTagClose
          || t.type == TokenType.StartTagSelfClose) {
        // dangling name and '='
        if ((attrState == 2) && (i >= 2)) {
          // create annotation
          Token nameRegion = (Token) region.get(i - 2);
          Object[] args = { nameRegion.tokenText };
          String messageText = XMLDiagnosticMessages.ATTRIBUTE_MISSING_VALUE;

          // quick fix info
          int delimiterIndex = i - 2 + 1;
          int attributeNameIndex = delimiterIndex - 1;
          Token delimiter = (Token) region.get(delimiterIndex);
          Token attributeName = (Token) region.get(attributeNameIndex);
          int start = delimiter.endOffset;
          int end = attributeName.startOffset;
         
          createAndSetCustomDiagnostic(messageText, start, end, diagnostics);

          
        }
        // name but no '=' (XML only)
        else if ((attrState == 1) && (i >= 1)) {
          // create annotation
          Token nameToken = (Token) region.get(i - 1);
          Object[] args = { nameToken.tokenText };
          String messageText = XMLDiagnosticMessages.ATTRIBUTE_NO_EQUALS;
          int start = nameToken.startOffset;
          int textLength = nameToken.tokenText.trim().length();
          //int lineNo = nameToken.line;

          createAndSetDiagnostic(nameToken, messageText, diagnostics);
        }
        attrState = 1;
      } else if (t.type == TokenType.DelimiterAssign) {
        attrState = 2;
      } else if (t.type == TokenType.AttributeValue) {
        attrState = 0;

        // Check that there are quotes around the attribute value and that they match
        final String trimmed = t.tokenText.trim();
        if (trimmed.length() > 0) {
          final char q1 = trimmed.charAt(0), q2 = trimmed.charAt(trimmed.length() - 1);
          if ((q1 == '\'' || q1 == '"') && (q1 != q2 || trimmed.length() == 1)) {
            // missing closing quote
            String message = XMLDiagnosticMessages.MISSING_CLOSED_QUOTE;
            createAndSetCustomDiagnostic(message, t.startOffset, t.endOffset, diagnostics); 
            errorCount++;
          } else if (q1 != '\'' && q1 != '"') {
            // missing both
            String message = XMLDiagnosticMessages.MISSING_BOTH_QUOTES;
            createAndSetCustomDiagnostic(message, t.startOffset, t.endOffset, diagnostics);
          }
        }

      }
    }
  }


  /**
   * Creates an error related to an attribute
   */
  private void createAndSetCustomDiagnostic(String messageText, int start, int end, List<Diagnostic> diagnostics) {
    Range range = createRange(start, end);
    Diagnostic tempDiagnostic = new Diagnostic(range, messageText);
    diagnostics.add(tempDiagnostic);
  }

	/**
	 * Do validation with extension (XML Schema, etc)
	 * 
	 * @param document
	 * @param diagnostics
	 * @param monitor
	 */
	private void doExtensionsDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor) {
		for (IDiagnosticsParticipant diagnosticsParticipant : extensionsRegistry.getDiagnosticsParticipants()) {
			diagnosticsParticipant.doDiagnostics(document, diagnostics, monitor);
		}
	}

  
	private Range createRange(int startOffset, int endOffset) {
		
		try {
		  Position	start = xmlDocument.positionAt(startOffset);
			Position end = xmlDocument.positionAt(endOffset);
			return new Range(start,end);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In Diagnostics the given offset was a BadLocation", e);
		}
		return null;
  }  
  
  private Range createOutOfBoundsRange(int startOffset, int additional) {
		
		try {
		  Position	start = xmlDocument.positionAt(startOffset);
      Position end = xmlDocument.positionAt(startOffset);
      end.setCharacter(end.getCharacter() + additional);
			return new Range(start,end);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In Diagnostics the given offset was a BadLocation", e);
		}
		return null;
	} 

	private Range createRange(Token token) {
		Position start;
		try {
			start = xmlDocument.positionAt(token.startOffset);
			Position end = xmlDocument.positionAt(token.endOffset);
			return new Range(start,end);
		} catch (BadLocationException e) {
			//TODO
		}
		return null;
	}

	private void createAndSetDiagnostic(Token token, String messageText, List<Diagnostic> diagnostics) {
		Range range = createRange(token);
		Diagnostic tempDiagnostic = new Diagnostic(range, messageText);
		diagnostics.add(tempDiagnostic);
	}

}
