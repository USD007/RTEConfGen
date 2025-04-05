/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.rtememmap.xpt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.Logger;
import org.artop.aal.common.metamodel.AutosarReleaseDescriptor;
import org.artop.aal.workspace.preferences.IAutosarWorkspacePreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;

import com.bosch.tisched.rteconfig.generator.rtememmap.xpt.TARGETS.TARGET;
import com.bosch.tisched.rteconfig.generator.rtememmap.xpt.TARGETS.TARGET.SECTIONS.SECTION;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;

import autosar40.bswmodule.bswbehavior.BswTimingEvent;
import autosar40.bswmodule.bswoverview.BswModuleDescription;
import autosar40.commonstructure.flatmap.FlatInstanceDescriptor;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucInstanceReferenceValue;
import autosar40.ecucdescription.EcucReferenceValue;
import autosar40.swcomponent.components.AbstractProvidedPortPrototype;
import autosar40.swcomponent.components.AbstractRequiredPortPrototype;
import autosar40.swcomponent.components.ApplicationSwComponentType;
import autosar40.swcomponent.components.AtomicSwComponentType;
import autosar40.swcomponent.components.ComplexDeviceDriverSwComponentType;
import autosar40.swcomponent.components.EcuAbstractionSwComponentType;
import autosar40.swcomponent.components.NvBlockSwComponentType;
import autosar40.swcomponent.components.PPortPrototype;
import autosar40.swcomponent.components.ParameterSwComponentType;
import autosar40.swcomponent.components.RPortPrototype;
import autosar40.swcomponent.components.SensorActuatorSwComponentType;
import autosar40.swcomponent.components.ServiceSwComponentType;
import autosar40.swcomponent.components.SwComponentType;
import autosar40.swcomponent.composition.AssemblySwConnector;
import autosar40.swcomponent.composition.CompositionSwComponentType;
import autosar40.swcomponent.composition.SwComponentPrototype;
import autosar40.swcomponent.composition.SwConnector;
import autosar40.swcomponent.composition.instancerefs.PPortInCompositionInstanceRef;
import autosar40.swcomponent.composition.instancerefs.RPortInCompositionInstanceRef;
import autosar40.swcomponent.nvblockcomponent.NvBlockDescriptor;
import autosar40.swcomponent.portinterface.ClientServerInterface;
import autosar40.swcomponent.portinterface.ClientServerOperation;
import autosar40.swcomponent.portinterface.PortInterface;
import autosar40.swcomponent.swcinternalbehavior.RunnableEntity;
import autosar40.swcomponent.swcinternalbehavior.SwcInternalBehavior;
import autosar40.swcomponent.swcinternalbehavior.rteevents.DataReceivedEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.OperationInvokedEvent;
import autosar40.swcomponent.swcinternalbehavior.rteevents.RTEEvent;
import autosar40.swcomponent.swcinternalbehavior.servercall.AsynchronousServerCallPoint;
import autosar40.system.RootSwCompositionPrototype;
/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */


/**
 * @author DTB1KOR
 */
public class GenerateRteMemmapXptFile {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(GenerateRteMemmapXptFile.class.getName());

  private final IProject project;

  private final Map<String, String> props;

  private final Map<EcucContainerValue, List<RTEEvent>> tasktoRteEventsMap = new HashMap<>();
  private final Map<EcucContainerValue, List<BswTimingEvent>> tasktoBSWEventsMap = new HashMap<>();

  private final Map<EcucContainerValue, List<EcucContainerValue>> osApptoTasksMap = new HashMap<>();
  private final Map<EcucContainerValue, EcucContainerValue> osApptoEcucParitionsMap = new HashMap<>();
  private final Map<EcucContainerValue, List<SwComponentPrototype>> ecucParitiontoSwpsMap = new HashMap<>();
  private final Map<RTEEvent, EcucContainerValue> rteEventToTaskMap = new HashMap<>();
  private final Map<String, EcucContainerValue> rteEventURIToTaskMap = new HashMap<>();
  private final Map<AsynchronousServerCallPoint, List<RTEEvent>> asctoRteEventsMap = new HashMap<>();
  private final Map<AsynchronousServerCallPoint, SwConnector> asctoAscMap = new HashMap<>();
  private final Map<PPortPrototype, List<RTEEvent>> pPortToEvents = new HashMap<>();
  private final List<AsynchronousServerCallPoint> requiredASCList = new ArrayList<>();


  private final Map<SwComponentType, List<SwComponentPrototype>> swtToswpMap = new HashMap<>();
  private final Map<SwComponentType, List<SwComponentPrototype>> swtToswpMap1 = new HashMap<>();
  private final Map<SwComponentType, List<SwComponentPrototype>> swtToswpMap2 = new HashMap<>();
  private final Map<SwComponentType, List<SwComponentPrototype>> swtToswpMap3 = new HashMap<>();


  private final Map<SwComponentPrototype, SwComponentType> swcpToSwcMap = new HashMap<>();
  private final Map<SwComponentPrototype, SwComponentType> swcpToSwcMap1 = new HashMap<>();
  private final Map<SwComponentPrototype, SwComponentType> swcpToSwcMapTaskWise = new HashMap<>();
  private final Map<SwComponentPrototype, SwComponentType> swcpToswcMapNV = new HashMap<>();
  private final Map<SwComponentPrototype, SwComponentType> swcpToswcMapParam = new HashMap<>();

  private final Map<FlatInstanceDescriptor, SwComponentPrototype> fldToswpMap = new HashMap<>();
  private final Map<FlatInstanceDescriptor, SwComponentPrototype> fldToswpMap1 = new HashMap<>();
  private final Map<FlatInstanceDescriptor, SwComponentPrototype> fldToswpMap2 = new HashMap<>();
  private final Map<FlatInstanceDescriptor, SwComponentPrototype> fldToswpMap3 = new HashMap<>();
  private final Map<FlatInstanceDescriptor, SwComponentPrototype> fldToswpMapTaskWise = new HashMap<>();


  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoTasksMap1 = new HashMap<>();
  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoEcucPartionsMap1 = new HashMap<>();
  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoOsAppsMap1 = new HashMap<>();
  private final Map<SwComponentPrototype, List<RTEEvent>> swptoDREMap1 = new HashMap<>();
  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoTasksMapTaskWise = new HashMap<>();
  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoOsAppsMapTaskWise = new HashMap<>();

  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoEcucPartionsMap2 = new HashMap<>();
  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoOsAppsMap2 = new HashMap<>();

  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoTasksMap3 = new HashMap<>();
  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoEcucPartionsMap3 = new HashMap<>();
  private final Map<SwComponentPrototype, List<EcucContainerValue>> swptoOsAppsMap3 = new HashMap<>();
  private final Map<SwComponentPrototype, List<RTEEvent>> swptoDREMap3 = new HashMap<>();

  private final Map<BswModuleDescription, List<EcucContainerValue>> bswtoTasksMap1 = new HashMap<>();
  private final Map<BswModuleDescription, List<EcucContainerValue>> bswtoOsAppsMap1 = new HashMap<>();


  private final RootSwCompositionPrototype rootCompostion;
  private final AutosarReleaseDescriptor autosarRelease;
  private final TransactionalEditingDomain editingDomain;
  private final String machineFamilyName;
  private final String rteconfigDir;
  private TARGET target = null;
  private final String memmapXptOutputLstPath;
  private final boolean allocateTaskAsPerOSApp;
  private final boolean addSwcAsPerOsAppWise;
  private final String deviceName;
  private final String xptFilePath;
  private final List<BswModuleDescription> listOfBswmd;
  private final String startsectionpath;
  private final String endsectionpath;
  private int RTARTEVersion = 0;


  /**
   * @param createAutosarProject
   * @param props
   * @param osConfigToEcucValueMapping
   */
  public GenerateRteMemmapXptFile(final IProject project, final Map<String, String> props,
      final RootSwCompositionPrototype rootCompostion) {

    this.project = project;
    this.props = props;
    this.rootCompostion = rootCompostion;
    this.autosarRelease = IAutosarWorkspacePreferences.AUTOSAR_RELEASE.get(this.project);
    this.editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(this.project, this.autosarRelease);
    this.machineFamilyName = props.get(RteConfigGeneratorConstants.MACHINE_FAMILY);
    this.rteconfigDir = props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR).replaceFirst("/", "");
    this.memmapXptOutputLstPath = props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST);

    this.deviceName = this.machineFamilyName.contains("IFX") ? "ifx"
        : this.machineFamilyName.contains("JDP") ? "jdp" : this.machineFamilyName.contains("NXP") ? "nxp" : "st";

    this.xptFilePath = this.rteconfigDir + "/rteconfgencustomdynamicpragmaconfig_" + this.deviceName + ".xpt";
    this.listOfBswmd = GenerateArxmlUtil.getListOfEObject(this.project, BswModuleDescription.class, "");
    this.allocateTaskAsPerOSApp =
        props.get(RteConfigGeneratorConstants.ALLOCATETASKASPEROSAPP).equals("y") ? true : false;
    this.addSwcAsPerOsAppWise = props.get(RteConfigGeneratorConstants.ADDSWCASPEROSAPP).equals("y") ? true : false;
    this.startsectionpath =
        this.project.getLocation().toOSString() + "/" + this.props.get(RteConfigGeneratorConstants.STARTSECTIONPATH);
    this.endsectionpath =
        this.project.getLocation().toOSString() + "/" + this.props.get(RteConfigGeneratorConstants.ENDSECTIONPATH);
    setRTARTEVersion(this.props.get(RteConfigGeneratorConstants.RTARTE_VERSION));
  }

  /**
   * @param string
   */
  private void setRTARTEVersion(final String stringVersion) {
    String[] split = stringVersion.split(".");
    if ((split.length != 0)) {
      try {
        Integer integer = Integer.getInteger(split[0]);
        this.RTARTEVersion = integer.intValue();
      }
      catch (NumberFormatException n) {
        LOGGER.warn("Invalid RTE Version passed");
      }
    }

  }

  /**
   * @param target
   * @throws Exception
   */
  public void generateRteMemmapXptFile() throws Exception {
    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("191_0"));
    List<String> xptLines = new ArrayList<>();
    xptLines
        .add("«REM»*** Generated by RTEConfGen version: " + RteConfigGeneratorConstants.TOOL_VERSION + " ***«ENDREM»");

    this.target = loadXmlFile();
    if (this.target != null) {

      File startsection = new File(this.startsectionpath);
      File endsection = new File(this.endsectionpath);
      if ((startsection.length() == 0L) || (endsection.length() == 0L)) {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("265_0"));
        generateDefaultStartXPTLines();
        generateDefaultEndXPTLines();
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("301_24", this.rteconfigDir));
      }
      prepareMemmapXptModel();
      xptLines.addAll(Files.readAllLines(startsection.toPath()));

      xptForTasks(xptLines);
      xptForImplicitBuffersOfTasks(xptLines);
      xptForTaskWiseSwcs(xptLines);
      xptForSwComponentPrototypes(xptLines);
      xptForDataReceivedEvents(xptLines);
      xptForTaskWiseMAKWSwcs(xptLines);
      xptForNotTasKWiseMAKWSwc(xptLines);
      xptForServerActivationFlags(xptLines);
      xptForClientAndServerOSCBK(xptLines);
      xptForOsAppMemoryAllocation(xptLines);
      xptForNvBlockRAMROM(xptLines);
      xptForNvBlockDRE(xptLines);
      xptFornvBlockCodes(xptLines);
      xptForBswmdCode(xptLines);
      xptForParamSwComponents(xptLines);
      xptForGeneralKeywords(xptLines);

      xptLines.addAll(Files.readAllLines(endsection.toPath()));

      writeDataToXPT(xptLines);
    }

  }


  /**
   * @throws Exception
   */
  private void generateDefaultStartXPTLines() throws Exception {
    String sampleStartSecPath =
        this.project.getLocation().toOSString() + "/" + this.rteconfigDir + "/sample_memmapXPT_startsection.txt";
    List<String> defStartLines = new ArrayList<>();
    Map<String, String> varForXpt = new HashMap<String, String>();
    getFormattedBuffer(MemmapXptConstants.STATICSTARTSECTION, MemmapXptConstants.CODE, varForXpt, defStartLines);
    getFormattedBuffer(MemmapXptConstants.GENERALSEC21119, MemmapXptConstants.CODE, varForXpt, defStartLines);

    writeDefaultLines(sampleStartSecPath, defStartLines);
  }


  /**
   * @throws Exception
   */
  private void generateDefaultEndXPTLines() throws Exception {
    String sampleEndSecPath =
        this.project.getLocation().toOSString() + "/" + this.rteconfigDir + "/sample_memmapXPT_endsection.txt";
    List<String> defEndLines = new ArrayList<>();
    Map<String, String> varForXpt = new HashMap<String, String>();

    defEndLines.add(" ");
    getFormattedBuffer(MemmapXptConstants.DEFAULTCODE, MemmapXptConstants.CODE, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.RIPSCODE, MemmapXptConstants.CODE, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.RIPSCALLOUTCODE, MemmapXptConstants.CODE, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.INITDATA, MemmapXptConstants.VARINIT, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.VARCLEAREDDATA, MemmapXptConstants.VARCLEARED, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.CALIBDATA, MemmapXptConstants.CALIB, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.CONSTDATA, MemmapXptConstants.CONST, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.RTECOMCALLBACKS, MemmapXptConstants.CODE, varForXpt, defEndLines);
    // getFormattedBuffer(MemmapXptConstants.RTEVARSAVEDZONE, MemmapXptConstants.VARSAVEDZONE, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.VARCLRECUCANDSWC, MemmapXptConstants.VARCLEARED, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.UNMAPDRE, MemmapXptConstants.CODE, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.NULLALARMCALLBACK, MemmapXptConstants.CODE, varForXpt, defEndLines);
    getFormattedBuffer(MemmapXptConstants.GENERICEND21120, MemmapXptConstants.VARINIT, varForXpt, defEndLines);
    defEndLines.add("«ENDDEFINE»");
    writeDefaultLines(sampleEndSecPath, defEndLines);
  }

  /**
   * @param sampleStartSecPath
   * @param defStartLines
   */
  private void writeDefaultLines(final String filePath, final List<String> defLines) {
    try (BufferedWriter bw =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), StandardCharsets.UTF_8))) {

      for (String value : defLines) {
        bw.write(value);
        bw.newLine();
      }
      bw.flush();
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("192_0", filePath));

    }
    catch (Exception ex) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER, ex.getLocalizedMessage());
    }
  }


  /**
   * @return
   * @throws Exception
   */
  private TARGET loadXmlFile() throws Exception {
    try {
      File file = new File(this.project.getLocation().toOSString() + "//" +
          this.props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_CONFIG_FILEPATH));
      JAXBContext jaxbContext;

      jaxbContext = JAXBContext.newInstance(TARGETS.class);

      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      TARGETS targets = (TARGETS) jaxbUnmarshaller.unmarshal(file);
      List<TARGET> targetList = targets.getTARGET();
      List<TARGET> collect = targetList.stream()
          .filter(t -> t.getSHORTNAME().equals(this.props.get(RteConfigGeneratorConstants.MACHINE_FAMILY)))
          .collect(Collectors.toList());
      if (collect.size() == 1) {
        return collect.get(0);
      }
      throw new Exception("Invalid config File");
    }
    catch (JAXBException e) {
      e.printStackTrace();
    }

    return this.target;

  }

  private void xptForParamSwComponents(final List<String> xptLines) throws Exception {
    for (Entry<SwComponentPrototype, SwComponentType> e : this.swcpToswcMapParam.entrySet()) {
      HashMap<String, String> varForXpt = new HashMap<String, String>();

      Optional<Entry<FlatInstanceDescriptor, SwComponentPrototype>> findFirst = this.fldToswpMap2.entrySet().stream()
          .filter(e1 -> GenerateArxmlUtil.isSameElement(e.getKey(), e1.getValue())).findFirst();

      if (findFirst.isPresent()) {
        FlatInstanceDescriptor flatInstanceDescriptor = findFirst.get().getKey();
        varForXpt.put(MemmapXptConstants.SN_FID_SWCP, flatInstanceDescriptor.getShortName());
        Optional<Entry<SwComponentPrototype, List<EcucContainerValue>>> findFirst2 = this.swptoOsAppsMap2.entrySet()
            .stream().filter(e3 -> GenerateArxmlUtil.isSameElement(e.getKey(), e3.getKey())).findFirst();
        if (findFirst2.isPresent() && (findFirst2.get().getValue().size() == 1)) {
          String osAppName = findFirst2.get().getValue().get(0).getShortName();
          varForXpt.put(MemmapXptConstants.SN_OSAPP, osAppName);
          getFormattedBuffer(MemmapXptConstants.PARAMSWCPCALIB, MemmapXptConstants.CALIB, varForXpt, xptLines);
        }
      }
    }

  }


  private void xptForBswmdCode(final List<String> xptLines) throws Exception {
    for (BswModuleDescription bswmd : this.listOfBswmd) {

      HashMap<String, String> varForXpt = new HashMap<String, String>();
      varForXpt.put(MemmapXptConstants.SN_BSWMD_MEMSEC, bswmd.getShortName().toUpperCase());
      varForXpt.put(MemmapXptConstants.SN_BSWMD, bswmd.getShortName());
      getFormattedBuffer(MemmapXptConstants.BSWMDCODE, MemmapXptConstants.CODE, varForXpt, xptLines);

    }

  }

  private void xptForNvBlockRAMROM(final List<String> xptLines) throws Exception {
    for (Entry<SwComponentPrototype, SwComponentType> e : this.swcpToSwcMap1.entrySet()) {
      if (e.getValue() instanceof NvBlockSwComponentType) {

        Optional<Entry<SwComponentPrototype, List<EcucContainerValue>>> findFirst = this.swptoOsAppsMap1.entrySet()
            .stream().filter(e1 -> GenerateArxmlUtil.isSameElement(e.getKey(), e1.getKey())).findFirst();
        if (findFirst.isPresent() && (findFirst.get().getValue().size() == 1)) {
          Map<String, String> varForXpt = new HashMap<String, String>();
          varForXpt.put(MemmapXptConstants.SN_NVSWC, e.getValue().getShortName().toUpperCase());
          varForXpt.put(MemmapXptConstants.SN_OSAPP, findFirst.get().getValue().get(0).getShortName());
          getFormattedBuffer(MemmapXptConstants.NVRAMCLEAREDDATA, MemmapXptConstants.NVVARPWRONCLEARED, varForXpt,
              xptLines);
          getFormattedBuffer(MemmapXptConstants.NVROMCONST, MemmapXptConstants.CONST, varForXpt, xptLines);
        }
      }
    }
  }

  private void xptForNvBlockDRE(final List<String> xptLines) throws Exception {
    for (Entry<SwComponentPrototype, List<RTEEvent>> e : this.swptoDREMap1.entrySet()) {
      if (e.getKey().getType() instanceof NvBlockSwComponentType) {
        Map<String, String> varForXpt = new HashMap<String, String>();
        varForXpt.put(MemmapXptConstants.SN_NVSWCP, e.getKey().getShortName().toUpperCase());
        if (!e.getValue().isEmpty()) {
          for (RTEEvent dre : e.getValue()) {

            varForXpt.put(MemmapXptConstants.SN_DRE, dre.getShortName().toUpperCase());
            Optional<Entry<SwComponentPrototype, List<EcucContainerValue>>> findFirst = this.swptoOsAppsMap1.entrySet()
                .stream().filter(e1 -> GenerateArxmlUtil.isSameElement(e.getKey(), e1.getKey())).findFirst();
            if (findFirst.isPresent() && (findFirst.get().getValue().size() == 1)) {
              String osAppName = findFirst.get().getValue().get(0).getShortName();
              varForXpt.put(MemmapXptConstants.SN_OSAPP, osAppName);

              getFormattedBuffer(MemmapXptConstants.NVDREACTIVEFLG, MemmapXptConstants.VARCLEARED, varForXpt, xptLines);
            }
          }
        }
      }
    }
  }

  private void xptForOsAppMemoryAllocation(final List<String> xptLines) throws Exception {
    for (Entry<EcucContainerValue, EcucContainerValue> e : this.osApptoEcucParitionsMap.entrySet()) {
      Map<String, String> varForXpt = new HashMap<String, String>();
      varForXpt.put(MemmapXptConstants.SN_OSAPP, e.getKey().getShortName());
      varForXpt.put(MemmapXptConstants.SN_OSAPP_MEMSEC, e.getKey().getShortName().toUpperCase());
      getFormattedBuffer(MemmapXptConstants.RESOURCEVARIABLES, MemmapXptConstants.VARCLEARED, varForXpt, xptLines);
      if (e.getValue() != null) {
        varForXpt.put(MemmapXptConstants.SN_ECUCP, e.getValue().getShortName());
        if (this.RTARTEVersion >= 12) {
          if (!this.addSwcAsPerOsAppWise) {
            getFormattedBuffer(MemmapXptConstants.CONSTOBJECTSNOSWC, MemmapXptConstants.CONST, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.VARINITOBJECTSNOSWC, MemmapXptConstants.VARINIT, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.VARCLROBJECTSNOSWC, MemmapXptConstants.VARCLEARED, varForXpt,
                xptLines);
            getFormattedBuffer(MemmapXptConstants.VARPWRONCLROBJECTSNOSWC, MemmapXptConstants.VARPOWERONCLEARED,
                varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.VARSAVEDZONEOBJECTNOSWC, MemmapXptConstants.VARSAVEDZONE, varForXpt,
                xptLines);
            getFormattedBuffer(MemmapXptConstants.CALIBOBJECTNOSWC, MemmapXptConstants.CALIB, varForXpt, xptLines);
          }
          else {
            getFormattedBuffer(MemmapXptConstants.CONSTOBJECTSSWC, MemmapXptConstants.CONST, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.VARINITOBJECTSSWC, MemmapXptConstants.VARINIT, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.VARCLROBJECTSSWC, MemmapXptConstants.VARCLEARED, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.VARPWRONCLROBJECTSSWC, MemmapXptConstants.VARPOWERONCLEARED,
                varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.VARSAVEDZONEOBJECTSWC, MemmapXptConstants.VARSAVEDZONE, varForXpt,
                xptLines);
            getFormattedBuffer(MemmapXptConstants.CALIBOBJECTSWC, MemmapXptConstants.CALIB, varForXpt, xptLines);
          }
        }
        getFormattedBuffer(MemmapXptConstants.CONSTOBJECTS, MemmapXptConstants.CONST, varForXpt, xptLines);
        getFormattedBuffer(MemmapXptConstants.VARINITOBJECTS, MemmapXptConstants.VARINIT, varForXpt, xptLines);
        getFormattedBuffer(MemmapXptConstants.VARCLROBJECTS, MemmapXptConstants.VARCLEARED, varForXpt, xptLines);
        getFormattedBuffer(MemmapXptConstants.VARPWRONCLROBJECTS, MemmapXptConstants.VARPOWERONCLEARED, varForXpt,
            xptLines);
        getFormattedBuffer(MemmapXptConstants.VARSAVEDZONEOBJECT, MemmapXptConstants.VARSAVEDZONE, varForXpt, xptLines);
        getFormattedBuffer(MemmapXptConstants.CALIBOBJECT, MemmapXptConstants.CALIB, varForXpt, xptLines);
        getFormattedBuffer(MemmapXptConstants.ECUCPARTITION8, MemmapXptConstants.VARINIT, varForXpt, xptLines);
      }
    }
  }


  private void xptForServerActivationFlags(final List<String> xptLines) throws Exception {
    for (Entry<AsynchronousServerCallPoint, SwConnector> e : this.asctoAscMap.entrySet()) {
      Map<String, String> varForXpt = new HashMap<String, String>();
      SwConnector swConnector = e.getValue();

      if (swConnector instanceof AssemblySwConnector) {
        AssemblySwConnector asc = (AssemblySwConnector) swConnector;
        PPortInCompositionInstanceRef provider = asc.getProvider();
        SwComponentPrototype swcp = provider.getContextComponent();
        AtomicSwComponentType type = (AtomicSwComponentType) swcp.getType();

        for (SwcInternalBehavior ib : type.getInternalBehaviors()) {
          List<OperationInvokedEvent> collect =
              ib.getEvents().stream().filter(ev -> ev instanceof OperationInvokedEvent)
                  .map(ev -> (OperationInvokedEvent) ev).collect(Collectors.toList());
          List<OperationInvokedEvent> collect2 = collect.stream()
              .filter(ev -> (ev.getOperation() != null) && (ev.getOperation().getContextPPort() != null) &&
                  GenerateArxmlUtil.isSameElement(ev.getOperation().getContextPPort(), provider.getTargetPPort()))
              .collect(Collectors.toList());

          for (OperationInvokedEvent oie : collect2) {

            varForXpt.put(MemmapXptConstants.SN_SWCP, swcp.getShortName().toUpperCase());
            varForXpt.put(MemmapXptConstants.SN_OIE, oie.getShortName().toUpperCase());

            Optional<Entry<SwComponentPrototype, List<EcucContainerValue>>> findFirst = this.swptoOsAppsMap1.entrySet()
                .stream().filter(o -> GenerateArxmlUtil.isSameElement(o.getKey(), swcp)).findFirst();

            if (findFirst.isPresent()) {
              List<EcucContainerValue> osAppList = findFirst.get().getValue();
              if (osAppList.size() == 1) {
                varForXpt.put(MemmapXptConstants.SN_OSAPP, osAppList.get(0).getShortName());
                if (!this.addSwcAsPerOsAppWise) {
                  getFormattedBuffer(MemmapXptConstants.NONSPLITSERVERACTIVATIONFLAG, MemmapXptConstants.VARINIT,
                      varForXpt, xptLines);
                }
                else {
                  varForXpt.put(MemmapXptConstants.SN_SWC, type.getShortName());
                  getFormattedBuffer(MemmapXptConstants.NONSPLITSERVERACTIVATIONFLAGSWC, MemmapXptConstants.VARINIT,
                      varForXpt, xptLines);
                }
              }
              else if (osAppList.size() > 1) {
                varForXpt.put(MemmapXptConstants.SN_SWC, type.getShortName());
                getFormattedBuffer(MemmapXptConstants.SPLITSERVERACTIVATIONFLAG, MemmapXptConstants.VARINIT, varForXpt,
                    xptLines);
              }
            }
          }
        }
      }
    }
  }


  /**
   * @throws Exception
   */
  private void xptForClientAndServerOSCBK(final List<String> xptLines) throws Exception {
    for (Entry<AsynchronousServerCallPoint, SwConnector> e : this.asctoAscMap.entrySet()) {
      Map<String, String> varForXpt = new HashMap<String, String>();
      SwConnector swConnector = e.getValue();

      if (swConnector instanceof AssemblySwConnector) {
        AssemblySwConnector asc = (AssemblySwConnector) swConnector;
        RPortInCompositionInstanceRef requester = asc.getRequester();
        SwComponentPrototype clientSwp = requester.getContextComponent();

        Optional<Entry<FlatInstanceDescriptor, SwComponentPrototype>> findFirst = this.fldToswpMap.entrySet().stream()
            .filter(f -> GenerateArxmlUtil.isSameElement(f.getValue(), clientSwp)).findFirst();

        if (findFirst.isPresent()) {
          FlatInstanceDescriptor flatInstanceDescriptor = findFirst.get().getKey();
          AbstractRequiredPortPrototype rPort = requester.getTargetRPort();
          PortInterface portInterface = ((RPortPrototype) rPort).getRequiredInterface();

          varForXpt.put(MemmapXptConstants.SN_FID_CLIENT, flatInstanceDescriptor.getShortName().toUpperCase());
          varForXpt.put(MemmapXptConstants.SN_RPORT_PROTOTYPE, rPort.getShortName().toUpperCase());

          if (portInterface instanceof ClientServerInterface) {
            ClientServerInterface csinterface = (ClientServerInterface) portInterface;
            EList<ClientServerOperation> csOperations = csinterface.getOperations();


            for (ClientServerOperation opration : csOperations) {
              varForXpt.put(MemmapXptConstants.SN_CS_OPERATION, opration.getShortName().toUpperCase());

              Optional<Entry<SwComponentPrototype, List<EcucContainerValue>>> findFirst2 = this.swptoOsAppsMap1
                  .entrySet().stream().filter(o -> GenerateArxmlUtil.isSameElement(o.getKey(), clientSwp)).findFirst();

              if (findFirst2.isPresent()) {
                List<EcucContainerValue> osAppList = findFirst2.get().getValue();
                if (this.allocateTaskAsPerOSApp) {

                  if (osAppList.size() == 1) {

                    varForXpt.put(MemmapXptConstants.SN_OSAPP, osAppList.get(0).getShortName());
                    if (!this.addSwcAsPerOsAppWise) {

                      getFormattedBuffer(MemmapXptConstants.CLIENTOSCBKTASKPERAPPLNOSPLIT, MemmapXptConstants.CODE,
                          varForXpt, xptLines);
                      getFormattedBuffer(MemmapXptConstants.SERVEROSCBKTASKPERAPPLNOSPLIT, MemmapXptConstants.CODE,
                          varForXpt, xptLines);
                    }
                    else {
                      varForXpt.put(MemmapXptConstants.SN_SWC, clientSwp.getType().getShortName());
                      getFormattedBuffer(MemmapXptConstants.CLIENTOSCBKTASKPERAPPLNOSPLITSWC, MemmapXptConstants.CODE,
                          varForXpt, xptLines);
                      getFormattedBuffer(MemmapXptConstants.SERVEROSCBKTASKPERAPPLNOSPLITSWC, MemmapXptConstants.CODE,
                          varForXpt, xptLines);
                    }


                  }
                  else if (osAppList.size() > 1) {
                    varForXpt.put(MemmapXptConstants.SN_SWC, clientSwp.getType().getShortName());
                    getFormattedBuffer(MemmapXptConstants.CLIENTOSCBKTASKPERAPPLSPLIT, MemmapXptConstants.CODE,
                        varForXpt, xptLines);
                    getFormattedBuffer(MemmapXptConstants.SERVEROSCBKTASKPERAPPLSPLIT, MemmapXptConstants.CODE,
                        varForXpt, xptLines);

                  }
                }
                else {
                  getFormattedBuffer(MemmapXptConstants.CLIENTANDSERVEROSAPPNOTPERAPPLICATION, MemmapXptConstants.CODE,
                      varForXpt, xptLines);

                }
              }
            }
          }
        }
      }
    }
  }


  private void xptForNotTasKWiseMAKWSwc(final List<String> xptLines) throws Exception {
    for (Entry<SwComponentPrototype, SwComponentType> e : this.swcpToSwcMap1.entrySet()) {

      boolean anyMatch =
          this.swcpToSwcMapTaskWise.keySet().stream().anyMatch(swp -> GenerateArxmlUtil.isSameElement(swp, e.getKey()));

      if (!anyMatch) {
        Optional<Entry<FlatInstanceDescriptor, SwComponentPrototype>> findFirst = this.fldToswpMap1.entrySet().stream()
            .filter(e1 -> GenerateArxmlUtil.isSameElement(e1.getValue(), e.getKey())).findFirst();
        if (findFirst.isPresent()) {
          Map<String, String> varForXpt = new HashMap<String, String>();
          FlatInstanceDescriptor flatInstanceDescriptor = findFirst.get().getKey();
          varForXpt.put(MemmapXptConstants.SN_FID_SWCP, flatInstanceDescriptor.getShortName());
          Optional<SwComponentPrototype> findFirst2 = this.swptoOsAppsMap1.keySet().stream()
              .filter(s -> GenerateArxmlUtil.isSameElement(s, e.getKey())).findFirst();
          if (findFirst2.isPresent()) {
            List<EcucContainerValue> osAppList = this.swptoOsAppsMap1.get(findFirst2.get());

            if (osAppList.size() == 1) {
              varForXpt.put(MemmapXptConstants.SN_OSAPP, osAppList.get(0).getShortName());
              if (!this.addSwcAsPerOsAppWise) {

                getFormattedBuffer(MemmapXptConstants.NOSPLITNOTASKSWINITDATA, MemmapXptConstants.VARINIT, varForXpt,
                    xptLines);
                getFormattedBuffer(MemmapXptConstants.NOSPLITNOTASKSWVARCLR, MemmapXptConstants.VARCLEARED, varForXpt,
                    xptLines);
                getFormattedBuffer(MemmapXptConstants.NOSPLITNOTASKSWVARCLRPWRON, MemmapXptConstants.VARPOWERONCLEARED,
                    varForXpt, xptLines);
                getFormattedBuffer(MemmapXptConstants.NOSPLITNOTASKSWVARSAVEDZONE, MemmapXptConstants.VARSAVEDZONE,
                    varForXpt, xptLines);
              }
              else {
                varForXpt.put(MemmapXptConstants.SN_SWC, e.getValue().getShortName());
                getFormattedBuffer(MemmapXptConstants.NOSPLITNOTASKSWINITDATASWC, MemmapXptConstants.VARINIT, varForXpt,
                    xptLines);
                getFormattedBuffer(MemmapXptConstants.NOSPLITNOTASKSWVARCLRSWC, MemmapXptConstants.VARCLEARED,
                    varForXpt, xptLines);
                getFormattedBuffer(MemmapXptConstants.NOSPLITNOTASKSWVARCLRSWCPWRON,
                    MemmapXptConstants.VARPOWERONCLEARED, varForXpt, xptLines);
                getFormattedBuffer(MemmapXptConstants.NOSPLITNOTASKSWVARSAVEDZONESWC, MemmapXptConstants.VARSAVEDZONE,
                    varForXpt, xptLines);
              }
            }
            else if (osAppList.size() > 1) {

              varForXpt.put(MemmapXptConstants.SN_SWC, e.getValue().getShortName());
              getFormattedBuffer(MemmapXptConstants.SPLITNOTASKSWINITDATA, MemmapXptConstants.VARINIT, varForXpt,
                  xptLines);
              getFormattedBuffer(MemmapXptConstants.SPLITNOTASKSWVARCLR, MemmapXptConstants.VARCLEARED, varForXpt,
                  xptLines);
              getFormattedBuffer(MemmapXptConstants.SPLITNOTASKSWVARCLRPWRON, MemmapXptConstants.VARPOWERONCLEARED,
                  varForXpt, xptLines);
              getFormattedBuffer(MemmapXptConstants.SPLITNOTASKSWVARSAVEDZONESWC, MemmapXptConstants.VARSAVEDZONE,
                  varForXpt, xptLines);
            }
          }
        }
      }
    }
  }

  private void xptForTaskWiseMAKWSwcs(final List<String> xptLines) throws Exception {
    for (Entry<SwComponentPrototype, SwComponentType> e : this.swcpToSwcMapTaskWise.entrySet()) {
      Optional<Entry<FlatInstanceDescriptor, SwComponentPrototype>> findFirst = this.fldToswpMap1.entrySet().stream()
          .filter(e1 -> GenerateArxmlUtil.isSameElement(e1.getValue(), e.getKey())).findFirst();
      if (findFirst.isPresent()) {
        HashMap<String, String> varForXpt = new HashMap<String, String>();
        FlatInstanceDescriptor flatInstanceDescriptor = findFirst.get().getKey();
        varForXpt.put(MemmapXptConstants.SN_FID_SWCP, flatInstanceDescriptor.getShortName());
        Optional<SwComponentPrototype> findFirst2 = this.swptoOsAppsMap1.keySet().stream()
            .filter(s -> GenerateArxmlUtil.isSameElement(s, e.getKey())).findFirst();
        if (findFirst2.isPresent()) {
          List<EcucContainerValue> osAppList = this.swptoOsAppsMap1.get(findFirst2.get());

          if (osAppList.size() == 1) {
            varForXpt.put(MemmapXptConstants.SN_OSAPP, osAppList.get(0).getShortName());

            if (!this.addSwcAsPerOsAppWise) {

              getFormattedBuffer(MemmapXptConstants.NOSPLITSWINITDATA, MemmapXptConstants.VARINIT, varForXpt, xptLines);
              getFormattedBuffer(MemmapXptConstants.NOSPLITSWVARCLR, MemmapXptConstants.VARCLEARED, varForXpt,
                  xptLines);
              getFormattedBuffer(MemmapXptConstants.NOSPLITSWVARCLRPOWERON, MemmapXptConstants.VARPOWERONCLEARED,
                  varForXpt, xptLines);
              getFormattedBuffer(MemmapXptConstants.NOSPLITSWVARSAVEDZONE, MemmapXptConstants.VARSAVEDZONE, varForXpt,
                  xptLines);
            }
            else {
              varForXpt.put(MemmapXptConstants.SN_SWC, e.getValue().getShortName());
              getFormattedBuffer(MemmapXptConstants.NOSPLITSWINITDATASWC, MemmapXptConstants.VARINIT, varForXpt,
                  xptLines);
              getFormattedBuffer(MemmapXptConstants.NOSPLITSWVARCLRSWC, MemmapXptConstants.VARCLEARED, varForXpt,
                  xptLines);
              getFormattedBuffer(MemmapXptConstants.NOSPLITSWVARCLRSWCPOWERON, MemmapXptConstants.VARPOWERONCLEARED,
                  varForXpt, xptLines);
              getFormattedBuffer(MemmapXptConstants.NOSPLITSWVARSAVEDZONESWC, MemmapXptConstants.VARSAVEDZONE,
                  varForXpt, xptLines);
            }
          }
          else if (osAppList.size() > 1) {

            varForXpt.put(MemmapXptConstants.SN_SWC, e.getValue().getShortName());
            getFormattedBuffer(MemmapXptConstants.SPLITSWINITDATA, MemmapXptConstants.VARINIT, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.SPLITSWVARCLR, MemmapXptConstants.VARCLEARED, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.SPLITSWVARCLRPWRON, MemmapXptConstants.VARPOWERONCLEARED, varForXpt,
                xptLines);
            getFormattedBuffer(MemmapXptConstants.SPLITSWVARSAVEDZONESWC, MemmapXptConstants.VARSAVEDZONE, varForXpt,
                xptLines);

          }
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  private void xptForDataReceivedEvents(final List<String> xptLines) throws Exception {

    for (Entry<SwComponentPrototype, List<EcucContainerValue>> entry : this.swptoOsAppsMap1.entrySet()) {

      Map<String, String> varForXpt = new HashMap<String, String>();
      Optional<SwComponentPrototype> findFirst = this.swptoDREMap1.keySet().stream()
          .filter(swp -> GenerateArxmlUtil.isSameElement(swp, entry.getKey())).findFirst();

      if (findFirst.isPresent()) {
        for (RTEEvent event : this.swptoDREMap1.get(findFirst.get())) {
          varForXpt.put(MemmapXptConstants.SN_DRE, event.getShortName().toUpperCase());

          Optional<FlatInstanceDescriptor> findFirst2 = this.fldToswpMap1.keySet().stream()
              .filter(fld -> GenerateArxmlUtil.isSameElement(this.fldToswpMap1.get(fld), entry.getKey())).findFirst();

          if (findFirst2.isPresent()) {
            varForXpt.put(MemmapXptConstants.SN_FID_SWCP, findFirst2.get().getShortName().toUpperCase());

            if (entry.getValue().size() == 1) {
              varForXpt.put(MemmapXptConstants.SN_OSAPP, entry.getValue().get(0).getShortName());

              if (!this.addSwcAsPerOsAppWise) {
                getFormattedBuffer(MemmapXptConstants.NOSPLITSWCDATARECV, MemmapXptConstants.VARCLEARED, varForXpt,
                    xptLines);
              }
              else {
                varForXpt.put(MemmapXptConstants.SN_SWC, entry.getKey().getType().getShortName());
                getFormattedBuffer(MemmapXptConstants.NOSPLITSWCDATARECVSWC, MemmapXptConstants.VARCLEARED, varForXpt,
                    xptLines);
              }
            }
            else if (entry.getValue().size() > 1) {
              varForXpt.put(MemmapXptConstants.SN_SWC, entry.getKey().getType().getShortName());
              getFormattedBuffer(MemmapXptConstants.SPLITSWCDATARECV, MemmapXptConstants.VARCLEARED, varForXpt,
                  xptLines);
            }
          }
        }
      }

    }


  }

  /**
   * @param buffer
   * @throws Exception
   */
  private void xptForSwComponentPrototypes(final List<String> xptLines) throws Exception {


    for (Entry<SwComponentPrototype, List<EcucContainerValue>> entry : this.swptoOsAppsMap1.entrySet()) {
      Map<String, String> varForXpt = new HashMap<String, String>();
      varForXpt.put(MemmapXptConstants.SN_SWC_MEMSEC, entry.getKey().getType().getShortName().toUpperCase());
      varForXpt.put(MemmapXptConstants.SN_SWC, entry.getKey().getType().getShortName());
      if (entry.getValue().size() == 1) {
        varForXpt.put(MemmapXptConstants.SN_OSAPP, entry.getValue().get(0).getShortName());

        if (!this.addSwcAsPerOsAppWise) {
          getFormattedBuffer(MemmapXptConstants.NOSPLITSWCCODE, MemmapXptConstants.CODE, varForXpt, xptLines);
          getFormattedBuffer(MemmapXptConstants.NOSPLITVARINT8, MemmapXptConstants.VARINIT, varForXpt, xptLines);

          Optional<FlatInstanceDescriptor> findFirst = this.fldToswpMap1.keySet().stream()
              .filter(fld -> GenerateArxmlUtil.isSameElement(this.fldToswpMap1.get(fld), entry.getKey())).findFirst();

          if (findFirst.isPresent()) {
            varForXpt.put(MemmapXptConstants.SN_FID_SWCP, findFirst.get().getShortName());
            getFormattedBuffer(MemmapXptConstants.NOSPLITSWCCALIB, MemmapXptConstants.CALIB, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.NOSPLITSWCCALIBASIL, MemmapXptConstants.CALIB, varForXpt, xptLines);

          }
        }
        else {
          getFormattedBuffer(MemmapXptConstants.NOSPLITSWCCODESWC, MemmapXptConstants.CODE, varForXpt, xptLines);
          getFormattedBuffer(MemmapXptConstants.NOSPLITVARINT8SWC, MemmapXptConstants.CODE, varForXpt, xptLines);

          Optional<FlatInstanceDescriptor> findFirst = this.fldToswpMap1.keySet().stream()
              .filter(fld -> GenerateArxmlUtil.isSameElement(this.fldToswpMap1.get(fld), entry.getKey())).findFirst();

          if (findFirst.isPresent()) {
            varForXpt.put(MemmapXptConstants.SN_FID_SWCP, findFirst.get().getShortName());
            getFormattedBuffer(MemmapXptConstants.NOSPLITSWCCALIBSWC, MemmapXptConstants.CALIB, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.NOSPLITSWCCALIBASILSWC, MemmapXptConstants.CALIB, varForXpt,
                xptLines);

          }
        }
      }
      else if (entry.getValue().size() > 1) {
        getFormattedBuffer(MemmapXptConstants.SPLITSWCCODE, MemmapXptConstants.CODE, varForXpt, xptLines);
        getFormattedBuffer(MemmapXptConstants.SPLITVARINT8, MemmapXptConstants.VARINIT, varForXpt, xptLines);
        Optional<FlatInstanceDescriptor> findFirst = this.fldToswpMap1.keySet().stream()
            .filter(fld -> GenerateArxmlUtil.isSameElement(this.fldToswpMap1.get(fld), entry.getKey())).findFirst();
        if (findFirst.isPresent()) {
          varForXpt.put(MemmapXptConstants.SN_FID_SWCP, findFirst.get().getShortName());
          getFormattedBuffer(MemmapXptConstants.SPLITSWCCALIB, MemmapXptConstants.CALIB, varForXpt, xptLines);
          getFormattedBuffer(MemmapXptConstants.SPLITCALIBASIL, MemmapXptConstants.CALIB, varForXpt, xptLines);


        }
      }
    }
  }


  private void xptForTaskWiseSwcs(final List<String> xptLines) throws Exception {
    for (Entry<SwComponentPrototype, List<EcucContainerValue>> e : this.swptoTasksMapTaskWise.entrySet()) {
      for (EcucContainerValue task : e.getValue()) {
        Optional<Entry<EcucContainerValue, List<EcucContainerValue>>> findFirst = this.osApptoTasksMap.entrySet()
            .stream().filter(entry -> entry.getValue().stream().anyMatch(t -> GenerateArxmlUtil.isSameElement(t, task)))
            .findFirst();
        if (findFirst.isPresent()) {
          EcucContainerValue osApp = findFirst.get().getKey();
          Optional<Entry<FlatInstanceDescriptor, SwComponentPrototype>> findFirst2 = this.fldToswpMapTaskWise.entrySet()
              .stream().filter(e2 -> GenerateArxmlUtil.isSameElement(e2.getValue(), e.getKey())).findFirst();
          if (findFirst2.isPresent()) {
            HashMap<String, String> varForXpt = new HashMap<>();
            varForXpt.put(MemmapXptConstants.SN_FID_SWCP, findFirst2.get().getKey().getShortName());
            varForXpt.put(MemmapXptConstants.SN_TASK, task.getShortName());
            varForXpt.put(MemmapXptConstants.SN_OSAPP, osApp.getShortName());
            getFormattedBuffer(MemmapXptConstants.INITVARTASK, MemmapXptConstants.VARINIT, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.CLEARVARTASK, MemmapXptConstants.VARCLEARED, varForXpt, xptLines);
            getFormattedBuffer(MemmapXptConstants.POWERONCLEARVARTASK, MemmapXptConstants.VARPOWERONCLEARED, varForXpt,
                xptLines);
            getFormattedBuffer(MemmapXptConstants.VARSAVEDZONETASK, MemmapXptConstants.VARSAVEDZONE, varForXpt,
                xptLines);

          }
        }
      }
    }
  }

  private void xptForImplicitBuffersOfTasks(final List<String> xptLines) throws Exception {
    for (EcucContainerValue task : this.tasktoRteEventsMap.keySet()) {
      for (Entry<EcucContainerValue, List<EcucContainerValue>> e : this.osApptoTasksMap.entrySet()) {
        if (GenerateArxmlUtil.listContainsElements(e.getValue(), task)) {
          EcucContainerValue osApp = e.getKey();
          Map<String, String> varForXpt = new HashMap<String, String>();
          varForXpt.put(MemmapXptConstants.SN_TASK, task.getShortName().toUpperCase());
          varForXpt.put(MemmapXptConstants.SN_OSAPP, osApp.getShortName());
          getFormattedBuffer(MemmapXptConstants.IMPLICITBUFFEROFTASKS, MemmapXptConstants.VARCLEARED, varForXpt,
              xptLines);
          getFormattedBuffer(MemmapXptConstants.OSTASKVARCLEARED, MemmapXptConstants.VARCLEARED, varForXpt, xptLines);

        }

      }
    }

  }

  /**
   * @param buffer
   * @throws Exception
   */
  private void xptForTasks(final List<String> xptLines) throws Exception {

    for (EcucContainerValue task : this.tasktoRteEventsMap.keySet()) {
      for (Entry<EcucContainerValue, List<EcucContainerValue>> e : this.osApptoTasksMap.entrySet()) {

        Optional<EcucContainerValue> findFirst2 =
            e.getValue().stream().filter(o -> o.getShortName().equals(task.getShortName())).findFirst();
        if (findFirst2.isPresent()) {

          Map<String, String> varForXpt = new HashMap<String, String>();
          varForXpt.put(MemmapXptConstants.SN_TASK, task.getShortName().toUpperCase());
          varForXpt.put(MemmapXptConstants.SN_OSAPP, e.getKey().getShortName());
          if (this.allocateTaskAsPerOSApp) {
            getFormattedBuffer(MemmapXptConstants.OSTASKPERAPPLICATIONS, MemmapXptConstants.CODE, varForXpt, xptLines);

          }
          else {
            getFormattedBuffer(MemmapXptConstants.OSTASKNOTPERAPPLICATION, MemmapXptConstants.CODE, varForXpt,
                xptLines);
          }
        }
      }
    }
  }

  /**
   * @param xptLines
   * @throws Exception
   */
  private void xptFornvBlockCodes(final List<String> xptLines) throws Exception {

    for (Entry<SwComponentPrototype, SwComponentType> e : this.swcpToSwcMap1.entrySet()) {
      if (e.getValue() instanceof NvBlockSwComponentType) {
        SwComponentPrototype nvSwc = e.getKey();
        HashMap<String, String> varForXpt = new HashMap<String, String>();
        varForXpt.put(MemmapXptConstants.SN_NVSWC, e.getValue().getShortName().toUpperCase());
        EList<NvBlockDescriptor> nvBlockDescriptors =
            ((NvBlockSwComponentType) nvSwc.getType()).getNvBlockDescriptors();

        List<Entry<SwComponentPrototype, List<EcucContainerValue>>> collect = this.swptoOsAppsMap1.entrySet().stream()
            .filter(e1 -> GenerateArxmlUtil.isSameElement(e.getKey(), e1.getKey())).collect(Collectors.toList());
        if (!collect.isEmpty() && (collect.get(0).getValue().size() == 1)) {
          String osAppName = collect.get(0).getValue().get(0).getShortName();
          varForXpt.put(MemmapXptConstants.SN_OSAPP, osAppName);

          for (NvBlockDescriptor desc : nvBlockDescriptors) {
            varForXpt.put(MemmapXptConstants.SN_NVBD, desc.getShortName().toUpperCase());
            getFormattedBuffer(MemmapXptConstants.NVBLOCKCODE, MemmapXptConstants.CODE, varForXpt, xptLines);
          }
          getFormattedBuffer(MemmapXptConstants.NVBLOCKVARSAVEDZONE, MemmapXptConstants.NVVARPWRONCLEARED, varForXpt,
              xptLines);
          getFormattedBuffer(MemmapXptConstants.NVBLOCKCONSTSAVEDRECZONE, MemmapXptConstants.CONST, varForXpt,
              xptLines);
          getFormattedBuffer(MemmapXptConstants.NVBLOCKVARPOSRTSAVEDZONE, MemmapXptConstants.NVVARSAVEDZONE, varForXpt,
              xptLines);
        }
      }
    }
  }


  // 2.11
  /**
   * @param xptLines
   * @throws Exception
   */
  private void xptForGeneralKeywords(final List<String> xptLines) throws Exception {
    HashMap<String, String> varForXpt = new HashMap<String, String>();

    getFormattedBuffer(MemmapXptConstants.MAINCODE, MemmapXptConstants.CODE, varForXpt, xptLines);
    getFormattedBuffer(MemmapXptConstants.EXTCODE, MemmapXptConstants.CODE, varForXpt, xptLines);
    getFormattedBuffer(MemmapXptConstants.SYSCODE, MemmapXptConstants.CODE, varForXpt, xptLines);
    getFormattedBuffer(MemmapXptConstants.SYSVARCLEARED, MemmapXptConstants.VARCLEARED, varForXpt, xptLines);
    getFormattedBuffer(MemmapXptConstants.LIBCODE, MemmapXptConstants.CODE, varForXpt, xptLines);
    getFormattedBuffer(MemmapXptConstants.RTECONSTDATA, MemmapXptConstants.CONST, varForXpt, xptLines);
  }

  /**
   * @param ostaskperapplications
   * @param string
   * @param varForXpt
   * @throws Exception
   */
  private void getFormattedBuffer(final String xptLine, final String sectionName, final Map<String, String> varForXpt,
      final List<String> xptLines)
      throws Exception {
    List<SECTION> sectionList = this.target.getSECTIONS().getSECTION().stream()
        .filter(s -> s.getSHORTNAME().equals(sectionName)).collect(Collectors.toList());
    if (!sectionList.isEmpty()) {
      SECTION section = sectionList.get(0);
      varForXpt.put(SECTION_KEYWORD.INITIAL_KEYWORD_1.getLiteral(), section.getINITIALKEYWORD1());
      varForXpt.put(SECTION_KEYWORD.INITIAL_KEYWORD_2.getLiteral(), section.getINITIALKEYWORD2());
      varForXpt.put(SECTION_KEYWORD.FINAL_KEYWORD_1.getLiteral(), section.getFINALKEYWORD1());
      varForXpt.put(SECTION_KEYWORD.FINAL_KEYWORD_2.getLiteral(), section.getFINALKEYWORD2());
      varForXpt.put(SECTION_KEYWORD.OPTIONAL_KEYWORD_1.getLiteral(), section.getOPTIONALKEYWORD1());
      varForXpt.put(SECTION_KEYWORD.OPTIONAL_KEYWORD_2.getLiteral(), section.getOPTIONALKEYWORD2());
      varForXpt.put(SECTION_KEYWORD.LINKER_FLAG_KEYWORD.getLiteral(), section.getLINKERFLAGKEYWORD());
      varForXpt.put(SECTION_KEYWORD.DEFAULT_INITIAL_KEYWORD_1.getLiteral(), section.getDEFAULTINITIALKEYWORD1());
      varForXpt.put(SECTION_KEYWORD.DEFAULT_INITIAL_KEYWORD_2.getLiteral(), section.getDEFAULTINITIALKEYWORD2());

      varForXpt.put(GENERAL_KEYWORD.PRAGMA_SECTION.getLiteral(), this.target.getGENERALKEYWORDS().getPRAGMASECTION());
      varForXpt.put(GENERAL_KEYWORD.OS_APPLICATION.getLiteral(), this.target.getGENERALKEYWORDS().getOSAPPLICATION());
      varForXpt.put(GENERAL_KEYWORD.SW_COMPONENT.getLiteral(), this.target.getGENERALKEYWORDS().getSWCOMPONENT());
      varForXpt.put(GENERAL_KEYWORD.OS_TASK_NAME_PATTERN.getLiteral(),
          this.target.getGENERALKEYWORDS().getOSTASKNAMEPATTERN());
      varForXpt.put(GENERAL_KEYWORD.BSW_MODULE_DESCRIPTION.getLiteral(),
          this.target.getGENERALKEYWORDS().getBSWMODULEDESCRIPTION());
      varForXpt.put(GENERAL_KEYWORD.ALIGNMENT_OPERATOR_BOOLEAN.getLiteral(),
          this.target.getGENERALKEYWORDS().getALIGNMENTOPERATORBOOLEAN());
      varForXpt.put(GENERAL_KEYWORD.ALIGNMENT_OPERATOR_8BIT.getLiteral(),
          this.target.getGENERALKEYWORDS().getALIGNMENTOPERATOR8BIT());
      varForXpt.put(GENERAL_KEYWORD.ALIGNMENT_OPERATOR_16BIT.getLiteral(),
          this.target.getGENERALKEYWORDS().getALIGNMENTOPERATOR16BIT());
      varForXpt.put(GENERAL_KEYWORD.ALIGNMENT_OPERATOR_32BIT.getLiteral(),
          this.target.getGENERALKEYWORDS().getALIGNMENTOPERATOR32BIT());
      varForXpt.put(GENERAL_KEYWORD.ALIGNMENT_OPERATOR_64BIT.getLiteral(),
          this.target.getGENERALKEYWORDS().getALIGNMENTOPERATOR64BIT());

      String tempXPT = replaceVariableinXPT(xptLine, varForXpt, sectionName);
      if (!xptLines.contains(tempXPT)) {
        xptLines.add(tempXPT);
      }
    }
    else {
      RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription.getFormattedMesssage("313_0",
          sectionName, this.props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_CONFIG_FILEPATH)));
    }
  }

  /**
   * @param xptLine
   * @param varForXpt
   * @param sectionName
   * @return
   * @throws Exception
   */
  private String replaceVariableinXPT(final String xptLine, final Map<String, String> varForXpt,
      final String sectionName)
      throws Exception {
    String xpt = xptLine;
    Matcher matcher = MemmapXptConstants.OPTIONAL_KEYWORD_PATTERN.matcher(xpt);
    while (matcher.find()) {
      String match = matcher.group();
      String tempmatch = match.substring(1, match.length() - 1);
      String replacement = "";
      if (varForXpt.containsKey("INITIAL_KEYWORD_2") && (varForXpt.get("INITIAL_KEYWORD_2") != null)) {
        replacement = replaceVariableinXPT(tempmatch, varForXpt, sectionName);
      }
      xpt = xpt.replace(match, replacement);
    }

    matcher = MemmapXptConstants.KEYWORD_PATTERN.matcher(xpt);

    while (matcher.find()) {
      String match = matcher.group();
      String tempmatch = match;
      tempmatch = tempmatch.substring(2, tempmatch.length() - 1);

      String value = varForXpt.get(tempmatch);
      match = "\\" + match;
      if (value != null) {
        xpt = xpt.replaceFirst(match, value);
      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("311_1", match.substring(1), sectionName));
      }

    }
    return xpt;
  }

  /**
   * @param xptLines
   * @param buffer
   */
  private void writeDataToXPT(final List<String> xptLines) {

    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream(new File(this.project.getLocation().toOSString() + "/" + this.xptFilePath))))) {

      for (String value : xptLines) {
        bw.write(value);
        bw.newLine();
      }
      bw.flush();
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("191_1",
          this.project.getLocation().toOSString() + "/" + this.xptFilePath));

      try (BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
          new File(this.project.getLocation().toOSString() + "/" + this.memmapXptOutputLstPath))))) {

        bw1.write(this.xptFilePath);
        bw1.newLine();
        bw1.flush();
      }
      catch (Exception ex) {
        RteConfigGeneratorLogger.logErrormessage(LOGGER, ex.getLocalizedMessage());
      }
    }
    catch (Exception ex) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER, ex.getLocalizedMessage());
    }
  }


  private void prepareMemmapXptModel() throws IOException {

    if (this.rootCompostion != null) {

      CompositionSwComponentType softwareComposition = this.rootCompostion.getSoftwareComposition();
      if ((softwareComposition != null) && !softwareComposition.getComponents().isEmpty()) {

        for (SwComponentPrototype swp : softwareComposition.getComponents()) {
          this.swcpToSwcMap.put(swp, swp.getType());
          List<SwComponentPrototype> list = this.swtToswpMap.get(swp.getType());
          if (list != null) {
            list.add(swp);
          }
          else {
            list = new ArrayList<>();
            list.add(swp);
            this.swtToswpMap.put(swp.getType(), list);
          }
        }

        List<FlatInstanceDescriptor> fldInstances = GenerateArxmlUtil
            .getAllInstancesOf(this.editingDomain.getResourceSet().getResources(), FlatInstanceDescriptor.class, false);

        Map<FlatInstanceDescriptor, SwComponentPrototype> collection =
            fldInstances.stream()
                .filter(fld -> (fld.getEcuExtractReference() != null) &&
                    (fld.getEcuExtractReference().getTarget() != null) &&
                    (fld.getEcuExtractReference().getTarget() instanceof SwComponentPrototype))
                .filter(fld -> GenerateArxmlUtil.listContainsElements(this.swcpToSwcMap.keySet(),
                    fld.getEcuExtractReference().getTarget()))
                .collect(Collectors.toMap(fld -> fld,
                    fld -> (SwComponentPrototype) fld.getEcuExtractReference().getTarget()));
        if ((collection != null) && !collection.isEmpty()) {
          this.fldToswpMap.putAll(collection);
        }

        List<EcucContainerValue> ecucContainerValues = GenerateArxmlUtil
            .getAllInstancesOf(this.editingDomain.getResourceSet().getResources(), EcucContainerValue.class, false);

        ecucContainerValues = ecucContainerValues.stream()
            .filter(c -> (c.getDefinition() != null) && !c.getDefinition().eIsProxy()).collect(Collectors.toList());

        updateTasktoRteEventsMap(ecucContainerValues);

        updateTasktoBSWEventsMap(ecucContainerValues);


        List<EcucContainerValue> osApps = ecucContainerValues.stream()
            .filter(c -> GenerateArxmlUtil.getPackagePath(c.getDefinition())
                .equals(this.props.get(RteConfigGeneratorConstants.OS_APPLICATION_PARAM_DEF_AR_PKG_PATH)))
            .collect(Collectors.toList());

        updateOsApptoTasksMap(osApps);
        updateOsApptoEcucParitionsMap(osApps);

        List<EcucContainerValue> ecucPartitions = ecucContainerValues.stream()
            .filter(c -> GenerateArxmlUtil.getPackagePath(c.getDefinition())
                .equals(this.props.get(RteConfigGeneratorConstants.ECUC_PARTITION_PARAM_DEF_AR_PACKAGE_PATH) +
                    "/EcucPartitionCollection/EcucPartition"))
            .collect(Collectors.toList());

        updateEcucParitiontoSwpsMap(ecucPartitions);

        updateSwtToSwpMap1(this.rootCompostion);

        updateSwtToSwpMap2(this.rootCompostion);

        // updateSwtToSwpMap3(this.rootCompostion);

        if (this.props
            .get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_TASKWISE_SW_COMPONENT_PROTOTYPE_LIST_FILEPATH) != null) {
          prepareTaskWiseComponentPrototypeList();
        }


        List<AsynchronousServerCallPoint> ascpList = GenerateArxmlUtil.getAllInstancesOf(
            this.editingDomain.getResourceSet().getResources(), AsynchronousServerCallPoint.class, false);

        List<AsynchronousServerCallPoint> collect = ascpList.stream()
            .filter(asc -> (asc.getOperation() != null) && (asc.getOperation().getContextRPort() != null) &&
                !asc.getOperation().getContextRPort().eIsProxy() && GenerateArxmlUtil.listContainsElements(
                    this.swtToswpMap.keySet(), asc.getOperation().getContextRPort().getSwComponentType()))
            .collect(Collectors.toList());
        this.requiredASCList.addAll(collect);

        for (AsynchronousServerCallPoint asc : this.requiredASCList) {

          RunnableEntity runnableEntity = asc.getRunnableEntity();
          List<RTEEvent> events = this.rteEventToTaskMap.keySet().stream()
              .filter(e -> GenerateArxmlUtil.isSameElement(e.getStartOnEvent(), runnableEntity))
              .collect(Collectors.toList());

          this.asctoRteEventsMap.put(asc, events);


          if ((asc.getOperation() != null) && (asc.getOperation().getContextRPort() != null)) {
            Optional<SwConnector> findFirst = softwareComposition.getConnectors().stream()
                .filter(c -> (c instanceof AssemblySwConnector) && (((AssemblySwConnector) c).getProvider() != null) &&
                    !((AssemblySwConnector) c).getProvider().eIsProxy() &&
                    (((AssemblySwConnector) c).getRequester() != null) &&
                    !((AssemblySwConnector) c).getRequester().eIsProxy())
                .filter(c -> GenerateArxmlUtil.isSameElement(((AssemblySwConnector) c).getRequester().getTargetRPort(),
                    asc.getOperation().getContextRPort()))
                .findFirst();
            if (findFirst.isPresent()) {
              this.asctoAscMap.put(asc, findFirst.get());
            }
          }
        }

        for (Entry<AsynchronousServerCallPoint, SwConnector> entry : this.asctoAscMap.entrySet()) {
          SwConnector swc = entry.getValue();
          AbstractProvidedPortPrototype targetPPort = ((AssemblySwConnector) swc).getProvider().getTargetPPort();

          List<RTEEvent> events =
              this.rteEventToTaskMap.keySet().stream().filter(e -> e instanceof OperationInvokedEvent)
                  .filter(e -> (((OperationInvokedEvent) e).getOperation() != null) && GenerateArxmlUtil
                      .isSameElement(((OperationInvokedEvent) e).getOperation().getContextPPort(), targetPPort))
                  .collect(Collectors.toList());

          this.pPortToEvents.put((PPortPrototype) targetPPort, events);

        }


        this.listOfBswmd.addAll(GenerateArxmlUtil.getAllInstancesOf(this.editingDomain.getResourceSet().getResources(),
            BswModuleDescription.class, false));
      }
    }
  }


  private void prepareTaskWiseComponentPrototypeList() throws IOException {

    File file = new File(this.project.getLocation().toOSString() + "//" +
        this.props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_TASKWISE_SW_COMPONENT_PROTOTYPE_LIST_FILEPATH));

    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
    try {
      String line = bufferedReader.readLine();
      if (line != null) {
        line = line.trim();

        if (MemmapXptConstants.TASK_WISE_SWP_PATTERN.matcher(line).matches()) {

          String tokens[] = line.split(":");

          String[] cmps = tokens[1].split(",");

          List<SwComponentPrototype> swcps = Arrays
              .asList(cmps).stream().map(cmp -> this.swcpToSwcMap.keySet().stream()
                  .filter(swcp -> swcp.getShortName().equals(cmp.trim())).findFirst())
              .filter(swcp -> swcp.isPresent()).map(swcpt -> swcpt.get()).collect(Collectors.toList());

          Map<SwComponentPrototype, SwComponentType> collect =
              swcps.stream().collect(Collectors.toMap(swcp -> swcp, swcp -> swcp.getType()));
          this.swcpToSwcMapTaskWise.putAll(collect);

          List<FlatInstanceDescriptor> fldInstances = GenerateArxmlUtil.getAllInstancesOf(
              this.editingDomain.getResourceSet().getResources(), FlatInstanceDescriptor.class, false);

          Map<FlatInstanceDescriptor, SwComponentPrototype> collection =
              fldInstances.stream()
                  .filter(fld -> (fld.getEcuExtractReference() != null) &&
                      (fld.getEcuExtractReference().getTarget() != null) &&
                      (fld.getEcuExtractReference().getTarget() instanceof SwComponentPrototype))
                  .filter(fld -> GenerateArxmlUtil.listContainsElements(this.swcpToSwcMapTaskWise.keySet(),
                      fld.getEcuExtractReference().getTarget()))
                  .collect(Collectors.toMap(fld -> fld,
                      fld -> (SwComponentPrototype) fld.getEcuExtractReference().getTarget()));
          if ((collection != null) && !collection.isEmpty()) {
            this.fldToswpMapTaskWise.putAll(collection);
          }


          try {
            for (SwComponentPrototype swp : this.swcpToSwcMapTaskWise.keySet()) {

              AtomicSwComponentType type = (AtomicSwComponentType) swp.getType();

              for (SwcInternalBehavior swIB : type.getInternalBehaviors()) {

                List<List<EcucContainerValue>> findFirst = swIB.getEvents().stream()
                    .map(e -> this.tasktoRteEventsMap.keySet().stream()
                        .filter(m -> GenerateArxmlUtil.listContainsElements(this.tasktoRteEventsMap.get(m), e))
                        .collect(Collectors.toList()))
                    .collect(Collectors.toList());
                if (!findFirst.isEmpty()) {

                  List<List<EcucContainerValue>> collect1 =
                      findFirst.stream().filter(e -> (e != null) && !e.isEmpty()).collect(Collectors.toList());
                  if (!collect1.isEmpty()) {
                    List<EcucContainerValue> collect2 = collect1.stream().map(e -> e.get(0))
                        .filter(distinctByKey(e -> e.getShortName())).collect(Collectors.toList());
                    this.swptoTasksMapTaskWise.put(swp, collect2);
                  }
                }

                if (this.swptoTasksMapTaskWise.get(swp) != null) {

                  List<List<EcucContainerValue>> findFirst2 = this.swptoTasksMapTaskWise.get(swp).stream()
                      .map(t -> this.osApptoTasksMap.keySet().stream()
                          .filter(o -> GenerateArxmlUtil.listContainsElements(this.osApptoTasksMap.get(o), t))
                          .collect(Collectors.toList()))
                      .collect(Collectors.toList());

                  if (!findFirst2.isEmpty()) {
                    List<List<EcucContainerValue>> collect1 =
                        findFirst.stream().filter(e -> (e != null) && !e.isEmpty()).collect(Collectors.toList());
                    if (!collect1.isEmpty()) {
                      List<EcucContainerValue> collect2 = collect1.stream().map(e -> e.get(0))
                          .filter(distinctByKey(e -> e.getShortName())).collect(Collectors.toList());
                      this.swptoOsAppsMapTaskWise.put(swp, collect2);
                    }
                  }
                }
              }
            }
          }
          catch (Exception ex) {
            System.out.println(ex.getMessage());
          }
        }
      }
    }
    finally {
      bufferedReader.close();
    }
  }

  private void updateTasktoRteEventsMap(final List<EcucContainerValue> ecucContainerValues) {

    List<EcucContainerValue> rteEventtoTasksMappings = ecucContainerValues.stream()
        .filter(c -> GenerateArxmlUtil.getPackagePath(c.getDefinition())
            .equals(this.props.get(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH) +
                "/RteSwComponentInstance/RteEventToTaskMapping"))
        .collect(Collectors.toList());

    for (EcucContainerValue ecucV : rteEventtoTasksMappings) {
      if (!ecucV.getReferenceValues().isEmpty()) {
        EcucReferenceValue eventCValue = null;
        EcucReferenceValue taskCValue = null;
        for (EcucAbstractReferenceValue ecuAbsRef : ecucV.getReferenceValues()) {
          if (ecuAbsRef instanceof EcucReferenceValue) {
            EcucReferenceValue ecucRefV = (EcucReferenceValue) ecuAbsRef;

            if ((ecucRefV.getDefinition() != null) && !ecucRefV.getDefinition().eIsProxy() &&
                (ecucRefV.getValue() != null) && !ecucRefV.getValue().eIsProxy() &&
                GenerateArxmlUtil.getPackagePath(ecucRefV.getDefinition())
                    .equals(this.props.get(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH) +
                        "/RteSwComponentInstance/RteEventToTaskMapping/RteEventRef")) {
              eventCValue = ecucRefV;
            }
            else if ((ecucRefV.getDefinition() != null) && !ecucRefV.getDefinition().eIsProxy() &&
                (ecucRefV.getValue() != null) && !ecucRefV.getValue().eIsProxy() &&
                GenerateArxmlUtil.getPackagePath(ecucRefV.getDefinition())
                    .equals(this.props.get(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH) +
                        "/RteSwComponentInstance/RteEventToTaskMapping/RteMappedToTaskRef")) {
              taskCValue = ecucRefV;
            }
          }
        }

        if ((eventCValue != null) && (taskCValue != null) && (eventCValue.getValue() instanceof RTEEvent) &&
            (taskCValue.getValue() instanceof EcucContainerValue)) {
          RTEEvent rteEvent = (RTEEvent) eventCValue.getValue();
          EcucContainerValue task = (EcucContainerValue) taskCValue.getValue();

          List<RTEEvent> list = this.tasktoRteEventsMap.get(task);
          if (list != null) {
            list.add(rteEvent);
          }
          else {
            list = new ArrayList<>();
            list.add(rteEvent);
            this.tasktoRteEventsMap.put(task, list);
          }
        }
      }
    }

    for (Entry<EcucContainerValue, List<RTEEvent>> entry : this.tasktoRteEventsMap.entrySet()) {
      for (RTEEvent rteEvent : entry.getValue()) {
        this.rteEventToTaskMap.put(rteEvent, entry.getKey());
        String uri = EcoreResourceUtil.getURI(rteEvent).fragment().split("\\?")[0].trim();
        this.rteEventURIToTaskMap.put(uri, entry.getKey());

      }
    }
  }


  private void updateOsApptoTasksMap(final List<EcucContainerValue> osApps) {

    for (EcucContainerValue ecucV : osApps) {
      if (!ecucV.getReferenceValues().isEmpty()) {
        for (EcucAbstractReferenceValue ecuAbsRef : ecucV.getReferenceValues()) {
          if (ecuAbsRef instanceof EcucReferenceValue) {
            EcucReferenceValue ecucRefV = (EcucReferenceValue) ecuAbsRef;

            if ((ecucRefV.getDefinition() != null) && !ecucRefV.getDefinition().eIsProxy() &&
                (ecucRefV.getValue() != null) && !ecucRefV.getValue().eIsProxy() &&
                GenerateArxmlUtil.getPackagePath(ecucRefV.getDefinition())
                    .equals(this.props.get(RteConfigGeneratorConstants.OS_APPLICATION_PARAM_DEF_AR_PKG_PATH) +
                        "/OsAppTaskRef") &&
                (ecucRefV.getValue() instanceof EcucContainerValue)) {
              List<EcucContainerValue> list = this.osApptoTasksMap.get(ecucV);

              if (list != null) {
                list.add((EcucContainerValue) ecucRefV.getValue());
              }
              else {
                list = new ArrayList<>();
                list.add((EcucContainerValue) ecucRefV.getValue());
                this.osApptoTasksMap.put(ecucV, list);
              }
            }
          }
        }
      }
    }
  }


  private void updateOsApptoEcucParitionsMap(final List<EcucContainerValue> osApps) {

    for (EcucContainerValue ecucV : osApps) {
      if (!ecucV.getReferenceValues().isEmpty()) {
        for (EcucAbstractReferenceValue ecuAbsRef : ecucV.getReferenceValues()) {
          if (ecuAbsRef instanceof EcucReferenceValue) {
            EcucReferenceValue ecucRefV = (EcucReferenceValue) ecuAbsRef;

            if ((ecucRefV.getDefinition() != null) && !ecucRefV.getDefinition().eIsProxy() &&
                (ecucRefV.getValue() != null) && !ecucRefV.getValue().eIsProxy() &&
                GenerateArxmlUtil.getPackagePath(ecucRefV.getDefinition())
                    .equals(this.props.get(RteConfigGeneratorConstants.OS_APPLICATION_PARAM_DEF_AR_PKG_PATH) +
                        "/OsAppEcucPartitionRef") &&
                (ecucRefV.getValue() instanceof EcucContainerValue)) {
              this.osApptoEcucParitionsMap.put(ecucV, (EcucContainerValue) ecucRefV.getValue());

            }
          }
        }
      }
    }
  }

  private void updateEcucParitiontoSwpsMap(final List<EcucContainerValue> ecucPartitions) {
    for (EcucContainerValue ecucV : ecucPartitions) {
      if (!ecucV.getReferenceValues().isEmpty()) {
        for (EcucAbstractReferenceValue ecuAbsRef : ecucV.getReferenceValues()) {
          if (ecuAbsRef instanceof EcucInstanceReferenceValue) {
            EcucInstanceReferenceValue ecucRefV = (EcucInstanceReferenceValue) ecuAbsRef;
            if ((ecucRefV.getDefinition() != null) && !ecucRefV.getDefinition().eIsProxy() &&
                (ecucRefV.getValue() != null) && !ecucRefV.getValue().eIsProxy() &&
                (ecucRefV.getValue().getTarget() != null) &&
                (!ecucRefV.getValue().eIsProxy() && GenerateArxmlUtil.getPackagePath(ecucRefV.getDefinition())
                    .equals(this.props.get(RteConfigGeneratorConstants.ECUC_PARTITION_PARAM_DEF_AR_PACKAGE_PATH) +
                        "/EcucPartitionCollection/EcucPartition/EcucPartitionSoftwareComponentInstanceRef"))) {
              List<SwComponentPrototype> list = this.ecucParitiontoSwpsMap.get(ecucV);

              if (list != null) {
                if (ecucRefV.getValue().getTarget() instanceof SwComponentPrototype) {
                  list.add((SwComponentPrototype) ecucRefV.getValue().getTarget());
                }
              }
              else {
                if (ecucRefV.getValue().getTarget() instanceof SwComponentPrototype) {
                  list = new ArrayList<>();
                  list.add((SwComponentPrototype) ecucRefV.getValue().getTarget());
                  this.ecucParitiontoSwpsMap.put(ecucV, list);
                }
              }
            }
          }
          else if (ecuAbsRef instanceof EcucReferenceValue) {
            EcucReferenceValue ecucRefV = (EcucReferenceValue) ecuAbsRef;
            if ((ecucRefV.getDefinition() != null) && !ecucRefV.getDefinition().eIsProxy() &&
                (ecucRefV.getValue() != null) && !ecucRefV.getValue().eIsProxy() &&
                (!ecucRefV.getValue().eIsProxy() && GenerateArxmlUtil.getPackagePath(ecucRefV.getDefinition())
                    .equals(this.props.get(RteConfigGeneratorConstants.ECUC_PARTITION_PARAM_DEF_AR_PACKAGE_PATH) +
                        "/EcucPartitionCollection/EcucPartition/EcucPartitionSoftwareComponentInstanceRef"))) {
              List<SwComponentPrototype> list = this.ecucParitiontoSwpsMap.get(ecucV);

              if (list != null) {
                if (ecucRefV.getValue() instanceof SwComponentPrototype) {
                  list.add((SwComponentPrototype) ecucRefV.getValue());
                }
              }
              else {
                if (ecucRefV.getValue() instanceof SwComponentPrototype) {
                  list = new ArrayList<>();
                  list.add((SwComponentPrototype) ecucRefV.getValue());
                  this.ecucParitiontoSwpsMap.put(ecucV, list);
                }
              }
            }
          }
        }
      }
    }
  }


  private void updateSwtToSwpMap1(final RootSwCompositionPrototype rootSwCompositionPrototype) {

    CompositionSwComponentType softwareComposition = rootSwCompositionPrototype.getSoftwareComposition();


    List<SwComponentPrototype> swpList = new ArrayList<>();
    for (SwComponentPrototype swp : softwareComposition.getComponents()) {

      if ((swp.getType() != null) && ((swp.getType() instanceof ApplicationSwComponentType) ||
          (swp.getType() instanceof ComplexDeviceDriverSwComponentType) ||
          (swp.getType() instanceof SensorActuatorSwComponentType) ||
          (swp.getType() instanceof ServiceSwComponentType) ||
          (swp.getType() instanceof EcuAbstractionSwComponentType) ||
          (swp.getType() instanceof NvBlockSwComponentType))) {

        this.swcpToSwcMap1.put(swp, swp.getType());

        List<SwComponentPrototype> list = this.swtToswpMap1.get(swp.getType());
        if (list != null) {
          list.add(swp);
        }
        else {
          list = new ArrayList<>();
          list.add(swp);
          this.swtToswpMap1.put(swp.getType(), list);
        }

        swpList.add(swp);
      }
    }

    List<FlatInstanceDescriptor> fldInstances = GenerateArxmlUtil
        .getAllInstancesOf(this.editingDomain.getResourceSet().getResources(), FlatInstanceDescriptor.class, false);

    Map<FlatInstanceDescriptor, SwComponentPrototype> collection = fldInstances.stream()
        .filter(fld -> (fld.getEcuExtractReference() != null) && (fld.getEcuExtractReference().getTarget() != null) &&
            (fld.getEcuExtractReference().getTarget() instanceof SwComponentPrototype))
        .filter(fld -> GenerateArxmlUtil.listContainsElements(swpList, fld.getEcuExtractReference().getTarget()))
        .collect(Collectors.toMap(fld -> fld, fld -> (SwComponentPrototype) fld.getEcuExtractReference().getTarget()));
    if ((collection != null) && !collection.isEmpty()) {
      this.fldToswpMap1.putAll(collection);
    }

    try {
      for (SwComponentPrototype swp : swpList) {

        AtomicSwComponentType type = (AtomicSwComponentType) swp.getType();
        if (this.ecucParitiontoSwpsMap.keySet() != null) {
          Optional<EcucContainerValue> findFirst = this.ecucParitiontoSwpsMap.keySet().stream()
              .filter(k -> GenerateArxmlUtil.listContainsElements(this.ecucParitiontoSwpsMap.get(k), swp)).findFirst();
          if (findFirst.isPresent()) {

            List<EcucContainerValue> list = this.swptoEcucPartionsMap1.get(swp);
            if (list != null) {
              list.add(findFirst.get());
            }
            else {
              list = new ArrayList<>();
              list.add(findFirst.get());
              this.swptoEcucPartionsMap1.put(swp, list);
            }
          }
        }

        for (SwcInternalBehavior swIB : type.getInternalBehaviors()) {

          List<EcucContainerValue> findFirst = swIB.getEvents().stream()
              .map(e -> this.rteEventURIToTaskMap.get(EcoreResourceUtil.getURI(e).fragment().split("\\?")[0].trim()))
              .collect(Collectors.toList());


          if (!findFirst.isEmpty()) {

            List<EcucContainerValue> collect2 = findFirst.stream().filter(e -> e != null).collect(Collectors.toList())
                .stream().filter(distinctByKey(e -> e.getShortName())).collect(Collectors.toList());
            this.swptoTasksMap1.put(swp, collect2);

          }
          if ((this.swptoTasksMap1.get(swp) != null) && !this.swptoTasksMap1.get(swp).isEmpty()) {

            List<List<EcucContainerValue>> findFirst2 = this.swptoTasksMap1.get(swp).stream()
                .map(t -> this.osApptoTasksMap.keySet().stream()
                    .filter(o -> GenerateArxmlUtil.listContainsElements(this.osApptoTasksMap.get(o), t))
                    .collect(Collectors.toList()))
                .collect(Collectors.toList());


            if (!findFirst2.isEmpty()) {
              List<List<EcucContainerValue>> collect =
                  findFirst2.stream().filter(e -> (e != null) && !e.isEmpty()).collect(Collectors.toList());
              if (!collect.isEmpty()) {
                List<EcucContainerValue> collect2 = collect.stream().map(e -> e.get(0))
                    .filter(distinctByKey(e -> e.getShortName())).collect(Collectors.toList());
                this.swptoOsAppsMap1.put(swp, collect2);
              }
            }
          }
          else {
            List<Entry<SwComponentPrototype, List<EcucContainerValue>>> ecucPartition =
                this.swptoEcucPartionsMap1.entrySet().stream()
                    .filter(e -> GenerateArxmlUtil.isSameElement(swp, e.getKey())).collect(Collectors.toList());

            if (!ecucPartition.isEmpty()) {
              List<Entry<EcucContainerValue, EcucContainerValue>> collect1 =
                  this.osApptoEcucParitionsMap.entrySet().stream()
                      .filter(
                          o -> GenerateArxmlUtil.isSameElement(ecucPartition.get(0).getValue().get(0), o.getValue()))
                      .collect(Collectors.toList());
              if (!collect1.isEmpty()) {

                List<EcucContainerValue> list = this.swptoOsAppsMap1.get(swp);
                if (list != null) {
                  list.add(collect1.get(0).getKey());
                }
                else {
                  list = new ArrayList<>();
                  list.add(collect1.get(0).getKey());
                  this.swptoOsAppsMap1.put(swp, list);
                }
              }
            }
          }

          List<RTEEvent> collect =
              swIB.getEvents().stream().filter(r -> r instanceof DataReceivedEvent).collect(Collectors.toList());
          if (!collect.isEmpty()) {
            this.swptoDREMap1.put(swp, collect);
          }
        }
      }
    }
    catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }

  private static <T> Predicate<T> distinctByKey(final Function<? super T, Object> keyExtractor) {
    Map<Object, Boolean> map = new ConcurrentHashMap<>();
    return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }


  private void updateSwtToSwpMap2(final RootSwCompositionPrototype rootSwCompositionPrototype) {

    CompositionSwComponentType softwareComposition = rootSwCompositionPrototype.getSoftwareComposition();


    List<SwComponentPrototype> swpList = new ArrayList<>();
    for (SwComponentPrototype swp : softwareComposition.getComponents()) {

      if ((swp.getType() != null) && (swp.getType() instanceof ParameterSwComponentType)) {

        List<SwComponentPrototype> list = this.swtToswpMap2.get(swp.getType());
        this.swcpToswcMapParam.put(swp, swp.getType());
        if (list != null) {
          list.add(swp);
        }
        else {
          list = new ArrayList<>();
          list.add(swp);
          this.swtToswpMap2.put(swp.getType(), list);
        }
        swpList.add(swp);
      }
    }

    List<FlatInstanceDescriptor> fldInstances = GenerateArxmlUtil
        .getAllInstancesOf(this.editingDomain.getResourceSet().getResources(), FlatInstanceDescriptor.class, false);

    Map<FlatInstanceDescriptor, SwComponentPrototype> collection = fldInstances.stream()
        .filter(fld -> (fld.getEcuExtractReference() != null) && (fld.getEcuExtractReference().getTarget() != null) &&
            (fld.getEcuExtractReference().getTarget() instanceof SwComponentPrototype))
        .filter(fld -> GenerateArxmlUtil.listContainsElements(swpList, fld.getEcuExtractReference().getTarget()))
        .collect(Collectors.toMap(fld -> fld, fld -> (SwComponentPrototype) fld.getEcuExtractReference().getTarget()));

    if ((collection != null) && !collection.isEmpty()) {
      this.fldToswpMap2.putAll(collection);
    }

    for (SwComponentPrototype swp : swpList) {
      if (this.ecucParitiontoSwpsMap.keySet() != null) {
        Optional<EcucContainerValue> findFirst = this.ecucParitiontoSwpsMap.keySet().stream()
            .filter(k -> GenerateArxmlUtil.listContainsElements(this.ecucParitiontoSwpsMap.get(k), swp)).findFirst();
        if (findFirst.isPresent()) {

          List<EcucContainerValue> list = this.swptoEcucPartionsMap2.get(swp);
          if (list != null) {
            list.add(findFirst.get());
          }
          else {
            list = new ArrayList<>();
            list.add(findFirst.get());
            this.swptoEcucPartionsMap2.put(swp, list);
          }
        }
      }
    }

    for (Entry<SwComponentPrototype, List<EcucContainerValue>> e : this.swptoEcucPartionsMap2.entrySet()) {
      if (!e.getValue().isEmpty() && (e.getValue().size() == 1)) {

        List<Entry<EcucContainerValue, EcucContainerValue>> collect = this.osApptoEcucParitionsMap.entrySet().stream()
            .filter(o -> GenerateArxmlUtil.isSameElement(e.getValue().get(0), o.getValue()))
            .collect(Collectors.toList());
        if (!collect.isEmpty()) {

          List<EcucContainerValue> list = this.swptoOsAppsMap2.get(e.getKey());
          if (list != null) {
            list.add(collect.get(0).getKey());
          }
          else {
            list = new ArrayList<>();
            list.add(collect.get(0).getKey());
            this.swptoOsAppsMap2.put(e.getKey(), list);
          }
        }
      }
    }
  }


  private void updateSwtToSwpMap3(final RootSwCompositionPrototype rootSwCompositionPrototype) {

    CompositionSwComponentType softwareComposition = rootSwCompositionPrototype.getSoftwareComposition();


    List<SwComponentPrototype> swpList = new ArrayList<>();
    for (SwComponentPrototype swp : softwareComposition.getComponents()) {

      if ((swp.getType() != null) && (swp.getType() instanceof NvBlockSwComponentType)) {

        List<SwComponentPrototype> list = this.swtToswpMap3.get(swp.getType());
        this.swcpToswcMapNV.put(swp, swp.getType());
        if (list != null) {
          list.add(swp);
        }
        else {
          list = new ArrayList<>();
          list.add(swp);
          this.swtToswpMap3.put(swp.getType(), list);
        }
        swpList.add(swp);
      }
    }

    List<FlatInstanceDescriptor> fldInstances = GenerateArxmlUtil
        .getAllInstancesOf(this.editingDomain.getResourceSet().getResources(), FlatInstanceDescriptor.class, false);

    Map<FlatInstanceDescriptor, SwComponentPrototype> collection = fldInstances.stream()
        .filter(fld -> (fld.getEcuExtractReference() != null) && (fld.getEcuExtractReference().getTarget() != null) &&
            (fld.getEcuExtractReference().getTarget() instanceof SwComponentPrototype))
        .filter(fld -> GenerateArxmlUtil.listContainsElements(swpList, fld.getEcuExtractReference().getTarget()))
        .collect(Collectors.toMap(fld -> fld, fld -> (SwComponentPrototype) fld.getEcuExtractReference().getTarget()));

    if ((collection != null) && !collection.isEmpty()) {
      this.fldToswpMap3.putAll(collection);
    }


    for (SwComponentPrototype swp : swpList) {

      AtomicSwComponentType type = (AtomicSwComponentType) swp.getType();

      for (SwcInternalBehavior swIB : type.getInternalBehaviors()) {

        List<List<EcucContainerValue>> findFirst = swIB.getEvents().stream()
            .map(e -> this.tasktoRteEventsMap.keySet().stream()
                .filter(m -> GenerateArxmlUtil.listContainsElements(this.tasktoRteEventsMap.get(m), e))
                .collect(Collectors.toList()))
            .collect(Collectors.toList());
        List<RTEEvent> collect =
            swIB.getEvents().stream().filter(r -> r instanceof DataReceivedEvent).collect(Collectors.toList());
        if (!collect.isEmpty()) {
          this.swptoDREMap3.put(swp, collect);
        }
      }

      Optional<EcucContainerValue> findFirst = this.ecucParitiontoSwpsMap.keySet().stream()
          .filter(k -> GenerateArxmlUtil.listContainsElements(this.ecucParitiontoSwpsMap.get(k), swp)).findFirst();
      if (findFirst.isPresent()) {

        List<EcucContainerValue> list = this.swptoEcucPartionsMap3.get(swp);
        if (list != null) {
          list.add(findFirst.get());
        }
        else {
          list = new ArrayList<>();
          list.add(findFirst.get());
          this.swptoEcucPartionsMap3.put(swp, list);
        }
      }
    }

    for (Entry<SwComponentPrototype, List<EcucContainerValue>> e : this.swptoEcucPartionsMap3.entrySet()) {
      if (!e.getValue().isEmpty() && (e.getValue().size() == 1)) {

        List<Entry<EcucContainerValue, EcucContainerValue>> collect = this.osApptoEcucParitionsMap.entrySet().stream()
            .filter(o -> GenerateArxmlUtil.isSameElement(e.getValue().get(0), o.getValue()))
            .collect(Collectors.toList());
        if (!collect.isEmpty()) {

          List<EcucContainerValue> list = this.swptoOsAppsMap3.get(e.getKey());
          if (list != null) {
            list.add(collect.get(0).getKey());
          }
          else {
            list = new ArrayList<>();
            list.add(collect.get(0).getKey());
            this.swptoOsAppsMap3.put(e.getKey(), list);
          }
        }
      }
    }
  }

  private void updateTasktoBSWEventsMap(final List<EcucContainerValue> ecucContainerValues) {

    List<EcucContainerValue> rteBswEventtoTasksMappings = ecucContainerValues.stream()
        .filter(c -> GenerateArxmlUtil.getPackagePath(c.getDefinition())
            .equals(this.props.get(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH) +
                "/RteBswModuleInstance/RteBswEventToTaskMapping"))
        .collect(Collectors.toList());

    for (EcucContainerValue ecucV : rteBswEventtoTasksMappings) {
      if (!ecucV.getReferenceValues().isEmpty()) {
        EcucReferenceValue eventCValue = null;
        EcucReferenceValue taskCValue = null;
        for (EcucAbstractReferenceValue ecuAbsRef : ecucV.getReferenceValues()) {
          if (ecuAbsRef instanceof EcucReferenceValue) {
            EcucReferenceValue ecucRefV = (EcucReferenceValue) ecuAbsRef;

            if ((ecucRefV.getDefinition() != null) && !ecucRefV.getDefinition().eIsProxy() &&
                (ecucRefV.getValue() != null) && !ecucRefV.getValue().eIsProxy() &&
                GenerateArxmlUtil.getPackagePath(ecucRefV.getDefinition())
                    .equals(this.props.get(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH) +
                        "/RteBswModuleInstance/RteBswEventToTaskMapping/RteBswEventRef")) {
              eventCValue = ecucRefV;
            }
            else if ((ecucRefV.getDefinition() != null) && !ecucRefV.getDefinition().eIsProxy() &&
                (ecucRefV.getValue() != null) && !ecucRefV.getValue().eIsProxy() &&
                GenerateArxmlUtil.getPackagePath(ecucRefV.getDefinition())
                    .equals(this.props.get(RteConfigGeneratorConstants.RTE_ECUC_PARAM_DEF_AR_PACKAGE_PATH) +
                        "/RteBswModuleInstance/RteBswEventToTaskMapping/RteBswMappedToTaskRef")) {
              taskCValue = ecucRefV;
            }
          }
        }

        if ((eventCValue != null) && (taskCValue != null) && (eventCValue.getValue() instanceof BswTimingEvent) &&
            (taskCValue.getValue() instanceof EcucContainerValue)) {
          BswTimingEvent rteEvent = (BswTimingEvent) eventCValue.getValue();
          EcucContainerValue task = (EcucContainerValue) taskCValue.getValue();
          List<BswTimingEvent> list = this.tasktoBSWEventsMap.get(task);
          if (list != null) {
            list.add(rteEvent);
          }
          else {
            list = new ArrayList<>();
            list.add(rteEvent);
            this.tasktoBSWEventsMap.put(task, list);
          }
        }
      }
    }

    for (Entry<EcucContainerValue, List<RTEEvent>> entry : this.tasktoRteEventsMap.entrySet()) {
      for (RTEEvent rteEvent : entry.getValue()) {
        this.rteEventToTaskMap.put(rteEvent, entry.getKey());

      }
    }
  }
}
