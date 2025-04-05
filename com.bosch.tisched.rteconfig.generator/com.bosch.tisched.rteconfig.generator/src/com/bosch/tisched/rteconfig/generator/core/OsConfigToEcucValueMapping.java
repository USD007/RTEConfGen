/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;

import com.bosch.tisched.rteconfig.generator.osconfig.CONFType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSAPPLICATIONIDMAPPINGSType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSAPPLICATIONIDMAPPINGType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSBSWMODULEINSTANCEType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSCFGType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSCOMPONENTINSTANCEType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSEVENTType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSGENERALCFGType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSPROCESSType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSTASKCONTEXTType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSTASKEVENTTYPEType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSTASKTYPEType;
import com.bosch.tisched.rteconfig.generator.osconfig.OSTASKType;
import com.bosch.tisched.rteconfig.generator.osconfig.ObjectFactory;
import com.bosch.tisched.rteconfig.generator.rips.GenerateRteRipsCSXfrmEcucValues;
import com.bosch.tisched.rteconfig.generator.util.AutosarUtil;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;

import autosar40.bswmodule.bswbehavior.BswEvent;
import autosar40.bswmodule.bswbehavior.BswExternalTriggerOccurredEvent;
import autosar40.bswmodule.bswbehavior.BswInternalBehavior;
import autosar40.bswmodule.bswimplementation.BswImplementation;
import autosar40.bswmodule.bswoverview.BswModuleDescription;
import autosar40.commonstructure.flatmap.FlatInstanceDescriptor;
import autosar40.commonstructure.flatmap.FlatMap;
import autosar40.commonstructure.flatmap.RtePluginProps;
import autosar40.commonstructure.swcbswmapping.SwcBswMapping;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.ecucdescription.EcucModuleConfigurationValuesRefConditional;
import autosar40.ecucdescription.EcucReferenceValue;
import autosar40.ecucdescription.EcucValueCollection;
import autosar40.ecucparameterdef.EcucAbstractReferenceDef;
import autosar40.ecucparameterdef.EcucChoiceContainerDef;
import autosar40.ecucparameterdef.EcucContainerDef;
import autosar40.ecucparameterdef.EcucModuleDef;
import autosar40.ecucparameterdef.EcucParamConfContainerDef;
import autosar40.ecucparameterdef.EcucReferenceDef;
import autosar40.genericstructure.abstractstructure.AtpFeature;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import autosar40.genericstructure.generaltemplateclasses.identifiable.Referrable;
import autosar40.swcomponent.components.AtomicSwComponentType;
import autosar40.swcomponent.components.PortPrototype;
import autosar40.swcomponent.components.SwComponentType;
import autosar40.swcomponent.composition.CompositionSwComponentType;
import autosar40.swcomponent.composition.DelegationSwConnector;
import autosar40.swcomponent.composition.SwComponentPrototype;
import autosar40.swcomponent.composition.SwConnector;
import autosar40.swcomponent.composition.instancerefs.PPortInCompositionInstanceRef;
import autosar40.swcomponent.composition.instancerefs.PortInCompositionTypeInstanceRef;
import autosar40.swcomponent.composition.instancerefs.RPortInCompositionInstanceRef;
import autosar40.swcomponent.portinterface.ClientServerOperation;
import autosar40.swcomponent.swcinternalbehavior.RunnableEntity;
import autosar40.swcomponent.swcinternalbehavior.SwcInternalBehavior;
import autosar40.swcomponent.swcinternalbehavior.dataelements.VariableAccess;
import autosar40.swcomponent.swcinternalbehavior.rteevents.AsynchronousServerCallReturnsEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.ExternalTriggerOccurredEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.OperationInvokedEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.RTEEvent;
import autosar40.swcomponent.swcinternalbehavior.servercall.AsynchronousServerCallPoint;
import autosar40.system.RootSwCompositionPrototype;
import autosar40.system.System;
import autosar40.system.fibex.fibexcore.coretopology.EcuInstance;

/**
 * @author shk1cob
 */
public class OsConfigToEcucValueMapping {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(OsConfigToEcucValueMapping.class.getName());
  private final IProject iProject;
  private List<TischedComponent> tischedComponents;
  private Map<String, TischedComponentInstance> tischedComponentInstances;
  private List<TischedEvent> tischedEvents;
  private List<TischedTask> tischedTasks;
  private List<SwComponentType> listOfComponentTypes;
  private List<SwcBswMapping> listOfSwcBswMappings;
  private Map<String, List<SwComponentPrototype>> osAppMap;
  private Map<String, List<TischedEvent>> instanceEventMap;
  private Map<String, String> taskEventMap;
  private float tischedVersion = 0.8f;
  private Set<String> configuredBswInstanceSet;
  private final List<EcucContainerValue> ecucContainerValues = new ArrayList<>();
  private final List<EcucContainerValue> rteEcucContainerValues = new ArrayList<>();
  private final Map<String, List<TischedEvent>> componentInstaceToEventsMap = new HashMap<>();
  private final boolean btriggerErrorAsWarning;
  private final Map<String, String> isrEventToTaskMap = new HashMap<String, String>();

  /**
   * @return the isrEventToTaskMap
   */
  public Map<String, String> getIsrEventToTaskMap() {
    return this.isrEventToTaskMap;
  }

  /**
   * @return the componentInstaceToEventsMap
   */
  public Map<String, List<TischedEvent>> getComponentInstaceToEventsMap() {
    return this.componentInstaceToEventsMap;
  }

  /**
   * @return the ecucContainerValues
   */
  public List<EcucContainerValue> getEcucContainerValues() {
    return this.ecucContainerValues;
  }

  public List<EcucContainerValue> getRteEcucContainerValues() {
    return this.rteEcucContainerValues;
  }

  /**
   * @return the configuredBswInstanceSet
   */
  public Set<String> getConfiguredBswInstanceSet() {
    if (this.configuredBswInstanceSet == null) {
      this.configuredBswInstanceSet = new HashSet<>();
    }
    return this.configuredBswInstanceSet;
  }

  /**
   * @return the instanceEventMap
   */
  public Map<String, List<TischedEvent>> getInstanceEventMap() {
    if (this.instanceEventMap == null) {
      this.instanceEventMap = new HashMap<>();
    }
    return this.instanceEventMap;
  }

  /**
   * @return the instanceEventMap
   */
  public Map<String, String> gettaskEventMap() {
    if (this.taskEventMap == null) {
      this.taskEventMap = new HashMap<>();
    }
    return this.taskEventMap;
  }

  /**
   * @return the listOfMappedEvents
   */
  public Map<String, List<SwComponentPrototype>> getOsAppSwCompPrototypesMap() {
    if (this.osAppMap == null) {
      this.osAppMap = new HashMap<String, List<SwComponentPrototype>>();
    }
    return this.osAppMap;
  }

  /**
   * @return the listOfSwcBswMaplings
   */
  private List<SwcBswMapping> getListOfSwcBswMappings() {
    if (this.listOfSwcBswMappings == null) {
      this.listOfSwcBswMappings = new ArrayList<SwcBswMapping>();
    }
    return this.listOfSwcBswMappings;
  }

  private List<SwComponentPrototype> listOfSWComponentProTypes;

  /**
   * @return the listOfSWComponentProTypes
   */
  private List<SwComponentPrototype> getListOfSWComponentProtoTypes() {
    if (this.listOfSWComponentProTypes == null) {
      this.listOfSWComponentProTypes = new ArrayList<SwComponentPrototype>();
    }
    return this.listOfSWComponentProTypes;
  }

  private Map<String, EcucContainerValue> osTaskMap;
  private Map<String, List<String>> rteBswModuleInstanceMap;
  private Map<String, String> bswImplPkgPath;

  /**
   * @return the rteBswModuleInstanceMap
   */
  public Map<String, List<String>> getRteBswModuleInstanceMap() {
    if (this.rteBswModuleInstanceMap == null) {
      this.rteBswModuleInstanceMap = new HashMap<>();
    }
    return this.rteBswModuleInstanceMap;
  }

  /**
   * @return the rteBswModuleInstanceMap
   */
  public Map<String, String> getRteBswImplMap() {
    if (this.bswImplPkgPath == null) {
      this.bswImplPkgPath = new HashMap();
    }
    return this.bswImplPkgPath;
  }

  private List<EcucContainerValue> listOfOsAlarms;
  private List<RTEEvent> listOfRteEvents;
  private List<BswEvent> listOfBswEvents;

  /**
   * @return the listOfOsAlarms
   */
  private List<EcucContainerValue> getListOfOsAlarms() {
    if (this.listOfOsAlarms == null) {
      this.listOfOsAlarms = new ArrayList<EcucContainerValue>();
    }
    return this.listOfOsAlarms;
  }

  private List<EcucContainerValue> listOfOsApps;

  /**
   * @return the listOfOsApps
   */
  private List<EcucContainerValue> getListOfOsApps() {
    if (this.listOfOsApps == null) {
      this.listOfOsApps = new ArrayList<EcucContainerValue>();
    }
    return this.listOfOsApps;
  }

  private List<EcucContainerValue> listOfOsScheduleTables;

  /**
   * @return the listOfOsScheduleTables
   */
  private List<EcucContainerValue> getListOfOsScheduleTables() {
    if (this.listOfOsScheduleTables == null) {
      this.listOfOsScheduleTables = new ArrayList<EcucContainerValue>();
    }
    return this.listOfOsScheduleTables;
  }

  private EcucReferenceDef osTaskAppRef;
  private EcucChoiceContainerDef osAlarmAction;
  private EcucParamConfContainerDef osAlarmActivateTask;
  private EcucReferenceDef osAlarmActivateTaskRef;
  private EcucParamConfContainerDef osScheduleTableExpiryPoint;
  private EcucParamConfContainerDef osScheduleTableTaskActivation;
  private EcucReferenceDef osScheduleTableActivateTaskRef;
  private String osAlarmarPkgPath;
  private String osApparPkgPath;
  private String osScheduleTablearPkgPath;
  private String rteBswModulePkgPath;
  private int minimumPreEmpPrio;
  private boolean preEmpPrioSet = false;
  private boolean addBswImpl = false;
  private boolean addAswComponents = false;
  private final Map<String, String> props;
  private RootSwCompositionPrototype rootSwCompositionProtoType;

  /**
   * @param rootSwCompositionProtoType the rootSwCompositionProtoType to set
   */
  public void setRootSwCompositionProtoType(final RootSwCompositionPrototype rootSwCompositionProtoType) {
    this.rootSwCompositionProtoType = rootSwCompositionProtoType;
  }

  /**
   * @param rootSwCompositionProtoType the rootSwCompositionProtoType to set
   * @return
   */
  public RootSwCompositionPrototype getRootSwCompositionProtoType() {
    return this.rootSwCompositionProtoType;
  }

  private final String ecuInstancePath;
  private final Map<String, EcucContainerValue> osAppEcuPartitionMap = new HashMap<String, EcucContainerValue>();
  private final GenerateRteRipsCSXfrmEcucValues createRteRipsCSXfrmEcucValues;

  /**
   * @param iProject IProject
   * @param map Map<String, String>
   */
  public OsConfigToEcucValueMapping(final IProject iProject, final Map<String, String> map,
      final GenerateRteRipsCSXfrmEcucValues createRteRipsCSXfrmEcucValues) {
    this.iProject = iProject;
    this.createRteRipsCSXfrmEcucValues = createRteRipsCSXfrmEcucValues;
    this.outputPath = map.get(RteConfigGeneratorConstants.EXCLUDE_ARXML_PATH);
    this.addBswImpl = Boolean.parseBoolean(
        map.get(RteConfigGeneratorConstants.CONSIDER_ALL_BSW_EVENTS_FROM_PVER_FOR_RTE_ECUC_VALUE_GENERATION));
    this.addAswComponents = Boolean.parseBoolean(map.get(
        RteConfigGeneratorConstants.CONSIDER_ALL_ASW_COMPONENTS_FROM_SWC_BSW_MAPPINGS_FOR_ECUC_PARTITION_GENERATION));
    this.ecuInstancePath = map.get(RteConfigGeneratorConstants.ECU_INSTANCE_PKG_PATH);
    this.props = map;
    this.btriggerErrorAsWarning =
        map.get(RteConfigGeneratorConstants.RTECONFGEN_TRIGGER_VALIDATION_ERROR_AS_WARNING).equals("y") ? true : false;
  }

  /**
   * @return the listOfOsTasks
   */
  private Map<String, EcucContainerValue> geOsTaskMap() {
    if (this.osTaskMap == null) {
      this.osTaskMap = new HashMap<>();
    }
    return this.osTaskMap;
  }

  private List<BswModuleDescription> listOfBswmd;
  private List<BswImplementation> listOfbswi;
  private String osTaskarPkgPath;
  private String rteBswModuleInstancePkgPath;
  private final String outputPath;
  private List<OSAPPLICATIONIDMAPPINGType> listOfOsApplicationMappings;
  private Map<SwComponentType, List<SwComponentPrototype>> swcMap;
  private Map<SwComponentPrototype, Map<String, List<VariableAccess>>> swcpTimedVariableAccessMap;
  private List<RunnableEntity> timedComRunnables;
  private boolean validateTimedCom = false;
  private boolean newPositioning = false;
  private boolean autoTriggerconnection = true;
  private final List<TischedTask> ETOTaskList = new ArrayList<TischedTask>();

  /**
   * @return the bAutoTriggerconnection
   */
  public boolean isbAutoTriggerconnection() {
    return this.autoTriggerconnection;
  }

  /**
   * @return the eTOTaskList
   */
  public List<TischedTask> getETOTaskList() {
    return this.ETOTaskList;
  }

  /**
   * @return the swcMap
   */
  private Map<SwComponentType, List<SwComponentPrototype>> getSwcMap() {
    if (this.swcMap == null) {
      this.swcMap = new HashMap<SwComponentType, List<SwComponentPrototype>>();
    }
    return this.swcMap;
  }

  private List<String> getListfInputFilePaths() throws Exception {
    List<String> lst = new ArrayList<String>();
    String path = this.props.get(RteConfigGeneratorConstants.ADDITIONAL_INPUT_FILES_LIST);
    if (!path.isEmpty()) {
      try (Scanner s = new Scanner(new File(path))) {
        while (s.hasNextLine()) {
          lst.add(this.iProject.getLocation().toOSString() + "/" + s.nextLine().trim());
        }
      }
      catch (FileNotFoundException e) {
        LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
        LOGGER.error("File not found: " + path.toString());
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
      }
    }
    return lst;
  }

  /**
   * @return List<CONFType>
   * @throws Exception
   */
  public List<CONFType> loadXmlContent() throws Exception {
    List<CONFType> list = new ArrayList<CONFType>();
    List<String> inputFiles = new ArrayList<String>();
    inputFiles.add(this.props.get(RteConfigGeneratorConstants.OVER_ALL_OS_CONFIG_INPUT_FILE));
    List<String> listfInputFilePaths = getListfInputFilePaths();
    if (!listfInputFilePaths.isEmpty()) {
      inputFiles.addAll(listfInputFilePaths);
    }
    for (String filePath : inputFiles) {
      try {
        LOGGER.info("Parsing file '" + filePath + "'");
        File file = new File(filePath);
        JAXBContext jaxbContext;
        jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<CONFType> confType = (JAXBElement<CONFType>) jaxbUnmarshaller.unmarshal(file);
        if (list.size() > 0) {
          mergeOsComponentInstances(list.get(0), confType.getValue(), inputFiles.get(0));
        }
        if (confType != null) {
          list.add(confType.getValue());
        }
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("130_0").trim());
      }
      catch (JAXBException e) {
        LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
        LOGGER.error("JAXB execption occured while parsing file:  " + filePath);
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
        list.clear();
      }
    }
    boolean mmDCSCompoPathSet = false;
    List<OSTASKType> tasklist = new ArrayList<>();
    for (CONFType c : list) {
      List<Object> objects = c.getOSCFG().getOSGENERALCFGAndOSAPPLICATIONIDMAPPINGSAndOSTASK();

      for (Object obj : objects) {

        if (obj instanceof OSTASKType) {
          tasklist.add((OSTASKType) obj);
        }
        if (obj instanceof OSAPPLICATIONIDMAPPINGSType) {
          List<OSAPPLICATIONIDMAPPINGType> osapplicationidmapping =
              ((OSAPPLICATIONIDMAPPINGSType) obj).getOSAPPLICATIONIDMAPPING();
          if (!osapplicationidmapping.isEmpty()) {
            getOsApplicationMappings().addAll(osapplicationidmapping);
          }
        }
        else if ((obj instanceof OSGENERALCFGType) && (((OSGENERALCFGType) obj).getOSMINPREEMPPRIO() != null) &&
            !this.preEmpPrioSet) {
          OSGENERALCFGType oSGENERALCFGType = (OSGENERALCFGType) obj;
          if ((oSGENERALCFGType.getMMDCSCOMPOSITIONPATH() != null) && (!mmDCSCompoPathSet)) {
            String compositionPath = oSGENERALCFGType.getMMDCSCOMPOSITIONPATH();
            String propertyValue =
                this.props.get(RteConfigGeneratorConstants.ECU_EXTRACT_REFER_COMPOSITION_SW_COMPONENT_TYPE);
            if ((propertyValue == null) || propertyValue.isEmpty()) {
              this.props.put(RteConfigGeneratorConstants.ECU_EXTRACT_REFER_COMPOSITION_SW_COMPONENT_TYPE,
                  compositionPath.trim());
              LOGGER
                  .info(
                      RteConfGenMessageDescription
                          .getFormattedMesssage("102_0",
                              this.props
                                  .get(RteConfigGeneratorConstants.ECU_EXTRACT_REFER_COMPOSITION_SW_COMPONENT_TYPE))
                          .trim());
            }
            mmDCSCompoPathSet = true;
          }
          if ((oSGENERALCFGType.getOSMINPREEMPPRIO() != null) && (!this.preEmpPrioSet)) {
            BigInteger osminpreempprio = oSGENERALCFGType.getOSMINPREEMPPRIO();
            this.minimumPreEmpPrio = osminpreempprio.intValue();
            this.preEmpPrioSet = true;
          }
        }
      }
    }
    if (!validateOSTasksForPositionTag(tasklist)) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("312_0").trim());
    }
    if (!this.preEmpPrioSet) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("201_0", inputFiles.get(0)).trim());
    }
    if (!mmDCSCompoPathSet) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("202_0", inputFiles.toString()).trim());
    }
    return list;
  }

  /**
   * @param tasklist
   * @return
   */
  private boolean validateOSTasksForPositionTag(final List<OSTASKType> tasklist) throws Exception {
    List<Object> osprocessOrOSEVENT = new ArrayList<>();
    List<OSPROCESSType> processList = new ArrayList<>();
    List<OSEVENTType> eventList = new ArrayList<>();
    tasklist.stream().forEach(task -> osprocessOrOSEVENT.addAll(task.getOSEVENTOrOSPROCESS()));
    for (Object obj : osprocessOrOSEVENT) {
      if (obj instanceof OSPROCESSType) {
        processList.add((OSPROCESSType) obj);
      }
      else {
        eventList.add((OSEVENTType) obj);
      }
    }
    if (!processList.isEmpty()) {
      for (OSPROCESSType process : processList) {
        if (process.getPosition() != null) {
          this.newPositioning = true;
          List<OSPROCESSType> collect =
              processList.stream().filter(proc -> proc.getPosition() == null).collect(Collectors.toList());
          List<OSEVENTType> collect2 =
              eventList.stream().filter(event -> event.getPosition() == null).collect(Collectors.toList());
          if (((collect != null) && !collect.isEmpty()) || ((collect2 != null) && !collect2.isEmpty())) {
            collect.stream().forEach(proc -> {
              try {
                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("264_0", proc.getValue()).trim());
              }
              catch (Exception e1) {
                e1.printStackTrace();
              }
            });
            collect2.stream().forEach(eve -> {
              try {
                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("264_0", eve.getValue()).trim());
              }
              catch (Exception e) {
                e.printStackTrace();
              }
            });
            return false;
          }
          return true;
        }
      }
    }
    if (!eventList.isEmpty()) {
      for (OSEVENTType event : eventList) {
        if (event.getPosition() != null) {
          this.newPositioning = true;
          List<OSPROCESSType> collect =
              processList.stream().filter(proc -> proc.getPosition() == null).collect(Collectors.toList());
          List<OSEVENTType> collect2 =
              eventList.stream().filter(eve -> eve.getPosition() == null).collect(Collectors.toList());
          if (((collect != null) && !collect.isEmpty()) || ((collect2 != null) && !collect2.isEmpty())) {
            collect.stream().forEach(proc -> {
              try {
                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("264_0", proc.getValue()).trim());
              }
              catch (Exception e1) {
                e1.printStackTrace();
              }
            });
            collect2.stream().forEach(eve -> {
              try {
                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("264_0", eve.getValue()).trim());
              }
              catch (Exception e) {
                e.printStackTrace();
              }
            });
            return false;
          }
          return true;
        }
      }
    }
    return true;
  }

  private void mergeOsComponentInstances(final CONFType confType1, final CONFType confType2, final String filePath1)
      throws Exception {
    OSCFGType oscfg = confType2.getOSCFG();
    if (oscfg != null) {
      Map<String, String> instancePathMap = getInstancePathMap(confType1);
      if (!instancePathMap.isEmpty()) {
        List<OSCOMPONENTINSTANCEType> listToMerge = new ArrayList<OSCOMPONENTINSTANCEType>();
        List<OSCOMPONENTINSTANCEType> listToRemove = new ArrayList<OSCOMPONENTINSTANCEType>();
        for (Object oscomptype : oscfg.getOSGENERALCFGAndOSAPPLICATIONIDMAPPINGSAndOSTASK()) {
          if (oscomptype instanceof OSCOMPONENTINSTANCEType) {
            OSCOMPONENTINSTANCEType oscInstance = (OSCOMPONENTINSTANCEType) oscomptype;
            if (oscInstance.getOSINSTANCENAME() != null) {
              String osinstance = oscInstance.getOSINSTANCENAME().trim();
              String ospath =
                  oscInstance.getOSCOMPONENTTYPEREF() == null ? "" : oscInstance.getOSCOMPONENTTYPEREF().trim();
              if (instancePathMap.containsKey(osinstance)) {
                String value = instancePathMap.get(osinstance);
                if (ospath.equals(value)) {
                  if (ospath.isEmpty()) {
                    LOGGER
                        .warn(RteConfGenMessageDescription.getFormattedMesssage("203_0", osinstance, filePath1).trim());
                  }
                  else {
                    LOGGER.warn(RteConfGenMessageDescription
                        .getFormattedMesssage("203_1", osinstance, ospath, filePath1).trim());
                  }
                }
                else {
                  LOGGER.warn(
                      RteConfGenMessageDescription.getFormattedMesssage("204_0", osinstance, filePath1, ospath).trim());
                }
              }
              else {
                listToMerge.add(oscInstance);
              }
            }
            listToRemove.add(oscInstance);
          }
        }
        if (!listToMerge.isEmpty()) {
          confType1.getOSCFG().getOSGENERALCFGAndOSAPPLICATIONIDMAPPINGSAndOSTASK().addAll(listToMerge);
        }
        if (!listToRemove.isEmpty()) {
          confType2.getOSCFG().getOSGENERALCFGAndOSAPPLICATIONIDMAPPINGSAndOSTASK().removeAll(listToRemove);
        }
      }
    }
  }

  private Map<String, String> getInstancePathMap(final CONFType confType1) {
    Map<String, String> instancePathMap = new HashMap<String, String>();
    OSCFGType oscfg = confType1.getOSCFG();
    if (oscfg != null) {
      for (Object oscomptype : oscfg.getOSGENERALCFGAndOSAPPLICATIONIDMAPPINGSAndOSTASK()) {
        if (oscomptype instanceof OSCOMPONENTINSTANCEType) {
          OSCOMPONENTINSTANCEType oscInstance = (OSCOMPONENTINSTANCEType) oscomptype;
          instancePathMap.put(oscInstance.getOSINSTANCENAME().trim(),
              oscInstance.getOSCOMPONENTTYPEREF() == null ? "" : oscInstance.getOSCOMPONENTTYPEREF().trim());
        }
      }
    }
    return instancePathMap;
  }

  /**
   * @return List<OSAPPLICATIONIDMAPPINGType>
   */
  public List<OSAPPLICATIONIDMAPPINGType> getOsApplicationMappings() {
    if (this.listOfOsApplicationMappings == null) {
      this.listOfOsApplicationMappings = new ArrayList<OSAPPLICATIONIDMAPPINGType>();
    }
    return this.listOfOsApplicationMappings;
  }

  private void updateSwcMap() {
    for (SwComponentPrototype swp : getListOfSWComponentProtoTypes()) {
      SwComponentType type = swp.getType();
      if (type != null) {
        List<SwComponentPrototype> list = getSwcMap().get(type);
        if (list != null) {
          list.add(swp);
        }
        else {
          List<SwComponentPrototype> li = new ArrayList<SwComponentPrototype>();
          li.add(swp);
          getSwcMap().put(type, li);
        }
      }
    }
  }

  /**
   * @param rootSwCompositionPrototype2
   * @return
   * @throws Exception
   */
  public boolean checkIfRteRipsPluginImplTimedComExist(final RootSwCompositionPrototype rootSwCompositionPrototype2,
      final boolean timedComAffected, final List<String> vaList)
      throws Exception {
    List<EcucModuleConfigurationValuesRefConditional> ecuCValueCollections =
        getEcuCValueCollections(rootSwCompositionPrototype2);
    for (EcucModuleConfigurationValuesRefConditional emcv : ecuCValueCollections) {
      EcucModuleConfigurationValues ecucModuleConfigurationValues = emcv.getEcucModuleConfigurationValues();
      if ((ecucModuleConfigurationValues != null) && (ecucModuleConfigurationValues.getDefinition() != null)) {
        EcucModuleDef definition = ecucModuleConfigurationValues.getDefinition();
        String shortName = definition.getShortName();
        String packagePath = GenerateArxmlUtil.getPackagePath(definition);
        if (ecucModuleConfigurationValues.getDefinition().eIsProxy()) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("305_0",
                  GenerateArxmlUtil.getPackagePath(ecucModuleConfigurationValues.getDefinition()),
                  ecucModuleConfigurationValues.getShortName()).trim());
          return this.validateTimedCom;
        }
        if ((packagePath != null) && shortName.equals("Rte_Rips_ImplTimedCom")) {
          this.props.put(RteConfigGeneratorConstants.RTE_TIMED_ECUC_PARAM_DEF_AR_PACKAGE_PATH, packagePath);
          EObject eContainer = ecucModuleConfigurationValues.eContainer();
          if (eContainer instanceof ARPackage) {
            String packagePath2 = GenerateArxmlUtil.getPackagePath(eContainer);
            this.props.put(RteConfigGeneratorConstants.RTE_TIMED_ECUC_VALUE_AR_PACKAGE_PATH, packagePath2);
            this.validateTimedCom = true;
          }
          if (!timedComAffected) {
            getRteRipsPluginImplTimedComVariableAccessList(ecuCValueCollections, vaList);
          }
        }
      }
    }
    if (!ecuCValueCollections.isEmpty() && !this.validateTimedCom) {
      LOGGER.warn(timedComAffected ? RteConfGenMessageDescription.getFormattedMesssage("239_0").trim()
          : RteConfGenMessageDescription.getFormattedMesssage("239_1").trim());
    }
    return this.validateTimedCom;
  }

  /**
   * @param rootSwCompositionPrototype2
   * @return
   * @throws Exception
   */
  public List<EcucModuleConfigurationValuesRefConditional> getEcuCValueCollections(
      final RootSwCompositionPrototype rootSwCompositionPrototype2)
      throws Exception {
    List<EcucModuleConfigurationValuesRefConditional> ecucValueCollections1 = new ArrayList();
    CompositionSwComponentType softwareComposition = rootSwCompositionPrototype2.getSoftwareComposition();
    System system = null;
    if (softwareComposition != null) {
      system = rootSwCompositionPrototype2.getSystem();
      if (system != null) {
        List<EcucValueCollection> listOfEObjects =
            GenerateArxmlUtil.getListOfEObject(this.iProject, EcucValueCollection.class, this.outputPath);
        if ((listOfEObjects != null)) {
          EList<EcucModuleConfigurationValuesRefConditional> ecucValueCollections =
              getEcucValueCollections(system, listOfEObjects);
          if ((ecucValueCollections != null)) {
            return ecucValueCollections;
          }
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("205_0").trim());
          this.validateTimedCom = false;

        }
      }
      else {
        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("104_0", GenerateArxmlUtil.getPackagePath(this.rootSwCompositionProtoType)).trim());
      }
    }
    return ecucValueCollections1;
  }

  /**
   * @param ecucValueCollections
   * @param vaList
   */
  public void getRteRipsPluginImplTimedComVariableAccessList(
      final List<EcucModuleConfigurationValuesRefConditional> ecucValueCollections, final List<String> vaList) {
    for (EcucModuleConfigurationValuesRefConditional emcv1 : ecucValueCollections) {
      EcucModuleConfigurationValues ecucModuleConfigurationValues1 = emcv1.getEcucModuleConfigurationValues();
      if ((ecucModuleConfigurationValues1 != null) && (ecucModuleConfigurationValues1.getDefinition() != null)) {
        EcucModuleDef definition1 = ecucModuleConfigurationValues1.getDefinition();
        String shortName1 = definition1.getShortName();
        String packagePath1 = GenerateArxmlUtil.getPackagePath(definition1);
        if ("Rte".equals(shortName1) && "/AUTOSAR_Rte/EcucModuleDefs/Rte".equals(packagePath1)) {
          EList<EcucContainerValue> containers = ecucModuleConfigurationValues1.getContainers();
          for (EcucContainerValue container : containers) {
            EcucContainerDef definition2 = container.getDefinition();
            String shortName2 = definition2.getShortName();
            String packagePath2 = GenerateArxmlUtil.getPackagePath(definition2);
            if ("RteImplicitCommunication".equals(shortName2) &&
                "/AUTOSAR_Rte/EcucModuleDefs/Rte/RteImplicitCommunication".equals(packagePath2)) {
              for (EcucAbstractReferenceValue ecucRefValue : container.getReferenceValues()) {
                EcucAbstractReferenceDef definition3 = ecucRefValue.getDefinition();
                String shortName3 = definition3.getShortName();
                String packagePath3 = GenerateArxmlUtil.getPackagePath(definition3);
                if (("RteVariableWriteAccessRef".equals(shortName3) &&
                    "/AUTOSAR_Rte/EcucModuleDefs/Rte/RteImplicitCommunication/RteVariableWriteAccessRef"
                        .equals(packagePath3)) ||
                    ("RteVariableReadAccessRef".equals(shortName3) &&
                        "/AUTOSAR_Rte/EcucModuleDefs/Rte/RteImplicitCommunication/RteVariableReadAccessRef"
                            .equals(packagePath3))) {
                  Referrable value = ((EcucReferenceValue) ecucRefValue).getValue();
                  if (value != null) {
                    VariableAccess va = (VariableAccess) value;
                    if (va.eContainer() != null) {
                      RunnableEntity r = (RunnableEntity) va.eContainer();
                      vaList.add(va.getShortName() + "_" + r.getShortName());
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * @return
   */
  public boolean isValidateTimedCom() {
    return this.validateTimedCom;
  }

  private boolean init() throws Exception {
    this.listOfComponentTypes =
        GenerateArxmlUtil.getListOfEObject(this.iProject, SwComponentType.class, this.outputPath);
    this.listOfBswmd = GenerateArxmlUtil.getListOfEObject(this.iProject, BswModuleDescription.class, this.outputPath);
    this.listOfbswi = GenerateArxmlUtil.getListOfEObject(this.iProject, BswImplementation.class, this.outputPath);
    this.listOfRteEvents = GenerateArxmlUtil.getListOfEObject(this.iProject, RTEEvent.class, this.outputPath);
    this.listOfBswEvents = GenerateArxmlUtil.getListOfEObject(this.iProject, BswEvent.class, this.outputPath);
    this.listOfSwcBswMappings = GenerateArxmlUtil.getListOfEObject(this.iProject, SwcBswMapping.class, this.outputPath);
    String mode = this.props.get(RteConfigGeneratorConstants.MODE);
    if (mode.equals(RteConfigGeneratorConstants.UPDATE_ECU_EXTRACT_FILES)) {
      if ((this.rootSwCompositionProtoType != null) && this.rootSwCompositionProtoType.eIsProxy()) {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("302_0").trim());
        return false;
      }
      if (this.rootSwCompositionProtoType == null) {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("303_0", this.ecuInstancePath).trim());
        return false;
      }
      if ((this.rootSwCompositionProtoType != null) && this.rootSwCompositionProtoType.eIsProxy()) {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("302_0").trim());
        return false;
      }
    }
    else {
      List<EcuInstance> list11 =
          GenerateArxmlUtil.getListOfEObject(this.iProject, this.ecuInstancePath, EcuInstance.class, this.outputPath);
      if ((list11 != null) && !list11.isEmpty()) {
        if (list11.size() > 1) {
          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("103_0", this.ecuInstancePath).trim());
        }
        this.rootSwCompositionProtoType =
            GenerateArxmlUtil.getRootSwCompositionPrototype(list11.get(0), this.iProject, this.outputPath);
        if (this.rootSwCompositionProtoType == null) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("303_0", this.ecuInstancePath).trim());
          return false;
        }
        if ((this.rootSwCompositionProtoType != null) && this.rootSwCompositionProtoType.eIsProxy()) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("302_0").trim());
          return false;
        }
      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("304_0", this.ecuInstancePath).trim());
      }
    }
    CompositionSwComponentType softwareComposition = this.rootSwCompositionProtoType.getSoftwareComposition();
    if (softwareComposition != null) {
      System system = this.rootSwCompositionProtoType.getSystem();
      if (system != null) {
        if (!softwareComposition.getComponents().isEmpty()) {
          getListOfSWComponentProtoTypes().addAll(softwareComposition.getComponents());
        }
        updateSwcMap();
        List<EcucValueCollection> listOfEObjects =
            GenerateArxmlUtil.getListOfEObject(this.iProject, EcucValueCollection.class, this.outputPath);
        if ((listOfEObjects != null)) {
          EList<EcucModuleConfigurationValuesRefConditional> ecucValueCollections =
              getEcucValueCollections(system, listOfEObjects);
          if ((ecucValueCollections != null)) {
            for (EcucModuleConfigurationValuesRefConditional emcv : ecucValueCollections) {
              EcucModuleConfigurationValues ecucModuleConfigurationValues = emcv.getEcucModuleConfigurationValues();
              if ((ecucModuleConfigurationValues != null) && (ecucModuleConfigurationValues.getDefinition() != null)) {
                EcucModuleDef definition = ecucModuleConfigurationValues.getDefinition();
                String shortName = definition.getShortName();
                String packagePath = GenerateArxmlUtil.getPackagePath(definition);
                if (ecucModuleConfigurationValues.getDefinition().eIsProxy()) {
                  RteConfigGeneratorLogger.logErrormessage(LOGGER,
                      RteConfGenMessageDescription.getFormattedMesssage("305_0",
                          GenerateArxmlUtil.getPackagePath(ecucModuleConfigurationValues.getDefinition()),
                          ecucModuleConfigurationValues.getShortName()).trim());
                  return false;
                }
                if (packagePath != null) {
                  if (shortName.equals("Os")) {
                    this.props.put(RteConfigGeneratorConstants.OS_TASK_PARAM_DEF_AR_PKG_PATH, packagePath + "/OsTask");
                    this.props.put(RteConfigGeneratorConstants.OS_ALARM_PARAM_DEF_AR_PKG_PATH,
                        packagePath + "/OsAlarm");
                    this.props.put(RteConfigGeneratorConstants.OS_APPLICATION_PARAM_DEF_AR_PKG_PATH,
                        packagePath + "/OsApplication");
                    this.props.put(RteConfigGeneratorConstants.OS_SCHEDULE_TABLE_PARAM_DEF_AR_PKG_PATH,
                        packagePath + "/OsScheduleTable");
                    this.osTaskarPkgPath = this.props.get(RteConfigGeneratorConstants.OS_TASK_PARAM_DEF_AR_PKG_PATH);
                    this.osAlarmarPkgPath = this.props.get(RteConfigGeneratorConstants.OS_ALARM_PARAM_DEF_AR_PKG_PATH);
                    this.osApparPkgPath =
                        this.props.get(RteConfigGeneratorConstants.OS_APPLICATION_PARAM_DEF_AR_PKG_PATH);
                    this.osScheduleTablearPkgPath =
                        this.props.get(RteConfigGeneratorConstants.OS_SCHEDULE_TABLE_PARAM_DEF_AR_PKG_PATH);
                  }
                  else if (shortName.equals("Rte")) {
                    this.props.put(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH, packagePath);
                    EObject eContainer = ecucModuleConfigurationValues.eContainer();
                    if (eContainer instanceof ARPackage) {
                      String packagePath2 = GenerateArxmlUtil.getPackagePath(eContainer);
                      this.props.put(RteConfigGeneratorConstants.RTE_ECUC_VALUE_AR_PACKAGE_PATH, packagePath2);
                    }
                    if (this.tischedVersion > 0.8f) {
                      this.props.put(RteConfigGeneratorConstants.RTE_BSW_MODULE_INSTANCE_DEF_AR_PKG_PATH,
                          packagePath + "/RteBswModuleInstance");
                      this.rteBswModuleInstancePkgPath =
                          this.props.get(RteConfigGeneratorConstants.RTE_BSW_MODULE_INSTANCE_DEF_AR_PKG_PATH);
                    }
                  }
                  else if (shortName.equals("EcuC")) {
                    this.props.put(RteConfigGeneratorConstants.ECUC_PARTITION_PARAM_DEF_AR_PACKAGE_PATH, packagePath);
                    EObject eContainer = ecucModuleConfigurationValues.eContainer();
                    if (eContainer instanceof ARPackage) {
                      String packagePath2 = GenerateArxmlUtil.getPackagePath(eContainer);
                      this.props.put(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_AR_PACKAGE_PATH,
                          packagePath2);
                    }
                  }
                }
              }
            }
            updateListOfOsTasksToList(ecucValueCollections);
            updateListOfOsAlarmsToList(ecucValueCollections);
            updateListOfOsAppsToList(ecucValueCollections);
            updateListOfOsScheduleTablesToList(ecucValueCollections);
            if (this.tischedVersion > 0.8f) {
              updateListOfRteBswModuleInstancesToList(ecucValueCollections);
            }
            EcucReferenceDef eObject1 = GenerateArxmlUtil.getEObject(this.iProject,
                this.osApparPkgPath + "/OsAppTaskRef", EcucReferenceDef.class, this.outputPath);
            if (eObject1 != null) {
              this.osTaskAppRef = eObject1;
            }
            EcucChoiceContainerDef eObject2 = GenerateArxmlUtil.getEObject(this.iProject,
                this.osAlarmarPkgPath + "/OsAlarmAction", EcucChoiceContainerDef.class, this.outputPath);
            if (eObject2 != null) {
              this.osAlarmAction = eObject2;
            }
            EcucParamConfContainerDef eObject3 = GenerateArxmlUtil.getEObject(this.iProject,
                this.osAlarmarPkgPath + "/OsAlarmAction/OsAlarmActivateTask", EcucParamConfContainerDef.class,
                this.outputPath);
            if (eObject3 != null) {
              this.osAlarmActivateTask = eObject3;
            }
            EcucReferenceDef eObject4 = GenerateArxmlUtil.getEObject(this.iProject,
                this.osAlarmarPkgPath + "/OsAlarmAction/OsAlarmActivateTask/OsAlarmActivateTaskRef",
                EcucReferenceDef.class, this.outputPath);
            if (eObject4 != null) {
              this.osAlarmActivateTaskRef = eObject4;
            }
            EcucParamConfContainerDef eObject5 = GenerateArxmlUtil.getEObject(this.iProject,
                this.osScheduleTablearPkgPath + "/OsScheduleTableExpiryPoint", EcucParamConfContainerDef.class,
                this.outputPath);
            if (eObject2 != null) {
              this.osScheduleTableExpiryPoint = eObject5;
            }
            EcucParamConfContainerDef eObject6 = GenerateArxmlUtil.getEObject(this.iProject,
                this.osScheduleTablearPkgPath + "/OsScheduleTableExpiryPoint/OsScheduleTableTaskActivation",
                EcucParamConfContainerDef.class, this.outputPath);
            if (eObject6 != null) {
              this.osScheduleTableTaskActivation = eObject6;
            }
            EcucReferenceDef eObject7 = GenerateArxmlUtil.getEObject(this.iProject,
                this.osScheduleTablearPkgPath +
                    "/OsScheduleTableExpiryPoint/OsScheduleTableTaskActivation/OsScheduleTableActivateTaskRef",
                EcucReferenceDef.class, this.outputPath);
            if (eObject7 != null) {
              this.osScheduleTableActivateTaskRef = eObject7;
            }
          }
          else {
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("205_0").trim());
            return false;
          }
        }
      }
      else {
        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("104_0", GenerateArxmlUtil.getPackagePath(this.rootSwCompositionProtoType)).trim());
      }
    }
    else {
      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("105_0", GenerateArxmlUtil.getPackagePath(this.rootSwCompositionProtoType)).trim());
    }
    return true;
  }

  private EList<EcucModuleConfigurationValuesRefConditional> getEcucValueCollections(final System system,
      final List<EcucValueCollection> listOfEObjects) {
    EList<EcucModuleConfigurationValuesRefConditional> ecucValues = null;
    for (EcucValueCollection ecucValueCollection : listOfEObjects) {
      boolean isSystemMatch = false;
      LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : ECUC Value Collection shortName : " + ecucValueCollection.getShortName());
      LOGGER
          .info("MM_DCS_RTECONFGEN_DEBUG : ECUC Value Collection size : " + ecucValueCollection.getEcucValues().size());
      if (system.equals(ecucValueCollection.getEcuExtract()) && (ecucValueCollection.getEcucValues().size() >= 3)) {
        boolean isOsPresent = false;
        boolean isRtePresent = false;
        boolean isEcuCPresent = false;
        isSystemMatch = true;
        for (EcucModuleConfigurationValuesRefConditional ecucV : ecucValueCollection.getEcucValues()) {
          if ((ecucV.getEcucModuleConfigurationValues() != null) &&
              (ecucV.getEcucModuleConfigurationValues().getDefinition() != null)) {
            EcucModuleDef emd = ecucV.getEcucModuleConfigurationValues().getDefinition();
            if (emd.getShortName().equals("Os")) {
              isOsPresent = true;
              LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : Os is present");
            }
            if (emd.getShortName().equals("Rte")) {
              isRtePresent = true;
              LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : Rte is present");
            }
            if (emd.getShortName().equals("EcuC")) {
              isEcuCPresent = true;
              this.ecucContainerValues.clear();
              this.ecucContainerValues.addAll(ecucV.getEcucModuleConfigurationValues().getContainers());
              LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : EcuC is present");
            }
          }
          if (isOsPresent && isRtePresent && isEcuCPresent) {
            return ecucValueCollection.getEcucValues();
          }
        }
        LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : Os, Rte & EcuC ecucvalue collections are missing");
      }
      if (!isSystemMatch && !system.equals(ecucValueCollection.getEcuExtract())) {
        LOGGER
            .warn("MM_DCS_RTECONFGEN_DEBUG : " + (ecucValueCollection.getEcuExtract() == null ? "EcuExrtact is not set "
                : "EcuExtract '" + ecucValueCollection.getEcuExtract().getShortName() + "'  is not matching"));
      }
    }
    return ecucValues;
  }

  private void clear() {
    if (this.listOfSWComponentProTypes != null) {
      this.listOfSWComponentProTypes.clear();
    }
    if (this.listOfComponentTypes != null) {
      this.listOfComponentTypes.clear();
    }
    if (this.listOfBswmd != null) {
      this.listOfBswmd.clear();
    }
    if (this.listOfbswi != null) {
      this.listOfbswi.clear();
    }
    if (this.listOfRteEvents != null) {
      this.listOfRteEvents.clear();
    }
    if (this.listOfBswEvents != null) {
      this.listOfBswEvents.clear();
    }
    if (this.osTaskMap != null) {
      this.osTaskMap.clear();
    }
    if (this.listOfOsAlarms != null) {
      this.listOfOsAlarms.clear();
    }
    if (this.listOfOsApps != null) {
      this.listOfOsApps.clear();
    }
    if (this.listOfOsScheduleTables != null) {
      this.listOfOsScheduleTables.clear();
    }
    if (this.listOfSwcBswMappings != null) {
      this.listOfSwcBswMappings.clear();
    }
    if (this.swcMap != null) {
      this.swcMap.clear();
    }
    if (this.rteBswModuleInstanceMap != null) {
      this.rteBswModuleInstanceMap.clear();
    }
    if (this.bswImplPkgPath != null) {
      this.bswImplPkgPath.clear();
    }
  }

  /**
   * @return the osAppEcuPartitionMap
   */
  public Map<String, EcucContainerValue> getOsAppEcuPartitionMap() {
    return this.osAppEcuPartitionMap;
  }

  /**
   * @return the tischedTasks
   */
  public List<TischedTask> getTischedTasks() {
    if (this.tischedTasks == null) {
      this.tischedTasks = new ArrayList<TischedTask>();
    }
    return this.tischedTasks;
  }

  /**
   * @return the tischedEvents
   */
  public List<TischedEvent> getTischedEvents() {
    if (this.tischedEvents == null) {
      this.tischedEvents = new ArrayList<TischedEvent>();
    }
    return this.tischedEvents;
  }

  /**
   * @return the tischedComponentInstances
   */
  public Map<String, TischedComponentInstance> getTischedComponentInstances() {
    if (this.tischedComponentInstances == null) {
      this.tischedComponentInstances = new HashMap<String, TischedComponentInstance>();
    }
    return this.tischedComponentInstances;
  }

  /**
   * @return the tischedComponents
   */
  public List<TischedComponent> getTischedComponents() {
    if (this.tischedComponents == null) {
      this.tischedComponents = new ArrayList<TischedComponent>();
    }
    return this.tischedComponents;
  }

  private float getTischedVersion(final String version) {
    float v = 0.8f;
    try {
      v = Float.parseFloat(version);
    }
    catch (Exception e) {
      v = 0.8f;
    }
    return v;
  }

  /**
   * @param list List<CONFType>
   * @return boolean
   * @throws Exception
   */
  public boolean doMapping(final List<CONFType> list) throws Exception {
    if ((list != null) && !list.isEmpty()) {
      LOGGER.info("*** Mapping overall OS config. elements to rte config. elements");
      for (CONFType confType : list) {
        OSCFGType oscfg = confType.getOSCFG();
        if (oscfg != null) {
          for (Object oscomptype : oscfg.getOSGENERALCFGAndOSAPPLICATIONIDMAPPINGSAndOSTASK()) {
            if ((oscomptype instanceof OSGENERALCFGType)) {
              OSGENERALCFGType osgeneralCFGType = (OSGENERALCFGType) oscomptype;
              this.autoTriggerconnection = osgeneralCFGType.isTRIGGERCONNECTIONAUTO();
              LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("140_0",
                  String.valueOf(this.autoTriggerconnection)));
              String tischedversion2 = osgeneralCFGType.getTISCHEDVERSION();
              this.tischedVersion =
                  ((tischedversion2 == null) || tischedversion2.isEmpty()) ? 0.8f : getTischedVersion(tischedversion2);
              break;
            }
          }
        }
      }
      boolean init = init();
      if (!init) {
        return false;
      }
      Map<String, OSTASKType> mergedTasks = new HashMap<String, OSTASKType>();
      try {
        List<OSCOMPONENTINSTANCEType> osCompInstances = new ArrayList<OSCOMPONENTINSTANCEType>();
        for (CONFType confType : list) {
          OSCFGType oscfg = confType.getOSCFG();
          if (oscfg != null) {
            if (this.tischedVersion > 0.8f) {
              for (Object oscomptype : oscfg.getOSGENERALCFGAndOSAPPLICATIONIDMAPPINGSAndOSTASK()) {
                if (oscomptype instanceof OSCOMPONENTINSTANCEType) {
                  osCompInstances.add((OSCOMPONENTINSTANCEType) oscomptype);
                  OSCOMPONENTINSTANCEType osCOMPONENTINSTANCEType = (OSCOMPONENTINSTANCEType) oscomptype;
                  doASWComponentMappingOnly(osCOMPONENTINSTANCEType.getOSINSTANCENAME(),
                      osCOMPONENTINSTANCEType.getOSCOMPONENTTYPEREF(), osCOMPONENTINSTANCEType.getOSAPPLICATIONID(),
                      (!osCOMPONENTINSTANCEType.isOSNOMEMMAPGEN()));
                }
                else if (oscomptype instanceof OSBSWMODULEINSTANCEType) {
                  OSBSWMODULEINSTANCEType osRTEBSWMODULEINSTANCEType = (OSBSWMODULEINSTANCEType) oscomptype;
                  updateTischedComponentInstanceForBSWComponentOnly(
                      osRTEBSWMODULEINSTANCEType.getOSBSWMODULEINSTANCENAME(),
                      osRTEBSWMODULEINSTANCEType.getOSBSWIMPLEMENTATIONREF());
                }
              }
            }
            else {
              for (Object oscomptype : oscfg.getOSGENERALCFGAndOSAPPLICATIONIDMAPPINGSAndOSTASK()) {
                if (oscomptype instanceof OSCOMPONENTINSTANCEType) {
                  osCompInstances.add((OSCOMPONENTINSTANCEType) oscomptype);
                  OSCOMPONENTINSTANCEType osCOMPONENTINSTANCEType = (OSCOMPONENTINSTANCEType) oscomptype;
                  doComponentMapping(osCOMPONENTINSTANCEType.getOSINSTANCENAME(),
                      osCOMPONENTINSTANCEType.getOSCOMPONENTTYPEREF(), osCOMPONENTINSTANCEType.getOSAPPLICATIONID(),
                      (!osCOMPONENTINSTANCEType.isOSNOMEMMAPGEN()));
                }
              }
            }
            for (Object ostask1 : oscfg.getOSGENERALCFGAndOSAPPLICATIONIDMAPPINGSAndOSTASK()) {
              if (ostask1 instanceof OSTASKType) {
                OSTASKType ostask = (OSTASKType) ostask1;
                OSTASKType ostaskType = mergedTasks.get(ostask.getOSTASKNAME().trim());
                if (ostaskType != null) {
                  if (ostask.getOSEVENTOrOSPROCESS().isEmpty()) {
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("241_0", ostask.getOSTASKNAME()).trim());
                  }
                  else {
                    ostaskType.getOSEVENTOrOSPROCESS().addAll(ostask.getOSEVENTOrOSPROCESS());
                  }
                }
                else {
                  mergedTasks.put(ostask.getOSTASKNAME().trim(), ostask);
                }
              }
              if (ostask1 instanceof OSTASKCONTEXTType) {
                OSTASKCONTEXTType ostask = (OSTASKCONTEXTType) ostask1;
                OSTASKType ostaskType = mergedTasks.get(ostask.getOSTASKNAME().trim());
                if ((ostaskType != null)) {
                  if (ostask.getOSEVENT().isEmpty()) {
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("241_0", ostask.getOSTASKNAME()).trim());
                  }
                  else {
                    ostaskType.getOSEVENTOrOSPROCESS().addAll(ostask.getOSEVENT());
                  }
                }
                else {
                  RteConfigGeneratorLogger.logErrormessage(LOGGER,
                      RteConfGenMessageDescription.getFormattedMesssage("306_0", ostask.getOSTASKNAME()).trim());
                }
              }
            }
          }
        }
        addMissingASWComponentPrototypes(osCompInstances);
        for (OSTASKType ostask : mergedTasks.values()) {
          addRteModuleInstancesForInstancesOfEvents(ostask);
        }
        List<String> highPrioTasklist = getHighPrioCoopTask(mergedTasks);
        for (OSTASKType ostask : mergedTasks.values()) {
          doTaskMapping(ostask, highPrioTasklist);
        }
        validateMixtureOfEvents();
        updateOsSwcMapFromFromSwcBswMappings();
      }
      finally {
        mergedTasks.clear();
        clear();
      }
      prepareForRipsInvocationHandlerFnc();
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("160_0").trim());
      return true;
    }
    return false;
  }

  /**
   * @throws Exception
   */
  public void validateMixtureOfEvents() throws Exception {
    List<TischedTask> tischedTasks = getTischedTasks();
    boolean bEventMixturePresent = false;
    for (TischedTask task : tischedTasks) {
      List<TischedEvent> etoEventList = new ArrayList<>();
      List<TischedEvent> nonETOEventList = new ArrayList<>();
      task.getEvents().stream().forEach(e -> {
        if ((e.getEvent() instanceof ExternalTriggerOccurredEvent) ||
            (e.getEvent() instanceof BswExternalTriggerOccurredEvent)) {
          etoEventList.add(e);
        }
        else {
          nonETOEventList.add(e);
        }
      });
      if ((task.getTischedTaskeventtypeenum() != null)) {
        if (task.getTischedTaskeventtypeenum().equals(TischedTaskEventTypeEnum.ETO)) {
          if (!nonETOEventList.isEmpty()) {
            StringBuilder eventlist = new StringBuilder();
            nonETOEventList.stream().filter(e -> (e != null) && (e.getInstance() != null)).forEach(e -> {
              if (e.getInstance() instanceof SwComponentPrototype) {
                SwComponentPrototype instance = (SwComponentPrototype) e.getInstance();
                eventlist.append(instance.getShortName() + " :: " + e.getShortName() + ",");
              }
              else {
                BswImplementation instance = (BswImplementation) e.getInstance();
                eventlist.append(instance.getShortName() + " :: " + e.getShortName() + ",");
              }
            });
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("271_0", task.getShortName(),
                eventlist.toString(), task.getShortName()));
            bEventMixturePresent = true;
          }
          this.ETOTaskList.add(task);
        }
        else {
          if (!etoEventList.isEmpty()) {
            StringBuilder eventlist = new StringBuilder();
            etoEventList.stream().forEach(e -> {
              if (e.getInstance() instanceof SwComponentPrototype) {
                SwComponentPrototype instance = (SwComponentPrototype) e.getInstance();
                eventlist.append(instance.getShortName() + " :: " + e.getShortName() + ",");
              }
              else {
                BswImplementation instance = (BswImplementation) e.getInstance();
                eventlist.append(instance.getShortName() + " :: " + e.getShortName() + ",");
              }
            });
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("271_1", task.getShortName(),
                task.getTischedTaskeventtypeenum().toString(), eventlist.toString(), task.getShortName()));
            bEventMixturePresent = true;
          }
        }
      }
      else {
        boolean containsBswEvent =
            etoEventList.stream().anyMatch(e -> (e.getEvent() instanceof BswExternalTriggerOccurredEvent));
        if (containsBswEvent) {
          this.ETOTaskList.add(task);
        }
      }
      if (!etoEventList.isEmpty() && !nonETOEventList.isEmpty()) {
        LOGGER
            .warn(RteConfGenMessageDescription.getFormattedMesssage("271_2", task.getShortName(), task.getShortName()));
        bEventMixturePresent = true;
      }
    }
    if (!this.btriggerErrorAsWarning && bEventMixturePresent) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription.getFormattedMesssage("316_0"));
    }
  }

  /**
   * @param mergedTasks
   * @return
   */
  private List<String> getHighPrioCoopTask(final Map<String, OSTASKType> mergedTasks) {
    int highestpriority = 0;
    List<String> highPrioTasklist = new ArrayList<>();
    for (OSTASKType ostask : mergedTasks.values()) {
      if ((ostask.getOSTASKPRIORITY() != null) && (ostask.getOSCOOPTASK() != null)) {
        if (ostask.getOSTASKPRIORITY().intValue() > highestpriority) {
          highestpriority = ostask.getOSTASKPRIORITY().intValue();
          highPrioTasklist.clear();
          highPrioTasklist.add(ostask.getOSTASKNAME());
        }
        else if (ostask.getOSTASKPRIORITY().intValue() == highestpriority) {
          highPrioTasklist.add(ostask.getOSTASKNAME());
        }
      }
    }
    return highPrioTasklist;
  }

  private void addMissingASWComponentPrototypes(final List<OSCOMPONENTINSTANCEType> osCompInstances) throws Exception {
    for (SwComponentPrototype swcp : getListOfSWComponentProtoTypes()) {
      boolean isConfigured = false;
      for (OSCOMPONENTINSTANCEType oscomponentinstanceType : osCompInstances) {
        if ((oscomponentinstanceType.getOSINSTANCENAME() != null) &&
            oscomponentinstanceType.getOSINSTANCENAME().trim().equals(swcp.getShortName())) {
          isConfigured = true;
          break;
        }
      }
      if (!isConfigured && (getTischedComponentInstances().get(swcp.getShortName()) == null)) {
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("106_0", swcp.getShortName()).trim());
        URI uri = EcoreResourceUtil.getURI(swcp.getType());
        String pkgPath = "";
        if (uri != null) {
          pkgPath = uri.fragment().split("\\?")[0];
        }
        doComponentMapping(swcp.getShortName(), pkgPath, new ArrayList<>(), true);
      }
    }
  }

  private void addAllBswImplementations() throws Exception {
    for (BswImplementation bswImpl : this.listOfbswi) {
      if ((getTischedComponentInstances().get(bswImpl.getShortName()) == null) && (bswImpl.getBehavior() != null) &&
          (bswImpl.getBehavior().eContainer() != null)) {
        BswModuleDescription bswmd = (BswModuleDescription) bswImpl.getBehavior().eContainer();
        // create bsw component
        TischedComponent tischedComponent = new TischedComponent();
        tischedComponent.setShortName(bswmd.getShortName());
        tischedComponent.setPackagePath(EcoreResourceUtil.getURI(bswmd).fragment().split("\\?")[0].trim());
        tischedComponent.setSwComponentTypeEnum(SwComponentTypeEnum.BSW);
        tischedComponent.setComponent(bswmd);
        // create bsw component instance
        TischedComponentInstance tischedComponentInstance = new TischedComponentInstance();
        tischedComponentInstance.setShortName(bswImpl.getShortName());
        tischedComponentInstance.setPackagePath("");
        tischedComponentInstance.setComponentInstance(bswImpl);
        tischedComponentInstance.setSwComponentInstanceTypeEnum(SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION);
        tischedComponentInstance.setTischedComponent(tischedComponent);
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("107_0", bswImpl.getShortName()).trim());
        getTischedComponentInstances().put(bswImpl.getShortName(), tischedComponentInstance);
        getTischedComponents().add(tischedComponent);
      }
    }
  }

  private String getBswImplName(final String implPath) {
    String name = "";
    for (String key : getRteBswImplMap().keySet()) {
      String value = getRteBswImplMap().get(key);
      if (value.equals(implPath)) {
        return key;
      }
    }
    return name;
  }

  private void addRteModuleInstancesForInstancesOfEvents(final OSTASKType ostask) throws Exception {
    if (ostask.getOSTASKTYPE() != OSTASKTYPEType.ISR) {
      for (Object object : ostask.getOSEVENTOrOSPROCESS()) {
        String swComponent = "";
        String componentTypeRef = "";
        if (object instanceof OSPROCESSType) {
          OSPROCESSType osProcess = (OSPROCESSType) object;
          swComponent = osProcess.getInstance() != null ? osProcess.getInstance().trim() : "";
        }
        else if (object instanceof OSEVENTType) {
          OSEVENTType osEvent = (OSEVENTType) object;
          swComponent = osEvent.getInstance() != null ? osEvent.getInstance().trim() : "";
        }
        for (BswImplementation b : this.listOfbswi) {
          if (b.getShortName().equals(swComponent)) {
            componentTypeRef = EcoreResourceUtil.getURI(b).fragment().split("\\?")[0].trim();
            break;
          }
        }
        String implpath = getRteBswImplMap().get(swComponent);
        TischedComponentInstance tischedComponentInstance = getTischedComponentInstances().get(swComponent);
        if ((tischedComponentInstance == null) && (implpath != null) && !implpath.isEmpty()) {
          TischedComponent tischedComponent = new TischedComponent();
          tischedComponent.setPackagePath(implpath);
          BswImplementation bswImplementation2 = getBSWImplementation(implpath);
          if (bswImplementation2 != null) {
            BswModuleDescription bswDesc =
                getBSWModuleDescFromShortName(bswImplementation2.getShortName(), tischedComponent);
            if (bswDesc != null) {
              if (bswDesc.eIsProxy()) {
                LOGGER.warn(RteConfGenMessageDescription
                    .getFormattedMesssage("214_0", tischedComponent.getPackagePath()).trim());
              }
              else {
                tischedComponent.setSwComponentTypeEnum(SwComponentTypeEnum.BSW);
                tischedComponent.setComponent(bswDesc);
                tischedComponentInstance = new TischedComponentInstance();
                tischedComponentInstance.setShortName(swComponent);
                tischedComponentInstance.setPackagePath("");
                tischedComponentInstance.setComponentInstance(bswImplementation2);
                tischedComponentInstance.setSwComponentInstanceTypeEnum(SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION);
                tischedComponentInstance.setTischedComponent(tischedComponent);
                getTischedComponentInstances().put(swComponent, tischedComponentInstance);
                if (!getRteBswImplMap().containsValue(implpath)) {
                  getRteBswImplMap().put(swComponent, implpath);
                }
              }
              if (tischedComponent.getSwComponentTypeEnum() != null) {
                getTischedComponents().add(tischedComponent);
                getConfiguredBswInstanceSet().add(swComponent);
              }
            }
            else {
              LOGGER.warn(
                  RteConfGenMessageDescription.getFormattedMesssage("244_0", bswImplementation2.getShortName()).trim());
            }
          }
          else {
            LOGGER.warn(
                RteConfGenMessageDescription.getFormattedMesssage("214_0", tischedComponent.getPackagePath()).trim());
          }
        }
        else {
          if ((componentTypeRef != null) && !componentTypeRef.isEmpty()) {
            updateTischedComponentInstanceForBSWComponentOnly(swComponent, componentTypeRef);
          }
        }
      }
    }
  }

  private void addRteModuleInstancesForAllBswImplementations() throws Exception {
    for (BswImplementation bswImpl : this.listOfbswi) {
      String rteBSWImplName = getRteBSWImplName(bswImpl);
      if ((rteBSWImplName != null) && rteBSWImplName.isEmpty() && (bswImpl.getShortName() != null)) {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("246_0", bswImpl.getShortName()).trim());
        rteBSWImplName = bswImpl.getShortName();
      }
      if (((rteBSWImplName != null) && !rteBSWImplName.isEmpty()) &&
          (getTischedComponentInstances().get(rteBSWImplName) == null) && (bswImpl.getBehavior() != null) &&
          (bswImpl.getBehavior().eContainer() != null)) {
        String implPkgPath = EcoreResourceUtil.getURI(bswImpl).fragment().split("\\?")[0].trim();
        boolean isConfigured = false;
        String implpath = getRteBswImplMap().get(rteBSWImplName);
        if ((implpath != null) && !implpath.isEmpty()) {
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("243_0", rteBSWImplName).trim());
          if (!implpath.equals(implPkgPath)) {
            LOGGER.warn(RteConfGenMessageDescription
                .getFormattedMesssage("247_0", rteBSWImplName, implpath, implPkgPath).trim());
            continue;
          }
          getConfiguredBswInstanceSet().add(rteBSWImplName);
          isConfigured = true;
        }
        if ((implpath == null) && getRteBswImplMap().containsValue(implPkgPath)) {
          String bswImplName = getBswImplName(implPkgPath);
          LOGGER.warn(RteConfGenMessageDescription
              .getFormattedMesssage("249_0", implPkgPath, bswImplName, rteBSWImplName).trim());
          rteBSWImplName = bswImplName;
          // getConfiguredBswInstanceSet().add(rteBSWImplName);
          isConfigured = true;
        }
        BswModuleDescription bswmd = (BswModuleDescription) bswImpl.getBehavior().eContainer();
        // create bsw component
        TischedComponent tischedComponent = new TischedComponent();
        tischedComponent.setShortName(bswmd.getShortName());
        tischedComponent.setPackagePath(EcoreResourceUtil.getURI(bswmd).fragment().split("\\?")[0].trim());
        tischedComponent.setSwComponentTypeEnum(SwComponentTypeEnum.BSW);
        tischedComponent.setComponent(bswmd);
        // create bsw component instance
        TischedComponentInstance tischedComponentInstance = new TischedComponentInstance();
        tischedComponentInstance.setShortName(rteBSWImplName);
        tischedComponentInstance.setPackagePath("");
        tischedComponentInstance.setComponentInstance(bswImpl);
        tischedComponentInstance.setSwComponentInstanceTypeEnum(SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION);
        tischedComponentInstance.setTischedComponent(tischedComponent);
        if (!isConfigured) {
          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("107_0", rteBSWImplName).trim());
        }
        getTischedComponentInstances().put(rteBSWImplName, tischedComponentInstance);
        getTischedComponents().add(tischedComponent);
        if (!getRteBswImplMap().containsValue(implPkgPath)) {
          getRteBswImplMap().put(rteBSWImplName, implPkgPath);
        }
      }
    }
  }

  private String getRteBSWImplName(final BswImplementation bswImpl) {
    String bswImplName = bswImpl.getShortName();
    Long vendorId = bswImpl.getVendorId();
    String vendorApiInfix = bswImpl.getVendorApiInfix();
    if ((vendorId != null) && (vendorId > 0) && (vendorApiInfix != null) && !vendorApiInfix.isEmpty()) {
      bswImplName = bswImplName + "_" + vendorId + "_" + vendorApiInfix;
    }
    if ((((vendorId != null) && (vendorId > 0)) && ((vendorApiInfix == null) || vendorApiInfix.isEmpty())) ||
        (((vendorId == null) || (vendorId <= 0)) && ((vendorApiInfix != null) && !vendorApiInfix.isEmpty()))) {
      bswImplName = "";
    }
    return bswImplName;
  }

  /**
   * @param highPrioTasklist
   * @param block Task
   * @throws Exception
   */
  private void doTaskMapping(final OSTASKType ostask, final List<String> highPrioTasklist) throws Exception {
    int position = 1;
    boolean isErrorLogPrinted = false;
    if (ostask.getOSTASKTYPE() != OSTASKTYPEType.ISR) {
      for (Object object : ostask.getOSEVENTOrOSPROCESS()) {
        OSPROCESSType osProcess = null;
        OSEVENTType osEvent = null;
        String eventName = "";
        String module = "";
        if (object instanceof OSPROCESSType) {
          osProcess = (OSPROCESSType) object;
          eventName = osProcess.getEvent() != null ? osProcess.getEvent().trim() : "";
          module = osProcess.getInstance() != null ? osProcess.getInstance().trim() : "";
        }
        else if (object instanceof OSEVENTType) {
          osEvent = (OSEVENTType) object;
          eventName = osEvent.getValue().trim();
          module = osEvent.getInstance() != null ? osEvent.getInstance().trim() : "";
        }
        List<String> list = getRteBswModuleInstanceMap().get(module);
        if ((list != null) && list.contains(eventName)) {
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("248_1", eventName, module).trim());
        }
        else {
          if ((eventName != null) && !eventName.isEmpty()) {
            TischedEvent tischedEvent = new TischedEvent();
            tischedEvent.setShortName(eventName);
            boolean matches = false;
            if ((module != null) && !module.isEmpty()) {
              for (TischedComponentInstance tischedComponentInstance : getTischedComponentInstances().values()) {
                if (tischedComponentInstance.getShortName().equals(module)) {
                  if (tischedComponentInstance
                      .getSwComponentInstanceTypeEnum() == SwComponentInstanceTypeEnum.SW_COMPONENT_PROTOTYPE) {
                    updateTischedInstanceForRteEvent(tischedEvent, eventName, tischedComponentInstance, false);
                    updateInstanceMap(tischedComponentInstance.getShortName(), tischedEvent);
                    matches = true;
                    break;
                  }
                  else if (tischedComponentInstance
                      .getSwComponentInstanceTypeEnum() == SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION) {
                    updateTischedInstanceForBswEvent(tischedEvent, eventName, tischedComponentInstance, false);
                    updateInstanceMap(tischedComponentInstance.getShortName(), tischedEvent);
                    matches = true;
                    break;
                  }
                }
              }
              if (!matches) {
                for (SwComponentPrototype swcpt : getListOfSWComponentProtoTypes()) {
                  if (swcpt.getShortName().equals(module)) {
                    updateTischedInstanceForRteEvent(tischedEvent, eventName, swcpt);
                    updateInstanceMap(swcpt.getShortName(), tischedEvent);
                    matches = true;
                    break;
                  }
                }
                if (!matches) {
                  if (this.tischedVersion > 0.8) {
                    for (BswImplementation bswi : this.listOfbswi) {
                      String rteBSWImplName = getRteBSWImplName(bswi);
                      if ((rteBSWImplName != null) && rteBSWImplName.isEmpty()) {
                        rteBSWImplName = bswi.getShortName();
                      }
                      if (module.equals(rteBSWImplName)) {
                        updateTischedInstanceForBswEvent(tischedEvent, eventName, bswi);
                        updateInstanceMap(rteBSWImplName, tischedEvent);
                        matches = true;
                        break;
                      }
                    }
                  }
                  else {
                    for (BswImplementation bswi : this.listOfbswi) {
                      if (bswi.getShortName().equals(module)) {
                        updateTischedInstanceForBswEvent(tischedEvent, eventName, bswi);
                        updateInstanceMap(bswi.getShortName(), tischedEvent);
                        matches = true;
                        break;
                      }
                    }
                  }
                }
                if (!matches) {
                  LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("206_0", module,
                      (object instanceof OSPROCESSType ? "OS_PROCESS" : "OS_EVENT"), eventName).trim());
                }
              }
            }
            else {
              LOGGER.warn(RteConfGenMessageDescription
                  .getFormattedMesssage("207_0", module, tischedEvent.getShortName()).trim());
            }
            if (matches && (tischedEvent.getEvent() == null)) {
              LOGGER.warn(RteConfGenMessageDescription
                  .getFormattedMesssage("208_0", tischedEvent.getShortName(), module).trim());
            }
            if (!this.newPositioning) {
              tischedEvent.setPosition(position);
            }
            else {
              if (osProcess != null) {
                tischedEvent.setPosition(Integer.parseInt(osProcess.getPosition()));
              }
              else {
                tischedEvent.setPosition(Integer.parseInt(osEvent.getPosition()));
              }
            }
            if (osProcess != null) {
              tischedEvent.setOffset(osProcess.getOffset());
            }
            else {
              tischedEvent.setOffset(osEvent.getOffset());
            }
            if ((ostask.getOSTASKPRIORITY() != null) &&
                (highPrioTasklist.stream().anyMatch(m -> m.equals(ostask.getOSTASKNAME())))) {
              tischedEvent.setCoOperative(false);
            }
            else if ((ostask.getOSCOOPTASK() != null) && !ostask.getOSCOOPTASK().trim().isEmpty()) {
              boolean isCoopTask =
                  !((tischedEvent.getEvent() != null) && (tischedEvent.getEvent() instanceof OperationInvokedEvent));
              tischedEvent.setCoOperative(isCoopTask);
            }
            else {
              tischedEvent.setCoOperative(false);
            }
            updateTischedTask(ostask.getOSTASKNAME().trim(),
                ostask.getOSTASKPRIORITY() != null ? ostask.getOSTASKPRIORITY().intValue() : 0,
                ostask.getOSTASKCYCLE() != null ? ostask.getOSTASKCYCLE() : "",
                ostask.getOSCHAINTASK() != null ? ostask.getOSCHAINTASK() : "", tischedEvent, isErrorLogPrinted,
                ostask.isOSRTETASKHOOKS() != null ? ostask.isOSRTETASKHOOKS() : false,
                ostask.getOSPPORTPROTOTYPE() != null ? ostask.getOSPPORTPROTOTYPE() : "",
                ostask.getOSBSWRELEASEDTRIGGER() != null ? ostask.getOSBSWRELEASEDTRIGGER() : "",
                ostask.getOSTASKEVENTTYPE(), ostask.getOSTASKTYPE());
            isErrorLogPrinted = true;
            getTischedEvents().add(tischedEvent);
          }
          else {
            if (object instanceof OSPROCESSType) {
              LOGGER.warn(RteConfGenMessageDescription
                  .getFormattedMesssage("209_0", ((OSPROCESSType) object).getValue()).trim());
            }
            else {
              LOGGER.warn(
                  RteConfGenMessageDescription.getFormattedMesssage("210_0", ((OSEVENTType) object).getValue()).trim());
            }
          }
        }
        if (!this.newPositioning) {
          position++;
        }
      }
    }
    else {
      updateISRProcessToTaskMap(ostask);
    }
  }

  /**
   * @param ostask
   */
  private void updateISRProcessToTaskMap(final OSTASKType ostask) {
    for (Object object : ostask.getOSEVENTOrOSPROCESS()) {
      OSPROCESSType osProcess = null;
      OSEVENTType osEvent = null;
      String eventName = "";
      if (object instanceof OSPROCESSType) {
        osProcess = (OSPROCESSType) object;
        eventName = osProcess.getEvent() != null ? osProcess.getEvent().trim() : "";
      }
      else if (object instanceof OSEVENTType) {
        osEvent = (OSEVENTType) object;
        eventName = osEvent.getValue().trim();
      }
      this.isrEventToTaskMap.put(eventName, ostask.getOSTASKNAME());
    }
  }

  private void updateInstanceMap(final String shortName, final TischedEvent tischedEvent) {
    List<TischedEvent> list = getInstanceEventMap().get(shortName);
    if (list != null) {
      if (!list.contains(tischedEvent)) {
        list.add(tischedEvent);
      }
    }
    else {
      List<TischedEvent> eventList = new ArrayList<TischedEvent>();
      eventList.add(tischedEvent);
      getInstanceEventMap().put(shortName, eventList);
    }
  }

  private void updateOsSwcMapFromFromSwcBswMappings() {
    for (SwcBswMapping map : getListOfSwcBswMappings()) {
      if ((map.getBswBehavior() != null) && (map.getSwcBehavior() != null)) {
        EObject swctype = map.getSwcBehavior().eContainer();
        List<SwComponentPrototype> list = getSwcMap().get(swctype);
        if ((list != null) && !list.isEmpty()) {
          TischedEvent event = getTischedEvent(map);
          String osAppName = (event != null) ? event.getTischedTask().getMappedOsApplication() : "";
          if ((osAppName != null) && !osAppName.isEmpty()) {
            List<SwComponentPrototype> listOfSwps = getOsAppSwCompPrototypesMap().get(osAppName);
            if (listOfSwps == null) {
              List<SwComponentPrototype> l = new ArrayList<>();
              l.addAll(list);
              getOsAppSwCompPrototypesMap().put(osAppName, list);
            }
            else {
              listOfSwps.addAll(list);
            }
          }
        }
      }
    }
  }

  private TischedEvent getTischedEvent(final SwcBswMapping map) {
    TischedEvent tischedEvent = null;
    for (TischedEvent event : getTischedEvents()) {
      if (event.getEvent() != null) {
        EObject eContainer = event.getEvent().eContainer();
        if ((eContainer == map.getBswBehavior()) && (event.getTischedTask() != null)) {
          return event;
        }
      }
    }
    return tischedEvent;
  }

  private void updateTischedTask(final String taskName, final int taskPriority, final String taskCycle,
      final String taskChain, final TischedEvent tischedEvent, final boolean isErrorLogPrinted,
      final boolean bOsTaskHooks, final String taskPPort, final String taskBSWRelTrigger,
      final OSTASKEVENTTYPEType ostaskeventtypeType, final OSTASKTYPEType ostasktypeType)
      throws Exception {
    boolean taskExists = false;

    for (EcucContainerValue ecucValue : geOsTaskMap().values()) {
      if (ecucValue.getShortName().equals(taskName)) {
        taskExists = true;
        TischedTask tischedTask = null;
        for (TischedTask tischedTask1 : getTischedTasks()) {
          if (tischedTask1.getShortName().equals(taskName)) {
            tischedTask = tischedTask1;
            break;
          }
        }
        if (tischedTask == null) {
          tischedTask = new TischedTask();
          tischedTask.setShortName(taskName);
          tischedTask.setTaskInstance(ecucValue);
          tischedTask.setOsRtetaskhooks(bOsTaskHooks);
          if ((taskCycle != null) && !taskCycle.isEmpty()) {
            tischedTask.setTaskCycle(taskCycle);
          }
          if ((taskChain != null) && !taskChain.isEmpty()) {
            EcucContainerValue ecucContainerValue = geOsTaskMap().get(taskChain);
            if (ecucContainerValue != null) {
              tischedTask.setTaskChain(ecucContainerValue);
            }
            else {
              LOGGER.warn("MM_DCS_RTECONFGEN_DEBUG : " + taskChain + " does not exist");
            }
          }
          if ((taskPPort != null) && !taskPPort.isEmpty()) {
            tischedTask.setOsPPortPrototype(taskPPort);
          }
          if ((taskBSWRelTrigger != null) && !taskBSWRelTrigger.isEmpty()) {
            tischedTask.setOsBSWReleasedTrigger(taskBSWRelTrigger);
          }
          if (ostaskeventtypeType != null) {
            tischedTask.setTischedTaskeventtypeenum(TischedTaskEventTypeEnum.fromValue(ostaskeventtypeType.value()));
          }
          if (ostasktypeType != null) {
            tischedTask.setTischedTaskTypeEnum(TischedTaskTypeEnum.fromValue(ostasktypeType.value()));
          }
          if (this.osTaskAppRef != null) {
            setMappedOsApplication(ecucValue, tischedTask);
          }
          if ((this.osAlarmAction != null) && (this.osAlarmActivateTask != null) &&
              (this.osAlarmActivateTaskRef != null)) {
            setTaskActivationEnumForOsAlarm(ecucValue, tischedTask);
          }
          if ((tischedTask.getTaskActivationEnum() == null) && (this.osScheduleTableExpiryPoint != null) &&
              (this.osScheduleTableTaskActivation != null) && (this.osScheduleTableActivateTaskRef != null)) {
            setTaskActivationEnumForOsScheduleTable(ecucValue, tischedTask);
          }
          getTischedTasks().add(tischedTask);
        }
        gettaskEventMap().put(tischedEvent.getShortName(), tischedTask.getShortName());
        tischedTask.getEvents().add(tischedEvent);
        tischedEvent.setTischedTask(tischedTask);


      }
    }
    if (!taskExists && !isErrorLogPrinted)

    {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("307_0", taskName).trim());
    }
  }

  private void setMappedOsApplication(final EcucContainerValue ecucValue, final TischedTask tischedTask)
      throws Exception {
    boolean exists = false;
    for (EcucContainerValue value : getListOfOsApps()) {
      for (EcucAbstractReferenceValue value2 : value.getReferenceValues()) {
        if (value2.getDefinition().equals(this.osTaskAppRef) && (value2 instanceof EcucReferenceValue)) {
          EcucReferenceValue v = (EcucReferenceValue) value2;
          if (v.getValue().getShortName().equals(ecucValue.getShortName())) {
            tischedTask.setMappedOsApplication(value.getShortName());
            tischedTask.setOsAppInstance(value);
            exists = true;
            return;
          }
        }
      }
    }
    if (!exists) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("211_0", tischedTask.getShortName()).trim());
    }
  }

  private boolean setTaskActivationEnumForOsAlarm(final EcucContainerValue ecucValue, final TischedTask tischedTask) {
    boolean exists = false;
    for (EcucContainerValue value : getListOfOsAlarms()) {
      for (EcucContainerValue value1 : value.getSubContainers()) {
        if (value1.getDefinition().equals(this.osAlarmAction)) {
          for (EcucContainerValue value2 : value1.getSubContainers()) {
            if (value2.getDefinition().equals(this.osAlarmActivateTask)) {
              for (EcucAbstractReferenceValue value3 : value2.getReferenceValues()) {
                if (value3.getDefinition().equals(this.osAlarmActivateTaskRef) &&
                    (value3 instanceof EcucReferenceValue) &&
                    ((EcucReferenceValue) value3).getValue().getShortName().equals(ecucValue.getShortName())) {
                  tischedTask.setTaskActivationEnum(TaskActivationEnum.OS_ALARM);
                  tischedTask.setAlarmInstance(value);
                  return true;
                }
              }
            }
          }
        }
      }
    }
    return exists;
  }

  private boolean setTaskActivationEnumForOsScheduleTable(final EcucContainerValue ecucValue,
      final TischedTask tischedTask) {
    boolean exists = false;
    for (EcucContainerValue value : getListOfOsScheduleTables()) {
      for (EcucContainerValue value1 : value.getSubContainers()) {
        if (value1.getDefinition().equals(this.osScheduleTableExpiryPoint)) {
          for (EcucContainerValue value2 : value1.getSubContainers()) {
            if (value2.getDefinition().equals(this.osScheduleTableTaskActivation)) {
              for (EcucAbstractReferenceValue value3 : value2.getReferenceValues()) {
                if (value3.getDefinition().equals(this.osScheduleTableActivateTaskRef) &&
                    (value3 instanceof EcucReferenceValue) &&
                    ((EcucReferenceValue) value3).getValue().getShortName().equals(ecucValue.getShortName())) {
                  tischedTask.setTaskActivationEnum(TaskActivationEnum.OS_SCHEDULE_TABLE);
                  tischedTask.setOsScheduleTableInstance(value);
                  return true;
                }
              }
            }
          }
        }
      }
    }
    return exists;
  }

  private void updateTischedInstanceForRteEvent(final TischedEvent tischedEvent, final String eventName,
      final TischedComponentInstance tischedComponentInstance, final boolean compoNameUnkNown) {
    String key =
        tischedComponentInstance.getTischedComponent().getShortName() + tischedComponentInstance.getShortName();
    if ((tischedEvent.getEvent() == null) || compoNameUnkNown) {
      tischedEvent.setEventTypeEnum(EventEnum.RTEEVENT);
      SwComponentPrototype componentInstance = (SwComponentPrototype) tischedComponentInstance.getComponentInstance();
      if (componentInstance.getType() instanceof AtomicSwComponentType) {
        AtomicSwComponentType aswt = (AtomicSwComponentType) componentInstance.getType();
        for (SwcInternalBehavior swb : aswt.getInternalBehaviors()) {
          for (RTEEvent rteEvent : swb.getEvents()) {
            if (rteEvent.getShortName().equals(eventName)) {
              if (tischedEvent.getEvent() == null) {
                tischedEvent.setEvent(rteEvent);
              }
              tischedEvent.setInstance(componentInstance);
              addEventToComponentInstanceMap(key, tischedEvent);
            }
          }
        }
        if (tischedEvent.getEvent() == null) {
          updateEvent(tischedEvent, eventName);
        }
      }
    }
    else {
      addEventToComponentInstanceMap(key, tischedEvent);
    }
  }

  /**
   * @param key
   * @param tischedEvent
   */
  private void addEventToComponentInstanceMap(final String key, final TischedEvent tischedEvent) {
    List<TischedEvent> list = this.componentInstaceToEventsMap.get(key);
    if ((list != null) && !list.isEmpty()) {
      list.add(tischedEvent);
    }
    else {
      List<TischedEvent> list1 = new ArrayList<>();
      list1.add(tischedEvent);
      this.componentInstaceToEventsMap.put(key, list1);
    }
  }

  private void updateTischedInstanceForRteEvent(final TischedEvent tischedEvent, final String eventName,
      final SwComponentPrototype componentInstance)
      throws Exception {
    if ((tischedEvent.getEvent() == null)) {
      tischedEvent.setEventTypeEnum(EventEnum.RTEEVENT);
      if (componentInstance.getType() != null) {
        if (componentInstance.getType() instanceof AtomicSwComponentType) {
          AtomicSwComponentType aswt = (AtomicSwComponentType) componentInstance.getType();
          for (SwcInternalBehavior swb : aswt.getInternalBehaviors()) {
            for (RTEEvent rteEvent : swb.getEvents()) {
              if (rteEvent.getShortName().equals(eventName)) {
                if (tischedEvent.getEvent() == null) {
                  tischedEvent.setEvent(rteEvent);
                }
              }
            }
          }
          if (tischedEvent.getEvent() == null) {
            updateEvent(tischedEvent, eventName);
          }
        }
        tischedEvent.setInstance(componentInstance);
      }
      else {
        LOGGER
            .warn(RteConfGenMessageDescription.getFormattedMesssage("212_0", componentInstance.getShortName()).trim());
      }
    }
  }

  private void updateEvent(final TischedEvent tischedEvent, final String eventName) {
    if (this.listOfRteEvents != null) {
      for (RTEEvent rteEvent : this.listOfRteEvents) {
        if (rteEvent.getShortName().equals(eventName)) {
          AtomicSwComponentType aswt =
              (AtomicSwComponentType) ((SwcInternalBehavior) rteEvent.eContainer()).eContainer();
          for (TischedComponentInstance tic : getTischedComponentInstances().values()) {
            String key = tic.getTischedComponent().getShortName() + tic.getShortName();
            if (aswt.getShortName().equals(tic.getTischedComponent().getShortName()) &&
                (tic.getSwComponentInstanceTypeEnum() != SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION)) {
              tischedEvent.setEvent(rteEvent);
              addEventToComponentInstanceMap(key, tischedEvent);
            }
          }
        }
      }
    }
  }

  private void updateTischedInstanceForBswEvent(final TischedEvent tischedEvent, final String eventName,
      final TischedComponentInstance tischedComponentInstance, final boolean compoNameUnkNown) {
    String key =
        tischedComponentInstance.getTischedComponent().getShortName() + tischedComponentInstance.getShortName();
    if ((tischedEvent.getEvent() == null) || compoNameUnkNown) {
      tischedEvent.setEventTypeEnum(EventEnum.BSWEVENT);
      BswModuleDescription bswMd = (BswModuleDescription) tischedComponentInstance.getTischedComponent().getComponent();
      for (EObject eObject : bswMd.getInternalBehaviors()) {
        BswInternalBehavior swb = (BswInternalBehavior) eObject;
        for (BswEvent bswEvent : swb.getEvents()) {
          if (bswEvent.getShortName().equals(eventName)) {
            if (tischedEvent.getEvent() == null) {
              tischedEvent.setEvent(bswEvent);
            }
            tischedEvent.setInstance(tischedComponentInstance.getComponentInstance());
            addEventToComponentInstanceMap(key, tischedEvent);
          }
        }
      }
    }
    else {
      addEventToComponentInstanceMap(key, tischedEvent);
    }
  }

  private void updateTischedInstanceForBswEvent(final TischedEvent tischedEvent, final String eventName,
      final BswImplementation bswImplementation)
      throws Exception {
    if (tischedEvent.getEvent() == null) {
      tischedEvent.setEventTypeEnum(EventEnum.BSWEVENT);
      BswModuleDescription bswMd = (BswModuleDescription) bswImplementation.getBehavior().eContainer();
      if (bswMd != null) {
        for (BswInternalBehavior swb : bswMd.getInternalBehaviors()) {
          for (BswEvent bswEvent : swb.getEvents()) {
            if (bswEvent.getShortName().equals(eventName)) {
              if (tischedEvent.getEvent() == null) {
                tischedEvent.setEvent(bswEvent);
              }
            }
          }
        }
      }
      else {
        LOGGER
            .warn(RteConfGenMessageDescription.getFormattedMesssage("213_0", bswImplementation.getShortName()).trim());
      }
    }
  }

  private void doASWComponentMapping(final TischedComponent tischedComponent, final SwComponentType swComponentType,
      final String componentTypeRef, final List<String> applicationIds, final boolean generateMemmapHeader)
      throws Exception {
    if (swComponentType.eIsProxy()) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("214_0", tischedComponent.getPackagePath()).trim());
    }
    else if (swComponentType.getShortName().equals(tischedComponent.getShortName())) {
      updateTischedComponentInstanceForASWComponent(tischedComponent, tischedComponent.getPackagePath(),
          generateMemmapHeader);
      List<SwComponentPrototype> list = getSwcMap().get(swComponentType);
      if ((list != null) && !list.isEmpty()) {
        for (String osapp : applicationIds) {
          boolean osappExists = false;
          for (EcucContainerValue ecucValue : getListOfOsApps()) {
            if (ecucValue.getShortName().equals(osapp.trim())) {
              List<SwComponentPrototype> listOfSwps = getOsAppSwCompPrototypesMap().get(osapp.trim());
              if (listOfSwps == null) {
                List<SwComponentPrototype> l = new ArrayList<>();
                l.addAll(list);
                getOsAppSwCompPrototypesMap().put(osapp.trim(), l);
              }
              else {
                listOfSwps.addAll(list);
              }
              osappExists = true;
            }
          }
          if (!osappExists) {
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("215_0", osapp.trim()).trim());
          }
        }
      }
    }
    else {
      LOGGER.warn(RteConfGenMessageDescription
          .getFormattedMesssage("216_0", tischedComponent.getShortName(), tischedComponent.getPackagePath()).trim());
    }
  }

  private void doASWComponentMappingOnly(final String swComponent, final String componentTypeRef,
      final List<String> applicationIds, final boolean generateMemmapHeader)
      throws Exception {
    if (swComponent != null) {
      TischedComponent tischedComponent = new TischedComponent();
      tischedComponent.setShortName(swComponent.trim());
      tischedComponent.setPackagePath(componentTypeRef == null ? "" : componentTypeRef.trim());
      SwComponentType swComponentType =
          (tischedComponent.getPackagePath() != null) && !tischedComponent.getPackagePath().isEmpty()
              ? getSwComponentType(tischedComponent.getPackagePath(), tischedComponent)
              : getSwComponentTypeFromShortName(tischedComponent.getShortName(), tischedComponent);
      if (swComponentType != null) {
        doASWComponentMapping(tischedComponent, swComponentType, componentTypeRef, applicationIds,
            generateMemmapHeader);
      }
      else {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("214_0",
            ((tischedComponent.getPackagePath() == null) || tischedComponent.getPackagePath().isEmpty()
                ? tischedComponent.getShortName() : tischedComponent.getPackagePath()))
            .trim());
      }
      if (tischedComponent.getSwComponentTypeEnum() != null) {
        getTischedComponents().add(tischedComponent);
      }
    }
  }

  private void doComponentMapping(final String swComponent, final String componentTypeRef,
      final List<String> applicationIds, final boolean generateMemmapHeader)
      throws Exception {
    if (swComponent != null) {
      TischedComponent tischedComponent = new TischedComponent();
      tischedComponent.setShortName(swComponent.trim());
      tischedComponent.setPackagePath(componentTypeRef == null ? "" : componentTypeRef.trim());
      SwComponentType swComponentType =
          (tischedComponent.getPackagePath() != null) && !tischedComponent.getPackagePath().isEmpty()
              ? getSwComponentType(tischedComponent.getPackagePath(), tischedComponent)
              : getSwComponentTypeFromShortName(tischedComponent.getShortName(), tischedComponent);
      if (swComponentType != null) {
        doASWComponentMapping(tischedComponent, swComponentType, componentTypeRef, applicationIds,
            generateMemmapHeader);
      }
      else {
        updateTischedComponentInstanceForBSWComponent(tischedComponent, tischedComponent.getPackagePath());
      }
      if (tischedComponent.getSwComponentTypeEnum() != null) {
        getTischedComponents().add(tischedComponent);
      }
    }
  }

  private void updateTischedComponentInstanceForASWComponent(final TischedComponent tischedComponent,
      final String pkgPath, final boolean generateMemmapHeader)
      throws Exception {
    boolean componentInstanceExists = false;
    for (SwComponentPrototype swComponentPrototype : getListOfSWComponentProtoTypes()) {
      SwComponentType swComponentType = getSwComponentType(swComponentPrototype.getType(), pkgPath);
      if (swComponentType != null) {
        tischedComponent.setComponent(swComponentType);
        tischedComponent.setSwComponentTypeEnum(SwComponentTypeEnum.ASW);
        if (getTischedComponentInstances().get(swComponentPrototype.getShortName()) == null) {
          TischedComponentInstance tischedComponentInstance = new TischedComponentInstance();
          tischedComponentInstance.setShortName(swComponentPrototype.getShortName());
          tischedComponentInstance.setPackagePath("");
          tischedComponentInstance.setComponentInstance(swComponentPrototype);
          tischedComponentInstance.setSwComponentInstanceTypeEnum(SwComponentInstanceTypeEnum.SW_COMPONENT_PROTOTYPE);
          tischedComponentInstance.setTischedComponent(tischedComponent);
          tischedComponentInstance.setGenerateMemmapHeader(generateMemmapHeader);
          getTischedComponentInstances().put(swComponentPrototype.getShortName(), tischedComponentInstance);
        }
        componentInstanceExists = true;
      }
    }
    if (!componentInstanceExists) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("217_0", tischedComponent.getPackagePath()).trim());
    }
  }

  private SwComponentType getSwComponentType(final SwComponentType swct, final String pkgPath) {
    SwComponentType st = null;
    if (this.listOfComponentTypes != null) {
      for (SwComponentType swc : this.listOfComponentTypes) {
        if (swct == swc) {
          String path = EcoreResourceUtil.getURI(swc).fragment().split("\\?")[0];
          if (path.equals(pkgPath)) {
            return swc;
          }
        }
      }
    }
    return st;
  }

  private void updateTischedComponentInstanceForBSWComponent(final TischedComponent tischedComponent,
      final String arPkgPath)
      throws Exception {
    BswModuleDescription bswDesc =
        (tischedComponent.getPackagePath() != null) && !tischedComponent.getPackagePath().isEmpty()
            ? getBSWModuleDesc(arPkgPath)
            : getBSWModuleDescFromShortName(tischedComponent.getShortName(), tischedComponent);
    if (bswDesc != null) {
      if (bswDesc.eIsProxy()) {
        LOGGER
            .warn(RteConfGenMessageDescription.getFormattedMesssage("214_0", tischedComponent.getPackagePath()).trim());
      }
      else if (bswDesc.getShortName().equals(tischedComponent.getShortName())) {
        boolean componentInstanceExists = false;
        if (this.listOfbswi != null) {
          for (BswImplementation bswImplementation : this.listOfbswi) {
            if (bswImplementation.getBehavior() != null) {
              bswDesc = getBswModuleDescription((BswModuleDescription) bswImplementation.getBehavior().eContainer(),
                  tischedComponent.getPackagePath());
              if (bswDesc != null) {
                tischedComponent.setSwComponentTypeEnum(SwComponentTypeEnum.BSW);
                tischedComponent.setComponent(bswDesc);
                TischedComponentInstance tischedComponentInstance2 =
                    getTischedComponentInstances().get(bswImplementation.getShortName());
                if (tischedComponentInstance2 == null) {
                  TischedComponentInstance tischedComponentInstance = new TischedComponentInstance();
                  tischedComponentInstance.setShortName(bswImplementation.getShortName());
                  tischedComponentInstance.setPackagePath("");
                  tischedComponentInstance.setComponentInstance(bswImplementation);
                  tischedComponentInstance
                      .setSwComponentInstanceTypeEnum(SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION);
                  tischedComponentInstance.setTischedComponent(tischedComponent);
                  getTischedComponentInstances().put(bswImplementation.getShortName(), tischedComponentInstance);
                }
                componentInstanceExists = true;
              }
            }
          }
        }
        if (!componentInstanceExists) {
          LOGGER.warn(
              RteConfGenMessageDescription.getFormattedMesssage("218_0", tischedComponent.getPackagePath()).trim());
        }
      }
      else {
        LOGGER.warn(RteConfGenMessageDescription
            .getFormattedMesssage("219_0", tischedComponent.getShortName(), tischedComponent.getPackagePath()).trim());
      }
    }
    else {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("214_0",
          ((arPkgPath == null) || arPkgPath.isEmpty() ? tischedComponent.getShortName() : arPkgPath)).trim());
    }
  }

  private void updateTischedComponentInstanceForBSWComponentOnly(final String swComponent,
      final String componentTypeRef)
      throws Exception {
    if (swComponent != null) {
      if ((componentTypeRef != null) && !componentTypeRef.isEmpty()) {
        TischedComponent tischedComponent = new TischedComponent();
        tischedComponent.setPackagePath(componentTypeRef.trim());
        String implpath = getRteBswImplMap().get(swComponent);
        if ((implpath != null) && !implpath.isEmpty()) {
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("243_0", swComponent).trim());
          if (!implpath.equals(componentTypeRef.trim())) {
            LOGGER.warn(RteConfGenMessageDescription
                .getFormattedMesssage("247_0", swComponent, implpath, tischedComponent.getPackagePath()).trim());
            return;
          }
          getConfiguredBswInstanceSet().add(swComponent);
        }
        if ((implpath == null) && getRteBswImplMap().containsValue(componentTypeRef.trim())) {
          String bswImplName = getBswImplName(tischedComponent.getPackagePath());
          LOGGER.warn(RteConfGenMessageDescription
              .getFormattedMesssage("249_0", tischedComponent.getPackagePath(), bswImplName, swComponent).trim());
          return;
        }
        BswImplementation bswImplementation2 = getBSWImplementation(tischedComponent.getPackagePath());
        if (bswImplementation2 != null) {
          BswModuleDescription bswDesc =
              getBSWModuleDescFromShortName(bswImplementation2.getShortName(), tischedComponent);
          if (bswDesc != null) {
            if (bswDesc.eIsProxy()) {
              LOGGER.warn(
                  RteConfGenMessageDescription.getFormattedMesssage("214_0", tischedComponent.getPackagePath()).trim());
            }
            else {
              tischedComponent.setSwComponentTypeEnum(SwComponentTypeEnum.BSW);
              tischedComponent.setComponent(bswDesc);
              TischedComponentInstance tischedComponentInstance2 = getTischedComponentInstances().get(swComponent);
              if (tischedComponentInstance2 == null) {
                TischedComponentInstance tischedComponentInstance = new TischedComponentInstance();
                tischedComponentInstance.setShortName(swComponent);
                tischedComponentInstance.setPackagePath("");
                tischedComponentInstance.setComponentInstance(bswImplementation2);
                tischedComponentInstance.setSwComponentInstanceTypeEnum(SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION);
                tischedComponentInstance.setTischedComponent(tischedComponent);
                getTischedComponentInstances().put(swComponent, tischedComponentInstance);
              }
              // bswImplPath.add(tischedComponent.getPackagePath());
              if (!getRteBswImplMap().containsValue(componentTypeRef.trim())) {
                getRteBswImplMap().put(swComponent, componentTypeRef.trim());
              }
            }
            if (tischedComponent.getSwComponentTypeEnum() != null) {
              getTischedComponents().add(tischedComponent);
            }
          }
          else {
            LOGGER.warn(
                RteConfGenMessageDescription.getFormattedMesssage("244_0", bswImplementation2.getShortName()).trim());
          }
        }
        else {
          LOGGER.warn(
              RteConfGenMessageDescription.getFormattedMesssage("214_0", tischedComponent.getPackagePath()).trim());
        }
      }
      else {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("245_0", swComponent).trim());
      }
    }
  }

  private BswModuleDescription getBswModuleDescription(final BswModuleDescription bswDesc, final String pkgPath) {
    BswModuleDescription bswmd = null;
    if (this.listOfBswmd != null) {
      for (BswModuleDescription bsw : this.listOfBswmd) {
        if (bswDesc == bsw) {
          String path = EcoreResourceUtil.getURI(bsw).fragment().split("\\?")[0];
          if (path.equals(pkgPath)) {
            return bsw;
          }
        }
      }
    }
    return bswmd;
  }

  private SwComponentType getSwComponentType(final String pkgPath, final TischedComponent tischedComponent) {
    SwComponentType swt = null;
    if (this.listOfComponentTypes != null) {
      for (SwComponentType swc : this.listOfComponentTypes) {
        String path = EcoreResourceUtil.getURI(swc).fragment().split("\\?")[0];
        if (path.equals(pkgPath)) {
          tischedComponent.setShortName(swc.getShortName());
          return swc;
        }
      }
    }
    return swt;
  }

  private SwComponentType getSwComponentTypeFromShortName(final String shortName,
      final TischedComponent tischedComponent) {
    SwComponentType swt = null;
    if (this.listOfSWComponentProTypes != null) {
      for (SwComponentPrototype swc : this.listOfSWComponentProTypes) {
        if (swc.getShortName().equals(shortName) && (swc.getType() != null)) {
          tischedComponent.setShortName(swc.getType().getShortName());
          tischedComponent.setPackagePath(EcoreResourceUtil.getURI(swc.getType()).fragment().split("\\?")[0]);
          return swc.getType();
        }
      }
    }
    return swt;
  }

  private BswModuleDescription getBSWModuleDesc(final String pkgPath) {
    BswModuleDescription bswmd = null;
    if (this.listOfBswmd != null) {
      for (BswModuleDescription bsw : this.listOfBswmd) {
        String path = EcoreResourceUtil.getURI(bsw).fragment().split("\\?")[0];
        if (path.equals(pkgPath)) {
          return bsw;
        }
      }
    }
    return bswmd;
  }

  private BswImplementation getBSWImplementation(final String pkgPath) {
    BswImplementation bswi = null;
    if (this.listOfbswi != null) {
      for (BswImplementation bsw : this.listOfbswi) {
        String path = EcoreResourceUtil.getURI(bsw).fragment().split("\\?")[0];
        if (path.equals(pkgPath)) {
          return bsw;
        }
      }
    }
    return bswi;
  }

  private BswModuleDescription getBSWModuleDescFromShortName(final String shortName,
      final TischedComponent tischedComponent) {
    BswModuleDescription bswmd = null;
    if (this.listOfbswi != null) {
      BswImplementation bswImpl1 = getBSWImplementation(tischedComponent.getPackagePath());
      if ((bswImpl1 != null) && bswImpl1.getShortName().equals(shortName) && (bswImpl1.getBehavior() != null) &&
          (bswImpl1.getBehavior().eContainer() instanceof BswModuleDescription)) {
        BswModuleDescription bswMod = (BswModuleDescription) bswImpl1.getBehavior().eContainer();
        tischedComponent.setShortName(bswMod.getShortName());
        tischedComponent.setPackagePath(EcoreResourceUtil.getURI(bswMod).fragment().split("\\?")[0]);
        return bswMod;
      }
      for (BswImplementation bswImpl : this.listOfbswi) {
        if (bswImpl.getShortName().equals(shortName) && (bswImpl.getBehavior() != null) &&
            (bswImpl.getBehavior().eContainer() instanceof BswModuleDescription)) {
          BswModuleDescription bswMod = (BswModuleDescription) bswImpl.getBehavior().eContainer();
          tischedComponent.setShortName(bswMod.getShortName());
          tischedComponent.setPackagePath(EcoreResourceUtil.getURI(bswMod).fragment().split("\\?")[0]);
          return bswMod;
        }
      }
    }
    return bswmd;
  }

  private void updateListOfOsTasksToList(final EList<EcucModuleConfigurationValuesRefConditional> ecucValues) {
    EcucParamConfContainerDef eObject = GenerateArxmlUtil.getEObject(this.iProject, this.osTaskarPkgPath,
        EcucParamConfContainerDef.class, this.outputPath);
    if (eObject != null) {

      for (EcucModuleConfigurationValuesRefConditional ecucModuleValue : ecucValues) {
        List<EcucContainerValue> l = new ArrayList<>();
        l.addAll(ecucModuleValue.getEcucModuleConfigurationValues().getContainers());
        for (EcucContainerValue ecucValue : l) {
          if (ecucValue.getDefinition().equals(eObject)) {
            geOsTaskMap().put(ecucValue.getShortName(), ecucValue);
          }
        }
      }
    }
  }

  private void updateListOfOsAlarmsToList(final EList<EcucModuleConfigurationValuesRefConditional> ecucValues) {
    EcucParamConfContainerDef eObject = GenerateArxmlUtil.getEObject(this.iProject, this.osAlarmarPkgPath,
        EcucParamConfContainerDef.class, this.outputPath);
    if (eObject != null) {
      for (EcucModuleConfigurationValuesRefConditional ecucModuleValue : ecucValues) {
        List<EcucContainerValue> l = new ArrayList<>();
        l.addAll(ecucModuleValue.getEcucModuleConfigurationValues().getContainers());
        for (EcucContainerValue ecucValue : l) {
          if (ecucValue.getDefinition().equals(eObject)) {
            getListOfOsAlarms().add(ecucValue);
          }
        }
      }
    }
  }

  private void updateListOfOsAppsToList(final EList<EcucModuleConfigurationValuesRefConditional> ecucValues)
      throws Exception {
    EcucParamConfContainerDef eObject = GenerateArxmlUtil.getEObject(this.iProject, this.osApparPkgPath,
        EcucParamConfContainerDef.class, this.outputPath);
    EcucReferenceDef osAppEcucPartitionRef = GenerateArxmlUtil.getEObject(this.iProject,
        this.osApparPkgPath + "/OsAppEcucPartitionRef", EcucReferenceDef.class, this.outputPath);
    if (osAppEcucPartitionRef == null) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("308_0", this.osApparPkgPath).trim());
    }
    if (eObject != null) {
      for (EcucModuleConfigurationValuesRefConditional ecucModuleValue : ecucValues) {
        List<EcucContainerValue> l = new ArrayList<>();
        l.addAll(ecucModuleValue.getEcucModuleConfigurationValues().getContainers());
        for (EcucContainerValue ecucValue : l) {
          if (ecucValue.getDefinition().equals(eObject)) {
            getListOfOsApps().add(ecucValue);
            if ((osAppEcucPartitionRef != null)) {
              EcucContainerValue ecucContainerValue =
                  GenerateArxmlUtil.getEcucContainerValue(ecucValue, osAppEcucPartitionRef);
              if (ecucContainerValue != null) {
                this.osAppEcuPartitionMap.put(ecucValue.getShortName(), ecucContainerValue);
              }
              else {
                LOGGER
                    .warn(RteConfGenMessageDescription.getFormattedMesssage("220_0", ecucValue.getShortName()).trim());
              }
            }
          }
        }
      }
    }
  }

  private void updateListOfOsScheduleTablesToList(final EList<EcucModuleConfigurationValuesRefConditional> ecucValues) {
    EcucParamConfContainerDef eObject = GenerateArxmlUtil.getEObject(this.iProject, this.osScheduleTablearPkgPath,
        EcucParamConfContainerDef.class, this.outputPath);
    if (eObject != null) {
      for (EcucModuleConfigurationValuesRefConditional ecucModuleValue : ecucValues) {
        List<EcucContainerValue> l = new ArrayList<>();
        l.addAll(ecucModuleValue.getEcucModuleConfigurationValues().getContainers());
        for (EcucContainerValue ecucValue : ecucModuleValue.getEcucModuleConfigurationValues().getContainers()) {
          if (ecucValue.getDefinition().equals(eObject)) {
            getListOfOsScheduleTables().add(ecucValue);
          }
        }
      }
    }
  }

  private void updateListOfRteBswModuleInstancesToList(
      final EList<EcucModuleConfigurationValuesRefConditional> ecucValues) {
    EcucParamConfContainerDef eObject = GenerateArxmlUtil.getEObject(this.iProject, this.rteBswModuleInstancePkgPath,
        EcucParamConfContainerDef.class, this.outputPath);
    if (eObject != null) {
      for (EcucModuleConfigurationValuesRefConditional ecucModuleValue : ecucValues) {
        List<EcucContainerValue> l = new ArrayList<>();
        l.addAll(ecucModuleValue.getEcucModuleConfigurationValues().getContainers());
        for (EcucContainerValue ecucValue : ecucModuleValue.getEcucModuleConfigurationValues().getContainers()) {
          if (ecucValue.getDefinition().equals(eObject)) {
            EList<EcucAbstractReferenceValue> referenceValues = ecucValue.getReferenceValues();
            for (EcucAbstractReferenceValue erV : referenceValues) {
              EcucReferenceValue eRv = (EcucReferenceValue) erV;
              if (!eRv.eIsProxy()) {
                String implPkgPath = EcoreResourceUtil.getURI(eRv.getValue()).fragment().split("\\?")[0].trim();
                getRteBswImplMap().put(ecucValue.getShortName(), implPkgPath);
              }
            }
            EList<EcucContainerValue> subContainers = ecucValue.getSubContainers();
            for (EcucContainerValue ecucCon : subContainers) {
              EcucContainerDef definition = ecucCon.getDefinition();
              if (definition.getShortName().equals("RteBswEventToTaskMapping")) {
                List<String> list = getRteBswModuleInstanceMap().get(ecucValue.getShortName());
                if (list == null) {
                  list = new ArrayList<>();
                  list.add(ecucCon.getShortName());
                  getRteBswModuleInstanceMap().put(ecucValue.getShortName(), list);
                }
                else {
                  list.add(ecucCon.getShortName());
                }
              }
            }
          }
        }
      }
    }
  }

  private void prepareForRipsInvocationHandlerFnc() throws Exception {
    FlatMap flatMap = this.rootSwCompositionProtoType.getFlatMap();
    EList<SwConnector> connectors = this.rootSwCompositionProtoType.getSoftwareComposition().getConnectors();
    Map<String, String> dcOpMap = new HashMap<String, String>();
    Map<String, String> dcIpMap = new HashMap<String, String>();
    for (SwConnector dc : connectors) {
      if ((dc instanceof DelegationSwConnector) && (((DelegationSwConnector) dc).getInnerPort() != null) &&
          (((DelegationSwConnector) dc).getOuterPort() != null)) {
        PortInCompositionTypeInstanceRef innerPort = ((DelegationSwConnector) dc).getInnerPort();
        PortPrototype outerPort = ((DelegationSwConnector) dc).getOuterPort();
        if (!outerPort.eIsProxy() && !innerPort.eIsProxy()) {
          PortPrototype port = (innerPort instanceof PPortInCompositionInstanceRef)
              ? ((PPortInCompositionInstanceRef) innerPort).getTargetPPort()
              : ((RPortInCompositionInstanceRef) innerPort).getTargetRPort();
          if (!port.eIsProxy()) {
            String fragment = EcoreUtil.getURI(outerPort).fragment();
            fragment = fragment.substring(0, fragment.lastIndexOf("?type="));
            dcOpMap.put(outerPort.getShortName(), fragment);
            dcIpMap.put(outerPort.getShortName(),
                port.getShortName() + "<->" + port.getSwComponentType().getShortName());
          }
          else {
            LOGGER.warn(RteConfGenMessageDescription
                .getFormattedMesssage("254_0", port.getShortName(), dc.getShortName()).trim());
          }
        }
        else {
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("255_0", dc.getShortName()).trim());
        }
      }
    }
    if (dcOpMap.isEmpty()) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("250_0").trim());
    }
    if (!dcOpMap.isEmpty()) {
      Map<String, String> opMap = new HashMap<>();
      List<RTEEvent> rteEvents = new ArrayList<>();
      EList<FlatInstanceDescriptor> instances = flatMap.getInstances();
      for (FlatInstanceDescriptor fd : instances) {
        RtePluginProps rtePluginProps = fd.getRtePluginProps();
        if ((rtePluginProps != null) && (rtePluginProps.getAssociatedRtePlugin() != null) &&
            (fd.getEcuExtractReference() != null) && (fd.getEcuExtractReference().getContextElements() != null) &&
            (fd.getEcuExtractReference().getTarget() != null)) {
          EcucContainerValue associatedRtePlugin = rtePluginProps.getAssociatedRtePlugin();
          String uriStr = EcoreUtil.getURI(associatedRtePlugin).fragment();
          uriStr = uriStr.substring(0, uriStr.indexOf("?type"));
          if (associatedRtePlugin.getShortName().equals("CSXfrm") &&
              uriStr.equals("/RB/UBK/Project/EcucModuleConfigurationValuess/Rte_Rips_CSXfrm/CSXfrm") &&
              (fd.getEcuExtractReference().getTarget() instanceof ClientServerOperation)) {
            ClientServerOperation csop = ((ClientServerOperation) fd.getEcuExtractReference().getTarget());
            PortPrototype port = null;
            String portPgkPath = null;
            for (AtpFeature atpFeature : fd.getEcuExtractReference().getContextElements()) {
              if (atpFeature instanceof PortPrototype) {
                port = (PortPrototype) atpFeature;
                if (!port.eIsProxy()) {
                  String fragment = EcoreUtil.getURI(port).fragment();
                  portPgkPath = fragment.substring(0, fragment.lastIndexOf("?type="));
                  break;
                }
                LOGGER.warn(RteConfGenMessageDescription
                    .getFormattedMesssage("248_0", fd.getShortName(), EcoreUtil.getURI(port).fragment()).trim());
              }
            }
            if ((port != null) && (dcOpMap.get(port.getShortName()) != null) &&
                dcOpMap.get(port.getShortName()).equals(portPgkPath)) {
              String fragment = EcoreUtil.getURI(csop).fragment();
              fragment = fragment.substring(0, fragment.lastIndexOf("?type="));
              String ipName = dcIpMap.get(port.getShortName());
              opMap.put(ipName + "<->" + csop.getShortName(), fragment);
              LOGGER.info(RteConfGenMessageDescription
                  .getFormattedMesssage("138_0", port.getShortName(), csop.getShortName()).trim());
            }
            else if ((port != null) && port.eIsProxy()) {
              LOGGER.warn(RteConfGenMessageDescription
                  .getFormattedMesssage("248_0", fd.getShortName(), EcoreUtil.getURI(port).fragment()).trim());
            }
            else if (port != null) {
              LOGGER.warn(RteConfGenMessageDescription
                  .getFormattedMesssage("251_0", port.getShortName(), csop.getShortName()).trim());
            }
          }
        }
      }
      CompositionSwComponentType softwareComposition = this.rootSwCompositionProtoType.getSoftwareComposition();
      EList<SwComponentPrototype> components = softwareComposition.getComponents();
      for (SwComponentPrototype swci : components) {
        if ((swci.getType() instanceof AtomicSwComponentType) &&
            !((AtomicSwComponentType) swci.getType()).getInternalBehaviors().isEmpty()) {
          SwcInternalBehavior swcIb = ((AtomicSwComponentType) swci.getType()).getInternalBehaviors().get(0);
          for (RTEEvent rEvent : swcIb.getEvents()) {
            if (rEvent instanceof OperationInvokedEvent) {
              OperationInvokedEvent oie = (OperationInvokedEvent) rEvent;
              if ((oie.getOperation() != null) && (oie.getOperation().getTargetProvidedOperation() != null) &&
                  (oie.getOperation().getContextPPort() != null)) {
                String fragment = EcoreUtil.getURI(oie.getOperation().getTargetProvidedOperation()).fragment();
                fragment = fragment.substring(0, fragment.lastIndexOf("?type="));
                String ipSName = oie.getOperation().getContextPPort().getShortName();
                String swcSName = swcIb.getAtomicSwComponentType().getShortName();
                String clientServerOperationFr = opMap.get(ipSName + "<->" + swcSName + "<->" +
                    oie.getOperation().getTargetProvidedOperation().getShortName());
                if ((clientServerOperationFr != null) && fragment.equals(clientServerOperationFr)) {
                  rteEvents.add(oie);
                }
//                else {
//                  LOGGER.warn("MM_DCS_RTECONFGEN_: *** ClientServerOperation Pkg. Path '" + fragment +
//                      "' is not matching to '" + clientServerOperationFr +
//                      "' referenced in the FlatInstanceDescriptor");
//                }
              }
            }
            else if (rEvent instanceof AsynchronousServerCallReturnsEvent) {
              AsynchronousServerCallReturnsEvent ascre = (AsynchronousServerCallReturnsEvent) rEvent;
              AsynchronousServerCallPoint asynchronousServerCallPoint =
                  ascre.getEventSource().getAsynchronousServerCallPoint();
              if ((ascre.getEventSource() != null) && (asynchronousServerCallPoint != null) &&
                  (asynchronousServerCallPoint.getOperation() != null) &&
                  (asynchronousServerCallPoint.getOperation().getTargetRequiredOperation() != null) &&
                  (asynchronousServerCallPoint.getOperation().getContextRPort() != null)) {
                String fragment = EcoreUtil
                    .getURI(asynchronousServerCallPoint.getOperation().getTargetRequiredOperation()).fragment();
                fragment = fragment.substring(0, fragment.lastIndexOf("?type="));
                String ipSName = asynchronousServerCallPoint.getOperation().getContextRPort().getShortName();
                String swcSName = swcIb.getAtomicSwComponentType().getShortName();
                String clientServerOperationFr = opMap.get(ipSName + "<->" + swcSName + "<->" +
                    asynchronousServerCallPoint.getOperation().getTargetRequiredOperation().getShortName());
                if ((clientServerOperationFr != null) && fragment.equals(clientServerOperationFr)) {
                  rteEvents.add(ascre);
                }
              }
            }
          }
        }
        else if (swci.getType().eIsProxy()) {
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("252_0", swci.getShortName()).trim());
        }
      }
      if (rteEvents.isEmpty()) {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("253_0").trim());
      }
      this.createRteRipsCSXfrmEcucValues.generateRteRipsCSXfrmEcucValues(rteEvents);
    }
  }
}