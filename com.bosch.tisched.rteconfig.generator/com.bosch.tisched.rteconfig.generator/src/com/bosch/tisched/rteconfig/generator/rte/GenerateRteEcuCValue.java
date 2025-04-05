/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.rte;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import com.bosch.tisched.rteconfig.generator.core.EventEnum;
import com.bosch.tisched.rteconfig.generator.core.OsConfigToEcucValueMapping;
import com.bosch.tisched.rteconfig.generator.core.SwComponentInstanceTypeEnum;
import com.bosch.tisched.rteconfig.generator.core.TaskActivationEnum;
import com.bosch.tisched.rteconfig.generator.core.TischedComponent;
import com.bosch.tisched.rteconfig.generator.core.TischedComponentInstance;
import com.bosch.tisched.rteconfig.generator.core.TischedEvent;
import com.bosch.tisched.rteconfig.generator.core.TischedTask;
import com.bosch.tisched.rteconfig.generator.core.TischedTaskEventTypeEnum;
import com.bosch.tisched.rteconfig.generator.rips.GenerateRteRipsCSXfrmEcucValues;
import com.bosch.tisched.rteconfig.generator.rips.GenerateRteTimedEcucValue;
import com.bosch.tisched.rteconfig.generator.util.AutosarUtil;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;
import com.bosch.tisched.rteconfig.generator.util.VariationPointUtil;

import autosar40.autosartoplevelstructure.AUTOSAR;
import autosar40.autosartoplevelstructure.AutosartoplevelstructureFactory;
import autosar40.bswmodule.bswbehavior.BswEvent;
import autosar40.bswmodule.bswbehavior.BswExternalTriggerOccurredEvent;
import autosar40.bswmodule.bswbehavior.BswInternalBehavior;
import autosar40.bswmodule.bswbehavior.BswTimingEvent;
import autosar40.bswmodule.bswimplementation.BswImplementation;
import autosar40.bswmodule.bswoverview.BswModuleDescription;
import autosar40.commonstructure.triggerdeclaration.Trigger;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucInstanceReferenceValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.ecucdescription.EcucModuleConfigurationValuesRefConditional;
import autosar40.ecucdescription.EcucNumericalParamValue;
import autosar40.ecucdescription.EcucReferenceValue;
import autosar40.ecucdescription.EcucTextualParamValue;
import autosar40.ecucparameterdef.EcucAbstractReferenceDef;
import autosar40.ecucparameterdef.EcucContainerDef;
import autosar40.ecucparameterdef.EcucEnumerationParamDef;
import autosar40.ecucparameterdef.EcucModuleDef;
import autosar40.ecucparameterdef.EcucParamConfContainerDef;
import autosar40.ecucparameterdef.EcucParameterDef;
import autosar40.ecucparameterdef.EcucReferenceDef;
import autosar40.ecucparameterdef.EcucUriReferenceDef;
import autosar40.genericstructure.generaltemplateclasses.anyinstanceref.AnyInstanceRef;
import autosar40.genericstructure.generaltemplateclasses.anyinstanceref.AnyinstancerefFactory;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import autosar40.genericstructure.generaltemplateclasses.identifiable.IdentifiablePackage;
import autosar40.genericstructure.generaltemplateclasses.identifiable.Referrable;
import autosar40.genericstructure.varianthandling.VariationPoint;
import autosar40.swcomponent.composition.SwComponentPrototype;
import autosar40.swcomponent.swcinternalbehavior.dataelements.VariableAccess;
import autosar40.swcomponent.swcinternalbehavior.rteevents.RTEEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.TimingEvent;
import autosar40.system.RootSwCompositionPrototype;
import autosar40.util.Autosar40Factory;

/**
 * @author shk1cob
 */
public class GenerateRteEcuCValue {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(GenerateRteEcuCValue.class.getName());

  private final String rteParamDefPkgPath;
  private final String rteecucvaluePkgPath;
  private final String autosarReleaseVersion;
  private final String autosarResourceVersion;
  private final IProject project;
  private final OsConfigToEcucValueMapping tischedToEcuCValueMapping;
  private final String outputFilePath;
  private final Map<TischedTask, TischedEvent> eventTaskMap = new HashMap<TischedTask, TischedEvent>();
  private final Map<TischedTask, List<TischedEvent>> eventListToTaskMap =
      new HashMap<TischedTask, List<TischedEvent>>();
  private final String taskRecurrencePath;
  private final String rteconfgenlogPath;
  private final RootSwCompositionPrototype rootSwCompositionProtoType;

  private Map<SwComponentPrototype, Map<String, List<VariableAccess>>> rteTimedComValues;

  private final GenerateRteTimedEcucValue rteTimedInstance;

  private final String enableDataConsistency;

  private boolean isRipsInvocationRefAdded = false;

  private final String paramdefs[] = {
      "/RteBswModuleInstance",
      "/RteBswModuleInstance/RteBswEventToTaskMapping",
      "/RteBswModuleInstance/RteBswRequiredTriggerConnection",
      "/RteSwComponentInstance",
      "/RteSwComponentInstance/RteEventToTaskMapping",
      "/RteRips",
      "/RteImplicitCommunication",
      "/RteOsInteraction",
      "/RteOsInteraction/RteOsTaskChain",
      "/RteOsInteraction/RteUsedOsActivation",
      "/RteGeneration",
      "/RteFillFlushHookEnabledOSTasks" };

  private final GenerateRteRipsCSXfrmEcucValues generateRteRipsCSXfrmEcucValues;


  /**
   * @param createRteTimedEcucValue
   * @param project IProject
   * @param map Map<String, String>
   * @param tischedToEcuCValueMapping OsConfigToEcucValueMapping
   * @param timedComSWCDVariableAccesMap
   * @throws Exception
   */
  public GenerateRteEcuCValue(final GenerateRteTimedEcucValue createRteTimedEcucValue, final IProject project,
      final Map<String, String> map, final OsConfigToEcucValueMapping tischedToEcuCValueMapping,
      final GenerateRteRipsCSXfrmEcucValues generateRteRipsCSXfrmEcucValues) throws Exception {
    this.project = project;
    this.rteParamDefPkgPath = map.get(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH);
    this.rteecucvaluePkgPath = map.get(RteConfigGeneratorConstants.RTE_ECUC_VALUE_AR_PACKAGE_PATH);
    this.autosarReleaseVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION);
    this.autosarResourceVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION);
    this.tischedToEcuCValueMapping = tischedToEcuCValueMapping;
    this.outputFilePath = map.get(RteConfigGeneratorConstants.RTE_ECUC_VALUE_OUTPUT_PATH);
    this.taskRecurrencePath = map.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_TASK_RECURRENCE_FILE_PATH);
    this.rteconfgenlogPath = map.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG);
    this.rootSwCompositionProtoType = tischedToEcuCValueMapping.getRootSwCompositionProtoType();
    this.enableDataConsistency = map.get(RteConfigGeneratorConstants.ENABLE_MM_DATA_CONSISTENCY);
    this.rteTimedInstance = createRteTimedEcucValue;
    createRteTimedEcucValue.updateEcucRipsConfigurationWithFillFlushRoutine();
    this.generateRteRipsCSXfrmEcucValues = generateRteRipsCSXfrmEcucValues;
  }

  /**
   * @param timedComSWCDVariableAccesMap contains List of timedcom affected dataaccess mapped to its corresponding
   *          softwarecomponentprototype
   */
  public void updateTimedComSWCDVariableAccMap(
      final Map<SwComponentPrototype, Map<String, List<VariableAccess>>> timedComSWCDVariableAccesMap) {
    this.rteTimedComValues = timedComSWCDVariableAccesMap;
  }

  /**
   * @throws Exception
   */
  public void generateRteEcuCValue() throws Exception {


    LOGGER.info("*** Generating Rte EcuCValue file");

    String conPath = this.outputFilePath.substring(0, this.outputFilePath.lastIndexOf("/"));
    String fileName =
        this.outputFilePath.substring(this.outputFilePath.lastIndexOf("/") + 1, this.outputFilePath.length());

    // autosar instance
    final AUTOSAR arPartRoot = AutosartoplevelstructureFactory.eINSTANCE.createAUTOSAR();


    ARPackage subPackage = GenerateArxmlUtil.getARArPackage(arPartRoot, this.rteecucvaluePkgPath);

    if (subPackage != null) {
      EcucModuleConfigurationValues createEcucModuleConfigurationValue =
          createEcucModuleConfigurationValue(this.outputFilePath);
      this.tischedToEcuCValueMapping.getRteEcucContainerValues()
          .addAll(createEcucModuleConfigurationValue.getContainers());
      subPackage.getElements().add(createEcucModuleConfigurationValue);

      GenerateArxmlUtil.addDefaultvalueForMandatoryParamDefs(createEcucModuleConfigurationValue.getDefinition(),
          createEcucModuleConfigurationValue, Arrays.asList(this.paramdefs));

      generateTimingEventsToTaskMappingReport();

      IResource findMember = this.project.findMember(conPath);
      URI resourceURI = null;
      if (findMember != null) {
        resourceURI = URI.createPlatformResourceURI(findMember.getFullPath().toOSString() + "/" + fileName, false);
      }
      {
        resourceURI = URI.createPlatformResourceURI(
            URI.createFileURI(this.project.getFullPath().toOSString() + conPath).path() + "/" + fileName, false);
      }


      IStatus saveFile = GenerateArxmlUtil.saveFile(this.project, arPartRoot, resourceURI,
          AutosarUtil.getMetaModelDescriptorByAutosarResoureVersion(
              AutosarUtil.getMetaModelDescriptorByAutosarReleaseVersion(this.autosarReleaseVersion),
              this.autosarResourceVersion));

      if (saveFile.isOK()) {
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("170_0",
            URI.createFileURI(this.project.getFullPath().toOSString() + conPath).path(), fileName).trim());

      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("399_2").trim());
      }
    }
    else {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_2").trim());
    }


    LOGGER.info("*** Generating Rte Task Recurrence output file");

    StringBuffer sb = new StringBuffer();
    sb.append("--task-recurrence ");

    for (TischedTask tischedTask : this.tischedToEcuCValueMapping.getTischedTasks()) {
      boolean bTimingTask = tischedTask.getTischedTaskeventtypeenum() != null
          ? tischedTask.getTischedTaskeventtypeenum().equals(TischedTaskEventTypeEnum.T) ? true : false : true;
      if (bTimingTask && (tischedTask.getTaskCycle() != null) && !tischedTask.getTaskCycle().isEmpty()) {

        TischedEvent tischedEvent = this.eventTaskMap.get(tischedTask);

        if (tischedEvent != null) {

          try {

            float parsedNum = (Float.parseFloat(tischedTask.getTaskCycle().trim()) / 1000);
            String parsedNStr = new BigDecimal(Float.toString(parsedNum)).toPlainString();
            int pos = getDigitPosition(parsedNStr);
            parsedNStr = pos > -1 ? BigDecimal.valueOf(parsedNum).setScale(pos, RoundingMode.HALF_UP).toPlainString()
                : parsedNStr;
            String[] s = parsedNStr.split("\\.");

            if ((s.length > 1) && (Integer.parseInt(s[1]) == 0)) {
              parsedNStr = Integer.parseInt(s[0]) + "";

            }
            s = parsedNStr.split("\\.");
            if ((s.length > 1) && (parsedNStr.endsWith("0"))) {
              parsedNStr = parsedNStr.substring(0, parsedNStr.lastIndexOf('0'));
            }


            sb.append(tischedTask.getShortName() + "=" + parsedNStr + ",");

          }
          catch (NumberFormatException ex) {
            LOGGER.warn(RteConfGenMessageDescription
                .getFormattedMesssage("231_0", tischedTask.getShortName(), tischedTask.getTaskCycle()).trim());

          }

        }
        else {
          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("113_0", tischedTask.getShortName()).trim());


        }
      }
    }

    BufferedWriter writer = null;
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(this.project.getLocation().toOSString() + this.taskRecurrencePath, false);
      writer = new BufferedWriter(fileWriter);
      writer.append(sb.toString().subSequence(0, (sb.toString().length()) - 1));
      writer.append(System.getProperty("line.separator"));


      if (this.enableDataConsistency.equals(RteConfigGeneratorConstants.ENABLE_MM_DATA_CONSISTENCY_MODE)) {
        String mmsOptions = getMMSOptions();
        String mmsDcOptions = getMMSDCOptions();
        writer.append(mmsOptions);
        writer.append(mmsDcOptions);
      }
      else {
        String mmsOptions = getMMSOptions();
        writer.append(mmsOptions);
      }

      writer.flush();
    }
    catch (IOException ex) {
      LOGGER.error("IOException occured " + ex.getMessage());
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());

    }
    finally {
      try {
        if (writer != null) {
          writer.close();
        }
        if (fileWriter != null) {
          fileWriter.close();
        }
        this.eventTaskMap.clear();
      }
      catch (IOException ex) {
        LOGGER.error("IOException occured " + ex.getMessage());
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());

      }
    }
    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("170_1").trim());


  }

  /**
   * @param osTasks
   * @param ecuCModuleDef
   * @param containerPath
   * @return
   */
  private EcucContainerValue createRteTaskHooks(final EcucModuleDef ecuCModuleDef, final List<TischedTask> osTasks,
      final String containerPath) {

    EcucContainerValue rtegeneration = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecuCModuleDef, "RteGeneration");

    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {

      EcucParamConfContainerDef ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;
      rtegeneration = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      rtegeneration.setShortName("RteGeneration");
      rtegeneration.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      rtegeneration.setDefinition(ecucContainerDef);

      EcucContainerValue rteFillFlushHookEnabledOSTasks = null;
      EcucContainerDef ecucContainerDef1 =
          getEcucContainerDef(ecucParamConfContainerDef, "RteFillFlushHookEnabledOSTasks");

      if ((ecucContainerDef1 != null) && (ecucContainerDef1 instanceof EcucParamConfContainerDef)) {

        EcucParamConfContainerDef ecucParamConfContainerDef1 = (EcucParamConfContainerDef) ecucContainerDef1;
        rteFillFlushHookEnabledOSTasks = Autosar40Factory.eINSTANCE.createEcucContainerValue();
        rteFillFlushHookEnabledOSTasks.setShortName("RteFillFlushHookEnabledOSTasks");
        rteFillFlushHookEnabledOSTasks.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
        rteFillFlushHookEnabledOSTasks.setDefinition(ecucParamConfContainerDef1);

        List<TischedTask> rteHookRefTasks =
            osTasks.stream().filter(t -> t.isOsRtetaskhooks()).collect(Collectors.toList());
        for (TischedTask task : rteHookRefTasks) {
          EcucReferenceValue ecucReferenceValue = null;
          EcucAbstractReferenceDef ecucReferenceDef = getEcucReferenceDef(ecucParamConfContainerDef1, "TaskRef");

          if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucReferenceDef)) {
            EcucReferenceDef ecucReferenceDef1 = (EcucReferenceDef) ecucReferenceDef;
            ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
            ecucReferenceValue.setDefinition(ecucReferenceDef1);
            ecucReferenceValue.setValue((Referrable) task.getTaskInstance());
          }
          rteFillFlushHookEnabledOSTasks.getReferenceValues().add(ecucReferenceValue);
        }
        rtegeneration.getSubContainers().add(rteFillFlushHookEnabledOSTasks);
      }
    }
    return rtegeneration;
  }

  int getDigitPosition(final String value) {
    int pos = 0;

    String[] s = value.split("\\.");

    if (s.length > 1) {

      char[] chars = s[1].toCharArray();

      for (int i = 0; i < chars.length; i++) {

        if (Character.isDigit(chars[i]) && (chars[i] != '0')) {
          pos = i + 1;
        }
      }

    }
    return pos;
  }


  private EcucModuleConfigurationValues createEcucModuleConfigurationValue(final String containerPath)
      throws Exception {
    EcucModuleConfigurationValues createEcucModuleConfigurationValues =
        Autosar40Factory.eINSTANCE.createEcucModuleConfigurationValues();
    createEcucModuleConfigurationValues.setShortName("Rte");
    createEcucModuleConfigurationValues.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());

    EcucModuleDef ecuCModuleDef =
        GenerateArxmlUtil.getEObject(this.project, this.rteParamDefPkgPath, EcucModuleDef.class, containerPath);
    if (ecuCModuleDef != null) {

      createEcucModuleConfigurationValues.setDefinition(ecuCModuleDef);

      EcucContainerValue createRteOsContainers =
          createRteOsInteraction(ecuCModuleDef, this.tischedToEcuCValueMapping.getTischedTasks());
      if ((createRteOsContainers != null) && !createRteOsContainers.getSubContainers().isEmpty()) {
        createEcucModuleConfigurationValues.getContainers().add(createRteOsContainers);
      }

      EcucContainerValue rteGenerationContainer =
          createRteTaskHooks(ecuCModuleDef, this.tischedToEcuCValueMapping.getTischedTasks(), containerPath);
      if ((rteGenerationContainer != null) && !rteGenerationContainer.getSubContainers().isEmpty() &&
          (rteGenerationContainer.getSubContainers().get(0).getReferenceValues() != null) &&
          !rteGenerationContainer.getSubContainers().get(0).getReferenceValues().isEmpty()) {
        createEcucModuleConfigurationValues.getContainers().add(rteGenerationContainer);
      }
      Map<String, EcucContainerValue> triggerModInstnceMap = new HashMap<>();
      Map<String, List<String>> compInstanceToReqTriggerMap = new HashMap<>();
      Map<String, Trigger> uriToTriggerMap = new HashMap<String, Trigger>();


      if (this.tischedToEcuCValueMapping.isbAutoTriggerconnection()) {
        getRequiredBSWTriggerMappingData(triggerModInstnceMap, compInstanceToReqTriggerMap, uriToTriggerMap);

      }

      for (TischedComponentInstance componentInstance : this.tischedToEcuCValueMapping.getTischedComponentInstances()
          .values()) {


        if (componentInstance.getSwComponentInstanceTypeEnum() == SwComponentInstanceTypeEnum.SW_COMPONENT_PROTOTYPE) {
          EcucContainerValue createRteSwComponentInstance =
              createRteSwComponentInstance(componentInstance, ecuCModuleDef);
          if (createRteSwComponentInstance != null) {
            createEcucModuleConfigurationValues.getContainers().add(createRteSwComponentInstance);
          }
        }
        else if (componentInstance.getSwComponentInstanceTypeEnum() == SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION) {

          EcucContainerValue createRteBswModuleInstance = createRteBswModuleInstance(componentInstance, ecuCModuleDef,
              triggerModInstnceMap, compInstanceToReqTriggerMap, uriToTriggerMap);
          if (createRteBswModuleInstance != null) {
            createEcucModuleConfigurationValues.getContainers().add(createRteBswModuleInstance);
          }
        }
      }

      boolean isImplicitTimedComAdded = false;
      if (this.tischedToEcuCValueMapping.isValidateTimedCom()) {
        for (Entry<SwComponentPrototype, Map<String, List<VariableAccess>>> rtv : this.rteTimedComValues.entrySet()) {

          List<EcucContainerValue> contList =
              createRteImplicitCommunication(rtv.getKey(), rtv.getValue(), ecuCModuleDef);

          if (!contList.isEmpty()) {
            createEcucModuleConfigurationValues.getContainers().addAll(contList);
            isImplicitTimedComAdded = true;
          }
        }

      }

      if (isImplicitTimedComAdded /* || this.isRipsInvocationRefAdded */) {
        createRteRipsContainer(ecuCModuleDef, createEcucModuleConfigurationValues);
      }

    }

    return createEcucModuleConfigurationValues;
  }


  private void getRequiredBSWTriggerMappingData(final Map<String, EcucContainerValue> triggerModInstnceMap,
      final Map<String, List<String>> compInstanceToReqTriggerMap, final Map<String, Trigger> uriToTriggerMap) {
    List<TischedTask> tischedTasks = this.tischedToEcuCValueMapping.getTischedTasks();

    List<EcucModuleConfigurationValues> ecucmodlist =
        GenerateArxmlUtil.getListOfEObject(this.project, EcucModuleConfigurationValues.class, "");
    List<EcucModuleConfigurationValues> collect =
        ecucmodlist.stream().filter(e -> e.getShortName().equals("Rte")).collect(Collectors.toList());
    if ((collect != null) && !collect.isEmpty()) {
      EcucModuleConfigurationValues rteEcucModuleConfig = collect.get(0);
      EList<EcucContainerValue> bswcontainers = rteEcucModuleConfig.getContainers();
      for (EcucContainerValue bswecu : bswcontainers) {
        BswImplementation bswimpl = getBswimplementationValue(bswecu);
        if ((bswimpl != null) && !bswimpl.eIsProxy()) {
          BswInternalBehavior ib = bswimpl.getBehavior();
          if ((ib != null) && (ib.eContainer() instanceof BswModuleDescription)) {
            BswModuleDescription bswmd = (BswModuleDescription) ib.eContainer();
            if ((bswmd != null) && !bswmd.eIsProxy()) {
              bswmd.getReleasedTriggers().stream()
                  .forEach(trigger -> triggerModInstnceMap.put(GenerateArxmlUtil.getFragmentURI(trigger), bswecu));
            }
          }
        }
      }
    }

    List<Trigger> triggers = GenerateArxmlUtil.getListOfEObject(this.project, Trigger.class, "");
    triggers.stream().forEach(trigger -> uriToTriggerMap.put(GenerateArxmlUtil.getFragmentURI(trigger), trigger));

    for (TischedTask task : tischedTasks) {
      for (TischedEvent event : task.getEvents()) {
        if ((event.getEvent() instanceof BswExternalTriggerOccurredEvent) &&
            (event.getInstance() instanceof BswImplementation)) {
          BswExternalTriggerOccurredEvent etoe = (BswExternalTriggerOccurredEvent) event.getEvent();
          BswImplementation componentInstance = (BswImplementation) event.getInstance();
          String relTriggerURI = task.getOsBSWReleasedTrigger();
          String key = componentInstance.getShortName() + "_ReleaseTrigger_" + relTriggerURI;
          String reqTriggerURI = GenerateArxmlUtil.getFragmentURI(etoe.getTrigger());
          List<String> list = compInstanceToReqTriggerMap.get(key);

          if ((list != null) && !list.contains(reqTriggerURI)) {
            list.add(reqTriggerURI);
          }
          else {
            List<String> list1 = new ArrayList<String>();
            list1.add(reqTriggerURI);
            compInstanceToReqTriggerMap.put(key, list1);
          }
        }
      }
    }
  }


  /**
   * @param bswecu
   * @return
   */
  private BswImplementation getBswimplementationValue(final EcucContainerValue bswecu) {
    // (BswImplementation)(((EcucReferenceValue)bswecu.getReferenceValues().get(0)).getValue()) instanceof

    for (EcucAbstractReferenceValue ref : bswecu.getReferenceValues()) {
      if ((ref instanceof EcucReferenceValue) && (((EcucReferenceValue) ref).getValue() != null) &&
          (((EcucReferenceValue) ref).getValue() instanceof BswImplementation)) {
        return (BswImplementation) (((EcucReferenceValue) ref).getValue());

      }
    }
    return null;
  }

  private String getMMSOptions() throws IOException {

    StringBuilder fileContent = new StringBuilder();
    InputStream resourceAsStream = getClass().getResource("MMS_OPTIONS.txt").openStream();

    int i;

    // reads till the end of the stream
    while ((i = resourceAsStream.read()) != -1) {
      fileContent.append((char) i);
    }
    resourceAsStream.close();

    return fileContent.toString();

  }

  private String getMMSDCOptions() throws IOException {
    StringBuilder fileContent = new StringBuilder();
    InputStream resourceAsStream = getClass().getResource("MMSDC_OPTIONS.txt").openStream();

    int i;

    // reads till the end of the stream
    while ((i = resourceAsStream.read()) != -1) {
      fileContent.append((char) i);
    }
    resourceAsStream.close();

    return fileContent.toString();
  }

  /**
   * @param createEcucModuleConfigurationValues
   * @param ecuCModuleDef
   */
  private void createRteRipsContainer(final EcucModuleDef ecuCModuleDef,
      final EcucModuleConfigurationValues createEcucModuleConfigurationValues) {
    EcucContainerValue createEcucContainerValue = null;
    EcucParamConfContainerDef ecucParamConfContainerDef = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecuCModuleDef, "RteRips");
    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {
      ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;
      createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      createEcucContainerValue.setShortName("RteRips");
      createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      createEcucContainerValue.setDefinition(ecucContainerDef);

    }


    if (createEcucContainerValue != null) {
      createParamRefValues(createEcucContainerValue, ecucParamConfContainerDef);
      createEcucModuleConfigurationValues.getContainers().add(createEcucContainerValue);
    }


  }


  /**
   * @param createEcucContainerValue
   * @param ecucParamConfContainerDef
   */
  private void createParamRefValues(final EcucContainerValue createEcucContainerValue,
      final EcucParamConfContainerDef ecucParamConfContainerDef) {
    EcucParameterDef supportfreeedin = getEcucParameterDef(ecucParamConfContainerDef, "RteRipsSupport");
    if (supportfreeedin instanceof EcucEnumerationParamDef) {
      EcucEnumerationParamDef enumdef = (EcucEnumerationParamDef) supportfreeedin;
      EcucTextualParamValue createEcucTextualParamValue = Autosar40Factory.eINSTANCE.createEcucTextualParamValue();
      createEcucTextualParamValue.setDefinition(enumdef);
      createEcucTextualParamValue.setValue("RTE_RIPS_ON");
      createEcucContainerValue.getParameterValues().add(createEcucTextualParamValue);
    }

    EcucReferenceValue ecucReferenceValue = null;
    EcucAbstractReferenceDef ecucReferenceDef =
        getEcucReferenceDef(ecucParamConfContainerDef, "RteRipsPluginConfigurationRef");
    if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucUriReferenceDef)) {
      EcucUriReferenceDef ecucReferenceDef1 = (EcucUriReferenceDef) ecucReferenceDef;
      ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
      ecucReferenceValue.setDefinition(ecucReferenceDef1);
      ecucReferenceValue.setIndex(Long.valueOf(0));
      if (this.tischedToEcuCValueMapping.isValidateTimedCom() &&
          (this.rteTimedInstance.getEcuConfiguration() != null)) {
        for (EcucContainerValue timedCont : this.rteTimedInstance.getEcuConfiguration().getContainers()) {
          if (timedCont.getDefinition().getShortName().equalsIgnoreCase("RteRipsPluginProps")) {
            ecucReferenceValue.setValue(timedCont);
            break;
          }
        }
      }
      createEcucContainerValue.getReferenceValues().add(ecucReferenceValue);
    }
  }


  /**
   * @param list
   * @param swComponentPrototype
   * @return
   * @throws Exception
   */
  private List<EcucContainerValue> createRteImplicitCommunication(final SwComponentPrototype swComponentPrototype,
      final Map<String, List<VariableAccess>> map, final EcucModuleDef ecucModuleDef)
      throws Exception {
    List<EcucContainerValue> createEcucContainerValueList = new ArrayList<EcucContainerValue>();


    Map<String, String> containerValueNameMap = new HashMap<String, String>();

    List<EcucModuleConfigurationValuesRefConditional> ecuCValueCollections =
        this.tischedToEcuCValueMapping.getEcuCValueCollections(this.rootSwCompositionProtoType);

    final List<String> vaList = new ArrayList<String>();

    this.tischedToEcuCValueMapping.getRteRipsPluginImplTimedComVariableAccessList(ecuCValueCollections, vaList);


    for (Entry<String, List<VariableAccess>> vl : map.entrySet()) {

      String name = "Rte_Rips_ImplTimedCom_" + swComponentPrototype.getShortName();


      if (vl.getKey().endsWith(RteConfigGeneratorConstants.DATA_WRITE_ACCESS)) {
        String r =
            vl.getKey().substring(0, vl.getKey().lastIndexOf("_" + RteConfigGeneratorConstants.DATA_WRITE_ACCESS));
        name = name + "_" + r;

        boolean createImplComm = false;
        if (!vaList.isEmpty()) {
          for (VariableAccess rv : vl.getValue()) {

            createImplComm = vaList.contains(rv.getShortName() + "_" + r) ? false : true;


          }
        }
        else {
          createImplComm = true;
        }
        if (createImplComm) {
          EcucContainerValue createEcucContainerValue = getRteImplicitCommunicationContainerValue(
              createEcucContainerValueList, name, swComponentPrototype, ecucModuleDef, containerValueNameMap);

          if (createEcucContainerValue != null) {
            for (VariableAccess rv : vl.getValue()) {

              if (!vaList.contains(rv.getShortName() + "_" + r)) {
                createEcucContainerValue.getReferenceValues().add(createEcucReferenceValue(rv,
                    (EcucParamConfContainerDef) createEcucContainerValue.getDefinition(), "RteVariableWriteAccessRef"));
              }
            }
            if (!createEcucContainerValueList.contains(createEcucContainerValue)) {
              createEcucContainerValueList.add(createEcucContainerValue);
            }
          }
        }
      }
      else if (vl.getKey().endsWith(RteConfigGeneratorConstants.DATA_READ_ACCESS)) {
        String r =
            vl.getKey().substring(0, vl.getKey().lastIndexOf("_" + RteConfigGeneratorConstants.DATA_READ_ACCESS));
        name = name + "_" + r;

        boolean createImplComm = false;
        if (!vaList.isEmpty()) {
          for (VariableAccess wv : vl.getValue()) {
            createImplComm = vaList.contains(wv.getShortName() + "_" + r) ? false : true;

          }
        }
        else {
          createImplComm = true;
        }
        if (createImplComm) {

          EcucContainerValue createEcucContainerValue = getRteImplicitCommunicationContainerValue(
              createEcucContainerValueList, name, swComponentPrototype, ecucModuleDef, containerValueNameMap);

          if (createEcucContainerValue != null) {
            for (VariableAccess wv : vl.getValue()) {
              if (!vaList.contains(wv.getShortName() + "_" + r)) {
                createEcucContainerValue.getReferenceValues().add(createEcucReferenceValue(wv,
                    (EcucParamConfContainerDef) createEcucContainerValue.getDefinition(), "RteVariableReadAccessRef"));
              }
            }

            if (!createEcucContainerValueList.contains(createEcucContainerValue)) {
              createEcucContainerValueList.add(createEcucContainerValue);
            }
          }
        }


      }

    }

    return createEcucContainerValueList;
  }

  private EcucContainerValue getRteImplicitCommunicationContainerValue(
      final List<EcucContainerValue> createEcucContainerValueList, final String containerValueName,
      final SwComponentPrototype swComponentPrototype, final EcucModuleDef ecucModuleDef,
      final Map<String, String> containerValueNameMap) {
    String tempcontainerValueName = null;

    String shortcontainerValueName = GenerateArxmlUtil.getShortenedNameOfElements(containerValueName,
        Collections.unmodifiableList(createEcucContainerValueList));

    if ((shortcontainerValueName != null) && !shortcontainerValueName.isEmpty() &&
        (containerValueName.length() > 127)) {
      containerValueNameMap.put(containerValueName, shortcontainerValueName);
    }

    tempcontainerValueName = containerValueNameMap.get(containerValueName);

    if (!createEcucContainerValueList.isEmpty()) {
      for (EcucContainerValue ecv : createEcucContainerValueList) {
        if (ecv.getShortName().equals((tempcontainerValueName == null ? containerValueName : tempcontainerValueName))) {
          return ecv;
        }
      }
    }

    EcucContainerValue createEcucContainerValue = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucModuleDef, "RteImplicitCommunication");
    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {


      EcucParamConfContainerDef ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;
      createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      createEcucContainerValue.setShortName(shortcontainerValueName);
      createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      createEcucContainerValue.setDefinition(ecucContainerDef);
      createEcucContainerValue.getReferenceValues().add(createEcucInstanceReferenceValue(swComponentPrototype,
          ecucParamConfContainerDef, "RteSoftwareComponentInstanceRef"));

      VariationPoint variationPoint =
          VariationPointUtil.getInstance().getVariationPoint(this.rootSwCompositionProtoType, swComponentPrototype);

      if (variationPoint != null) {
        createEcucContainerValue.setVariationPoint(variationPoint);
      }

      EcucParameterDef ecucParameterDef = getEcucParameterDef(ecucParamConfContainerDef, "RteTimedCommunication");
      if (ecucParameterDef != null) {

        EcucNumericalParamValue createEcucNumericalParamValue =
            Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
        createEcucNumericalParamValue.setDefinition(ecucParameterDef);
        createEcucNumericalParamValue.setValue(GenerateArxmlUtil.createNumericalValueVariationPoint("true"));
        createEcucContainerValue.getParameterValues().add(createEcucNumericalParamValue);

      }

      ecucParameterDef = getEcucParameterDef(ecucParamConfContainerDef, "RteImmediateBufferUpdate");
      if (ecucParameterDef != null) {

        EcucNumericalParamValue createEcucNumericalParamValue =
            Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
        createEcucNumericalParamValue.setDefinition(ecucParameterDef);
        createEcucNumericalParamValue.setValue(GenerateArxmlUtil.createNumericalValueVariationPoint("true"));
        createEcucContainerValue.getParameterValues().add(createEcucNumericalParamValue);

      }


      ecucParameterDef = getEcucParameterDef(ecucParamConfContainerDef, "RteCoherentAccess");
      if (ecucParameterDef != null) {

        EcucNumericalParamValue createEcucNumericalParamValue =
            Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
        createEcucNumericalParamValue.setDefinition(ecucParameterDef);
        createEcucNumericalParamValue.setValue(GenerateArxmlUtil.createNumericalValueVariationPoint("true"));
        createEcucContainerValue.getParameterValues().add(createEcucNumericalParamValue);

      }


    }
    return createEcucContainerValue;

  }


  /**
   * @param vl list@param ecucParamConfContainerDef
   * @param string
   * @return
   */
  private EcucAbstractReferenceValue createEcucReferenceValue(final VariableAccess varAccess,
      final EcucParamConfContainerDef ecucParamConfContainerDef, final String referenceName) {

    EcucReferenceValue createEcucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();

    for (EcucAbstractReferenceDef ecucEcucAbstractReferenceDef : ecucParamConfContainerDef.getReferences()) {

      if (ecucEcucAbstractReferenceDef.getShortName().equals(referenceName)) {
        createEcucReferenceValue.setDefinition(ecucEcucAbstractReferenceDef);
      }
    }

    VariationPoint variationPoint = VariationPointUtil.getInstance().getVariationPoint(varAccess);

    if (variationPoint != null) {
      createEcucReferenceValue.setVariationPoint(variationPoint);
    }


    createEcucReferenceValue.setValue(varAccess);
    return createEcucReferenceValue;
  }

  /**
   * @param swComponentPrototype
   * @param ecucParamConfContainerDef
   * @param string
   * @return
   */
  private EcucInstanceReferenceValue createEcucInstanceReferenceValue(final SwComponentPrototype swComponentPrototype,
      final EcucParamConfContainerDef ecucParamConfContainerDef, final String referenceName) {


    EcucInstanceReferenceValue createEcucInstanceReferenceValue =
        Autosar40Factory.eINSTANCE.createEcucInstanceReferenceValue();

    for (EcucAbstractReferenceDef ecucEcucAbstractReferenceDef : ecucParamConfContainerDef.getReferences()) {

      if (ecucEcucAbstractReferenceDef.getShortName().equals(referenceName)) {
        createEcucInstanceReferenceValue.setDefinition(ecucEcucAbstractReferenceDef);
      }
    }
    AnyInstanceRef ecuExtractReference = AnyinstancerefFactory.eINSTANCE.createAnyInstanceRef();
    ecuExtractReference.getContextElements().add(this.rootSwCompositionProtoType);
    ecuExtractReference.setTarget(swComponentPrototype);


    createEcucInstanceReferenceValue.setValue(ecuExtractReference);
    return createEcucInstanceReferenceValue;
  }

  private EcucContainerValue createRteOsInteraction(final EcucModuleDef ecucModuleDef,
      final List<TischedTask> tischedTasks) {
    EcucContainerValue createEcucContainerValue = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucModuleDef, "RteOsInteraction");

    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {

      EcucParamConfContainerDef ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;
      createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      createEcucContainerValue.setShortName("RteOsInteraction");
      createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      createEcucContainerValue.setDefinition(ecucContainerDef);

      int i = 0;
      for (TischedTask tischedTask : tischedTasks) {

        if (tischedTask.getTaskChain() != null) {

          EcucContainerValue rteOsActivation =
              getRteOsTaskChain(ecucParamConfContainerDef, tischedTask, "RteOsInteraction_" + i);

          if (rteOsActivation != null) {
            createEcucContainerValue.getSubContainers().add(rteOsActivation);
          }
          i++;
        }


      }
      // EcucContainerValue rteUsedOsActivation = getRteUsedOsActivation(ecucParamConfContainerDef, tischedTask);
      // if (rteUsedOsActivation != null) {
      // createEcucContainerValue.getSubContainers().add(rteUsedOsActivation);
      // }


    }

    return createEcucContainerValue;
  }


  private EcucContainerValue getRteOsTaskChain(final EcucParamConfContainerDef ecucModuleDef,
      final TischedTask tischedTask, final String taskChainName) {
    EcucContainerValue createEcucContainerValue = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucModuleDef, "RteOsTaskChain");

    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {

      EcucParamConfContainerDef ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;
      createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      createEcucContainerValue.setShortName(taskChainName);
      createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      createEcucContainerValue.setDefinition(ecucParamConfContainerDef);


      EcucReferenceValue ecucReferenceValue = null;
      EcucAbstractReferenceDef ecucReferenceDef =
          getEcucReferenceDef(ecucParamConfContainerDef, "RtePredecessorOsTaskRef");


      if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucReferenceDef)) {
        EcucReferenceDef ecucReferenceDef1 = (EcucReferenceDef) ecucReferenceDef;
        ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
        ecucReferenceValue.setDefinition(ecucReferenceDef1);
        ecucReferenceValue.setValue((Referrable) tischedTask.getTaskInstance());
      }
      createEcucContainerValue.getReferenceValues().add(ecucReferenceValue);


      ecucReferenceDef = getEcucReferenceDef(ecucParamConfContainerDef, "RteSuccessorOsTaskRef");


      if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucReferenceDef)) {
        EcucReferenceDef ecucReferenceDef1 = (EcucReferenceDef) ecucReferenceDef;
        ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
        ecucReferenceValue.setDefinition(ecucReferenceDef1);
        ecucReferenceValue.setValue((Referrable) tischedTask.getTaskChain());
      }
      createEcucContainerValue.getReferenceValues().add(ecucReferenceValue);


    }
    return createEcucContainerValue;
  }

  private EcucContainerValue getRteUsedOsActivation(final EcucParamConfContainerDef ecucModuleDef,
      final TischedTask tischedTask) {
    EcucContainerValue createEcucContainerValue = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucModuleDef, "RteUsedOsActivation");

    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {

      EcucParamConfContainerDef ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;
      createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      createEcucContainerValue.setShortName("RteUsedOsActivation");
      createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      createEcucContainerValue.setDefinition(ecucParamConfContainerDef);


      if ((tischedTask.getTaskActivationEnum() == TaskActivationEnum.OS_ALARM) &&
          (tischedTask.getAlarmInstance() != null)) {
        EcucReferenceValue ecucReferenceValue = null;
        EcucAbstractReferenceDef ecucReferenceDef =
            getEcucReferenceDef(ecucParamConfContainerDef, "RteActivationOsAlarmRef");


        if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucReferenceDef)) {
          EcucReferenceDef ecucReferenceDef1 = (EcucReferenceDef) ecucReferenceDef;
          ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
          ecucReferenceValue.setDefinition(ecucReferenceDef1);
          ecucReferenceValue.setValue((Referrable) tischedTask.getAlarmInstance());
        }
        createEcucContainerValue.getReferenceValues().add(ecucReferenceValue);
      }
      else if ((tischedTask.getTaskActivationEnum() == TaskActivationEnum.OS_SCHEDULE_TABLE) &&
          (tischedTask.getOsScheduleTableInstance() != null)) {
        EcucReferenceValue ecucReferenceValue = null;
        EcucAbstractReferenceDef ecucReferenceDef =
            getEcucReferenceDef(ecucParamConfContainerDef, "RteActivationOsSchTblRef");


        if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucReferenceDef)) {
          EcucReferenceDef ecucReferenceDef1 = (EcucReferenceDef) ecucReferenceDef;
          ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
          ecucReferenceValue.setDefinition(ecucReferenceDef1);
          ecucReferenceValue.setValue((Referrable) tischedTask.getOsScheduleTableInstance());
        }
        createEcucContainerValue.getReferenceValues().add(ecucReferenceValue);
      }

      EcucReferenceValue ecucReferenceValue = null;
      EcucAbstractReferenceDef ecucReferenceDef =
          getEcucReferenceDef(ecucParamConfContainerDef, "RteActivationOsTaskRef");


      if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucReferenceDef)) {
        EcucReferenceDef ecucReferenceDef1 = (EcucReferenceDef) ecucReferenceDef;
        ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
        ecucReferenceValue.setDefinition(ecucReferenceDef1);
        ecucReferenceValue.setValue((Referrable) tischedTask.getTaskInstance());
      }


    }
    return createEcucContainerValue;
  }


  private EcucContainerDef getEcucContainerDef(final EcucModuleDef ecucModuleDef, final String shortName) {
    EcucContainerDef ecucContainerDef = null;
    for (EcucContainerDef item : ecucModuleDef.getContainers()) {
      if (item.getShortName().equals(shortName)) {
        return item;
      }
    }
    return ecucContainerDef;
  }

  private EcucContainerDef getEcucContainerDef(final EcucParamConfContainerDef ecucModuleDef, final String shortName) {
    EcucContainerDef ecucContainerDef = null;
    for (EcucContainerDef item : ecucModuleDef.getSubContainers()) {
      if (item.getShortName().equals(shortName)) {
        return item;
      }
    }
    return ecucContainerDef;
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


  private EcucAbstractReferenceDef getEcucReferenceDef(final EcucParamConfContainerDef ecucParamConfContainerDef,
      final String shortName) {
    EcucAbstractReferenceDef ecucAbstractReferenceDef = null;
    for (EcucAbstractReferenceDef item : ecucParamConfContainerDef.getReferences()) {
      if (item.getShortName().equals(shortName)) {
        return item;
      }
    }
    return ecucAbstractReferenceDef;
  }


  private EcucTextualParamValue createEcucTextualParamValue(final String shortName,
      final EcucParamConfContainerDef ecucParamConfContainerDef) {

    EcucTextualParamValue createEcucTextualParamValue = null;
    EcucParameterDef ecucParameterDef = getEcucParameterDef(ecucParamConfContainerDef, shortName);

    if (ecucParameterDef != null) {
      createEcucTextualParamValue = Autosar40Factory.eINSTANCE.createEcucTextualParamValue();
      createEcucTextualParamValue.setDefinition(ecucParameterDef);
    }
    return createEcucTextualParamValue;
  }

  private EcucNumericalParamValue createEcucNumericalParamValue(final String shortName,
      final EcucParamConfContainerDef ecucParamConfContainerDef) {

    EcucNumericalParamValue createEcucNumericalParamValue = null;
    EcucParameterDef ecucParameterDef = getEcucParameterDef(ecucParamConfContainerDef, shortName);

    if (ecucParameterDef != null) {
      createEcucNumericalParamValue = Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
      createEcucNumericalParamValue.setDefinition(ecucParameterDef);
    }
    return createEcucNumericalParamValue;
  }

  private EcucContainerValue createRteGeneration(final EcucModuleDef ecucModuleDef) {

    EcucContainerValue createEcucContainerValue = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucModuleDef, "RteGeneration");

    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {
      EcucParamConfContainerDef ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;
      createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      createEcucContainerValue.setShortName("RteGeneration");
      createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      createEcucContainerValue.setDefinition(ecucContainerDef);


      EcucTextualParamValue createEcucTextualParamValue =
          createEcucTextualParamValue("RteCalibrationSupport", ecucParamConfContainerDef);

      if (createEcucTextualParamValue != null) {
        createEcucTextualParamValue.setValue("NONE");
        createEcucContainerValue.getParameterValues().add(createEcucTextualParamValue);
      }

      createEcucTextualParamValue = createEcucTextualParamValue("RteGenerationMode", ecucParamConfContainerDef);

      if (createEcucTextualParamValue != null) {
        createEcucTextualParamValue.setValue("VENDOR_MODE");
        createEcucContainerValue.getParameterValues().add(createEcucTextualParamValue);
      }

      EcucNumericalParamValue createEcucNumericalParamValue =
          createEcucNumericalParamValue("RteMeasurementSupport", ecucParamConfContainerDef);

      if (createEcucNumericalParamValue != null) {
        createEcucNumericalParamValue.setValue(GenerateArxmlUtil.createNumericalValueVariationPoint("1"));
        createEcucContainerValue.getParameterValues().add(createEcucNumericalParamValue);
      }

      createEcucTextualParamValue = createEcucTextualParamValue("RteOptimizationMode", ecucParamConfContainerDef);

      if (createEcucTextualParamValue != null) {
        createEcucTextualParamValue.setValue("RUNTIME");
        createEcucContainerValue.getParameterValues().add(createEcucTextualParamValue);
      }

      createEcucNumericalParamValue =
          createEcucNumericalParamValue("RteToolChainSignificantCharacters", ecucParamConfContainerDef);

      if (createEcucNumericalParamValue != null) {
        createEcucNumericalParamValue.setValue(GenerateArxmlUtil.createNumericalValueVariationPoint("127"));
        createEcucContainerValue.getParameterValues().add(createEcucNumericalParamValue);
      }

    }
    return createEcucContainerValue;

  }


  private EcucContainerValue createRteSwComponentInstance(final TischedComponentInstance componentInstance,
      final EcucModuleDef ecucModuleDef)
      throws Exception {

    EcucContainerValue createEcucContainerValue = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucModuleDef, "RteSwComponentInstance");

    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {
      EcucParamConfContainerDef ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;
      createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      createEcucContainerValue.setShortName(componentInstance.getShortName());
      createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      createEcucContainerValue.setDefinition(ecucContainerDef);
      createEcucContainerValue.getReferenceValues().add(
          createEcucReferenceValue(componentInstance, ecucParamConfContainerDef, "RteSoftwareComponentInstanceRef"));

      VariationPoint variationPoint =
          VariationPointUtil.getInstance().getVariationPoint(componentInstance.getComponentInstance());

      if (variationPoint != null) {
        createEcucContainerValue.setVariationPoint(variationPoint);
      }

      TischedComponent tischedComponent = componentInstance.getTischedComponent();
      if (tischedComponent != null) {
        List<TischedEvent> eventlist = this.tischedToEcuCValueMapping.getComponentInstaceToEventsMap()
            .get(componentInstance.getTischedComponent().getShortName() + componentInstance.getShortName());
        if ((eventlist != null) && !eventlist.isEmpty()) {
          for (TischedEvent event : eventlist) {

            List<TischedEvent> list =
                this.tischedToEcuCValueMapping.getInstanceEventMap().get(componentInstance.getShortName());

            if ((list != null) && list.contains(event)) {
              EcucContainerValue createRteEventToTaskMapping = createEventToTaskMapping(event, "RteEventToTaskMapping",
                  "RteEventRef", "RteMappedToTaskRef", "RtePositionInTask", "RteActivationOffset",
                  "RteImmediateRestart", "RteRipsFillRoutineRef", "RteRipsFlushRoutineRef", ecucParamConfContainerDef);
              createEcucContainerValue.getSubContainers().add(createRteEventToTaskMapping);
            }
          }
        }
      }


    }
    return createEcucContainerValue;

  }

  private void generateTimingEventsToTaskMappingReport() {
    LOGGER.info("***Generating Timing events to task mapping file***");

    StringBuilder sb = new StringBuilder()
        .append("================Timing events to task mapping report=================\n\n").append("(sample format)\n")
        .append("Task :: taskcycle \n\n").append("  Event list :: eventperiod\n").append(
            "======================================================================================================\n");

    this.eventListToTaskMap.forEach((task, events) -> {
      sb.append(task.getShortName()).append(" :: ");

      try {
//        double taskCycle = Double.parseDouble(task.getTaskCycle()) / 1000.0;
//        sb.append(taskCycle);
        BigDecimal taskCycle = BigDecimal.valueOf(Double.parseDouble(task.getTaskCycle()) / 1000.0);
        sb.append(taskCycle.stripTrailingZeros().toPlainString());
      }
      catch (NumberFormatException e) {
        LOGGER.warn("Invalid task cycle format for task {}: {}", task.getShortName(), task.getTaskCycle());
        sb.append("Invalid Task Cycle");
      }

      sb.append("\n\n");

      events.forEach(evt -> {
        EObject eventEObject = evt.getEvent();
        EAttribute periodAttribute = (EAttribute) eventEObject.eClass().getEStructuralFeature("period");

        Object periodValue = eventEObject.eGet(periodAttribute);
        String periodString = (periodValue != null) ? periodValue.toString() : "not set";

        sb.append("\t").append(evt.getShortName()).append(" :: ");

        if (periodString.equals("null or not set")) {
          sb.append(periodString);
        }
        else {
          try {
//            double period = Double.parseDouble(periodString);
//            sb.append(period);
            BigDecimal period = BigDecimal.valueOf(Double.parseDouble(periodString));
            sb.append(period.stripTrailingZeros().toPlainString());
          }
          catch (NumberFormatException e) {
            LOGGER.warn("Invalid period format for event {}: {}", evt.getShortName(), periodString);
            sb.append("Invalid Period");
          }
        }

        sb.append("\n");
      });

      sb.append("\n").append(
          "=======================================================================================================\n");
    });


    String timingTaskConfigPath = this.rteconfgenlogPath.substring(0, this.rteconfgenlogPath.lastIndexOf("/"));
    String reportPath = this.project.getLocation().toString() + timingTaskConfigPath + "/task_event_evperiod.txt";

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportPath))) {
      writer.write(sb.toString());
      LOGGER.info("***Timing events to task mapping file successfully generated (task_event_evperiod.txt)***");
    }
    catch (IOException ex) {
      LOGGER.error("Failed to generate Timing events to task mapping file", ex);
    }
  }


  private EcucContainerValue createEventToTaskMapping(final TischedEvent event, final String eventMapping,
      final String eventRef, final String taskRef, final String positionInTask, final String activationOffset,
      final String immediateRestart, final String rteFillRoutine, final String rteFlushRoutine,
      final EcucParamConfContainerDef ecucParamConfContainerDef)
      throws Exception {


    EcucContainerValue createEcucContainerValue = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucParamConfContainerDef, eventMapping);


    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {
      EcucParamConfContainerDef ecucParamConfContainerDef1 = (EcucParamConfContainerDef) ecucContainerDef;
      createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      createEcucContainerValue.setShortName(event.getShortName());
      createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      createEcucContainerValue.setDefinition(ecucContainerDef);
      createEcucContainerValue.getReferenceValues()
          .add(createEcucReferenceValue(event, ecucParamConfContainerDef1, eventRef));

      VariationPoint variationPoint = VariationPointUtil.getInstance().getVariationPoint(event.getEvent());

      if (variationPoint != null) {
        createEcucContainerValue.setVariationPoint(variationPoint);
      }

      // fill and flush routines for First and Last respective Events of the task which is affected by TIMEDCOM
      if (event.getTischedTask() != null) {
        if (this.tischedToEcuCValueMapping.isValidateTimedCom() &&
            this.rteTimedInstance.getTimedComTasks().contains(event.getTischedTask().getShortName())) {
          generateFillFlushRoutine(event, rteFillRoutine, rteFlushRoutine, ecucParamConfContainerDef1,
              createEcucContainerValue);
        }


        if ((this.generateRteRipsCSXfrmEcucValues.getEventEcucContainerMap() != null) &&
            !this.generateRteRipsCSXfrmEcucValues.getEventEcucContainerMap().keySet().isEmpty()) {

          EcucContainerValue ecucContainerValue = null;
          String runnableEntityName = (event.getEventTypeEnum() == EventEnum.RTEEVENT)
              ? (((RTEEvent) event.getEvent()).getStartOnEvent().getShortName())
              : (((BswEvent) event.getEvent()).getStartsOnEvent().getShortName());

          String startonAndEventName = this.generateRteRipsCSXfrmEcucValues.getShortenedEventEcucConNameMap()
              .get(event.getShortName() + "_" + runnableEntityName);

          if (startonAndEventName != null) {
            ecucContainerValue =
                this.generateRteRipsCSXfrmEcucValues.getEventEcucContainerMap().get(startonAndEventName);
          }
          else {
            ecucContainerValue = this.generateRteRipsCSXfrmEcucValues.getEventEcucContainerMap()
                .get(event.getShortName() + "_" + runnableEntityName);
          }
          if (ecucContainerValue != null) {

            EcucReferenceValue createRteRipsInvocationHandlerRef =
                createRteRipsInvocationHandlerRef(ecucParamConfContainerDef1, ecucContainerValue);

            if (createRteRipsInvocationHandlerRef != null) {

              createEcucContainerValue.getReferenceValues().add(createRteRipsInvocationHandlerRef);
              this.isRipsInvocationRefAdded = true;
            }

          }


        }


        EcucReferenceValue createTaskRef =
            createTaskRef(event, taskRef, event.getTischedTask().getShortName(), ecucParamConfContainerDef1);
        createEcucContainerValue.getReferenceValues().add(createTaskRef);

        if ((createTaskRef != null) &&
            (((event.getEventTypeEnum() == EventEnum.RTEEVENT) && (event.getEvent() instanceof TimingEvent)) ||
                ((event.getEventTypeEnum() == EventEnum.BSWEVENT) && (event.getEvent() instanceof BswTimingEvent)))) {

          TischedTask tischedTask = event.getTischedTask();
          this.eventTaskMap.put(tischedTask, event);
          this.eventListToTaskMap.computeIfAbsent(tischedTask, k -> new ArrayList<>()).add(event);
        }

      }
      else {
        if (((event.getEventTypeEnum() == EventEnum.RTEEVENT) && (event.getEvent() instanceof TimingEvent)) ||
            ((event.getEventTypeEnum() == EventEnum.BSWEVENT) && (event.getEvent() instanceof BswTimingEvent))) {
          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("114_0", event.getShortName()).trim());

        }
      }


      EcucParameterDef ecucParameterDef = getEcucParameterDef(ecucParamConfContainerDef1, positionInTask);
      if (ecucParameterDef != null) {

        EcucNumericalParamValue createEcucNumericalParamValue =
            Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
        createEcucNumericalParamValue.setDefinition(ecucParameterDef);
        createEcucNumericalParamValue
            .setValue(GenerateArxmlUtil.createNumericalValueVariationPoint(event.getPosition() + ""));
        createEcucContainerValue.getParameterValues().add(createEcucNumericalParamValue);

      }

      ecucParameterDef = getEcucParameterDef(ecucParamConfContainerDef1, activationOffset);
      if (ecucParameterDef != null) {

        EcucNumericalParamValue createEcucNumericalParamValue =
            Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
        createEcucNumericalParamValue.setDefinition(ecucParameterDef);
        if ((event.getOffset() != null) && !event.getOffset().isEmpty() && !event.getOffset().isBlank()) {
          try {
            Double.parseDouble(event.getOffset());
            createEcucNumericalParamValue
                .setValue(GenerateArxmlUtil.createNumericalValueVariationPoint(event.getOffset()));
          }
          catch (NumberFormatException e) {
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("267_0", event.getShortName()).trim());
            createEcucNumericalParamValue.setValue(GenerateArxmlUtil.createNumericalValueVariationPoint("0.0"));
          }
        }
        else {
          createEcucNumericalParamValue.setValue(GenerateArxmlUtil.createNumericalValueVariationPoint("0.0"));
        }
        createEcucContainerValue.getParameterValues().add(createEcucNumericalParamValue);
      }

      ecucParameterDef = getEcucParameterDef(ecucParamConfContainerDef1, "RteOsSchedulePoint");
      if (ecucParameterDef != null) {
        EcucTextualParamValue createEcucTextualParamValue = Autosar40Factory.eINSTANCE.createEcucTextualParamValue();
        createEcucTextualParamValue.setDefinition(ecucParameterDef);
        createEcucTextualParamValue.setValue(event.isCoOperative() ? "CONDITIONAL" : "NONE");
        createEcucContainerValue.getParameterValues().add(createEcucTextualParamValue);
      }

      ecucParameterDef = getEcucParameterDef(ecucParamConfContainerDef1, immediateRestart);
      if (ecucParameterDef != null) {

        EcucNumericalParamValue createEcucNumericalParamValue =
            Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
        createEcucNumericalParamValue.setDefinition(ecucParameterDef);
        createEcucNumericalParamValue.setValue(GenerateArxmlUtil.createNumericalValueVariationPoint("false"));
        createEcucContainerValue.getParameterValues().add(createEcucNumericalParamValue);

      }
    }
    return createEcucContainerValue;

  }


  /**
   * @param event
   * @param rteFillRoutine
   * @param rteFlushRoutine
   * @param createEcucContainerValue
   * @param ecucParamConfContainerDef1
   * @throws Exception
   */
  private void generateFillFlushRoutine(final TischedEvent event, final String rteFillRoutine,
      final String rteFlushRoutine, final EcucParamConfContainerDef ecucParamConfContainerDef1,
      final EcucContainerValue createEcucContainerValue)
      throws Exception {
    TischedTask task = event.getTischedTask();


    if ((event.getPosition() == 1) && (task.getEvents().size() == 1)) {
      EcucReferenceValue createTaskfillRef = createRteFillFlushRoutineRef(event, rteFillRoutine,
          ecucParamConfContainerDef1, task.getShortName() + "_fill");
      createEcucContainerValue.getReferenceValues().add(createTaskfillRef);
      EcucReferenceValue createTaskflushRef = createRteFillFlushRoutineRef(event, rteFlushRoutine,
          ecucParamConfContainerDef1, task.getShortName() + "_flush");
      createEcucContainerValue.getReferenceValues().add(createTaskflushRef);

    }
    else if (event.getPosition() == task.getEvents().size())

    {
      EcucReferenceValue createTaskRef = createRteFillFlushRoutineRef(event, rteFlushRoutine,
          ecucParamConfContainerDef1, task.getShortName() + "_flush");
      createEcucContainerValue.getReferenceValues().add(createTaskRef);
    }
    else if (event.getPosition() == 1) {
      EcucReferenceValue createTaskRef = createRteFillFlushRoutineRef(event, rteFillRoutine, ecucParamConfContainerDef1,
          task.getShortName() + "_fill");
      createEcucContainerValue.getReferenceValues().add(createTaskRef);

    }
  }

  private EcucReferenceValue createRteFillFlushRoutineRef(final TischedEvent event, final String rtefillflushroutineRef,
      final EcucParamConfContainerDef ecucParamConfContainerDef, final String fillflushContainerName)
      throws Exception {


    EcucReferenceValue ecucReferenceValue = null;
    EcucAbstractReferenceDef ecucReferenceDef = getEcucReferenceDef(ecucParamConfContainerDef, rtefillflushroutineRef);


    if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucUriReferenceDef)) {
      EcucUriReferenceDef ecucReferenceDef1 = (EcucUriReferenceDef) ecucReferenceDef;
      ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
      ecucReferenceValue.setDefinition(ecucReferenceDef1);
      EcucContainerValue container = this.rteTimedInstance.getFillFlushEcucContainer(fillflushContainerName);
      if (container != null) {
        ecucReferenceValue.setValue(container);
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("123_0", event.getShortName()).trim());

      }
      else {
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("124_0", event.getShortName()).trim());

      }
    }
    return ecucReferenceValue;

  }


  private EcucReferenceValue createRteRipsInvocationHandlerRef(
      final EcucParamConfContainerDef ecucParamConfContainerDef, final EcucContainerValue ecuContainerValue) {


    EcucReferenceValue ecucReferenceValue = null;
    EcucAbstractReferenceDef ecucReferenceDef =
        getEcucReferenceDef(ecucParamConfContainerDef, "RteRipsInvocationHandlerRef");


    if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucUriReferenceDef)) {
      EcucUriReferenceDef ecucReferenceDef1 = (EcucUriReferenceDef) ecucReferenceDef;
      ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
      ecucReferenceValue.setDefinition(ecucReferenceDef1);

      ecucReferenceValue.setValue(ecuContainerValue);
    }
    return ecucReferenceValue;

  }


  private EcucReferenceValue createTaskRef(final TischedEvent event, final String taskRef, final String eventRef,
      final EcucParamConfContainerDef ecucParamConfContainerDef) {


    EcucReferenceValue ecucReferenceValue = null;
    EcucAbstractReferenceDef ecucReferenceDef = getEcucReferenceDef(ecucParamConfContainerDef, taskRef);


    if ((ecucReferenceDef != null) && (ecucReferenceDef instanceof EcucReferenceDef)) {
      EcucReferenceDef ecucReferenceDef1 = (EcucReferenceDef) ecucReferenceDef;
      ecucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
      // ecucReferenceValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      ecucReferenceValue.setDefinition(ecucReferenceDef1);
      ecucReferenceValue.setValue((Referrable) event.getTischedTask().getTaskInstance());


    }
    return ecucReferenceValue;

  }


  private EcucContainerValue createRteBswModuleInstance(final TischedComponentInstance componentInstance,
      final EcucModuleDef ecucModuleDef, final Map<String, EcucContainerValue> triggerModInstnceMap,
      final Map<String, List<String>> compInstanceToReqTriggerMap, final Map<String, Trigger> uriToTriggerMap)
      throws Exception {

    EcucContainerValue createEcucContainerValue = null;
    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucModuleDef, "RteBswModuleInstance");

    if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {
      EcucParamConfContainerDef ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;
      createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
      createEcucContainerValue.setShortName(componentInstance.getShortName());
      createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
      createEcucContainerValue.setDefinition(ecucContainerDef);

      if (!this.tischedToEcuCValueMapping.getConfiguredBswInstanceSet().contains(componentInstance.getShortName())) {
        createEcucContainerValue.getReferenceValues()
            .add(createEcucReferenceValue(componentInstance, ecucParamConfContainerDef, "RteBswImplementationRef"));
      }
      else {

        List<? extends EClass> list = GenerateArxmlUtil.getEObject(this.project,
            GenerateArxmlUtil.getPackagePath(componentInstance.getComponentInstance()),
            componentInstance.getComponentInstance().eClass().getClass());

        if (list.isEmpty()) {
          createEcucContainerValue.getReferenceValues()
              .add(createEcucReferenceValue(componentInstance, ecucParamConfContainerDef, "RteBswImplementationRef"));
        }

      }


      TischedComponent tischedComponent = componentInstance.getTischedComponent();
      if (tischedComponent != null) {
        List<TischedEvent> eventlist = this.tischedToEcuCValueMapping.getComponentInstaceToEventsMap()
            .get(componentInstance.getTischedComponent().getShortName() + componentInstance.getShortName());
        if ((eventlist != null) && !eventlist.isEmpty()) {
          for (TischedEvent event : eventlist) {

            List<TischedEvent> list =
                this.tischedToEcuCValueMapping.getInstanceEventMap().get(componentInstance.getShortName());

            if ((list != null) && list.contains(event)) {
              EcucContainerValue createRteEventToTaskMapping =
                  createEventToTaskMapping(event, "RteBswEventToTaskMapping", "RteBswEventRef", "RteBswMappedToTaskRef",
                      "RteBswPositionInTask", "RteBswActivationOffset", "RteBswImmediateRestart",
                      "RteRipsFillRoutineRef", "RteRipsFlushRoutineRef", ecucParamConfContainerDef);
              createEcucContainerValue.getSubContainers().add(createRteEventToTaskMapping);
            }
          }

          if (this.tischedToEcuCValueMapping.isbAutoTriggerconnection()) {
            List<EcucContainerValue> createRteBswRequiredTriggerConnectionList = createRteBswRequiredTriggerConnection(
                componentInstance, ecucParamConfContainerDef, triggerModInstnceMap, compInstanceToReqTriggerMap,
                uriToTriggerMap, "RteBswRequiredTriggerConnection", "RteBswReleasedTriggerModInstRef",
                "RteBswRequiredTriggerRef", "RteBswReleasedTriggerRef");
            if (!createRteBswRequiredTriggerConnectionList.isEmpty()) {
              EList<EcucContainerValue> subContainers = createEcucContainerValue.getSubContainers();
              createRteBswRequiredTriggerConnectionList.stream().forEach(e -> subContainers.add(e));
            }
          }
        }
      }
    }
    return createEcucContainerValue;
  }

  /**
   * @param componentInstance
   * @param string
   * @param ecucParamConfContainerDef
   * @param compInstanceToReqTriggerMap
   * @param uriToTriggerMap
   * @param triggerModInstnceMap
   * @return
   */
  private List<EcucContainerValue> createRteBswRequiredTriggerConnection(
      final TischedComponentInstance componentInstance, final EcucParamConfContainerDef ecucParamConfContainerDef,
      final Map<String, EcucContainerValue> triggerModInstnceMap,
      final Map<String, List<String>> compInstanceToReqTriggerMap, final Map<String, Trigger> uriToTriggerMap,
      final String triggerConnection, final String releaseTriggerModRef, final String requiredTriggerRef,
      final String releaseTriggerRef) {

    List<EcucContainerValue> createEcucContainerValueList = new ArrayList<EcucContainerValue>();
    EcucContainerValue createEcucContainerValue = null;

    EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucParamConfContainerDef, triggerConnection);
    List<Entry<String, List<String>>> collect = compInstanceToReqTriggerMap.entrySet().stream()
        .filter(e -> e.getKey().split("_ReleaseTrigger_")[0].equals(componentInstance.getShortName()))
        .collect(Collectors.toList());
    if ((collect != null) && !collect.isEmpty()) {
      for (Entry<String, List<String>> e : collect) {
        String[] keyset = e.getKey().split("_ReleaseTrigger_");
        if (keyset.length == 2) {

          Trigger releaseTrigger = uriToTriggerMap.get(keyset[1]);
          if (releaseTrigger != null) {
            EcucContainerValue bswModInRef = triggerModInstnceMap.get(keyset[1]);

            if (bswModInRef != null) {
              List<String> reqTriggerURIList = e.getValue();
              if (!reqTriggerURIList.isEmpty()) {
                for (String reqTriggerURI : reqTriggerURIList) {
                  Trigger reqTrigger = uriToTriggerMap.get(reqTriggerURI);

                  if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {

                    EcucParamConfContainerDef ecucParamConfContainerDef1 = (EcucParamConfContainerDef) ecucContainerDef;
                    createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
                    createEcucContainerValue.setShortName(componentInstance.getShortName() + "_" +
                        reqTrigger.getShortName() + "_" + releaseTrigger.getShortName());
                    createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
                    createEcucContainerValue.setDefinition(ecucContainerDef);

                    createEcucContainerValue.getReferenceValues().add(
                        createTriggerEcucReferenceValue(ecucParamConfContainerDef1, releaseTriggerModRef, bswModInRef));
                    createEcucContainerValue.getReferenceValues().add(
                        createTriggerEcucReferenceValue(ecucParamConfContainerDef1, requiredTriggerRef, reqTrigger));
                    createEcucContainerValue.getReferenceValues().add(
                        createTriggerEcucReferenceValue(ecucParamConfContainerDef1, releaseTriggerRef, releaseTrigger));
                    createEcucContainerValueList.add(createEcucContainerValue);
                  }
                }
              }
            }
          }
        }
      }
    }
    return createEcucContainerValueList;
  }

  /**
   * @param referrable
   * @param ecucParamConfContainerDef1
   * @param releaseTriggerModRef
   * @return
   */
  private EcucReferenceValue createTriggerEcucReferenceValue(final EcucParamConfContainerDef ecucParamConfContainerDef,
      final String referencepath, final EObject referrable) {


    EcucReferenceValue createEcucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();
    for (EcucAbstractReferenceDef ecucEcucAbstractReferenceDef : ecucParamConfContainerDef.getReferences()) {

      if (ecucEcucAbstractReferenceDef.getShortName().equals(referencepath)) {
        createEcucReferenceValue.setDefinition(ecucEcucAbstractReferenceDef);
      }
    }

    createEcucReferenceValue.setValue((Referrable) referrable);


    return createEcucReferenceValue;
  }

  private EcucReferenceValue createEcucReferenceValue(final TischedComponentInstance componentInstance,
      final EcucParamConfContainerDef ecucParamConfContainerDef, final String referenceName) {
    EcucReferenceValue createEcucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();

    for (EcucAbstractReferenceDef ecucEcucAbstractReferenceDef : ecucParamConfContainerDef.getReferences()) {

      if (ecucEcucAbstractReferenceDef.getShortName().equals(referenceName)) {
        createEcucReferenceValue.setDefinition(ecucEcucAbstractReferenceDef);
      }
    }

    createEcucReferenceValue.setValue((Referrable) componentInstance.getComponentInstance());
    return createEcucReferenceValue;

  }

  private EcucReferenceValue createEcucReferenceValue(final TischedEvent tischedEvent,
      final EcucParamConfContainerDef ecucParamConfContainerDef, final String referenceName) {
    EcucReferenceValue createEcucReferenceValue = Autosar40Factory.eINSTANCE.createEcucReferenceValue();

    for (EcucAbstractReferenceDef ecucEcucAbstractReferenceDef : ecucParamConfContainerDef.getReferences()) {

      if (ecucEcucAbstractReferenceDef.getShortName().equals(referenceName)) {
        createEcucReferenceValue.setDefinition(ecucEcucAbstractReferenceDef);
      }
    }

    createEcucReferenceValue.setValue((Referrable) tischedEvent.getEvent());


    return createEcucReferenceValue;

  }


}
