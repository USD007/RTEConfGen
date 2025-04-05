/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * @author DTB1KOR
 */
public class PostExecutionActionUtil {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(PostExecutionActionUtil.class.getName());


  /**
   * @param preexaction
   * @param appArgs
   * @param props
   */
  public PostExecutionActionUtil() {


  }


  /**
   * @param preexaction
   * @return
   * @throws Exception
   */
  public static boolean deleteTempOutputDirs(final PreExecutionActionUtil preexaction) throws Exception {
    try {
      if (new File(preexaction.getTemprteconfgen()).exists()) {
        File parentFile = new File(preexaction.getTemprteconfgen()).getParentFile();
        FileUtils.deleteDirectory(parentFile);
      }
    }
    catch (IOException e) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("270_4"));
      return false;
    }
    return true;
  }

  /**
   * @param preexaction
   * @param entry
   * @param curCRCMap
   * @param prevCRCMap
   * @throws Exception
   */
  public void compareAndReplaceMandatoryArxmlFiles(final PreExecutionActionUtil preexaction,
      final Entry<String, List<String>> entry, final Map<String, CRC32> prevCRCMap, final Map<String, CRC32> curCRCMap)
      throws Exception {
    List<String> fileList = entry.getValue();
    boolean bfilechanged = true;
    for (String file : fileList) {

      if (file.endsWith(".arxml")) {
        bfilechanged = compareAndReplaceArxmlfiles(preexaction, file, prevCRCMap, curCRCMap);
      }
    }
    for (String file : fileList) {
      if (file.endsWith(".lst") && !bfilechanged) {
        String filename = new File(file).getName();
        String tempfile = preexaction.getTemprteconfgen() + "\\" + filename;
        FileUtils.copyFile(new File(tempfile), new File(file));
      }
    }
  }

  /**
   * @param tempfilepath
   * @param finalfilepath
   * @return
   * @throws Exception
   */
  private static boolean compareARXMLFiles(final String tempfilepath, final String finalfilepath) throws Exception {

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      dbf.setCoalescing(true);
      dbf.setIgnoringElementContentWhitespace(true);
      dbf.setIgnoringComments(true);
      DocumentBuilder db = dbf.newDocumentBuilder();

      Document prevfile = db.parse(new File(finalfilepath));
      prevfile.normalizeDocument();

      Document currentfile = db.parse(new File(tempfilepath));
      currentfile.normalizeDocument();

      if (prevfile.isEqualNode(currentfile)) {
        return true;
      }
    }
    catch (Exception e) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("270_2", new File(finalfilepath).getName()));

    }
    return false;

  }

  /**
   * @param preexaction
   * @param entry
   * @param curCRCMap
   * @param prevCRCMap
   * @param curCRCMap
   * @param prevCRCMap
   * @throws Exception
   */
  public void compareAndReplaceTextFiles(final PreExecutionActionUtil preexaction,
      final Entry<String, List<String>> entry, final Map<String, CRC32> prevCRCMap, final Map<String, CRC32> curCRCMap)
      throws Exception {
    List<String> fileList = entry.getValue();
    for (String file : fileList) {
      if (file.endsWith(".txt")) {
        String filename = new File(file).getName();
        String tempfile = preexaction.getTemprteconfgen() + "\\" + filename;
        compareAndReplaceTextOrHeader(new File(tempfile), new File(file), prevCRCMap, curCRCMap);
      }
    }
  }

  /**
   * @param tempfile
   * @param orgfile
   * @param curCRCMap
   * @param prevCRCMap
   * @param curCRCMap
   * @param prevCRCMap
   * @return
   * @throws Exception
   */
  private boolean compareAndReplaceTextOrHeader(final File tempfile, final File orgfile,
      final Map<String, CRC32> prevCRCMap, final Map<String, CRC32> curCRCMap)
      throws Exception {
    boolean bSameFile = true;
    if (tempfile.exists() && orgfile.exists()) {
      if ((prevCRCMap.get(orgfile.getAbsolutePath()) != null) && (curCRCMap.get(orgfile.getAbsolutePath()) != null) &&
          (prevCRCMap.get(orgfile.getAbsolutePath()).getValue() != curCRCMap.get(orgfile.getAbsolutePath())
              .getValue())) {
        return false;
      }

      try {
        BufferedReader tempreader = new BufferedReader(new FileReader(tempfile));
        BufferedReader originalreader = new BufferedReader(new FileReader(orgfile));

        String line1 = null;
        String line2 = null;
        while (bSameFile && ((line1 = tempreader.readLine()) != null) &&
            ((line2 = originalreader.readLine()) != null)) {
          if (!line1.equals(line2)) {
            bSameFile = false;
            break;
          }
        }
        tempreader.close();
        originalreader.close();
      }
      catch (FileNotFoundException e) {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("270_3", orgfile.getName()));
        e.printStackTrace();
      }
    }

    if (bSameFile) {
      FileUtils.copyFile(tempfile, orgfile);
      return true;
    }
    return false;

  }

  /**
   * @param preexaction
   * @param entry
   * @param curCRCMap
   * @param prevCRCMap
   * @throws Exception
   */
  public void compareAndReplaceMemmapHeaders(final PreExecutionActionUtil preexaction,
      final Entry<String, List<String>> entry, final Map<String, CRC32> prevCRCMap, final Map<String, CRC32> curCRCMap)
      throws Exception {
    String temprteconfgenmemmap = preexaction.getTemprteconfgenmemmap();
    boolean bReplaced = false;
    if (!entry.getValue().isEmpty()) {
      String originalmemmapdir = entry.getValue().get(0);
      File orgdir = new File(originalmemmapdir);
      File tempdir = new File(temprteconfgenmemmap);
      List<File> tempfiles = new ArrayList<File>();
      Collections.addAll(tempfiles, tempdir.listFiles());
      if ((orgdir.listFiles().length != 0) && !tempfiles.isEmpty() && (orgdir.listFiles().length == tempfiles.size())) {
        for (File file : orgdir.listFiles()) {
          List<File> collect =
              tempfiles.stream().filter(f -> f.getName().equals(file.getName())).collect(Collectors.toList());
          if (!collect.isEmpty() && compareAndReplaceTextOrHeader(collect.get(0), file, prevCRCMap, curCRCMap)) {
            bReplaced = true;
          }
        }
      }

      if (bReplaced) {
        for (String file : entry.getValue()) {
          if (new File(file).isFile()) {
            String filename = new File(file).getName();
            String tempfile = preexaction.getTemprteconfgen() + "\\" + filename;
            FileUtils.copyFile(new File(tempfile), new File(file));
          }
        }
      }
    }
  }

  /**
   * @param preexaction
   * @param props
   * @param prjPath
   * @param curCRCMap
   * @param prevCRCMap
   * @throws Exception
   */
  public void compareAndReplaceOptionalFiles(final PreExecutionActionUtil preexaction, final Map<String, String> props,
      final String prjPath, final Map<String, CRC32> prevCRCMap, final Map<String, CRC32> curCRCMap)
      throws Exception {
    String csxfrmFilePath = prjPath + props.get(RteConfigGeneratorConstants.RTE_RIPS_CSXFRM_ECUC_VALUE_OUTPUT_PATH);
    String timedFilePath = prjPath + props.get(RteConfigGeneratorConstants.RTE_TIMED_ECUC_VALUE_OUTPUT_PATH);
    compareAndReplaceArxmlfiles(preexaction, csxfrmFilePath, prevCRCMap, curCRCMap);
    compareAndReplaceArxmlfiles(preexaction, timedFilePath, prevCRCMap, curCRCMap);
  }

  /**
   * @param preexaction
   * @param filepath
   * @param curCRCMap
   * @param prevCRCMap
   * @throws Exception
   */
  private boolean compareAndReplaceArxmlfiles(final PreExecutionActionUtil preexaction, final String filepath,
      final Map<String, CRC32> prevCRCMap, final Map<String, CRC32> curCRCMap)
      throws Exception {
    File file = new File(filepath);
    if (file.exists()) {
      if ((prevCRCMap.get(filepath) != null) && (curCRCMap.get(filepath) != null) &&
          (prevCRCMap.get(filepath).getValue() != curCRCMap.get(filepath).getValue())) {
        return true;
      }

      String tempfilePath = preexaction.getTemprteconfgen() + "\\" + file.getName();
      File tempFile = new File(tempfilePath);
      if (tempFile.exists()) {
        if (compareARXMLFiles(tempfilePath, filepath)) {
          FileUtils.copyFile(tempFile, file);
          return false;
        }
      }

    }
    return true;
  }


}
