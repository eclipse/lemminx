package org.eclipse.lsp4xml.settings;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


import org.eclipse.lsp4j.FormattingOptions;

/**
 * This class is the root of all formatting settings.
 * It is necessary to update this class for any new additions.
 * 
 * All defaults should be set here to eventually be overriden if needed.
 */
public class XMLFormatterSettings extends FormattingOptions{
  private static final Logger LOGGER = Logger.getLogger(XMLFormatterSettings.class.getName());

  //All possible keys
  public final static String SPLIT_ATTRIBUTES = "splitAttributes";
  public final static String JOIN_CDATA_LINES = "joinCDATALines";
  public final static String FORMAT_COMMENTS = "formatComments";
  public final static String JOIN_COMMENT_LINES = "joinCommentLines";


  public XMLFormatterSettings() {
    initializeDefaultSettings();
  }

  public XMLFormatterSettings(int tabSize, boolean insertSpaces) {
    super(tabSize,insertSpaces);
    initializeDefaultSettings();
  }

  public XMLFormatterSettings(FormattingOptions options) {
    super(options.getTabSize(), options.isInsertSpaces());
    initializeDefaultSettings();
  }

 

  //***Keep this up to date with all possible settings***
  private void initializeDefaultSettings() { 
    this.putBoolean(XMLFormatterSettings.SPLIT_ATTRIBUTES, false);
    this.putBoolean(XMLFormatterSettings.JOIN_CDATA_LINES, false);
    this.putBoolean(XMLFormatterSettings.FORMAT_COMMENTS, true);
    this.putBoolean(XMLFormatterSettings.JOIN_COMMENT_LINES, false);
  }

  

  public void updateSettingsFromMap(Map<String, Object> newSettings) {
    if(newSettings != null) {
      for (String key : newSettings.keySet()) {
        Object value = newSettings.get(key);
        if(value instanceof Double) {    
          this.putNumber(key, (Double) value);
        }
        else if(value instanceof Boolean) {
          this.putBoolean(key, (Boolean) value);
        }
      }
    }
  }

  public int getInt(String key) {
    return this.getNumber(key).intValue();
  }

  public boolean isSplitAttributes() {
    return getBoolean(XMLFormatterSettings.SPLIT_ATTRIBUTES);
  }

  public boolean isJoinCDATALines() {
    return getBoolean(XMLFormatterSettings.JOIN_CDATA_LINES);
  }

  public boolean isFormatComments() {
    return getBoolean(XMLFormatterSettings.FORMAT_COMMENTS);
  }

  public boolean isJoinCommentLines() {
    return getBoolean(XMLFormatterSettings.JOIN_COMMENT_LINES);
  }

public void updateFromFormattingOptions(FormattingOptions formattingOptions) {
  this.setTabSize(formattingOptions.getTabSize());
  this.setInsertSpaces(formattingOptions.isInsertSpaces());
}

}