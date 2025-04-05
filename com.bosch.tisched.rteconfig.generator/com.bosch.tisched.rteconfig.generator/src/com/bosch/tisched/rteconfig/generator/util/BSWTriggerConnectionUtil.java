/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.artop.aal.workspace.preferences.IAutosarWorkspacePreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;

import com.bosch.tisched.rteconfig.generator.core.OsConfigToEcucValueMapping;
import com.bosch.tisched.rteconfig.generator.core.TischedEvent;
import com.bosch.tisched.rteconfig.generator.core.TischedTask;

import autosar40.bswmodule.bswbehavior.BswCalledEntity;
import autosar40.bswmodule.bswbehavior.BswEvent;
import autosar40.bswmodule.bswbehavior.BswExternalTriggerOccurredEvent;
import autosar40.bswmodule.bswbehavior.BswInterruptEntity;
import autosar40.bswmodule.bswbehavior.BswModuleEntity;
import autosar40.bswmodule.bswbehavior.BswSchedulableEntity;
import autosar40.bswmodule.bswinterfaces.BswModuleEntry;
import autosar40.commonstructure.triggerdeclaration.Trigger;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucReferenceValue;

/**
 * @author DTB1KOR
 */
public class BSWTriggerConnectionUtil {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(ASWTriggerConnectionUtil.class.getName());
  private final OsConfigToEcucValueMapping osConfigToEcucValueMapping;
  Map<String, List<BswExternalTriggerOccurredEvent>> osTaskToBSWETOMap = new HashMap<>();
  private final List<TischedTask> etoTaskList;
  private final IProject project;
  private final Map<String, String> props;
  private final TransactionalEditingDomain editingDomain;
  private final IMetaModelDescriptor autosarRelease;
  List<EcucContainerValue> bswRequiredTriggerConList = new ArrayList<>();

  /**
   * @param project
   * @param osConfigToEcucValueMapping
   * @param props
   */
  public BSWTriggerConnectionUtil(final IProject project, final OsConfigToEcucValueMapping osConfigToEcucValueMapping,
      final Map<String, String> props) {
    this.osConfigToEcucValueMapping = osConfigToEcucValueMapping;
    this.etoTaskList = this.osConfigToEcucValueMapping.getETOTaskList();
    this.project = project;
    this.props = props;
    this.autosarRelease = IAutosarWorkspacePreferences.AUTOSAR_RELEASE.get(this.project);
    this.editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(this.project, this.autosarRelease);
    updateBSWTriggerConnectionList();
  }

  /**
   * @throws Exception
   */
  public void validateBSWTriggerPorts() throws Exception {
    updateOsTaskToBSWETOMap();

    for (Entry<String, List<BswExternalTriggerOccurredEvent>> entry : this.osTaskToBSWETOMap.entrySet()) {
      List<String> relTriggerAndRelTriggerModList = new ArrayList<String>();
      List<EcucContainerValue> bswConnList =
          getBSWTriggerConnectionListForReqTrigger(entry.getValue(), relTriggerAndRelTriggerModList);

      if (relTriggerAndRelTriggerModList.size() > 1) {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("275_0", entry.getKey()));

      }
      if (bswConnList.isEmpty()) {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("275_1", entry.getKey()));
      }
    }
  }

  /**
   * @throws Exception
   */
  public void validateBSWTriggerConnections() throws Exception {

    Map<String, String> schedulableEntityToETOmap = new HashMap<>();

    this.osConfigToEcucValueMapping.getTischedEvents().forEach(e -> {
      if (e.getEvent() instanceof BswEvent) {
        BswEvent eto = (BswEvent) e.getEvent();
        BswModuleEntity moduleEntity = eto.getStartsOnEvent();
        if ((moduleEntity != null) && !moduleEntity.eIsProxy() && (moduleEntity instanceof BswSchedulableEntity)) {
          schedulableEntityToETOmap.put(GenerateArxmlUtil.getFragmentURI(moduleEntity), e.getShortName());
        }
      }
    });

    List<BswCalledEntity> bswCalledEntityList = GenerateArxmlUtil
        .getAllInstancesOf(this.editingDomain.getResourceSet().getResources(), BswCalledEntity.class, false);

    List<BswInterruptEntity> bswInterruptEntityList = GenerateArxmlUtil
        .getAllInstancesOf(this.editingDomain.getResourceSet().getResources(), BswInterruptEntity.class, false);

    List<BswSchedulableEntity> bswSchedulableEntityList = GenerateArxmlUtil
        .getAllInstancesOf(this.editingDomain.getResourceSet().getResources(), BswSchedulableEntity.class, false);

    for (EcucContainerValue ecucV : this.bswRequiredTriggerConList) {
      String paramDef = "/RteBswModuleInstance/RteBswRequiredTriggerConnection/RteBswReleasedTriggerRef";
      EcucReferenceValue relTriggerRef = null;
      relTriggerRef = getEcucRefValue(ecucV, paramDef);

      EcucReferenceValue reqTriggerRef = null;
      paramDef = "/RteBswModuleInstance/RteBswRequiredTriggerConnection/RteBswRequiredTriggerRef";
      reqTriggerRef = getEcucRefValue(ecucV, paramDef);

      if ((relTriggerRef != null) && (reqTriggerRef != null) && (relTriggerRef.getValue() instanceof Trigger) &&
          (reqTriggerRef.getValue() instanceof Trigger)) {
        Trigger relTrigger = (Trigger) relTriggerRef.getValue();
        String relTriggerURI = GenerateArxmlUtil.getFragmentURI(relTrigger);

        Trigger reqTrigger = (Trigger) reqTriggerRef.getValue();

        List<BswCalledEntity> collect = bswCalledEntityList.stream()
            .filter(c -> c.getIssuedTriggers().stream()
                .anyMatch(t -> GenerateArxmlUtil.getFragmentURI(t.getTrigger()).equals(relTriggerURI)))
            .collect(Collectors.toList());

        List<BswInterruptEntity> collect1 = bswInterruptEntityList.stream()
            .filter(c -> c.getIssuedTriggers().stream()
                .anyMatch(t -> GenerateArxmlUtil.getFragmentURI(t.getTrigger()).equals(relTriggerURI)))
            .collect(Collectors.toList());

        List<BswSchedulableEntity> collect2 = bswSchedulableEntityList.stream()
            .filter(c -> c.getIssuedTriggers().stream()
                .anyMatch(t -> GenerateArxmlUtil.getFragmentURI(t.getTrigger()).equals(relTriggerURI)))
            .collect(Collectors.toList());

        if (collect.isEmpty() && collect1.isEmpty() && collect2.isEmpty()) {
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("276_0", relTrigger.getShortName(),
              reqTrigger.getShortName()));
        }
// BSW part of validations are not required to be mapped to event , or OSISR
// https://bosch-pmt.atlassian.net/browse/PMT-41988
//        else if (collect2.isEmpty()) {
//          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("276_3", relTrigger.getShortName(),
//              reqTrigger.getShortName()));
//        }
        else if (collect1.size() == 1) {
          BswInterruptEntity bswInterruptEntity = collect1.get(0);
          BswModuleEntry bswModuleEntry = bswInterruptEntity.getImplementedEntry();
          if ((bswModuleEntry != null) && !bswModuleEntry.eIsProxy()) {
            String osISRTaskName =
                this.osConfigToEcucValueMapping.getIsrEventToTaskMap().get(bswModuleEntry.getShortName());
//            if (osISRTaskName == null) {
//              LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("276_4", bswModuleEntry.getShortName(),
//                  relTrigger.getShortName()));
//            }
          }
//          else {
//            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("276_5", bswInterruptEntity.getShortName(),
//                relTrigger.getShortName()));
//          }
        }
        else if (collect2.size() == 1) {
          String scheduleEntity = collect2.get(0).getShortName();
          String bswETOEvent = schedulableEntityToETOmap.get(scheduleEntity);
          if (bswETOEvent != null) {
            String osTask = this.osConfigToEcucValueMapping.gettaskEventMap().get(bswETOEvent);
//            if (osTask == null) {
//              LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("276_2", bswETOEvent, scheduleEntity,
//                  relTrigger.getShortName()));
//            }
          }
//          else {
//            LOGGER.warn(
//                RteConfGenMessageDescription.getFormattedMesssage("276_1", scheduleEntity, relTrigger.getShortName()));
//          }
        }

      }
    }
  }


  private void updateBSWTriggerConnectionList() {

    List<EcucContainerValue> ecucContainerValues = GenerateArxmlUtil
        .getAllInstancesOf(this.editingDomain.getResourceSet().getResources(), EcucContainerValue.class, false);

    ecucContainerValues = ecucContainerValues.stream()
        .filter(c -> (c.getDefinition() != null) && !c.getDefinition().eIsProxy()).collect(Collectors.toList());

    this.bswRequiredTriggerConList.addAll(ecucContainerValues.stream()
        .filter(c -> GenerateArxmlUtil.getPackagePath(c.getDefinition())
            .equals(this.props.get(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH) +
                "/RteBswModuleInstance/RteBswRequiredTriggerConnection"))
        .collect(Collectors.toList()));
  }


  private void updateOsTaskToBSWETOMap() {

    for (TischedTask task : this.etoTaskList) {
      for (TischedEvent event : task.getEvents()) {
        if ((event.getEvent() != null) && (event.getEvent() instanceof BswExternalTriggerOccurredEvent)) {
          BswExternalTriggerOccurredEvent bswETO = (BswExternalTriggerOccurredEvent) event.getEvent();
          List<BswExternalTriggerOccurredEvent> list = this.osTaskToBSWETOMap.get(task.getShortName());
          if (list != null) {
            list.add(bswETO);
          }
          else {
            List<BswExternalTriggerOccurredEvent> list1 = new ArrayList<>();
            list1.add(bswETO);
            this.osTaskToBSWETOMap.put(task.getShortName(), list1);
          }
        }
      }
    }


  }


  private List<EcucContainerValue> getBSWTriggerConnectionListForReqTrigger(
      final List<BswExternalTriggerOccurredEvent> etoeList, final List<String> relTriggerAndRelTriggerModList) {
    List<EcucContainerValue> BSWTriggerConList = new ArrayList<EcucContainerValue>();
    List<String> triggerURIList = new ArrayList<>();
    for (BswExternalTriggerOccurredEvent etoe : etoeList) {
      Trigger trigger = etoe.getTrigger();
      if ((trigger != null) && !trigger.eIsProxy()) {
        if (!triggerURIList.contains(GenerateArxmlUtil.getFragmentURI(trigger))) {
          triggerURIList.add(GenerateArxmlUtil.getFragmentURI(trigger));
        }
      }
    }


    for (EcucContainerValue ecucV : this.bswRequiredTriggerConList) {
      if (!ecucV.getReferenceValues().isEmpty()) {
        EcucReferenceValue reqTriggerRef = null;
        String paramDef = "/RteBswModuleInstance/RteBswRequiredTriggerConnection/RteBswRequiredTriggerRef";
        reqTriggerRef = getEcucRefValue(ecucV, paramDef);

        if ((reqTriggerRef != null) && (reqTriggerRef.getValue() instanceof Trigger)) {
          Trigger reqTrigger = (Trigger) reqTriggerRef.getValue();
          if (triggerURIList.contains(GenerateArxmlUtil.getFragmentURI(reqTrigger)) &&
              !GenerateArxmlUtil.listContainsElements(BSWTriggerConList, ecucV)) {
            BSWTriggerConList.add(ecucV);

            paramDef = "/RteBswModuleInstance/RteBswRequiredTriggerConnection/RteBswReleasedTriggerModInstRef";
            EcucReferenceValue relTriggerModRef = null;
            relTriggerModRef = getEcucRefValue(ecucV, paramDef);

            paramDef = "/RteBswModuleInstance/RteBswRequiredTriggerConnection/RteBswReleasedTriggerRef";
            EcucReferenceValue relTriggerRef = null;
            relTriggerRef = getEcucRefValue(ecucV, paramDef);

            if ((relTriggerModRef != null) && (relTriggerRef != null) &&
                (relTriggerModRef.getValue() instanceof EcucContainerValue) &&
                (relTriggerRef.getValue() instanceof Trigger)) {
              Trigger relTrigger = (Trigger) relTriggerRef.getValue();
              EcucContainerValue relTriggerModInstance = (EcucContainerValue) relTriggerModRef.getValue();
              String element = GenerateArxmlUtil.getFragmentURI(relTriggerModInstance) + "_ReleaseTrigger_" +
                  GenerateArxmlUtil.getFragmentURI(relTrigger);
              if (!relTriggerAndRelTriggerModList.contains(element)) {
                relTriggerAndRelTriggerModList.add(element);
              }
            }
          }
        }
      }
    }
    return BSWTriggerConList;
  }


  /**
   * @param ecucV
   * @param paramDef
   * @return
   */
  private EcucReferenceValue getEcucRefValue(final EcucContainerValue ecucV, final String paramDef) {
    EcucReferenceValue ecucRef = null;
    for (EcucAbstractReferenceValue ecuAbsRef : ecucV.getReferenceValues()) {
      if (ecuAbsRef instanceof EcucReferenceValue) {
        EcucReferenceValue ecucRefV = (EcucReferenceValue) ecuAbsRef;
        if ((ecucRefV.getDefinition() != null) && !ecucRefV.getDefinition().eIsProxy() &&
            (ecucRefV.getValue() != null) && !ecucRefV.getValue().eIsProxy() &&
            GenerateArxmlUtil.getPackagePath(ecucRefV.getDefinition())
                .equals(this.props.get(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH) + paramDef)) {
          ecucRef = ecucRefV;
        }
      }
    }
    return ecucRef;
  }
}
