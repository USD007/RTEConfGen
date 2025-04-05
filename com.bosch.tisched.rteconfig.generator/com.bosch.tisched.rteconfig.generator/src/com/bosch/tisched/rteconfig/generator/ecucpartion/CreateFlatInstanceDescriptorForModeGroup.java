/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.ecucpartion;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;

import com.bosch.tisched.rteconfig.generator.core.OsConfigToEcucValueMapping;
import com.bosch.tisched.rteconfig.generator.ecuextract.UpdateExcuExtractFiles;
import com.bosch.tisched.rteconfig.generator.util.AssemblySwConnectionUtil;
import com.bosch.tisched.rteconfig.generator.util.AutosarProjectCreationUtil;
import com.bosch.tisched.rteconfig.generator.util.AutosarUtil;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;
import com.bosch.tisched.rteconfig.generator.util.VariationPointUtil;

import autosar40.autosartoplevelstructure.AUTOSAR;
import autosar40.commonstructure.auxillaryobjects.AuxillaryobjectsFactory;
import autosar40.commonstructure.auxillaryobjects.MemoryAllocationKeywordPolicyType;
import autosar40.commonstructure.auxillaryobjects.MemorySectionType;
import autosar40.commonstructure.auxillaryobjects.SwAddrMethod;
import autosar40.commonstructure.datadefproperties.DatadefpropertiesFactory;
import autosar40.commonstructure.datadefproperties.SwDataDefProps;
import autosar40.commonstructure.datadefproperties.SwDataDefPropsConditional;
import autosar40.commonstructure.flatmap.FlatInstanceDescriptor;
//import autosar40.commonstructure.flatmap.FlatInstanceDescriptor;
import autosar40.commonstructure.flatmap.FlatMap;
import autosar40.commonstructure.flatmap.FlatmapFactory;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucInstanceReferenceValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.ecucdescription.EcucReferenceValue;
import autosar40.genericstructure.generaltemplateclasses.anyinstanceref.AnyInstanceRef;
import autosar40.genericstructure.generaltemplateclasses.anyinstanceref.AnyinstancerefFactory;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ArpackageFactory;
import autosar40.genericstructure.varianthandling.VariationPoint;
import autosar40.swcomponent.components.AbstractProvidedPortPrototype;
import autosar40.swcomponent.components.AbstractRequiredPortPrototype;
import autosar40.swcomponent.components.AtomicSwComponentType;
import autosar40.swcomponent.components.PPortPrototype;
import autosar40.swcomponent.components.PRPortPrototype;
import autosar40.swcomponent.components.PortPrototype;
import autosar40.swcomponent.components.RPortPrototype;
import autosar40.swcomponent.components.SwComponentType;
import autosar40.swcomponent.components.instancerefs.PModeGroupInAtomicSwcInstanceRef;
import autosar40.swcomponent.components.instancerefs.POperationInAtomicSwcInstanceRef;
import autosar40.swcomponent.components.instancerefs.RModeGroupInAtomicSWCInstanceRef;
import autosar40.swcomponent.components.instancerefs.RModeInAtomicSwcInstanceRef;
import autosar40.swcomponent.components.instancerefs.RVariableInAtomicSwcInstanceRef;
import autosar40.swcomponent.composition.AssemblySwConnector;
import autosar40.swcomponent.composition.CompositionSwComponentType;
import autosar40.swcomponent.composition.SwComponentPrototype;
import autosar40.swcomponent.portinterface.ModeSwitchInterface;
import autosar40.swcomponent.portinterface.PortInterface;
import autosar40.swcomponent.swcinternalbehavior.RunnableEntity;
import autosar40.swcomponent.swcinternalbehavior.SwcInternalBehavior;
import autosar40.swcomponent.swcinternalbehavior.dataelements.VariableAccess;
import autosar40.swcomponent.swcinternalbehavior.modedeclarationgroup.ModeAccessPoint;
import autosar40.swcomponent.swcinternalbehavior.modedeclarationgroup.ModeSwitchPoint;
import autosar40.swcomponent.swcinternalbehavior.rteevents.DataReceivedEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.OperationInvokedEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.RTEEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.SwcModeSwitchEvent;
import autosar40.swcomponent.swcinternalbehavior.servercall.ServerCallPoint;

/**
 * @author SHK1COB
 */
public class CreateFlatInstanceDescriptorForModeGroup {


  private static final Logger LOGGER =
      RteConfigGeneratorLogger.getLogger(CreateFlatInstanceDescriptorForModeGroup.class.getName());

  private static final String OS_APPLICATION = "OsApplication";
  private static final String OS_APPTASKREF = "OsAppTaskRef";

  private final String flatMapFilePath;
  private final String autosarReleaseVersion;
  private final String autosarResourceVersion;
  private final IProject project;
  private final OsConfigToEcucValueMapping tischedToEcuCValueMapping;
//  private final Map<String, String> map;

  private final UpdateExcuExtractFiles UpdateExcuExtractFiles;

  private Map<SwComponentPrototype, List<EcucContainerValue>> swcOsAppMap;

  private List<AssemblySwConnector> msConnectors;

  private Map<String, List<String>> portMap;

  private ARPackage flatMapArPackage;

  private FlatMap flatMap;

  private ARPackage swAddrMethodPackage;

  private Map<String, FlatInstanceDescriptor> fMap;

  private Map<String, SwAddrMethod> sMap;

  private Map<String, PortPrototype> pMap;

  private Map<String, PortPrototype> rMap;

  private Map<String, SwComponentPrototype> scMap;

  private Map<String, SwComponentPrototype> scMap1;

  private AUTOSAR autosarRoot;

  private IResource findMember;

  private Map<String, ModeSwitchPoint> pMspMap;

  private Map<String, List<ModeAccessPoint>> pMapMap;

  private Map<String, List<ModeAccessPoint>> rMapMap;

  private Map<String, SwcModeSwitchEvent> rMSEMap;

  //
  private Map<String, RTEEvent> ruEvMap;

  //
  private Map<String, List<ServerCallPoint>> rSCPMap;

//  private Map<String, DataReceivedEvent> rRCEMap;

  private Map<String, EcucContainerValue> eventTaskMap;

  private Map<String, EcucContainerValue> taskOsAppMap;

  //
  private Map<String, List<PortPrototype>> pRMap;

  //
  private Map<String, List<PortPrototype>> rPMap;


  private Map<String, String> oipMap;

  private Map<String, String> derMap;

//
  private Map<String, List<VariableAccess>> pVMap;

  //
  private Map<String, List<RunnableEntity>> vRuVMap;

  private Map<String, SwComponentPrototype> allScMap;

  private final StringBuilder uriBuf = new StringBuilder();

  private String rteConfGenlogDir;

  private final String newLine = "\r\n";


  /**
   * @param project IProject
   * @param map Map<String, String>
   * @param tischedToEcuCValueMapping TischedToEcuCValueMapping
   * @param assemblySwConnUtil
   */
  public CreateFlatInstanceDescriptorForModeGroup(final IProject project, final Map<String, String> map,
      final OsConfigToEcucValueMapping tischedToEcuCValueMapping, final UpdateExcuExtractFiles UpdateExcuExtractFiles,
      final AssemblySwConnectionUtil assemblySwConnUtil) {
    this.project = project;
//    this.map = map;
    this.flatMapFilePath = map.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH);
    this.autosarReleaseVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION);
    this.autosarResourceVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION);
    this.tischedToEcuCValueMapping = tischedToEcuCValueMapping;
    this.UpdateExcuExtractFiles = UpdateExcuExtractFiles;
    String rteConfGenDir = map.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG);
    this.rteConfGenlogDir =
        rteConfGenDir.substring(1, rteConfGenDir.length()).substring(0, rteConfGenDir.lastIndexOf("/"));
    this.rteConfGenlogDir = this.rteConfGenlogDir.endsWith("/")
        ? this.rteConfGenlogDir.substring(0, this.rteConfGenlogDir.length() - 1) : this.rteConfGenlogDir;
    initialize(assemblySwConnUtil);
  }


  @SuppressWarnings("unlikely-arg-type")
  private void initialize(AssemblySwConnectionUtil assemblySwConnUtil) {
    try {

      Map<String, String> aswcToSwcpMap = assemblySwConnUtil.getAswcToSwcpMap();

      this.findMember = this.project.findMember(this.flatMapFilePath);

      if ((this.findMember != null) && this.findMember.exists() &&
          !this.UpdateExcuExtractFiles.getFlatInstanceDescriptors().isEmpty() &&
          !this.UpdateExcuExtractFiles.getAssemblySwConnectors().isEmpty()) {

        Resource resource = EcorePlatformUtil.getResource((IFile) this.findMember);
        this.autosarRoot = EcoreUtil.copy((AUTOSAR) resource.getContents().get(0));

        ARPackage arP =
            (ARPackage) this.UpdateExcuExtractFiles.getFlatInstanceDescriptors().get(0).eContainer().eContainer();

        String fragment = GenerateArxmlUtil.getFragmentURI(arP);

        this.flatMapArPackage = GenerateArxmlUtil.getARArPackageFromRoot(this.autosarRoot, fragment);

        this.flatMap = (FlatMap) this.flatMapArPackage.getElements().get(0);

        this.swAddrMethodPackage = ArpackageFactory.eINSTANCE.createARPackage();
        this.swAddrMethodPackage.setShortName("SwAddrMethods");
        ((ARPackage) this.flatMapArPackage.eContainer()).getArPackages().add(this.swAddrMethodPackage);

        EList<SwComponentPrototype> components =
            this.UpdateExcuExtractFiles.getUpdatedRootSwCompositionPrototype().getSoftwareComposition().getComponents();

        Map<String, SwComponentPrototype> flwComponents = new HashMap<>();
        for (SwComponentPrototype swc : components) {
          flwComponents.put(swc.getShortName(), swc);
        }

        CompositionSwComponentType softwareComposition =
            this.UpdateExcuExtractFiles.getUpdatedRootSwCompositionPrototype().getSoftwareComposition();

        String flatViewUri = EcoreUtil.getURI(softwareComposition).fragment();


        List<EcucContainerValue> ecucContainerValues = this.tischedToEcuCValueMapping.getEcucContainerValues();
        Map<String, List<SwComponentPrototype>> ecuConSwCMap = new HashMap<String, List<SwComponentPrototype>>();
        for (EcucContainerValue ecucConn : ecucContainerValues) {
          AutosarUtil.setCurrentProcessingEObject(ecucConn);
          if ((ecucConn.getDefinition() != null) &&
              ecucConn.getDefinition().getShortName().equals("EcucPartitionCollection")) {

            for (EcucContainerValue ecucCon : ecucConn.getSubContainers()) {
              AutosarUtil.setCurrentProcessingEObject(ecucCon);
              if ((ecucCon.getDefinition() != null) && ecucCon.getDefinition().getShortName().equals("EcucPartition")) {

                List<SwComponentPrototype> li = ecuConSwCMap.get(ecucCon.getShortName());
                if (li == null) {
                  li = new ArrayList<SwComponentPrototype>();
                  ecuConSwCMap.put(ecucCon.getShortName(), li);
                }

                for (EcucAbstractReferenceValue ecucRef : ecucCon.getReferenceValues()) {
                  AutosarUtil.setCurrentProcessingEObject(ecucRef);
                  if (ecucRef.getDefinition().getShortName().equals("EcucPartitionSoftwareComponentInstanceRef") &&
                      (ecucRef instanceof EcucInstanceReferenceValue)) {
                    EcucInstanceReferenceValue eir = (EcucInstanceReferenceValue) ecucRef;
                    AnyInstanceRef aif = eir.getValue();
                    if ((aif.getTarget() != null) && (aif
                        .getTarget() instanceof SwComponentPrototype)/* && components.contains(aif.getTarget()) */) {
                      SwComponentPrototype swc = (SwComponentPrototype) aif.getTarget();
                      if ((swc.getCompositionSwComponentType() != null) &&
                          flatViewUri.equals(EcoreUtil.getURI(swc.getCompositionSwComponentType()).fragment())) {

                        SwComponentPrototype swc1 = flwComponents.get(swc.getShortName());
                        if (!li.contains(swc1)) {
                          li.add(swc1);
                        }
                      }
                      else {
                        LOGGER.warn(RteConfGenMessageDescription
                            .getFormattedMesssage("256_0", EcoreUtil.getURI(swc).fragment(), ecucCon.getShortName())
                            .trim());
                      }
                    }
                  }
                }
              }
            }
          }
        }


        this.swcOsAppMap = new HashMap<SwComponentPrototype, List<EcucContainerValue>>();

        Map<String, EcucContainerValue> osAppEcuPartitionMap = this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap();

        for (String osApp : osAppEcuPartitionMap.keySet()) {

          EcucContainerValue ecucContainerValue = osAppEcuPartitionMap.get(osApp);

          if (ecucContainerValue != null) {


            List<SwComponentPrototype> list = ecuConSwCMap.get(ecucContainerValue.getShortName());

            if (list != null) {

              for (SwComponentPrototype swc : list) {
                List<EcucContainerValue> list2 = this.swcOsAppMap.get(swc);

                if ((list2 != null) && !list2.contains(osAppEcuPartitionMap.get(osApp))) {
                  list2.add(osAppEcuPartitionMap.get(osApp));
                }
                else if (list2 == null) {
                  list2 = new ArrayList<>();
                  list2.add(osAppEcuPartitionMap.get(osApp));
                  this.swcOsAppMap.put(swc, list2);
                }
              }
            }
          }
        }


        this.allScMap = new HashMap<String, SwComponentPrototype>();
        for (SwComponentPrototype swc : this.swcOsAppMap.keySet()) {
          this.allScMap.put(swc.getShortName(), swc);
        }

        this.msConnectors = new ArrayList<AssemblySwConnector>();
        this.pMap = new HashMap<String, PortPrototype>();
        this.rMap = new HashMap<String, PortPrototype>();
        this.scMap = new HashMap<String, SwComponentPrototype>();
        this.scMap1 = new HashMap<String, SwComponentPrototype>();
        this.pRMap = new HashMap<String, List<PortPrototype>>();
        this.rPMap = new HashMap<String, List<PortPrototype>>();
        boolean proxypportflag = false;

        for (AssemblySwConnector aswc : this.UpdateExcuExtractFiles.getAssemblySwConnectors()) {
          AutosarUtil.setCurrentProcessingEObject(aswc);

          if ((aswc.getProvider() != null) && (aswc.getRequester() != null)) {
            List<String> portList = assemblySwConnUtil.getASWCToPortsMap().get(aswc.getShortName());
            if ((portList != null) && !portList.isEmpty()) {
              AbstractProvidedPortPrototype targetPPort =
                  (AbstractProvidedPortPrototype) assemblySwConnUtil.getUriToPortMap().get(portList.get(0));
              AbstractRequiredPortPrototype targetRPort =
                  (AbstractRequiredPortPrototype) assemblySwConnUtil.getUriToPortMap().get(portList.get(1));

              if ((targetPPort != null) && (targetRPort != null)) {

                PortInterface pInterface = targetPPort instanceof PPortPrototype
                    ? ((PPortPrototype) targetPPort).getProvidedInterface() : (targetPPort instanceof PRPortPrototype
                        ? ((PRPortPrototype) targetPPort).getProvidedRequiredInterface() : null);

                PortInterface rInterface = targetRPort instanceof RPortPrototype
                    ? ((RPortPrototype) targetRPort).getRequiredInterface() : (targetRPort instanceof PRPortPrototype
                        ? ((PRPortPrototype) targetRPort).getProvidedRequiredInterface() : null);

                SwComponentPrototype swComponentPrototype1 = flwComponents.get(aswcToSwcpMap
                    .get(aswc.getShortName() + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(targetPPort)));
                SwComponentPrototype swComponentPrototype2 = flwComponents.get(aswcToSwcpMap
                    .get(aswc.getShortName() + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(targetRPort)));

                String pswctname = targetPPort.getSwComponentType().getShortName();
                String rswctname = targetRPort.getSwComponentType().getShortName();
                if ((pInterface != null) && (pInterface instanceof ModeSwitchInterface) && (rInterface != null) &&
                    (rInterface instanceof ModeSwitchInterface) && !this.msConnectors.contains(aswc)) {


                  this.pMap.put(targetPPort.getShortName() + "<->" + pswctname, targetPPort);
                  this.rMap.put(targetRPort.getShortName() + "<->" + rswctname, targetRPort);

                  if (swComponentPrototype1 != null) {
                    this.scMap.put(swComponentPrototype1.getShortName(), swComponentPrototype1);
                  }
                  if (swComponentPrototype2 != null) {
                    this.scMap1.put(swComponentPrototype2.getShortName(), swComponentPrototype2);
                  }
                  this.msConnectors.add(aswc);
                }

                if ((swComponentPrototype1 != null) && (swComponentPrototype2 != null) &&
                    (this.allScMap.get(swComponentPrototype1.getShortName()) != null) &&
                    (this.allScMap.get(swComponentPrototype2.getShortName()) != null)) {

                  List<PortPrototype> list = this.pRMap.get(targetPPort.getShortName() + "<->" + pswctname);
                  if (list != null) {
                    list.add(targetRPort);
                  }
                  else {
                    list = new ArrayList<>();
                    list.add(targetRPort);
                    this.pRMap.put(targetPPort.getShortName() + "<->" + rswctname, list);
                  }

                  List<PortPrototype> list1 = this.rPMap.get(targetRPort.getShortName() + "<->" + pswctname);
                  if (list1 != null) {
                    list1.add(targetPPort);
                  }
                  else {
                    list1 = new ArrayList<>();
                    list1.add(targetPPort);
                    this.rPMap.put(targetRPort.getShortName() + "<->" + rswctname, list1);
                  }
                }
              }
            }
          }
        }


        this.portMap = new HashMap<String, List<String>>();

        Map<String, String> portMap1 = new HashMap<String, String>();

        for (AssemblySwConnector aswc : this.msConnectors) {
          AutosarUtil.setCurrentProcessingEObject(aswc);

          List<String> portList = assemblySwConnUtil.getASWCToPortsMap().get(aswc.getShortName());
          if ((portList != null) && !portList.isEmpty()) {
            AbstractProvidedPortPrototype targetPPort =
                (AbstractProvidedPortPrototype) assemblySwConnUtil.getUriToPortMap().get(portList.get(0));
            AbstractRequiredPortPrototype targetRPort =
                (AbstractRequiredPortPrototype) assemblySwConnUtil.getUriToPortMap().get(portList.get(1));

            SwComponentPrototype swComponentPrototype1 = flwComponents.get(aswcToSwcpMap
                .get(aswc.getShortName() + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(targetPPort)));
            SwComponentPrototype swComponentPrototype2 = flwComponents.get(aswcToSwcpMap
                .get(aswc.getShortName() + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(targetRPort)));

            if ((targetPPort != null) && (targetRPort != null) && (swComponentPrototype1 != null) &&
                (swComponentPrototype2 != null) &&
                (!swComponentPrototype1.getShortName().equals(swComponentPrototype2.getShortName()))) {
              String pShortName = targetPPort.getShortName() + "<->" + targetPPort.getSwComponentType().getShortName() +
                  "<->" + swComponentPrototype1.getShortName();

              String rShortName = targetRPort.getShortName() + "<->" + targetRPort.getSwComponentType().getShortName() +
                  "<->" + swComponentPrototype2.getShortName();

              if (portMap1.get(rShortName) == null) {
                portMap1.put(rShortName, pShortName);
                List<String> list = this.portMap.get(pShortName);

                if ((list != null) && !list.contains(rShortName)) {
                  list.add(rShortName);
                }
                else if (list == null) {
                  list = new ArrayList<>();
                  list.add(rShortName);
                  this.portMap.put(pShortName, list);
                }
              }
            }
          }
        }
        portMap1.clear();
        this.fMap = new HashMap<>();

        for (FlatInstanceDescriptor fld : this.UpdateExcuExtractFiles.getFlatInstanceDescriptors()) {
          this.fMap.put(fld.getShortName(), fld);
        }

        this.sMap = new HashMap<>();
        this.pMspMap = new HashMap<>();
        this.pMapMap = new HashMap<>();
        this.rMapMap = new HashMap<>();
        this.rMSEMap = new HashMap<>();
        this.oipMap = new HashMap<>();
        this.derMap = new HashMap<>();
        this.eventTaskMap = new HashMap<>();
        this.taskOsAppMap = new HashMap<>();
        getEventTaskMappings();
        getTaskOsAppMapping();

        List<SwComponentPrototype> allSwcp = new ArrayList<>();

        allSwcp.addAll(this.scMap.values());
        allSwcp.addAll(this.scMap1.values());

        for (SwComponentPrototype swcp : allSwcp) {

          AutosarUtil.setCurrentProcessingEObject(swcp);
          SwComponentType type = swcp.getType();

          if (type instanceof AtomicSwComponentType) {

            EList<SwcInternalBehavior> internalBehaviors = ((AtomicSwComponentType) type).getInternalBehaviors();

            for (SwcInternalBehavior swcb : internalBehaviors) {
              AutosarUtil.setCurrentProcessingEObject(swcb);

              for (RunnableEntity runnable : swcb.getRunnables()) {
                AutosarUtil.setCurrentProcessingEObject(runnable);

                for (ModeSwitchPoint msp : runnable.getModeSwitchPoints()) {
                  AutosarUtil.setCurrentProcessingEObject(msp);

                  if ((msp.getModeGroup() != null)) {
                    PortPrototype portPrototype =
                        assemblySwConnUtil.getUriToPortMap().get(assemblySwConnUtil.getrunnableMap()
                            .get(type.getShortName() + runnable.getShortName() + "<->" + msp.getShortName()));
                    if (portPrototype != null) {

                      String shortName = portPrototype.getShortName();
                      this.pMspMap.put(shortName + "<->" + type.getShortName(), msp);
                    }
                  }
                }

                for (ModeAccessPoint map : runnable.getModeAccessPoints()) {
                  AutosarUtil.setCurrentProcessingEObject(map);

                  if ((map.getModeGroup() != null) &&
                      (map.getModeGroup() instanceof PModeGroupInAtomicSwcInstanceRef)) {
                    AbstractProvidedPortPrototype ap =
                        ((PModeGroupInAtomicSwcInstanceRef) map.getModeGroup()).getContextPPort();

                    if (ap != null) {
                      List<ModeAccessPoint> mL = this.pMapMap.get(ap.getShortName() + "<->" + type.getShortName());
                      if (mL != null) {
                        mL.add(map);
                      }
                      else {
                        mL = new ArrayList<>();
                        mL.add(map);
                        this.pMapMap.put(ap.getShortName() + "<->" + type.getShortName(), mL);
                      }
                    }
                  }

                  if ((map.getModeGroup() != null) &&
                      (map.getModeGroup() instanceof RModeGroupInAtomicSWCInstanceRef)) {
                    AbstractRequiredPortPrototype ar =
                        ((RModeGroupInAtomicSWCInstanceRef) map.getModeGroup()).getContextRPort();

                    if (ar != null) {

                      List<ModeAccessPoint> mL = this.rMapMap.get(ar.getShortName() + "<->" + type.getShortName());
                      if (mL != null) {
                        mL.add(map);
                      }
                      else {
                        mL = new ArrayList<>();
                        mL.add(map);
                        this.rMapMap.put(ar.getShortName() + "<->" + type.getShortName(), mL);
                      }
                    }
                  }
                }
              }


              for (RTEEvent rteEvent : swcb.getEvents()) {
                AutosarUtil.setCurrentProcessingEObject(rteEvent);

                if ((rteEvent.getStartOnEvent() != null) && (rteEvent instanceof SwcModeSwitchEvent)) {

                  EList<RModeInAtomicSwcInstanceRef> modes = ((SwcModeSwitchEvent) rteEvent).getModes();
                  for (RModeInAtomicSwcInstanceRef rPort : modes) {
                    AutosarUtil.setCurrentProcessingEObject(rPort);
                    this.rMSEMap.put(rPort.getContextPort().getShortName() + "<->" + type.getShortName(),
                        (SwcModeSwitchEvent) rteEvent);
                  }
                }
                else if ((rteEvent.getStartOnEvent() != null) && (rteEvent instanceof OperationInvokedEvent)) {

                  POperationInAtomicSwcInstanceRef operation = ((OperationInvokedEvent) rteEvent).getOperation();
                  this.oipMap.put(rteEvent.getShortName() + "<->" + type.getShortName(),
                      operation.getContextPPort().getShortName());

                }
                else if ((rteEvent.getStartOnEvent() != null) && (rteEvent instanceof DataReceivedEvent)) {

                  RVariableInAtomicSwcInstanceRef data = ((DataReceivedEvent) rteEvent).getData();
                  this.derMap.put(rteEvent.getShortName() + "<->" + type.getShortName(),
                      data.getContextRPort().getShortName());
                }
              }
            }
          }
        }

        this.rSCPMap = new HashMap<>();
        this.ruEvMap = new HashMap<>();
        this.pVMap = new HashMap<>();
        this.vRuVMap = new HashMap<>();

        for (SwComponentPrototype swcp : CreateFlatInstanceDescriptorForModeGroup.this.allScMap.values()) {
          AutosarUtil.setCurrentProcessingEObject(swcp);

          SwComponentType type = swcp.getType();

          if (type instanceof AtomicSwComponentType) {

            EList<SwcInternalBehavior> internalBehaviors = ((AtomicSwComponentType) type).getInternalBehaviors();

            for (SwcInternalBehavior swcb : internalBehaviors) {
              AutosarUtil.setCurrentProcessingEObject(swcb);

              for (RunnableEntity runnable : swcb.getRunnables()) {
                AutosarUtil.setCurrentProcessingEObject(runnable);

                for (ServerCallPoint scp : runnable.getServerCallPoints()) {
                  AutosarUtil.setCurrentProcessingEObject(scp);

                  if (scp.getOperation() != null) {
                    // AbstractRequiredPortPrototype ar = scp.getOperation().getContextRPort();

                    PortPrototype ar = assemblySwConnUtil.getUriToPortMap().get(assemblySwConnUtil.getrunnableMap()
                        .get(type.getShortName() + runnable.getShortName() + "<->" + scp.getShortName()));

                    if (ar != null) {

                      List<ServerCallPoint> scl = this.rSCPMap.get(ar.getShortName() + "<->" + type.getShortName());

                      if (scl != null) {
                        scl.add(scp);
                      }
                      else {
                        scl = new ArrayList<>();
                        scl.add(scp);
                        this.rSCPMap.put(ar.getShortName() + "<->" + type.getShortName(), scl);

                      }
                    }
                  }
                }

                for (VariableAccess va : runnable.getDataSendPoints()) {
                  AutosarUtil.setCurrentProcessingEObject(va);

                  if ((va.getAccessedVariable() != null) && (va.getAccessedVariable().getAutosarVariable() != null)) {

                    PortPrototype portPrototype =
                        assemblySwConnUtil.getUriToPortMap().get(assemblySwConnUtil.getrunnableMap()
                            .get(type.getShortName() + runnable.getShortName() + "<->" + va.getShortName()));
                    // PortPrototype portPrototype = va.getAccessedVariable().getAutosarVariable().getPortPrototype();

                    if (portPrototype != null) {
                      List<VariableAccess> val =
                          this.pVMap.get(portPrototype.getShortName() + "<->" + type.getShortName());

                      if (val != null) {
                        val.add(va);
                      }
                      else {
                        val = new ArrayList<>();
                        val.add(va);
                        this.pVMap.put(portPrototype.getShortName() + "<->" + type.getShortName(), val);
                      }
                    }

                    List<RunnableEntity> rl = this.vRuVMap.get(va.getShortName() + "<->" + type.getShortName());

                    if (rl != null) {
                      rl.add(runnable);
                    }
                    else {
                      rl = new ArrayList<>();
                      rl.add(runnable);
                      this.vRuVMap.put(va.getShortName() + "<->" + type.getShortName(), rl);
                    }
                  }
                }

                for (VariableAccess va : runnable.getDataWriteAccess()) {
                  AutosarUtil.setCurrentProcessingEObject(va);

                  if ((va.getAccessedVariable() != null) && (va.getAccessedVariable().getAutosarVariable() != null)) {
                    PortPrototype portPrototype =
                        assemblySwConnUtil.getUriToPortMap().get(assemblySwConnUtil.getrunnableMap()
                            .get(type.getShortName() + runnable.getShortName() + "<->" + va.getShortName()));
                    // PortPrototype portPrototype = va.getAccessedVariable().getAutosarVariable().getPortPrototype();

                    if (portPrototype != null) {
                      List<VariableAccess> val =
                          this.pVMap.get(portPrototype.getShortName() + "<->" + type.getShortName());

                      if (val != null) {
                        val.add(va);
                      }
                      else {
                        val = new ArrayList<>();
                        val.add(va);
                        this.pVMap.put(portPrototype.getShortName() + "<->" + type.getShortName(), val);
                      }
                    }

                    List<RunnableEntity> rl = this.vRuVMap.get(va.getShortName() + "<->" + type.getShortName());

                    if (rl != null) {
                      rl.add(runnable);
                    }
                    else {
                      rl = new ArrayList<>();
                      rl.add(runnable);
                      this.vRuVMap.put(va.getShortName() + "<->" + type.getShortName(), rl);
                    }
                  }
                }
              }


              for (RTEEvent rteEvent : swcb.getEvents()) {

                if ((rteEvent.getStartOnEvent() != null)) {

                  String shortName = rteEvent.getStartOnEvent().getShortName();

                  this.ruEvMap.put(shortName + "<->" + type.getShortName(), rteEvent);

                }
              }
            }
          }
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    finally {

      assemblySwConnUtil.clearAll();
      assemblySwConnUtil = null;
      System.gc();

    }
  }

  /**
   * @throws Exception
   */
  public void createFlatInstanceDescriptorForModeGroup() throws Exception {

    Map<String, List<EcucContainerValue>> swcOsAppMapp = new HashMap<String, List<EcucContainerValue>>();


    for (Map.Entry<SwComponentPrototype, List<EcucContainerValue>> e : this.swcOsAppMap.entrySet()) {

      SwComponentPrototype key = e.getKey();

      swcOsAppMapp.put(key.getShortName(), e.getValue());
    }

    Map<String, List<String>> portMap1 = new HashMap<String, List<String>>();

    Map<String, List<String>> portMap2 = new HashMap<String, List<String>>();

    for (Map.Entry<String, List<String>> e : this.portMap.entrySet()) {

      List<String> value = e.getValue();

      if ((value != null) && (value.size() == 1)) {
        portMap1.put(e.getKey(), e.getValue());
      }

      else if ((value != null) && (value.size() > 1)) {
        portMap2.put(e.getKey(), e.getValue());
      }

    }

//    createFlatInstanceDescriptorForSwcInSingleOsApp(swcOsAppMap1, portMap1, portMap2);
//
//    createFlatInstanceDescriptorForSwcInMulitpleOsApp(swcOsAppMap2, portMap1, portMap2);

    createFlatInstanceDescriptorForSwc(swcOsAppMapp, portMap1, portMap2);

    IStatus saveFile = GenerateArxmlUtil.saveFile(CreateFlatInstanceDescriptorForModeGroup.this.project,
        CreateFlatInstanceDescriptorForModeGroup.this.autosarRoot,
        URI.createPlatformResourceURI(
            CreateFlatInstanceDescriptorForModeGroup.this.findMember.getFullPath().toOSString(), false),
        AutosarUtil.getMetaModelDescriptorByAutosarResoureVersion(
            AutosarUtil.getMetaModelDescriptorByAutosarReleaseVersion(
                CreateFlatInstanceDescriptorForModeGroup.this.autosarReleaseVersion),
            CreateFlatInstanceDescriptorForModeGroup.this.autosarResourceVersion));

    if (saveFile.isOK()) {
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("150_1",
          CreateFlatInstanceDescriptorForModeGroup.this.findMember.getFullPath().toOSString()).trim());


    }
    else {
      RteConfigGeneratorLogger.logErrormessage(LOGGER, "*** Updating FlatMap file is failed");
    }

    AutosarProjectCreationUtil.writeLogs(this.project, this.uriBuf,
        this.rteConfGenlogDir + "/" + "RteConfGen_FlatInstanceDescriptors_For_ModeGroups.txt");

  }


  private void createFlatInstanceDescriptorForSwc(final Map<String, List<EcucContainerValue>> swcOsAppMapp,
      final Map<String, List<String>> portMap1, final Map<String, List<String>> portMap2)
      throws Exception {
    Map<String, String> swAddrMethodMap = new HashMap<String, String>();
    for (String p : portMap1.keySet()) {

      String s1[] = p.split("<->");
      List<EcucContainerValue> list1 = swcOsAppMapp.get(s1[2]);

      if (list1 != null) {

        if (list1.size() == 1) {
          LOGGER.info(
              RteConfGenMessageDescription.getFormattedMesssage("153_1", s1[0], list1.get(0).getShortName()).trim());


          String s2[] = portMap1.get(p).get(0).split("<->");
          List<EcucContainerValue> list2 = swcOsAppMapp.get(s2[2]);


          if (list2 != null) {

            this.uriBuf.append(this.newLine + "AssemblySwConnection: " + s1[0] + " -> " + s2[0] + this.newLine +
                "===============================================================" + this.newLine);

            String uri1 = GenerateArxmlUtil.getFragmentURI(this.pMap.get(s1[0] + "<->" + s1[1]));
            String uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap.get(s1[2]));
            String uri3 = GenerateArxmlUtil.getFragmentURI(list1.get(0));
            this.uriBuf.append(" Provider Port: " + uri1 + this.newLine);
            this.uriBuf.append(" Instance: " + uri2 + this.newLine);
            this.uriBuf.append(" Mapped OS Applications: " + list1.get(0).getShortName() + this.newLine);
            this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);

            if (list2.size() == 1) {

              uri1 = GenerateArxmlUtil.getFragmentURI(this.rMap.get(s2[0] + "<->" + s2[1]));
              uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap1.get(s2[2]));
              uri3 = GenerateArxmlUtil.getFragmentURI(list2.get(0));
              this.uriBuf.append(" Require Port: " + uri1 + this.newLine);
              this.uriBuf.append(" Instance: " + uri2 + this.newLine);
              this.uriBuf.append(" Mapped OS Applications: " + list2.get(0).getShortName() + this.newLine);
              this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);

              LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("154_1", s2[0], list2.get(0).getShortName())
                  .trim());


              createFlatInstanceDescriptor(s1[0], list1.get(0).getShortName(), list2.get(0).getShortName(), s1[1],
                  s1[2], swAddrMethodMap);

            }
            else {
              uri1 = GenerateArxmlUtil.getFragmentURI(this.rMap.get(s2[0] + "<->" + s2[1]));
              uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap1.get(s2[2]));

              this.uriBuf.append(" Require Port: " + uri1 + this.newLine);
              this.uriBuf.append(" Instance: " + uri2 + this.newLine);
              List<String> osApplications =
                  list2.stream().map(EcucContainerValue::getShortName).collect(Collectors.toList());
              this.uriBuf.append(" Mapped OS Applications: " + osApplications + this.newLine);
              String osAppName2 = getOsAppforRPort(s2[0], s2[1]);

              EcucContainerValue osApp2 = this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap().get(osAppName2);
              uri3 = GenerateArxmlUtil.getFragmentURI(osApp2);

              if ((osAppName2 == null) || (osAppName2.isEmpty())) {
                this.uriBuf.append(" Access OS Application: " +
                    RteConfigGeneratorConstants.WARN_FAILING_TO_CHOOSE_OS_APP + this.newLine);
              }
              else {
                this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);
              }

              if (osApp2 != null) {
                LOGGER.info(
                    RteConfGenMessageDescription.getFormattedMesssage("154_1", s2[0], osApp2.getShortName()).trim());

                createFlatInstanceDescriptor(s1[0], list1.get(0).getShortName(), osApp2.getShortName(), s1[1], s1[2],
                    swAddrMethodMap);
              }
              else {
                this.uriBuf.append(" FlatInstanceDescriptor creation: No" + this.newLine);
              }
            }

          }
        }
        else {


          String s2[] = portMap1.get(p).get(0).split("<->");
          this.uriBuf.append(this.newLine + "AssemblySwConnection: " + s1[0] + " -> " + s2[0] + this.newLine +
              "===============================================================" + this.newLine);


          String uri1 = GenerateArxmlUtil.getFragmentURI(this.pMap.get(s1[0] + "<->" + s1[1]));
          String uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap.get(s1[2]));

          this.uriBuf.append(" Provider Port: " + uri1 + this.newLine);
          this.uriBuf.append(" Instance: " + uri2 + this.newLine);
          List<String> osApplications =
              swcOsAppMapp.get(s1[2]).stream().map(EcucContainerValue::getShortName).collect(Collectors.toList());
          this.uriBuf.append(" Mapped OS Applications: " + osApplications + this.newLine);

          String osAppName1 = getOsAppforPPort(swcOsAppMapp.get(s1[2]), s1[0], s1[1]);


          EcucContainerValue osApp1 = this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap().get(osAppName1);// getEcucValue(swcOsAppMapp.get(s1[1]),
                                                                                                               // osAppName1);
          String uri3 = GenerateArxmlUtil.getFragmentURI(osApp1);

          if ((osAppName1 == null) || (osAppName1.isEmpty())) {
            this.uriBuf.append(
                " Access OS Application: " + RteConfigGeneratorConstants.WARN_FAILING_TO_CHOOSE_OS_APP + this.newLine);
          }
          else {
            this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);
          }
          LOGGER.info(RteConfGenMessageDescription
              .getFormattedMesssage("153_1", s1[0], (osApp1 != null ? osApp1.getShortName() : "")).trim());


          List<EcucContainerValue> list2 = swcOsAppMapp.get(s2[2]);


          if (list2 != null) {

            if (list2.size() == 1) {

              uri1 = GenerateArxmlUtil.getFragmentURI(this.rMap.get(s2[0] + "<->" + s2[1]));
              uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap1.get(s2[2]));
              uri3 = GenerateArxmlUtil.getFragmentURI(list2.get(0));
              this.uriBuf.append(" Require Port: " + uri1 + this.newLine);
              this.uriBuf.append(" Instance: " + uri2 + this.newLine);
              this.uriBuf.append(" Mapped OS Applications: " + list2.get(0).getShortName() + this.newLine);
              this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);
              if (osApp1 != null) {
                LOGGER.info(RteConfGenMessageDescription
                    .getFormattedMesssage("154_1", s2[0], list2.get(0).getShortName()).trim());


                createFlatInstanceDescriptor(s1[0], osApp1.getShortName(), list2.get(0).getShortName(), s1[1], s1[2],
                    swAddrMethodMap);
              }
              else {
                this.uriBuf.append(" FlatInstanceDescriptor creation: No" + this.newLine);
              }

            }
            else {

              uri1 = GenerateArxmlUtil.getFragmentURI(this.rMap.get(s2[0] + "<->" + s2[1]));
              uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap1.get(s2[2]));
              this.uriBuf.append(" Require Port: " + uri1 + this.newLine);
              this.uriBuf.append(" Instance: " + uri2 + this.newLine);

              osApplications = list2.stream().map(EcucContainerValue::getShortName).collect(Collectors.toList());

              this.uriBuf.append(" Mapped OS Applications: " + osApplications + this.newLine);

              String osAppName2 = getOsAppforRPort(s2[0], s2[1]);
              EcucContainerValue osApp2 = this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap().get(osAppName2);// getEcucValue(swcOsAppMapp.get(s2[1]),
                                                                                                                   // osAppName2);
              uri3 = GenerateArxmlUtil.getFragmentURI(osApp2);

              if ((osAppName2 == null) || (osAppName2.isEmpty())) {
                this.uriBuf.append(" Access OS Application: " +
                    RteConfigGeneratorConstants.WARN_FAILING_TO_CHOOSE_OS_APP + this.newLine);
              }
              else {
                this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);
              }

              if ((osApp1 != null) && (osApp2 != null)) {
                LOGGER.info(
                    RteConfGenMessageDescription.getFormattedMesssage("154_1", s2[0], osApp2.getShortName()).trim());


                createFlatInstanceDescriptor(s1[0], osApp1.getShortName(), osApp2.getShortName(), s1[1], s1[2],
                    swAddrMethodMap);
              }
              else {
                this.uriBuf.append(" FlatInstanceDescriptor creation: No" + this.newLine);
              }
            }
          }
        }
      }
    }

    for (String p : portMap2.keySet()) {
      String s1[] = p.split("<->");
      List<EcucContainerValue> list1 = swcOsAppMapp.get(s1[2]);

      if (list1 != null) {

        if (list1.size() == 1) {
          EcucContainerValue osApp1 = list1.get(0);
          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("153_1", s1[0], osApp1.getShortName()).trim());


          int sizeOfRports = portMap2.get(p).size();
          List<String> listOfOsApps = null;

          for (String r : portMap2.get(p)) {

            String s2[] = r.split("<->");

            this.uriBuf.append(this.newLine + "AssemblySwConnection: " + s1[0] + " -> " + s2[0] + this.newLine +
                "===============================================================" + this.newLine);
            String uri1 = GenerateArxmlUtil.getFragmentURI(this.pMap.get(s1[0] + "<->" + s1[1]));
            String uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap.get(s1[2]));
            String uri3 = GenerateArxmlUtil.getFragmentURI(osApp1);
            this.uriBuf.append(" Provider Port: " + uri1 + this.newLine);
            this.uriBuf.append(" Instance: " + uri2 + this.newLine);
            this.uriBuf.append(" Mapped OS Applications: " + list1.get(0).getShortName() + this.newLine);
            this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);

            List<EcucContainerValue> list2 = swcOsAppMapp.get(s2[2]);

            if (list2 != null) {

              if (list2.size() == 1) {

                uri1 = GenerateArxmlUtil.getFragmentURI(this.rMap.get(s2[0] + "<->" + s2[1]));
                uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap1.get(s2[2]));
                uri3 = GenerateArxmlUtil.getFragmentURI(list2.get(0));
                this.uriBuf.append(" Require Port: " + uri1 + this.newLine);
                this.uriBuf.append(" Instance: " + uri2 + this.newLine);
                this.uriBuf.append(" Mapped OS Applications: " + list2.get(0).getShortName() + this.newLine);
                this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);

                LOGGER.info(RteConfGenMessageDescription
                    .getFormattedMesssage("154_1", s2[0], list2.get(0).getShortName()).trim());

                if (listOfOsApps != null) {
                  listOfOsApps.add(list2.get(0).getShortName());
                }
                else {
                  listOfOsApps = new ArrayList<>();
                  listOfOsApps.add(list2.get(0).getShortName());
                }

              }
              else {

                uri1 = GenerateArxmlUtil.getFragmentURI(this.rMap.get(s2[0] + "<->" + s2[1]));
                uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap1.get(s2[2]));
                this.uriBuf.append(" Require Port: " + uri1 + this.newLine);
                this.uriBuf.append(" Instance: " + uri2 + this.newLine);

                List<String> osApplications =
                    list2.stream().map(EcucContainerValue::getShortName).collect(Collectors.toList());
                this.uriBuf.append(" Mapped OS Applications: " + osApplications + this.newLine);

                String osAppName2 = getOsAppforRPort(s2[0], s2[1]);
                EcucContainerValue osApp2 = this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap().get(osAppName2);// getEcucValue(swcOsAppMapp.get(s2[1]),
                                                                                                                     // osAppName2);
                uri3 = GenerateArxmlUtil.getFragmentURI(osApp2);

                if ((osAppName2 == null) || (osAppName2.isEmpty())) {
                  this.uriBuf.append(" Access OS Application: " +
                      RteConfigGeneratorConstants.WARN_FAILING_TO_CHOOSE_OS_APP + this.newLine);
                }
                else {
                  this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);
                }

                LOGGER.info(RteConfGenMessageDescription
                    .getFormattedMesssage("154_1", s2[0], (osApp2 != null ? osApp2.getShortName() : "")).trim());

                if (osApp2 != null) {
                  if (listOfOsApps != null) {
                    listOfOsApps.add(osApp2.getShortName());
                  }
                  else {
                    listOfOsApps = new ArrayList<>();
                    listOfOsApps.add(osApp2.getShortName());
                  }
                }
              }
            }
          }


          if ((listOfOsApps != null) &&
              (sizeOfRports == listOfOsApps.size()) /*
                                                     * && isNotSameOsApp(listOfOsApps, osApp1.getShortName())
                                                     */) {
            createFlatInstanceDescriptor(s1[0], osApp1.getShortName(),
                getOsAppName(new TreeSet<String>(listOfOsApps), osApp1.getShortName()), s1[1], s1[2], swAddrMethodMap);
          }
          else {
            this.uriBuf.append(" FlatInstanceDescriptor creation: No" + this.newLine);
          }
        }
        else {

          String osAppName = getOsAppforPPort(swcOsAppMapp.get(s1[2]), s1[0], s1[1]);
          EcucContainerValue osApp1 = this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap().get(osAppName);// getEcucValue(swcOsAppMapp.get(s1[1]),
          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("153_1", s1[0],
              (osApp1 != null ? osApp1.getShortName() : "")));


          int sizeOfRports = portMap2.get(p).size();
          List<String> listOfOsApps = null;


          for (String r : portMap2.get(p)) {


            String s2[] = r.split("<->");

            this.uriBuf.append(this.newLine + "AssemblySwConnection: " + s1[0] + " -> " + s2[0] + this.newLine +
                "===============================================================" + this.newLine);
            String uri1 = GenerateArxmlUtil.getFragmentURI(this.pMap.get(s1[0] + "<->" + s1[1]));
            String uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap.get(s1[2]));
            String uri3 = GenerateArxmlUtil.getFragmentURI(osApp1);
            this.uriBuf.append(" Provider Port: " + uri1 + this.newLine);
            this.uriBuf.append(" Instance: " + uri2 + this.newLine);

            List<String> osApplications =
                list1.stream().map(EcucContainerValue::getShortName).collect(Collectors.toList());
            this.uriBuf.append(" Mapped OS Applications: " + osApplications + this.newLine);

            if ((osAppName == null) || (osAppName.isEmpty())) {
              this.uriBuf.append(" Access OS Application: " +
                  RteConfigGeneratorConstants.WARN_FAILING_TO_CHOOSE_OS_APP + this.newLine);
            }
            else {
              this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);
            }


            List<EcucContainerValue> list2 = swcOsAppMapp.get(s2[2]);

            if (list2 != null) {

              if (list2.size() == 1) {
                uri1 = GenerateArxmlUtil.getFragmentURI(this.rMap.get(s2[0] + "<->" + s2[1]));
                uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap1.get(s2[2]));
                uri3 = GenerateArxmlUtil.getFragmentURI(list2.get(0));
                this.uriBuf.append(" Require Port: " + uri1 + this.newLine);
                this.uriBuf.append(" Instance: " + uri2 + this.newLine);
                this.uriBuf.append(" Mapped OS Applications: " + list2.get(0).getShortName() + this.newLine);
                this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);

                LOGGER.info(RteConfGenMessageDescription
                    .getFormattedMesssage("154_1", s2[0], list2.get(0).getShortName()).trim());

                if (listOfOsApps != null) {
                  listOfOsApps.add(list2.get(0).getShortName());
                }
                else {
                  listOfOsApps = new ArrayList<>();
                  listOfOsApps.add(list2.get(0).getShortName());
                }
              }
              else {
                uri1 = GenerateArxmlUtil.getFragmentURI(this.rMap.get(s2[0] + "<->" + s2[1]));
                uri2 = GenerateArxmlUtil.getFragmentURI(this.scMap1.get(s2[2]));
                this.uriBuf.append(" Require Port: " + uri1 + this.newLine);
                this.uriBuf.append(" Instance: " + uri2 + this.newLine);
                osApplications = list2.stream().map(EcucContainerValue::getShortName).collect(Collectors.toList());
                this.uriBuf.append(" Mapped OS Applications: " + osApplications + this.newLine);

                String osAppName2 = getOsAppforRPort(s2[0], s2[1]);
                EcucContainerValue osApp2 = this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap().get(osAppName2);// getEcucValue(swcOsAppMapp.get(s2[1]),
                                                                                                                     // osAppName2);
                uri3 = GenerateArxmlUtil.getFragmentURI(osApp2);
                if ((osAppName2 == null) || (osAppName2.isEmpty())) {
                  this.uriBuf.append(" Access OS Application: " +
                      RteConfigGeneratorConstants.WARN_FAILING_TO_CHOOSE_OS_APP + this.newLine);
                }
                else {
                  this.uriBuf.append(" Access OS Application: " + uri3 + this.newLine);
                }
                LOGGER.info(RteConfGenMessageDescription
                    .getFormattedMesssage("154_1", s2[0], (osApp2 != null ? osApp2.getShortName() : "")).trim());

                if (osApp2 != null) {
                  if (listOfOsApps != null) {
                    listOfOsApps.add(osApp2.getShortName());
                  }
                  else {
                    listOfOsApps = new ArrayList<>();
                    listOfOsApps.add(osApp2.getShortName());
                  }
                }
              }
            }
          }

          if ((osApp1 != null) && (listOfOsApps != null) &&
              (sizeOfRports == listOfOsApps.size()) /*
                                                     * && isNotSameOsApp(listOfOsApps, osApp1.getShortName())
                                                     */) {
            createFlatInstanceDescriptor(s1[0], osApp1.getShortName(),
                getOsAppName(new TreeSet<String>(listOfOsApps), osApp1.getShortName()), s1[1], s1[2], swAddrMethodMap);
          }
          else {
            this.uriBuf.append(" FlatInstanceDescriptor creation: No" + this.newLine);
          }
        }

      }

    }

  }


  private void createFlatInstanceDescriptor(final String modePPort, final String osApp1, final String osApp2,
      final String swctName, final String swcpName, final Map<String, String> swAddrMethodMap)
      throws Exception {
    boolean isfidCreated = false;


//    if (!osApp1.equals(osApp2)) {
    String swAddShortName = osApp1.equals(osApp2) ? osApp1 : (osApp1 + "_" + osApp2 + "_SHARED");
    String flatInstanceDescriptorShortName = getFlatInstanceDescriptorShortName(modePPort + '_' + swcpName);


    if (this.fMap.get(flatInstanceDescriptorShortName) == null) {


      FlatInstanceDescriptor fld = FlatmapFactory.eINSTANCE.createFlatInstanceDescriptor();
      fld.setShortName(flatInstanceDescriptorShortName);
      fld.setRole("MMI_SWADDRMETHOD");


      SwAddrMethod createSwAddrMethod = createSwAddrMethod(swAddShortName, swAddrMethodMap);


      SwDataDefProps createSwDataDefProps = DatadefpropertiesFactory.eINSTANCE.createSwDataDefProps();

      SwDataDefPropsConditional createSwDataDefPropsConditional =
          DatadefpropertiesFactory.eINSTANCE.createSwDataDefPropsConditional();

      createSwDataDefPropsConditional.setSwAddrMethod(createSwAddrMethod);

      createSwDataDefProps.getSwDataDefPropsVariants().add(createSwDataDefPropsConditional);

      fld.setSwDataDefProps(createSwDataDefProps);

      AnyInstanceRef ecuExtractReference = AnyinstancerefFactory.eINSTANCE.createAnyInstanceRef();
      ecuExtractReference.getContextElements().add(this.UpdateExcuExtractFiles.getUpdatedRootSwCompositionPrototype());
      ecuExtractReference.getContextElements().add(this.scMap.get(swcpName));
      ecuExtractReference.getContextElements().add(this.pMap.get(modePPort + "<->" + swctName));

      PortPrototype portPrototype = this.pMap.get(modePPort + "<->" + swctName);
      VariationPoint variationPoint = null;
      if (portPrototype != null) {
        PortInterface msi =
            portPrototype instanceof PPortPrototype ? ((PPortPrototype) portPrototype).getProvidedInterface()
                : ((PRPortPrototype) portPrototype).getProvidedRequiredInterface();
        ecuExtractReference.setTarget(((ModeSwitchInterface) msi).getModeGroup());
        variationPoint = VariationPointUtil.getInstance().getVariationPoint(
            this.UpdateExcuExtractFiles.getUpdatedRootSwCompositionPrototype(), this.scMap.get(swcpName),
            this.pMap.get(modePPort + "<->" + swctName), ((ModeSwitchInterface) msi).getModeGroup());
      }
      else {
        variationPoint = VariationPointUtil.getInstance().getVariationPoint(
            this.UpdateExcuExtractFiles.getUpdatedRootSwCompositionPrototype(), this.scMap.get(swcpName),
            this.pMap.get(modePPort + "<->" + swctName));
      }
      fld.setEcuExtractReference(ecuExtractReference);

      if (variationPoint != null) {
        fld.setVariationPoint(variationPoint);
      }
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("151_0", swAddShortName).trim());
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("152_0", flatInstanceDescriptorShortName).trim());

      isfidCreated = true;

      this.flatMap.getInstances().add(fld);
      this.fMap.put(flatInstanceDescriptorShortName, fld);

    }
//    }
    this.uriBuf.append(" FlatInstanceDescriptor creation: " + (isfidCreated ? "Yes" : "No") + this.newLine);

  }


  private String getOsAppName(final Set<String> listOfOsApp, final String osAppName1) {
    String name = "";

    // Collections.sort(listOfOsApp);

    for (String s : listOfOsApp) {

      if (!osAppName1.equals(s)) {
        name = name + s;
        name = name + "_";
      }
    }
    if (name.length() > 0) {
      name = name.substring(0, name.length() - 1);
    }
    else {
      name = osAppName1;
    }

    // name = name + "SHARED";

    return name;

  }

  private String getFlatInstanceDescriptorShortName(final String expectedshortName) {

    String shortName = expectedshortName;

    FlatInstanceDescriptor flatInstanceDescriptor = this.fMap.get(expectedshortName);

    if (flatInstanceDescriptor != null) {
      int i = 1;
      String expectedshortName1 = expectedshortName.substring(0, expectedshortName.lastIndexOf('_')) + '_' + i;
      shortName = getName(expectedshortName1);

    }

    return shortName;

  }

  private SwAddrMethod createSwAddrMethod(final String swAddShortName, final Map<String, String> swAddrMethodMap) {


    SwAddrMethod swAddrMethod2 = null;

    String swAddrName = swAddrMethodMap.get(swAddShortName);

    if (swAddrName != null) {
      swAddrMethod2 = this.sMap.get(swAddrName);

    }
    else {
      swAddrMethod2 = this.sMap.get(swAddShortName);
    }


    if (swAddrMethod2 != null) {
      return swAddrMethod2;
    }


    SwAddrMethod swAddrMethod = AuxillaryobjectsFactory.eINSTANCE.createSwAddrMethod();
    String newShortName = GenerateArxmlUtil.getShortenedNameOfElements(swAddShortName,
        Collections.unmodifiableMap(this.sMap), swAddrMethodMap);
    if ((newShortName != null) && !newShortName.isEmpty() && (swAddShortName.length() > 127)) {
      swAddrMethodMap.put(swAddShortName, newShortName);
    }


    swAddrMethod.setShortName(newShortName);
    swAddrMethod
        .setMemoryAllocationKeywordPolicy(MemoryAllocationKeywordPolicyType.ADDR_METHOD_SHORT_NAME_AND_ALIGNMENT);
    swAddrMethod.setSectionInitializationPolicy("INIT");
    swAddrMethod.setSectionType(MemorySectionType.VAR);

    this.swAddrMethodPackage.getElements().add(swAddrMethod);
    this.sMap.put(newShortName, swAddrMethod);

    return swAddrMethod;

  }

  private String getName(final String expectedshortName) {

    String name = expectedshortName;

    FlatInstanceDescriptor flatInstanceDescriptor = this.fMap.get(expectedshortName);

    if (flatInstanceDescriptor != null) {

      String suffix = expectedshortName.substring(expectedshortName.lastIndexOf('_') + 1, expectedshortName.length());
      try {
        int i = Integer.parseInt(suffix);
        i++;
        String expectedshortName1 = expectedshortName.substring(0, expectedshortName.lastIndexOf('_')) + '_' + i;
        name = getName(expectedshortName1);
      }
      catch (Exception ex) {
        int i = 1;
        String expectedshortName1 = expectedshortName.substring(0, expectedshortName.lastIndexOf('_')) + '_' + i;
        name = getName(expectedshortName1);
      }
      name = GenerateArxmlUtil.getShortenedNameOfElements(name, Collections.unmodifiableMap(this.fMap));

    }


    return name;

  }

  /**
   * @param ecucContainer
   * @param taskOsAppMap
   * @param taskWithMoreOsApp
   * @throws Exception
   */
  private void getmappedOSApp(final EcucContainerValue ecucContainer,
      final Map<String, EcucContainerValue> taskOsAppMap, final List<EcucContainerValue> taskWithMoreOsApp)
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
              taskWithMoreOsApp.add((EcucContainerValue) ecurefer.getValue());
              LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("127_0", taskname).trim());

            }
            else {
              taskOsAppMap.put(taskname, ecucContainer);
            }
          }
        }
      }
    }


  }

  public void getTaskOsAppMapping() throws Exception {
    Map<String, EcucContainerValue> taskOsAppMap = new HashMap<>();
    List<EcucContainerValue> taskWithMoreOsApp = new ArrayList<>();
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
//    for (EcucContainerValue tasktoremove : taskWithMoreOsApp) {
//      taskOsAppMap.remove(tasktoremove);
//    }
    this.taskOsAppMap.putAll(taskOsAppMap);

  }

  private void getEventTaskMappings() {
    List<EcucContainerValue> ecuccontainer =
        GenerateArxmlUtil.getListOfEObject(this.project, EcucContainerValue.class, "");

    List<EcucContainerValue> rteEcucContainerValues = this.tischedToEcuCValueMapping.getRteEcucContainerValues();

    for (EcucContainerValue val : rteEcucContainerValues) {

      if ((val != null) && (val.getDefinition() != null) && (val.getShortName() != null)) {

        if (val.getDefinition().getShortName().equals("RteSwComponentInstance")) {

          ecuccontainer.addAll(val.getSubContainers());
        }

      }
    }


    for (EcucContainerValue val : ecuccontainer) {

      AutosarUtil.setCurrentProcessingEObject(val);

      if ((val != null) && (val.getDefinition() != null) && (val.getShortName() != null)) {

        if (val.getDefinition().getShortName().equals("RteEventToTaskMapping")) {
          String eventName = null;
          String taskName = null;
          EcucContainerValue taskContainer = null;

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
              taskContainer = (EcucContainerValue) ecucval.getValue();
              if (taskName.isEmpty()) {
                LOGGER.info("MM_DCS_RTECONFGEN_DEBUG: EcuC value reference for RteMappedToTaskRef is missing in " +
                    val.getShortName() + " EcucContainer");
              }
            }
          }
          if ((eventName != null) && (!eventName.isEmpty() && (taskContainer != null)) /* && !taskName.isEmpty() */) {
            this.eventTaskMap.put(eventName, taskContainer);
          }
        }

      }

      else {

        LOGGER.warn("MM_DCS_RTECONFGEN_DEBUG: Ecuc Container '" +
            (val != null ? val.getShortName().toString() : "NULL") + "' contains missing/invalid configuration");

      }
    }
  }

  private List<String> getListOfOsApps(final List<EcucContainerValue> lecuc) {
    List<String> ls = new ArrayList<>();
    for (EcucContainerValue ecuc : lecuc) {
      ls.add(ecuc.getShortName());
    }
    return ls;
  }


  public String getOsAppforPPort(final List<EcucContainerValue> listOfOsApps, final String portName,
      final String swctName)
      throws Exception {

    String osAppName = null;
    if (listOfOsApps != null) {

      List<String> osApplications =
          listOfOsApps.stream().map(EcucContainerValue::getShortName).collect(Collectors.toList());

      ModeSwitchPoint modeSwitchPoint = this.pMspMap.get(portName + "<->" + swctName);

      if ((modeSwitchPoint != null) && (modeSwitchPoint.getRunnableEntity() != null)) {

        RTEEvent rteEvent = this.ruEvMap.get(modeSwitchPoint.getRunnableEntity().getShortName() + "<->" + swctName);

        if ((rteEvent != null) && (this.eventTaskMap.get(rteEvent.getShortName()) != null)) {
          EcucContainerValue task = this.eventTaskMap.get(rteEvent.getShortName());
          if (task != null) {
            EcucContainerValue osApp = this.taskOsAppMap.get(task.getShortName());
            if (osApp != null) {
              osAppName = osApp.getShortName();
            }
          }
          if (osAppName != null) {
            LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("153_0", portName, rteEvent.getShortName(),
                modeSwitchPoint.getRunnableEntity().getShortName()).trim());

          }

        }

      }


      if (osAppName == null) {

        List<ModeAccessPoint> list = this.pMapMap.get(portName + "<->" + swctName);
        if (list != null) {
          for (ModeAccessPoint map : list) {
            AutosarUtil.setCurrentProcessingEObject(map);

            if ((map != null) && (map.getRunnableEntity() != null)) {

              RTEEvent rteEvent = this.ruEvMap.get(map.getRunnableEntity().getShortName() + "<->" + swctName);

              if ((rteEvent != null) && (this.eventTaskMap.get(rteEvent.getShortName()) != null)) {
                EcucContainerValue task = this.eventTaskMap.get(rteEvent.getShortName());
                if (task != null) {
                  EcucContainerValue osApp = this.taskOsAppMap.get(task.getShortName());
                  if (osApp != null) {
                    osAppName = osApp.getShortName();
                  }
                }

                if (osAppName != null) {
                  LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("153_0", portName,
                      rteEvent.getShortName(), map.getRunnableEntity().getShortName()).trim());

                }
              }

            }

            if (osAppName != null) {
              return osAppName;
            }
          }
        }

      }


      if (!osApplications.contains(osAppName)) {
        return null;
      }
    }
    return osAppName;
  }


  public String getOsAppforRPort(final String portName, final String swctName) throws Exception {

    String osAppName = null;
    List<ModeAccessPoint> list = this.rMapMap.get(portName + "<->" + swctName);
    if (list != null) {
      for (ModeAccessPoint map : list) {
        AutosarUtil.setCurrentProcessingEObject(map);

        if ((map != null) && (map.getRunnableEntity() != null)) {

          RTEEvent rteEvent = this.ruEvMap.get(map.getRunnableEntity().getShortName() + "<->" + swctName);

          if ((rteEvent != null) && (this.eventTaskMap.get(rteEvent.getShortName()) != null)) {
            EcucContainerValue task = this.eventTaskMap.get(rteEvent.getShortName());
            if (task != null) {
              EcucContainerValue osApp = this.taskOsAppMap.get(task.getShortName());
              if (osApp != null) {
                osAppName = osApp.getShortName();
                LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("154_0", portName,
                    rteEvent.getShortName(), map.getRunnableEntity().getShortName()).trim());

              }
            }
          }


        }

        if (osAppName != null) {
          return osAppName;
        }
      }
    }

    if (osAppName == null) {
      SwcModeSwitchEvent modeSwitchEvent = this.rMSEMap.get(portName + "<->" + swctName);

      if ((modeSwitchEvent != null) && (this.eventTaskMap.get(modeSwitchEvent.getShortName()) != null)) {
        EcucContainerValue task = this.eventTaskMap.get(modeSwitchEvent.getShortName());
        if (task != null) {
          EcucContainerValue osApp = this.taskOsAppMap.get(task.getShortName());
          if (osApp != null) {
            osAppName = osApp.getShortName();
          }
        }
      }

      if (osAppName != null) {
        return osAppName;
      }

      if ((modeSwitchEvent != null) && (modeSwitchEvent.getStartOnEvent() != null)) {

        RTEEvent rteEvent = this.ruEvMap.get(modeSwitchEvent.getStartOnEvent().getShortName() + "<->" + swctName);

        if ((rteEvent != null) && (this.eventTaskMap.get(rteEvent.getShortName()) != null)) {
          EcucContainerValue task = this.eventTaskMap.get(rteEvent.getShortName());
          if (task != null) {
            EcucContainerValue osApp = this.taskOsAppMap.get(task.getShortName());
            if (osApp != null) {
              osAppName = osApp.getShortName();
              LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("154_0", portName, rteEvent.getShortName(),
                  modeSwitchEvent.getStartOnEvent().getShortName()).trim());

            }
          }
        }
      }

      if (osAppName != null) {
        return osAppName;
      }
    }

    if (osAppName == null) {

      if (list != null) {
        for (ModeAccessPoint map : list) {

          AutosarUtil.setCurrentProcessingEObject(map);

          if ((map != null) && (map.getRunnableEntity() != null)) {

            RTEEvent rteEvent = this.ruEvMap.get(map.getRunnableEntity().getShortName() + "<->" + swctName);

            if ((rteEvent != null) && (rteEvent instanceof OperationInvokedEvent)) {
              OperationInvokedEvent oie = (OperationInvokedEvent) rteEvent;
              String pPort = this.oipMap.get(oie.getShortName() + "<->" + swctName);

              if ((oie.getOperation() != null) && (oie.getOperation().getTargetProvidedOperation() != null) &&
                  (pPort != null) && (this.pRMap.get(pPort + "<->" + swctName) != null)) {

                for (PortPrototype rPort : this.pRMap.get(pPort + "<->" + swctName)) {
                  AutosarUtil.setCurrentProcessingEObject(rPort);

                  List<ServerCallPoint> list2 =
                      this.rSCPMap.get(rPort.getShortName() + "<->" + rPort.getSwComponentType().getShortName());

                  if (list2 != null) {

                    for (ServerCallPoint scp : list2) {
                      AutosarUtil.setCurrentProcessingEObject(scp);

                      if ((scp.getRunnableEntity() != null) && (scp.getOperation() != null) &&
                          (scp.getOperation().getTargetRequiredOperation() != null)) {

                        String shortName1 = oie.getOperation().getTargetProvidedOperation().getShortName();
                        String shortName2 = scp.getOperation().getTargetRequiredOperation().getShortName();

                        if (shortName1.equals(shortName2)) {

                          RTEEvent rteEvent1 = this.ruEvMap.get(scp.getRunnableEntity().getShortName() + "<->" +
                              rPort.getSwComponentType().getShortName());

                          if ((rteEvent1 != null) && (this.eventTaskMap.get(rteEvent1.getShortName()) != null)) {
                            EcucContainerValue task = this.eventTaskMap.get(rteEvent1.getShortName());
                            if (task != null) {
                              EcucContainerValue osApp = this.taskOsAppMap.get(task.getShortName());
                              if (osApp != null) {
                                osAppName = osApp.getShortName();
                              }
                            }
                          }

                          if (osAppName != null) {
                            LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("154_0", portName,
                                rteEvent.getShortName(), scp.getRunnableEntity().getShortName()).trim());
                            return osAppName;
                          }

                        }
                      }
                    }
                  }

                }
              }

            }
            else if ((rteEvent != null) && (rteEvent instanceof DataReceivedEvent)) {

              DataReceivedEvent dre = (DataReceivedEvent) rteEvent;
              String rPort = this.derMap.get(dre.getShortName() + "<->" + swctName);

              if ((dre.getData() != null) && (dre.getData().getTargetDataElement() != null) && (rPort != null) &&
                  (this.rPMap.get(rPort + "<->" + swctName) != null)) {

                for (PortPrototype pPort : this.rPMap.get(rPort + "<->" + swctName)) {
                  AutosarUtil.setCurrentProcessingEObject(pPort);
                  List<VariableAccess> list2 =
                      this.pVMap.get(pPort.getShortName() + "<->" + pPort.getSwComponentType().getShortName());

                  if (list2 != null) {

                    for (VariableAccess va : list2) {
                      AutosarUtil.setCurrentProcessingEObject(va);
                      List<RunnableEntity> rl =
                          this.vRuVMap.get(va.getShortName() + "<->" + pPort.getSwComponentType().getShortName());
                      if ((rl != null) && (va.getAccessedVariable() != null) && (va.getAccessedVariable() != null) &&
                          (va.getAccessedVariable().getAutosarVariable() != null) &&
                          (va.getAccessedVariable().getAutosarVariable().getTargetDataPrototype() != null)) {
                        for (RunnableEntity re : rl) {
                          AutosarUtil.setCurrentProcessingEObject(re);
                          String shortName1 =
                              va.getAccessedVariable().getAutosarVariable().getTargetDataPrototype().getShortName();
                          String shortName2 = dre.getData().getTargetDataElement().getShortName();

                          if (shortName1.equals(shortName2)) {

                            RTEEvent rteEvent1 =
                                this.ruEvMap.get(re.getShortName() + "<->" + pPort.getSwComponentType().getShortName());

                            if ((rteEvent1 != null) && (this.eventTaskMap.get(rteEvent1.getShortName()) != null)) {
                              EcucContainerValue task = this.eventTaskMap.get(rteEvent1.getShortName());
                              if (task != null) {
                                EcucContainerValue osApp = this.taskOsAppMap.get(task.getShortName());
                                if (osApp != null) {
                                  osAppName = osApp.getShortName();
                                }
                              }
                            }

                            if (osAppName != null) {
                              LOGGER.info(RteConfGenMessageDescription
                                  .getFormattedMesssage("154_0", portName, rteEvent.getShortName(), re.getShortName())
                                  .trim());

                              return osAppName;
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

          if (osAppName != null) {
            return osAppName;
          }
        }
      }
    }

    return osAppName;
  }


}
