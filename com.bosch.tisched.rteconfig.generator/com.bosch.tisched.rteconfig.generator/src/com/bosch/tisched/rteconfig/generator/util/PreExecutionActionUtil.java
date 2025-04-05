/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * @author DTB1KOR
 */
public class PreExecutionActionUtil {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(PreExecutionActionUtil.class.getName());
  private final Map<String, String> props;
  private final String workspace;
  private final String temprteconfgen;
  private final String temprteconfgenmemmap;
  private final String prjPath;
  private final String rteConfGenDir;
  private final List<String> possibleOutputList = new ArrayList<String>();


  /**
   * @param props
   * @param prjPath
   */
  public PreExecutionActionUtil(final Map<String, String> props, final String prjPath) {
    this.props = props;
    this.prjPath = prjPath;
    this.workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
    this.temprteconfgen = this.workspace + "\\_temp\\rteconfgen";
    this.temprteconfgenmemmap = this.workspace + "\\_temp\\rteconfgen_memmap";
    this.rteConfGenDir = props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR);
    generatePossibleToolOutputs();
  }

  /**
  *
  */
  private void generatePossibleToolOutputs() {
    List<String> tempList = new ArrayList<String>();
    tempList.addAll(Arrays.asList(this.prjPath + this.props.get(RteConfigGeneratorConstants.RTE_ECUC_VALUE_OUTPUT_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_TASK_RECURRENCE_FILE_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_FILES),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FILE_OUTPUT_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_VIEW_FILE_OUTPUT_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.LIST_OF_ECU_EXTRACT_FILES),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.RTE_RIPS_CSXFRM_ECUC_VALUE_OUTPUT_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.RTE_TIMED_ECUC_VALUE_OUTPUT_PATH),
        this.prjPath + this.rteConfGenDir + "/RteConfGen_MemMap.ht",
        this.prjPath + this.rteConfGenDir + "/rteconfgen_rteopts.txt"));
    this.possibleOutputList.addAll(tempList.stream().map(f -> f.replace("\\", "/")).collect(Collectors.toList()));

  }


  /**
   * @return the temprteconfgen
   */
  public String getTemprteconfgen() {
    return this.temprteconfgen;
  }


  /**
   * @return the temprteconfgenmemmap
   */
  public String getTemprteconfgenmemmap() {
    return this.temprteconfgenmemmap;
  }


  /**
   * @return
   */
  public Map<String, List<String>> getExpectedOutputMap() {
    Map<String, List<String>> outputmap = new HashMap<>();
    List<String> outputList = new ArrayList<String>();
    Collections.addAll(outputList,
        this.prjPath + this.props.get(RteConfigGeneratorConstants.RTE_ECUC_VALUE_OUTPUT_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_TASK_RECURRENCE_FILE_PATH),
        this.prjPath + this.props.get(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_FILES),
        this.prjPath + this.rteConfGenDir + "/rteconfgen_rteopts.txt");


    outputmap.put("EcuC Values", new ArrayList<>(outputList));
    outputList.clear();
    if (this.props.get(RteConfigGeneratorConstants.MODE).equals(RteConfigGeneratorConstants.UPDATE_ECU_EXTRACT_FILES)) {
      Collections.addAll(outputList,
          this.prjPath + this.props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FILE_OUTPUT_PATH),
          this.prjPath + this.props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH),
          this.prjPath + this.props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_VIEW_FILE_OUTPUT_PATH),
          this.prjPath + this.props.get(RteConfigGeneratorConstants.LIST_OF_ECU_EXTRACT_FILES));
      outputmap.put("Ecu Extract", new ArrayList<>(outputList));
      outputList.clear();
    }

    if (this.props.get(RteConfigGeneratorConstants.MEMMAP_HEADERFILE_GENERATION)
        .equals(RteConfigGeneratorConstants.ENABLE_MEMMAP_HEADERFILE_GENERATION)) {
      Collections.addAll(outputList,
          this.prjPath + this.props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR),
          this.prjPath + this.props.get(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_MEMMAP_HEADER_FILES),
          this.prjPath + this.rteConfGenDir + "//RteConfGen_MemMap.ht");
      outputmap.put("Memmap Header", new ArrayList<>(outputList));
      outputList.clear();
    }
    outputmap.entrySet().stream().forEach(l -> {
      List<String> newlist = l.getValue().stream().map(f -> f.replace("\\", "/")).collect(Collectors.toList());
      outputmap.put(l.getKey(), newlist);
    });
    return outputmap;
  }


  /**
   * @return
   * @throws Exception
   */
  public boolean createTempOutputDirs(final List<String> paths) throws Exception {
    boolean bpathcreated = false;
    for (String path : paths) {
      Path rteconfgendirs = null;

      try {

        if (new File(path).exists()) {
          FileUtils.cleanDirectory(new File(path));
        }
        else {
          rteconfgendirs = Paths.get(path);
          Files.createDirectories(rteconfgendirs);
        }
      }
      catch (IOException e) {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("270_0"));
        return false;
      }
      bpathcreated = true;
    }
    return bpathcreated;
  }


  /**
   * @return
   * @throws Exception
   */
  public boolean copyPreviousOutputs() throws Exception {
    try {
      FileUtils.copyDirectory(new File(this.prjPath + this.props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR)),
          new File(this.temprteconfgen));

      if (this.props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR) != null) {
        FileUtils.copyDirectory(
            new File(this.prjPath + this.props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR)),
            new File(this.temprteconfgenmemmap));
      }
    }
    catch (IOException e) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("270_1"));
      return false;
    }
    return true;

  }

  /**
   * @param prevCRCMap
   * @return
   */
  public List<String> getListOfExistingOutput(final Map<String, CRC32> prevCRCMap) {
    List<String> outputFileList = new ArrayList<String>();
    this.possibleOutputList.stream().forEach(path -> {
      if (new File(path).exists()) {
        outputFileList.add(path);
        try {
          updateCRC(path, prevCRCMap);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    Collections.sort(outputFileList);
    return outputFileList;
  }

  /**
   * @param filepath
   * @param prevCRCMap
   * @throws IOException
   */
  private void updateCRC(final String filepath, final Map<String, CRC32> prevCRCMap) throws IOException {
    Path path = Paths.get(filepath);
    CRC32 crc = new CRC32();
    byte[] arr = Files.readAllBytes(path);
    crc.update(arr, 0, arr.length);
    prevCRCMap.put(filepath, crc);

  }


}
