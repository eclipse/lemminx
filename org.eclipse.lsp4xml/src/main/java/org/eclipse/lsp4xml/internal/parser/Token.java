package org.eclipse.lsp4xml.internal.parser;

/**
 * Token
 */
public class Token {
  public TokenType type;
  public String tokenText;
  public int startOffset;
  public int endOffset;
  public int length;

  public Token(TokenType type, String tokenText, int startOffset, int endOffset) {
    this.type = type;
    this.tokenText = tokenText;
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.length = endOffset - startOffset;
  }
}