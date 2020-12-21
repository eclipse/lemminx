/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.utils;

import static java.lang.System.lineSeparator;

import java.util.Properties;
import java.util.ResourceBundle;

public class ServerInfo {
  private Properties sysProps;

  static final String MASTER = "master";

  // https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.core/src/com/oracle/svm/core/jdk/SystemPropertiesSupport.java#L97
  private static final boolean IS_NATIVE_IMAGE = "Substrate VM".equals(System.getProperty("java.vm.name"));

  private ResourceBundle rb = ResourceBundle.getBundle("git");

  public ServerInfo() {
    this(null);
  }

  //For testing purposes
  ServerInfo(Properties props) {
    this.sysProps = new Properties(props == null?System.getProperties():props);
  }

  /**
   * @return the server version
   */
  public String getVersion() {
    return rb.getString("git.build.version");
  }

  /**
   * @return the git commit id, used to build the server
   */
  public String getShortCommitId() {
    return rb.getString("git.commit.id.abbrev");
  }

  /**
   * @return the git commit message, used to build the server
   */
  public String getCommitMessage() {
    return rb.getString("git.commit.message.short");
  }

  /**
   * @return the Java Home used to launch the server
   */
  public String getJava() {
    return sysProps.getProperty("java.home", "unknown");
  }

  /**
   * @return the git branch used to build the server
   */
  public String getBranch() {
    return rb.getString("git.branch");
  }

  @Override
  public String toString() {
    return getVersion();
  }

  /**
   * Returns the server details, using the format:<br/>
   * <pre>
   * LemMinX Server info:
   *  - Version : (build version)
   *  - Java : (path to java.home])
   *  - Git : ([Branch] short commit id - commit message)
   * </pre>
   *
   * @return the formatted server details
   */
  public String details() {
    StringBuilder details = new StringBuilder();
    details.append("LemMinX Server info:");
    append(details, "Version", getVersion());
    if (IS_NATIVE_IMAGE) {
      append(details, "Native Image", null);
    } else {
      append(details, "Java", getJava());
    }
    append(details, "VM Version", System.getProperty("java.vm.version"));
    append(details, "Git", null);
    String branch = getBranch();
    if (!MASTER.equals(branch)) {
      details.append(" [Branch ")
      .append(branch)
      .append("]");
    }
    details.append(" ")
           .append(getShortCommitId())
           .append(" - ")
           .append(getCommitMessage());
    return details.toString();
  }

  private void append(StringBuilder sb, String key, String value){
    sb.append(lineSeparator())
      .append(" - ")
      .append(key);
      if (value != null) {
        sb.append(" : ")
        .append(value);
      }
  }
}
