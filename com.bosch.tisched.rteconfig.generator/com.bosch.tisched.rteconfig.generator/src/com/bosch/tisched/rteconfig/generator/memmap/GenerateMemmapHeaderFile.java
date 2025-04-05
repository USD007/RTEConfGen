/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.memmap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;

import com.bosch.tisched.rteconfig.generator.core.OsConfigToEcucValueMapping;
import com.bosch.tisched.rteconfig.generator.core.SwComponentInstanceTypeEnum;
import com.bosch.tisched.rteconfig.generator.core.TischedComponentInstance;
import com.bosch.tisched.rteconfig.generator.util.AutosarUtil;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;

import autosar40.bswmodule.bswimplementation.BswImplementation;
import autosar40.bswmodule.bswoverview.BswModuleDescription;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucInstanceReferenceValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.genericstructure.abstractstructure.AtpFeature;
import autosar40.genericstructure.generaltemplateclasses.anyinstanceref.AnyInstanceRef;
import autosar40.swcomponent.composition.CompositionSwComponentType;
import autosar40.swcomponent.composition.SwComponentPrototype;
import autosar40.util.Autosar40ReleaseDescriptor;
import gautosar.gecucdescription.GConfigReferenceValue;
import gautosar.gecucdescription.GContainer;

/**
 * @author SHK1COB
 */
public class GenerateMemmapHeaderFile {


  private final IProject project;
  private final String swCompositionTypePath;
  private String excludeDir = "";
  private final String memmapHeaderFilesOutputDir;
  private final String listMemmapHeaderFiles;
  private final String outputPath;
  private final EcucModuleConfigurationValues createEcucModuleConfigurationValue;
  private final OsConfigToEcucValueMapping osConfigToEcucValueMapping;
  private final String rteConfGenDir;
  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(GenerateMemmapHeaderFile.class.getName());

  /**
   * @param createEcucModuleConfigurationValue EcucModuleConfigurationValues
   * @param project IProject
   * @param map Map<String, String>
   * @param osConfigToEcucValueMapping OsConfigToEcucValueMapping
   */
  public GenerateMemmapHeaderFile(final EcucModuleConfigurationValues createEcucModuleConfigurationValue,
      final IProject project, final Map<String, String> map,
      final OsConfigToEcucValueMapping osConfigToEcucValueMapping) {
    this.project = project;
    this.createEcucModuleConfigurationValue = createEcucModuleConfigurationValue;
    this.swCompositionTypePath = map.get(RteConfigGeneratorConstants.ECU_EXTRACT_REFER_COMPOSITION_SW_COMPONENT_TYPE);
    this.excludeDir = map.get(RteConfigGeneratorConstants.EXCLUDE_ARXML_PATH);
    this.memmapHeaderFilesOutputDir = map.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR).trim()
        .substring(1, map.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR).length());
    this.listMemmapHeaderFiles = map.get(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_MEMMAP_HEADER_FILES);
    this.outputPath = map.get(RteConfigGeneratorConstants.EXCLUDE_ARXML_PATH);
    this.osConfigToEcucValueMapping = osConfigToEcucValueMapping;
    this.rteConfGenDir = map.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR);

  }


  /**
   * @throws Exception
   */
  public void generateMemmapHeaderFiles() throws Exception {

    try {

      LOGGER.info("Generating memmap header files");

      List<CompositionSwComponentType> list = GenerateArxmlUtil.getListOfEObject(this.project,
          this.swCompositionTypePath, CompositionSwComponentType.class, this.excludeDir);

      Map<String, String> memmapHeaderBSwMdMap = getMemmapHeaderBSwMdMap();

      if ((list != null) && (list.size() == 1)) {

        Map<String, List<String>> componentOsApplicationMap = getComponentOsApplicationMap();

        CompositionSwComponentType compositionSwComponentType = list.get(0);

        EList<SwComponentPrototype> components = compositionSwComponentType.getComponents();

        Map<String, String> comMap = new HashMap<String, String>();
        List<String> memmapBuf = new ArrayList<>();
        memmapBuf.add("#if (0)");
        memmapBuf.add("LINE_BREAK");

        for (SwComponentPrototype swComponentPrototype : components) {

          AutosarUtil.setCurrentProcessingEObject(swComponentPrototype);

          String shortName = swComponentPrototype.getType().getShortName();

          if (comMap.get(shortName) == null) {


            String bswmdName = memmapHeaderBSwMdMap.get(swComponentPrototype.getType().getShortName() + "_MemMap.h");

            if ((bswmdName != null) && !bswmdName.isEmpty()) {
              LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("232_0", bswmdName).trim());

            }
            else {


              Map<String, TischedComponentInstance> tischedComponentInstances =
                  this.osConfigToEcucValueMapping.getTischedComponentInstances();


              TischedComponentInstance tischedComponentInstance =
                  tischedComponentInstances.get(swComponentPrototype.getShortName());

              if ((tischedComponentInstance != null) &&
                  (tischedComponentInstance
                      .getSwComponentInstanceTypeEnum() == SwComponentInstanceTypeEnum.SW_COMPONENT_PROTOTYPE) &&
                  !tischedComponentInstance.isGenerateMemmapHeader()) {

                continue;
              }


              InputStream resourceAsStream = getClass().getResource("Template_MemMap.h").openStream();

              int i;
              StringBuilder fileData = new StringBuilder();


              // reads till the end of the stream
              while ((i = resourceAsStream.read()) != -1) {
                fileData.append((char) i);
              }
              resourceAsStream.close();

              List<String> list2 = componentOsApplicationMap.get(shortName);

              if (list2 != null) {

                String osAppName = "";
                if (list2.size() > 1) {
                  Collections.sort(list2);
                  for (String str : list2) {
                    osAppName = osAppName.isEmpty() ? osAppName : osAppName + "_";
                    osAppName = osAppName + str;
                  }
                }
                else {
                  osAppName = list2.get(0);
                }

                String replaceData = fileData.toString().replaceAll("<COMPONENT_UC>", shortName.toUpperCase());
                replaceData = replaceData.replaceAll("<COMPONENT_CC>", shortName);
                replaceData = replaceData.replaceAll("<OS_APPLICATION>", osAppName);

                String startSec = "#elif defined RTECONFGEN_START_SEC_" + osAppName + "_CODE";
                if (!memmapBuf.contains(startSec)) {
                  memmapBuf.add(startSec);
                  memmapBuf.add("   /* pragma for start code section */");
                  memmapBuf.add("   #undef RTECONFGEN_START_SEC_" + osAppName + "_CODE");
                  memmapBuf.add("LINE_BREAK");
                  memmapBuf.add("#elif defined RTECONFGEN_STOP_SEC_" + osAppName + "_CODE");
                  memmapBuf.add("   /* pragma for stop code section */");
                  memmapBuf.add("   #undef RTECONFGEN_STOP_SEC_" + osAppName + "_CODE");
                  memmapBuf.add("LINE_BREAK");
                }


                BufferedWriter writer = null;
                FileWriter fileWriter = null;
                try {

                  String dirPath = this.project.getLocation().toOSString() + "/" + this.memmapHeaderFilesOutputDir;
                  String pathh = dirPath + "/" + swComponentPrototype.getType().getShortName() + "_MemMap.h";

                  if (!new File(dirPath).exists()) {

                    boolean mkdir = new File(dirPath).mkdirs();
                    if (!mkdir) {
                      RteConfigGeneratorLogger.logErrormessage(LOGGER,
                          RteConfGenMessageDescription.getFormattedMesssage("309_0", dirPath).trim());

                    }
                  }

                  if (!new File(pathh).exists()) {

                    boolean mkdir = new File(pathh).createNewFile();
                    if (!mkdir) {
                      RteConfigGeneratorLogger.logErrormessage(LOGGER,
                          RteConfGenMessageDescription.getFormattedMesssage("310_0", pathh).trim());

                    }

                  }
                  fileWriter = new FileWriter(pathh, false);
                  writer = new BufferedWriter(fileWriter);
                  writer.append(new StringBuilder(replaceData));
                  writer.flush();
                }
                catch (IOException ex) {
                  LOGGER.error("IOException occured " + ex.getMessage());
                  RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                      .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());

                }
                finally {
                  try {
                    if (writer != null) {
                      writer.close();
                    }
                    if (fileWriter != null) {
                      fileWriter.close();
                    }
                  }
                  catch (IOException ex) {
                    LOGGER.error("IOException occured " + ex.getMessage());
                    RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                        .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());

                  }
                }
                LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("115_0", this.memmapHeaderFilesOutputDir,
                    swComponentPrototype.getType().getShortName()).trim());

                comMap.put(shortName, this.memmapHeaderFilesOutputDir + "/" +
                    swComponentPrototype.getType().getShortName() + "_MemMap.h");

              }
              else {
                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("236_0").trim());
              }
            }
          }
        }

        memmapBuf.add("#else");
        memmapBuf.add(" #error \"RteConfGen_MemMap.h, wrong pragma command\"");
        memmapBuf.add("#endif");
        memmapBuf.add("LINE_BREAK");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
            new File(this.project.getLocation().toOSString() + "//" + this.listMemmapHeaderFiles))))) {

          for (String path : comMap.values()) {
            bw.write(path);
            bw.newLine();
          }
          bw.flush();

        }
        catch (Exception ex) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER, ex.getLocalizedMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
            new File(this.project.getLocation().toOSString() + this.rteConfGenDir + "//RteConfGen_MemMap.ht"))))) {

          for (String value : memmapBuf) {
            if (value.equals("LINE_BREAK")) {
              bw.newLine();
              bw.newLine();
            }
            else {
              bw.write(value);
              bw.newLine();
            }

          }
          bw.flush();

        }
        catch (Exception ex) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER, ex.getLocalizedMessage());
        }

      }
      else {
        LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("237_0").trim());
      }
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("190_0").trim());
    }
    catch (IOException ex) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER, ex.getLocalizedMessage());
    }


  }


  private Map<String, List<String>> getComponentOsApplicationMap() {
    Map<String, List<String>> map = new HashMap<String, List<String>>();


    if (this.createEcucModuleConfigurationValue != null) {

      for (EcucContainerValue ecucContainerValue : this.createEcucModuleConfigurationValue.getContainers()) {

        AutosarUtil.setCurrentProcessingEObject(ecucContainerValue);

        EList<GContainer> gGetSubContainers = ecucContainerValue.gGetSubContainers();

        for (GContainer gContainer : gGetSubContainers) {

          String gGetShortName = gContainer.gGetShortName();
          gGetShortName = gGetShortName.startsWith("EcucPartition_")
              ? gGetShortName.substring("EcucPartition_".length(), gGetShortName.length()) : gGetShortName;

          EList<GConfigReferenceValue> gGetReferenceValues = gContainer.gGetReferenceValues();

          for (GConfigReferenceValue referenceValue : gGetReferenceValues) {

            if (referenceValue instanceof EcucInstanceReferenceValue) {

              EcucInstanceReferenceValue ecucInstanceReferenceValue = (EcucInstanceReferenceValue) referenceValue;

              AnyInstanceRef value = ecucInstanceReferenceValue.getValue();

              if ((value != null) && (value.getTarget() != null) &&
                  (value.getTarget() instanceof SwComponentPrototype)) {

                AtpFeature atpFeature = (value.getTarget().eIsProxy()
                    ? (AtpFeature) EcoreUtil.resolve(value.getTarget(), getResourceSet()) : value.getTarget());

                if ((atpFeature != null) && (!atpFeature.eIsProxy())) {

                  SwComponentPrototype swc = (SwComponentPrototype) atpFeature;
                  List<String> list = map.get(swc.getType().getShortName());

                  if (list != null) {
                    list.add(gGetShortName);
                  }
                  else {
                    ArrayList<String> arrayL = new ArrayList<String>();
                    if (!arrayL.contains(gGetShortName)) {
                      arrayL.add(gGetShortName);
                    }
                    map.put(swc.getType().getShortName(), arrayL);
                  }
                }
              }
            }
          }
        }
      }
    }
    return map;

  }


  private ResourceSet getResourceSet() {
    final TransactionalEditingDomain currentEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(this.project, Autosar40ReleaseDescriptor.INSTANCE);
    if (currentEditingDomain != null) {

      return currentEditingDomain.getResourceSet();
    }
    return null;

  }


  private Map<String, String> getMemmapHeaderBSwMdMap() {

    Map<String, String> mmap = new HashMap<String, String>();
    List<BswModuleDescription> listOfEObject1 =
        GenerateArxmlUtil.getListOfEObject(this.project, BswModuleDescription.class, this.outputPath);

    List<BswImplementation> listOfEObject2 =
        GenerateArxmlUtil.getListOfEObject(this.project, BswImplementation.class, this.outputPath);

    for (BswImplementation bswimpl : listOfEObject2) {
      AutosarUtil.setCurrentProcessingEObject(bswimpl);
      if ((bswimpl.getResourceConsumption() != null) &&
          (bswimpl.getResourceConsumption().getMemorySections() != null) &&
          !bswimpl.getResourceConsumption().getMemorySections().isEmpty() && (bswimpl.getBehavior() != null)) {

        if (!bswimpl.getBehavior().eIsProxy() && (bswimpl.getBehavior().eContainer() instanceof BswModuleDescription)) {
          BswModuleDescription bswmd = (BswModuleDescription) bswimpl.getBehavior().eContainer();
          if (listOfEObject1.contains(bswmd)) {
            mmap.put(bswmd.getShortName() + "_MemMap.h", bswmd.getShortName());

          }
        }
      }
    }

    return mmap;

  }
}
