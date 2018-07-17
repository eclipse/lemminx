package org.eclipse.lsp4xml.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XMLLogger
 */
public class XMLLogger {
  private Logger logger;
  private static String outputFile = "LoggerOutput.xml";
  
  public XMLLogger(String className) {
    String[] classNameSplit = className.split("\\.");
    String shortClassName = classNameSplit[classNameSplit.length - 1];
    this.logger = Logger.getLogger(className);
    File logsDir = new File("Logs/");
    logsDir.mkdir();
    
    FileHandler fh = null;
    String filePath = "Logs/" + shortClassName + outputFile;
    try {
      new FileWriter(filePath, false).close();
      fh = new FileHandler(filePath);
    } catch (SecurityException e) {  
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    logger.addHandler(fh);
    logger.setLevel(Level.ALL);
  }

  public Logger getLogger() {
    return logger;
  }

  public void logCatch(Exception e) {
    String msg = "\n\t\t[" + e.getStackTrace()[2].getFileName() + " | Line: " + e.getStackTrace()[2].getLineNumber() + "]" +
                   "\n\t\t\t" + e.getMessage() + "\n\t";
    //String cleanMsg = "[" + e.getStackTrace()[2].getFileName() + " | Line: " + e.getStackTrace()[2].getLineNumber() + "] " + e.getMessage();
    this.logger.log(Level.SEVERE,  msg);
  }



}