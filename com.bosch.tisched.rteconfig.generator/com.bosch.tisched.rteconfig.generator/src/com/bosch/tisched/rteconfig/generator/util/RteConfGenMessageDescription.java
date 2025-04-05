/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.Logger;

import com.bosch.tisched.rteconfig.generator.RteConfigGenerator;

/**
 * @author HRT7KOR
 */
public class RteConfGenMessageDescription {

  private static Logger LOGGER;

  public RteConfGenMessageDescription() throws IOException {
    loadFormattedMesssage();
  }

  public static final String PATH = "/lib/Error_Codes_Formatted.csv";
  public static Map<String, HashMap> messageDescription;

  private Map<String, HashMap> loadFormattedMesssage() throws IOException {
    messageDescription = new HashMap<String, HashMap>();

    InputStream csvstream = RteConfigGeneratorLogger.class.getResourceAsStream("Error_Codes_Formatted.csv");

    try {
      InputStreamReader lineReader = new InputStreamReader(csvstream);
      CSVParser records =
          CSVParser.parse(lineReader, CSVFormat.EXCEL.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

      for (CSVRecord record : records) {
        HashMap<String, String> argMap = null;
        argMap = (HashMap<String, String>) record.toMap();
        messageDescription.put(record.get(0), argMap);
      }
      return messageDescription;

    }
    catch (Exception e) {
      e.printStackTrace();
      return messageDescription;
      // TODO: handle exception
    }
  }


  public static String getFormattedMesssage(final String errId, final String... values) throws Exception {
    LOGGER = RteConfigGeneratorLogger.getLogger(RteConfigGenerator.class.getName());
    HashMap<String, String> val = messageDescription.get(errId);
    String formattedMessage = "";
    if (val != null) {

      String message = val.get("Message_Format");
      if (!message.isEmpty()) {
        String start = "\\$<";
        String messageData[] = message.split(start);
        formattedMessage = val.get("Error_ID") + " : ";
        if (messageData.length > 1) {
          int count = 0;
          if ((messageData.length - 1) == values.length) {
            for (int i = 0; i <= (messageData.length - 1); i++) {
              if (messageData[i].indexOf(">") >= 0) {
                String updatedMessage =
                    values[count] + messageData[i].substring(messageData[i].indexOf(">") + 1, messageData[i].length());
                formattedMessage = formattedMessage + updatedMessage;
                count++;
              }
              else {
                formattedMessage = formattedMessage + messageData[i];
              }
            }
          }
          else {
            RteConfigGeneratorLogger.logErrormessage(LOGGER,
                "MM_DCS_RTECONFGEN_401 : *** Logger Formatted Message is missing with value with Eorror_Id :" + errId);
          }
        }
        else {
          formattedMessage = message;
        }
      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            "MM_DCS_RTECONFGEN_401 : *** Logger Formatted Message is missing with Eorror_Id :" + errId);
      }
    }
    else {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          "MM_DCS_RTECONFGEN_401 : *** Logger Details is missing with Eorror_Id :" + errId);
    }
    return formattedMessage;
  }
}
