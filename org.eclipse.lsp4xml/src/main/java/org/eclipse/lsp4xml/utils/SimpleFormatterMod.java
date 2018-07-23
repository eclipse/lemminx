package org.eclipse.lsp4xml.utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * SimpleFormatterMod
 */
public class SimpleFormatterMod extends Formatter{
  //private static final String format = LoggingSupport.getSimpleFormat();
  private final Date dat = new Date();
  private String testClassName;
  private String testMethodName;

  public SimpleFormatterMod() {
    
  }

  public SimpleFormatterMod(String testClassName, String testMethodName) {
    this.testClassName = testClassName;
    this.testMethodName = testMethodName;
  }
  @Override
  public synchronized String format(LogRecord record) {
    dat.setTime(record.getMillis());
    String source;
    if(testClassName != null) {
      source = " " + testClassName;
      if(testMethodName != null) {
        source += " " + testMethodName + "()";
      }
    }
    else{
      if (record.getSourceClassName() != null) {
        source = record.getSourceClassName();
        if (record.getSourceMethodName() != null) {
           source += " " + record.getSourceMethodName();
        }
      } else {
        source = record.getLoggerName();
      }
    }
    
    String message = formatMessage(record);
    
    return dat + source + message + "\n";
  }
  
}