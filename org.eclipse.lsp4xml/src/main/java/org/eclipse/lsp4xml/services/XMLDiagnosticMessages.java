package org.eclipse.lsp4xml.services;

/**
 * XMLDiagnosticMessages
 */
public class XMLDiagnosticMessages {

  public static final String CONTENT_BEFORE_PROLOG = "This should not be before the prolog (<?xml ... ?>)";
  public static final String CONTENT_BEFORE_OPEN_TAG = "This content should not be before the initial root tag";
  public static final String TAG_MISSING_NAME = "This tag is missing it's tag name";
  public static final String TAG_NOT_CLOSED = "This tag is not closed, add a '>'";
  public static final String PROLOG_CONTAINS_NAMESPACE = "This prolog contains a namespace";
  public static final String MISSING_START_TAG = "The start tag is missing";
  public static final String MISSING_END_TAG = "The end tag is missing";
  public static final String ATTRIBUTE_MISSING_VALUE = "This attribute is missing a value";

  public static final String ATTRIBUTE_NO_EQUALS = "This attribute is missing the '=' delimiter";
  public static final String MISSING_CLOSED_QUOTE = "This attribute value is missing a closing quote";
  public static final String MISSING_BOTH_QUOTES = "This attribute value is missing quotes";
  public static final String END_TAG_HAD_ATTRIBUTES = "The end tag should not have attribute(s) in it";
  public static final String CONTENT_OUTSIDE_OF_ROOT_TAG = "Everything under and including this should be nested in root element";
  public static final String SPACE_BEFORE_TAG_NAME = "Space(s) should not be before tag name";

}