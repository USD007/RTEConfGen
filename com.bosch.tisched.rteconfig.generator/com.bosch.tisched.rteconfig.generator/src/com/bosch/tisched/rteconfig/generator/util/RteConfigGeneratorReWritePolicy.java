/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;

import com.bosch.blueworx.core.logging.logmanager.ILogManager;
import com.bosch.blueworx.core.logging.logmanager.LogManager;
import com.bosch.blueworx.ulc.ILogEntry;
import com.bosch.blueworx.ulc.ILogWriter;
import com.bosch.blueworx.ulc.LogFactory;
import com.bosch.blueworx.ulc.entry.Language;


/**
 * @author SHK1COB
 */

public class RteConfigGeneratorReWritePolicy implements RewritePolicy {

  String mode;
  List<ILogEntry> logEntries;
  private final ILogManager logManager = LogManager.getInstance();
  private final String ulfFile;

  RteConfigGeneratorReWritePolicy(final String ulfFile, final String mode, final List<ILogEntry> logEntries) {
    this.mode = mode;
    this.logEntries = logEntries;
    this.ulfFile = ulfFile;
  }

  private String getMessageID(final String message) {

    String messageId = "";
    int index = message.indexOf("MM_DCS_RTECONFGEN_");

    if (index >= 0) {
      return message.substring(0, message.indexOf(" "));

    }


    return messageId;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogEvent rewrite(final LogEvent event) {
    if ((event.getMessage() != null) && !(event.getMessage().getFormattedMessage()).isEmpty()) {
      try {

        ILogEntry createLogEntry = this.logManager.createLogEntry();
        createLogEntry.setMessage(event.getMessage().getFormattedMessage());
        createLogEntry.setToolID("RteConfGen");
        createLogEntry.setToolVersion(RteConfigGeneratorConstants.TOOL_VERSION);
        createLogEntry.setLogLevel(event.getLevel().toString());
        createLogEntry.setIncidentURL("RteConfGen");
        String messageID = getMessageID(event.getMessage().getFormattedMessage());
        if ((messageID != null) && !messageID.isEmpty()) {
          createLogEntry.setMessageID(messageID);
          String str = event.getMessage().getFormattedMessage();
          createLogEntry.setMessage(str.substring(messageID.length() + 3, str.length()));
        }

        this.logEntries.add(createLogEntry);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }


    if ((Level.ERROR == event.getLevel()) && RteConfigGeneratorConstants.PRODUCTIVE_MODE.equals(this.mode)) {
      // String loggerName = event.getLoggerName();
      // Logger logger = org.apache.logging.log4j.LogManager.getLogger(loggerName);
      // logger.log(Level.ERROR, event.getMessage().getFormattedMessage());

      if ((this.logEntries != null) && !this.logEntries.isEmpty()) {
        File ulfFilee = new File(this.ulfFile);
        if (!ulfFilee.exists()) {
          try {
            ulfFilee.createNewFile();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
        String filePath = ulfFilee.getAbsolutePath();

        ILogWriter ulfLogWriter = LogFactory.createLogWriter(filePath);
        ulfLogWriter.setRootMessageLanguage(Language.EN);
        ulfLogWriter.setExpectedMessageLanguages(Language.EN, Language.DE);

        for (ILogEntry logEntry : this.logEntries) {
          ulfLogWriter.submit(logEntry);
        }

        ulfLogWriter.close();


      }

      // System.exit(1);
    }

    return event;
  }

}
