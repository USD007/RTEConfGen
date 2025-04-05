/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.rips;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import com.bosch.tisched.rteconfig.generator.core.OsConfigToEcucValueMapping;
import com.bosch.tisched.rteconfig.generator.util.AutosarUtil;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;

import autosar40.autosartoplevelstructure.AUTOSAR;
import autosar40.autosartoplevelstructure.AutosartoplevelstructureFactory;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.ecucdescription.EcucNumericalParamValue;
import autosar40.ecucdescription.EcucReferenceValue;
import autosar40.ecucdescription.EcucTextualParamValue;
import autosar40.ecucparameterdef.EcucContainerDef;
import autosar40.ecucparameterdef.EcucModuleDef;
import autosar40.ecucparameterdef.EcucParamConfContainerDef;
import autosar40.ecucparameterdef.EcucParameterDef;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import autosar40.genericstructure.generaltemplateclasses.identifiable.IdentifiablePackage;
import autosar40.swcomponent.swcinternalbehavior.RunnableEntity;
import autosar40.swcomponent.swcinternalbehavior.rteevents.RTEEvent;
import autosar40.system.RootSwCompositionPrototype;
import autosar40.util.Autosar40Factory;

/**
 * @author MSI6COB
 */
public class GenerateRteTimedEcucValue {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(GenerateRteTimedEcucValue.class.getName());
  private static final String OS_APPLICATION = "OsApplication";
  private static final String OS_APPTASKREF = "OsAppTaskRef";
  private final String outputFilePath;
  private final IProject project;
  private final String autosarReleaseVersion;
  private final String autosarResourceVersion;
  private final Map<String, String> props;
  private EcucModuleDef rteRIPSModuleDef = null;
  private final RootSwCompositionPrototype rootSwCompositionProtoType;
  private String rteecucvaluePkgPath;
  private List<RunnableEntity> rteTimedComRunnable;
  private final List<RunnableEntity> fillFlushRunnableList;
  private final Map<String, List<String>> runnableToEventMap;
  private final Map<String, String> eventToTaskMap;
  private Map<String, String> taskToOsappMap;

  private final Map<String, EcucContainerValue> taskFillFlushContainerMap;
  private EcucModuleConfigurationValues createEcucModuleConfigurationValues = null;
  private final OsConfigToEcucValueMapping osconfiginstance;
  private final Set<String> timedComTasks;
  private AUTOSAR arPartRoot;
  private ARPackage subPackageRTE;

  private final String paramdefs[] = { "/RteRipsPluginProps", "/RteRipsPluginFillFlushRoutine" };

  /**
   * @param createAutosarProject
   * @param props2
   * @param osConfigToEcucValueMapping
   */
  public GenerateRteTimedEcucValue(final IProject createAutosarProject, final Map<String, String> map,
      final OsConfigToEcucValueMapping osConfigToEcucValueMapping) {
    this.osconfiginstance = osConfigToEcucValueMapping;
    this.fillFlushRunnableList = new ArrayList<>();
    this.runnableToEventMap = new HashMap<>();
    this.eventToTaskMap = this.osconfiginstance.gettaskEventMap();
    this.outputFilePath = map.get(RteConfigGeneratorConstants.RTE_TIMED_ECUC_VALUE_OUTPUT_PATH);
    this.project = createAutosarProject;
    this.autosarReleaseVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION);
    this.autosarResourceVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION);
    this.props = map;
    this.taskFillFlushContainerMap = new HashMap<>();
    this.timedComTasks = new HashSet<>();
    this.rootSwCompositionProtoType = this.osconfiginstance.getRootSwCompositionProtoType();

  }

  /**
   * @param swcpTimedVariableAccessMap
   * @param timedRunnables
   * @param outputFilePath
   * @param project
   * @param osConfigToEcucValueMapping
   * @return
   */
  public List<RunnableEntity> getRteTimedRunnable() {
    if (this.rteTimedComRunnable == null) {
      this.rteTimedComRunnable = new ArrayList<>();
    }
    return this.rteTimedComRunnable;
  }

  /**
   * @return
   * @throws Exception
   */
  private Map<String, String> getfillFlushdata() throws Exception {
    Map<String, String> fillflushList = new HashMap<>();
    for (RunnableEntity timedRunValue : this.fillFlushRunnableList) {
      AutosarUtil.setCurrentProcessingEObject(timedRunValue);
      String fillFlushname = null;
      if (this.runnableToEventMap.containsKey(timedRunValue.getShortName())) {
        List<String> l = this.runnableToEventMap.get(timedRunValue.getShortName());
        for (String eventval : l) {
          if (this.eventToTaskMap.containsKey(eventval)) {
            String taskname = this.eventToTaskMap.get(eventval);
            if (this.taskToOsappMap.containsKey(taskname)) {
              String osapp = this.taskToOsappMap.get(taskname);
              fillFlushname = taskname.concat("_").concat(osapp);
              fillflushList.put(taskname, fillFlushname);
              this.timedComTasks.add(taskname);
              LOGGER.info(
                  RteConfGenMessageDescription.getFormattedMesssage("125_0", timedRunValue.getShortName()).trim());

            }
            else {
              LOGGER.info(
                  RteConfGenMessageDescription.getFormattedMesssage("126_0", timedRunValue.getShortName()).trim());

            }
          }
          else {
            LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("126_1", eventval).trim());

          }
        }
      }


    }
    return fillflushList;

  }

  /**
   * @param containername
   * @return
   */
  public EcucContainerValue getFillFlushEcucContainer(final String containername) {
    return this.taskFillFlushContainerMap.get(containername);
  }

  /**
   * @param containername
   * @return
   */
  public Set<String> getTimedComTasks() {
    return this.timedComTasks;
  }

  /**
   *
   */
  private void getEventTaskMappings() {
    List<EcucContainerValue> ecuccontainer =
        GenerateArxmlUtil.getListOfEObject(this.project, EcucContainerValue.class, "");
    for (EcucContainerValue val : ecuccontainer) {
      AutosarUtil.setCurrentProcessingEObject(val);

      if ((val != null) && (val.getDefinition() != null) && (val.getShortName() != null)) {

        if (val.getDefinition().getShortName().equals("RteEventToTaskMapping")) {
          String eventName = null;
          String taskName = null;
          for (EcucAbstractReferenceValue refVal : val.getReferenceValues()) {
            AutosarUtil.setCurrentProcessingEObject(refVal);
            if (refVal.getDefinition().getShortName().equals("RteEventRef")) {

              EcucReferenceValue ecucval = (EcucReferenceValue) refVal;

              eventName = ecucval.getValue() != null ? ecucval.getValue().getShortName() : "";

              if (eventName.isEmpty()) {
                LOGGER.info("MM_DCS_RTECONFGEN_DEBUG: EcuC value reference for RteEventRef is missing in " +
                    val.getShortName() + " EcucContainer");
              }
            }
            else if (refVal.getDefinition().getShortName().equals("RteMappedToTaskRef")) {
              EcucReferenceValue ecucval = (EcucReferenceValue) refVal;
              taskName = ecucval.getValue() != null ? ecucval.getValue().getShortName() : "";
              if (taskName.isEmpty()) {
                LOGGER.info("MM_DCS_RTECONFGEN_DEBUG: EcuC value reference for RteMappedToTaskRef is missing in " +
                    val.getShortName() + " EcucContainer");
              }
            }
          }
          if ((eventName != null) && (!eventName.isEmpty() && (taskName != null)) && !taskName.isEmpty()) {
            this.eventToTaskMap.put(eventName, taskName);
          }
        }

      }

      else {

        LOGGER.warn("MM_DCS_RTECONFGEN_DEBUG: Ecuc Container '" +
            (val != null ? val.getShortName().toString() : "NULL") + "' contains missing/invalid configuration");

      }
    }
  }

  /**
   * @throws Exception
   */
  public void getTaskOsAppMapping() throws Exception {
    Map<String, String> taskOsAppMap = new HashMap<>();
    List<String> taskWithMoreOsApp = new ArrayList<>();
    List<EcucModuleConfigurationValues> allInstancesModuConfigVal =
        GenerateArxmlUtil.getListOfEObject(this.project, EcucModuleConfigurationValues.class, "");
    for (EcucModuleConfigurationValues element : allInstancesModuConfigVal) {
      AutosarUtil.setCurrentProcessingEObject(element);
      if ((element.getShortName().equalsIgnoreCase("Os") || element.getShortName().equalsIgnoreCase("RTAOS")) &&
          (element.getDefinition() != null) && // $NON-NLS
          element.getDefinition().getShortName().equalsIgnoreCase("Os"))// $NON-NLS
      {
        EList<EcucContainerValue> ecucContainers = element.getContainers();
        for (EcucContainerValue ecucContainer : ecucContainers) {
          getmappedOSApp(ecucContainer, taskOsAppMap, taskWithMoreOsApp);

        }

      }
    }
    for (String tasktoremove : taskWithMoreOsApp) {
      taskOsAppMap.remove(tasktoremove);
    }
    this.taskToOsappMap = taskOsAppMap;

  }

  /**
   * @param ecucContainer
   * @param taskOsAppMap
   * @param taskWithMoreOsApp
   * @throws Exception
   */
  private void getmappedOSApp(final EcucContainerValue ecucContainer, final Map<String, String> taskOsAppMap,
      final List<String> taskWithMoreOsApp)
      throws Exception {
    if ((ecucContainer != null) && ecucContainer.getDefinition().getShortName().equalsIgnoreCase(OS_APPLICATION)) // $NON-NLS
    {
      String osAppname = ecucContainer.getShortName();
      EList<EcucAbstractReferenceValue> refer = ecucContainer.getReferenceValues();
      for (EcucAbstractReferenceValue refobj : refer) {
        AutosarUtil.setCurrentProcessingEObject(refobj);
        if (refobj instanceof EcucReferenceValue) {
          EcucReferenceValue ecurefer = (EcucReferenceValue) refobj;

          if (refobj.getDefinition().getShortName().equals(OS_APPTASKREF)) {
            String taskname = ecurefer.getValue().getShortName();
            if (taskOsAppMap.containsKey(taskname) && !(taskOsAppMap.get(taskname).equals(osAppname))) {
              taskWithMoreOsApp.add(taskname);
              LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("127_0", taskname).trim());


            }
            else {
              taskOsAppMap.put(taskname, osAppname);
            }
          }
        }
      }
    }


  }

  //
  /**
   * @param events
   */
  private void findTimedCOMRunnableEventMapping(final List<RTEEvent> events) {
    for (RTEEvent eve : events) {
      for (RunnableEntity timeRun : getRteTimedRunnable()) {
        AutosarUtil.setCurrentProcessingEObject(timeRun);
        if (timeRun.getShortName().equalsIgnoreCase(eve.getStartOnEvent().getShortName())) {
          this.fillFlushRunnableList.add(eve.getStartOnEvent());

          List<String> list = this.runnableToEventMap.get(eve.getStartOnEvent().getShortName());

          if (list != null) {
            if (!list.contains(eve.getShortName())) {
              list.add(eve.getShortName());
            }

          }
          else {
            List<String> l = new ArrayList<String>();
            l.add(eve.getShortName());
            this.runnableToEventMap.put(eve.getStartOnEvent().getShortName(), l);
          }


          break;
        }
      }
    }
  }


  /**
   * @throws Exception
   */
  public void generateTimedValueFile() throws Exception {
    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("128_0").trim());

    String conPath = this.outputFilePath.substring(0, this.outputFilePath.lastIndexOf("/"));
    String fileName =
        this.outputFilePath.substring(this.outputFilePath.lastIndexOf("/") + 1, this.outputFilePath.length());


    if ((this.subPackageRTE != null)) {


      GenerateArxmlUtil.addDefaultvalueForMandatoryParamDefs(this.createEcucModuleConfigurationValues.getDefinition(),
          this.createEcucModuleConfigurationValues, Arrays.asList(this.paramdefs));


      IResource findMember = this.project.findMember(conPath);
      URI resourceURI = null;
      if (findMember != null) {
        resourceURI = URI.createPlatformResourceURI(findMember.getFullPath().toOSString() + "/" + fileName, false);
      }
      else {
        resourceURI = URI.createPlatformResourceURI(
            URI.createFileURI(this.project.getFullPath().toOSString() + conPath).path() + "/" + fileName, false);
      }


      IStatus saveFile = GenerateArxmlUtil.saveFile(this.project, this.arPartRoot, resourceURI,
          AutosarUtil.getMetaModelDescriptorByAutosarResoureVersion(
              AutosarUtil.getMetaModelDescriptorByAutosarReleaseVersion(this.autosarReleaseVersion),
              this.autosarResourceVersion));

      if (saveFile.isOK()) {
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("129_0",
            URI.createFileURI(this.project.getFullPath().toOSString() + conPath).path(), fileName).trim());

      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("131_0").trim());


      }
    }
    else {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("132_0").trim());

    }
  }


  /**
   * @return
   * @throws Exception
   */
  public EcucModuleConfigurationValues generateRteRipsEcuCcontainerValue() throws Exception {
    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("133_0").trim());
    List<EcucModuleDef> ecucModuleDef = GenerateArxmlUtil.getListOfEObject(this.project, EcucModuleDef.class, "");

    for (EcucModuleDef moduleDef : ecucModuleDef) {
      String shortName = moduleDef.getShortName();

      if (shortName.equalsIgnoreCase("Rte_Rips_ImplTimedCom")) {
        String packagePath = GenerateArxmlUtil.getPackagePath(moduleDef);
        this.rteRIPSModuleDef = moduleDef;
        this.props.put(RteConfigGeneratorConstants.RTE_TIMED_ECUC_PARAM_DEF_AR_PACKAGE_PATH, packagePath);
      }
    }

    this.createEcucModuleConfigurationValues = createEcucModuleConfigurationValueforRTERIPS(this.outputFilePath);
    this.rteecucvaluePkgPath = this.props.get(RteConfigGeneratorConstants.RTE_TIMED_ECUC_VALUE_AR_PACKAGE_PATH);
    if (this.rteecucvaluePkgPath != null) {
      updateArPackagePath();
    }
    return this.createEcucModuleConfigurationValues;
  }

  /**
   *
   */
  private void updateArPackagePath() {

    this.arPartRoot = AutosartoplevelstructureFactory.eINSTANCE.createAUTOSAR();
    this.subPackageRTE = GenerateArxmlUtil.getARArPackage(this.arPartRoot, this.rteecucvaluePkgPath);
    if ((this.rteRIPSModuleDef != null) && (this.createEcucModuleConfigurationValues != null)) {
      this.subPackageRTE.getElements().add(this.createEcucModuleConfigurationValues);
    }
  }

  /**
   * @return
   */
  public EcucModuleConfigurationValues getEcuConfiguration() {
    return this.createEcucModuleConfigurationValues;
  }

  public EcucModuleConfigurationValues updateEcucRipsConfigurationWithFillFlushRoutine() throws Exception {
    this.rteecucvaluePkgPath = this.props.get(RteConfigGeneratorConstants.RTE_TIMED_ECUC_VALUE_AR_PACKAGE_PATH);
    if (this.rteecucvaluePkgPath != null) {
      updateArPackagePath();
    }
    Map<String, EcucContainerValue> fillFlushContainerMap = new HashMap<String, EcucContainerValue>();
    List<RTEEvent> rteEventList = GenerateArxmlUtil.getListOfEObject(this.project, RTEEvent.class, "");
    findTimedCOMRunnableEventMapping(rteEventList);
    getEventTaskMappings();
    getTaskOsAppMapping();
    Map<String, String> fillFlushInputTask = getfillFlushdata();
    // EcucContainerValue createTimedContainer = createTimedComContainer();
    // if (createTimedContainer != null) {
    for (Entry<String, String> fillflush : fillFlushInputTask.entrySet()) {
      String fillflushname = GenerateArxmlUtil.getShortenedNameOfElements(fillflush.getValue(),
          Collections.unmodifiableMap(fillFlushContainerMap));

      EcucContainerValue createFillEcucContainer = createfilFlushEcucContainer("FillRoutine_" + fillflushname,
          "Rte_Rips_ImplTimedCom" + fillflush.getValue() + "_BufferFill");
      if (createFillEcucContainer != null) {
        // createTimedContainer.getSubContainers().add(createFillEcucContainer);
        this.createEcucModuleConfigurationValues.getContainers().add(createFillEcucContainer);
        this.taskFillFlushContainerMap.put(fillflush.getKey() + "_fill", createFillEcucContainer);
        fillFlushContainerMap.put(fillflush.getKey(), createFillEcucContainer);
      }
      fillflushname = GenerateArxmlUtil.getShortenedNameOfElements(fillflush.getValue(), fillFlushContainerMap);
      EcucContainerValue createFlushEcucContainer = createfilFlushEcucContainer("FlushRoutine_" + fillflushname,
          "Rte_Rips_ImplTimedCom" + fillflush.getValue() + "_BufferFlush");
      if (createFlushEcucContainer != null) {
        // createTimedContainer.getSubContainers().add(createFlushEcucContainer);
        this.createEcucModuleConfigurationValues.getContainers().add(createFlushEcucContainer);
        this.taskFillFlushContainerMap.put(fillflush.getKey() + "_flush", createFlushEcucContainer);
        fillFlushContainerMap.put(fillflush.getKey(), createFlushEcucContainer);
      }
    }
    // this.createEcucModuleConfigurationValues.getContainers().add(createTimedContainer);
    // }
    return this.createEcucModuleConfigurationValues;
  }

  /**
   * @param outputFilePath2
   * @return
   */
  private EcucModuleConfigurationValues createEcucModuleConfigurationValueforRTERIPS(final String outputFilePath2) {
    this.createEcucModuleConfigurationValues = Autosar40Factory.eINSTANCE.createEcucModuleConfigurationValues();
    this.createEcucModuleConfigurationValues.setShortName("Rte_Rips_ImplTimedCom");
    this.createEcucModuleConfigurationValues.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
    this.createEcucModuleConfigurationValues.setDefinition(this.rteRIPSModuleDef);
    EcucContainerValue rtePluginProps = createRtePluginPropsContainer();
    if (rtePluginProps != null) {
      this.createEcucModuleConfigurationValues.getContainers().add(rtePluginProps);
    }
    return this.createEcucModuleConfigurationValues;
  }

  /**
   * @param ecucContainerDef
   */
  private EcucContainerValue createfilFlushEcucContainer(final String fillFlushname, final String paramname) {
    EcucContainerValue createEcucContainerValue = null;
    EcucParamConfContainerDef ecucParameterDef = null;
    for (EcucContainerDef item : this.rteRIPSModuleDef.getContainers()) {
      if (item.getShortName().equals("RteRipsPluginFillFlushRoutine")) {
        if (item instanceof EcucParamConfContainerDef) {
          createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
          createEcucContainerValue.setShortName(fillFlushname);
          createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
          createEcucContainerValue.setDefinition(item);
          ecucParameterDef = (EcucParamConfContainerDef) item;
          break;
        }

      }
    }
    if (createEcucContainerValue != null) {

      EcucParameterDef routinesymbol = getEcucParameterDef(ecucParameterDef, "RteRipsPluginFillFlushRoutineFncSymbol");
      // EcucParameterDef modeDisableHandling = getEcucParameterDef(ecucParameterDef, "RteRipsModeDisablingHandling");
      // EcucParameterDef ripsSchedulepoint = getEcucParameterDef(ecucParameterDef, "RteRipsOsSchedulePoint");

      routinesymbol = routinesymbol == null
          ? getEcucParameterDef(ecucParameterDef, "RteRipsPluginFillFlushRoutineSymbol") : routinesymbol;
      if (routinesymbol != null) {
        createTextualValue(routinesymbol, paramname, createEcucContainerValue);
        // if (modeDisableHandling != null) {
        // createTextualValue(modeDisableHandling, "RTE_RIPS_CONSIDER_MODE_DISABLINGS", createEcucContainerValue);
        //
        // }
        // if (ripsSchedulepoint != null) {
        // createTextualValue(ripsSchedulepoint, "NONE", createEcucContainerValue);
        //
        // }
      }
    }
    return createEcucContainerValue;
  }


  /**
   * @return
   */
  private EcucContainerValue createTimedComContainer() {
    EcucContainerValue createEcucContainerValue = null;
    if (this.rteRIPSModuleDef != null) {
      for (EcucContainerDef item : this.rteRIPSModuleDef.getContainers()) {
        if (item.getShortName().equals("RteRipsPluginImplTimedComSettings")) {
          if (item instanceof EcucParamConfContainerDef) {
            createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
            createEcucContainerValue.setShortName("RteRipsPluginImplTimedComSettings");
            createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
            createEcucContainerValue.setDefinition(item);
            break;
          }

        }
      }
    }


    return createEcucContainerValue;
  }


  /**
   * @return
   */
  private EcucContainerValue createRtePluginPropsContainer() {
    EcucContainerValue createEcucContainerValue = null;
    EcucParamConfContainerDef ecucParameterDef = null;
    for (EcucContainerDef item : this.rteRIPSModuleDef.getContainers()) {
      if (item.getShortName().equals("RteRipsPluginProps")) {
        if (item instanceof EcucParamConfContainerDef) {
          createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
          createEcucContainerValue.setShortName("ImplTimedCom");
          createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
          createEcucContainerValue.setDefinition(item);
          ecucParameterDef = (EcucParamConfContainerDef) item;
          break;
        }

      }
    }
    if ((createEcucContainerValue != null)) {
      createParameterValuesforRterips(createEcucContainerValue, ecucParameterDef);
    }

    return createEcucContainerValue;
  }

  /**
   * @param ecucParameterDef
   * @param createEcucContainerValue
   */
  private void createParameterValuesforRterips(final EcucContainerValue createEcucContainerValue,
      final EcucParamConfContainerDef ecucParameterDef) {

    EcucParameterDef supportfreeedin =
        getEcucParameterDef(ecucParameterDef, "RtePluginSupportsFreedomFromInterference");
    EcucParameterDef supportIOC = getEcucParameterDef(ecucParameterDef, "RtePluginSupportsOptimizedIOC");
    EcucParameterDef supportIRW = getEcucParameterDef(ecucParameterDef, "RtePluginSupportsIReadIWrite");
    EcucParameterDef supportRW = getEcucParameterDef(ecucParameterDef, "RtePluginSupportsReadWrite");
    EcucParameterDef globalCopy = getEcucParameterDef(ecucParameterDef, "RteRipsGlobalCopyInstantiationPolicy");


    if (supportfreeedin != null) {
      createNumericalValue(supportfreeedin, "true", createEcucContainerValue);

    }
    if (supportIOC != null) {
      createNumericalValue(supportIOC, "true", createEcucContainerValue);

    }

    if (supportIRW != null) {
      createNumericalValue(supportIRW, "true", createEcucContainerValue);

    }
    if (supportRW != null) {
      createNumericalValue(supportRW, "true", createEcucContainerValue);

    }
    if (globalCopy != null) {
      createTextualValue(globalCopy, "RTE_RIPS_INSTANTIATION_BY_RTE", createEcucContainerValue);

    }

  }

  /**
   * @param supportfreeedin
   * @param string
   * @param createEcucContainerValue
   */
  private void createNumericalValue(final EcucParameterDef supportfreeedin, final String value,
      final EcucContainerValue createEcucContainerValue) {
    EcucNumericalParamValue createEcucNumericalParamValue = Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
    createEcucNumericalParamValue.setDefinition(supportfreeedin);
    createEcucNumericalParamValue.setValue(GenerateArxmlUtil.createNumericalValueVariationPoint(value));
    createEcucContainerValue.getParameterValues().add(createEcucNumericalParamValue);

  }

  private void createTextualValue(final EcucParameterDef supportfreeedin, final String value,
      final EcucContainerValue createEcucContainerValue) {
    EcucTextualParamValue createEcucTextualParamValue = Autosar40Factory.eINSTANCE.createEcucTextualParamValue();
    createEcucTextualParamValue.setDefinition(supportfreeedin);
    createEcucTextualParamValue.setValue(value);
    createEcucContainerValue.getParameterValues().add(createEcucTextualParamValue);

  }

  private EcucParameterDef getEcucParameterDef(final EcucParamConfContainerDef ecucParamConfContainerDef,
      final String shortName) {
    EcucParameterDef ecucParameterDef = null;
    for (EcucParameterDef item : ecucParamConfContainerDef.getParameters()) {
      if (item.getShortName().equals(shortName)) {
        return item;
      }
    }
    return ecucParameterDef;
  }


}
