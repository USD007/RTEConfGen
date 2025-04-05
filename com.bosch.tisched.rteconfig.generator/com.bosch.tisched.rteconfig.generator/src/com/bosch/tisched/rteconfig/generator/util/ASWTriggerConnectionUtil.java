/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;

import com.bosch.tisched.rteconfig.generator.core.OsConfigToEcucValueMapping;
import com.bosch.tisched.rteconfig.generator.core.TischedEvent;
import com.bosch.tisched.rteconfig.generator.core.TischedTask;
import com.bosch.tisched.rteconfig.generator.ecuextract.UpdateExcuExtractFiles;

import autosar40.autosartoplevelstructure.AUTOSAR;
import autosar40.commonstructure.triggerdeclaration.Trigger;
import autosar40.commonstructure.triggerdeclaration.TriggerMapping;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.ecucdescription.EcucReferenceValue;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import autosar40.swcomponent.components.AbstractProvidedPortPrototype;
import autosar40.swcomponent.components.AbstractRequiredPortPrototype;
import autosar40.swcomponent.components.AtomicSwComponentType;
import autosar40.swcomponent.components.PPortPrototype;
import autosar40.swcomponent.components.PRPortPrototype;
import autosar40.swcomponent.components.PortPrototype;
import autosar40.swcomponent.components.RPortPrototype;
import autosar40.swcomponent.components.instancerefs.RTriggerInAtomicSwcInstanceRef;
import autosar40.swcomponent.composition.AssemblySwConnector;
import autosar40.swcomponent.composition.CompositionFactory;
import autosar40.swcomponent.composition.CompositionSwComponentType;
import autosar40.swcomponent.composition.SwComponentPrototype;
import autosar40.swcomponent.composition.SwConnector;
import autosar40.swcomponent.composition.instancerefs.InstancerefsFactory;
import autosar40.swcomponent.composition.instancerefs.PPortInCompositionInstanceRef;
import autosar40.swcomponent.composition.instancerefs.RPortInCompositionInstanceRef;
import autosar40.swcomponent.portinterface.PortInterface;
import autosar40.swcomponent.portinterface.TriggerInterface;
import autosar40.swcomponent.portinterface.TriggerInterfaceMapping;
import autosar40.swcomponent.swcinternalbehavior.RunnableEntity;
import autosar40.swcomponent.swcinternalbehavior.rteevents.ExternalTriggerOccurredEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.RTEEvent;
import autosar40.swcomponent.swcinternalbehavior.trigger.ExternalTriggeringPoint;
import autosar40.system.RootSwCompositionPrototype;
import autosar40.util.Autosar40ReleaseDescriptor;

/**
 * @author DTB1KOR
 */
public class ASWTriggerConnectionUtil {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(ASWTriggerConnectionUtil.class.getName());
  private final OsConfigToEcucValueMapping osConfigToEcucValueMapping;
  private final AssemblySwConnectionUtil assemblySwConnUtil;
  private final IProject project;
  private final String autosarReleaseVersion;
  private final String autosarResourceVersion;
  private final String flatviewfilepath;
  private final Map<String, PortPrototype> uriToPortMap;
  private final Map<String, List<String>> aswcToPortsMap;
  private List<AssemblySwConnector> triggerConnList;
  private final RootSwCompositionPrototype rootSwCompositionPrototype;
  private final List<TischedTask> etoTaskList;
  private final String ecuInstancePath;
  private final String excludeDir;
  private final Map<String, List<TriggerMapping>> triggerMappings = new HashMap<>();
  private final Map<String, TriggerInterfaceMapping> uriToTriggerMappingMap = new HashMap<>();
  private final boolean btriggerErrorAsWarning;


  /**
   * @param UpdateExcuExtractFiles
   * @param osConfigToEcucValueMapping
   * @param assemblySwConnUtil
   * @param project
   * @param map
   * @throws Exception
   */
  public ASWTriggerConnectionUtil(final OsConfigToEcucValueMapping osConfigToEcucValueMapping,
      final AssemblySwConnectionUtil assemblySwConnUtil, final IProject project, final Map<String, String> map)
      throws Exception {

    this.osConfigToEcucValueMapping = osConfigToEcucValueMapping;
    this.etoTaskList = this.osConfigToEcucValueMapping.getETOTaskList();
    this.assemblySwConnUtil = assemblySwConnUtil;
    this.project = project;
    this.autosarReleaseVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION);
    this.autosarResourceVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION);
    this.flatviewfilepath = map.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_VIEW_FILE_OUTPUT_PATH);
    this.uriToPortMap = this.assemblySwConnUtil.getUriToPortMap();
    this.aswcToPortsMap = this.assemblySwConnUtil.getASWCToPortsMap();
    this.ecuInstancePath = map.get(RteConfigGeneratorConstants.ECU_INSTANCE_PKG_PATH);
    this.excludeDir = map.get(RteConfigGeneratorConstants.EXCLUDE_ARXML_PATH);
    this.btriggerErrorAsWarning =
        map.get(RteConfigGeneratorConstants.RTECONFGEN_TRIGGER_VALIDATION_ERROR_AS_WARNING).equals("y") ? true : false;
    this.rootSwCompositionPrototype = this.osConfigToEcucValueMapping.getRootSwCompositionProtoType();
    getListOfTriggerConnectors();
    getTriggerMapping();

  }


  /**
   *
   */
  private void getTriggerMapping() {
    List<TriggerInterfaceMapping> triggerInterfaceMappingList =
        GenerateArxmlUtil.getListOfEObject(this.project, TriggerInterfaceMapping.class, "");
    try {
      if (!triggerInterfaceMappingList.isEmpty()) {
        triggerInterfaceMappingList.stream().forEach(ifMap -> {
          if ((ifMap.getTriggerMappings() != null) && !ifMap.getTriggerMappings().isEmpty()) {
            EList<TriggerMapping> triggerMappingList = ifMap.getTriggerMappings();
            String ifMapURI = GenerateArxmlUtil.getFragmentURI(ifMap);
            this.triggerMappings.put(ifMapURI, triggerMappingList);
            this.uriToTriggerMappingMap.put(ifMapURI, ifMap);
          }
        });
      }
    }
    catch (Exception e) {
      System.out.println(e);
    }

  }


  /**
   *
   */
  private void getListOfTriggerConnectors() {
    if ((this.rootSwCompositionPrototype != null) &&
        (this.rootSwCompositionPrototype.getSoftwareComposition() != null) &&
        !this.rootSwCompositionPrototype.getSoftwareComposition().getConnectors().isEmpty()) {
      List<AssemblySwConnector> listOfAssemblySwConnectors = new ArrayList<>();
      this.rootSwCompositionPrototype.getSoftwareComposition().getConnectors().stream().forEach(swc -> {
        if (swc instanceof AssemblySwConnector) {
          listOfAssemblySwConnectors.add((AssemblySwConnector) swc);
        }
      });

      this.triggerConnList = listOfAssemblySwConnectors.stream().filter(aswc -> isValidTriggerConnection(aswc))
          .collect(Collectors.toList());
    }
  }

  /**
   * @param UpdateExcuExtractFiles
   * @throws Exception
   */
  public void generateASWTriggerConnection(final UpdateExcuExtractFiles UpdateExcuExtractFiles) throws Exception {


    IResource findMember = this.project.findMember(this.flatviewfilepath);
    if ((findMember != null) && findMember.exists()) {

      Resource resource = EcorePlatformUtil.getResource((IFile) findMember);
      AUTOSAR autosarRoot = EcoreUtil.copy((AUTOSAR) resource.getContents().get(0));
      ARPackage flatviewpkg = GenerateArxmlUtil.getARArPackageFromRoot(autosarRoot,
          GenerateArxmlUtil.getFragmentURI(UpdateExcuExtractFiles.getFlatView().eContainer()));
      CompositionSwComponentType flatview = (CompositionSwComponentType) flatviewpkg.getElements().get(0);

      createASWTriggerConnections(flatview, UpdateExcuExtractFiles);

      IStatus saveFile = GenerateArxmlUtil.saveFile(this.project, autosarRoot,
          URI.createPlatformResourceURI(findMember.getFullPath().toOSString(), false),
          AutosarUtil.getMetaModelDescriptorByAutosarResoureVersion(
              AutosarUtil.getMetaModelDescriptorByAutosarReleaseVersion(this.autosarReleaseVersion),
              this.autosarResourceVersion));

      if (saveFile.isOK()) {
        LOGGER.info(
            RteConfGenMessageDescription.getFormattedMesssage("150_2", findMember.getFullPath().toOSString()).trim());
      }
      else {
        LOGGER.warn("*** Updating FlatView file for Trigger connection is failed");
      }
    }
  }


  private void createASWTriggerConnections(final CompositionSwComponentType flatview,
      final UpdateExcuExtractFiles UpdateExcuExtractFiles)
      throws Exception {

    List<TischedTask> etoTasks = this.etoTaskList;
    Map<String, List<String>> aswcToPortsMap2 = this.assemblySwConnUtil.getASWCToPortsMap();
    List<SwConnector> swconList = new ArrayList<>();

    TransactionalEditingDomain arEditingDomain = WorkspaceEditingDomainUtil
        .getEditingDomain(this.rootSwCompositionPrototype.getSoftwareComposition().eResource());

    AbstractEMFOperation abstractEmfOperation =
        new AbstractEMFOperation(arEditingDomain, "Updating Ecu Flatview file") {

          @Override
          protected IStatus doExecute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {


            Map<String, List<SwComponentPrototype>> swcTypeToSWCPMap = getSWCTypeToSWCPMap(
                ASWTriggerConnectionUtil.this.rootSwCompositionPrototype.getSoftwareComposition().getComponents());

            for (TischedTask task : etoTasks) {
              AtomicBoolean bConnectionCreated = new AtomicBoolean(false);
              String osPPortURI = task.getOsPPortPrototype();
              Map<String, Map<SwComponentPrototype, AbstractRequiredPortPrototype>> rPortSWCPMap =
                  validateAndGetRPortSWCPMap(task);
              List<Entry<String, PortPrototype>> collect = ASWTriggerConnectionUtil.this.uriToPortMap.entrySet()
                  .stream().filter(uri -> uri.getKey().equals(osPPortURI)).collect(Collectors.toList());

              // component created by OASE will have single instance
              if ((collect != null) && !collect.isEmpty() && (collect.size() == 1) &&
                  (collect.get(0).getValue() instanceof AbstractProvidedPortPrototype)) {
                AbstractProvidedPortPrototype pPort = (AbstractProvidedPortPrototype) collect.get(0).getValue();

                List<SwComponentPrototype> pSWCPList =
                    swcTypeToSWCPMap.get(GenerateArxmlUtil.getFragmentURI(pPort.getSwComponentType()));

                if ((rPortSWCPMap != null) && !rPortSWCPMap.isEmpty() && (pSWCPList != null) && !pSWCPList.isEmpty() &&
                    (pSWCPList.size() == 1)) {
                  for (Entry<String, Map<SwComponentPrototype, AbstractRequiredPortPrototype>> entry : rPortSWCPMap
                      .entrySet()) {

                    try {

                      createAssemblyConnection(task, pPort, pSWCPList.get(0), entry.getValue(), swconList,
                          bConnectionCreated, UpdateExcuExtractFiles, aswcToPortsMap2);

                    }
                    catch (Exception e) {
                      e.printStackTrace();
                    }
                  }
                }
              }
              else {
                try {
                  LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("273_0", task.getShortName()));
                }
                catch (Exception e) {
                  e.printStackTrace();
                }
              }
              if (!bConnectionCreated.getPlain()) {
                try {
                  LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("273_1", task.getShortName()));
                }
                catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }
            if (!swconList.isEmpty()) {
              ASWTriggerConnectionUtil.this.rootSwCompositionPrototype.getSoftwareComposition().getConnectors()
                  .addAll(swconList);
              flatview.getConnectors().addAll(EcoreUtil.copyAll(swconList));

            }
            return Status.OK_STATUS;
          }


        };
    try {
      WorkspaceTransactionUtil.getOperationHistory(arEditingDomain).execute(abstractEmfOperation,
          new NullProgressMonitor(), null);
    }
    catch (ExecutionException e) {
      LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
    }

    for (SwConnector swc : swconList) {
      if (swc instanceof AssemblySwConnector) {
        UpdateExcuExtractFiles.getAssemblySwConnectors().add((AssemblySwConnector) swc);
      }
    }
  }

  private boolean aswConnectionExists(final Map<String, List<String>> aswcToPortsMap2,
      final AbstractProvidedPortPrototype pPort, final AbstractRequiredPortPrototype rPort) {
    String pPortURI = GenerateArxmlUtil.getFragmentURI(pPort);
    String rPortURI = GenerateArxmlUtil.getFragmentURI(rPort);
    return aswcToPortsMap2.entrySet().stream().anyMatch(e -> (e.getValue().size() == 2) &&
        e.getValue().get(0).equals(pPortURI) && e.getValue().get(1).equals(rPortURI));
  }

  /**
   * @param components
   * @return
   */
  private Map<String, List<SwComponentPrototype>> getSWCTypeToSWCPMap(final EList<SwComponentPrototype> components) {

    Map<String, List<SwComponentPrototype>> swcTypeToSWCPMap = new HashMap<String, List<SwComponentPrototype>>();

    for (SwComponentPrototype swcp : components) {
      List<SwComponentPrototype> list = swcTypeToSWCPMap.get(GenerateArxmlUtil.getFragmentURI(swcp.getType()));
      if ((list != null) && !list.isEmpty()) {
        list.add(swcp);
      }
      else {
        List<SwComponentPrototype> list1 = new ArrayList<SwComponentPrototype>();
        list1.add(swcp);
        swcTypeToSWCPMap.put(GenerateArxmlUtil.getFragmentURI(swcp.getType()), list1);
      }
    }
    return swcTypeToSWCPMap;
  }

  /**
   * @param task
   * @return
   */
  private Map<String, Map<SwComponentPrototype, AbstractRequiredPortPrototype>> validateAndGetRPortSWCPMap(
      final TischedTask task) {
    Map<String, Map<SwComponentPrototype, AbstractRequiredPortPrototype>> rportSWCPMap = new HashMap<>();

    for (TischedEvent event : task.getEvents()) {
      if (event.getEvent() instanceof ExternalTriggerOccurredEvent) {
        ExternalTriggerOccurredEvent etoEvent = (ExternalTriggerOccurredEvent) event.getEvent();
        if (etoEvent.getTrigger() != null) {
          AbstractRequiredPortPrototype contextRPort = etoEvent.getTrigger().getContextRPort();
          SwComponentPrototype rSWCP = (SwComponentPrototype) event.getInstance();
          if ((contextRPort != null) && !contextRPort.eIsProxy() && (rSWCP != null) && !rSWCP.eIsProxy() &&
              !rportSWCPMap.containsKey(rSWCP.getShortName() + contextRPort.getShortName())) {
            Map<SwComponentPrototype, AbstractRequiredPortPrototype> swcpToPortMap = new HashMap<>();
            swcpToPortMap.put(rSWCP, contextRPort);
            rportSWCPMap.put(rSWCP.getShortName() + contextRPort.getShortName(), swcpToPortMap);
          }
        }
      }
    }
    return rportSWCPMap;
  }

  /**
   * @param task
   * @param pPort
   * @param pSWCP
   * @param entry
   * @param swconList
   * @param bConnectionCreated
   * @param UpdateExcuExtractFiles
   * @param aswcToPortsMap2
   * @throws Exception
   */
  private void createAssemblyConnection(final TischedTask task, final AbstractProvidedPortPrototype pPort,
      final SwComponentPrototype pSWCP, final Map<SwComponentPrototype, AbstractRequiredPortPrototype> rportSWCPMap,
      final List<SwConnector> swconList, final AtomicBoolean bConnectionCreated,
      final UpdateExcuExtractFiles UpdateExcuExtractFiles, final Map<String, List<String>> aswcToPortsMap2)
      throws Exception {

    Entry<SwComponentPrototype, AbstractRequiredPortPrototype> entry = rportSWCPMap.entrySet().iterator().next();
    AbstractRequiredPortPrototype rPort = entry.getValue();
    SwComponentPrototype rSWCP = entry.getKey();
    TriggerInterfaceMapping interfaceMapping = null;
    if (!aswConnectionExists(aswcToPortsMap2, pPort, entry.getValue())) {

      boolean bSameTrigger = validateTriggers(pPort, rPort);

      if (!bSameTrigger) {
        interfaceMapping = isPRTriggersMapped(pPort, rPort);
        bSameTrigger = interfaceMapping == null ? false : true;
      }

      if (bSameTrigger) {

        AssemblySwConnector createAssemblySwConnector = CompositionFactory.eINSTANCE.createAssemblySwConnector();

        PPortInCompositionInstanceRef createPPortInCompositionInstanceRef =
            InstancerefsFactory.eINSTANCE.createPPortInCompositionInstanceRef();
        createPPortInCompositionInstanceRef.setTargetPPort(pPort);
        createPPortInCompositionInstanceRef.setContextComponent(pSWCP);
        createAssemblySwConnector.setProvider(createPPortInCompositionInstanceRef);

        RPortInCompositionInstanceRef createRPortInCompositionInstanceRef =
            InstancerefsFactory.eINSTANCE.createRPortInCompositionInstanceRef();
        createRPortInCompositionInstanceRef.setTargetRPort(rPort);
        createRPortInCompositionInstanceRef.setContextComponent(rSWCP);
        createAssemblySwConnector.setRequester(createRPortInCompositionInstanceRef);

        String shortnameofASC = UpdateExcuExtractFiles.getShortNameOfASC("ASC_" + pSWCP.getShortName() + "_" +
            pPort.getShortName() + "_" + rSWCP.getShortName() + "_" + rPort.getShortName(), swconList);

        createAssemblySwConnector.setShortName(shortnameofASC);

        if (interfaceMapping != null) {
          createAssemblySwConnector.setMapping(interfaceMapping);
        }

        swconList.add(createAssemblySwConnector);
        bConnectionCreated.set(true);
        this.assemblySwConnUtil.updateASWCToSWCPMap(shortnameofASC, pPort, rPort, pSWCP.getShortName(),
            rSWCP.getShortName());

      }
      else {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("271_3", pPort.getShortName(),
            rPort.getShortName(), task.getShortName()));
      }
    }
    else {
      bConnectionCreated.set(true);
    }
  }

  /**
   * @param pport
   * @param rport
   * @param UpdateExcuExtractFiles
   * @return
   */
  private boolean validateTriggers(final PortPrototype pport, final PortPrototype rport) {

    boolean isValid = false;
    TriggerInterface pTriggerIFace = getTriggerIFaceFromPort(pport);
    TriggerInterface rTriggerIFace = getTriggerIFaceFromPort(rport);

    if ((pTriggerIFace != null) && (rTriggerIFace != null)) {

      EList<Trigger> triggers1 = pTriggerIFace.getTriggers();
      EList<Trigger> triggers2 = rTriggerIFace.getTriggers();

      if (pTriggerIFace.getShortName().equals(rTriggerIFace.getShortName()) && (triggers1.size() == triggers2.size()) &&
          GenerateArxmlUtil.isTriggersSame(triggers1, triggers2)) {
        isValid = true;
      }
    }
    return isValid;
  }

  private TriggerInterface getTriggerIFaceFromPort(final PortPrototype port) {
    TriggerInterface tinterface = null;

    PortInterface portIFace = null;

    if (port instanceof PPortPrototype) {
      portIFace = ((PPortPrototype) port).getProvidedInterface();
    }
    else if (port instanceof PRPortPrototype) {
      portIFace = ((PRPortPrototype) port).getProvidedRequiredInterface();
    }
    else if (port instanceof RPortPrototype) {
      portIFace = ((RPortPrototype) port).getRequiredInterface();
    }
    else if ((port instanceof PRPortPrototype)) {
      portIFace = ((PRPortPrototype) port).getProvidedRequiredInterface();
    }

    if ((portIFace != null) && (portIFace instanceof TriggerInterface)) {

      tinterface = (TriggerInterface) portIFace;

    }
    return tinterface;
  }


  /**
   * @param pPort
   * @param rPort
   * @return
   */
  private TriggerInterfaceMapping isPRTriggersMapped(final PortPrototype pPort, final PortPrototype rPort) {
    TriggerInterfaceMapping ifMapping = null;
    TriggerInterface pTriggerIFace = getTriggerIFaceFromPort(pPort);
    TriggerInterface rTriggerIFace = getTriggerIFaceFromPort(rPort);


    if ((pTriggerIFace != null) && (rTriggerIFace != null)) {

      EList<Trigger> triggers1 = pTriggerIFace.getTriggers();
      EList<Trigger> triggers2 = rTriggerIFace.getTriggers();


      for (Trigger pTrigger : triggers1) {
        ifMapping = null;
        String pURI = GenerateArxmlUtil.getFragmentURI(pTrigger);
        for (Trigger rTrigger : triggers2) {
          String rURI = GenerateArxmlUtil.getFragmentURI(rTrigger);
          String ifMappingURI = isMappingPresent(rURI, pURI);
          if (ifMappingURI != null) {
            ifMapping = this.uriToTriggerMappingMap.get(ifMappingURI);
          }
        }
      }
    }

    return ifMapping;
  }


  /**
   * @throws Exception
   */
  public void validateTriggerPorts() throws Exception {

    validateOASETriggers();

    Map<String, List<RTEEvent>> osTaskToEventMap = getOSTaskToEventMapping();
    if (!osTaskToEventMap.isEmpty()) {
      validateEcucValueTriggers(osTaskToEventMap);
    }
  }

  /**
   * @param osTaskToEventMap
   */
  private Map<String, List<RTEEvent>> getOSTaskToEventMapping() {
    Map<String, List<RTEEvent>> osTaskToEventMap = new HashMap<>();
    final TransactionalEditingDomain currentEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(this.project, Autosar40ReleaseDescriptor.INSTANCE);
    ResourceSet resourceSet = currentEditingDomain.getResourceSet();

    List<EcucModuleConfigurationValues> allInstancesOf =
        EObjectUtil.getAllInstancesOf(resourceSet.getResources(), EcucModuleConfigurationValues.class, true);

    if ((allInstancesOf != null) && !allInstancesOf.isEmpty()) {
      EcucModuleConfigurationValues rteContainer =
          allInstancesOf.stream().filter(c -> c.getShortName().equals("Rte")).findFirst().orElse(null);
      for (EcucContainerValue container : rteContainer.getContainers()) {
        if (container.getDefinition().getShortName().equals("RteSwComponentInstance") &&
            !container.getSubContainers().isEmpty()) {
          List<EcucContainerValue> collect = container.getSubContainers().stream()
              .filter(sub -> (sub.getReferenceValues() != null) && isOstaskRefPresent(sub))
              .collect(Collectors.toList());

          if (!collect.isEmpty()) {
            for (EcucContainerValue subcon : collect) {
              EList<EcucAbstractReferenceValue> referenceValues = subcon.getReferenceValues();
              EcucAbstractReferenceValue ostaskRefAbstract =
                  referenceValues.stream().filter(ref -> (ref instanceof EcucReferenceValue) &&
                      ref.getDefinition().getShortName().equals("RteMappedToTaskRef")).findFirst().orElse(null);

              EcucAbstractReferenceValue eventRefAbstact =
                  referenceValues.stream().filter(ref -> (ref instanceof EcucReferenceValue) &&
                      ref.getDefinition().getShortName().equals("RteEventRef")).findFirst().orElse(null);
              if ((ostaskRefAbstract != null) && (eventRefAbstact != null)) {
                EcucReferenceValue eventRef = (EcucReferenceValue) eventRefAbstact;
                RTEEvent event = (RTEEvent) eventRef.getValue();
                EcucReferenceValue osTaskRef = (EcucReferenceValue) ostaskRefAbstract;
                List<RTEEvent> list = osTaskToEventMap.get(GenerateArxmlUtil.getFragmentURI(osTaskRef));
                if (list == null) {
                  List<RTEEvent> list1 = new ArrayList<>();
                  list1.add(event);
                  osTaskToEventMap.put(GenerateArxmlUtil.getFragmentURI(osTaskRef), list1);
                }
                else if (list.stream().noneMatch(e -> e.getShortName().equals(event.getShortName()))) {
                  list.add(event);
                }
              }
            }
          }
        }
      }
    }
    return osTaskToEventMap;
  }

  /**
   * @param sub
   * @return
   */
  private boolean isOstaskRefPresent(final EcucContainerValue sub) {

    if (!sub.getReferenceValues().isEmpty()) {
      EList<EcucAbstractReferenceValue> referenceValues = sub.getReferenceValues();
      if (!referenceValues.isEmpty()) {
        return referenceValues.stream().anyMatch(ref -> (ref instanceof EcucReferenceValue) &&
            ref.getDefinition().getShortName().equals("RteMappedToTaskRef"));
      }
    }
    return false;
  }

  /**
   * @param osTaskToEventMap
   * @throws Exception
   */
  private void validateEcucValueTriggers(final Map<String, List<RTEEvent>> osTaskToEventMap) throws Exception {
    for (Entry<String, List<RTEEvent>> entry : osTaskToEventMap.entrySet()) {
      Map<String, List<RTEEvent>> triggerToEventMap = new HashMap<>();
      for (RTEEvent event : entry.getValue()) {
        if (event instanceof ExternalTriggerOccurredEvent) {
          ExternalTriggerOccurredEvent etoEvent = (ExternalTriggerOccurredEvent) event;
          Trigger trigger = etoEvent.getTrigger().getTargetTrigger();

          List<RTEEvent> list = triggerToEventMap.get(trigger.getShortName());
          if (list != null) {
            list.add(event);
          }
          else {
            List<RTEEvent> list1 = new ArrayList<>();
            list1.add(event);
            triggerToEventMap.put(trigger.getShortName(), list1);
          }

          AbstractRequiredPortPrototype contextRPort = etoEvent.getTrigger().getContextRPort();
          if ((contextRPort != null) && !contextRPort.eIsProxy() && !isConnectedToPTriggerPort(contextRPort)) {
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("274_0",
                GenerateArxmlUtil.getFragmentURI(contextRPort), event.getShortName()));
          }
        }
      }
    }
  }

  /**
   * @param contextRPort
   */
  private boolean isConnectedToPTriggerPort(final AbstractRequiredPortPrototype contextRPort) {
    return this.triggerConnList.stream()
        .anyMatch(conn -> (this.aswcToPortsMap.get(conn.getShortName()).get(1) != null) &&
            this.aswcToPortsMap.get(conn.getShortName()).get(1).equals(GenerateArxmlUtil.getFragmentURI(contextRPort)));
  }

  /**
   * @throws Exception
   */
  private void validateOASETriggers() throws Exception {
    List<TischedTask> filteredTasks = this.etoTaskList.stream().filter(
        task -> task.getEvents().stream().anyMatch(event -> event.getEvent() instanceof ExternalTriggerOccurredEvent))
        .collect(Collectors.toList());
    for (TischedTask task : filteredTasks) {
      Map<String, List<TischedEvent>> triggerToEventMap = new HashMap<>();
      Map<String, Set<String>> triggerToTriggerURIMap = new HashMap<>();
      for (TischedEvent tischedEvent : task.getEvents()) {
        if (tischedEvent.getEvent() instanceof ExternalTriggerOccurredEvent) {
          ExternalTriggerOccurredEvent etoEvent = (ExternalTriggerOccurredEvent) tischedEvent.getEvent();
          Trigger trigger = etoEvent.getTrigger().getTargetTrigger();

          getTriggerToEventMap(tischedEvent, trigger, triggerToEventMap);
          getTriggerToTriggerURIMap(trigger, triggerToTriggerURIMap);
        }
      }


      String pPortURI = task.getOsPPortPrototype();

      if (pPortURI == null) {
        validateOldOASETriggers(task);
      }
      else {
        List<Entry<String, PortPrototype>> collect = this.uriToPortMap.entrySet().stream()
            .filter(uri -> uri.getKey().equals(pPortURI)).collect(Collectors.toList());
        if ((collect != null) && !collect.isEmpty() && (collect.size() == 1) &&
            (collect.get(0).getValue() instanceof AbstractProvidedPortPrototype)) {
          AbstractProvidedPortPrototype pPort = (AbstractProvidedPortPrototype) collect.get(0).getValue();
          PortInterface pPortIface = null;
          if (pPort instanceof PPortPrototype) {
            pPortIface = ((PPortPrototype) pPort).getProvidedInterface();
          }
          else if (pPort instanceof PRPortPrototype) {
            pPortIface = ((PRPortPrototype) pPort).getProvidedRequiredInterface();
          }

          if ((pPortIface != null) && (pPortIface instanceof TriggerInterface)) {
            TriggerInterface pTrigIFace = (TriggerInterface) pPortIface;
            if (pTrigIFace.getTriggers().size() == 1) {
              Trigger pTrigger = pTrigIFace.getTriggers().get(0);
              if ((triggerToEventMap.size() == 1) && (triggerToEventMap.get(pTrigger.getShortName()) == null)) {

                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("272_0", task.getShortName(),
                    task.getShortName()));
              }
              else if (triggerToEventMap.size() > 1) {

                if (!triggerToEventMap.containsKey(pTrigger.getShortName()) &&
                    !isTriggerMappingPresent(triggerToEventMap.keySet(), GenerateArxmlUtil.getFragmentURI(pTrigger))) {

                  LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("272_1", task.getShortName(),
                      task.getShortName()));
                }
                else {

                  StringBuilder eventlist = new StringBuilder();
                  triggerToEventMap.entrySet().forEach(entry -> {

                    if (!pTrigger.getShortName().equals(entry.getKey()) &&
                        !isTriggerMappingPresent(triggerToTriggerURIMap.get(entry.getKey()),
                            GenerateArxmlUtil.getFragmentURI(pTrigger))) {
                      entry.getValue().stream().forEach(e -> eventlist.append(e.getShortName() + ","));
                    }
                  });


                  if (!eventlist.toString().isEmpty()) {
                    LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("272_2", task.getShortName(),
                        eventlist.toString(), task.getShortName()));
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
   * @param trigger
   * @param triggerToTriggerURIMap2
   * @return
   */
  private void getTriggerToTriggerURIMap(final Trigger trigger, final Map<String, Set<String>> triggerToTriggerURIMap) {


    Set<String> uriSet = triggerToTriggerURIMap.get(trigger.getShortName());
    if (uriSet != null) {
      uriSet.add(GenerateArxmlUtil.getFragmentURI(trigger));
    }
    else {
      Set<String> uriSet1 = new HashSet<>();
      uriSet1.add(GenerateArxmlUtil.getFragmentURI(trigger));
      triggerToTriggerURIMap.put(trigger.getShortName(), uriSet1);
    }
  }


  /**
   * @param trigger
   * @param tischedEvent
   * @param triggerToEventMap
   * @return
   */
  private void getTriggerToEventMap(final TischedEvent tischedEvent, final Trigger trigger,
      final Map<String, List<TischedEvent>> triggerToEventMap) {
    List<TischedEvent> list = triggerToEventMap.get(trigger.getShortName());
    if (list != null) {
      list.add(tischedEvent);
    }
    else {
      List<TischedEvent> list1 = new ArrayList<>();
      list1.add(tischedEvent);
      triggerToEventMap.put(trigger.getShortName(), list1);
    }
  }


  /**
   * @param triggers
   * @param pTriggerURI
   * @return
   */
  private boolean isTriggerMappingPresent(final Set<String> triggers, final String pTriggerURI) {
    boolean isMappingPresent = false;

    for (String rTriggerURI : triggers) {
      isMappingPresent = isMappingPresent(rTriggerURI, pTriggerURI) == null ? false : true;
    }
    return isMappingPresent;
  }


  /**
   * @param rTriggerURI
   * @param pTriggerURI
   * @return
   */
  private String isMappingPresent(final String rTriggerURI, final String pTriggerURI) {


    for (Entry<String, List<TriggerMapping>> entry : this.triggerMappings.entrySet()) {
      List<TriggerMapping> mapList = entry.getValue();

      for (TriggerMapping map : mapList) {
        if (((GenerateArxmlUtil.getFragmentURI(map.getFirstTrigger()).equals(rTriggerURI) &&
            GenerateArxmlUtil.getFragmentURI(map.getSecondTrigger()).equals(pTriggerURI))) ||
            (GenerateArxmlUtil.getFragmentURI(map.getSecondTrigger()).equals(rTriggerURI) &&
                GenerateArxmlUtil.getFragmentURI(map.getFirstTrigger()).equals(pTriggerURI))) {
          return entry.getKey();

        }

      }

    }
    return null;
  }


  /**
   * @throws Exception
   */
  public void validateTriggerConnections() throws Exception {

    Map<String, TischedTask> eventToTaskMap = new HashMap<String, TischedTask>();
    Map<String, RTEEvent> runnableToEventMap = new HashMap<String, RTEEvent>();
    Map<String, ExternalTriggerOccurredEvent> rPortToEventMap = new HashMap<>();
    this.osConfigToEcucValueMapping.getTischedTasks().stream().forEach(t -> t.getEvents().stream().forEach(event -> {
      if (event.getEvent() instanceof RTEEvent) {
        RTEEvent rteEvent = (RTEEvent) event.getEvent();
        if ((rteEvent != null) && !rteEvent.eIsProxy()) {
          eventToTaskMap.put(GenerateArxmlUtil.getFragmentURI(rteEvent), t);
          if (event.getEvent() instanceof ExternalTriggerOccurredEvent) {
            ExternalTriggerOccurredEvent extevent = (ExternalTriggerOccurredEvent) rteEvent;
            if ((extevent.getTrigger() != null) && !extevent.getTrigger().eIsProxy()) {
              AbstractRequiredPortPrototype contextRPort = extevent.getTrigger().getContextRPort();
              rPortToEventMap.put(GenerateArxmlUtil.getFragmentURI(contextRPort), extevent);
            }
          }
        }
      }
    }));

    List<RTEEvent> rteEventList = GenerateArxmlUtil.getListOfEObject(this.project, RTEEvent.class, this.excludeDir);
    rteEventList.stream().forEach(event -> {
      if (!event.eIsProxy()) {
        RunnableEntity startonEvent = event.getStartOnEvent();
        if ((startonEvent != null) && !startonEvent.eIsProxy()) {
          runnableToEventMap.put(GenerateArxmlUtil.getFragmentURI(startonEvent), event);
        }
      }
    });


    List<ExternalTriggeringPoint> etpList =
        GenerateArxmlUtil.getListOfEObject(this.project, ExternalTriggeringPoint.class, this.excludeDir);
    Map<String, List<ExternalTriggeringPoint>> swcToETPMap = new HashMap<String, List<ExternalTriggeringPoint>>();
    Map<String, ExternalTriggeringPoint> portToETPMap = new HashMap<String, ExternalTriggeringPoint>();
    etpList.stream().forEach(etp -> generateRESWCPPortsMap(etp, swcToETPMap, portToETPMap));
    List<String> triggerwarningList = new ArrayList<String>();
    List<String> rPortList = new ArrayList<>();
    for (AssemblySwConnector triggeraswc : this.triggerConnList) {
      List<String> portList = this.aswcToPortsMap.get(triggeraswc.getShortName());
      if ((portList != null) && (portList.size() == 2)) {
        String rPortURI = portList.get(1);
        rPortList.add(rPortURI);
        String pPortURI = portList.get(0);
        PortPrototype pPort = this.uriToPortMap.get(portList.get(0));
        PortPrototype rPort = this.uriToPortMap.get(portList.get(1));
        if ((pPort != null) && (rPort != null) && !pPort.eIsProxy() && !rPort.eIsProxy()) {
          boolean bvalidateTriggers = validateTriggers(pPort, rPort);
          if (!bvalidateTriggers) {
            bvalidateTriggers = isPRTriggersMapped(pPort, rPort) == null ? false : true;
          }


          if (!bvalidateTriggers) {
            String warning = RteConfGenMessageDescription.getFormattedMesssage("273_2", triggeraswc.getShortName());
            if (!triggerwarningList.contains(warning)) {
              triggerwarningList.add(warning);
            }
            continue;

          }
          ExternalTriggeringPoint etp = portToETPMap.get(pPortURI);
          if (etp != null) {
            List<ExternalTriggeringPoint> etpList1 = swcToETPMap.get(pPort.getSwComponentType().getShortName());
            if ((etpList1 != null)) {
              if (etpList1.stream().noneMatch(e -> e.equals(etp))) {
                String warning = RteConfGenMessageDescription.getFormattedMesssage("273_3",
                    pPort.getSwComponentType().getShortName(), pPort.getShortName());
                if (!triggerwarningList.contains(warning)) {
                  triggerwarningList.add(warning);
                }
              }
              else {
                RTEEvent rteEvent = runnableToEventMap.get(GenerateArxmlUtil.getFragmentURI(etp.getRunnableEntity()));
                if ((rteEvent != null)) {
                  if (eventToTaskMap.get(GenerateArxmlUtil.getFragmentURI(rteEvent)) == null) {
                    String warning = RteConfGenMessageDescription.getFormattedMesssage("273_5",
                        pPort.getSwComponentType().getShortName(), pPort.getShortName(),
                        pPort.getSwComponentType().getShortName(), rteEvent.getShortName());
                    if (!triggerwarningList.contains(warning)) {
                      triggerwarningList.add(warning);
                    }
                  }
                }
                else {
                  String warning = RteConfGenMessageDescription.getFormattedMesssage("273_6",
                      pPort.getSwComponentType().getShortName(), pPort.getShortName(),
                      pPort.getSwComponentType().getShortName(), etp.getRunnableEntity().getShortName());
                  if (!triggerwarningList.contains(warning)) {
                    triggerwarningList.add(warning);
                  }
                }
              }
            }
          }
        }
      }
    }
    triggerwarningList.stream().forEach(warn -> LOGGER.warn(warn));
    checkIfRPortIsMapped(rPortList, rPortToEventMap);


  }

  /**
   * @param task
   * @throws Exception
   */
  private void validateOldOASETriggers(final TischedTask task) throws Exception {


    Map<String, List<String>> newASWCToPortsMap = new HashMap<>();
    for (TischedEvent tischedEvent : task.getEvents()) {
      if (tischedEvent.getEvent() instanceof ExternalTriggerOccurredEvent) {
        ExternalTriggerOccurredEvent etoEvent = (ExternalTriggerOccurredEvent) tischedEvent.getEvent();
        RTriggerInAtomicSwcInstanceRef triggerRef = etoEvent.getTrigger();
        AbstractRequiredPortPrototype contextRPort = triggerRef.getContextRPort();
        if (contextRPort != null) {
          String rPortURI = GenerateArxmlUtil.getFragmentURI(contextRPort);
          this.aswcToPortsMap.entrySet().forEach(entry -> {
            if (entry.getValue().get(1).equals(rPortURI)) {
              newASWCToPortsMap.putIfAbsent(entry.getKey(), entry.getValue());
            }
          });
        }
      }
    }

    Map<String, List<String>> pPortURIToASWCMap = new HashMap<>();
    if (newASWCToPortsMap.isEmpty()) {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("274_1", task.getShortName()));
    }
    else if (newASWCToPortsMap.size() > 1) {
      for (Entry<String, List<String>> entry : newASWCToPortsMap.entrySet()) {
        String pPortURI = entry.getValue().get(0);
        List<String> list = pPortURIToASWCMap.get(pPortURI);
        if (list != null) {
          list.add(entry.getKey());
        }
        else {
          List<String> list1 = new ArrayList<>();
          list1.add(entry.getKey());
          pPortURIToASWCMap.put(pPortURI, list1);
        }
      }
    }

    if ((pPortURIToASWCMap.size() > 1) && !isTriggerMappingPresent(pPortURIToASWCMap)) {

      if (!this.btriggerErrorAsWarning) {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("315_0", task.getShortName()));
      }
    }
  }


  /**
   * @param pPortURIToASWCMap
   * @return
   */
  private boolean isTriggerMappingPresent(final Map<String, List<String>> pPortURIToASWCMap) {
    boolean isValid = false;
    List<Entry<String, List<String>>> collect = pPortURIToASWCMap.entrySet().stream()
        .filter(entry -> !isMappingPresent(entry.getValue())).collect(Collectors.toList());
    if (!collect.isEmpty() && (collect.size() == 1)) {
      String pURI = collect.get(0).getKey();
      PortPrototype pPort = this.uriToPortMap.get(pURI);
      EList<Trigger> pTriggers = null;
      if (pPort != null) {
        TriggerInterface pIFace = getTriggerIFaceFromPort(pPort);
        if (pIFace != null) {
          pTriggers = pIFace.getTriggers();
        }
      }
      for (Entry<String, List<String>> entry : pPortURIToASWCMap.entrySet()) {
        if (!entry.getKey().equals(pURI)) {
          for (String aswcName : entry.getValue()) {
            List<String> list = this.aswcToPortsMap.get(aswcName);
            if (list != null) {
              String rURI = list.get(1);
              PortPrototype rPort = this.uriToPortMap.get(rURI);
              EList<Trigger> rTriggers = null;
              if (pPort != null) {
                TriggerInterface rIFace = getTriggerIFaceFromPort(rPort);
                if (rIFace != null) {
                  rTriggers = rIFace.getTriggers();
                }
              }
              if ((pTriggers != null) && (rTriggers != null) && (pTriggers.size() == rTriggers.size()) &&
                  GenerateArxmlUtil.isTriggersSame(rTriggers, rTriggers)) {
                isValid = true;
              }
            }
          }

        }
      }
    }
    return isValid;
  }


  /**
   * @param value
   * @return
   */
  private boolean isMappingPresent(final List<String> aswcList) {
    List<SwConnector> swconList = this.rootSwCompositionPrototype.getSoftwareComposition().getConnectors();
    boolean bMapping = false;
    for (String aswcName : aswcList) {
      List<SwConnector> collect =
          swconList.stream().filter(c -> c.getShortName().equals(aswcName)).collect(Collectors.toList());
      if (!collect.isEmpty() && !collect.get(0).eIsProxy() && (collect.get(0) instanceof AssemblySwConnector)) {
        AssemblySwConnector aswc = (AssemblySwConnector) collect.get(0);
        if ((aswc.getProvider() != null) && (aswc.getRequester() != null) && !aswc.getProvider().eIsProxy() &&
            !aswc.getRequester().eIsProxy() &&
            (isPRTriggersMapped(aswc.getProvider().getTargetPPort(), aswc.getRequester().getTargetRPort()) != null)) {
          bMapping = true;
        }
        else {
          bMapping = false;
        }
      }
      else {
        bMapping = false;
      }
    }
    return bMapping;
  }


  /**
   * @param rPortList
   * @param rPortToEventMap
   */
  private void checkIfRPortIsMapped(final List<String> rPortList,
      final Map<String, ExternalTriggerOccurredEvent> rPortToEventMap) {
    Set<String> rPorts = rPortToEventMap.keySet();
    rPorts.stream().forEach(port -> {
      if (!rPortList.contains(port)) {
        ExternalTriggerOccurredEvent event = rPortToEventMap.get(port);
        try {
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("274_0", port, event.getShortName()));
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }


  /**
   * @param etp
   * @param swcToETPMap
   * @param portToETPMap
   * @return
   */
  private void generateRESWCPPortsMap(final ExternalTriggeringPoint etp,
      final Map<String, List<ExternalTriggeringPoint>> swcToETPMap,
      final Map<String, ExternalTriggeringPoint> portToETPMap) {
    if (etp.getRunnableEntity() != null) {
      AtomicSwComponentType swcType =
          (AtomicSwComponentType) etp.getRunnableEntity().getSwcInternalBehavior().eContainer();

      List<ExternalTriggeringPoint> list = swcToETPMap.get(swcType.getShortName());
      if (list != null) {
        list.add(etp);
      }
      else {
        List<ExternalTriggeringPoint> list1 = new ArrayList<ExternalTriggeringPoint>();
        list1.add(etp);
        swcToETPMap.put(swcType.getShortName(), list1);
      }
    }

    if (etp.getTrigger() != null) {
      AbstractProvidedPortPrototype contextPPort = etp.getTrigger().getContextPPort();
      String portkey = GenerateArxmlUtil.getFragmentURI(contextPPort);
      if (portToETPMap.get(portkey) == null) {
        portToETPMap.put(portkey, etp);
      }
    }
  }

  /**
   * @param aswc
   * @return
   */
  private boolean isValidTriggerConnection(final AssemblySwConnector aswc) {
    List<String> portList = this.aswcToPortsMap.get(aswc.getShortName());
    if ((portList != null) && (portList.size() == 2)) {
      return isTriggerInterface(this.uriToPortMap.get(portList.get(0)), this.uriToPortMap.get(portList.get(1)));
    }
    return false;
  }

  /**
   * @param portPrototype
   * @param portPrototype2
   */
  private boolean isTriggerInterface(final PortPrototype portPrototype, final PortPrototype portPrototype2) {
    PortInterface pPortIface = null;
    PortInterface rPortIface = null;
    if ((portPrototype != null) && (portPrototype2 != null) && !portPrototype.eIsProxy() &&
        !portPrototype2.eIsProxy()) {
      if ((portPrototype2 instanceof PRPortPrototype) && (portPrototype instanceof PPortPrototype)) {
        pPortIface = ((PPortPrototype) portPrototype).getProvidedInterface();
        rPortIface = ((PRPortPrototype) portPrototype2).getProvidedRequiredInterface();
      }
      else if ((portPrototype instanceof PRPortPrototype) && (portPrototype2 instanceof RPortPrototype)) {
        pPortIface = ((PRPortPrototype) portPrototype).getProvidedRequiredInterface();
        rPortIface = ((RPortPrototype) portPrototype2).getRequiredInterface();
      }
      else if ((portPrototype2 instanceof RPortPrototype) && (portPrototype instanceof PPortPrototype)) {
        pPortIface = ((PPortPrototype) portPrototype).getProvidedInterface();
        rPortIface = ((RPortPrototype) portPrototype2).getRequiredInterface();
      }
      if ((pPortIface != null) && (rPortIface != null) && (pPortIface instanceof TriggerInterface) &&
          (rPortIface instanceof TriggerInterface)) {
        return true;
      }
    }
    return false;
  }
}
