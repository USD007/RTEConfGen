/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.ecucpartion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import com.bosch.tisched.rteconfig.generator.core.OsConfigToEcucValueMapping;
import com.bosch.tisched.rteconfig.generator.core.SwComponentInstanceTypeEnum;
import com.bosch.tisched.rteconfig.generator.core.TischedComponentInstance;
import com.bosch.tisched.rteconfig.generator.core.TischedEvent;
import com.bosch.tisched.rteconfig.generator.core.TischedTask;
import com.bosch.tisched.rteconfig.generator.memmap.GenerateMemmapHeaderFile;
import com.bosch.tisched.rteconfig.generator.osconfig.OSAPPLICATIONIDMAPPINGType;
import com.bosch.tisched.rteconfig.generator.util.AutosarUtil;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;
import com.bosch.tisched.rteconfig.generator.util.VariationPointUtil;

import autosar40.autosartoplevelstructure.AUTOSAR;
import autosar40.autosartoplevelstructure.AutosartoplevelstructureFactory;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucInstanceReferenceValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.ecucparameterdef.EcucAbstractReferenceDef;
import autosar40.ecucparameterdef.EcucContainerDef;
import autosar40.ecucparameterdef.EcucModuleDef;
import autosar40.ecucparameterdef.EcucParamConfContainerDef;
import autosar40.genericstructure.generaltemplateclasses.anyinstanceref.AnyInstanceRef;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import autosar40.genericstructure.generaltemplateclasses.identifiable.IdentifiablePackage;
import autosar40.genericstructure.varianthandling.VariationPoint;
import autosar40.swcomponent.composition.SwComponentPrototype;
import autosar40.util.Autosar40Factory;

/**
 * @author shk1cob
 */
public class GenerateEcuCPartitionValue {


  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(GenerateEcuCPartitionValue.class.getName());

  private final String ecucPartitionParamDefPkgPath;
  private final String ecuPartitionecucvaluePkgPath;
  private final String autosarReleaseVersion;
  private final String autosarResourceVersion;
  private final IProject project;
  private final OsConfigToEcucValueMapping tischedToEcuCValueMapping;
  private final String outputFilePath;
  private final String enablememmapheaderfilegen;
  private final Map<String, String> map;

  private final String paramdefs[] = { "/EcucPartitionCollection", "/EcucPartitionCollection/EcucPartition" };

  /**
   * @param project IProject
   * @param map Map<String, String>
   * @param tischedToEcuCValueMapping TischedToEcuCValueMapping
   */
  public GenerateEcuCPartitionValue(final IProject project, final Map<String, String> map,
      final OsConfigToEcucValueMapping tischedToEcuCValueMapping) {
    this.project = project;
    this.map = map;
    this.ecucPartitionParamDefPkgPath = map.get(RteConfigGeneratorConstants.ECUC_PARTITION_PARAM_DEF_AR_PACKAGE_PATH);
    this.ecuPartitionecucvaluePkgPath = map.get(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_AR_PACKAGE_PATH);
    this.autosarReleaseVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION);
    this.autosarResourceVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION);
    this.tischedToEcuCValueMapping = tischedToEcuCValueMapping;
    this.outputFilePath = map.get(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH);
    this.enablememmapheaderfilegen = map.get(RteConfigGeneratorConstants.MEMMAP_HEADERFILE_GENERATION);
  }


  /**
   * @throws Exception
   */
  public void generateEcuCPartitionEcuCValue() throws Exception {

    LOGGER.info("*** Generating EcuC Partition EcuCValue file");

    String conPath = this.outputFilePath.substring(0, this.outputFilePath.lastIndexOf("/"));
    String fileName =
        this.outputFilePath.substring(this.outputFilePath.lastIndexOf("/") + 1, this.outputFilePath.length());

    // autosar instance
    final AUTOSAR arPartRoot = AutosartoplevelstructureFactory.eINSTANCE.createAUTOSAR();


    ARPackage subPackage = GenerateArxmlUtil.getARArPackage(arPartRoot, this.ecuPartitionecucvaluePkgPath);

    if (subPackage != null) {
      EcucModuleConfigurationValues createEcucModuleConfigurationValue =
          createEcucModuleConfigurationValue(this.outputFilePath);

      subPackage.getElements().add(createEcucModuleConfigurationValue);

      GenerateArxmlUtil.addDefaultvalueForMandatoryParamDefs(createEcucModuleConfigurationValue.getDefinition(),
          createEcucModuleConfigurationValue, Arrays.asList(this.paramdefs));


      this.tischedToEcuCValueMapping.getEcucContainerValues()
          .addAll(createEcucModuleConfigurationValue.getContainers());

      IResource findMember = this.project.findMember(conPath);
      URI resourceURI = null;
      if (findMember != null) {
        resourceURI = URI.createPlatformResourceURI(findMember.getFullPath().toOSString() + "/" + fileName, false);
      }
      else {
        resourceURI = URI.createPlatformResourceURI(
            URI.createFileURI(this.project.getFullPath().toOSString() + conPath).path() + "/" + fileName, false);
      }


      IStatus saveFile = GenerateArxmlUtil.saveFile(this.project, arPartRoot, resourceURI,
          AutosarUtil.getMetaModelDescriptorByAutosarResoureVersion(
              AutosarUtil.getMetaModelDescriptorByAutosarReleaseVersion(this.autosarReleaseVersion),
              this.autosarResourceVersion));

      if (saveFile.isOK()) {
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("170_2",
            URI.createFileURI(this.project.getFullPath().toOSString() + conPath).path(), fileName).trim());

        if (this.enablememmapheaderfilegen
            .equalsIgnoreCase(RteConfigGeneratorConstants.ENABLE_MEMMAP_HEADERFILE_GENERATION)) {
          new GenerateMemmapHeaderFile(createEcucModuleConfigurationValue, this.project, this.map,
              this.tischedToEcuCValueMapping).generateMemmapHeaderFiles();
        }

      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("399_1").trim());

      }
    }
    else {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_1").trim());
    }


  }


  private EcucContainerValue getPartitionCollectionContainerValue(final String osAppName,
      final Map<String, EcucContainerValue> map1, final EcucParamConfContainerDef ecucParamConfContainerDef,
      final EcucModuleConfigurationValues createEcucModuleConfigurationValues) {
    EcucContainerValue ecucContainerValue = null;
    EcucContainerValue ecucContainerValue1 = this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap().get(osAppName);
    if ((ecucContainerValue1 != null) && (ecucContainerValue1.eContainer() != null) &&
        (ecucContainerValue1.eContainer() instanceof EcucContainerValue)) {

      String shortName = ((EcucContainerValue) ecucContainerValue1.eContainer()).getShortName();

      if ((shortName != null) && !shortName.isEmpty()) {

        EcucContainerValue ecucContainerValue2 = map1.get(shortName);

        if (ecucContainerValue2 != null) {
          return ecucContainerValue2;
        }

        EcucContainerValue createEcucContainerValue = createEcucContainerValue(ecucParamConfContainerDef, shortName);
        createEcucModuleConfigurationValues.getContainers().add(createEcucContainerValue);
        map1.put(shortName, createEcucContainerValue);
        return createEcucContainerValue;


      }
    }
    return ecucContainerValue;
  }

  private Map<String, EcucContainerValue> getPartitionCollectionMap(
      final EcucModuleConfigurationValues createEcucModuleConfigurationValues) {
    Map<String, EcucContainerValue> map1 = new HashMap<String, EcucContainerValue>();
    for (EcucContainerValue ecuCContainerValue : createEcucModuleConfigurationValues.getContainers()) {
      map1.put(ecuCContainerValue.getShortName(), ecuCContainerValue);
    }
    return map1;
  }


  private EcucContainerValue createEcucContainerValue(final EcucParamConfContainerDef ecucParamConfContainerDef,
      final String shortName) {

    EcucContainerValue createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
    createEcucContainerValue.setShortName(shortName);
    createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
    createEcucContainerValue.setDefinition(ecucParamConfContainerDef);
    return createEcucContainerValue;
  }


  private EcucModuleConfigurationValues createEcucModuleConfigurationValue(final String containerPath) {
    EcucModuleConfigurationValues createEcucModuleConfigurationValues =
        Autosar40Factory.eINSTANCE.createEcucModuleConfigurationValues();
    createEcucModuleConfigurationValues.setShortName("EcuC");
    createEcucModuleConfigurationValues.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());

    EcucModuleDef ecuCModuleDef = GenerateArxmlUtil.getEObject(this.project, this.ecucPartitionParamDefPkgPath,
        EcucModuleDef.class, containerPath);
    if (ecuCModuleDef != null) {

      createEcucModuleConfigurationValues.setDefinition(ecuCModuleDef);

      EcucContainerDef ecucContainerDef = getEcucContainerDef(ecuCModuleDef, "EcucPartitionCollection");

      if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {

        Map<String, EcucContainerValue> partitionCollectionMap =
            getPartitionCollectionMap(createEcucModuleConfigurationValues);

        EcucParamConfContainerDef ecucParamConfContainerDef = (EcucParamConfContainerDef) ecucContainerDef;

        List<OSAPPLICATIONIDMAPPINGType> osApplicationMappings =
            this.tischedToEcuCValueMapping.getOsApplicationMappings();

        for (OSAPPLICATIONIDMAPPINGType osApplicationMapping : osApplicationMappings) {

          EcucContainerValue partitionCollectionContainerValue =
              getPartitionCollectionContainerValue(osApplicationMapping.getOSAPPLICATIONID(), partitionCollectionMap,
                  ecucParamConfContainerDef, createEcucModuleConfigurationValues);

          if (partitionCollectionContainerValue != null) {

            AutosarUtil.setCurrentProcessingEObject(partitionCollectionContainerValue);

            EcucContainerValue ecucPartition = getEcucPartition(partitionCollectionContainerValue,
                osApplicationMapping.getOSAPPLICATIONID(), this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap());
            if (ecucPartition == null) {
              EcucContainerValue createEcucPartition =
                  createEcucPartition(osApplicationMapping.getOSAPPLICATIONID(), partitionCollectionContainerValue,
                      ecucParamConfContainerDef, this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap());

              if (createEcucPartition != null) {
                partitionCollectionContainerValue.getSubContainers().add(createEcucPartition);
              }
            }
          }

        }

        for (TischedComponentInstance componentInstance : this.tischedToEcuCValueMapping.getTischedComponentInstances()
            .values()) {

          if (componentInstance
              .getSwComponentInstanceTypeEnum() == SwComponentInstanceTypeEnum.SW_COMPONENT_PROTOTYPE) {

            List<TischedEvent> eventlist = this.tischedToEcuCValueMapping.getComponentInstaceToEventsMap()
                .get(componentInstance.getTischedComponent().getShortName() + componentInstance.getShortName());
            if ((eventlist != null) && !eventlist.isEmpty()) {
              for (TischedEvent tischedEvent : eventlist) {

                TischedTask tischedTask = tischedEvent.getTischedTask();

                if ((tischedTask != null) && (tischedTask.getMappedOsApplication() != null)) {

                  EcucContainerValue partitionCollectionContainerValue =
                      getPartitionCollectionContainerValue(tischedTask.getMappedOsApplication(), partitionCollectionMap,
                          ecucParamConfContainerDef, createEcucModuleConfigurationValues);

                  if (partitionCollectionContainerValue != null) {

                    AutosarUtil.setCurrentProcessingEObject(partitionCollectionContainerValue);

                    EcucContainerValue ecucPartition = getEcucPartition(partitionCollectionContainerValue,
                        tischedTask.getMappedOsApplication(), this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap());

                    if (ecucPartition == null) {
                      EcucContainerValue createEcucPartition =
                          createEcucPartition(tischedTask.getMappedOsApplication(), partitionCollectionContainerValue,
                              ecucParamConfContainerDef, this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap());

                      if (createEcucPartition != null) {
                        partitionCollectionContainerValue.getSubContainers().add(createEcucPartition);
                        ecucPartition = createEcucPartition;
                      }
                    }

                    if ((ecucPartition != null) &&
                        (!checkIfEcucPartitionSoftwareComponentInstanceRefExists(ecucPartition,
                            (SwComponentPrototype) componentInstance.getComponentInstance()))) {

                      createEcucPartitionSoftwareComponentInstanceRef(ecucPartition,
                          (EcucParamConfContainerDef) ecucPartition.getDefinition(),
                          (SwComponentPrototype) componentInstance.getComponentInstance());

                    }
                  }
                }
              }
            }
          }
        }


        if (this.tischedToEcuCValueMapping.getOsAppSwCompPrototypesMap() != null) {

          for (String osApp : this.tischedToEcuCValueMapping.getOsAppSwCompPrototypesMap().keySet()) {

            List<SwComponentPrototype> list = this.tischedToEcuCValueMapping.getOsAppSwCompPrototypesMap().get(osApp);

            for (SwComponentPrototype swc : list) {

              AutosarUtil.setCurrentProcessingEObject(swc);

              EcucContainerValue partitionCollectionContainerValue = getPartitionCollectionContainerValue(osApp,
                  partitionCollectionMap, ecucParamConfContainerDef, createEcucModuleConfigurationValues);

              if (partitionCollectionContainerValue != null) {

                AutosarUtil.setCurrentProcessingEObject(partitionCollectionContainerValue);

                EcucContainerValue ecucPartition = getEcucPartition(partitionCollectionContainerValue, osApp,
                    this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap());

                if (ecucPartition == null) {
                  EcucContainerValue createEcucPartition = createEcucPartition(osApp, partitionCollectionContainerValue,
                      ecucParamConfContainerDef, this.tischedToEcuCValueMapping.getOsAppEcuPartitionMap());

                  if (createEcucPartition != null) {
                    partitionCollectionContainerValue.getSubContainers().add(createEcucPartition);
                    ecucPartition = createEcucPartition;
                  }
                }


                if ((ecucPartition != null) &&
                    (!checkIfEcucPartitionSoftwareComponentInstanceRefExists(ecucPartition, swc))) {
                  createEcucPartitionSoftwareComponentInstanceRef(ecucPartition,
                      (EcucParamConfContainerDef) ecucPartition.getDefinition(), swc);
                }
              }
            }
          }
        }
      }
    }

    return createEcucModuleConfigurationValues;
  }

  private EcucContainerValue getEcucPartition(final EcucContainerValue parentContainer,
      final String mappedOsApplication, final Map<String, EcucContainerValue> osAppEcuPartitionMap) {
    EcucContainerValue ecucContainerValue = null;
    EcucContainerValue ecuPartition = osAppEcuPartitionMap.get(mappedOsApplication);
    if ((ecuPartition != null) && (ecuPartition.getShortName() != null) && !ecuPartition.getShortName().isEmpty()) {
      for (EcucContainerValue ecucV : parentContainer.getSubContainers()) {

        if (ecucV.getShortName().equals(ecuPartition.getShortName())) {
          return ecucV;
        }
      }
    }
    return ecucContainerValue;

  }


  private EcucContainerValue createEcucPartition(final String mappedOsApplication,
      final EcucContainerValue parentContainer, final EcucParamConfContainerDef ecucParamConfContainerDef,
      final Map<String, EcucContainerValue> osAppEcuPartitionMap) {

    EcucContainerValue createEcucContainerValue = null;
    EcucContainerValue ecuPartition = osAppEcuPartitionMap.get(mappedOsApplication);
    if ((ecuPartition != null) && (ecuPartition.getShortName() != null) && !ecuPartition.getShortName().isEmpty()) {

      EcucContainerDef ecucContainerDef = getEcucContainerDef(ecucParamConfContainerDef, "EcucPartition");
      if ((ecucContainerDef != null) && (ecucContainerDef instanceof EcucParamConfContainerDef)) {
        createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
        createEcucContainerValue.setShortName(ecuPartition.getShortName());
        createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
        createEcucContainerValue.setDefinition(ecucContainerDef);
      }
    }
    return createEcucContainerValue;
  }


  private void createEcucPartitionSoftwareComponentInstanceRef(final EcucContainerValue createEcucContainerValue,
      final EcucParamConfContainerDef ecucParamConfContainerDef, final SwComponentPrototype componentInstance) {


    EcucInstanceReferenceValue createEcucInstanceReferenceValue =
        createEcucInstanceReferenceValue("EcucPartitionSoftwareComponentInstanceRef", ecucParamConfContainerDef);

    if (createEcucInstanceReferenceValue != null) {
      AnyInstanceRef createAnyInstanceRef = Autosar40Factory.eINSTANCE.createAnyInstanceRef();
      createAnyInstanceRef.setTarget(componentInstance);


      VariationPoint variationPoint = VariationPointUtil.getInstance().getVariationPoint(componentInstance);

      if (variationPoint != null) {
        createEcucInstanceReferenceValue.setVariationPoint(variationPoint);
      }

      createEcucInstanceReferenceValue.setValue(createAnyInstanceRef);
      createEcucContainerValue.getReferenceValues().add(createEcucInstanceReferenceValue);
    }

  }


  private boolean checkIfEcucPartitionSoftwareComponentInstanceRefExists(
      final EcucContainerValue createEcucContainerValue, final SwComponentPrototype componentInstance) {

    EList<EcucAbstractReferenceValue> referenceValues = createEcucContainerValue.getReferenceValues();

    for (EcucAbstractReferenceValue referenceValue : referenceValues) {
      AutosarUtil.setCurrentProcessingEObject(referenceValue);

      if (referenceValue instanceof EcucInstanceReferenceValue) {
        EcucInstanceReferenceValue value = (EcucInstanceReferenceValue) referenceValue;

        AnyInstanceRef value2 = value.getValue();

        if ((value2 != null) && (value2.getTarget() != null) && (value2.getTarget() instanceof SwComponentPrototype)) {

          if (componentInstance.getShortName().equals(((SwComponentPrototype) value2.getTarget()).getShortName())) {
            return true;
          }
        }
      }
    }

    return false;
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


  private EcucInstanceReferenceValue createEcucInstanceReferenceValue(final String shortName,
      final EcucParamConfContainerDef ecucParamConfContainerDef) {

    EcucInstanceReferenceValue createEcucInstanceReferenceValue = null;
    EcucAbstractReferenceDef ecucReferenceDef = getEcucReferenceDef(ecucParamConfContainerDef, shortName);

    if (ecucReferenceDef != null) {
      createEcucInstanceReferenceValue = Autosar40Factory.eINSTANCE.createEcucInstanceReferenceValue();
      createEcucInstanceReferenceValue.setDefinition(ecucReferenceDef);
    }
    return createEcucInstanceReferenceValue;
  }

}
