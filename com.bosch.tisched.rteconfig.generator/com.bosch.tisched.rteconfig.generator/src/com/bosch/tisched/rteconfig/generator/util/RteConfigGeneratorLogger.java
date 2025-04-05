/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.bosch.blueworx.core.logging.logmanager.ILogManager;
import com.bosch.blueworx.ulc.ILogEntry;

/**
 * @author shk1cob
 */
public class RteConfigGeneratorLogger {

  private static String mode = "";

  private static List<ILogEntry> logEntries;

  private static String ulfFile;

  private static ILogManager logManager = null;

  private static LoggerContext ctx = null;

  /**
   * @return the logEntries
   */
  public static List<ILogEntry> getLogEntries() {

    if (RteConfigGeneratorLogger.logEntries == null) {
      RteConfigGeneratorLogger.logEntries = new ArrayList<ILogEntry>();
    }
    return RteConfigGeneratorLogger.logEntries;
  }

  /**
   * @param logFile String
   */
  public void updateLog4jConfiguration(final String file, final String ulfFile1, final Map<String, String> modeprops) {
    Properties props = new Properties();

    try {

      mode = modeprops.get(RteConfigGeneratorConstants.RTE_CONFGEN_EXECUTION_MODE);
      ulfFile = ulfFile1;
      InputStream configStream = RteConfigGeneratorLogger.class.getResourceAsStream("log4j2.xml");

      ConfigurationSource source = new ConfigurationSource(configStream);
      Configurator.initialize(null, source);
      configStream.close();

      final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      final Configuration config = ctx.getConfiguration();

      PatternLayout layout =
          PatternLayout.newBuilder().withConfiguration(config).withPattern("[%d{HH:mm:ss:SSS}] - %-5p - %m%n").build();

      LoggerConfig loggerConfig = config.getLoggerConfig("com.bosch.tisched.rteconfig.generator");


      AppenderRef a[] = {};
      RewriteAppender createAppender = RewriteAppender.createAppender("RteConfGenRewriteAppender", "false", a, config,
          new RteConfigGeneratorReWritePolicy(ulfFile, mode, getLogEntries()), null);

      createAppender.start();
      config.addAppender(createAppender);
      loggerConfig.addAppender(createAppender, Level.TRACE, null);

      RollingFileAppender appender = RollingFileAppender.newBuilder().setConfiguration(config)
          .withName("com.bosch.tisched.rteconfig.generator").withLayout(layout).withFileName(file)
          .withFilePattern("Zuzi").withPolicy(SizeBasedTriggeringPolicy.createPolicy("5MB")).build();
      appender.start();


      config.addAppender(appender);
      loggerConfig = config.getLoggerConfig("com.bosch.tisched.rteconfig.generator.logger");
      loggerConfig.addAppender(appender, null, null);
    }
    catch (IOException e) {
      System.out.println(e.getLocalizedMessage());
    }
  }

  /**
   * it creates and returns the logger
   *
   * @param name of the class
   * @return Logger
   */
  public static Logger getLogger(final String name) {
    Logger logger = LogManager.getLogger(name);
    return logger;
  }


  /**
   * @param string
   */
  public static void logErrormessage(final Logger LOGGER, final String message) {

    LOGGER.error(message);

    System.exit(1);

  }

  /**
   * @param logger
   * @param localizedMessage
   * @param e
   */
  public static void logErrormessage(final Logger LOGGER, final String message, final Exception e) {

    LOGGER.error(message, e);

    System.exit(1);

  }
}
