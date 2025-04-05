/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.rips;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.artop.aal.common.metamodel.AutosarReleaseDescriptor;
import org.artop.aal.workspace.preferences.IAutosarWorkspacePreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;

import com.bosch.tisched.rteconfig.generator.util.AutosarUtil;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;

import autosar40.autosartoplevelstructure.AUTOSAR;
import autosar40.autosartoplevelstructure.AutosartoplevelstructureFactory;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.ecucdescription.EcucTextualParamValue;
import autosar40.ecucparameterdef.EcucConfigurationVariantEnum;
import autosar40.ecucparameterdef.EcucContainerDef;
import autosar40.ecucparameterdef.EcucFunctionNameDef;
import autosar40.ecucparameterdef.EcucModuleDef;
import autosar40.ecucparameterdef.EcucParamConfContainerDef;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import autosar40.genericstructure.generaltemplateclasses.identifiable.IdentifiablePackage;
import autosar40.genericstructure.generaltemplateclasses.identifiable.Referrable;
import autosar40.swcomponent.components.AtomicSwComponentType;
import autosar40.swcomponent.swcinternalbehavior.rteevents.RTEEvent;
import autosar40.util.Autosar40Factory;

/**
 * @author SHK1COB
 */
public class GenerateRteRipsCSXfrmEcucValues {


  private static final Logger LOGGER =
      RteConfigGeneratorLogger.getLogger(GenerateRteRipsCSXfrmEcucValues.class.getName());

  private final IProject project;

  private EcucModuleConfigurationValues createEcucModuleConfigurationValues;


  private final Map<String, String> props;

  private final Map<String, EcucContainerValue> eventEcucContainerMap = new HashMap<>();


  private final String outputFilePath;

  private final String autosarReleaseVersion;

  private final String autosarResourceVersion;

  private AUTOSAR arPartRoot;

  private final String paramdefs[] =
      { "/RteRipsInvocationHandler", "/RteRipsInvocationHandler/RteRipsInvocationHandlerFnc" };

  private final Map<String, String> shortenedEventEcucConNameMap = new HashMap<>();


  /**
   * @param project
   * @param map
   * @param osConfigToEcucValueMapping
   */
  public GenerateRteRipsCSXfrmEcucValues(final IProject project, final Map<String, String> map) {
    this.project = project;
    this.props = map;
    this.outputFilePath = this.props.get(RteConfigGeneratorConstants.RTE_RIPS_CSXFRM_ECUC_VALUE_OUTPUT_PATH);
    this.autosarReleaseVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION);
    this.autosarResourceVersion = map.get(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION);
  }


  public void generateRteRipsCSXfrmEcucValues(final List<RTEEvent> rteEvents) throws Exception {
    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("134_0").trim());
    this.props.put(RteConfigGeneratorConstants.RTE_RIPS_CSXFRM_ECUC_VALUE_AR_PACKAGE_PATH,
        "RB/UBK/Project/EcucModuleConfigurationValuess/");

    AutosarReleaseDescriptor autosarRelease = IAutosarWorkspacePreferences.AUTOSAR_RELEASE.get(this.project);
    TransactionalEditingDomain editingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(this.project, autosarRelease);
    List<Referrable> allInstancesOf =
        EObjectUtil.getAllInstancesOf(editingDomain.getResourceSet().getResources(), Referrable.class, false);
    Map<String, Referrable> refList = new HashMap<>();
    for (Referrable ref : allInstancesOf) {

      String fragment = EcoreUtil.getURI(ref).fragment();
      fragment = fragment.substring(0, fragment.lastIndexOf("?type="));
      refList.put(fragment, ref);

    }

    Referrable referrable1 =
        refList.get("/AUTOSAR_Rte_Rips_CSXfrm/EcucModuleDefs/Rte_Rips_CSXfrm/RteRipsInvocationHandler");
    Referrable referrable2 = refList.get(
        "/AUTOSAR_Rte_Rips_CSXfrm/EcucModuleDefs/Rte_Rips_CSXfrm/RteRipsInvocationHandler/RteRipsInvocationHandlerFnc");

    if ((referrable1 != null) && (referrable1 instanceof EcucParamConfContainerDef) && (referrable2 != null) &&
        (referrable2 instanceof EcucParamConfContainerDef)) {


      // autosar instance
      this.arPartRoot = AutosartoplevelstructureFactory.eINSTANCE.createAUTOSAR();
      ARPackage subPackage =
          GenerateArxmlUtil.getARArPackage(this.arPartRoot, "/RB/UBK/Project/EcucModuleConfigurationValuess");


      Referrable referrable = refList.get("/AUTOSAR_Rte_Rips_CSXfrm/EcucModuleDefs/Rte_Rips_CSXfrm");
      if ((referrable != null) && (referrable instanceof EcucModuleDef)) {

        this.createEcucModuleConfigurationValues = Autosar40Factory.eINSTANCE.createEcucModuleConfigurationValues();
        this.createEcucModuleConfigurationValues.setShortName("Rte_Rips_CSXfrm");
        this.createEcucModuleConfigurationValues.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());

        EcucModuleDef ecuCModuleDef = (EcucModuleDef) referrable;
        this.createEcucModuleConfigurationValues.setDefinition(ecuCModuleDef);
        this.createEcucModuleConfigurationValues.setCategory("VENDOR_SPECIFIC_MODULE_DEFINITION");
        this.createEcucModuleConfigurationValues.setPostBuildVariantUsed(false);
        this.createEcucModuleConfigurationValues
            .setImplementationConfigVariant(EcucConfigurationVariantEnum.VARIANT_PRE_COMPILE);

        EcucContainerValue createEcucContainerValue = Autosar40Factory.eINSTANCE.createEcucContainerValue();
        createEcucContainerValue.setShortName("RteRipsInvocationHandler");
        createEcucContainerValue.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
        createEcucContainerValue.setDefinition((EcucParamConfContainerDef) referrable1);

        Map<String, EcucContainerValue> ecucContainerMap = new HashMap<>();
        for (RTEEvent rEvent : rteEvents) {

          AutosarUtil.setCurrentProcessingEObject(rEvent);

          String handlrefnc = "InvocationHandler_" +
              ((AtomicSwComponentType) rEvent.getSwcInternalBehavior().eContainer()).getShortName() + "_" +
              rEvent.getShortName();


          Referrable ref =
              refList.get("/RB/UBK/Project/EcucModuleConfigurationValuess/RteRipsInvocationHandler/" + handlrefnc);

          String startonAndEventName = rEvent.getShortName() + "_" + rEvent.getStartOnEvent().getShortName();
          startonAndEventName = GenerateArxmlUtil.getShortenedNameOfElements(startonAndEventName,
              Collections.unmodifiableMap(this.eventEcucContainerMap), this.shortenedEventEcucConNameMap);


          if ((ref != null) && (ref instanceof EcucContainerValue)) {
            EcucContainerValue ecucValue = (EcucContainerValue) ref;
            EcucContainerDef definition = ecucValue.getDefinition();
            String fragment3 = EcoreUtil.getURI(definition).fragment();
            fragment3 = fragment3.substring(0, fragment3.lastIndexOf("?type="));
            if (fragment3.equals(
                "/AUTOSAR_Rte_Rips_CSXfrm/EcucModuleDefs/Rte_Rips_CSXfrm/RteRipsInvocationHandler/RteRipsInvocationHandlerFnc")) {
              this.eventEcucContainerMap.put(startonAndEventName, ecucValue);
            }
            else {
              LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("241_1", ecucValue.getShortName()).trim());

            }

          }
          else {

            EcucContainerValue createEcucContainerValue1 = Autosar40Factory.eINSTANCE.createEcucContainerValue();

            handlrefnc =
                GenerateArxmlUtil.getShortenedNameOfElements(handlrefnc, Collections.unmodifiableMap(ecucContainerMap));
            createEcucContainerValue1.setShortName(handlrefnc);
            createEcucContainerValue1.eUnset(IdentifiablePackage.eINSTANCE.getIdentifiable_Uuid());
            createEcucContainerValue1.setDefinition((EcucParamConfContainerDef) referrable2);
            ecucContainerMap.put(handlrefnc, createEcucContainerValue1);

            EcucTextualParamValue createEcucTextualParamValue =
                Autosar40Factory.eINSTANCE.createEcucTextualParamValue();
            referrable = refList.get(
                "/AUTOSAR_Rte_Rips_CSXfrm/EcucModuleDefs/Rte_Rips_CSXfrm/RteRipsInvocationHandler/RteRipsInvocationHandlerFnc/RteRipsInvocationHandlerFncSymbol");


            if ((referrable != null) && (referrable instanceof EcucFunctionNameDef)) {
              createEcucTextualParamValue.setDefinition((EcucFunctionNameDef) referrable);
              createEcucTextualParamValue.setValue("Rte_Rips_CSXfrm_" + startonAndEventName + "_InvocationHandler");
              createEcucContainerValue1.getParameterValues().add(createEcucTextualParamValue);
            }

            createEcucContainerValue.getSubContainers().add(createEcucContainerValue1);
            this.eventEcucContainerMap.put(startonAndEventName, createEcucContainerValue1);
          }
        }

        this.createEcucModuleConfigurationValues.getContainers().add(createEcucContainerValue);
        subPackage.getElements().add(this.createEcucModuleConfigurationValues);
      }
    }
    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("135_0").trim());
  }

  public Map<String, EcucContainerValue> getEventEcucContainerMap() {
    return Collections.unmodifiableMap(this.eventEcucContainerMap);
  }


  /**
   * @throws Exception
   */
  public void generateRteRipsCSXfrmEcucValuesFile() throws Exception {
    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("136_0").trim());

    String conPath = this.outputFilePath.substring(0, this.outputFilePath.lastIndexOf("/"));
    String fileName =
        this.outputFilePath.substring(this.outputFilePath.lastIndexOf("/") + 1, this.outputFilePath.length());


    if ((this.createEcucModuleConfigurationValues != null) &&
        !this.createEcucModuleConfigurationValues.getContainers().isEmpty()) {

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
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("137_0",
            URI.createFileURI(this.project.getFullPath().toOSString() + conPath).path(), fileName).trim());


      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("309_1").trim());

      }
    }
    else {
      LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("242_0").trim());

    }
  }


  /**
   * @return
   */
  public Map<String, String> getShortenedEventEcucConNameMap() {

    return Collections.unmodifiableMap(this.shortenedEventEcucConNameMap);
  }


}

