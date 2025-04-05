/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.ecuextract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.bosch.tisched.rteconfig.generator.core.OsConfigToEcucValueMapping;
import com.bosch.tisched.rteconfig.generator.rips.GenerateRteTimedEcucValue;
import com.bosch.tisched.rteconfig.generator.util.AssemblySwConnectionUtil;
import com.bosch.tisched.rteconfig.generator.util.AutosarUtil;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;
import com.bosch.tisched.rteconfig.generator.util.VariationPointUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import autosar40.autosartoplevelstructure.AUTOSAR;
import autosar40.autosartoplevelstructure.AutosartoplevelstructureFactory;
import autosar40.commonstructure.auxillaryobjects.SwAddrMethod;
import autosar40.commonstructure.datadefproperties.SwDataDefProps;
import autosar40.commonstructure.datadefproperties.SwDataDefPropsConditional;
import autosar40.commonstructure.flatmap.FlatInstanceDescriptor;
import autosar40.commonstructure.flatmap.FlatMap;
import autosar40.commonstructure.flatmap.FlatmapFactory;
import autosar40.commonstructure.flatmap.RtePluginProps;
import autosar40.commonstructure.modedeclaration.ModeDeclaration;
import autosar40.commonstructure.triggerdeclaration.Trigger;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.genericstructure.abstractstructure.AtpFeature;
import autosar40.genericstructure.generaltemplateclasses.admindata.AdminData;
import autosar40.genericstructure.generaltemplateclasses.anyinstanceref.AnyInstanceRef;
import autosar40.genericstructure.generaltemplateclasses.anyinstanceref.AnyinstancerefFactory;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ArpackageFactory;
import autosar40.genericstructure.generaltemplateclasses.arpackage.PackageableElement;
import autosar40.genericstructure.generaltemplateclasses.identifiable.IdentifiablePackage;
import autosar40.genericstructure.generaltemplateclasses.multidimensionaltime.MultidimensionalTime;
import autosar40.genericstructure.generaltemplateclasses.specialdata.Sd;
import autosar40.genericstructure.generaltemplateclasses.specialdata.Sdg;
import autosar40.genericstructure.generaltemplateclasses.specialdata.SdgContents;
import autosar40.genericstructure.varianthandling.VariationPoint;
import autosar40.swcomponent.components.AbstractProvidedPortPrototype;
import autosar40.swcomponent.components.AbstractRequiredPortPrototype;
import autosar40.swcomponent.components.ApplicationSwComponentType;
import autosar40.swcomponent.components.AtomicSwComponentType;
import autosar40.swcomponent.components.PPortPrototype;
import autosar40.swcomponent.components.PRPortPrototype;
import autosar40.swcomponent.components.PortPrototype;
import autosar40.swcomponent.components.RPortPrototype;
import autosar40.swcomponent.components.SwComponentType;
import autosar40.swcomponent.composition.AssemblySwConnector;
import autosar40.swcomponent.composition.CompositionFactory;
// import autosar40.swcomponent.composition.AssemblySwConnector;
import autosar40.swcomponent.composition.CompositionSwComponentType;
import autosar40.swcomponent.composition.DelegationSwConnector;
import autosar40.swcomponent.composition.SwComponentPrototype;
import autosar40.swcomponent.composition.SwConnector;
import autosar40.swcomponent.composition.instancerefs.InstancerefsFactory;
import autosar40.swcomponent.composition.instancerefs.PPortInCompositionInstanceRef;
import autosar40.swcomponent.composition.instancerefs.PortInCompositionTypeInstanceRef;
import autosar40.swcomponent.composition.instancerefs.RPortInCompositionInstanceRef;
import autosar40.swcomponent.datatype.dataprototypes.AutosarDataPrototype;
import autosar40.swcomponent.datatype.dataprototypes.DataPrototype;
import autosar40.swcomponent.datatype.dataprototypes.VariableDataPrototype;
import autosar40.swcomponent.datatype.datatypes.ApplicationDataType;
import autosar40.swcomponent.portinterface.ClientServerInterface;
import autosar40.swcomponent.portinterface.ClientServerOperation;
import autosar40.swcomponent.portinterface.DataPrototypeMapping;
import autosar40.swcomponent.portinterface.ModeSwitchInterface;
import autosar40.swcomponent.portinterface.NvDataInterface;
import autosar40.swcomponent.portinterface.PortInterface;
import autosar40.swcomponent.portinterface.PortInterfaceMappingSet;
import autosar40.swcomponent.portinterface.SenderReceiverInterface;
import autosar40.swcomponent.portinterface.TriggerInterface;
import autosar40.swcomponent.portinterface.VariableAndParameterInterfaceMapping;
import autosar40.swcomponent.swcimplementation.SwcImplementation;
import autosar40.swcomponent.swcinternalbehavior.RunnableEntity;
import autosar40.swcomponent.swcinternalbehavior.SwcInternalBehavior;
import autosar40.swcomponent.swcinternalbehavior.dataelements.VariableAccess;
import autosar40.system.RootSwCompositionPrototype;
import autosar40.system.SystemFactory;
import autosar40.system.SystemMapping;
import autosar40.system.fibex.fibexcore.coretopology.EcuInstance;
import autosar40.system.instancerefs.ComponentInSystemInstanceRef;
import autosar40.system.swmapping.SwcToEcuMapping;
import autosar40.system.swmapping.SwcToImplMapping;
import autosar40.system.swmapping.SwmappingFactory;
import autosar40.util.Autosar40Factory;
import autosar40.util.Autosar40ReleaseDescriptor;

/**
 * @author shk1cob
 */
public class UpdateExcuExtractFiles {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(UpdateExcuExtractFiles.class.getName());
  private final IProject project;

  private final String autosarReleaseVersion;
  private final String autosarResourceVersion;
  private String swComponentToBeReferred = "";
  private String excludeDir = "";
  private final String flatviewfilepath;
  private final String flatmappath;
  private final String ecuextractpath;
  private final String systemExtractPath;
  private String ecuInstancePath = "";
  private Set<Object> timedComVDP;
  private RootSwCompositionPrototype rootSwCompositionPrototype = null;
  private String listofEcuExtractFiles = "";
  private Map<SwComponentPrototype, Map<String, List<VariableAccess>>> swcpTimedVariableAccessMap;
  private List<RunnableEntity> timedRunnables;
  private final Map<String, SwComponentPrototype> internalbehaviourSwCompMap;
  private final GenerateRteTimedEcucValue createRTEtimedCOMValues;
  private EcucModuleConfigurationValues timedcomECUCConfigValue;
  private final OsConfigToEcucValueMapping osConfigInstance;


  private final List<FlatInstanceDescriptor> listOfFlatInstanceDescriptors = new ArrayList<>();

  private final List<AssemblySwConnector> listOfAssemblySwConnectors = new ArrayList<>();

  private IResource flatMapFile;
  private String rteconfgenlogdir;
  private final String bswaddremethodref;
  private final String rteconfgendir;
  private IResource findMember;
  private AUTOSAR autosarRoot;
  private ARPackage flatViewArPackage;
  private CompositionSwComponentType flatView;
  private final Map<String, String> ascNameMap = new HashMap<String, String>();
  private final String rteconfgenTACsv;
  private final String rteconfgenTALog;
  private final String rteconfgenTAJson;
  private final boolean generate_sys_extract;


  /**
   * @param osConfigToEcucValueMapping
   * @param createRteTimedEcucValue
   * @param project IProject
   * @param map Map<String, String>
   */
  public UpdateExcuExtractFiles(final OsConfigToEcucValueMapping osConfigToEcucValueMapping,
      final GenerateRteTimedEcucValue createRteTimedEcucValue, final IProject project, final Map<String, String> map) {
    this.project = project;
    this.createRTEtimedCOMValues = createRteTimedEcucValue;

    this.autosarReleaseVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION);
    this.autosarResourceVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION);
    this.swComponentToBeReferred = map.get(RteConfigGeneratorConstants.ECU_EXTRACT_REFER_COMPOSITION_SW_COMPONENT_TYPE);
    this.generate_sys_extract =
        map.get(RteConfigGeneratorConstants.RTECONFGEN_GENERATE_SYSTEM_EXTRACT).equalsIgnoreCase("y") ? true : false;
    this.excludeDir = map.get(RteConfigGeneratorConstants.EXCLUDE_ARXML_PATH);
    this.flatviewfilepath = map.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_VIEW_FILE_OUTPUT_PATH);
    this.flatmappath = map.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH);
    this.ecuextractpath = map.get(RteConfigGeneratorConstants.ECU_EXTRACT_FILE_OUTPUT_PATH);
    this.systemExtractPath = map.get(RteConfigGeneratorConstants.SYSTEM_EXTRACT_FILE_OUTPUT_PATH);
    this.ecuInstancePath = map.get(RteConfigGeneratorConstants.ECU_INSTANCE_PKG_PATH);
    this.listofEcuExtractFiles =
        this.project.getLocation().toOSString() + map.get(RteConfigGeneratorConstants.LIST_OF_ECU_EXTRACT_FILES);
    this.internalbehaviourSwCompMap = new HashMap<>();
    this.osConfigInstance = osConfigToEcucValueMapping;

    this.rteconfgenlogdir = map.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG).substring(1,
        map.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG).lastIndexOf("/"));
    this.rteconfgenlogdir = this.rteconfgenlogdir.endsWith("/")
        ? this.rteconfgenlogdir.substring(0, this.rteconfgenlogdir.length() - 1) : this.rteconfgenlogdir;
    this.bswaddremethodref = map.get(RteConfigGeneratorConstants.RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD);
    this.rteconfgendir = map.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR);

    this.rteconfgenTALog = "/" + map.get(RteConfigGeneratorConstants.RTECONFGEN_TA_CONNECTION_LOG_PATH);
    this.rteconfgenTACsv = "/" + map.get(RteConfigGeneratorConstants.RTECONFGEN_TA_CONNECTION_CSV_PATH);
    this.rteconfgenTAJson = "/" + map.get(RteConfigGeneratorConstants.RTECONFGEN_TA_CONNECTION_JSON_PATH);
  }

  /**
   * @return the flatView
   */
  public CompositionSwComponentType getFlatView() {
    return this.flatView;
  }


  /**
   * @param Internal behaviour map has been created to associate the internal behaviours with the swcomponentPrototype.
   *          from SWCInternalbehaviour it is not possible to fetch teh swcomponentPrototype information
   */
  private void updateInternalBehaviourSwCompMap() {
    List<SwComponentPrototype> swCompType =
        GenerateArxmlUtil.getListOfEObject(this.project, SwComponentPrototype.class, "");

    for (SwComponentPrototype com : swCompType) {

      AutosarUtil.setCurrentProcessingEObject(com);

      if (com.getType() instanceof ApplicationSwComponentType) {
        ApplicationSwComponentType asct = (ApplicationSwComponentType) com.getType();
        for (SwcInternalBehavior behv : asct.getInternalBehaviors()) {

          this.internalbehaviourSwCompMap.put(behv.getShortName(), com);

        }
      }
    }
  }

  /**
   * @param Internal behaviour is not considered from the above (internalbehaviourSwCompMap) because in case of splitted
   *          internalbehaviour all the internal behaviours will not be associated with the swcomponentprototype. Hence
   *          separately all the behaviours were fetched from the project and furthur implementation done.
   * @return
   * @throws Exception
   */
  private Set<Object> getTimedComVDPSet(final boolean checkTimedComAffected, final List<String> vaList)
      throws Exception {

    Set<Object> timedvdp = new HashSet<>();

    List<SwcInternalBehavior> localSWCDbeh =
        GenerateArxmlUtil.getListOfEObject(this.project, SwcInternalBehavior.class, "");

    if (checkTimedComAffected) {

      for (SwcInternalBehavior behv : localSWCDbeh) {

        AutosarUtil.setCurrentProcessingEObject(behv);

        for (RunnableEntity runnable : behv.getRunnables()) {

          AutosarUtil.setCurrentProcessingEObject(runnable);

          for (VariableAccess rA : runnable.getDataReadAccess()) {

            AutosarUtil.setCurrentProcessingEObject(rA);

            if ((rA.getCategory() != null) && rA.getCategory().equalsIgnoreCase("TIMEDCOM")) {
              getTIMEDCOMDataAccess(timedvdp, getTimedComSWCPvariablesAccessMap(), runnable, behv,
                  getTimedComRunnabeles(), rA,
                  runnable.getShortName() + "_" + RteConfigGeneratorConstants.DATA_READ_ACCESS);
            }
          }
          for (VariableAccess wA : runnable.getDataWriteAccess()) {
            AutosarUtil.setCurrentProcessingEObject(wA);
            if ((wA.getCategory() != null) && wA.getCategory().equalsIgnoreCase("TIMEDCOM")) {
              getTIMEDCOMDataAccess(timedvdp, getTimedComSWCPvariablesAccessMap(), runnable, behv,
                  getTimedComRunnabeles(), wA,
                  runnable.getShortName() + "_" + RteConfigGeneratorConstants.DATA_WRITE_ACCESS);
            }
          }
        }

      }

    }
    else {

      for (SwcInternalBehavior behv : localSWCDbeh) {

        AutosarUtil.setCurrentProcessingEObject(behv);
        {
          for (RunnableEntity runnable : behv.getRunnables()) {
            AutosarUtil.setCurrentProcessingEObject(runnable);
            for (VariableAccess rA : runnable.getDataReadAccess()) {
              AutosarUtil.setCurrentProcessingEObject(rA);

              if (vaList.contains(rA.getShortName() + "_" + runnable.getShortName())) {
                getTIMEDCOMDataAccess(timedvdp, getTimedComSWCPvariablesAccessMap(), runnable, behv,
                    getTimedComRunnabeles(), rA,
                    runnable.getShortName() + "_" + RteConfigGeneratorConstants.DATA_READ_ACCESS);
              }
            }
            for (VariableAccess wA : runnable.getDataWriteAccess()) {
              AutosarUtil.setCurrentProcessingEObject(wA);
              if (vaList.contains(wA.getShortName() + "_" + runnable.getShortName())) {
                getTIMEDCOMDataAccess(timedvdp, getTimedComSWCPvariablesAccessMap(), runnable, behv,
                    getTimedComRunnabeles(), wA,
                    runnable.getShortName() + "_" + RteConfigGeneratorConstants.DATA_WRITE_ACCESS);
              }
            }
          }
        }
      }

    }

    return timedvdp;
  }

  /**
   * @param behv
   * @param runnable
   * @param comVariableAccessMap
   * @param timedvdp
   * @param timedRunnable
   * @param wA
   * @throws Exception
   */
  private void getTIMEDCOMDataAccess(final Set<Object> timedvdp,
      final Map<SwComponentPrototype, Map<String, List<VariableAccess>>> comVariableAccessMap,
      final RunnableEntity runnable, final SwcInternalBehavior behv, final List<RunnableEntity> timedRunnable,
      final VariableAccess wA, final String dataAccess)
      throws Exception {

    SwComponentPrototype swPro = this.internalbehaviourSwCompMap.get(behv.getShortName());
    DataPrototype pro = wA.getAccessedVariable().getAutosarVariable().getTargetDataPrototype();
    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("121_0", runnable.getShortName()).trim());
    timedvdp.add(pro);
    timedRunnable.add(runnable);
    if (swPro != null) {
      addVariableAccessToMap(wA, swPro, comVariableAccessMap, dataAccess);
    }
    else {
      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("122_0", pro.getShortName(), runnable.getShortName()).trim());

    }

  }


  /**
   * @param timedRunnable
   */
  public List<RunnableEntity> getTimedComRunnabeles() {

    if (this.timedRunnables == null) {
      this.timedRunnables = new ArrayList<>();
    }
    return this.timedRunnables;
  }

  /**
   * @param rA
   * @param com
   * @param comVariableAccessMap
   * @param dataRWAccess
   * @param accessType
   * @param varAccessTypeMap
   */
  private void addVariableAccessToMap(final VariableAccess rA, final SwComponentPrototype com,
      final Map<SwComponentPrototype, Map<String, List<VariableAccess>>> comVariableAccessMap,
      final String dataRWAccess) {

    if (comVariableAccessMap.containsKey(com)) {
      Map<String, List<VariableAccess>> map = comVariableAccessMap.get(com);
      if (map.containsKey(dataRWAccess)) {
        map.get(dataRWAccess).add(rA);

      }
      else {
        List<VariableAccess> rwist = new ArrayList<>();
        rwist.add(rA);
        map.put(dataRWAccess, rwist);
      }

    }
    else {
      List<VariableAccess> vaAccList = new ArrayList<>();
      vaAccList.add(rA);
      Map<String, List<VariableAccess>> varAccessTypeMap = new HashMap<>();
      varAccessTypeMap.put(dataRWAccess, vaAccList);
      comVariableAccessMap.put(com, varAccessTypeMap);
    }

  }

  /**
   * @return RootSwCompositionPrototype
   */
  public RootSwCompositionPrototype getUpdatedRootSwCompositionPrototype() {
    return this.rootSwCompositionPrototype;
  }


  private IResource getResource(final String filePath) throws CoreException {

    String path = filePath.substring(0, filePath.lastIndexOf("/"));
    String name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
    IResource findMember = UpdateExcuExtractFiles.this.project.findMember(path);

    if (findMember instanceof IContainer) {
      IContainer dir = (IContainer) findMember;
      for (IResource iResource : dir.members()) {

        if (iResource.getName().equalsIgnoreCase(name)) {
          return iResource;
        }
      }
    }


    return null;

  }


  private WorkspaceJob generateFile(final String job, final String pkgPath, final String containerPath,
      final BufferedWriter bw, final EObject... eObject) {

    WorkspaceJob workspaceJob = new WorkspaceJob(job) {

      @Override
      public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {

        LOGGER.info("*** Generating " + job + " file");

        String conPath = containerPath.substring(0, containerPath.lastIndexOf("/"));
        String fileName = containerPath.substring(containerPath.lastIndexOf("/") + 1, containerPath.length());

        IResource findMember = UpdateExcuExtractFiles.this.project.findMember(conPath);
        URI resourceURI = null;
        if (findMember != null) {
          resourceURI = URI.createPlatformResourceURI(findMember.getFullPath().toOSString() + "/" + fileName, false);
        }
        {
          resourceURI = URI.createPlatformResourceURI(
              URI.createFileURI(UpdateExcuExtractFiles.this.project.getFullPath().toOSString() + conPath).path() + "/" +
                  fileName,
              false);
        }

        // autosar instance
        final AUTOSAR arPartRoot = AutosartoplevelstructureFactory.eINSTANCE.createAUTOSAR();


        if (eObject.length == 1) {
          ARPackage arArPackage = GenerateArxmlUtil.getARArPackage(arPartRoot, pkgPath);
          arArPackage.getElements().add((PackageableElement) eObject[0]);
        }
        else if (eObject[1] instanceof AUTOSAR) {
          AUTOSAR autosarRt = (AUTOSAR) eObject[1];
          ARPackage arArPackage = GenerateArxmlUtil.getExistingARArPackage(autosarRt, pkgPath);
          arArPackage.getElements().add((PackageableElement) eObject[0]);
          arPartRoot.getArPackages().addAll(autosarRt.getArPackages());

        }
        else if (eObject[1] instanceof ARPackage) {
          ARPackage arArPackage = GenerateArxmlUtil.getARArPackage(arPartRoot, pkgPath);
          arArPackage.getElements().add((PackageableElement) eObject[0]);
          arPartRoot.getArPackages().get(0).getArPackages().add((ARPackage) eObject[1]);
        }
        IStatus saveFile = GenerateArxmlUtil.saveFile(UpdateExcuExtractFiles.this.project, arPartRoot, resourceURI,
            AutosarUtil.getMetaModelDescriptorByAutosarResoureVersion(
                AutosarUtil
                    .getMetaModelDescriptorByAutosarReleaseVersion(UpdateExcuExtractFiles.this.autosarReleaseVersion),
                UpdateExcuExtractFiles.this.autosarResourceVersion));

        if (saveFile.isOK()) {
          try {

            LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("150_0", job,
                URI.createFileURI(UpdateExcuExtractFiles.this.project.getFullPath().toOSString() + conPath).path(),
                fileName).trim());

            findMember = UpdateExcuExtractFiles.this.project.findMember(conPath);
            if (findMember != null) {
              if (!fileName.equals("RTEConfGen_SystemExtract.arxml")) {
                bw.write(findMember.getProjectRelativePath().toOSString() + "\\" + fileName);
                bw.newLine();
                bw.flush();

                if (containerPath.equals(UpdateExcuExtractFiles.this.flatviewfilepath) && findMember.exists()) {
                  UpdateExcuExtractFiles.this.flatMapFile = findMember;
                }
              }

            }
          }
          catch (Exception e) {

            try {

              LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
              RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                  .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
            }
            catch (Exception e1) {
              e1.printStackTrace();
            }
          }
        }
        else {
          RteConfigGeneratorLogger.logErrormessage(LOGGER, "*** Generating " + job + " file is failed");
        }
        return Status.OK_STATUS;
      }
    };

    workspaceJob.setRule(this.project);
    workspaceJob.schedule();
    workspaceJob.getResult();
    return workspaceJob;
  }


  private autosar40.system.System generateSystemExtractFile(final SystemMapping originalSystemMapping,
      final CompositionSwComponentType mmdcsSwComponentType,
      final RootSwCompositionPrototype originalRootSwCompPrototype)
      throws Exception {
    AutosarUtil.setCurrentProcessingEObject(originalSystemMapping);
    autosar40.system.System sys = SystemFactory.eINSTANCE.createSystem();// EcoreUtil.copy(originalSystemMapping.getSystem());
    sys.setShortName(originalSystemMapping.getSystem().getShortName());
    sys.setCategory("SYSTEM_EXTRACT");
    SystemMapping createSystemMapping = SystemFactory.eINSTANCE.createSystemMapping();
    createSystemMapping.setShortName(originalSystemMapping.getSystem().getMappings().get(0).getShortName());
    sys.getMappings().add(createSystemMapping);
    TransactionalEditingDomain arEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(originalSystemMapping.eResource());
    AbstractEMFOperation abstractEmfOperation =
        new AbstractEMFOperation(arEditingDomain, "Updating System extract file") {

          @Override
          protected IStatus doExecute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
            try {
              SwcToEcuMapping createSwcToEcuMapping = SwmappingFactory.eINSTANCE.createSwcToEcuMapping();
              Map<String, SwcToEcuMapping> swComponentPrototypeToEcuMap =
                  getSwComponentPrototypeEcuMap(originalSystemMapping.getSwMappings());
              EcuInstance ecuInstance = null;
              if (!swComponentPrototypeToEcuMap.isEmpty()) {
                SwcToEcuMapping swctoecumap = (SwcToEcuMapping) swComponentPrototypeToEcuMap.values().toArray()[0];
                ecuInstance = swctoecumap.getEcuInstance();
              }
              AutosarUtil.setCurrentProcessingEObject(originalRootSwCompPrototype);
              Map<String, SwComponentPrototype> swCompositionComponentMap =
                  getSwCompositionComponentMap(originalRootSwCompPrototype.getSoftwareComposition().getComponents());
              boolean createMapping = false;
              for (SwComponentPrototype swcp1 : mmdcsSwComponentType.getComponents()) {
                AutosarUtil.setCurrentProcessingEObject(swcp1);
                SwComponentPrototype swcp = swCompositionComponentMap.get(swcp1.getShortName());
                if (swComponentPrototypeToEcuMap.get(swcp.getShortName()) == null) {
                  ComponentInSystemInstanceRef createComponentInSystemInstanceRef =
                      autosar40.system.instancerefs.InstancerefsFactory.eINSTANCE.createComponentInSystemInstanceRef();
                  createComponentInSystemInstanceRef.setContextComposition(originalRootSwCompPrototype);
                  createComponentInSystemInstanceRef.setTargetComponent(swcp);
                  VariationPoint variationPoint =
                      VariationPointUtil.getInstance().getVariationPoint(originalRootSwCompPrototype, swcp);
                  if (variationPoint != null) {
                    createSwcToEcuMapping.setVariationPoint(variationPoint);
                  }
                  createSwcToEcuMapping.getComponents().add(createComponentInSystemInstanceRef);
                  createMapping = true;
                }
                else {
                  LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("110_0", swcp.getShortName(),
                      swComponentPrototypeToEcuMap.get(swcp.getShortName()).getShortName()).trim());
                }
              }
              if (createMapping) {
                String name = UpdateExcuExtractFiles.this.swComponentToBeReferred.substring(
                    UpdateExcuExtractFiles.this.swComponentToBeReferred.lastIndexOf("/") + 1,
                    UpdateExcuExtractFiles.this.swComponentToBeReferred.length());
                String shortName = name;
                shortName = GenerateArxmlUtil.getShortenedNameOfElements(shortName,
                    Collections.unmodifiableMap(swComponentPrototypeToEcuMap));
                createSwcToEcuMapping.setShortName("SwcToEcuMapping_" + shortName);
                if (ecuInstance != null) {
                  createSwcToEcuMapping.setEcuInstance(ecuInstance);
                }
                originalSystemMapping.getSwMappings().add(createSwcToEcuMapping);
                createSystemMapping.getSwMappings().add(EcoreUtil.copy(createSwcToEcuMapping));
              }
              AutosarUtil.setCurrentProcessingEObject(originalSystemMapping);
              Map<String, SwcToImplMapping> swComponentPrototypeToImplMap =
                  getSwComponentPrototypeImplMap(originalSystemMapping.getSwImplMappings());
              List<SwcImplementation> listOfSwcs = GenerateArxmlUtil.getListOfEObject(
                  UpdateExcuExtractFiles.this.project, SwcImplementation.class, UpdateExcuExtractFiles.this.excludeDir);
              Map<SwcInternalBehavior, SwcImplementation> swcImplementationIBMap =
                  getSwcImplementationIBMap(listOfSwcs);
              for (SwComponentPrototype swcp1 : mmdcsSwComponentType.getComponents()) {
                AutosarUtil.setCurrentProcessingEObject(swcp1);
                SwComponentPrototype swcp = swCompositionComponentMap.get(swcp1.getShortName());
                if ((swComponentPrototypeToImplMap.get(swcp.getShortName()) == null)) {
                  if ((swcp.getType() != null) && !swcp.getType().eIsProxy()) {
                    AtomicSwComponentType aswc = (AtomicSwComponentType) swcp.getType();
                    EList<SwcInternalBehavior> listOfIBs = aswc.getInternalBehaviors();
                    if (!listOfIBs.isEmpty() && (listOfIBs.size() == 1)) {
                      if (swcImplementationIBMap.get(listOfIBs.get(0)) != null) {
                        SwcToImplMapping createSwcToImplMapping = SwmappingFactory.eINSTANCE.createSwcToImplMapping();
                        ComponentInSystemInstanceRef createComponentInSystemInstanceRef =
                            autosar40.system.instancerefs.InstancerefsFactory.eINSTANCE
                                .createComponentInSystemInstanceRef();
                        createComponentInSystemInstanceRef.setContextComposition(originalRootSwCompPrototype);
                        createComponentInSystemInstanceRef.setTargetComponent(swcp);
                        createSwcToImplMapping.getComponents().add(createComponentInSystemInstanceRef);
                        String shortName = aswc.getShortName();
                        shortName =
                            GenerateArxmlUtil.getShortenedNameOfElements(shortName, swComponentPrototypeToImplMap);
                        createSwcToImplMapping.setShortName("SwcToImplMapping_" + shortName);
                        createSwcToImplMapping.setComponentImplementation(swcImplementationIBMap.get(listOfIBs.get(0)));
                        VariationPoint variationPoint =
                            VariationPointUtil.getInstance().getVariationPoint(originalRootSwCompPrototype, swcp);
                        if (variationPoint != null) {
                          createSwcToImplMapping.setVariationPoint(variationPoint);
                        }
                        originalSystemMapping.getSwImplMappings().add(createSwcToImplMapping);
                        createSystemMapping.getSwImplMappings().add(EcoreUtil.copy(createSwcToImplMapping));
                      }
                      else {
                        LOGGER.warn(
                            RteConfGenMessageDescription.getFormattedMesssage("238_1", swcp.getShortName()).trim());
                      }
                    }
                  }
                  else {
                    LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("238_0", swcp.getShortName()).trim());
                  }
                }
                else {
                  LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("110_1", swcp.getShortName(),
                      swComponentPrototypeToImplMap.get(swcp.getShortName()).getShortName()).trim());
                }
              }
            }
            catch (Exception ex) {
              try {
                LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                    .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());
              }
              catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
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
    return sys;
  }

  /**
   * @param aswcToSwcpMap
   * @param pPortToRPortsMap
   * @param rPortToPPortsMap
   * @throws Exception
   */
  public void UpdateExcuExtractFiles(final AssemblySwConnectionUtil assemblySwConnUtil) throws Exception {


    try (BufferedWriter bw =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.listofEcuExtractFiles))))) {

      LOGGER.info("*** Updating Ecu Extract files");

      List<CompositionSwComponentType> mmdcsCompositions = GenerateArxmlUtil.getListOfEObject(this.project,
          this.swComponentToBeReferred, CompositionSwComponentType.class, this.excludeDir);

      if ((mmdcsCompositions != null) && !mmdcsCompositions.isEmpty()) {

        if (mmdcsCompositions.size() > 1) {
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("221_0", this.swComponentToBeReferred).trim());

        }

        List<EcuInstance> ecuInstances =
            GenerateArxmlUtil.getListOfEObject(this.project, this.ecuInstancePath, EcuInstance.class, this.excludeDir);

        if ((ecuInstances != null) && !ecuInstances.isEmpty()) {

          if (ecuInstances.size() > 1) {
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("222_0", this.ecuInstancePath).trim());

          }

          RootSwCompositionPrototype originalRootSwCompPrototype =
              GenerateArxmlUtil.getRootSwCompositionPrototype(ecuInstances.get(0), this.project, this.excludeDir);


          if (originalRootSwCompPrototype != null) {

            AutosarUtil.setCurrentProcessingEObject(originalRootSwCompPrototype);

            if ((originalRootSwCompPrototype.getSoftwareComposition() != null)) {

              Map<String, SwComponentPrototype> originalSwCompPrototypeMaps =
                  getSwComponentPrototypeMap(originalRootSwCompPrototype.getSoftwareComposition(), true);

              Map<String, SwComponentPrototype> mmdcsSwComponentPrototypeMaps =
                  getSwComponentPrototypeMap(mmdcsCompositions.get(0), false);

              Map<String, SwConnector> orginalSwConnectorMaps =
                  getSwConnectorMap(originalRootSwCompPrototype.getSoftwareComposition());

              // autosar instance
              final AUTOSAR autosarRoot = AutosartoplevelstructureFactory.eINSTANCE.createAUTOSAR();

              CompositionSwComponentType flatviewTobeSaved =
                  updateEcuFlatViewFile(mmdcsCompositions.get(0), originalRootSwCompPrototype,
                      originalSwCompPrototypeMaps, orginalSwConnectorMaps, autosarRoot, assemblySwConnUtil);


              String packagePath =
                  GenerateArxmlUtil.getPackagePath(originalRootSwCompPrototype.getSoftwareComposition());

              WorkspaceJob generateFile =
                  generateFile("Flat view", packagePath.substring(0, packagePath.lastIndexOf("/")),
                      this.flatviewfilepath, bw, flatviewTobeSaved, autosarRoot);


              generateFile.join();

              FlatMap originalFlatmap = originalRootSwCompPrototype.getFlatMap();

              if (originalFlatmap != null) {

                AutosarUtil.setCurrentProcessingEObject(originalFlatmap);

                Map<String, FlatInstanceDescriptor> originalFDMap = getFlatInstanceDescriptorMap(originalFlatmap);

                updateInternalBehaviourSwCompMap();

                // timedcom affected runnables will be updated in the list
                final List<String> vaList = new ArrayList<String>();
                this.timedComVDP = getTimedComVDPSet(true, vaList);

                if (!this.timedComVDP.isEmpty()) {
                  if (this.osConfigInstance.checkIfRteRipsPluginImplTimedComExist(this.rootSwCompositionPrototype, true,
                      vaList)) {
                    this.createRTEtimedCOMValues.getRteTimedRunnable().addAll(getTimedComRunnabeles());
                    this.timedcomECUCConfigValue = this.createRTEtimedCOMValues.generateRteRipsEcuCcontainerValue();
                  }
                }
                else {

                  if (this.osConfigInstance.checkIfRteRipsPluginImplTimedComExist(this.rootSwCompositionPrototype,
                      false, vaList)) {
                    this.timedComVDP = getTimedComVDPSet(false, vaList);
                    this.createRTEtimedCOMValues.getRteTimedRunnable().addAll(getTimedComRunnabeles());
                    this.timedcomECUCConfigValue = this.createRTEtimedCOMValues.generateRteRipsEcuCcontainerValue();
                  }

                }
                ARPackage swAddrMethodArPackage = null;
                if (this.bswaddremethodref.equals("y")) {
                  swAddrMethodArPackage = ArpackageFactory.eINSTANCE.createARPackage();
                  swAddrMethodArPackage.setShortName("SwAddrMethods");
                }
                FlatMap flatMaptoBeSaved =
                    updateEcuFlatMapFile(originalFlatmap, originalRootSwCompPrototype, mmdcsCompositions.get(0),
                        originalFDMap, mmdcsSwComponentPrototypeMaps, swAddrMethodArPackage, assemblySwConnUtil);

                LOGGER.info(RteConfGenMessageDescription
                    .getFormattedMesssage("108_0", GenerateArxmlUtil.getPackagePath(originalFlatmap)).trim());
                assemblySwConnUtil.clearPRMaps();


                packagePath = GenerateArxmlUtil.getPackagePath(originalFlatmap);

                if (swAddrMethodArPackage == null) {
                  generateFile = generateFile("Flat map", packagePath.substring(0, packagePath.lastIndexOf("/")),
                      this.flatmappath, bw, flatMaptoBeSaved);
                }
                else {
                  generateFile = generateFile("Flat map", packagePath.substring(0, packagePath.lastIndexOf("/")),
                      this.flatmappath, bw, flatMaptoBeSaved, swAddrMethodArPackage);
                }

                generateFile.join();


                List<SystemMapping> originalSystemMappings = originalRootSwCompPrototype.getSystem().getMappings();


                if ((originalSystemMappings != null) && !originalSystemMappings.isEmpty()) {

                  if (originalSystemMappings.size() > 1) {
                    LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("223_0",
                        GenerateArxmlUtil.getPackagePath(originalRootSwCompPrototype.getSystem()),
                        Integer.toString(originalSystemMappings.size()),
                        GenerateArxmlUtil.getPackagePath(originalSystemMappings.get(0))).trim());

                  }

                  LOGGER.info(RteConfGenMessageDescription
                      .getFormattedMesssage("109_0", GenerateArxmlUtil.getPackagePath(originalSystemMappings.get(0)))
                      .trim());


                  autosar40.system.System systemToBeSaved = updateEcuExtractFile(originalSystemMappings.get(0),
                      mmdcsCompositions.get(0), originalRootSwCompPrototype);

                  packagePath = GenerateArxmlUtil.getPackagePath(originalSystemMappings.get(0).getSystem());

                  generateFile = generateFile("Ecu extract", packagePath.substring(0, packagePath.lastIndexOf("/")),
                      this.ecuextractpath, bw, systemToBeSaved);
                  generateFile.join();

                  if (this.generate_sys_extract) {
                    RootSwCompositionPrototype ogRootswcp2 = GenerateArxmlUtil
                        .getRootSwCompositionPrototype2(ecuInstances.get(0), this.project, this.excludeDir);
                    if (ogRootswcp2 != null) {
                      List<SystemMapping> ogsm2 = ogRootswcp2.getSystem().getMappings();
                      autosar40.system.System systemextractToBeSaved = generateSystemExtractFile(ogsm2.get(0),
                          mmdcsCompositions.get(0), originalRootSwCompPrototype);
                      packagePath = GenerateArxmlUtil.getPackagePath(ogsm2.get(0).getSystem());
                      generateFile =
                          generateFile("system extract", packagePath.substring(0, packagePath.lastIndexOf("/")),
                              this.systemExtractPath, bw, systemextractToBeSaved);
                      generateFile.join();
                    }
                    else {
                      LOGGER.warn(RteConfGenMessageDescription
                          .getFormattedMesssage("226_0", GenerateArxmlUtil.getPackagePath(ecuInstances.get(0))).trim());
                    }

                  }

                  String devicepath = getDevicepath(originalSystemMappings.get(0).eResource());
                  if (!Arrays.asList(this.excludeDir.split(","))
                      .contains(getDevicepath(originalSystemMappings.get(0).eResource()))) {
                    this.excludeDir = this.excludeDir + "," + devicepath;
                  }

                }
                else {
                  LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("224_0",
                      GenerateArxmlUtil.getPackagePath(originalRootSwCompPrototype.getSystem())).trim());
                }

                String devicepath = getDevicepath(originalFlatmap.eResource());
                if (!Arrays.asList(this.excludeDir.split(",")).contains(getDevicepath(originalFlatmap.eResource()))) {
                  this.excludeDir = this.excludeDir + "," + devicepath;
                }

              }
              else {
                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("225_0").trim());

              }

              String devicepath = getDevicepath(originalRootSwCompPrototype.getSoftwareComposition().eResource());
              if (!Arrays.asList(this.excludeDir.split(",")).contains(devicepath)) {
                this.excludeDir = this.excludeDir + "," + devicepath;
              }


            }

            this.rootSwCompositionPrototype = originalRootSwCompPrototype;


            LOGGER.info("*** Updating Ecu Extract files done");

          }
          else {
            LOGGER.warn(RteConfGenMessageDescription
                .getFormattedMesssage("226_0", GenerateArxmlUtil.getPackagePath(ecuInstances.get(0))).trim());

          }


        }
        else {
          LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("227_0", this.ecuInstancePath).trim());
        }


      }
      else {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("228_0", this.swComponentToBeReferred).trim());
      }

    }
    catch (Exception e) {
      LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());

    }

  }


  private String getDevicepath(final Resource resource) {

    String devicePath = "";
    if (resource != null) {
      devicePath = resource.getURI().devicePath();
      devicePath = devicePath.substring(0, devicePath.lastIndexOf("/"));
    }
    return devicePath;

  }

  private autosar40.system.System updateEcuExtractFile(final SystemMapping originalSystemMapping,
      final CompositionSwComponentType mmdcsSwComponentType,
      final RootSwCompositionPrototype originalRootSwCompPrototype)
      throws Exception {

    AutosarUtil.setCurrentProcessingEObject(originalSystemMapping);

    // LOGGER.info("*** Updating Ecu extract file");
    autosar40.system.System sys = SystemFactory.eINSTANCE.createSystem();// EcoreUtil.copy(originalSystemMapping.getSystem());
    sys.setShortName(originalSystemMapping.getSystem().getShortName());
    sys.setCategory("ECU_EXTRACT");

    SystemMapping createSystemMapping = SystemFactory.eINSTANCE.createSystemMapping();
    createSystemMapping.setShortName(originalSystemMapping.getSystem().getMappings().get(0).getShortName());
    sys.getMappings().add(createSystemMapping);

    TransactionalEditingDomain arEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(originalSystemMapping.eResource());
    AbstractEMFOperation abstractEmfOperation = new AbstractEMFOperation(arEditingDomain, "Updating Ecu extractfile") {


      @Override
      protected IStatus doExecute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {


        try {
          SwcToEcuMapping createSwcToEcuMapping = SwmappingFactory.eINSTANCE.createSwcToEcuMapping();

          Map<String, SwcToEcuMapping> swComponentPrototypeToEcuMap =
              getSwComponentPrototypeEcuMap(originalSystemMapping.getSwMappings());

          EcuInstance ecuInstance = null;
          if (!swComponentPrototypeToEcuMap.isEmpty()) {
            SwcToEcuMapping swctoecumap = (SwcToEcuMapping) swComponentPrototypeToEcuMap.values().toArray()[0];

            ecuInstance = swctoecumap.getEcuInstance();
          }

          AutosarUtil.setCurrentProcessingEObject(originalRootSwCompPrototype);

          Map<String, SwComponentPrototype> swCompositionComponentMap =
              getSwCompositionComponentMap(originalRootSwCompPrototype.getSoftwareComposition().getComponents());

          boolean createMapping = false;
          for (SwComponentPrototype swcp1 : mmdcsSwComponentType.getComponents()) {

            AutosarUtil.setCurrentProcessingEObject(swcp1);

            SwComponentPrototype swcp = swCompositionComponentMap.get(swcp1.getShortName());

            if (swComponentPrototypeToEcuMap.get(swcp.getShortName()) == null) {

              ComponentInSystemInstanceRef createComponentInSystemInstanceRef =
                  autosar40.system.instancerefs.InstancerefsFactory.eINSTANCE.createComponentInSystemInstanceRef();
              createComponentInSystemInstanceRef.setContextComposition(originalRootSwCompPrototype);
              createComponentInSystemInstanceRef.setTargetComponent(swcp);

              VariationPoint variationPoint =
                  VariationPointUtil.getInstance().getVariationPoint(originalRootSwCompPrototype, swcp);

              if (variationPoint != null) {
                createSwcToEcuMapping.setVariationPoint(variationPoint);
              }

              createSwcToEcuMapping.getComponents().add(createComponentInSystemInstanceRef);
              createMapping = true;
            }
            else {
              LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("110_0", swcp.getShortName(),
                  swComponentPrototypeToEcuMap.get(swcp.getShortName()).getShortName()).trim());

            }

          }


          if (createMapping) {

            String name = UpdateExcuExtractFiles.this.swComponentToBeReferred.substring(
                UpdateExcuExtractFiles.this.swComponentToBeReferred.lastIndexOf("/") + 1,
                UpdateExcuExtractFiles.this.swComponentToBeReferred.length());
            String shortName = name;
            shortName = GenerateArxmlUtil.getShortenedNameOfElements(shortName,
                Collections.unmodifiableMap(swComponentPrototypeToEcuMap));
            createSwcToEcuMapping.setShortName("SwcToEcuMapping_" + shortName);
            if (ecuInstance != null) {
              createSwcToEcuMapping.setEcuInstance(ecuInstance);
            }
            originalSystemMapping.getSwMappings().add(createSwcToEcuMapping);
            createSystemMapping.getSwMappings().add(EcoreUtil.copy(createSwcToEcuMapping));
          }


          AutosarUtil.setCurrentProcessingEObject(originalSystemMapping);

          Map<String, SwcToImplMapping> swComponentPrototypeToImplMap =
              getSwComponentPrototypeImplMap(originalSystemMapping.getSwImplMappings());

          List<SwcImplementation> listOfSwcs = GenerateArxmlUtil.getListOfEObject(UpdateExcuExtractFiles.this.project,
              SwcImplementation.class, UpdateExcuExtractFiles.this.excludeDir);

          Map<SwcInternalBehavior, SwcImplementation> swcImplementationIBMap = getSwcImplementationIBMap(listOfSwcs);

          for (SwComponentPrototype swcp1 : mmdcsSwComponentType.getComponents()) {

            AutosarUtil.setCurrentProcessingEObject(swcp1);

            SwComponentPrototype swcp = swCompositionComponentMap.get(swcp1.getShortName());

            if ((swComponentPrototypeToImplMap.get(swcp.getShortName()) == null)) {

              if ((swcp.getType() != null) && !swcp.getType().eIsProxy()) {
                AtomicSwComponentType aswc = (AtomicSwComponentType) swcp.getType();
                EList<SwcInternalBehavior> listOfIBs = aswc.getInternalBehaviors();
                if (!listOfIBs.isEmpty() && (listOfIBs.size() == 1)) {
                  if (swcImplementationIBMap.get(listOfIBs.get(0)) != null) {

                    SwcToImplMapping createSwcToImplMapping = SwmappingFactory.eINSTANCE.createSwcToImplMapping();
                    ComponentInSystemInstanceRef createComponentInSystemInstanceRef =
                        autosar40.system.instancerefs.InstancerefsFactory.eINSTANCE
                            .createComponentInSystemInstanceRef();
                    createComponentInSystemInstanceRef.setContextComposition(originalRootSwCompPrototype);
                    createComponentInSystemInstanceRef.setTargetComponent(swcp);
                    createSwcToImplMapping.getComponents().add(createComponentInSystemInstanceRef);


                    String shortName = aswc.getShortName();

                    shortName = GenerateArxmlUtil.getShortenedNameOfElements(shortName, swComponentPrototypeToImplMap);

                    createSwcToImplMapping.setShortName("SwcToImplMapping_" + shortName);

                    createSwcToImplMapping.setComponentImplementation(swcImplementationIBMap.get(listOfIBs.get(0)));


                    VariationPoint variationPoint =
                        VariationPointUtil.getInstance().getVariationPoint(originalRootSwCompPrototype, swcp);

                    if (variationPoint != null) {
                      createSwcToImplMapping.setVariationPoint(variationPoint);
                    }

                    originalSystemMapping.getSwImplMappings().add(createSwcToImplMapping);
                    createSystemMapping.getSwImplMappings().add(EcoreUtil.copy(createSwcToImplMapping));
                  }
                  else {
                    LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("238_1", swcp.getShortName()).trim());
                  }
                }
              }
              else {
                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("238_0", swcp.getShortName()).trim());
              }


            }
            else {
              LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("110_1", swcp.getShortName(),
                  swComponentPrototypeToImplMap.get(swcp.getShortName()).getShortName()).trim());

            }
          }
        }
        catch (Exception ex) {
          try {
            LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
            RteConfigGeneratorLogger.logErrormessage(LOGGER,
                RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());
          }
          catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

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

    return sys;

  }


  private Map<SwcInternalBehavior, SwcImplementation> getSwcImplementationIBMap(final List<SwcImplementation> swcs) {
    Map<SwcInternalBehavior, SwcImplementation> map = new HashMap<>();
    for (SwcImplementation swc : swcs) {
      AutosarUtil.setCurrentProcessingEObject(swc);
      if (swc.getBehavior() != null) {
        map.put(swc.getBehavior(), swc);
      }
    }
    return map;

  }

  private Map<String, List<String>> getAdminData(final PortPrototype portP) throws Exception {
    Map<String, List<String>> map = new HashMap<>();
    List<String> portlist = new ArrayList<>();
    AdminData adminData = portP.getAdminData();
    if (adminData != null) {

      EList<Sdg> sdgs = adminData.getSdgs();
      boolean autoConnection = false;
      boolean dedicatedconnection = false;
      List<String> message = null;
      if ((sdgs.size() == 1) && "RTEConfGen".equals(sdgs.get(0).getGid())) {

        if ((sdgs.get(0).getSdgContentsType() != null)) {
          if ((sdgs.get(0).getSdgContentsType().getSds() != null) &&
              !sdgs.get(0).getSdgContentsType().getSds().isEmpty()) {

            message = new ArrayList<>(Arrays.asList(sdgs.get(0).getSdgContentsType().getSds().get(0).getValue()));
            map.put("Message", message);
          }
          if ((sdgs.get(0).getSdgContentsType().getSdgs() != null) &&
              (!sdgs.get(0).getSdgContentsType().getSdgs().isEmpty())) {
            EList<Sdg> sdgs2 = sdgs.get(0).getSdgContentsType().getSdgs();
            for (Sdg sdg : sdgs2) {
              if ("AutoConnection".equals(sdg.getGid())) {
                autoConnection = true;
                if ((sdg.getSdgContentsType() != null) && (sdg.getSdgContentsType().getSdgs() != null) &&
                    (sdg.getSdgContentsType().getSdgs().size() > 0)) {
                  EList<Sdg> sdgsList = sdg.getSdgContentsType().getSdgs();
                  for (Sdg sdg2 : sdgsList) {
                    if ((sdg2.getSdgContentsType() != null) && (sdg2.getSdgContentsType().getSds() != null) &&
                        (!sdg2.getSdgContentsType().getSds().isEmpty())) {
                      if ("IgnorePort".equals(sdg2.getGid())) {
                        EList<Sd> sds = sdg2.getSdgContentsType().getSds();
                        if (sds.size() == 2) {
                          portlist.add(sds.get(0).getValue() + "," + sds.get(1).getValue());
                        }
                      }
                      else {
                        LOGGER.warn(
                            RteConfGenMessageDescription.getFormattedMesssage("260_3", portP.getShortName()).trim());
                      }
                    }
                  }
                }
              }
              else if ("DedicatedConnection".equals(sdg.getGid())) {
                dedicatedconnection = true;

                if ((sdg.getSdgContentsType() != null) && (sdg.getSdgContentsType().getSdgs() != null) &&
                    (sdg.getSdgContentsType().getSdgs().size() > 0)) {
                  EList<Sdg> sdgsList = sdg.getSdgContentsType().getSdgs();
                  for (Sdg sdg2 : sdgsList) {

                    if ((sdg2.getSdgContentsType() != null) && (sdg2.getSdgContentsType().getSds() != null) &&
                        (!sdg2.getSdgContentsType().getSds().isEmpty())) {
                      if ("ConsiderPort".equals(sdg2.getGid())) {
                        EList<Sd> sds = sdg2.getSdgContentsType().getSds();
                        if (sds.size() == 2) {
                          portlist.add(sds.get(0).getValue() + "," + sds.get(1).getValue());
                        }
                      }
                      else {
                        LOGGER.warn(
                            RteConfGenMessageDescription.getFormattedMesssage("260_4", portP.getShortName()).trim());
                      }
                    }
                  }
                }
              }
              if (dedicatedconnection) {
                map.put("ConsiderPort", portlist);
              }
              else {
                map.put("IgnorePort", portlist);
              }
            }

          }
        }
      }
    }
    return map;
  }

  private boolean isIgnorePort(final boolean ignoreport, final Map<String, List<String>> portAndCompRef,
      final String portURI, final String swcURI) {

    for (Entry<String, List<String>> entry : portAndCompRef.entrySet()) {
      if ((ignoreport &&
          (entry.getKey().endsWith(swcURI) && entry.getValue().stream().anyMatch(l -> l.endsWith(portURI)))) ||
          (!ignoreport &&
              !(entry.getKey().endsWith(swcURI) && entry.getValue().stream().anyMatch(l -> l.endsWith(portURI))))) {
        return true;
      }
    }

    return false;
  }

  private boolean isConsiderPort(final boolean considerport, final Map<String, List<String>> portAndCompRef,
      final String portURI, final String swcURI) {
    for (Entry<String, List<String>> entry : portAndCompRef.entrySet()) {
      if ((considerport &&
          (entry.getKey().endsWith(swcURI) && entry.getValue().stream().anyMatch(l -> l.endsWith(portURI))))) {
        return true;
      }
    }

    return false;
  }

  private Map<String, String> getPortAndCompRef(final PortPrototype portP, final String value,
      final Map<PortPrototype, List<SwComponentPrototype>> portSWCMap)
      throws Exception {

    Map<String, String> portAndCompRef = new HashMap<>();
    List<SwComponentPrototype> compList = new ArrayList<>();
    String componentRef = "";
    String portRef = "";

    String tempportRef = value.split(",")[1];
    String tempcompRef = value.split(",")[0];

    final String portKey = tempportRef.startsWith("/")
        ? tempportRef.substring(tempportRef.lastIndexOf("/") + 1, tempportRef.length()) : tempportRef;
    final String compKey = tempcompRef.startsWith("/")
        ? tempcompRef.substring(tempcompRef.lastIndexOf("/") + 1, tempcompRef.length()) : tempcompRef;

    boolean bproxyport = false;
    boolean bproxycomp = false;
    boolean bportInSWC = false;

    List<Entry<PortPrototype, List<SwComponentPrototype>>> portList = null;
    if (tempportRef.startsWith("/")) {
      final String portURIKey = tempportRef;
      portList = portSWCMap.entrySet().stream()
          .filter(entry -> GenerateArxmlUtil.getFragmentURI(entry.getKey()).equals(portURIKey))
          .collect(Collectors.toList());
    }
    else {
      portList = portSWCMap.entrySet().stream().filter(entry -> entry.getKey().getShortName().equals(portKey))
          .collect(Collectors.toList());
    }
    portSWCMap.values().stream().forEach(s -> compList.addAll(s));

    if ((portList != null) && !portList.isEmpty()) {
      List<SwComponentPrototype> complist = null;
      if (tempcompRef.startsWith("/")) {
        final String compURIKey = tempcompRef;
        complist = compList.stream().filter(c -> GenerateArxmlUtil.getFragmentURI(c).equals(compURIKey))
            .collect(Collectors.toList());
      }
      else {
        complist = compList.stream().filter(c -> c.getShortName().equals(compKey)).collect(Collectors.toList());
      }
      if ((complist != null) && !complist.isEmpty()) {

        STOP: for (Entry<PortPrototype, List<SwComponentPrototype>> entry : portList) {

          for (SwComponentPrototype port : entry.getValue()) {
            AutosarUtil.setCurrentProcessingEObject(port);
            if (port.getShortName().equals(compKey)) {
              bportInSWC = true;

              portRef = GenerateArxmlUtil.getFragmentURI(entry.getKey());
              componentRef = GenerateArxmlUtil.getFragmentURI(port);
              break STOP;
            }
          }
        }
      }
      else {
        bproxycomp = true;
      }
    }
    else {
      bproxyport = true;
    }


    String warning = bproxyport && bproxycomp
        ? RteConfGenMessageDescription.getFormattedMesssage("260_5", tempportRef, tempcompRef, portP.getShortName())
        : (bproxyport
            ? RteConfGenMessageDescription.getFormattedMesssage("260_6", tempportRef, tempcompRef, portP.getShortName())
            : (bproxycomp
                ? RteConfGenMessageDescription.getFormattedMesssage("260_7", tempcompRef, portP.getShortName())
                : !bportInSWC ? RteConfGenMessageDescription.getFormattedMesssage("260_8", tempportRef,
                    portP.getShortName(), tempportRef) : null));
    if (warning != null) {
      LOGGER.warn(warning);
    }

    if (!(portRef.isEmpty()) && !(componentRef.isEmpty())) {
      portAndCompRef.put(componentRef, portRef);
    }
    return portAndCompRef;
  }

  private CompositionSwComponentType updateEcuFlatViewFile(final CompositionSwComponentType mmdcsCompositionType,
      final RootSwCompositionPrototype originalRootSwCompPrototype,
      final Map<String, SwComponentPrototype> originalSwComponentPrototypeMaps,
      final Map<String, SwConnector> originalSwConnectorMaps, final AUTOSAR autosarRoot,
      final AssemblySwConnectionUtil assemblySwConnUtil)
      throws Exception {


    this.flatView = CompositionFactory.eINSTANCE.createCompositionSwComponentType();
    this.flatView.setShortName(originalRootSwCompPrototype.getSoftwareComposition().getShortName());
    OsConfigToEcucValueMapping osConfigToEcucValueMapping = this.osConfigInstance;

    final TransactionalEditingDomain currentEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(this.project, Autosar40ReleaseDescriptor.INSTANCE);
    ResourceSet resourceSet = currentEditingDomain.getResourceSet();

    List<VariableAndParameterInterfaceMapping> allInstancesOf =
        EObjectUtil.getAllInstancesOf(resourceSet.getResources(), VariableAndParameterInterfaceMapping.class, true);

    Map<String, VariableAndParameterInterfaceMapping> vapimList = new HashMap<>();

    for (VariableAndParameterInterfaceMapping vapim : allInstancesOf) {
      vapimList.put(vapim.getShortName() + "_PIMS_" + vapim.getPortInterfaceMappingSet().getShortName(), vapim);
    }
    TransactionalEditingDomain arEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(originalRootSwCompPrototype.getSoftwareComposition().eResource());

    AbstractEMFOperation abstractEmfOperation =
        new AbstractEMFOperation(arEditingDomain, "Updating Ecu Flatview file") {

          @Override
          protected IStatus doExecute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {


            EList<SwComponentPrototype> components = mmdcsCompositionType.getComponents();

            List<SwComponentPrototype> swcList = new ArrayList<SwComponentPrototype>();

            for (SwComponentPrototype swcComponent : components) {

              AutosarUtil.setCurrentProcessingEObject(swcComponent);

              if ((originalSwComponentPrototypeMaps.get(swcComponent.getShortName()) == null) &&
                  !swcComponent.eIsProxy()) {
                swcList.add(EcoreUtil.copy(swcComponent));
              }
              else {
                try {
                  LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("111_0", swcComponent.getShortName(),
                      originalRootSwCompPrototype.getSoftwareComposition().getShortName()).trim());
                }
                catch (Exception e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }


              }
            }

            AutosarUtil.setCurrentProcessingEObject(originalRootSwCompPrototype);

            Map<String, SwComponentPrototype> olderSwCompositionComponentMap =
                getSwCompositionComponentMap(originalRootSwCompPrototype.getSoftwareComposition().getComponents());

            if (!swcList.isEmpty()) {
              originalRootSwCompPrototype.getSoftwareComposition().getComponents().addAll(swcList);
              UpdateExcuExtractFiles.this.flatView.getComponents().addAll(EcoreUtil.copyAll(swcList));

            }

            Map<String, SwComponentPrototype> swCompositionComponentMap =
                getSwCompositionComponentMap(originalRootSwCompPrototype.getSoftwareComposition().getComponents());

            EList<SwConnector> connectors = mmdcsCompositionType.getConnectors();
            List<SwConnector> swconList = new ArrayList<SwConnector>();
            for (SwConnector asc : connectors) {

              AutosarUtil.setCurrentProcessingEObject(asc);

              if (asc instanceof AssemblySwConnector) {
                if (originalSwConnectorMaps.get(asc.getShortName()) == null) {
                  AssemblySwConnector as = EcoreUtil.copy((AssemblySwConnector) asc);
                  PPortInCompositionInstanceRef provider = as.getProvider();
                  SwComponentPrototype pPortContextComponent = null;
                  SwComponentPrototype rPortContextComponent = null;
                  if (provider != null) {
                    pPortContextComponent = provider.getContextComponent();

                    if ((pPortContextComponent != null) && !pPortContextComponent.eIsProxy()) {
                      SwComponentPrototype swComponentPrototype =
                          swCompositionComponentMap.get(pPortContextComponent.getShortName());
                      if (swComponentPrototype != null) {
                        provider.setContextComponent(swComponentPrototype);
                      }
                    }
                  }


                  RPortInCompositionInstanceRef requester = as.getRequester();
                  if (requester != null) {
                    rPortContextComponent = requester.getContextComponent();
                    if ((rPortContextComponent != null) && !rPortContextComponent.eIsProxy()) {
                      SwComponentPrototype swComponentPrototype =
                          swCompositionComponentMap.get(rPortContextComponent.getShortName());
                      if (swComponentPrototype != null) {
                        requester.setContextComponent(swComponentPrototype);
                      }
                    }
                  }

                  assemblySwConnUtil.updateASWCToSWCPMap(as.getShortName(), provider.getTargetPPort(),
                      requester.getTargetRPort(), pPortContextComponent.getShortName(),
                      rPortContextComponent.getShortName());
                  swconList.add(as);
                }
                else {
                  try {
                    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("112_0", asc.getShortName(),
                        originalRootSwCompPrototype.getSoftwareComposition().getShortName()).trim());
                  }
                  catch (Exception e) {
                    e.printStackTrace();
                  }

                }
              }
            }


            AutosarUtil.setCurrentProcessingEObject(mmdcsCompositionType);

            EList<PortPrototype> ports = mmdcsCompositionType.getPorts();


            Map<String, List<PortPrototype>> vdpPortMap = getVdpPortMap(swCompositionComponentMap, swcList);
            Map<PortPrototype, List<SwComponentPrototype>> portSWCMap = new HashMap<>();
            Map<SwComponentPrototype, List<PortPrototype>> swcToPortsMap =
                getPortSWCMap(swCompositionComponentMap, portSWCMap);
            List<String> messageDetailedLoglist = new ArrayList<>();
            List<String> messageLoglist = new ArrayList<>();
            List<Map<String, String>> messageCSVList = new ArrayList<>();
            for (PortPrototype portP : ports) {
              AutosarUtil.setCurrentProcessingEObject(portP);

              boolean adminDataPresent = false;
              boolean ignoreport = false;
              boolean considerport = false;
              Map<String, List<String>> portAndCompRef = new HashMap<>();
              String message = "";


              Map<String, List<String>> adminDataMap = null;
              try {
                adminDataMap = getAdminData(portP);
              }
              catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
              if ((portP.getAdminData() != null) && adminDataMap.isEmpty()) {
                try {
                  LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("260_0", portP.getShortName()).trim());
                  continue;
                }
                catch (Exception e) {
                  e.printStackTrace();
                }
              }
              else if ((adminDataMap.containsKey("Message") && (adminDataMap.size() > 2)) ||
                  (!adminDataMap.containsKey("Message") && (adminDataMap.size() > 1))) {

                try {
                  LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("260_1", portP.getShortName()).trim());
                  continue;
                }
                catch (Exception e) {
                  e.printStackTrace();
                }

              }
              else if (!adminDataMap.isEmpty() && (adminDataMap.size() <= 2)) {
                adminDataPresent = true;
                String key = adminDataMap.containsKey("IgnorePort") ? "IgnorePort" : "ConsiderPort";
                message = adminDataMap.containsKey("Message") ? adminDataMap.get("Message").get(0) : "";

                ignoreport = (adminDataMap.containsKey("IgnorePort") && !(adminDataMap.get("IgnorePort").isEmpty()))
                    ? true : false;
                considerport = adminDataMap.containsKey("ConsiderPort") ? true : false;

                for (String value : adminDataMap.get(key)) {

                  Map<String, String> portAndCompRef2 = null;
                  try {
                    portAndCompRef2 = getPortAndCompRef(portP, value, portSWCMap);
                  }
                  catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                  }
                  if (portAndCompRef2.isEmpty()) {
                    try {
                      LOGGER.warn(
                          RteConfGenMessageDescription.getFormattedMesssage("260_2", portP.getShortName()).trim());
                      continue;
                    }
                    catch (Exception e) {
                      e.printStackTrace();
                    }
                  }

                  for (Map.Entry<String, String> entry : portAndCompRef2.entrySet()) {
                    List<String> list = portAndCompRef.get(entry.getKey());

                    if (list != null) {
                      list.add(entry.getValue());
                    }
                    else {
                      List<String> l = new ArrayList<>();
                      l.add(entry.getValue());
                      portAndCompRef.put(entry.getKey(), l);
                    }

                  }

                }

              }
              Map<SwComponentPrototype, List<PortPrototype>> portsFromDelegationConnection =
                  getPortsFromDelegationConnection(portP, mmdcsCompositionType);


              createAssemblyConnection(portsFromDelegationConnection, swCompositionComponentMap,
                  olderSwCompositionComponentMap, portAndCompRef, adminDataPresent, ignoreport, considerport, portP,
                  swconList, adminDataMap, mmdcsCompositionType, autosarRoot, vapimList, message,
                  messageDetailedLoglist, messageCSVList, assemblySwConnUtil);


              if ((portP.getAdminData() != null) && !adminDataMap.isEmpty() && (adminDataMap.size() <= 2)) {
                continue;
              }

              List<DelegationSwConnector> delegationConnections = getDelegationConnection(portP, mmdcsCompositionType);

              for (DelegationSwConnector delegationConnection : delegationConnections) {
                AutosarUtil.setCurrentProcessingEObject(delegationConnection);
                if ((delegationConnection.getMapping() != null) &&
                    (delegationConnection.getMapping() instanceof VariableAndParameterInterfaceMapping)) {

                  VariableAndParameterInterfaceMapping vdpMapping =
                      (VariableAndParameterInterfaceMapping) delegationConnection.getMapping();

                  if (!vdpMapping.getDataMappings().isEmpty()) {

                    DataPrototypeMapping dMapping = vdpMapping.getDataMappings().get(0);

                    AutosarDataPrototype firstDataPrototype = dMapping.getFirstDataPrototype();
                    AutosarDataPrototype secondDataPrototype = dMapping.getSecondDataPrototype();

                    if ((firstDataPrototype != null) && (secondDataPrototype != null) &&
                        !firstDataPrototype.eIsProxy() && !secondDataPrototype.eIsProxy() &&
                        !checkIfVDPsAreSame(firstDataPrototype, secondDataPrototype)) {

                      createAssemblyConnection(delegationConnection, vdpPortMap, portSWCMap, swconList, vdpMapping,
                          autosarRoot, vapimList, message, messageDetailedLoglist, messageCSVList, assemblySwConnUtil);
                    }
                  }
                }
              }
            }
            messageCSVList
                .sort(Comparator.comparing(m -> m.get("message"), Comparator.nullsLast(Comparator.naturalOrder())));
            getMessageLogData(messageLoglist, messageDetailedLoglist, messageCSVList);
            try {
              writeMessageToLog(messageLoglist, messageDetailedLoglist);
              writeMessageToCSV(messageCSVList);
              writeMessageToJSON(messageCSVList);
            }
            catch (Exception e) {
              e.printStackTrace();
            }


            if (!swconList.isEmpty()) {
              originalRootSwCompPrototype.getSoftwareComposition().getConnectors().addAll(swconList);
              UpdateExcuExtractFiles.this.flatView.getConnectors().addAll(EcoreUtil.copyAll(swconList));
            }

            UpdateExcuExtractFiles.this.rootSwCompositionPrototype = originalRootSwCompPrototype;

            return Status.OK_STATUS;
          }
        };

    try

    {
      WorkspaceTransactionUtil.getOperationHistory(arEditingDomain).execute(abstractEmfOperation,
          new NullProgressMonitor(), null);
    }
    catch (ExecutionException e) {
      LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());

    }

    for (SwConnector swc : this.flatView.getConnectors()) {
      if (swc instanceof AssemblySwConnector) {
        UpdateExcuExtractFiles.this.listOfAssemblySwConnectors.add((AssemblySwConnector) swc);
      }

    }

    return this.flatView;
  }


  private void writeMessageToLog(final List<String> messageLoglist, final List<String> messageDetailedLoglist)
      throws Exception {
    String logFilePath = this.project.getLocation().toOSString() + this.rteconfgenTALog;
    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(logFilePath))))) {

      bw.write("Connection OverView:\n===================");
      bw.newLine();
      for (String value : messageLoglist) {
        bw.write(value);
        bw.newLine();
      }
      bw.newLine();
      bw.write("Connection Details:\n===================");
      bw.newLine();
      for (String value : messageDetailedLoglist) {
        bw.write(value);
        bw.newLine();
      }
      bw.flush();
    }
    catch (Exception ex) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("314_1").trim());

    }

  }


  private void writeMessageToCSV(final List<Map<String, String>> messageCSVList) throws Exception {
    String csvFilePath = this.project.getLocation().toOSString() + this.rteconfgenTACsv;

    TreeSet<String> csvlines = new TreeSet<>();

    for (Map<String, String> entry : messageCSVList) {
      if (!entry.isEmpty() && entry.get("interface").equals("SR")) {

        String mmdccomponent = entry.get("innercomponent").substring(entry.get("innercomponent").lastIndexOf("/") + 1,
            entry.get("innercomponent").length());
        mmdccomponent = mmdccomponent.split("CPT_")[1];

        if (entry.get("direction").equals("MSR to AR")) {

          csvlines.add(entry.get("message") + "," + mmdccomponent + "," + entry.get("outerport") + "," +
              entry.get("outercomponent"));
        }
        else {
          csvlines.add(entry.get("outerport") + "," + entry.get("outercomponent") + "," + entry.get("message") + "," +
              mmdccomponent);
        }
      }
    }


    FileWriter writer;
    try {
      writer = new FileWriter(csvFilePath);

      try (CSVPrinter csvPrinter = new CSVPrinter(writer,
          CSVFormat.EXCEL.withHeader("exportedPort", "exportedComponent", "importedPort", "importedComponent"));) {

        for (String entry : csvlines) {
          String[] entries = entry.split(",");
          csvPrinter.printRecord(entries[0], entries[1], entries[2], entries[3]);
        }
      }
    }
    catch (IOException e1) {
      e1.printStackTrace();
    }
    catch (Exception ex) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("314_0").trim());

    }

  }


  @SuppressWarnings("unchecked")
  private void writeMessageToJSON(final List<Map<String, String>> messageList) throws IOException {

    String jsonFilePath = this.project.getLocation().toOSString() + this.rteconfgenTAJson;

    TreeSet<String> jsonlines = new TreeSet<>();

    for (Map<String, String> entry : messageList) {
      if (!entry.isEmpty() && entry.get("interface").equals("SR")) {

        String mmdccomponent = entry.get("innercomponent").substring(entry.get("innercomponent").lastIndexOf("/") + 1,
            entry.get("innercomponent").length());
        mmdccomponent = mmdccomponent.split("CPT_")[1];

        if (entry.get("direction").equals("MSR to AR")) {

          jsonlines.add(entry.get("message") + "," + mmdccomponent + "," + entry.get("outerport") + "," +
              entry.get("outercomponent"));
        }
        else {
          jsonlines.add(entry.get("outerport") + "," + entry.get("outercomponent") + "," + entry.get("message") + "," +
              mmdccomponent);
        }
      }
    }


    JSONArray jsonarray = new JSONArray();
    for (String entry : jsonlines) {
      JSONObject connection = new JSONObject();
      String[] entries = entry.split(",");
      JSONObject exported = new JSONObject();
      exported.put("port", entries[0]);
      exported.put("component", entries[1]);
      JSONObject imported = new JSONObject();
      imported.put("port", entries[2]);
      imported.put("component", entries[3]);
      connection.put("exported", exported);
      connection.put("imported", imported);
      jsonarray.add(connection);

    }

    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    Object json = mapper.readValue(jsonarray.toJSONString(), Object.class);
    String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);


    FileWriter writer = null;
    try {
      writer = new FileWriter(jsonFilePath);
      writer.write(indented);
    }
    catch (Exception e) {
      System.out.println(e);
    }
    finally {
      writer.close();
    }
  }

  /**
   * @param messageloglist
   * @param messageDetailedLoglist
   * @param messageCSVList
   */
  protected void getMessageLogData(final List<String> messageLoglist, final List<String> messageDetailedLoglist,
      final List<Map<String, String>> messageCSVList) {
    Map<String, List<Map<String, String>>> messageMap = new HashMap<>();

    for (Map<String, String> e : messageCSVList) {
      List<Map<String, String>> list = messageMap.get(e.get("message"));
      if ((list == null) || list.isEmpty()) {
        list = new ArrayList<>();
        list.add(e);
        messageMap.put(e.get("message"), list);

      }
      else {
        list.add(e);
      }
    }
    TreeMap<String, List<Map<String, String>>> tm = new TreeMap<String, List<Map<String, String>>>(messageMap);
    Map<String, List<Map<String, String>>> exportImportMap = getExportImportMap(messageMap);

    for (String e : tm.keySet()) {

      messageDetailedLoglist.add(System.lineSeparator() + e);
      messageDetailedLoglist.add("---------");

      messageLoglist.add(System.lineSeparator() + e);
      messageLoglist.add("---------");

      List<Entry<String, List<Map<String, String>>>> collect = exportImportMap.entrySet().stream()
          .filter(o -> o.getKey().split("@")[0].equals(e)).collect(Collectors.toList());
      collect.sort(Comparator.comparing(m -> m.getKey(), Comparator.nullsLast(Comparator.naturalOrder())));
      if (!collect.isEmpty()) {
        for (Entry<String, List<Map<String, String>>> c : collect) {
          if (c.getValue().get(0).get("direction").equals("MSR to AR")) {

            messageDetailedLoglist.add("- Exporter (MSR) : " + e + " ( " +
                GenerateArxmlUtil.getShortNameOfURI(c.getValue().get(0).get("innerport")) + " @ " +
                GenerateArxmlUtil.getShortNameOfURI(c.getValue().get(0).get("innercomponent")) + " )");
            messageLoglist.add("- Exporter (MSR) : " + e + " ( " +
                GenerateArxmlUtil.getShortNameOfURI(c.getValue().get(0).get("innerport")) + " @ " +
                GenerateArxmlUtil.getShortNameOfURI(c.getValue().get(0).get("innercomponent")) + " )");

            messageDetailedLoglist.add("  - Port: " + c.getValue().get(0).get("innerport"));
            messageDetailedLoglist.add("  - CPT : " + c.getValue().get(0).get("innercomponent"));
            List<Map<String, String>> importers = c.getValue();
            importers
                .sort(Comparator.comparing(m -> m.get("outerport"), Comparator.nullsLast(Comparator.naturalOrder())));


            for (Map<String, String> importer : importers) {

              messageDetailedLoglist
                  .add("  - Importer (AR) : " + GenerateArxmlUtil.getShortNameOfURI(importer.get("outerport")) + " @ " +
                      GenerateArxmlUtil.getShortNameOfURI(importer.get("outercomponent")));
              messageLoglist
                  .add("  - Importer (AR) : " + GenerateArxmlUtil.getShortNameOfURI(importer.get("outerport")) + " @ " +
                      GenerateArxmlUtil.getShortNameOfURI(importer.get("outercomponent")));
              messageDetailedLoglist.add("      - Port: " + importer.get("outerport"));
              messageDetailedLoglist.add("      - CPT : " + importer.get("outercomponent"));

            }
          }
          else {

            messageDetailedLoglist
                .add("- Exporter (AR) : " + GenerateArxmlUtil.getShortNameOfURI(c.getValue().get(0).get("outerport")) +
                    " @ " + GenerateArxmlUtil.getShortNameOfURI(c.getValue().get(0).get("outercomponent")));
            messageLoglist
                .add("- Exporter (AR) : " + GenerateArxmlUtil.getShortNameOfURI(c.getValue().get(0).get("outerport")) +
                    " @ " + GenerateArxmlUtil.getShortNameOfURI(c.getValue().get(0).get("outercomponent")));

            messageDetailedLoglist.add("  - Port: " + c.getValue().get(0).get("outerport"));
            messageDetailedLoglist.add("  - CPT : " + c.getValue().get(0).get("outercomponent"));
            List<Map<String, String>> importers = c.getValue();
            importers
                .sort(Comparator.comparing(m -> m.get("innerport"), Comparator.nullsLast(Comparator.naturalOrder())));

            for (Map<String, String> importer : importers) {


              messageDetailedLoglist.add(
                  "  - Importer (MSR) : " + e + " ( " + GenerateArxmlUtil.getShortNameOfURI(importer.get("innerport")) +
                      " @ " + GenerateArxmlUtil.getShortNameOfURI(importer.get("innercomponent")) + " )");
              messageLoglist.add(
                  "  - Importer (MSR) : " + e + " ( " + GenerateArxmlUtil.getShortNameOfURI(importer.get("innerport")) +
                      " @ " + GenerateArxmlUtil.getShortNameOfURI(importer.get("innercomponent")) + " )");
              messageDetailedLoglist.add("      - Port: " + importer.get("innerport"));
              messageDetailedLoglist.add("      - CPT : " + importer.get("innercomponent"));

            }
          }
        }
      }
    }
  }

  /**
   * @param messageMap
   * @return
   */
  private Map<String, List<Map<String, String>>> getExportImportMap(
      final Map<String, List<Map<String, String>>> messageMap) {
    Map<String, List<Map<String, String>>> outportMap = new HashMap<>();
    for (Entry<String, List<Map<String, String>>> e : messageMap.entrySet()) {
      for (Map<String, String> s : e.getValue()) {
        if (s.get("direction").equals("MSR to AR")) {

          List<Map<String, String>> list =
              outportMap.get(e.getKey() + "@" + s.get("innerport") + " @ " + s.get("innercomponent"));
          if ((list == null) || list.isEmpty()) {
            list = new ArrayList<>();
            list.add(s);
            outportMap.put(e.getKey() + "@" + s.get("innerport") + " @ " + s.get("innercomponent"), list);
          }
          else {
            list.add(s);
            outportMap.put(e.getKey() + "@" + s.get("innerport") + " @ " + s.get("innercomponent"), list);
          }
        }
        else {
          List<Map<String, String>> list =
              outportMap.get(e.getKey() + "@" + s.get("outerport") + " @ " + s.get("outercomponent"));
          if ((list == null) || list.isEmpty()) {
            list = new ArrayList<>();
            list.add(s);
            outportMap.put(e.getKey() + "@" + s.get("outerport") + " @ " + s.get("outercomponent"), list);
          }
          else {
            list.add(s);
            outportMap.put(e.getKey() + "@" + s.get("outerport") + " @ " + s.get("outercomponent"), list);
          }
        }
      }
    }
    return outportMap;
  }

  private boolean checkIfVDPsAreSame(final AutosarDataPrototype firstDataPrototype,
      final AutosarDataPrototype secondDataPrototype) {


    String dep1ShortName = GenerateArxmlUtil.getShortName(firstDataPrototype);

    String dep2ShortName = GenerateArxmlUtil.getShortName(secondDataPrototype);

    return dep1ShortName.equals(dep2ShortName);

  }

  private boolean checkIfInterfacesAreSame(final AutosarDataPrototype firstDataPrototype,
      final AutosarDataPrototype secondDataPrototype) {

    String if1ShortName = EcoreUtil.getURI(firstDataPrototype.eContainer()).fragment();

    String if2ShortName = EcoreUtil.getURI(secondDataPrototype.eContainer()).fragment();

    return if1ShortName.equals(if2ShortName);

  }

  /**
   * @param outerPort
   * @param shortName
   * @return
   */
  private AutosarDataPrototype getDataPrototype(final PortPrototype portPrototype, final String shortName) {

    PortInterface pi = portPrototype instanceof PPortPrototype ? ((PPortPrototype) portPrototype).getProvidedInterface()
        : ((portPrototype instanceof RPortPrototype ? ((RPortPrototype) portPrototype).getRequiredInterface()
            : (portPrototype instanceof PRPortPrototype
                ? ((PRPortPrototype) portPrototype).getProvidedRequiredInterface() : null)));

    if (pi instanceof SenderReceiverInterface) {
      SenderReceiverInterface si = (SenderReceiverInterface) pi;
      if (!si.getDataElements().isEmpty()) {

        List<AutosarDataPrototype> collector = si.getDataElements().stream()
            .filter(l -> (l.getShortName().equals(shortName))).collect(Collectors.toList());
        if ((collector != null) && !collector.isEmpty() && (collector.size() == 1)) {
          return collector.get(0);
        }
      }
    }

    return null;
  }

  private AutosarDataPrototype getDataPrototype(final List<VariableDataPrototype> externalDataPrototypeList,
      final AutosarDataPrototype outerDataPrototype) {

    if (!externalDataPrototypeList.isEmpty()) {
      List<AutosarDataPrototype> collector = externalDataPrototypeList.stream()
          .filter(l -> (l.getShortName().equals(outerDataPrototype.getShortName()))).collect(Collectors.toList());
      if ((collector != null) && !collector.isEmpty() && (collector.size() == 1)) {
        return collector.get(0);
      }
    }


    return null;


  }


  /**
   * @param rPort
   * @return
   */
  private List<VariableDataPrototype> getDataPrototypeList(final PortPrototype portPrototype) {
    List<VariableDataPrototype> vdpList = new ArrayList<VariableDataPrototype>();

    PortInterface pi = portPrototype instanceof PPortPrototype ? ((PPortPrototype) portPrototype).getProvidedInterface()
        : ((portPrototype instanceof RPortPrototype ? ((RPortPrototype) portPrototype).getRequiredInterface()
            : (portPrototype instanceof PRPortPrototype
                ? ((PRPortPrototype) portPrototype).getProvidedRequiredInterface() : null)));

    if (pi instanceof SenderReceiverInterface) {
      SenderReceiverInterface si = (SenderReceiverInterface) pi;
      if (!si.getDataElements().isEmpty()) {

        vdpList.addAll(si.getDataElements());
      }
    }

    return vdpList;

  }

  private void createAssemblyConnection(final DelegationSwConnector delegationConnection,
      final Map<String, List<PortPrototype>> vdpPortMap,
      final Map<PortPrototype, List<SwComponentPrototype>> portSWCMap, final List<SwConnector> swconList,
      final VariableAndParameterInterfaceMapping vdpMapping, final AUTOSAR autosarRoot,
      final Map<String, VariableAndParameterInterfaceMapping> vapimList, String message,
      final List<String> messageloglist, final List<Map<String, String>> messageCSVList,
      final AssemblySwConnectionUtil assemblySwConnUtil) {


    PortPrototype outerPort = delegationConnection.getOuterPort();

    AutosarDataPrototype outerPortDT =
        getDataPrototype(outerPort, vdpMapping.getDataMappings().get(0).getFirstDataPrototype().getShortName());

    if (outerPortDT != null) {

      if (message.equals("")) {
        message = outerPortDT.getShortName().replace("VDP_", "");
      }

      List<PortPrototype> list = vdpPortMap.get(outerPortDT.getShortName());

      if (delegationConnection.getInnerPort() != null) {


        for (PortPrototype externalPort : list) {
          AutosarUtil.setCurrentProcessingEObject(externalPort);
          if ((outerPort instanceof RPortPrototype) &&
              (((RPortInCompositionInstanceRef) delegationConnection.getInnerPort()).getTargetRPort() != null) &&
              ((externalPort instanceof PPortPrototype) || (externalPort instanceof PRPortPrototype))) {

            AbstractRequiredPortPrototype mmdcsPort =
                ((RPortInCompositionInstanceRef) delegationConnection.getInnerPort()).getTargetRPort();

            if (checkIfCompatiblePortIsValid(externalPort, outerPort, portSWCMap.get(externalPort))) {

              AutosarDataPrototype externalPortDT = getDataPrototype(externalPort, outerPortDT.getShortName());

              boolean isSame = checkIfInterfacesAreSame(outerPortDT, externalPortDT);


              List<SwComponentPrototype> oswcL = portSWCMap.get(mmdcsPort);
              List<SwComponentPrototype> iswcL = portSWCMap.get(externalPort);

              if ((oswcL != null) && (iswcL != null)) {

                for (SwComponentPrototype iswc : iswcL) {
                  AutosarUtil.setCurrentProcessingEObject(iswc);

                  for (SwComponentPrototype oswc : oswcL) {
                    AutosarUtil.setCurrentProcessingEObject(oswc);

                    String shortNameOfASC =
                        getShortNameOfASC("ASC_" + iswc.getShortName() + "_" + externalPort.getShortName() + "_" +
                            oswc.getShortName() + "_" + mmdcsPort.getShortName(), swconList);

                    SwConnector swConnector = null;
                    String ascName = this.ascNameMap.get(shortNameOfASC);
                    if (ascName != null) {
                      swConnector = getSwConnector(swconList, ascName);

                    }
                    else {
                      swConnector = getSwConnector(swconList, shortNameOfASC);
                    }

                    if (swConnector != null) {
                      swConnector.setMapping(vdpMapping);

                    }
                    else {
                      AssemblySwConnector createAssemblySwConnector =
                          CompositionFactory.eINSTANCE.createAssemblySwConnector();

                      PPortInCompositionInstanceRef createPPortInCompositionInstanceRef =
                          InstancerefsFactory.eINSTANCE.createPPortInCompositionInstanceRef();

                      createPPortInCompositionInstanceRef.setTargetPPort((AbstractProvidedPortPrototype) externalPort);
                      createPPortInCompositionInstanceRef.setContextComponent(iswc);

                      VariationPoint variationPoint =
                          VariationPointUtil.getInstance().getVariationPoint(externalPort, iswc);

                      createAssemblySwConnector.setProvider(createPPortInCompositionInstanceRef);

                      RPortInCompositionInstanceRef createRPortInCompositionInstanceRef =
                          InstancerefsFactory.eINSTANCE.createRPortInCompositionInstanceRef();

                      createRPortInCompositionInstanceRef.setTargetRPort(mmdcsPort);
                      createRPortInCompositionInstanceRef.setContextComponent(oswc);

                      createAssemblySwConnector.setRequester(createRPortInCompositionInstanceRef);
                      createAssemblySwConnector.setShortName(shortNameOfASC);

                      if (isSame) {

                        createAssemblySwConnector.setMapping(vdpMapping);
                      }
                      else {
                        createAssemblySwConnector = createVAPIM(createAssemblySwConnector, autosarRoot, externalPortDT,
                            vdpMapping.getDataMappings().get(0).getSecondDataPrototype(), vdpMapping, vapimList);
                      }

                      if (variationPoint != null) {
                        createAssemblySwConnector.setVariationPoint(variationPoint);
                      }
                      swconList.add(createAssemblySwConnector);

                      Map<String, String> csvmap = new HashMap<String, String>();

                      PortInterface pPortIf = ((RPortPrototype) mmdcsPort).getRequiredInterface();
                      String portInterface = (pPortIf instanceof SenderReceiverInterface) ? "SR"
                          : (pPortIf instanceof TriggerInterface) ? "TR"
                              : (pPortIf instanceof NvDataInterface) ? "NV" : (pPortIf instanceof ModeSwitchInterface)
                                  ? "MD" : (pPortIf instanceof ClientServerInterface) ? "CS" : "Other";

                      csvmap.put("message", message);
                      csvmap.put("outerport", GenerateArxmlUtil.getFragmentURI(externalPort));
                      csvmap.put("outercomponent", GenerateArxmlUtil.getFragmentURI(iswc));
                      csvmap.put("innerport", GenerateArxmlUtil.getFragmentURI(mmdcsPort));
                      csvmap.put("innercomponent", GenerateArxmlUtil.getFragmentURI(oswc));
                      csvmap.put("direction", "AR to MSR");
                      csvmap.put("interface", portInterface);

                      messageCSVList.add(csvmap);
                      assemblySwConnUtil.updateASWCToSWCPMap(shortNameOfASC, externalPort, mmdcsPort,
                          iswc.getShortName(), oswc.getShortName());

                    }
                  }
                }
              }

            }
          }
          else if ((outerPort instanceof PPortPrototype) &&
              (((PPortInCompositionInstanceRef) delegationConnection.getInnerPort()).getTargetPPort() != null) &&
              ((externalPort instanceof RPortPrototype) || (externalPort instanceof PRPortPrototype))) {

            AbstractProvidedPortPrototype mmdcsPort =
                ((PPortInCompositionInstanceRef) delegationConnection.getInnerPort()).getTargetPPort();

            if (checkIfCompatiblePortIsValid(externalPort, outerPort, portSWCMap.get(externalPort))) {

              AutosarDataPrototype externalPortDT = getDataPrototype(externalPort, outerPortDT.getShortName());

              boolean isSame = checkIfInterfacesAreSame(outerPortDT, externalPortDT);

              List<SwComponentPrototype> oswcL = portSWCMap.get(mmdcsPort);
              List<SwComponentPrototype> iswcL = portSWCMap.get(externalPort);

              if ((oswcL != null) && (iswcL != null)) {

                for (SwComponentPrototype iswc : iswcL) {
                  AutosarUtil.setCurrentProcessingEObject(iswc);

                  for (SwComponentPrototype oswc : oswcL) {
                    AutosarUtil.setCurrentProcessingEObject(oswc);
                    String shortNameOfASC =
                        getShortNameOfASC("ASC_" + oswc.getShortName() + "_" + mmdcsPort.getShortName() + "_" +
                            iswc.getShortName() + "_" + externalPort.getShortName(), swconList);

                    SwConnector swConnector = null;
                    String ascName = this.ascNameMap.get(shortNameOfASC);
                    if (ascName != null) {
                      swConnector = getSwConnector(swconList, ascName);

                    }
                    else {
                      swConnector = getSwConnector(swconList, shortNameOfASC);
                    }


                    if (swConnector != null) {
                      swConnector.setMapping(vdpMapping);

                    }
                    else {
                      AssemblySwConnector createAssemblySwConnector =
                          CompositionFactory.eINSTANCE.createAssemblySwConnector();

                      PPortInCompositionInstanceRef createPPortInCompositionInstanceRef =
                          InstancerefsFactory.eINSTANCE.createPPortInCompositionInstanceRef();


                      createPPortInCompositionInstanceRef.setTargetPPort(mmdcsPort);
                      createPPortInCompositionInstanceRef.setContextComponent(oswc);

                      createAssemblySwConnector.setProvider(createPPortInCompositionInstanceRef);

                      RPortInCompositionInstanceRef createRPortInCompositionInstanceRef =
                          InstancerefsFactory.eINSTANCE.createRPortInCompositionInstanceRef();

                      createRPortInCompositionInstanceRef.setTargetRPort((AbstractRequiredPortPrototype) externalPort);

                      createRPortInCompositionInstanceRef.setContextComponent(iswc);

                      VariationPoint variationPoint =
                          VariationPointUtil.getInstance().getVariationPoint(externalPort, iswc);

                      createAssemblySwConnector.setRequester(createRPortInCompositionInstanceRef);
                      createAssemblySwConnector.setShortName(shortNameOfASC);
                      if (isSame) {

                        createAssemblySwConnector.setMapping(vdpMapping);
                      }
                      else {
                        createAssemblySwConnector = createVAPIM(createAssemblySwConnector, autosarRoot,
                            vdpMapping.getDataMappings().get(0).getSecondDataPrototype(), externalPortDT, vdpMapping,
                            vapimList);
                      }
                      if (variationPoint != null) {
                        createAssemblySwConnector.setVariationPoint(variationPoint);
                      }
                      swconList.add(createAssemblySwConnector);

                      Map<String, String> csvmap = new HashMap<String, String>();
                      PortInterface pPortIf = ((PPortPrototype) mmdcsPort).getProvidedInterface();
                      String portInterface = (pPortIf instanceof SenderReceiverInterface) ? "SR"
                          : (pPortIf instanceof TriggerInterface) ? "TR"
                              : (pPortIf instanceof NvDataInterface) ? "NV" : (pPortIf instanceof ModeSwitchInterface)
                                  ? "MD" : (pPortIf instanceof ClientServerInterface) ? "CS" : "Other";

                      csvmap.put("message", message);
                      csvmap.put("outerport", GenerateArxmlUtil.getFragmentURI(externalPort));
                      csvmap.put("outercomponent", GenerateArxmlUtil.getFragmentURI(iswc));
                      csvmap.put("innerport", GenerateArxmlUtil.getFragmentURI(mmdcsPort));
                      csvmap.put("innercomponent", GenerateArxmlUtil.getFragmentURI(oswc));
                      csvmap.put("direction", "MSR to AR");
                      csvmap.put("interface", portInterface);

                      messageCSVList.add(csvmap);
                      assemblySwConnUtil.updateASWCToSWCPMap(shortNameOfASC, externalPort, mmdcsPort,
                          oswc.getShortName(), iswc.getShortName());


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


  private boolean checkIfCompatiblePortIsValid(final PortPrototype externalPort, final PortPrototype mmdcsPort,
      final List<SwComponentPrototype> swcpList) {

    boolean isValid = false;

    for (SwComponentPrototype swcp : swcpList) {
      AutosarUtil.setCurrentProcessingEObject(swcp);

      if (externalPort instanceof AbstractRequiredPortPrototype) {

        final Map<AbstractRequiredPortPrototype, List<SwComponentPrototype>> rPortMap = new HashMap<>();

        AbstractRequiredPortPrototype rPort = (AbstractRequiredPortPrototype) externalPort;


        if ((rPort instanceof PRPortPrototype) && (mmdcsPort instanceof PPortPrototype)) {
          PortInterface portInterface1 = ((PPortPrototype) mmdcsPort).getProvidedInterface();
          PortInterface portInterface2 = ((PRPortPrototype) rPort).getProvidedRequiredInterface();

          if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {
            updateRPortMap(portInterface1, portInterface2, rPort, swcp, rPortMap);
          }

        }
        else if ((mmdcsPort instanceof PRPortPrototype) && (rPort instanceof RPortPrototype)) {

          PortInterface portInterface1 = ((PRPortPrototype) mmdcsPort).getProvidedRequiredInterface();
          PortInterface portInterface2 = ((RPortPrototype) rPort).getRequiredInterface();

          if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {
            updateRPortMap(portInterface1, portInterface2, rPort, swcp, rPortMap);
          }

        }

        else if ((rPort instanceof RPortPrototype) && (mmdcsPort instanceof PPortPrototype)) {
          PortInterface portInterface1 = ((PPortPrototype) mmdcsPort).getProvidedInterface();
          PortInterface portInterface2 = ((RPortPrototype) rPort).getRequiredInterface();

          if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {
            updateRPortMap(portInterface1, portInterface2, rPort, swcp, rPortMap);
          }
        }

        if (rPortMap.size() == 1) {
          return true;
        }

      }
      else {

        if (externalPort instanceof AbstractProvidedPortPrototype) {

          final Map<AbstractProvidedPortPrototype, List<SwComponentPrototype>> pPortMap = new HashMap<>();

          AbstractProvidedPortPrototype pPort = (AbstractProvidedPortPrototype) externalPort;


          if ((pPort instanceof PRPortPrototype) && (mmdcsPort instanceof RPortPrototype)) {
            PortInterface portInterface1 = ((RPortPrototype) mmdcsPort).getRequiredInterface();
            PortInterface portInterface2 = ((PRPortPrototype) pPort).getProvidedRequiredInterface();

            if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {
              updatePPortMap(portInterface1, portInterface2, pPort, swcp, pPortMap);
            }
          }
          else if ((mmdcsPort instanceof PRPortPrototype) && (pPort instanceof PPortPrototype)) {

            PortInterface portInterface1 = ((PRPortPrototype) mmdcsPort).getProvidedRequiredInterface();
            PortInterface portInterface2 = ((PPortPrototype) pPort).getProvidedInterface();

            if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {

              updatePPortMap(portInterface1, portInterface2, pPort, swcp, pPortMap);
            }
          }
          else if ((pPort instanceof PPortPrototype) && (mmdcsPort instanceof RPortPrototype)) {
            PortInterface portInterface1 = ((RPortPrototype) mmdcsPort).getRequiredInterface();
            PortInterface portInterface2 = ((PPortPrototype) pPort).getProvidedInterface();

            if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {

              updatePPortMap(portInterface1, portInterface2, pPort, swcp, pPortMap);
            }
          }

          if (pPortMap.size() == 1) {
            return true;
          }
        }
      }
    }
    return isValid;
  }

  private void createAssemblyConnection(
      final Map<SwComponentPrototype, List<PortPrototype>> portsFromDelegationConnection,
      final Map<String, SwComponentPrototype> swCompositionComponentMap,
      final Map<String, SwComponentPrototype> olderSwCompositionComponentMap,
      final Map<String, List<String>> portAndCompRef, final boolean adminDataPresent, final boolean ignoreport,
      final boolean considerport, final PortPrototype portP, final List<SwConnector> swconList,
      final Map<String, List<String>> adminDataMap, final CompositionSwComponentType mmdcsCompositionType,
      final AUTOSAR autosarRoot, final Map<String, VariableAndParameterInterfaceMapping> vapimList, String message,
      final List<String> messageloglist, final List<Map<String, String>> messageCSVList,
      final AssemblySwConnectionUtil assemblySwConnUtil) {
    if (message.equals("")) {
      PortInterface pi = portP instanceof PPortPrototype ? ((PPortPrototype) portP).getProvidedInterface()
          : ((portP instanceof RPortPrototype ? ((RPortPrototype) portP).getRequiredInterface()
              : (portP instanceof PRPortPrototype ? ((PRPortPrototype) portP).getProvidedRequiredInterface() : null)));
      if ((pi != null) && (pi instanceof SenderReceiverInterface)) {
        SenderReceiverInterface sr = (SenderReceiverInterface) pi;
        if (!sr.getDataElements().isEmpty()) {
          message = sr.getDataElements().get(0).getShortName().replace("VDP_", "");
        }
      }
    }

    for (SwComponentPrototype swc : portsFromDelegationConnection.keySet()) {
      AutosarUtil.setCurrentProcessingEObject(swc);
      SwComponentPrototype swComponentPrototype = swCompositionComponentMap.get(swc.getShortName());

      if (swComponentPrototype != null) {

        List<PortPrototype> portss = portsFromDelegationConnection.get(swc);

        if (portss == null) {
          continue;
        }

        for (PortPrototype port : portss) {
          AutosarUtil.setCurrentProcessingEObject(port);


          if (port instanceof AbstractProvidedPortPrototype) {

            AbstractProvidedPortPrototype pPort = (AbstractProvidedPortPrototype) port;

            Map<AbstractRequiredPortPrototype, List<SwComponentPrototype>> equivalentRPortsForASW =
                getEquivalentRPortsForASW((AbstractProvidedPortPrototype) portP,
                    olderSwCompositionComponentMap.values());

            for (AbstractRequiredPortPrototype rPort : equivalentRPortsForASW.keySet()) {
              AutosarUtil.setCurrentProcessingEObject(rPort);

              for (SwComponentPrototype swC : equivalentRPortsForASW.get(rPort)) {
                AutosarUtil.setCurrentProcessingEObject(swC);
                SwComponentPrototype swComponentPrototype1 = swCompositionComponentMap.get(swC.getShortName());

                if (adminDataPresent) {
                  String portURI = GenerateArxmlUtil.getFragmentURI(rPort);
                  String swcURI = GenerateArxmlUtil.getFragmentURI(swComponentPrototype1);

                  if (ignoreport && isIgnorePort(ignoreport, portAndCompRef, portURI, swcURI)) {
                    continue;
                  }
                  else if (considerport &&
                      (portAndCompRef.isEmpty() || !isConsiderPort(considerport, portAndCompRef, portURI, swcURI))) {
                    continue;
                  }
                }

                if (swComponentPrototype1 != null) {
                  AssemblySwConnector createAssemblySwConnector =
                      CompositionFactory.eINSTANCE.createAssemblySwConnector();

                  PPortInCompositionInstanceRef createPPortInCompositionInstanceRef =
                      InstancerefsFactory.eINSTANCE.createPPortInCompositionInstanceRef();

                  createPPortInCompositionInstanceRef.setTargetPPort(pPort);
                  createPPortInCompositionInstanceRef.setContextComponent(swComponentPrototype);

                  createAssemblySwConnector.setProvider(createPPortInCompositionInstanceRef);

                  RPortInCompositionInstanceRef createRPortInCompositionInstanceRef =
                      InstancerefsFactory.eINSTANCE.createRPortInCompositionInstanceRef();


                  createRPortInCompositionInstanceRef.setTargetRPort(rPort);
                  createRPortInCompositionInstanceRef.setContextComponent(swComponentPrototype1);
                  VariationPoint variationPoint =
                      VariationPointUtil.getInstance().getVariationPoint(rPort, swComponentPrototype1);


                  createAssemblySwConnector.setRequester(createRPortInCompositionInstanceRef);
                  String shortnameofASC =
                      getShortNameOfASC("ASC_" + swComponentPrototype.getShortName() + "_" + pPort.getShortName() +
                          "_" + swComponentPrototype1.getShortName() + "_" + rPort.getShortName(), swconList);


                  createAssemblySwConnector.setShortName(shortnameofASC);

                  if (variationPoint != null) {
                    createAssemblySwConnector.setVariationPoint(variationPoint);
                  }

                  if ((portP.getAdminData() != null) && !adminDataMap.isEmpty() && (adminDataMap.size() <= 2)) {
                    createAssemblySwConnector = setpPortVAPIM(createAssemblySwConnector, portP, pPort, rPort,
                        mmdcsCompositionType, autosarRoot, vapimList);

                  }
                  swconList.add(createAssemblySwConnector);

                  Map<String, String> csvmap = new HashMap<String, String>();
                  PortInterface pPortIf = ((PPortPrototype) pPort).getProvidedInterface();
                  String portInterface = (pPortIf instanceof SenderReceiverInterface) ? "SR"
                      : (pPortIf instanceof TriggerInterface) ? "TR"
                          : (pPortIf instanceof NvDataInterface) ? "NV" : (pPortIf instanceof ModeSwitchInterface)
                              ? "MD" : (pPortIf instanceof ClientServerInterface) ? "CS" : "Other";


                  csvmap.put("message", message);
                  csvmap.put("outerport", GenerateArxmlUtil.getFragmentURI(rPort));
                  csvmap.put("outercomponent", GenerateArxmlUtil.getFragmentURI(swComponentPrototype1));
                  csvmap.put("innerport", GenerateArxmlUtil.getFragmentURI(port));
                  csvmap.put("innercomponent", GenerateArxmlUtil.getFragmentURI(swComponentPrototype));
                  csvmap.put("direction", "MSR to AR");
                  csvmap.put("interface", portInterface);

                  messageCSVList.add(csvmap);
                  assemblySwConnUtil.updateASWCToSWCPMap(shortnameofASC, port, rPort,
                      swComponentPrototype.getShortName(), swComponentPrototype1.getShortName());

                }
              }

            }

          }
          else if (port instanceof AbstractRequiredPortPrototype) {

            AbstractRequiredPortPrototype rPort = (AbstractRequiredPortPrototype) port;

            Map<AbstractProvidedPortPrototype, List<SwComponentPrototype>> equivalentPPortsForASW =
                getEquivalentPPortsForASW((AbstractRequiredPortPrototype) portP,
                    olderSwCompositionComponentMap.values());

            for (AbstractProvidedPortPrototype pPort : equivalentPPortsForASW.keySet()) {

              AutosarUtil.setCurrentProcessingEObject(pPort);
              for (SwComponentPrototype swC : equivalentPPortsForASW.get(pPort)) {
                AutosarUtil.setCurrentProcessingEObject(swC);
                SwComponentPrototype swComponentPrototype1 = swCompositionComponentMap.get(swC.getShortName());

                if (adminDataPresent) {
                  String portURI = GenerateArxmlUtil.getFragmentURI(pPort);
                  String swcURI = GenerateArxmlUtil.getFragmentURI(swComponentPrototype1);

                  if (ignoreport && isIgnorePort(ignoreport, portAndCompRef, portURI, swcURI)) {
                    continue;
                  }
                  else if (considerport &&
                      (portAndCompRef.isEmpty() || !isConsiderPort(considerport, portAndCompRef, portURI, swcURI))) {
                    continue;
                  }
                }


                if (swComponentPrototype1 != null) {

                  AssemblySwConnector createAssemblySwConnector =
                      CompositionFactory.eINSTANCE.createAssemblySwConnector();

                  PPortInCompositionInstanceRef createPPortInCompositionInstanceRef =
                      InstancerefsFactory.eINSTANCE.createPPortInCompositionInstanceRef();

                  createPPortInCompositionInstanceRef.setTargetPPort(pPort);
                  createPPortInCompositionInstanceRef.setContextComponent(swComponentPrototype1);

                  createAssemblySwConnector.setProvider(createPPortInCompositionInstanceRef);

                  RPortInCompositionInstanceRef createRPortInCompositionInstanceRef =
                      InstancerefsFactory.eINSTANCE.createRPortInCompositionInstanceRef();

                  createRPortInCompositionInstanceRef.setTargetRPort(rPort);

                  createRPortInCompositionInstanceRef.setContextComponent(swComponentPrototype);

                  VariationPoint variationPoint =
                      VariationPointUtil.getInstance().getVariationPoint(rPort, swComponentPrototype);

                  createAssemblySwConnector.setRequester(createRPortInCompositionInstanceRef);
                  String shortnameofASC =
                      getShortNameOfASC("ASC_" + swComponentPrototype1.getShortName() + "_" + pPort.getShortName() +
                          "_" + swComponentPrototype.getShortName() + "_" + rPort.getShortName(), swconList);

                  createAssemblySwConnector.setShortName(shortnameofASC);

                  if (variationPoint != null) {
                    createAssemblySwConnector.setVariationPoint(variationPoint);
                  }

                  if ((portP.getAdminData() != null) && !adminDataMap.isEmpty() && (adminDataMap.size() <= 2)) {

                    createAssemblySwConnector = setrPortVAPIM(createAssemblySwConnector, portP, pPort, rPort,
                        mmdcsCompositionType, autosarRoot, vapimList);


                  }

                  swconList.add(createAssemblySwConnector);

                  Map<String, String> csvmap = new HashMap<String, String>();

                  PortInterface pPortIf = ((PPortPrototype) pPort).getProvidedInterface();
                  String portInterface = (pPortIf instanceof SenderReceiverInterface) ? "SR"
                      : (pPortIf instanceof TriggerInterface) ? "TR"
                          : (pPortIf instanceof NvDataInterface) ? "NV" : (pPortIf instanceof ModeSwitchInterface)
                              ? "MD" : (pPortIf instanceof ClientServerInterface) ? "CS" : "Other";

                  csvmap.put("message", message);
                  csvmap.put("outerport", GenerateArxmlUtil.getFragmentURI(pPort));
                  csvmap.put("outercomponent", GenerateArxmlUtil.getFragmentURI(swComponentPrototype1));
                  csvmap.put("innerport", GenerateArxmlUtil.getFragmentURI(port));
                  csvmap.put("innercomponent", GenerateArxmlUtil.getFragmentURI(swComponentPrototype));
                  csvmap.put("direction", "AR to MSR");
                  csvmap.put("interface", portInterface);

                  messageCSVList.add(csvmap);

                  assemblySwConnUtil.updateASWCToSWCPMap(shortnameofASC, pPort, port,
                      swComponentPrototype1.getShortName(), swComponentPrototype.getShortName());

                }


              }

            }
          }
        }
      }
    }

  }


  /**
   * @param vapimList
   * @param autosarRoot
   * @param mmdcsCompositionType
   * @param rPort
   * @param pPort
   * @param portP
   * @param createAssemblySwConnector
   * @return
   */
  private AssemblySwConnector setrPortVAPIM(AssemblySwConnector createAssemblySwConnector, final PortPrototype portP,
      final AbstractProvidedPortPrototype pPort, final AbstractRequiredPortPrototype rPort,
      final CompositionSwComponentType mmdcsCompositionType, final AUTOSAR autosarRoot,
      final Map<String, VariableAndParameterInterfaceMapping> vapimList) {
    List<DelegationSwConnector> delegationConnections = getDelegationConnection(portP, mmdcsCompositionType);

    for (DelegationSwConnector delegationConnection : delegationConnections) {
      AutosarUtil.setCurrentProcessingEObject(delegationConnection);

      PortInCompositionTypeInstanceRef innerPort = delegationConnection.getInnerPort();
      if (innerPort instanceof RPortInCompositionInstanceRef) {
        RPortInCompositionInstanceRef rInPort = (RPortInCompositionInstanceRef) innerPort;

        if ((rPort == rInPort.getTargetRPort()) && (delegationConnection.getMapping() != null) &&
            (delegationConnection.getMapping() instanceof VariableAndParameterInterfaceMapping)) {

          VariableAndParameterInterfaceMapping vapiM =
              (VariableAndParameterInterfaceMapping) delegationConnection.getMapping();
          if (!vapiM.getDataMappings().isEmpty() && (vapiM.getDataMappings().get(0).getFirstDataPrototype() != null) &&
              (vapiM.getDataMappings().get(0).getSecondDataPrototype() != null)) {

            List<VariableDataPrototype> externalDataPrototypeList = getDataPrototypeList(pPort);
            List<VariableDataPrototype> innerDataPrototypeList = getDataPrototypeList(rPort);
            List<VariableDataPrototype> outerDataPrototypeList = getDataPrototypeList(portP);
            if ((!externalDataPrototypeList.isEmpty()) && (innerDataPrototypeList.size() == 1) &&
                (outerDataPrototypeList.size() == 1)) {
              VariableDataPrototype innerDataPrototype = innerDataPrototypeList.get(0);
              VariableDataPrototype outerDataPrototype = outerDataPrototypeList.get(0);
              if ((innerDataPrototype != null) && (outerDataPrototype != null)) {
                AutosarDataPrototype externalDataPrototype =
                    getDataPrototype(externalDataPrototypeList, outerDataPrototype);
                if (!checkIfVDPsAreSame(innerDataPrototype, externalDataPrototype)) {

                  if (isSameVapiM(outerDataPrototype, innerDataPrototype,
                      vapiM.getDataMappings().get(0).getFirstDataPrototype(),
                      vapiM.getDataMappings().get(0).getSecondDataPrototype())) {


                    boolean isSame = checkIfInterfacesAreSame(outerDataPrototype, externalDataPrototype);
                    if (isSame) {

                      createAssemblySwConnector.setMapping(vapiM);
                      return createAssemblySwConnector;

                    }
                    else {

                      createAssemblySwConnector = createVAPIM(createAssemblySwConnector, autosarRoot,
                          externalDataPrototype, innerDataPrototype, vapiM, vapimList);
                      return createAssemblySwConnector;

                    }
                  }
                  else {

                    createAssemblySwConnector = getValidVAPIM(createAssemblySwConnector, autosarRoot,
                        externalDataPrototype, innerDataPrototype, vapiM, vapimList);
                    return createAssemblySwConnector;
                  }
                }
              }
            }
          }
        }
      }
    }
    return createAssemblySwConnector;
  }

  /**
   * @param createAssemblySwConnector
   * @param portP
   * @param rPort
   * @param pPort
   * @param mmdcsCompositionType
   * @param autosarRoot
   * @param vapimList
   * @return
   */
  private AssemblySwConnector setpPortVAPIM(AssemblySwConnector createAssemblySwConnector, final PortPrototype portP,
      final AbstractProvidedPortPrototype pPort, final AbstractRequiredPortPrototype rPort,
      final CompositionSwComponentType mmdcsCompositionType, final AUTOSAR autosarRoot,
      final Map<String, VariableAndParameterInterfaceMapping> vapimList) {

    List<DelegationSwConnector> delegationConnections = getDelegationConnection(portP, mmdcsCompositionType);

    for (DelegationSwConnector delegationConnection : delegationConnections) {
      AutosarUtil.setCurrentProcessingEObject(delegationConnection);

      PortInCompositionTypeInstanceRef innerPort = delegationConnection.getInnerPort();
      if (innerPort instanceof PPortInCompositionInstanceRef) {
        PPortInCompositionInstanceRef pInPort = (PPortInCompositionInstanceRef) innerPort;

        if ((pPort == pInPort.getTargetPPort()) && (delegationConnection.getMapping() != null) &&
            (delegationConnection.getMapping() instanceof VariableAndParameterInterfaceMapping)) {

          VariableAndParameterInterfaceMapping vapiM =
              (VariableAndParameterInterfaceMapping) delegationConnection.getMapping();
          if (!vapiM.getDataMappings().isEmpty() && (vapiM.getDataMappings().get(0).getFirstDataPrototype() != null) &&
              (vapiM.getDataMappings().get(0).getSecondDataPrototype() != null)) {

            List<VariableDataPrototype> externalDataPrototypeList = getDataPrototypeList(rPort);
            List<VariableDataPrototype> innerDataPrototypeList = getDataPrototypeList(pPort);
            List<VariableDataPrototype> outerDataPrototypeList = getDataPrototypeList(portP);
            if ((!externalDataPrototypeList.isEmpty()) && (innerDataPrototypeList.size() == 1) &&
                (outerDataPrototypeList.size() == 1)) {

              VariableDataPrototype innerDataPrototype = innerDataPrototypeList.get(0);
              VariableDataPrototype outerDataPrototype = outerDataPrototypeList.get(0);


              if ((innerDataPrototype != null) && (outerDataPrototype != null)) {
                AutosarDataPrototype externalDataPrototype =
                    getDataPrototype(externalDataPrototypeList, outerDataPrototype);
                if (!checkIfVDPsAreSame(innerDataPrototype, externalDataPrototype)) {

                  if (isSameVapiM(outerDataPrototype, innerDataPrototype,
                      vapiM.getDataMappings().get(0).getFirstDataPrototype(),
                      vapiM.getDataMappings().get(0).getSecondDataPrototype())) {


                    boolean isSame = checkIfInterfacesAreSame(outerDataPrototype, externalDataPrototype);
                    if (isSame) {

                      createAssemblySwConnector.setMapping(vapiM);
                      return createAssemblySwConnector;

                    }
                    else {

                      createAssemblySwConnector = createVAPIM(createAssemblySwConnector, autosarRoot,
                          innerDataPrototype, externalDataPrototype, vapiM, vapimList);
                      return createAssemblySwConnector;

                    }
                  }
                  else {

                    createAssemblySwConnector = getValidVAPIM(createAssemblySwConnector, autosarRoot,
                        innerDataPrototype, externalDataPrototype, vapiM, vapimList);
                    return createAssemblySwConnector;
                  }
                }
              }
            }
          }

        }
      }
    }
    return createAssemblySwConnector;
  }

  private AssemblySwConnector getValidVAPIM(final AssemblySwConnector createAssemblySwConnector,
      final AUTOSAR autosarRoot, final AutosarDataPrototype pPortVDP, final AutosarDataPrototype rPortVDP,
      final VariableAndParameterInterfaceMapping vapiM,
      final Map<String, VariableAndParameterInterfaceMapping> vapimList) {
    String pkgPath = GenerateArxmlUtil.getPackagePath(vapiM.eContainer().eContainer());
    ARPackage arArPackage = GenerateArxmlUtil.getExistingARArPackage(autosarRoot, pkgPath);

    if (arArPackage != null) {

      String vpimName = GenerateArxmlUtil.getShortName(vapiM.eContainer());

      PackageableElement packageableElement = !arArPackage.getElements().isEmpty()
          ? arArPackage.getElements().stream().filter(l -> l.getShortName().equals(vpimName)).findFirst().get() : null;
      PortInterfaceMappingSet createPortInterfaceMappingSet = null;
      if (packageableElement != null) {
        createPortInterfaceMappingSet = (PortInterfaceMappingSet) packageableElement;
      }
      else {
        createPortInterfaceMappingSet =
            autosar40.swcomponent.portinterface.PortinterfaceFactory.eINSTANCE.createPortInterfaceMappingSet();
        createPortInterfaceMappingSet.setShortName(GenerateArxmlUtil.getShortName(vapiM.eContainer()));
        arArPackage.getElements().add(createPortInterfaceMappingSet);
      }

      VariableAndParameterInterfaceMapping vapiMapping =
          autosar40.swcomponent.portinterface.PortinterfaceFactory.eINSTANCE
              .createVariableAndParameterInterfaceMapping();

      int cnt = 1;

      String tempName1 = "VAPIM_" + pPortVDP.getShortName() + "_" + rPortVDP.getShortName() + "_PIMS_" +
          vapiM.getPortInterfaceMappingSet().getShortName();
      String tempName2 = "VAPIM_" + rPortVDP.getShortName() + "_" + pPortVDP.getShortName() + "_PIMS_" +
          vapiM.getPortInterfaceMappingSet().getShortName();
      String tempName = tempName1;

      if (vapimList.containsKey(tempName1) || vapimList.containsKey(tempName2)) {
        String tempName3 = vapimList.containsKey(tempName1) ? tempName1 : tempName2;
        tempName = tempName3;
        while (vapimList.containsKey(tempName)) {

          VariableAndParameterInterfaceMapping tempvapiMapping = vapimList.get(tempName);
          boolean issame =
              isSameVapiM(pPortVDP, rPortVDP, tempvapiMapping.getDataMappings().get(0).getFirstDataPrototype(),
                  tempvapiMapping.getDataMappings().get(0).getSecondDataPrototype());
          if (issame) {
            createAssemblySwConnector.setMapping(tempvapiMapping);
            return createAssemblySwConnector;

          }

          tempName = tempName3 + "_" + cnt;
          cnt++;
        }
      }
      String tempName4 =
          tempName.substring(0, tempName.indexOf("_PIMS_")) + tempName.substring(tempName.lastIndexOf("_"));
      vapiMapping.setShortName(tempName4);


      EList<DataPrototypeMapping> dataMappings = vapiMapping.getDataMappings();

      DataPrototypeMapping dataPrototypeMapping =
          autosar40.swcomponent.portinterface.PortinterfaceFactory.eINSTANCE.createDataPrototypeMapping();
      dataPrototypeMapping.setFirstDataPrototype(pPortVDP);
      dataPrototypeMapping.setSecondDataPrototype(rPortVDP);
      dataMappings.add(dataPrototypeMapping);
      createPortInterfaceMappingSet.getPortInterfaceMappings().add(vapiMapping);
      vapimList.put(tempName, vapiMapping);

      createAssemblySwConnector.setMapping(vapiMapping);
    }
    return createAssemblySwConnector;
  }


  private AssemblySwConnector createVAPIM(final AssemblySwConnector createAssemblySwConnector,
      final AUTOSAR autosarRoot, final AutosarDataPrototype pPortVDP, final AutosarDataPrototype rPortVDP,
      final VariableAndParameterInterfaceMapping vapiM,
      final Map<String, VariableAndParameterInterfaceMapping> vapimList) {
    String pkgPath = GenerateArxmlUtil.getPackagePath(vapiM.eContainer().eContainer());
    ARPackage arArPackage = GenerateArxmlUtil.getExistingARArPackage(autosarRoot, pkgPath);

    if (arArPackage != null) {

      String vpimName = GenerateArxmlUtil.getShortName(vapiM.eContainer());

      PackageableElement packageableElement = !arArPackage.getElements().isEmpty()
          ? arArPackage.getElements().stream().filter(l -> l.getShortName().equals(vpimName)).findFirst().get() : null;
      PortInterfaceMappingSet createPortInterfaceMappingSet = null;
      if (packageableElement != null) {
        createPortInterfaceMappingSet = (PortInterfaceMappingSet) packageableElement;
      }
      else {
        createPortInterfaceMappingSet =
            autosar40.swcomponent.portinterface.PortinterfaceFactory.eINSTANCE.createPortInterfaceMappingSet();
        createPortInterfaceMappingSet.setShortName(GenerateArxmlUtil.getShortName(vapiM.eContainer()));
        arArPackage.getElements().add(createPortInterfaceMappingSet);
      }

      VariableAndParameterInterfaceMapping vapiMapping =
          autosar40.swcomponent.portinterface.PortinterfaceFactory.eINSTANCE
              .createVariableAndParameterInterfaceMapping();

      int cnt = 1;

      String tempName = vapiM.getShortName() + "_PIMS_" + vapiM.getPortInterfaceMappingSet().getShortName() + "_" + cnt;
      while (vapimList.containsKey(tempName)) {
        VariableAndParameterInterfaceMapping tempvapiMapping = vapimList.get(tempName);
        boolean issame =
            isSameVapiM(pPortVDP, rPortVDP, tempvapiMapping.getDataMappings().get(0).getFirstDataPrototype(),
                tempvapiMapping.getDataMappings().get(0).getSecondDataPrototype());
        if (issame) {
          createAssemblySwConnector.setMapping(tempvapiMapping);
          return createAssemblySwConnector;
        }
        cnt++;
        tempName = vapiM.getShortName() + "_PIMS_" + vapiM.getPortInterfaceMappingSet().getShortName() + "_" + cnt;
      }
      String tempName1 =
          tempName.substring(0, tempName.indexOf("_PIMS_")) + tempName.substring(tempName.lastIndexOf("_"));
      vapiMapping.setShortName(tempName1);

      EList<DataPrototypeMapping> dataMappings = vapiMapping.getDataMappings();

      DataPrototypeMapping dataPrototypeMapping =
          autosar40.swcomponent.portinterface.PortinterfaceFactory.eINSTANCE.createDataPrototypeMapping();
      dataPrototypeMapping.setFirstDataPrototype(pPortVDP);
      dataPrototypeMapping.setSecondDataPrototype(rPortVDP);
      dataMappings.add(dataPrototypeMapping);
      createPortInterfaceMappingSet.getPortInterfaceMappings().add(vapiMapping);
      vapimList.put(tempName, vapiMapping);

      createAssemblySwConnector.setMapping(vapiMapping);
    }
    return createAssemblySwConnector;
  }

  /**
   * @param pPortVDP
   * @param rPortVDP
   * @param firstDataPrototype
   * @param secondDataPrototype
   */
  private boolean isSameVapiM(final AutosarDataPrototype pPortVDP, final AutosarDataPrototype rPortVDP,
      final AutosarDataPrototype firstDataPrototype, final AutosarDataPrototype secondDataPrototype) {

    if ((EcoreUtil.getURI(pPortVDP).equals(EcoreUtil.getURI(firstDataPrototype)) &&
        EcoreUtil.getURI(rPortVDP).equals(EcoreUtil.getURI(secondDataPrototype))) ||
        (EcoreUtil.getURI(rPortVDP).equals(EcoreUtil.getURI(firstDataPrototype)) &&
            EcoreUtil.getURI(pPortVDP).equals(EcoreUtil.getURI(secondDataPrototype)))) {
      return true;
    }

    return false;

  }


  public String getShortNameOfASC(final String shortnameofASC, final List<SwConnector> swconList) {

    String ascShortName = shortnameofASC;
    if (ascShortName != null) {
      int cnt = 0;
      if (ascShortName.length() > 127) {
        ascShortName = ascShortName.substring(0, 120);
        String tempName = ascShortName + "_" + cnt;

        while (getSwConnector(swconList, tempName) != null) {
          cnt++;
          tempName = ascShortName + "_" + cnt;
        }
        ascShortName = tempName;
        this.ascNameMap.put(shortnameofASC, ascShortName);
      }
    }

    return ascShortName;

  }


  private Map<String, List<PortPrototype>> getVdpPortMap(
      final Map<String, SwComponentPrototype> swCompositionComponentMap, final List<SwComponentPrototype> swcList) {

    Map<String, List<PortPrototype>> map = new HashMap<String, List<PortPrototype>>();

    for (SwComponentPrototype swc : swCompositionComponentMap.values()) {
      AutosarUtil.setCurrentProcessingEObject(swc);
      if (!(swcList.contains(swc))) {

        for (PortPrototype p : swc.getType().getPorts()) {
          AutosarUtil.setCurrentProcessingEObject(p);
          PortInterface pi = p instanceof PPortPrototype ? ((PPortPrototype) p).getProvidedInterface()
              : ((p instanceof RPortPrototype ? ((RPortPrototype) p).getRequiredInterface()
                  : (p instanceof PRPortPrototype ? ((PRPortPrototype) p).getProvidedRequiredInterface() : null)));

          if ((pi != null) && (pi instanceof SenderReceiverInterface)) {
            SenderReceiverInterface si = (SenderReceiverInterface) pi;

            for (VariableDataPrototype vdp : si.getDataElements()) {

              List<PortPrototype> list = map.get(vdp.getShortName());

              if (list == null) {
                List<PortPrototype> pList = new ArrayList<PortPrototype>();
                pList.add(p);
                map.put(vdp.getShortName(), pList);
              }
              else {
                list.add(p);
              }
            }
          }
        }
      }
    }
    return map;

  }

  private Map<SwComponentPrototype, List<PortPrototype>> getPortSWCMap(
      final Map<String, SwComponentPrototype> swCompositionComponentMap,
      final Map<PortPrototype, List<SwComponentPrototype>> portToSwcMap) {

    Map<SwComponentPrototype, List<PortPrototype>> map = new HashMap<SwComponentPrototype, List<PortPrototype>>();

    for (SwComponentPrototype swc : swCompositionComponentMap.values()) {
      AutosarUtil.setCurrentProcessingEObject(swc);


      if (!swc.getType().getPorts().isEmpty()) {


        List<PortPrototype> list = map.get(swc);

        if (list != null) {
          list.addAll(swc.getType().getPorts());
        }
        else {
          List<PortPrototype> l = new ArrayList<>();
          l.addAll(swc.getType().getPorts());
          map.put(swc, l);
        }


        for (PortPrototype p : swc.getType().getPorts()) {


          List<SwComponentPrototype> swclist = portToSwcMap.get(p);

          if (swclist != null) {
            swclist.add(swc);
          }
          else {
            List<SwComponentPrototype> l = new ArrayList<>();
            l.add(swc);
            portToSwcMap.put(p, l);
          }
        }
      }

    }
    return map;

  }


  private SwConnector getSwConnector(final List<SwConnector> swList, final String ascName) {

    SwConnector sc = null;

    for (SwConnector swc : swList) {
      AutosarUtil.setCurrentProcessingEObject(swc);

      if (swc.getShortName().equals(ascName)) {
        return swc;
      }
    }
    return sc;

  }


  private List<DelegationSwConnector> getDelegationConnection(final PortPrototype port,
      final CompositionSwComponentType compoSwComponentType) {

    List<DelegationSwConnector> dscs = new ArrayList<>();

    for (SwConnector swConnector : compoSwComponentType.getConnectors()) {
      AutosarUtil.setCurrentProcessingEObject(swConnector);
      if (swConnector instanceof DelegationSwConnector) {

        DelegationSwConnector dsc = (DelegationSwConnector) swConnector;

        PortPrototype outerPort = dsc.getOuterPort();

        if (port.equals(outerPort)) {
          dscs.add(dsc);
        }
      }
    }

    return dscs;
  }

  private Map<SwComponentPrototype, List<PortPrototype>> getPortsFromDelegationConnection(final PortPrototype port,
      final CompositionSwComponentType compoSwComponentType) {

    Map<SwComponentPrototype, List<PortPrototype>> portMap = new HashMap<SwComponentPrototype, List<PortPrototype>>();

    for (SwConnector swConnector : compoSwComponentType.getConnectors()) {
      AutosarUtil.setCurrentProcessingEObject(swConnector);
      if (swConnector instanceof DelegationSwConnector) {

        DelegationSwConnector dsc = (DelegationSwConnector) swConnector;

        PortPrototype outerPort = dsc.getOuterPort();

        if (port.equals(outerPort)) {

          PortPrototype targetport = null;
          SwComponentPrototype targetSwc = null;

          if (port instanceof AbstractRequiredPortPrototype) {

            RPortInCompositionInstanceRef innerRport = (RPortInCompositionInstanceRef) dsc.getInnerPort();

            targetport = innerRport.getTargetRPort();
            targetSwc = innerRport.getContextComponent();

          }
          else if (port instanceof AbstractProvidedPortPrototype) {

            PPortInCompositionInstanceRef innerPport = (PPortInCompositionInstanceRef) dsc.getInnerPort();

            targetport = innerPport.getTargetPPort();
            targetSwc = innerPport.getContextComponent();
          }

          if ((targetport != null) && (targetSwc != null)) {


            List<PortPrototype> list = portMap.get(targetSwc);

            if (list == null) {
              List<PortPrototype> pList = new ArrayList<>();
              pList.add(targetport);
              portMap.put(targetSwc, pList);
            }
            else {
              list.add(targetport);
            }
          }
        }
      }
    }

    return portMap;
  }

  private Map<AbstractProvidedPortPrototype, List<SwComponentPrototype>> getEquivalentPPortsForASW(
      final AbstractRequiredPortPrototype rPort, final Collection<SwComponentPrototype> swcs) {

    Map<AbstractProvidedPortPrototype, List<SwComponentPrototype>> pPortMap =
        new HashMap<AbstractProvidedPortPrototype, List<SwComponentPrototype>>();


    for (SwComponentPrototype swcp : swcs) {

      AutosarUtil.setCurrentProcessingEObject(swcp);

      SwComponentType type = swcp.getType();

      for (PortPrototype port : type.getPorts()) {
        AutosarUtil.setCurrentProcessingEObject(port);
        if (port instanceof AbstractProvidedPortPrototype) {

          AbstractProvidedPortPrototype pPort = (AbstractProvidedPortPrototype) port;


          if ((pPort instanceof PRPortPrototype) && (rPort instanceof RPortPrototype)) {
            PortInterface portInterface1 = ((RPortPrototype) rPort).getRequiredInterface();
            PortInterface portInterface2 = ((PRPortPrototype) pPort).getProvidedRequiredInterface();

            if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {

              updatePPortMap(portInterface1, portInterface2, pPort, swcp, pPortMap);

            }
          }
          else if ((rPort instanceof PRPortPrototype) && (pPort instanceof PPortPrototype)) {

            PortInterface portInterface1 = ((PRPortPrototype) rPort).getProvidedRequiredInterface();
            PortInterface portInterface2 = ((PPortPrototype) pPort).getProvidedInterface();

            if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {

              updatePPortMap(portInterface1, portInterface2, pPort, swcp, pPortMap);

            }
          }
          else if ((pPort instanceof PPortPrototype) && (rPort instanceof RPortPrototype)) {
            PortInterface portInterface1 = ((RPortPrototype) rPort).getRequiredInterface();
            PortInterface portInterface2 = ((PPortPrototype) pPort).getProvidedInterface();

            if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {

              updatePPortMap(portInterface1, portInterface2, pPort, swcp, pPortMap);

            }
          }
        }
      }
    }


    return pPortMap;
  }


  private Map<AbstractRequiredPortPrototype, List<SwComponentPrototype>> getEquivalentRPortsForASW(
      final AbstractProvidedPortPrototype pPort, final Collection<SwComponentPrototype> swcs) {

    Map<AbstractRequiredPortPrototype, List<SwComponentPrototype>> rPortMap =
        new HashMap<AbstractRequiredPortPrototype, List<SwComponentPrototype>>();


    for (SwComponentPrototype swcp : swcs) {
      AutosarUtil.setCurrentProcessingEObject(swcp);

      SwComponentType type = swcp.getType();

      for (PortPrototype port : type.getPorts()) {
        AutosarUtil.setCurrentProcessingEObject(port);
        if (port instanceof AbstractRequiredPortPrototype) {

          AbstractRequiredPortPrototype rPort = (AbstractRequiredPortPrototype) port;


          if ((rPort instanceof PRPortPrototype) && (pPort instanceof PPortPrototype)) {
            PortInterface portInterface1 = ((PPortPrototype) pPort).getProvidedInterface();
            PortInterface portInterface2 = ((PRPortPrototype) rPort).getProvidedRequiredInterface();

            if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {

              updateRPortMap(portInterface1, portInterface2, rPort, swcp, rPortMap);

            }
          }
          else if ((pPort instanceof PRPortPrototype) && (rPort instanceof RPortPrototype)) {

            PortInterface portInterface1 = ((PRPortPrototype) pPort).getProvidedRequiredInterface();
            PortInterface portInterface2 = ((RPortPrototype) rPort).getRequiredInterface();

            if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {

              updateRPortMap(portInterface1, portInterface2, rPort, swcp, rPortMap);
            }
          }

          else if ((rPort instanceof RPortPrototype) && (pPort instanceof PPortPrototype)) {
            PortInterface portInterface1 = ((PPortPrototype) pPort).getProvidedInterface();
            PortInterface portInterface2 = ((RPortPrototype) rPort).getRequiredInterface();

            if (portInterface1.eClass().getName().equals(portInterface2.eClass().getName())) {

              updateRPortMap(portInterface1, portInterface2, rPort, swcp, rPortMap);

            }
          }
        }
      }
    }

    return rPortMap;
  }


  private void updatePPortMap(final PortInterface portInterface1, final PortInterface portInterface2,
      final AbstractProvidedPortPrototype pPort, final SwComponentPrototype swcp,
      final Map<AbstractProvidedPortPrototype, List<SwComponentPrototype>> pPortMap) {
    if (portInterface1 instanceof SenderReceiverInterface) {
      SenderReceiverInterface srinterface1 = (SenderReceiverInterface) portInterface1;
      SenderReceiverInterface srinterface2 = (SenderReceiverInterface) portInterface2;

      EList<VariableDataPrototype> dataElements1 = srinterface1.getDataElements();
      EList<VariableDataPrototype> dataElements2 = srinterface2.getDataElements();

      if ((dataElements2.size() >= dataElements1.size()) && isDataElementsSame(dataElements1, dataElements2)) {
        List<SwComponentPrototype> list = pPortMap.get(pPort);
        if (list != null) {
          list.add(swcp);
        }
        else {
          List<SwComponentPrototype> l = new ArrayList<>();
          l.add(swcp);
          pPortMap.put(pPort, l);
        }
      }
    }
    else if (portInterface1 instanceof NvDataInterface) {
      NvDataInterface srinterface1 = (NvDataInterface) portInterface1;
      NvDataInterface srinterface2 = (NvDataInterface) portInterface2;

      EList<VariableDataPrototype> dataElements1 = srinterface1.getNvDatas();
      EList<VariableDataPrototype> dataElements2 = srinterface2.getNvDatas();

      if ((dataElements2.size() >= dataElements1.size()) && isDataElementsSame(dataElements1, dataElements2)) {
        List<SwComponentPrototype> list = pPortMap.get(pPort);
        if (list != null) {
          list.add(swcp);
        }
        else {
          List<SwComponentPrototype> l = new ArrayList<>();
          l.add(swcp);
          pPortMap.put(pPort, l);
        }
      }
    }
    else if (portInterface1 instanceof ClientServerInterface) {
      ClientServerInterface csinterface1 = (ClientServerInterface) portInterface1;
      ClientServerInterface csinterface2 = (ClientServerInterface) portInterface2;
      EList<ClientServerOperation> csOperations1 = csinterface1.getOperations();
      EList<ClientServerOperation> csOperations2 = csinterface2.getOperations();

      if ((csOperations1.size() == csOperations2.size()) && isCSOperationsSame(csOperations1, csOperations2)) {
        List<SwComponentPrototype> list = pPortMap.get(pPort);
        if (list != null) {
          list.add(swcp);
        }
        else {
          List<SwComponentPrototype> l = new ArrayList<>();
          l.add(swcp);
          pPortMap.put(pPort, l);
        }
      }
    }

    else if (portInterface1 instanceof TriggerInterface) {
      TriggerInterface tinterface1 = (TriggerInterface) portInterface1;
      TriggerInterface tinterface2 = (TriggerInterface) portInterface2;
      EList<Trigger> triggers1 = tinterface1.getTriggers();
      EList<Trigger> triggers2 = tinterface2.getTriggers();

      if ((triggers1.size() == triggers2.size()) && GenerateArxmlUtil.isTriggersSame(triggers1, triggers2)) {
        List<SwComponentPrototype> list = pPortMap.get(pPort);
        if (list != null) {
          list.add(swcp);
        }
        else {
          List<SwComponentPrototype> l = new ArrayList<>();
          l.add(swcp);
          pPortMap.put(pPort, l);
        }
      }
    }


    else if (portInterface1 instanceof ModeSwitchInterface) {
      ModeSwitchInterface msinterface1 = (ModeSwitchInterface) portInterface1;
      ModeSwitchInterface msinterface2 = (ModeSwitchInterface) portInterface2;

      if ((msinterface1.getModeGroup() != null) && (msinterface1.getModeGroup().getType() != null) &&
          (msinterface2.getModeGroup() != null) && (msinterface2.getModeGroup().getType() != null)) {
        EList<ModeDeclaration> modeDeclarations1 = msinterface1.getModeGroup().getType().getModeDeclarations();
        EList<ModeDeclaration> modeDeclarations2 = msinterface2.getModeGroup().getType().getModeDeclarations();


        if ((modeDeclarations1.size() == modeDeclarations2.size()) &&
            isModeDeclarationsSame(modeDeclarations1, modeDeclarations2)) {
          List<SwComponentPrototype> list = pPortMap.get(pPort);
          if (list != null) {
            list.add(swcp);
          }
          else {
            List<SwComponentPrototype> l = new ArrayList<>();
            l.add(swcp);
            pPortMap.put(pPort, l);
          }
        }

      }

    }
    else if (portInterface1.getShortName().equals(portInterface2.getShortName())) {
      List<SwComponentPrototype> list = pPortMap.get(pPort);
      if (list != null) {
        list.add(swcp);
      }
      else {
        List<SwComponentPrototype> l = new ArrayList<>();
        l.add(swcp);
        pPortMap.put(pPort, l);
      }

    }


  }


  private void updateRPortMap(final PortInterface portInterface1, final PortInterface portInterface2,
      final AbstractRequiredPortPrototype rPort, final SwComponentPrototype swcp,
      final Map<AbstractRequiredPortPrototype, List<SwComponentPrototype>> rPortMap) {
    if (portInterface1 instanceof SenderReceiverInterface) {
      SenderReceiverInterface srinterface1 = (SenderReceiverInterface) portInterface1;
      SenderReceiverInterface srinterface2 = (SenderReceiverInterface) portInterface2;

      EList<VariableDataPrototype> dataElements1 = srinterface1.getDataElements();
      EList<VariableDataPrototype> dataElements2 = srinterface2.getDataElements();

      if ((dataElements1.size() >= dataElements2.size()) && isDataElementsSame(dataElements2, dataElements1)) {

        List<SwComponentPrototype> list = rPortMap.get(rPort);
        if (list != null) {
          list.add(swcp);
        }
        else {
          List<SwComponentPrototype> l = new ArrayList<>();
          l.add(swcp);
          rPortMap.put(rPort, l);
        }


      }
    }
    else if (portInterface1 instanceof NvDataInterface) {
      NvDataInterface srinterface1 = (NvDataInterface) portInterface1;
      NvDataInterface srinterface2 = (NvDataInterface) portInterface2;

      EList<VariableDataPrototype> dataElements1 = srinterface1.getNvDatas();
      EList<VariableDataPrototype> dataElements2 = srinterface2.getNvDatas();

      if ((dataElements1.size() >= dataElements2.size()) && isDataElementsSame(dataElements2, dataElements1)) {
        List<SwComponentPrototype> list = rPortMap.get(rPort);
        if (list != null) {
          list.add(swcp);
        }
        else {
          List<SwComponentPrototype> l = new ArrayList<>();
          l.add(swcp);
          rPortMap.put(rPort, l);
        }
      }
    }
    else if (portInterface1 instanceof ClientServerInterface) {
      ClientServerInterface csinterface1 = (ClientServerInterface) portInterface1;
      ClientServerInterface csinterface2 = (ClientServerInterface) portInterface2;
      EList<ClientServerOperation> csOperations1 = csinterface1.getOperations();
      EList<ClientServerOperation> csOperations2 = csinterface2.getOperations();

      if ((csOperations1.size() == csOperations2.size()) && isCSOperationsSame(csOperations1, csOperations2)) {
        List<SwComponentPrototype> list = rPortMap.get(rPort);
        if (list != null) {
          list.add(swcp);
        }
        else {
          List<SwComponentPrototype> l = new ArrayList<>();
          l.add(swcp);
          rPortMap.put(rPort, l);
        }
      }
    }

    else if (portInterface1 instanceof TriggerInterface) {
      TriggerInterface tinterface1 = (TriggerInterface) portInterface1;
      TriggerInterface tinterface2 = (TriggerInterface) portInterface2;
      EList<Trigger> triggers1 = tinterface1.getTriggers();
      EList<Trigger> triggers2 = tinterface2.getTriggers();

      if ((triggers1.size() == triggers2.size()) && GenerateArxmlUtil.isTriggersSame(triggers1, triggers2)) {
        List<SwComponentPrototype> list = rPortMap.get(rPort);
        if (list != null) {
          list.add(swcp);
        }
        else {
          List<SwComponentPrototype> l = new ArrayList<>();
          l.add(swcp);
          rPortMap.put(rPort, l);
        }
      }
    }


    else if (portInterface1 instanceof ModeSwitchInterface) {
      ModeSwitchInterface msinterface1 = (ModeSwitchInterface) portInterface1;
      ModeSwitchInterface msinterface2 = (ModeSwitchInterface) portInterface2;

      if ((msinterface1.getModeGroup() != null) && (msinterface1.getModeGroup().getType() != null) &&
          (msinterface2.getModeGroup() != null) && (msinterface2.getModeGroup().getType() != null)) {
        EList<ModeDeclaration> modeDeclarations1 = msinterface1.getModeGroup().getType().getModeDeclarations();
        EList<ModeDeclaration> modeDeclarations2 = msinterface2.getModeGroup().getType().getModeDeclarations();

        if ((modeDeclarations1.size() == modeDeclarations2.size()) &&
            isModeDeclarationsSame(modeDeclarations1, modeDeclarations2)) {
          List<SwComponentPrototype> list = rPortMap.get(rPort);
          if (list != null) {
            list.add(swcp);
          }
          else {
            List<SwComponentPrototype> l = new ArrayList<>();
            l.add(swcp);
            rPortMap.put(rPort, l);
          }
        }

      }

    }
    else if (portInterface1.getShortName().equals(portInterface2.getShortName())) {
      List<SwComponentPrototype> list = rPortMap.get(rPort);
      if (list != null) {
        list.add(swcp);
      }
      else {
        List<SwComponentPrototype> l = new ArrayList<>();
        l.add(swcp);
        rPortMap.put(rPort, l);
      }

    }


  }


  private boolean isDataElementsSame(final EList<VariableDataPrototype> dataElements1,
      final EList<VariableDataPrototype> dataElements2) {

    boolean isSame = false;

    for (VariableDataPrototype vdp1 : dataElements1) {

      isSame = false;
      for (VariableDataPrototype vdp2 : dataElements2) {

        if (vdp1.getShortName().equals(vdp2.getShortName())) {
          isSame = true;
        }

      }

      if (!isSame) {
        return false;
      }

    }

    return isSame;
  }


  private boolean isCSOperationsSame(final EList<ClientServerOperation> csoperations1,
      final EList<ClientServerOperation> csoperations2) {

    boolean isSame = false;

    for (ClientServerOperation cs1 : csoperations1) {

      isSame = false;
      for (ClientServerOperation cs2 : csoperations2) {

        if (cs1.getShortName().equals(cs2.getShortName())) {
          isSame = true;
        }
      }

      if (!isSame) {
        return false;
      }
    }

    return isSame;
  }

  private boolean isModeDeclarationsSame(final EList<ModeDeclaration> mdg1, final EList<ModeDeclaration> mdg2) {

    boolean isSame = false;

    for (ModeDeclaration m1 : mdg1) {

      isSame = false;
      for (ModeDeclaration m2 : mdg2) {

        if (m1.getShortName().equals(m2.getShortName())) {
          isSame = true;
        }
      }

      if (!isSame) {
        return false;
      }
    }
    return isSame;
  }

  private Map<String, SwComponentPrototype> getSwCompositionComponentMap(
      final List<SwComponentPrototype> listOfSwComponentPrototypes) {

    Map<String, SwComponentPrototype> nameComponentMap = new HashMap<String, SwComponentPrototype>();

    for (SwComponentPrototype swc : listOfSwComponentPrototypes) {

      nameComponentMap.put(swc.getShortName(), swc);
    }
    return nameComponentMap;
  }


  private Map<String, SwcToEcuMapping> getSwComponentPrototypeEcuMap(final List<SwcToEcuMapping> swcToEcuMappings) {
    Map<String, SwcToEcuMapping> map = new HashMap<String, SwcToEcuMapping>();

    for (SwcToEcuMapping swcToEcuMapping : swcToEcuMappings) {

      for (ComponentInSystemInstanceRef swc : swcToEcuMapping.getComponents()) {

        if (map.get(swc.getTargetComponent().getShortName()) == null) {
          map.put(swc.getTargetComponent().getShortName(), swcToEcuMapping);
        }
      }
    }

    return map;
  }

  private Map<String, SwcToImplMapping> getSwComponentPrototypeImplMap(final List<SwcToImplMapping> swcToImplMappings) {
    Map<String, SwcToImplMapping> map = new HashMap<String, SwcToImplMapping>();

    for (SwcToImplMapping swcToEcuMapping : swcToImplMappings) {

      for (ComponentInSystemInstanceRef swc : swcToEcuMapping.getComponents()) {

        if (map.get(swc.getTargetComponent().getShortName()) == null) {
          map.put(swc.getTargetComponent().getShortName(), swcToEcuMapping);
        }
      }
    }

    return map;
  }

  private Map<String, SwComponentPrototype> getSwComponentPrototypeMap(
      final CompositionSwComponentType swCompositionSwComponentType2, final boolean merge) {
    Map<String, SwComponentPrototype> map = new HashMap<String, SwComponentPrototype>();

    for (SwComponentPrototype swc : swCompositionSwComponentType2.getComponents()) {

      if (map.get(swc.getShortName()) == null) {

        map.put(swc.getShortName(), swc);
      }
    }

    return map;
  }

  private Map<String, FlatInstanceDescriptor> getFlatInstanceDescriptorMap(final FlatMap flatMap) {
    Map<String, FlatInstanceDescriptor> map = new HashMap<String, FlatInstanceDescriptor>();

    for (FlatInstanceDescriptor fld : flatMap.getInstances()) {

      if (map.get(fld.getShortName()) == null) {
        map.put(fld.getShortName(), fld);
      }
    }

    return map;
  }


  private Map<String, SwConnector> getSwConnectorMap(final CompositionSwComponentType swCompositionSwComponentType2) {
    Map<String, SwConnector> map = new HashMap<String, SwConnector>();

    for (SwConnector swc : swCompositionSwComponentType2.getConnectors()) {

      if (map.get(swc.getShortName()) == null) {
        map.put(swc.getShortName(), swc);
      }

      if (swc instanceof AssemblySwConnector) {
        this.listOfAssemblySwConnectors.add((AssemblySwConnector) swc);
      }
    }

    return map;
  }

  private SwAddrMethod geSwAddressMethodReference(final SenderReceiverInterface srInterface, final PortPrototype port,
      final EList<FlatInstanceDescriptor> instances, final ARPackage swAddrMethodArPackage,
      final Map<String, SwAddrMethod> swAddrMethodMap)
      throws Exception {
    SwAddrMethod swAddrMethod = null;
    EList<VariableDataPrototype> dataElements = srInterface.getDataElements();
    if ((dataElements != null) && (dataElements.size() == 1)) {

      VariableDataPrototype vdp = dataElements.get(0);


      if ((vdp.getSwDataDefProps() != null) && !vdp.getSwDataDefProps().getSwDataDefPropsVariants().isEmpty() &&
          (vdp.getSwDataDefProps().getSwDataDefPropsVariants().get(0).getSwAddrMethod() != null)) {
        SwAddrMethod swAddrMethod2 = vdp.getSwDataDefProps().getSwDataDefPropsVariants().get(0).getSwAddrMethod();

        if (swAddrMethod2.eIsProxy()) {
          LOGGER.warn(RteConfGenMessageDescription
              .getFormattedMesssage("266_0", EcoreUtil.getURI(swAddrMethod2).toString(), vdp.getShortName()).trim());
          return null;
        }
        String osApplicationName = "";

        AdminData admindata = port.getAdminData();
        if (admindata != null) {
          if (admindata.getSdgs().get(0) != null) {
            EList<EObject> eContents = admindata.getSdgs().get(0).eContents();
            if (admindata.getSdgs().get(0).getGid().equals("RTEConfGen") && (eContents.get(0) instanceof SdgContents) &&
                (eContents.size() == 1)) {
              SdgContents sdvar = (SdgContents) eContents.get(0);
              Sd sd = sdvar.getSds().get(0);
              osApplicationName = sd.getValue();
            }
          }
        }

        if ((osApplicationName == null) || (osApplicationName.isEmpty())) {
          LOGGER.warn(
              RteConfGenMessageDescription.getFormattedMesssage("266_2", EcoreUtil.getURI(port).toString()).trim());
          return null;
        }


        swAddrMethod = swAddrMethodMap.get(swAddrMethod2.getShortName() + "_" + osApplicationName);
        if (swAddrMethod != null) {
          return swAddrMethod;
        }

        swAddrMethod = Autosar40Factory.eINSTANCE.createSwAddrMethod();
        swAddrMethod = EcoreUtil.copy(swAddrMethod2);
        swAddrMethod.setShortName(swAddrMethod2.getShortName() + "_" + osApplicationName);
        swAddrMethodArPackage.getElements().add(swAddrMethod);
        swAddrMethodMap.put(swAddrMethod2.getShortName() + "_" + osApplicationName, swAddrMethod);

      }
    }
    return swAddrMethod;

  }

  private static <T> Predicate<T> distinctByKey(final Function<? super T, Object> keyExtractor) {
    Map<Object, Boolean> map = new ConcurrentHashMap<>();
    return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }


  private FlatMap updateEcuFlatMapFile(final FlatMap originalFlatmap,
      final RootSwCompositionPrototype originalRootSwCompPrototype,
      final CompositionSwComponentType mmdcsCompoSwComponentType,
      final Map<String, FlatInstanceDescriptor> originalFDMap,
      final Map<String, SwComponentPrototype> mmdcsSwComponentPrototypeMaps, final ARPackage swAddrMethodArPackage,
      final AssemblySwConnectionUtil assemblySwConnUtil)
      throws Exception {


    FlatMap flatMap = FlatmapFactory.eINSTANCE.createFlatMap();
    flatMap.setShortName(originalFlatmap.getShortName());

    AutosarUtil.setCurrentProcessingEObject(originalFlatmap);

    TransactionalEditingDomain arEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(originalFlatmap.eResource());
    AbstractEMFOperation abstractEmfOperation =
        new AbstractEMFOperation(arEditingDomain, "Updating Ecu Flat map file") {


          @Override
          protected IStatus doExecute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {


            try {
              List<String> rteconfgenFIDList = new ArrayList<>();

              Map<String, List<String>> rPortToPPortsMap = assemblySwConnUtil.getrPortToPPortsMap();
              Map<String, List<String>> pPortToRPortsMap = assemblySwConnUtil.getpPortToRPortsMap();

              assemblySwConnUtil.setURIToPortMap(originalRootSwCompPrototype.getSoftwareComposition().getComponents());

              EList<FlatInstanceDescriptor> instances = originalFlatmap.getInstances();
              UpdateExcuExtractFiles.this.listOfFlatInstanceDescriptors.addAll(instances);


              EList<FlatInstanceDescriptor> copyInstances = flatMap.getInstances();
              List<String> rPorts = new ArrayList<String>();
              List<String> pPorts = new ArrayList<String>();

              Iterator<Entry<String, List<String>>> iterator = rPortToPPortsMap.entrySet().iterator();

              while (iterator.hasNext()) {
                Entry<String, List<String>> next = iterator.next();
                List<String> value = next.getValue();
                if ((value != null) && (value.size() > 1)) {
                  pPorts.addAll(value);
                }
              }

              Iterator<Entry<String, List<String>>> iterator1 = pPortToRPortsMap.entrySet().iterator();

              while (iterator1.hasNext()) {
                Entry<String, List<String>> next = iterator1.next();
                List<String> value = next.getValue();
                if ((value != null) && (value.size() > 1)) {
                  rPorts.addAll(value);
                }
              }

              Map<String, PortPrototype> uriToPortMap = assemblySwConnUtil.getUriToPortMap();

              Map<String, SwComponentPrototype> swCompositionComponentMap =
                  getSwCompositionComponentMap(originalRootSwCompPrototype.getSoftwareComposition().getComponents());
              Map<String, SwAddrMethod> swAddrMethodMap = new HashMap<>();

              for (SwComponentPrototype swcp1 : mmdcsCompoSwComponentType.getComponents()) {
                AutosarUtil.setCurrentProcessingEObject(swcp1);

                SwComponentPrototype swcp = swCompositionComponentMap.get(swcp1.getShortName());

                if ((originalFDMap.get(swcp.getShortName()) == null) &&
                    (originalFDMap.get(swcp.getShortName().startsWith("CPT_")
                        ? swcp.getShortName().substring("CPT_".length(), swcp.getShortName().length())
                        : swcp.getShortName()) == null)) {

                  FlatInstanceDescriptor createFlatInstanceDescriptor =
                      Autosar40Factory.eINSTANCE.createFlatInstanceDescriptor();

                  AnyInstanceRef upstreamReference = AnyinstancerefFactory.eINSTANCE.createAnyInstanceRef();

                  SwComponentPrototype swComponentPrototype2 = mmdcsSwComponentPrototypeMaps.get(swcp.getShortName());
                  if (swComponentPrototype2 != null) {
                    upstreamReference.setTarget(swComponentPrototype2);
                  }
                  createFlatInstanceDescriptor.setUpstreamReference(upstreamReference);


                  AnyInstanceRef ecuExtractReference = AnyinstancerefFactory.eINSTANCE.createAnyInstanceRef();
                  ecuExtractReference.getContextElements().add(originalRootSwCompPrototype);
                  ecuExtractReference.setTarget(swcp);
                  createFlatInstanceDescriptor.setEcuExtractReference(ecuExtractReference);
                  String shortName = swcp.getShortName();
                  shortName = shortName.startsWith("CPT_") ? shortName.substring("CPT_".length(), shortName.length())
                      : shortName;
                  shortName = GenerateArxmlUtil.getShortenedNameOfElements(shortName,
                      Collections.unmodifiableMap(originalFDMap));
                  createFlatInstanceDescriptor.setShortName(shortName);

                  VariationPoint variationPoint =
                      VariationPointUtil.getInstance().getVariationPoint(originalRootSwCompPrototype, swcp);

                  if (variationPoint != null) {
                    createFlatInstanceDescriptor.setVariationPoint(variationPoint);
                  }

                  createFlatInstanceDescriptor.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());

                  originalFDMap.put(shortName, createFlatInstanceDescriptor);

                  SwComponentType type = swcp.getType();

                  EList<PortPrototype> ports = type.getPorts();


                  for (PortPrototype port : ports) {

                    boolean bSwAddressReference = true;
                    AutosarUtil.setCurrentProcessingEObject(port);
                    if (port instanceof AbstractRequiredPortPrototype) {

                      if (!rPorts
                          .contains(swcp.getShortName() + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(port))) {
                        List<String> list = rPortToPPortsMap.get(swcp.getShortName() + port.getShortName());
                        if ((list == null) || list.isEmpty() || (list.size() > 1)) {

                          AbstractRequiredPortPrototype rPort = (AbstractRequiredPortPrototype) port;
                          PortInterface requiredInterface = rPort instanceof PRPortPrototype
                              ? ((PRPortPrototype) rPort).getProvidedRequiredInterface()
                              : ((RPortPrototype) rPort).getRequiredInterface();
                          if (requiredInterface instanceof SenderReceiverInterface) {
                            createFlatInstanceDescriptor((SenderReceiverInterface) requiredInterface, swcp,
                                originalFDMap, rteconfgenFIDList, rPort, mmdcsSwComponentPrototypeMaps, instances,
                                copyInstances, swAddrMethodArPackage, swAddrMethodMap, bSwAddressReference);
                          }
                        }
                        else if ((list.size() == 1)) {
                          bSwAddressReference = false;
                          String portURI = list.get(0).split("_PortPrototype_")[1];
                          PortPrototype pPort = uriToPortMap.get(portURI);
                          if (pPort != null) {
                            AbstractRequiredPortPrototype rPort = (AbstractRequiredPortPrototype) port;
                            PortInterface requiredInterface = rPort instanceof PRPortPrototype
                                ? ((PRPortPrototype) rPort).getProvidedRequiredInterface()
                                : ((RPortPrototype) rPort).getRequiredInterface();
                            PortInterface providedInterface = pPort instanceof PRPortPrototype
                                ? ((PRPortPrototype) pPort).getProvidedRequiredInterface()
                                : ((PPortPrototype) pPort).getProvidedInterface();
                            if ((providedInterface instanceof SenderReceiverInterface) &&
                                (requiredInterface instanceof SenderReceiverInterface)) {
                              SwComponentPrototype swcpFromAR =
                                  swCompositionComponentMap.get(list.get(0).split("_PortPrototype_")[0]);
                              createFlatInstanceDescriptor((SenderReceiverInterface) providedInterface, swcpFromAR,
                                  originalFDMap, rteconfgenFIDList, pPort, mmdcsSwComponentPrototypeMaps, instances,
                                  copyInstances, swAddrMethodArPackage, swAddrMethodMap, bSwAddressReference,
                                  (SenderReceiverInterface) requiredInterface);
                            }
                          }
                          else {
                            LOGGER.warn(RteConfGenMessageDescription
                                .getFormattedMesssage("260_9", portURI, GenerateArxmlUtil.getFragmentURI(port)).trim());
                          }
                        }
                      }
                      else {
                        bSwAddressReference = false;
                        List<String> pPortlist = rPortToPPortsMap.get(swcp.getShortName() + port.getShortName());

                        if ((pPortlist != null) && !pPortlist.isEmpty() && (pPortlist.size() == 1)) {

                          String portURI = pPortlist.get(0).split("_PortPrototype_")[1];

                          PortPrototype pPort = uriToPortMap.get(portURI);
                          if (pPort != null) {
                            AbstractRequiredPortPrototype rPort = (AbstractRequiredPortPrototype) port;
                            PortInterface requiredInterface = rPort instanceof PRPortPrototype
                                ? ((PRPortPrototype) rPort).getProvidedRequiredInterface()
                                : ((RPortPrototype) rPort).getRequiredInterface();
                            PortInterface providedInterface = pPort instanceof PRPortPrototype
                                ? ((PRPortPrototype) pPort).getProvidedRequiredInterface()
                                : ((PPortPrototype) pPort).getProvidedInterface();
                            if ((providedInterface instanceof SenderReceiverInterface) &&
                                (requiredInterface instanceof SenderReceiverInterface)) {
                              SwComponentPrototype swcpFromAR =
                                  swCompositionComponentMap.get(pPortlist.get(0).split("_PortPrototype_")[0]);
                              createFlatInstanceDescriptor((SenderReceiverInterface) providedInterface, swcpFromAR,
                                  originalFDMap, rteconfgenFIDList, pPort, mmdcsSwComponentPrototypeMaps, instances,
                                  copyInstances, swAddrMethodArPackage, swAddrMethodMap, bSwAddressReference,
                                  (SenderReceiverInterface) requiredInterface);
                            }
                          }
                          else {
                            LOGGER.warn(RteConfGenMessageDescription
                                .getFormattedMesssage("260_9", portURI, GenerateArxmlUtil.getFragmentURI(port)).trim());
                          }
                        }
                      }
                    }
                    if ((port instanceof AbstractProvidedPortPrototype)) {
                      if (!pPorts
                          .contains(swcp.getShortName() + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(port))) {
                        AbstractProvidedPortPrototype pPort = (AbstractProvidedPortPrototype) port;
                        PortInterface providedInterface =
                            pPort instanceof PRPortPrototype ? ((PRPortPrototype) pPort).getProvidedRequiredInterface()
                                : ((PPortPrototype) pPort).getProvidedInterface();
                        if (providedInterface instanceof SenderReceiverInterface) {
                          createFlatInstanceDescriptor((SenderReceiverInterface) providedInterface, swcp, originalFDMap,
                              rteconfgenFIDList, pPort, mmdcsSwComponentPrototypeMaps, instances, copyInstances,
                              swAddrMethodArPackage, swAddrMethodMap, bSwAddressReference);
                        }
                      }
                      else {
                        bSwAddressReference = false;
                        List<String> rPortlist = pPortToRPortsMap.get(swcp.getShortName() + port.getShortName());

                        if ((rPortlist != null) && !rPortlist.isEmpty() && (rPortlist.size() == 1)) {
                          String portURI = rPortlist.get(0).split("_PortPrototype_")[1];
                          PortPrototype rPort = uriToPortMap.get(portURI);
                          if (rPort != null) {
                            AbstractProvidedPortPrototype pPort = (AbstractProvidedPortPrototype) port;
                            PortInterface providedInterface = pPort instanceof PRPortPrototype
                                ? ((PRPortPrototype) pPort).getProvidedRequiredInterface()
                                : ((PPortPrototype) pPort).getProvidedInterface();
                            PortInterface requiredInterface = rPort instanceof PRPortPrototype
                                ? ((PRPortPrototype) rPort).getProvidedRequiredInterface()
                                : ((RPortPrototype) rPort).getRequiredInterface();
                            if ((requiredInterface instanceof SenderReceiverInterface) &&
                                (providedInterface instanceof SenderReceiverInterface)) {
                              SwComponentPrototype swcpFromAR =
                                  swCompositionComponentMap.get(rPortlist.get(0).split("_PortPrototype_")[0]);
                              createFlatInstanceDescriptor((SenderReceiverInterface) requiredInterface, swcpFromAR,
                                  originalFDMap, rteconfgenFIDList, rPort, mmdcsSwComponentPrototypeMaps, instances,
                                  copyInstances, swAddrMethodArPackage, swAddrMethodMap, bSwAddressReference,
                                  (SenderReceiverInterface) providedInterface);
                            }
                          }
                          else {
                            LOGGER.warn(RteConfGenMessageDescription
                                .getFormattedMesssage("260_9", portURI, GenerateArxmlUtil.getFragmentURI(port)).trim());
                          }
                        }
                      }
                    }
                  }
                }
                else {
                  LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("230_0", swcp.getShortName()).trim());

                }
              }

            }
            catch (Exception ex) {
              try {
                LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                    .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());
              }
              catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }

            }

            return Status.OK_STATUS;
          }

        };

    try {
      WorkspaceTransactionUtil.getOperationHistory(arEditingDomain).execute(abstractEmfOperation,
          new NullProgressMonitor(), null);
    }
    catch (ExecutionException e) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());

    }
    UpdateExcuExtractFiles.this.listOfFlatInstanceDescriptors.addAll(flatMap.getInstances());
    return flatMap;
  }

  private void createFlatInstanceDescriptor(final SenderReceiverInterface srInterface, final SwComponentPrototype swcp,
      final Map<String, FlatInstanceDescriptor> flatInstanceDescriptorMap, final List<String> rteconfgenFIDList,
      final PortPrototype port, final Map<String, SwComponentPrototype> swComponentPrototypeMap,
      final EList<FlatInstanceDescriptor> instances, final EList<FlatInstanceDescriptor> copyInstances,
      final ARPackage swAddrMethodArPackage, final Map<String, SwAddrMethod> swAddrMethodMap,
      final boolean bSwAddressReference, final EObject... eObject)
      throws Exception {

    EList<VariableDataPrototype> dataElements = srInterface.getDataElements();
    if ((dataElements != null) && (dataElements.size() == 1)) {
      SenderReceiverInterface srinterfaceForShortName = null;
      srinterfaceForShortName = (eObject != null) && (eObject.length == 1) && (eObject[0] != null) &&
          (eObject[0] instanceof SenderReceiverInterface) ? (SenderReceiverInterface) eObject[0] : null;
      VariableDataPrototype vdp = dataElements.get(0);

      StringBuilder shortName = new StringBuilder();

      boolean bExistingFID = checkIfFIDExists(srInterface, srinterfaceForShortName, vdp, swcp,
          flatInstanceDescriptorMap, rteconfgenFIDList, port, shortName, eObject);
      if (!bExistingFID && (shortName.length() != 0)) {

        FlatInstanceDescriptor createFlatInstanceDescriptor = Autosar40Factory.eINSTANCE.createFlatInstanceDescriptor();

        SwAddrMethod referredswaddr = null;
        SwDataDefPropsConditional createSwDataDefPropsConditional =
            Autosar40Factory.eINSTANCE.createSwDataDefPropsConditional();
        if ((swAddrMethodArPackage != null) && bSwAddressReference) {
          referredswaddr =
              geSwAddressMethodReference(srInterface, port, instances, swAddrMethodArPackage, swAddrMethodMap);
          if (referredswaddr != null) {
            createSwDataDefPropsConditional.setSwAddrMethod(referredswaddr);
          }
          else {
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("266_1", port.getShortName()).trim());
          }
        }


        MultidimensionalTime swRefreshTiming = null;
        swRefreshTiming = getSWRefreshTiming(vdp);
        if (swRefreshTiming != null) {
          createSwDataDefPropsConditional.setSwRefreshTiming(swRefreshTiming);
        }

        if ((referredswaddr != null) || (swRefreshTiming != null)) {
          SwDataDefProps createSwDataDefProps = Autosar40Factory.eINSTANCE.createSwDataDefProps();
          createSwDataDefProps.getSwDataDefPropsVariants().add(createSwDataDefPropsConditional);
          createFlatInstanceDescriptor.setSwDataDefProps(createSwDataDefProps);
        }


        AnyInstanceRef upstreamReference = AnyinstancerefFactory.eINSTANCE.createAnyInstanceRef();
        SwComponentPrototype swComponentPrototype2 = swComponentPrototypeMap.get(swcp.getShortName());
        if (swComponentPrototype2 != null) {
          upstreamReference.getContextElements().add(swComponentPrototype2);
          upstreamReference.getContextElements().add(port);
          upstreamReference.setTarget(vdp);
          createFlatInstanceDescriptor.setUpstreamReference(upstreamReference);
        }

        AnyInstanceRef ecuExtractReference = AnyinstancerefFactory.eINSTANCE.createAnyInstanceRef();
        ecuExtractReference.getContextElements().add(this.rootSwCompositionPrototype);
        ecuExtractReference.getContextElements().add(swcp);
        ecuExtractReference.getContextElements().add(port);
        ecuExtractReference.setTarget(vdp);
        createFlatInstanceDescriptor.setEcuExtractReference(ecuExtractReference);

        VariationPoint variationPoint =
            VariationPointUtil.getInstance().getVariationPoint(this.rootSwCompositionPrototype, swcp);

        if (variationPoint != null) {
          createFlatInstanceDescriptor.setVariationPoint(variationPoint);
        }


        String shortNameString = GenerateArxmlUtil.getShortenedNameOfElements(shortName.toString(),
            Collections.unmodifiableMap(flatInstanceDescriptorMap));
        createFlatInstanceDescriptor.setShortName(shortNameString);

        if (this.osConfigInstance.isValidateTimedCom() && this.timedComVDP.contains(vdp)) {
          addRtePliginPropsToFlatInstanceDes(createFlatInstanceDescriptor, vdp);

        }

        createFlatInstanceDescriptor.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
        instances.add(createFlatInstanceDescriptor);
        copyInstances.add(EcoreUtil.copy(createFlatInstanceDescriptor));
        flatInstanceDescriptorMap.put(shortNameString, createFlatInstanceDescriptor);
        rteconfgenFIDList.add(shortNameString);

      }
    }
  }


  /**
   * @param flatInstanceDescriptorMap
   * @param swcp
   * @param srInterface
   * @param srinterfaceForShortName
   * @param vdp
   * @param rteconfgenFIDList
   * @param port
   * @param shortName
   * @param eObject
   * @return
   * @throws Exception
   */
  private boolean checkIfFIDExists(final SenderReceiverInterface srInterface,
      final SenderReceiverInterface srinterfaceForShortName, final VariableDataPrototype vdp,
      final SwComponentPrototype swcp, final Map<String, FlatInstanceDescriptor> flatInstanceDescriptorMap,
      final List<String> rteconfgenFIDList, final PortPrototype port, final StringBuilder shortName,
      final EObject[] eObject)
      throws Exception {

    int cnt = 0;
    String tempshortname = null;
    boolean bExistingFID = false;
    if (srinterfaceForShortName != null) {
      EList<VariableDataPrototype> dataElements1 = srinterfaceForShortName.getDataElements();
      tempshortname =
          ((dataElements1 != null) && (dataElements1.size() == 1)) ? dataElements1.get(0).getShortName() : null;
    }
    else {
      tempshortname = vdp.getShortName();
    }

    if (tempshortname != null) {
      tempshortname = tempshortname.startsWith("VDP_")
          ? tempshortname.substring("VDP_".length(), tempshortname.length()) : tempshortname;
      while (flatInstanceDescriptorMap.containsKey(tempshortname)) {
        FlatInstanceDescriptor existingFID = flatInstanceDescriptorMap.get(tempshortname);
        SwComponentPrototype existingswcp = null;
        PortPrototype existingport = null;
        VariableDataPrototype existingvdp = null;
        if (existingFID != null) {
          EList<AtpFeature> contextElements = existingFID.getEcuExtractReference().getContextElements();
          for (AtpFeature apt : contextElements) {
            if (apt instanceof SwComponentPrototype) {
              existingswcp = (SwComponentPrototype) apt;
            }
            else if (apt instanceof PortPrototype) {
              existingport = (PortPrototype) apt;
            }
            else if (apt instanceof VariableDataPrototype) {
              existingvdp = (VariableDataPrototype) apt;
            }
          }
        }

        bExistingFID = (existingswcp != null) && (existingport != null) &&
            existingswcp.getShortName().equals(swcp.getShortName()) &&
            existingport.getShortName().equals(port.getShortName())
                ? existingvdp != null ? existingvdp.getShortName().equals(vdp.getShortName()) ? true : false : true
                : false;
        if (bExistingFID) {
          if (!rteconfgenFIDList.contains(tempshortname)) {
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("229_0", port.getShortName()).trim());
          }
          break;
        }
        tempshortname = (tempshortname.endsWith("MSR") || tempshortname.endsWith("AR")) ? tempshortname + "_" + ++cnt
            : tempshortname + "MSR";
      }


    }
    if (tempshortname != null) {
      shortName.append(tempshortname);
    }
    return bExistingFID;
  }


  /**
   * @param vdp
   * @return
   */
  private MultidimensionalTime getSWRefreshTiming(final VariableDataPrototype vdp) {
    MultidimensionalTime swRefreshTiming = null;
    if ((vdp.getSwDataDefProps() != null) && (vdp.getSwDataDefProps().getSwDataDefPropsVariants() != null) &&
        !vdp.getSwDataDefProps().getSwDataDefPropsVariants().isEmpty()) {
      swRefreshTiming = vdp.getSwDataDefProps().getSwDataDefPropsVariants().get(0).getSwRefreshTiming();
    }

    if (swRefreshTiming == null) {
      ApplicationDataType apt = (ApplicationDataType) vdp.getType();
      if (apt.getSwDataDefProps() != null) {
        EList<SwDataDefPropsConditional> swDataDefPropsVariants = apt.getSwDataDefProps().getSwDataDefPropsVariants();

        if ((swDataDefPropsVariants != null) && !swDataDefPropsVariants.isEmpty()) {
          swRefreshTiming = swDataDefPropsVariants.get(0).getSwRefreshTiming();

        }
      }
    }
    return swRefreshTiming;
  }

  /**
   * @param createFlatInstanceDescriptor
   * @param vdp
   * @throws Exception
   */
  private void addRtePliginPropsToFlatInstanceDes(final FlatInstanceDescriptor createFlatInstanceDescriptor,
      final VariableDataPrototype vdp)
      throws Exception {

    RtePluginProps rtePluginPropsInstance = Autosar40Factory.eINSTANCE.createRtePluginProps();

    EcucContainerValue value = getImplTimedComRtePlugin();
    if (value != null) {
      rtePluginPropsInstance.setAssociatedRtePlugin(value);

    }
    else {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("240_0", vdp.getShortName()).trim());

    }
    createFlatInstanceDescriptor.setRtePluginProps(rtePluginPropsInstance);
  }

  /**
   * @return
   */
  private EcucContainerValue getImplTimedComRtePlugin() {
    for (EcucContainerValue containers : this.timedcomECUCConfigValue.getContainers()) {
      if (containers.getDefinition().getShortName().equalsIgnoreCase("RteRipsPluginProps")) {
        return containers;
      }
    }
    return null;
  }


  /**
   * @return Map<SwComponentPrototype, Map<String, List<VariableAccess>>>
   */
  public Map<SwComponentPrototype, Map<String, List<VariableAccess>>> getTimedComSWCPvariablesAccessMap() {

    if (this.swcpTimedVariableAccessMap == null) {
      this.swcpTimedVariableAccessMap = new HashMap<>();
    }
    return this.swcpTimedVariableAccessMap;
  }


  /**
   * @return List<FlatInstanceDescriptor>
   */
  public List<FlatInstanceDescriptor> getFlatInstanceDescriptors() {
    return this.listOfFlatInstanceDescriptors;
  }


  /**
   * @return List<AssemblySwConnector>
   */
  public List<AssemblySwConnector> getAssemblySwConnectors() {
    return this.listOfAssemblySwConnectors;
  }


}
