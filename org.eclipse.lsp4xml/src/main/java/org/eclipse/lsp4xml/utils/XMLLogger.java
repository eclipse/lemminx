package org.eclipse.lsp4xml.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
/**
 * XMLLogger
 */
public class XMLLogger {


//public class MyLogger {

  //Referenced from: http://www.vogella.com/tutorials/Logging/article.html    
  static private FileHandler fh;
  static private SimpleFormatterMod formatterTxt;
  


  public static void setup() {
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    logger.setLevel(Level.INFO);
    logger.setUseParentHandlers(false);//Stops output to console  
  }

  public static void logCatch(Exception e) {
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    logger.setLevel(Level.INFO);
    try {
      new File("Logs/").mkdirs();
      fh = new FileHandler("Logs/Logging.txt", true);
    } catch (SecurityException eSE) {
      //TODO
    } catch (IOException eIO) {
      //TODO
    }
          
    int length = e.getStackTrace().length;
    int i;
    for (i = 0; i < length; i++) {
      if (e.getStackTrace()[i].getFileName().endsWith("Test.java")) {
        break;
      }
    }
    String errorFile = e.getStackTrace()[2].getFileName();
    int errorFileLine = e.getStackTrace()[2].getLineNumber();
    String msg;
    if (i == length) {
      formatterTxt = new SimpleFormatterMod();
      msg = "\n\t\t[Failure at" + errorFile + " | Line: " + errorFileLine + "]" +
                   "\n\t\t\t" + e.getMessage() + "\n\t";
    }
    else{
      String initialTestClassName = e.getStackTrace()[i].getClassName();
      String initialTestMethodName = e.getStackTrace()[i].getMethodName();
      formatterTxt = new SimpleFormatterMod(initialTestClassName, initialTestMethodName);
      String line1 = "\n\t\t Error in {" + errorFile + "} on line {" + errorFileLine + "}";
      String line2 = "\n\t\t\t Error Message: {" + e.getMessage() + "}\n";
      msg = line1 + line2;
    }

    fh.setFormatter(formatterTxt);
    logger.addHandler(fh);
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE,msg);
    fh.close();
  }

}