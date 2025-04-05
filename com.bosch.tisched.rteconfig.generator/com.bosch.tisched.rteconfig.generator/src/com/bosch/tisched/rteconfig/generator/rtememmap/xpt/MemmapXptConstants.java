/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.rtememmap.xpt;

import java.util.regex.Pattern;

/**
 * @author DTB1KOR
 */
public class MemmapXptConstants {


  public static final String CODE = "CODE";
  public static final String VARINIT = "VAR_INIT";
  public static final String VARINT8 = "VAR_INT8";
  public static final String VARCLEARED = "VAR_CLEARED";
  public static final String CONST = "CONST";
  public static final String CALIB = "CALIB";
  public static final String VARSAVEDZONE = "VAR_SAVED_ZONE";
  public static final String NVVARPWRONCLEARED = "NV_VAR_POWER_ON_CLEARED";
  public static final String NVVARSAVEDZONE = "NV_VAR_SAVED_ZONE";
  public static final String VARPOWERONCLEARED = "VAR_POWER_ON_CLEARED";

  public static final String SN_OSAPP = "SN_OSAPP";
  public static final String SN_OSAPP_MEMSEC = "SN_OSAPP_MEMSEC";
  public static final String SN_DRE = "SN_DRE";
  public static final String SN_SWCP = "SN_SWCP";
  public static final String SN_SWC = "SN_SWC";
  public static final String SN_SWC_MEMSEC = "SN_SWC_MEMSEC";
  public static final String SN_ECUCP = "SN_ECUCP";
  public static final String SN_FID_SWCP = "SN_FID_SWCP";
  public static final String SN_TASK = "SN_TASK";
  public static final String SN_NVSWCP = "SN_NVSWCP";
  public static final String SN_NVSWC = "SN_NVSWC";
  public static final String SN_NVBD = "SN_NVBD";
  public static final String SN_BSWMD = "SN_BSWMD";
  public static final String SN_BSWMD_MEMSEC = "SN_BSWMD_MEMSEC";
  public static final String SN_OIE = "SN_OIE";
  public static final String SN_FID_CLIENT = "SN_FID_CLIENT";
  public static final String SN_RPORT_PROTOTYPE = "SN_RPORT_PROTOTYPE";
  public static final String SN_CS_OPERATION = "SN_CS_OPERATION";
  /**
   *
   */
  public static final Pattern KEYWORD_PATTERN = Pattern.compile("\\$\\<[A-Z0-9_]*\\>");
  /**
   *
   */
  public static final Pattern OPTIONAL_KEYWORD_PATTERN = Pattern.compile("\\[[a-zA-Z0-9_\\$\\<\\>\\s\\t\"\\n\\\\]*\\]");

  /**
   *
   */
  public static final Pattern TASK_WISE_SWP_PATTERN = Pattern
      .compile("SW_COMPONENT_PROTOTYPES:(\\s)*[a-zA-Z_][A-Z0-9_a-z]*((\\s)*,(\\s)*[a-zA-Z_][a-zA-Z0-9_]*(\\s)*)*");

  /**
   *
   */
  public static final String cPFMSN = "configPragmaForMemSectionName";
  /**
   *
   */
  public static final String sSMP = "setStartMemmapPragma";
  /**
   *
   */
  public static final String sEMP = "setEndMemmapPragma";
  // 2.2.1
  /**
   *
   */
  static final String MEMSEC221 = ".*_$<SN_TASK>_CODE";
  /**
   *
   */
  public static final String OSTASKPERAPPLICATIONS = "«" + cPFMSN + "(\"" + MEMSEC221 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC221 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";
  /**
   *
   */
  public static final String OSTASKNOTPERAPPLICATION = "«" + cPFMSN + "(\"" + MEMSEC221 + "\",\"Rte\")." + sSMP +
      "(\"#define OS_START_SEC_CODE \\n  #include \\\"Os_MemMap.h\\\" \")»" + System.lineSeparator() + "«" + cPFMSN +
      "(\"" + MEMSEC221 + "\",\"Rte\")." + sEMP + "(\"#define OS_STOP_SEC_CODE \\n #include \\\"Os_MemMap.h\\\" \")»";
  // 2.2.2
  /**
   *
   */
  static final String MEMSEC222 = ".*VAR_IMPLICITSR_$<SN_TASK>";
  /**
   *
   */
  public static final String IMPLICITBUFFEROFTASKS = "«" + cPFMSN + "(\"" + MEMSEC222 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC222 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";
  /**
   *
   */
  // 2.2.3
  static final String MEMSEC223 = ".*$<SN_TASK>_VAR_CLEARED";
  /**
   *
   */
  public static final String OSTASKVARCLEARED = "«" + cPFMSN + "(\"" + MEMSEC223 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC223 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  /**
   *
   */
  // 2.3.1
  static final String MEMSEC231 = "(?i:R?T?E?_?$<SN_FID_SWCP>_$<SN_TASK>)_VAR_INIT";
  /**
   *
   */
  public static final String INITVARTASK = "«" + cPFMSN + "(\"" + MEMSEC231 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2> \\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC231 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  // 2.3.2
  static final String MEMSEC232 = "(?i:R?T?E?_?$<SN_FID_SWCP>_$<SN_TASK>)_VAR_CLEARED";
  /**
   *
   */
  public static final String CLEARVARTASK = "«" + cPFMSN + "(\"" + MEMSEC232 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC232 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC233 = "(?i:R?T?E?_?$<SN_FID_SWCP>_$<SN_TASK>)_VAR_POWER_ON_CLEARED";
  /**
   *
   */
  public static final String POWERONCLEARVARTASK = "«" + cPFMSN + "(\"" + MEMSEC232 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC233 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC234 = "(?i:R?T?E?_?$<SN_FID_SWCP>_$<SN_TASK>)_VAR_SAVED_ZONE";
  /**
   *
   */
  public static final String VARSAVEDZONETASK = "«" + cPFMSN + "(\"" + MEMSEC234 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC234 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
   *
   */
  // 2.4.1
  static final String MEMSEC241 = "^[^_]*_$<SN_SWC_MEMSEC>_CODE";
  /**
   *
   */
  public static final String NOSPLITSWCCODE = "«" + cPFMSN + "(\"" + MEMSEC241 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC241 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  /**
  *
  */
  public static final String NOSPLITSWCCODESWC = "«" + cPFMSN + "(\"" + MEMSEC241 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC241 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";
  /**
   *
   */
  // 2.4.2
  static final String MEMSEC242 = "^[^_]*_$<SN_SWC_MEMSEC>_VAR_INIT";

  /**
   *
   */
  public static final String NOSPLITVARINT8 = "«" + cPFMSN + "(\"" + MEMSEC242 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC242 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";
  /**
   *
   */
  public static final String NOSPLITVARINT8SWC = "«" + cPFMSN + "(\"" + MEMSEC242 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC242 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  // 2.4.3
  static final String MEMSEC243 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_CALIB";
  /**
   *
   */
  public static final String NOSPLITSWCCALIB = "«" + cPFMSN + "(\"" + MEMSEC243 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";
  /**
  *
  */
  public static final String NOSPLITSWCCALIBSWC = "«" + cPFMSN + "(\"" + MEMSEC243 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC243 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  // 2.4.4
  static final String MEMSEC244 = ".*_AF_$<SN_FID_SWCP>_$<SN_DRE>_VAR_CLEARED_8";
  /**
   *
   */
  public static final String NOSPLITSWCDATARECV = "«" + cPFMSN + "(\"" + MEMSEC244 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC244 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";
  /**
  *
  */
  public static final String NOSPLITSWCDATARECVSWC = "«" + cPFMSN + "(\"" + MEMSEC244 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC244 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  // 2.4.5
  static final String MEMSEC245 = "^[^_]*_$<SN_FID_SWCP>_CALIB_ASIL_?0?";

  /**
   *
   */
  public static final String NOSPLITSWCCALIBASIL = "«" + cPFMSN + "(\"" + MEMSEC245 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_BOOLEA" + MEMSEC242 + "\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
   *
   */
  public static final String NOSPLITSWCCALIBASILSWC = "«" + cPFMSN + "(\"" + MEMSEC245 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC245 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  // 2.4.4.1
  /**
   *
   */
  static final String MEMSEC2461 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_VAR_INIT_?0?";
  /**
   *
   */
  public static final String NOSPLITSWINITDATA = "«" + cPFMSN + "(\"" + MEMSEC2461 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";
  /**
  *
  */
  public static final String NOSPLITSWINITDATASWC = "«" + cPFMSN + "(\"" + MEMSEC2461 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2> \\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2461 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  // 2.4.4.2
  static final String MEMSEC2462 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_VAR_CLEARED_?0?";
  /**
   *
   */
  public static final String NOSPLITSWVARCLR = "«" + cPFMSN + "(\"" + MEMSEC2462 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
  *
  */
  public static final String NOSPLITSWVARCLRSWC = "«" + cPFMSN + "(\"" + MEMSEC2462 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2462 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2463 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_VAR_POWER_ON_CLEARED_?0?";
  /**
   *
   */
  public static final String NOSPLITSWVARCLRPOWERON = "«" + cPFMSN + "(\"" + MEMSEC2463 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
  *
  */
  public static final String NOSPLITSWVARCLRSWCPOWERON = "«" + cPFMSN + "(\"" + MEMSEC2463 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2463 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2464 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_VAR_SAVED_ZONE_?0?";
  /**
   *
   */
  public static final String NOSPLITSWVARSAVEDZONE = "«" + cPFMSN + "(\"" + MEMSEC2464 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
   *
   */
  public static final String NOSPLITSWVARSAVEDZONESWC = "«" + cPFMSN + "(\"" + MEMSEC2464 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2464 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  // 2.4.5.1
  /**
  *
  */
  static final String MEMSEC2471 = "(?i:R?T?E?_?$<SN_FID_SWCP>)(?i:_$<OS_TASK_NAME_PATTERN>)??_VAR_INIT_?0?";
  /**
  *
  */
  public static final String NOSPLITNOTASKSWINITDATA = "«" + cPFMSN + "(\"" + MEMSEC2471 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
  *
  */
  public static final String NOSPLITNOTASKSWINITDATASWC = "«" + cPFMSN + "(\"" + MEMSEC2471 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2> \\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2471 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  // 2.4.5.2
  static final String MEMSEC2472 = "(?i:R?T?E?_?$<SN_FID_SWCP>)(?i:_$<OS_TASK_NAME_PATTERN>)??_VAR_CLEARED_?0?";
  /**
  *
  */
  public static final String NOSPLITNOTASKSWVARCLR = "«" + cPFMSN + "(\"" + MEMSEC2472 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
  *
  */
  public static final String NOSPLITNOTASKSWVARCLRSWC = "«" + cPFMSN + "(\"" + MEMSEC2472 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2472 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2473 =
      "(?i:R?T?E?_?$<SN_FID_SWCP>)(?i:_$<OS_TASK_NAME_PATTERN>)??_VAR_POWER_ON_CLEARED_?0?";

  /**
  *
  */
  public static final String NOSPLITNOTASKSWVARCLRPWRON = "«" + cPFMSN + "(\"" + MEMSEC2473 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
  *
  */
  public static final String NOSPLITNOTASKSWVARCLRSWCPWRON = "«" + cPFMSN + "(\"" + MEMSEC2473 +
      "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2473 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2474 = "(?i:R?T?E?_?$<SN_FID_SWCP>)(?i:_$<OS_TASK_NAME_PATTERN>)??_VAR_SAVED_ZONE_?0?";
  /**
   *
   */
  public static final String NOSPLITNOTASKSWVARSAVEDZONE = "«" + cPFMSN + "(\"" + MEMSEC2474 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
  *
  */
  public static final String NOSPLITNOTASKSWVARSAVEDZONESWC = "«" + cPFMSN + "(\"" + MEMSEC2474 +
      "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2474 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


//2.5.1
  static final String MEMSEC251 = "^[^_]*_$<SN_SWC_MEMSEC>_CODE";
  /**
   *
   */
  public static final String SPLITSWCCODE = "«" + cPFMSN + "(\"" + MEMSEC251 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC251 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

// 2.5.2
  static final String MEMSEC252 = "^[^_]*_$<SN_SWC>_VAR_INIT";

  /**
   *
   */
  public static final String SPLITVARINT8 = "«" + cPFMSN + "(\"" + MEMSEC252 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC252 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "";

// 2.5.3
  static final String MEMSEC253 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_CALIB";
  /**
   *
   */
  public static final String SPLITSWCCALIB = "«" + cPFMSN + "(\"" + MEMSEC253 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC253 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  /**
   *
   */

// 2.5.4
  static final String MEMSEC254 = ".*_AF_$<SN_FID_SWCP>_$<SN_DRE>_VAR_CLEARED_8";
  /**
   *
   */
  public static final String SPLITSWCDATARECV = "«" + cPFMSN + "(\"" + MEMSEC254 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC254 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  // 2.5.5
  static final String MEMSEC255 = "^[^_]*_$<SN_FID_SWCP>_CALIB_ASIL_?0?";

  /**
   *
   */
  public static final String SPLITCALIBASIL = "«" + cPFMSN + "(\"" + MEMSEC255 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC255 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


// 2.5.6.1
  /**
   *
   */
  static final String MEMSEC2561 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_VAR_INIT_?0?";
  /**
   *
   */
  public static final String SPLITSWINITDATA = "«" + cPFMSN + "(\"" + MEMSEC2561 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2561 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

// 2.5.4.2
  static final String MEMSEC2562 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_VAR_CLEARED_?0?";
  /**
   *
   */
  public static final String SPLITSWVARCLR = "«" + cPFMSN + "(\"" + MEMSEC2562 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2562 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2563 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_VAR_POWER_ON_CLEARED_?0?";
  /**
   *
   */
  public static final String SPLITSWVARCLRPWRON = "«" + cPFMSN + "(\"" + MEMSEC2563 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2563 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2564 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_VAR_SAVED_ZONE_?0?";
  /**
  *
  */
  public static final String SPLITSWVARSAVEDZONESWC = "«" + cPFMSN + "(\"" + MEMSEC2564 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2564 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


//2.5.5.1
  /**
  *
  */
  static final String MEMSEC2571 = "(?i:R?T?E?_?$<SN_FID_SWCP>)(?i:_$<OS_TASK_NAME_PATTERN>)??_VAR_INIT_?0?";
  /**
  *
  */

  public static final String SPLITNOTASKSWINITDATA = "«" + cPFMSN + "(\"" + MEMSEC2571 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2571 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

// 2.5.5.2
  static final String MEMSEC2572 = "(?i:R?T?E?_?$<SN_FID_SWCP>)(?i:_$<OS_TASK_NAME_PATTERN>)??_VAR_CLEARED_?0?";
  /**
  *
  */
  public static final String SPLITNOTASKSWVARCLR = "«" + cPFMSN + "(\"" + MEMSEC2572 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2572 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2573 =
      "(?i:R?T?E?_?$<SN_FID_SWCP>)(?i:_$<OS_TASK_NAME_PATTERN>)??_VAR_POWER_ON_CLEARED_?0?";
  /**
  *
  */
  public static final String SPLITNOTASKSWVARCLRPWRON = "«" + cPFMSN + "(\"" + MEMSEC2573 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2573 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2574 = "(?i:R?T?E?_?$<SN_FID_SWCP>)(?i:_$<OS_TASK_NAME_PATTERN>)??_VAR_SAVED_ZONE _?0?";
  /**
  *
  */
  public static final String SPLITNOTASKSWVARSAVEDZONESWC = "«" + cPFMSN + "(\"" + MEMSEC2574 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\"$<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2574 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  // 2.6.1
  static final String MEMSEC261 = ".*AF_$<SN_SWCP>_$<SN_OIE>_VAR_INIT";
  /**
   *
   */
  public static final String NONSPLITSERVERACTIVATIONFLAG = "«" + cPFMSN + "(\"" + MEMSEC261 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC261 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  /**
   *
   */
  public static final String NONSPLITSERVERACTIVATIONFLAGSWC = "«" + cPFMSN + "(\"" + MEMSEC261 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC261 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  /**
   *
   */
  public static final String SPLITSERVERACTIVATIONFLAG = "«" + cPFMSN + "(\"" + MEMSEC261 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC261 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC262 =
      ".*_OSCBK_RTE_RECEIVERPULLCB_$<SN_FID_CLIENT>_$<SN_RPORT_PROTOTYPE>_$<SN_CS_OPERATION>_RTN_CODE";
  /**
   *
   */
  public static final String CLIENTOSCBKTASKPERAPPLNOSPLIT = "«" + cPFMSN + "(\"" + MEMSEC262 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC262 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";
  /**
  *
  */
  public static final String CLIENTOSCBKTASKPERAPPLNOSPLITSWC = "«" + cPFMSN + "(\"" + MEMSEC262 + "\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC262 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";
  /**
   *
   */
  public static final String CLIENTOSCBKTASKPERAPPLSPLIT = "«" + cPFMSN + "(\"" + MEMSEC262 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC262 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  // 2.6.3
  static final String MEMSEC263 =
      ".*_OSCBK_RTE_RECEIVERPULLCB_$<SN_FID_CLIENT>_$<SN_RPORT_PROTOTYPE>_$<SN_CS_OPERATION>_CODE";
  /**
   *
   */
  public static final String SERVEROSCBKTASKPERAPPLNOSPLIT = "«" + cPFMSN + "(\"" + MEMSEC263 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC263 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  /**
  *
  */
  public static final String SERVEROSCBKTASKPERAPPLNOSPLITSWC = "«" + cPFMSN + "(\"" + MEMSEC263 + "\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC263 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";
  /**
   *
   */
  public static final String SERVEROSCBKTASKPERAPPLSPLIT = "«" + cPFMSN + "(\"" + MEMSEC263 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC263 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  // 2.6.4
  static final String MEMSEC264 = ".*_OSCBK_RTE_.*_CODE";
  /**
   *
   */
  public static final String CLIENTANDSERVEROSAPPNOTPERAPPLICATION = "«" + cPFMSN + "(\"" + MEMSEC264 + "\",\"Rte\")." +
      sSMP + "(\"#define OS_START_SEC_CALLOUT_CODE\\n #include \\\"Os_MemMap.h\\\" \")»" + System.lineSeparator() +
      "«" + cPFMSN + "(\"" + MEMSEC264 + "\",\"Rte\")." + sEMP +
      "(\"#define OS_STOP_SEC_CALLOUT_CODE \\n #include \\\"Os_MemMap.h\\\" \")»";


  static final String MEMSEC271 = ".*$<SN_OSAPP_MEMSEC>_VAR_CLEARED_16";
  /**
   *
   */
  public static final String RESOURCEVARIABLES = "«" + cPFMSN + "(\"" + MEMSEC271 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC271 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC272 = "(?i:.*$<SN_ECUCP>)_CONST";

  /**
   *
   */
  public static final String CONSTOBJECTS = "«" + cPFMSN + "(\"" + MEMSEC272 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC272 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC273 = "RTE_(?i)$<SN_ECUCP>(?-i)_?C?P?T?_([a-zA-Z0-9_]+)_CONST_?0?";
  /**
   *
   */
  public static final String CONSTOBJECTSNOSWC = "«" + cPFMSN + "(\"" + MEMSEC273 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";
  /**
   *
   */
  public static final String CONSTOBJECTSSWC = "«" + cPFMSN + "(\"" + MEMSEC273 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP> $<SW_COMPONENT>$1$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC273 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  static final String MEMSEC274 = "(?i:.*$<SN_ECUCP>)_VAR_INIT";

  /**
   *
   */
  public static final String VARINITOBJECTS = "«" + cPFMSN + "(\"" + MEMSEC274 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC274 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  static final String MEMSEC275 = "RTE_(?i)$<SN_ECUCP>)(?-i)_?C?P?T?_([a-zA-Z0-9_]+)_VAR_INIT_?0?";
  /**
  *
  */
  public static final String VARINITOBJECTSNOSWC = "«" + cPFMSN + "(\"" + MEMSEC275 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2> \\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
  *
  */
  public static final String VARINITOBJECTSSWC = "«" + cPFMSN + "(\"" + MEMSEC275 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_2> \\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC275 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC276 = "(?i:.*$<SN_ECUCP>)_VAR_CLEARED";

  /**
   *
   */
  public static final String VARCLROBJECTS = "«" + cPFMSN + "(\"" + MEMSEC276 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC276 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  static final String MEMSEC277 = "RTE_(?i)$<SN_ECUCP>)(?-i)_?C?P?T?_([a-zA-Z0-9_]+)_VAR_CLEARED_?0?";

  /**
   *
   */
  public static final String VARCLROBJECTSNOSWC = "«" + cPFMSN + "(\"" + MEMSEC277 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  /**
   *
   */
  public static final String VARCLROBJECTSSWC = "«" + cPFMSN + "(\"" + MEMSEC277 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC277 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC278 = "(?i:.*$<SN_ECUCP>)_VAR_POWER_ON_CLEARED";
  /**
  *
  */
  public static final String VARPWRONCLROBJECTS = "«" + cPFMSN + "(\"" + MEMSEC278 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC278 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC279 = "RTE_(?i)$<SN_ECUCP>)(?-i)_?C?P?T?_([a-zA-Z0-9_]+)_VAR_POWER_ON_CLEARED_?0?";
  /**
  *
  */
  public static final String VARPWRONCLROBJECTSNOSWC = "«" + cPFMSN + "(\"" + MEMSEC279 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";
  /**
  *
  */
  public static final String VARPWRONCLROBJECTSSWC = "«" + cPFMSN + "(\"" + MEMSEC279 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_16\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC279 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2710 = "i:.*$<SN_ECUCP>)_VAR_SAVED_ZONE";
  /**
  *
  */
  public static final String VARSAVEDZONEOBJECT = "«" + cPFMSN + "(\"" + MEMSEC2710 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2710 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  static final String MEMSEC2711 = "RTE_(?i)$<SN_ECUCP>(?-i)_?C?P?T?_([a-zA-Z0-9_]+)_VAR_SAVED_ZONE_?0?";

  /**
  *
  */
  public static final String VARSAVEDZONEOBJECTNOSWC = "«" + cPFMSN + "(\"" + MEMSEC2711 + "_BOOLEAN\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";
  /**
  *
  */
  public static final String VARSAVEDZONEOBJECTSWC = "«" + cPFMSN + "(\"" + MEMSEC2711 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2711 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2712 = "(?i:.*$<SN_ECUCP>)_CALIB";
  /**
  *
  */
  public static final String CALIBOBJECT = "«" + cPFMSN + "(\"" + MEMSEC2712 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>t_fkw1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2712 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  static final String MEMSEC2713 = "RTE_(?i)$<SN_ECUCP>(?-i)_?C?P?T?_([a-zA-Z0-9_]+)_CALIB_?0?";
  /**
  *
  */
  public static final String CALIBOBJECTNOSWC = "«" + cPFMSN + "(\"" + MEMSEC2713 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";
  /**
  *
  */
  public static final String CALIBOBJECTSWC = "«" + cPFMSN + "(\"" + MEMSEC2713 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$1 $<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2713 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2714 = "(?i:.*$<SN_ECUCP>)";
  /**
   *
   */
  public static final String ECUCPARTITION8 = "«" + cPFMSN + "(\"" + MEMSEC2714 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2714 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";


  static final String MEMSEC281 = ".*$<SN_NVSWC>_RAM_CLEARED";

  /**
   *
   */
  public static final String NVRAMCLEAREDDATA = "«" + cPFMSN + "(\"" + MEMSEC281 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC281 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC282 = ".*$<SN_NVSWC>_ROM_CONST";

  /**
   *
   */
  public static final String NVROMCONST = "«" + cPFMSN + "(\"" + MEMSEC282 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC282 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC283 = ".*_AF_$<SN_NVSWCP>_$<SN_DRE>_VAR_CLEARED";

  /**
   *
   */
  public static final String NVDREACTIVEFLG = "«" + cPFMSN + "(\"" + MEMSEC283 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC283 + "_8\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC284 = ".*$<SN_NVSWC>_$<SN_NVBD>_CODE";
  /**
   *
   */
  public static final String NVBLOCKCODE = "«" + cPFMSN + "(\"" + MEMSEC284 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC284 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC285 = ".*$<SN_NVSWC>_VAR_SAVED_ZONE";
  /**
   *
   */
  public static final String NVBLOCKVARSAVEDZONE = "«" + cPFMSN + "(\"" + MEMSEC285 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC285 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC286 = ".*$<SN_NVSWC>_CONST_SAVED_RECOVERY_ZONE";
  /**
   *
   */
  public static final String NVBLOCKCONSTSAVEDRECZONE = "«" + cPFMSN + "(\"" + MEMSEC286 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC286 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC287 = ".*$<SN_NVSWC>_VAR_PORST_SAVED_ZONE";
  /**
   *
   */
  public static final String NVBLOCKVARPOSRTSAVEDZONE = "«" + cPFMSN + "(\"" + MEMSEC287 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC287 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC291 = "^[^_]*_$<SN_BSWMD_MEMSEC>_CODE";

  /**
   *
   */
  public static final String BSWMDCODE = "«" + cPFMSN + "(\"" + MEMSEC291 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<BSW_MODULE_DESCRIPTION>$<SN_BSWMD>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC291 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2101 = "(?i:R?T?E?_?$<SN_FID_SWCP>)_CALIB";

  /**
   *
   */
  public static final String PARAMSWCPCALIB = "«" + cPFMSN + "(\"" + MEMSEC2101 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  /**
   *
   */
  public static final String PARAMSWCPCALIBSWC = "«" + cPFMSN + "(\"" + MEMSEC2101 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>$<SN_OSAPP>$<SW_COMPONENT>$<SN_SWC>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2101 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2111 = "CODE";
  /**
   *
   */
  public static final String DEFAULTCODE = "«" + cPFMSN + "(\"" + MEMSEC2111 + "\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2111 + "\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2112 = ".*_MAIN_CODE";
  /**
   *
   */
  public static final String MAINCODE = "«" + cPFMSN + "(\"" + MEMSEC2112 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>_main$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2112 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2113 = ".*_EXT_CODE";
  /**
   *
   */
  public static final String EXTCODE = "«" + cPFMSN + "(\"" + MEMSEC2113 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>_ext$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2113 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2114 = ".*_SYS_CODE";
  /**
   *
   */
  public static final String SYSCODE = "«" + cPFMSN + "(\"" + MEMSEC2114 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>_sys$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2114 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2115 = ".*_SYS_VAR_CLEARED";
  /**
   *
   */
  public static final String SYSVARCLEARED = "«" + cPFMSN + "(\"" + MEMSEC2115 + "_32\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>_sys$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2115 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2116 = ".*_LIB_CODE";
  /**
   *
   */
  public static final String LIBCODE = "«" + cPFMSN + "(\"" + MEMSEC2116 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>_lib$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2116 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC2117 = ".*_RIPS_.*CODE";
  /**
   *
   */
  public static final String RIPSCODE =
      "«LET {\"Rte_Rips\",\"Rte_Rips_CSXfrm\",\"Rte_Rips_SRCom\"} AS listOfComponents»" + System.lineSeparator() + "«" +
          cPFMSN + "(\"" + MEMSEC2117 + "\",listOfComponents)." + sSMP +
          "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
          System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2117 + "\",listOfComponents)." + sEMP +
          "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«ENDLET»";

  static final String MEMSEC2118 = ".*_RIPS_.*CALLOUT_CODE";
  /**
   *
   */
  public static final String RIPSCALLOUTCODE =
      "«LET {\"Rte_Rips\",\"Rte_Rips_CSXfrm\",\"Rte_Rips_SRCom\"} AS listOfComponents»" + System.lineSeparator() + "«" +
          cPFMSN + "(\"" + MEMSEC2118 + "\",listOfComponents)." + sSMP +
          "(\"#define OS_START_SEC_CALLOUT_CODE \\n #include \\\"Os_MemMap.h\\\"\")»" + System.lineSeparator() + "«" +
          cPFMSN + "(\"" + MEMSEC2118 + "\",listOfComponents)." + sEMP +
          "(\"#define OS_STOP_SEC_CALLOUT_CODE \\n #include \\\"Os_MemMap.h\\\"\")»" + System.lineSeparator() +
          "«ENDLET»";

  static final String MEMSEC2119 = "VAR_INIT";
  /**
   *
   */
  public static final String INITDATA = "«" + cPFMSN + "(\"" + MEMSEC2119 + "_BOOLEAN\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<DEFAULT_INITIAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_BOOLEAN\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_8\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<DEFAULT_INITIAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_8\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_16\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<DEFAULT_INITIAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_16\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_32\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<DEFAULT_INITIAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_32\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_64\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<DEFAULT_INITIAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_64\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_UNSPECIFIED\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<DEFAULT_INITIAL_KEYWORD_2>\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC2119 + "_UNSPECIFIED\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  static final String MEMSEC21110 = "VAR_CLEARED";
  /**
   *
   */
  public static final String VARCLEAREDDATA = "«" + cPFMSN + "(\"" + MEMSEC21110 + "_BOOLEAN\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_BOOLEAN\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_8\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_8\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_16\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_16\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_32\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_32\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_64\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_64\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_UNSPECIFIED\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21110 + "_UNSPECIFIED\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC21111 = "CALIB";
  /**
   *
   */
  public static final String CALIBDATA = "«" + cPFMSN + "(\"" + MEMSEC21111 + "_BOOLEAN\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_BOOLEAN\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_8\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_8\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_16\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_16\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_32\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_32\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_64\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_64\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_UNSPECIFIED\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21111 + "_UNSPECIFIED\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC21112 = "CONST";
  /**
   *
   */
  public static final String CONSTDATA = "«" + cPFMSN + "(\"" + MEMSEC21112 + "_BOOLEAN\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_BOOLEAN\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_8\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_8\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_16\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_16\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_32\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_32\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_64\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_64\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_UNSPECIFIED\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<DEFAULT_INITIAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21112 + "_UNSPECIFIED\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC21113 = "R?T?E?_?CONST";
  /**
   *
   */
  public static final String RTECONSTDATA = "«" + cPFMSN + "(\"" + MEMSEC21113 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_64\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21113 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  static final String MEMSEC21114 = ".*_SIG_.*_CODE";
  /**
   *
   */
  public static final String RTECOMCALLBACKS = "«" + cPFMSN + "(\"" + MEMSEC21114 + "\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>_comcbk$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21114 + "\",\"Rte\")." + sEMP + "(\"$<PRAGMA_SECTION>\")»";

  // DELETED IN NEW REQUIREMENT
//  static final String MEMSEC21115 = "CPT_([a-zA-Z0-9_]+).*_VAR_SAVED_ZONE.*";
//  /**
//   *
//   */
//  public static final String RTEVARSAVEDZONE = "«" + cPFMSN + "(\"" + MEMSEC21115 + "_BOOLEAN\",\"Rte\")." + sSMP +
//      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
//      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_BOOLEAN\",\"Rte\")." + sEMP +
//      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_8\",\"Rte\")." +
//      sSMP +
//      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
//      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_8\",\"Rte\")." + sEMP +
//      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_16\",\"Rte\")." +
//      sSMP +
//      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
//      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_16\",\"Rte\")." + sEMP +
//      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_32\",\"Rte\")." +
//      sSMP +
//      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
//      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_32\",\"Rte\")." + sEMP +
//      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_64\",\"Rte\")." +
//      sSMP +
//      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
//      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_64\",\"Rte\")." + sEMP +
//      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 +
//      "_UNSPECIFIED\",\"Rte\")." + sSMP +
//      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<SW_COMPONENT>$1$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
//      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21115 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
//      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC21116 =
      "(?i:.*?ECUCPARTITION_OSAPP_([0-9]_[A-Z]_[0-9])).*_C?P?T?_?([a-zA-Z0-9]+)_VAR_CLEARED";

  /**
   *
   */
  public static final String VARCLRECUCANDSWC = "«" + cPFMSN + "(\"" + MEMSEC21116 + "_BOOLEAN\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>OsApp_$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_BOOLEAN\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_8\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>OsApp_$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>OsApp_$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_32\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>OsApp_$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_32BIT>a4\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_32\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_64\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>OsApp_$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_64BIT>a8\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_64 \",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 +
      "_UNSPECIFIED\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>OsApp_$1$<FINAL_KEYWORD_1>\\\" $<LINKER_FLAG_KEYWORD>\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21116 + "_UNSPECIFIED\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";

  static final String MEMSEC21117 = ".*OSCBK_RTE_RECEIVERPULLCB_RTE_RX_([0-9])*_.*_CODE";

  /**
   *
   */
  public static final String UNMAPDRE = "«" + cPFMSN + "(\"" + MEMSEC21117 + "\",\"Rte\")." + sSMP +
      "(\"#define OS_START_SEC_CALLOUT_CODE\\n #include \\\"Os_MemMap.h\\\" \")»" + System.lineSeparator() + "«" +
      cPFMSN + "(\"" + MEMSEC21117 + "\",\"Rte\")." + sEMP +
      "(\"#define OS_STOP_SEC_CALLOUT_CODE \\n #include \\\"Os_MemMap.h\\\" \")»";


  static final String MEMSEC21118 = ".*OSCBK_RTE_NULLALARMCALLBACK_CODE";

  /**
   *
   */
  public static final String NULLALARMCALLBACK = "«" + cPFMSN + "(\"" + MEMSEC21118 + "\",\"Rte\")." + sSMP +
      "(\"#define OS_START_SEC_CALLOUT_CODE \\n #include \\\"Os_MemMap.h\\\" \")»" + System.lineSeparator() + "«" +
      cPFMSN + "(\"" + MEMSEC21118 + "\",\"Rte\")." + sEMP +
      "(\"#define OS_STOP_SEC_CALLOUT_CODE \\n #include \\\"Os_MemMap.h\\\" \")»";

  /**
   *
   */
  public static final String GENERALSEC21119 = "«" + cPFMSN + "(\"RBA_SVBB_ADAPTIVECALIB_8\")." + sSMP + "(" +
      System.lineSeparator() + "\"#if defined(__ghs__)" + System.lineSeparator() +
      "  #pragma ghs section rodata=\\\".data.CalSup_CalAdaptive_a1\\\"" + System.lineSeparator() + "#else" +
      System.lineSeparator() + "  #pragma section  \\\".bss.CalSup_CalAdaptive_a1\\\" awB" + System.lineSeparator() +
      "#endif\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"RBA_SVBB_ADAPTIVECALIB_8\")." + sEMP + "(" +
      System.lineSeparator() + "\"#if defined(__ghs__)" + System.lineSeparator() +
      "  #pragma ghs section rodata=default" + System.lineSeparator() + "#else" + System.lineSeparator() +
      "  #pragma section" + System.lineSeparator() + "#endif\")»" + System.lineSeparator() + "«" + cPFMSN +
      "(\"RBA_SVBB_ADAPTIVECALIB_16\")." + sSMP + "(" + System.lineSeparator() + "\"#if defined(__ghs__)" +
      System.lineSeparator() + "  #pragma ghs section rodata=\\\".data.CalSup_CalAdaptive.a2\\\"" +
      System.lineSeparator() + "#else" + System.lineSeparator() +
      "  #pragma section  \\\".bss.CalSup_CalAdaptive.a2\\\" awB" + System.lineSeparator() + "#endif\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"RBA_SVBB_ADAPTIVECALIB_16\")." + sEMP + "(" + System.lineSeparator() +
      "\"#if defined(__ghs__)" + System.lineSeparator() + "  #pragma ghs section rodata=default" +
      System.lineSeparator() + "#else" + System.lineSeparator() + "  #pragma section" + System.lineSeparator() +
      "#endif\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"RBA_SVBB_ADAPTIVECALIB_32\")." + sSMP + "(" +
      System.lineSeparator() + "\"#if defined(__ghs__)" + System.lineSeparator() +
      "  #pragma ghs section rodata=\\\".data.CalSup_CalAdaptive.a4\\\"" + System.lineSeparator() + "#else" +
      System.lineSeparator() + "  #pragma section  \\\".bss.CalSup_CalAdaptive.a4\\\" awB" + System.lineSeparator() +
      "#endif\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"RBA_SVBB_ADAPTIVECALIB_32\")." + sEMP + "(" +
      System.lineSeparator() + "\"#if defined(__ghs__)" + System.lineSeparator() +
      "  #pragma ghs section rodata=default" + System.lineSeparator() + "#else" + System.lineSeparator() +
      "  #pragma section" + System.lineSeparator() + "#endif\")»";

  static final String MEMSEC21120 = "(?i:.*?EcucPartition_OsApp)_([0-9]_[A-Z]_[0-9]).*_SHARED";

  /**
   *
   */
  public static final String GENERICEND21120 = "«" + cPFMSN + "(\"" + MEMSEC21120 + "_8\",\"Rte\")." + sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>OsApp_$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION> OsApp_$1$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_8BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21120 + "_8\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»" + System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21120 + "_16\",\"Rte\")." +
      sSMP +
      "(\"$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_1>\\\"$<INITIAL_KEYWORD_1>$<OS_APPLICATION>OsApp_$1$<FINAL_KEYWORD_1>$<ALIGNMENT_OPERATOR_16BIT>a2\\\" $<LINKER_FLAG_KEYWORD>[\\n$<PRAGMA_SECTION> $<OPTIONAL_KEYWORD_2>\\\"$<INITIAL_KEYWORD_2>$<OS_APPLICATION> OsApp_$1$<FINAL_KEYWORD_2>$<ALIGNMENT_OPERATOR_16BIT>a1\\\" $<LINKER_FLAG_KEYWORD>]\")»" +
      System.lineSeparator() + "«" + cPFMSN + "(\"" + MEMSEC21120 + "_16\",\"Rte\")." + sEMP +
      "(\"$<PRAGMA_SECTION>\")»";


  /**
   *
   */
  public static final String STATICSTARTSECTION = "«IMPORT arm»" + System.lineSeparator() +
      "«EXTENSION com::bosch::dgs::ice::armemmap::templates::MemMap_ArUtils»" + System.lineSeparator() +
      "«DEFINE MCU_RB_IFX_UCU_ARMEMMAP_PRAGMA_CONFIG_ENTRY FOR Collection-»" + System.lineSeparator() +
      "«invokeDefinition(\"MCU_RB_IFX_UCU_ARMEMMAP_PRAGMA_CONFIG\",getICEArProject())-»" + System.lineSeparator() +
      "«ENDDEFINE»" + System.lineSeparator() + System.lineSeparator() + "«REM» Pragma Configuration«ENDREM»" +
      System.lineSeparator() + "«DEFINE MCU_RB_IFX_UCU_ARMEMMAP_PRAGMA_CONFIG FOR ICEArProject-»" +
      System.lineSeparator() + System.lineSeparator() +
      "«REM»Start to store pragma sections from the template«ENDREM»" + System.lineSeparator() +
      "«REM»***********************************************«ENDREM»" + System.lineSeparator() +
      "«REM»Case 1: Adding memory sections«ENDREM»" + System.lineSeparator() +
      "«REM»***********************************************«ENDREM»" + System.lineSeparator() + "«REM»" +
      System.lineSeparator() + "«addMemorySection(\"TEST\")»" + System.lineSeparator() +
      "«addMemorySection(\"TEST_RTE\",\"Rte\")»" + System.lineSeparator() + "«ENDREM»" + System.lineSeparator() +
      "«REM»***********************************************«ENDREM»" + System.lineSeparator() +
      "«REM»Case2: Configuring default pragma... «ENDREM»" + System.lineSeparator() +
      "«REM»***********************************************«ENDREM»" + System.lineSeparator() + "«REM»" +
      System.lineSeparator() +
      "    «configPragmaDefault().setStartMemmapPragma(\"#pragma section \\\".sbss\\\" awBs\")»" +
      System.lineSeparator() + "    «configPragmaDefault().setEndMemmapPragma(\"#pragma section\")»" +
      System.lineSeparator() + "«ENDREM»" + System.lineSeparator() +
      "«REM»***********************************************«ENDREM»" + System.lineSeparator() +
      "«REM»Case3: Configuring non-D-Serap sections «ENDREM»" + System.lineSeparator() +
      "«REM»***********************************************«ENDREM»" + System.lineSeparator() + "«REM»" +
      System.lineSeparator() +
      "    «configPragmaForMemSectionName(\"VAR_8BIT\").setStartMemmapPragma(\"#pragma section \\\".data.ReIni\\\" aw\")»" +
      System.lineSeparator() +
      "    «configPragmaForMemSectionName(\"VAR_8BIT\").setEndMemmapPragma(\"#pragma section\")»" +
      System.lineSeparator() + "«ENDREM»" + System.lineSeparator() +
      "«REM»***********************************************«ENDREM»" + System.lineSeparator() +
      "«REM»Case4: D-Serap «ENDREM»" + System.lineSeparator() +
      "«REM»***********************************************«ENDREM»" + System.lineSeparator() +
      "«REM» can be used for PTA vectors and data structures, because {{memsectionname}} is used  «ENDREM»" +
      System.lineSeparator() + "«REM»" + System.lineSeparator() +
      "    «configPragmaForAds(\"DATAFAR\").setStartMemmapPragma(\"#pragma section \\\".rodata\\\" a\")»" +
      System.lineSeparator() + "    «configPragmaForAds(\"DATAFAR\").setEndMemmapPragma(\"#pragma section\")»" +
      System.lineSeparator() + "«ENDREM»" + System.lineSeparator() + "" + System.lineSeparator() +
      System.lineSeparator();

}
