/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;


/**
 * @author shk1cob
 */
public class RteConfigGeneratorConstants {

  /**
  *
  *
  */
  public static final String TOOL_VERSION = "2.2.0_rc0";

  /**
   *
   */
  public static final String AUTOSAR_RELEASE_VERSION = "AUTOSAR_RELEASE_VERSION";

  /**
  *
  */
  public static final Integer EXIT_FAIL = 1;

  /**
   *
   */
  public static final String AUTOSAR_RESOURCE_VERSION = "AUTOSAR_RESOURCE_VERSION";
  /**
   *
   */
  public static final String RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH = "RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH";
  /**
   *
   */
  public static final String RTE_TIMED_ECUC_PARAM_DEF_AR_PACKAGE_PATH = "RTE_TIMED_ECUC_PARAM_DEF_AR_PACKAGE_PATH";
  /**
   *
   */
  public static final String ECUC_PARTITION_PARAM_DEF_AR_PACKAGE_PATH = "ECUC_PARTITION_PARAM_DEF_AR_PACKAGE_PATH";

  /**
  *
  */
  public static final String DATA_READ_ACCESS = "DATA_READ_ACCESS";

  /**
   *
   */
  public static final String DATA_WRITE_ACCESS = "DATA_WRITE_ACCESS";
  /**
   *
   */
  public static final String RTE_ECUC_VALUE_AR_PACKAGE_PATH = "RTE_ECUC_VALUE_AR_PACKAGE_PATH";

  /**
   *
   */
  public static final String RTE_TIMED_ECUC_VALUE_AR_PACKAGE_PATH = "RTE_TIMED_ECUC_VALUE_AR_PACKAGE_PATH";


  /**
  *
  */
  public static final String RTE_RIPS_CSXFRM_ECUC_VALUE_AR_PACKAGE_PATH = "RTE_RIPS_CSXFRM_ECUC_VALUE_AR_PACKAGE_PATH";


  /**
  *
  */
  public static final String ECUC_PARTITION_ECUC_VALUE_AR_PACKAGE_PATH = "ECUC_PARTITION_ECUC_VALUE_AR_PACKAGE_PATH";


  /**
  *
  */
  public static final String OS_TASK_PARAM_DEF_AR_PKG_PATH = "OS_TASK_PARAM_DEF_AR_PKG_PATH";

  /**
  *
  */
  public static final String OS_ALARM_PARAM_DEF_AR_PKG_PATH = "OS_ALARM_PARAM_DEF_AR_PKG_PATH";

  /**
  *
  */
  public static final String OS_APPLICATION_PARAM_DEF_AR_PKG_PATH = "OS_APPLICATION_PARAM_DEF_AR_PKG_PATH";


  /**
  *
  */
  public static final String OS_SCHEDULE_TABLE_PARAM_DEF_AR_PKG_PATH = "OS_SCHEDULE_TABLE_PARAM_DEF_AR_PKG_PATH";


  /**
  *
  */
  public static final String RTE_BSW_MODULE_INSTANCE_DEF_AR_PKG_PATH = "RTE_BSW_MODULE_INSTANCE_DEF_AR_PKG_PATH";


  /**
   *
   *
   */
  public static final String EXCLUDE_ARXML_PATH = "EXCLUDE_ARXML_PATH";


  /**
  *
  *
  */
  public static final String RTE_ECUC_VALUE_OUTPUT_PATH = "RTE_ECUC_VALUE_OUTPUT_PATH";
  /**
  *
  *
  */
  public static final String RTE_TIMED_ECUC_VALUE_OUTPUT_PATH = "RTE_TIMED_ECUC_VALUE_OUTPUT_PATH";

  /**
  *
  *
  */
  public static final String RTE_RIPS_CSXFRM_ECUC_VALUE_OUTPUT_PATH = "RTE_RIPS_CSXFRM_ECUC_VALUE_OUTPUT_PATH";

  /**
  *
  *
  */
  public static final String ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH = "ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH";


  /**
  *
  *
  */
  public static final String MODE = "--mode_ecu_extract";

  /**
  *
  *
  */
  public static final String GENERATE_RTE_ECUC_VALUES = "n";

  /**
  *
  *
  */
  public static final String UPDATE_ECU_EXTRACT_FILES = "y";


  /**
  *
  *
  */
  public static final String ECU_EXTRACT_REFER_COMPOSITION_SW_COMPONENT_TYPE =
      "ECU_EXTRACT_REFER_COMPOSITION_SW_COMPONENT_TYPE";

  /**
  *
  *
  */
  public static final String RTE_CONFGEN_EXECUTION_MODE = "RTE_CONFGEN_EXECUTION_MODE";

  /**
  *
  *
  */
  public static final String TEST_MODE = "TEST_MODE";


  /**
  *
  *
  */
  public static final String PRODUCTIVE_MODE = "PRODUCTIVE_MODE";


  /**
   *
   */
  public static final String CONSIDER_ALL_BSW_EVENTS_FROM_PVER_FOR_RTE_ECUC_VALUE_GENERATION =
      "--instantiate_all_bsw_modules";
  /**
  *
  */
  public static final String CONSIDER_ALL_ASW_COMPONENTS_FROM_SWC_BSW_MAPPINGS_FOR_ECUC_PARTITION_GENERATION =
      "--instantiate_all_asw_components";

  /**
   *
   */
  public static final String ECU_EXTRACT_FLAT_VIEW_FILE_OUTPUT_PATH = "ECU_EXTRACT_FLAT_VIEW_FILE_OUTPUT_PATH";

  /**
  *
  */
  public static final String ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH = "ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH";

  /**
  *
  */
  public static final String ECU_EXTRACT_FILE_OUTPUT_PATH = "ECU_EXTRACT_FILE_OUTPUT_PATH";

  /**
  *
  */
  public static final String SYSTEM_EXTRACT_FILE_OUTPUT_PATH = "SYSTEM_EXTRACT_FILE_OUTPUT_PATH";

  /**
   *
   */
  public static final String RTE_ECUCVALUE_COLLECTION_AR_PACKAGE_PATH = "RTE_ECUCVALUE_COLLECTION_AR_PACKAGE_PATH";

  /**
   *
   */
  public static final String OVER_ALL_OS_CONFIG_INPUT_FILE = "--os_auto_conf_sched_file";

  /**
   *
   */
  // public static final String EVENTS_TASK_MAPPINGS_INPUT_FILE = "--condc_sched_file";


  /**
  *
  */
  public static final String ADDITIONAL_INPUT_FILES_LIST = "--additional_event_mapping_files";


  /**
  *
  */
  public static final String RTE_CONFIG_GEN_LOG = "--rteconfgen_log";

  /**
  *
  */
  public static final String LIST_OF_RTE_CONFIG_GEN_FILES = "--list_rteconfgen_rteconfig_files";

  /**
  *
  */
  public static final String LIST_OF_RTE_CONFIG_GEN_MEMMAP_HEADER_FILES = "--list_rteconfgen_memmap_header_files";

  /**
  *
  */
  public static final String LIST_OF_ECU_EXTRACT_FILES = "--list_rteconfgen_ecuextract_files";

  /**
  *
  */
  public static final String ECU_INSTANCE_PKG_PATH = "--artop_ecu_instance_path";

  /**
  *
  */
  public static final String ALL_AUTOSAR_FILE_LIST = "--all_autosar_files_list";


  /**
  *
  */
  public static final String RTE_CONFIG_GEN_DIR = "--rteconfgen_dir";


  /**
  *
  */
  public static final String RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR = "--rteconfgen_memmap_header_files_dir";

  /**
  *
  */
  public static final String RTE_CONFIG_GEN_ULF = "--rteconfgen_ulf";


  /**
  *
  */
  public static final String RTE_CONFIG_GEN_TASK_RECURRENCE_FILE_PATH = "--rteconfgen_task_recurrence_file_path";


  /**
  *
  */
  public static final String ENABLE_MM_DATA_CONSISTENCY = "--enable_mm_data_consistency";

  /**
  *
  */
  public static final String CREATE_FID_FOR_MODE_GROUPS = "--create_fid_for_mode_groups";


  /**
  *
  *
  */
  public static final String ENABLE_MM_DATA_CONSISTENCY_MODE = "y";

  /**
  *
  *
  */
  public static final String DISABLE_MM_DATA_CONSISTENCY_MODE = "n";


  /**
  *
  */
  public static final String ENABLE_CREATE_FID_FOR_MODE_GROUPS = "y";


  /**
  *
  */
  public static final String DISABLE_CREATE_FID_FOR_MODE_GROUPS = "n";

  /**
  *
  *
  */
  public static final String ENABLE_MEMMAP_HEADERFILE_GENERATION = "y";

  /**
  *
  *
  */
  public static final String MEMMAP_HEADERFILE_GENERATION = "--enable_memmap_headerfile_generation";

  /**
   *
   */
  public static final String WARN_FAILING_TO_CHOOSE_OS_APP = " Failed to choose the OS Application";
  /**
  *
  */
  public static final String RTECONFGEN_MEMMAP_XPT_CONFIG_FILEPATH = "--rteconfgen_memmap_xpt_config_filepath";
  /**
  *
  */
  public static final String MACHINE_FAMILY = "--machine_family";
  /**
  *
  */
  public static final String RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST =
      "--rteconfgen_memmap_xpt_output_filepath_list";
  /**
  *
  */
  public static final String ENABLE_MEMMAP_XPT_GENERATION = "--enable_memmap_xpt_generation";
  /**
   *
   */
  public static final String STARTSECTIONPATH = "--rteconfgen_memmap_start_section_filepath";

  /**
   *
   */
  public static final String ENDSECTIONPATH = "--rteconfgen_memmap_end_section_filepath";
  /**
   *
   */
  public static final String ALLOCATETASKASPEROSAPP = "--enable_rteconfgen_allocate_task_asper_osapp";
  /**
   *
   */
  public static final String ADDSWCASPEROSAPP = "--enable_rteconfgen_add_swc_asper_osapp";
  /**
   *
   */
  public static final String RTECONFGEN_MEMMAP_TASKWISE_SW_COMPONENT_PROTOTYPE_LIST_FILEPATH =
      "--rteconfgen_memmap_taskwise_swp_list_filepath";
  /**
  *
  */
  public static final String RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD = "--enable_rteconfgen_osapp_specific_swaddrmethod";
  /**
  *
  *
  */
  public static final String ENABLE_RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD = "n";
  /**
  *
  *
  */
  public static final String RTECONFGEN_INPUTS_CHANGED = "--rteconfgen_inputs_changed";
  /**
  *
  *
  */
  public static final String RTECONFGEN_MEMMAP_XPT_INPUTS_CHANGED = "--rteconfgen_memmap_xpt_inputs_changed";

  /**
   *
   */
  public static final String RTECONFGEN_TA_CONNECTION_LOG_PATH = "--rteconfgen_ta_connection_log_path";

  /**
   *
   */
  public static final String RTECONFGEN_TA_CONNECTION_CSV_PATH = "--rteconfgen_ta_connection_csv_path";

  /**
   *
   */
  public static final String RTECONFGEN_TA_CONNECTION_JSON_PATH = "--rteconfgen_ta_connection_json_path";

  /**
   *
   */
  public static final String RTARTE_VERSION = "--rtarte_version";

  /**
  *
  */
  public static final String RTECONFGEN_TRIGGER_VALIDATION_ERROR_AS_WARNING =
      "--rteconfgen_trigger_validation_error_as_warning";

  /**
  *
  */
  public static final String RTECONFGEN_GENERATE_SYSTEM_EXTRACT = "--enable_sys_extract_file_gen";
  // system extract file switch
}
