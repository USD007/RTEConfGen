/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.artop.aal.common.util.IdentifiableUtil;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;

import autosar40.autosartoplevelstructure.AUTOSAR;
import autosar40.commonstructure.triggerdeclaration.Trigger;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucModuleConfigurationValues;
import autosar40.ecucdescription.EcucNumericalParamValue;
import autosar40.ecucdescription.EcucParameterValue;
import autosar40.ecucdescription.EcucReferenceValue;
import autosar40.ecucdescription.EcucTextualParamValue;
import autosar40.ecucparameterdef.EcucAbstractReferenceDef;
import autosar40.ecucparameterdef.EcucBooleanParamDef;
import autosar40.ecucparameterdef.EcucContainerDef;
import autosar40.ecucparameterdef.EcucEnumerationParamDef;
import autosar40.ecucparameterdef.EcucFloatParamDef;
import autosar40.ecucparameterdef.EcucIntegerParamDef;
import autosar40.ecucparameterdef.EcucModuleDef;
import autosar40.ecucparameterdef.EcucParamConfContainerDef;
import autosar40.ecucparameterdef.EcucParameterDef;
import autosar40.ecucparameterdef.EcucReferenceDef;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import autosar40.genericstructure.generaltemplateclasses.arpackage.ArpackageFactory;
import autosar40.genericstructure.varianthandling.attributevaluevariationpoints.AttributevaluevariationpointsFactory;
import autosar40.genericstructure.varianthandling.attributevaluevariationpoints.BooleanValueVariationPoint;
import autosar40.genericstructure.varianthandling.attributevaluevariationpoints.FloatValueVariationPoint;
import autosar40.genericstructure.varianthandling.attributevaluevariationpoints.NumericalValueVariationPoint;
import autosar40.genericstructure.varianthandling.attributevaluevariationpoints.PositiveIntegerValueVariationPoint;
import autosar40.genericstructure.varianthandling.attributevaluevariationpoints.UnlimitedIntegerValueVariationPoint;
import autosar40.system.RootSwCompositionPrototype;
import autosar40.system.System;
import autosar40.system.SystemMapping;
import autosar40.system.fibex.fibexcore.coretopology.EcuInstance;
import autosar40.system.swmapping.SwcToEcuMapping;
import autosar40.util.Autosar40Factory;
import autosar40.util.Autosar40ReleaseDescriptor;
import gautosar.ggenericstructure.ginfrastructure.GReferrable;

/**
 * @author shk1cob
 */
public class GenerateArxmlUtil {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(GenerateArxmlUtil.class.getName());


  /**
   * @param limit String
   * @return PositiveIntegerValueVariationPoint
   */
  public static PositiveIntegerValueVariationPoint getPositiveIntegerValueVariationPoint(final String limit) {

    PositiveIntegerValueVariationPoint createPositiveIntegerValueVariationPoint =
        AttributevaluevariationpointsFactory.eINSTANCE.createPositiveIntegerValueVariationPoint();
    createPositiveIntegerValueVariationPoint.setMixedText(limit);
    return createPositiveIntegerValueVariationPoint;

  }

  /**
   * @param value String
   * @return NumericalValueVariationPoint
   */
  public static NumericalValueVariationPoint createNumericalValueVariationPoint(final String value) {

    NumericalValueVariationPoint createNumericalValueVariationPoint =
        AttributevaluevariationpointsFactory.eINSTANCE.createNumericalValueVariationPoint();
    createNumericalValueVariationPoint.setMixedText(value);
    return createNumericalValueVariationPoint;

  }


  /**
   * @param project IProject
   * @param arPartRoot AUTOSAR
   * @param resourceURI URI
   * @param metaModelDescriptor IMetaModelDescriptor
   * @return IStatus
   */
  public static IStatus saveFile(final IProject project, final AUTOSAR arPartRoot, final URI resourceURI,
      final IMetaModelDescriptor metaModelDescriptor) {

    final TransactionalEditingDomain currentEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(project, Autosar40ReleaseDescriptor.INSTANCE);
    if (currentEditingDomain != null) {

      final ResourceSet resourceSet = currentEditingDomain.getResourceSet();
      Runnable runnable = new Runnable() {

        @Override
        public void run() {

          EcoreResourceUtil.saveNewModelResource(resourceSet, resourceURI,
              Autosar40ReleaseDescriptor.ARXML_CONTENT_TYPE_ID, arPartRoot, Collections.emptyMap());
          EcoreUtil.resolveAll(arPartRoot);

        }
      };

      try {
        WorkspaceTransactionUtil.executeInWriteTransaction(currentEditingDomain, runnable, "Saving ARXML file");

      }
      catch (OperationCanceledException e) {
        RteConfigGeneratorLogger.logErrormessage(LOGGER, e.getLocalizedMessage(), e);
        return Status.CANCEL_STATUS;
      }
      catch (ExecutionException e) {
        RteConfigGeneratorLogger.logErrormessage(LOGGER, e.getLocalizedMessage(), e);
        return Status.CANCEL_STATUS;
      }


    }
    return Status.OK_STATUS;
  }


  /**
   * @param project IProject
   * @param arPartRoot AUTOSAR
   * @param resourceURI URI
   * @param metaModelDescriptor IMetaModelDescriptor
   * @return IStatus
   */
  public static IStatus saveOnly(final IProject project, final Resource resource) {

    final TransactionalEditingDomain currentEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(project, Autosar40ReleaseDescriptor.INSTANCE);
    if (currentEditingDomain != null) {

      Runnable runnable = new Runnable() {

        @Override
        public void run() {

          EcoreResourceUtil.saveModelResource(resource, Collections.emptyMap());
        }
      };

      try {
        WorkspaceTransactionUtil.executeInWriteTransaction(currentEditingDomain, runnable, "Saving ARXML file");

      }
      catch (OperationCanceledException e) {
        RteConfigGeneratorLogger.logErrormessage(LOGGER, e.getLocalizedMessage(), e);
        return Status.CANCEL_STATUS;
      }
      catch (ExecutionException e) {
        RteConfigGeneratorLogger.logErrormessage(LOGGER, e.getLocalizedMessage(), e);
        return Status.CANCEL_STATUS;
      }


    }
    return Status.OK_STATUS;
  }


  // /**
  // * @param iProject IProject
  // * @param pkgPath String
  // * @param excludeDir String
  // * @return ARPackage
  // */
  // public static ARPackage getArPackage(final IProject iProject, final String pkgPath, final String excludeDir) {
  //
  // ARPackage arPackage = null;
  // final TransactionalEditingDomain currentEditingDomain =
  // WorkspaceEditingDomainUtil.getEditingDomain(iProject, Autosar40ReleaseDescriptor.INSTANCE);
  // ResourceSet resourceSet = currentEditingDomain.getResourceSet();
  //
  // List<Resource> resourcesOnly = new ArrayList<Resource>();
  //
  // for (Resource resource : resourceSet.getResources()) {
  // String devicePath = resource.getURI().devicePath();
  // devicePath = devicePath.substring(0, devicePath.lastIndexOf("/"));
  // excludeResources(devicePath, excludeDir, resourcesOnly, resource);
  // }
  //
  // List<ARPackage> allInstancesOf = EObjectUtil.getAllInstancesOf(resourcesOnly, ARPackage.class, true);
  //
  // for (ARPackage instance : allInstancesOf) {
  //
  // if (getPackagePath(instance).equals(pkgPath)) {
  // return instance;
  // }
  //
  // }
  //
  // return arPackage;
  //
  // }


  /**
   * @param resources
   * @param type
   * @param exactMatch
   * @return
   */
  public static <T> List<T> getAllInstancesOf(final List<Resource> resources, final Class<T> type,
      final boolean exactMatch) {

    List<T> allInstancesOf = EObjectUtil.getAllInstancesOf(resources, type, exactMatch);

    Map<String, List<T>> pathTypeMap =
        allInstancesOf.stream().collect(Collectors.groupingBy(t -> EcoreUtil.getURI((EObject) t).fragment()));

    return pathTypeMap.keySet().stream().map(p -> pathTypeMap.get(p).get(0)).collect(Collectors.toList());


  }

  /**
   * @param eObject EObject
   * @return String
   */
  public static String getPackagePath(final EObject eObject) {
    URI uri = EcoreResourceUtil.getURI(eObject);

    if ((uri != null) && (uri.fragment() != null)) {
      return uri.fragment().split("\\?")[0];
    }
    return "";
  }


  /**
   * @param iProject IProject
   * @param pkgPath String
   * @param type Class<T>
   * @param excludeDir String
   * @return <T>
   */
  public static <T> T getEObject(final IProject iProject, final String pkgPath, final Class<T> type,
      final String excludeDir) {

    T t = null;
    final TransactionalEditingDomain currentEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(iProject, Autosar40ReleaseDescriptor.INSTANCE);
    ResourceSet resourceSet = currentEditingDomain.getResourceSet();

    List<Resource> resourcesOnly = new ArrayList<Resource>();

    for (Resource resource : resourceSet.getResources()) {
      String devicePath = resource.getURI().devicePath();
      devicePath = devicePath.substring(0, devicePath.lastIndexOf("/"));

      excludeResources(devicePath, excludeDir, resourcesOnly, resource);

    }
    List<T> allInstancesOf = EObjectUtil.getAllInstancesOf(resourcesOnly, type, true);

    for (T instance : allInstancesOf) {

      URI uri = EcoreResourceUtil.getURI((EObject) instance);
      if ((uri != null) && uri.fragment().split("\\?")[0].equals(pkgPath)) {
        return instance;
      }

    }

    return t;

  }

  /**
   * @param iProject IProject
   * @param pkgPath String
   * @param type Class<T>
   * @return <T>
   */
  public static <T> List<T> getEObject(final IProject iProject, final String pkgPath, final Class<T> type) {

    List<T> t = new ArrayList<>();
    final TransactionalEditingDomain currentEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(iProject, Autosar40ReleaseDescriptor.INSTANCE);
    ResourceSet resourceSet = currentEditingDomain.getResourceSet();


    List<T> allInstancesOf = EObjectUtil.getAllInstancesOf(resourceSet.getResources(), type, true);

    for (T instance : allInstancesOf) {

      URI uri = EcoreResourceUtil.getURI((EObject) instance);
      if ((uri != null) &&
          uri.fragment().split("\\?")[0].equals(pkgPath) /*
                                                          * && resources.contains(EcorePlatformUtil.getFile(((EObject)
                                                          * instance).eResource()).getFullPath().toOSString())
                                                          */) {
        t.add(instance);
      }

    }

    return t;

  }

  private static void excludeResources(final String devicePath, final String excludeDir,
      final List<Resource> resourcesOnly, final Resource resource) {
    if (!excludeDir.isEmpty()) {
      for (String dir : excludeDir.split(",")) {
        if (dir.isEmpty()) {
          continue;
        }
        if (devicePath.endsWith(dir)) {
          return;
        }
      }
      resourcesOnly.add(resource);
    }
    else {
      resourcesOnly.add(resource);
    }
  }

  /**
   * @param iProject IProject
   * @param pkgPath String
   * @param type Class<T>
   * @param excludeDir String
   * @return <T>
   */
  public static <T> List<T> getListOfEObject(final IProject iProject, final String pkgPath, final Class<T> type,
      final String excludeDir) {

    List<T> list = new ArrayList<>();
    final TransactionalEditingDomain currentEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(iProject, Autosar40ReleaseDescriptor.INSTANCE);
    ResourceSet resourceSet = currentEditingDomain.getResourceSet();

    List<Resource> resourcesOnly = new ArrayList<Resource>();

    for (Resource resource : resourceSet.getResources()) {
      String devicePath = resource.getURI().devicePath();
      devicePath = devicePath.substring(0, devicePath.lastIndexOf("/"));

      excludeResources(devicePath, excludeDir, resourcesOnly, resource);

    }
    List<T> allInstancesOf = GenerateArxmlUtil.getAllInstancesOf(resourcesOnly, type, false);


    for (T instance : allInstancesOf) {

      URI uri = EcoreResourceUtil.getURI((EObject) instance);
      if ((uri != null) && uri.fragment().split("\\?")[0].equals(pkgPath)) {
        list.add(instance);
      }

    }
    return list;

  }

  /**
   * @param ecuInstance EcuInstance
   * @param iProject IProject
   * @param excludeDir String
   * @return RootSwCompositionPrototype
   */
  public static RootSwCompositionPrototype getRootSwCompositionPrototype(final EcuInstance ecuInstance,
      final IProject iProject, final String excludeDir)
      throws Exception {
    RootSwCompositionPrototype rootSwCompositionPrototype1 = null;
    List<System> systems = GenerateArxmlUtil.getListOfEObject(iProject, System.class, excludeDir);


    if (systems != null) {

      Map<String, System> systemMap = new HashMap<String, System>();


      for (System system : systems) {
        systemMap.put(system.getShortName(), system);
      }


      stopp: for (System system : systemMap.values()) {


        if (system.getCategory().equals("ECU_EXTRACT")) {

          EList<SystemMapping> mappings = system.getMappings();
          for (SystemMapping systemMapping : mappings) {
            EList<SwcToEcuMapping> swMappings = systemMapping.getSwMappings();
            for (SwcToEcuMapping swcEcuMapping : swMappings) {
              EcuInstance ecuInstance2 = swcEcuMapping.getEcuInstance();
              if ((ecuInstance2 != null) && getFragmentURI(ecuInstance2).equals(getFragmentURI(ecuInstance))) {
                LOGGER.info(RteConfGenMessageDescription
                    .getFormattedMesssage("116_0", GenerateArxmlUtil.getPackagePath(system), ecuInstance.getShortName())
                    .trim());


                if ((system.getRootSoftwareCompositions() != null)) {
                  if (system.getRootSoftwareCompositions().size() == 1) {

                    rootSwCompositionPrototype1 = system.getRootSoftwareCompositions().get(0);
                    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("117_0",
                        GenerateArxmlUtil.getPackagePath(system.getRootSoftwareCompositions().get(0)).trim()));


                  }
                  else if (system.getRootSoftwareCompositions().size() > 1) {
                    rootSwCompositionPrototype1 = system.getRootSoftwareCompositions().get(0);

                    LOGGER
                        .warn(
                            RteConfGenMessageDescription
                                .getFormattedMesssage("233_0", GenerateArxmlUtil.getPackagePath(system),
                                    Integer.toString(system.getRootSoftwareCompositions().size()),
                                    GenerateArxmlUtil.getPackagePath(system.getRootSoftwareCompositions().get(0)))
                                .trim());

                  }
                  else {
                    LOGGER.warn(RteConfGenMessageDescription
                        .getFormattedMesssage("234_0", GenerateArxmlUtil.getPackagePath(system)).trim());

                  }
                }
                else {
                  LOGGER.warn(RteConfGenMessageDescription
                      .getFormattedMesssage("235_0", GenerateArxmlUtil.getPackagePath(system)).trim());

                }
                break stopp;
              }
            }
          }
        }
      }
    }
    return rootSwCompositionPrototype1;
  }

  /**
   * @param System sysinstance
   * @param iProject IProject
   * @param excludeDir String
   * @return RootSwCompositionPrototype
   */
  public static RootSwCompositionPrototype getRootSwCompositionPrototype2(final EcuInstance ecuInstance,
      final IProject iProject, final String excludeDir)
      throws Exception {
    RootSwCompositionPrototype rootSwCompositionPrototype2 = null;
    List<System> systems = GenerateArxmlUtil.getListOfEObject(iProject, System.class, excludeDir);


    if (systems != null) {

      Map<String, System> systemMap = new HashMap<String, System>();


      for (System system : systems) {
        if ((system.getCategory() != null) && system.getCategory().equals("SYSTEM_EXTRACT")) {
          systemMap.put(system.getShortName(), system);
        }
      }
      stopp: for (System system : systemMap.values()) {


        if (system.getCategory().equals("SYSTEM_EXTRACT")) {

          EList<SystemMapping> mappings = system.getMappings();
          for (SystemMapping systemMapping : mappings) {
            EList<SwcToEcuMapping> swMappings = systemMapping.getSwMappings();
            for (SwcToEcuMapping swcEcuMapping : swMappings) {
              EcuInstance ecuInstance2 = swcEcuMapping.getEcuInstance();
              if ((ecuInstance2 != null) && getFragmentURI(ecuInstance2).equals(getFragmentURI(ecuInstance))) {
                LOGGER.info(RteConfGenMessageDescription
                    .getFormattedMesssage("116_0", GenerateArxmlUtil.getPackagePath(system), ecuInstance.getShortName())
                    .trim());


                if ((system.getRootSoftwareCompositions() != null)) {
                  if (system.getRootSoftwareCompositions().size() == 1) {

                    rootSwCompositionPrototype2 = system.getRootSoftwareCompositions().get(0);
                    LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("117_0",
                        GenerateArxmlUtil.getPackagePath(system.getRootSoftwareCompositions().get(0)).trim()));


                  }
                  else if (system.getRootSoftwareCompositions().size() > 1) {
                    rootSwCompositionPrototype2 = system.getRootSoftwareCompositions().get(0);

                    LOGGER
                        .warn(
                            RteConfGenMessageDescription
                                .getFormattedMesssage("233_0", GenerateArxmlUtil.getPackagePath(system),
                                    Integer.toString(system.getRootSoftwareCompositions().size()),
                                    GenerateArxmlUtil.getPackagePath(system.getRootSoftwareCompositions().get(0)))
                                .trim());

                  }
                  else {
                    LOGGER.warn(RteConfGenMessageDescription
                        .getFormattedMesssage("234_0", GenerateArxmlUtil.getPackagePath(system)).trim());

                  }
                }
                else {
                  LOGGER.warn(RteConfGenMessageDescription
                      .getFormattedMesssage("235_0", GenerateArxmlUtil.getPackagePath(system)).trim());

                }
                break stopp;
              }
            }
          }
        }
      }
    }
    return rootSwCompositionPrototype2;
  }

  /**
   * @param ecuContainerValue EcucContainerValue
   * @param defintionRef EcucReferenceDef
   * @return EcucContainerValue
   */
  public static EcucContainerValue getEcucContainerValue(final EcucContainerValue ecuContainerValue,
      final EcucReferenceDef defintionRef) {

    EcucContainerValue ecuConValue = null;

    EList<EcucAbstractReferenceValue> referenceValues = ecuContainerValue.getReferenceValues();

    for (EcucAbstractReferenceValue ecuRefValue : referenceValues) {
      if ((ecuRefValue.getDefinition() != null) && ecuRefValue.getDefinition().equals(defintionRef)) {

        return (EcucContainerValue) ((EcucReferenceValue) ecuRefValue).getValue();
      }
    }

    return ecuConValue;

  }

  public static boolean isSameElement(final EObject eObject1, final EObject eObject2) {

    if ((eObject1 != null) && (eObject2 != null) && !eObject1.eIsProxy() && !eObject2.eIsProxy()) {

      String fragement1 = EcoreUtil.getURI(eObject1).fragment();

      String fragement2 = EcoreUtil.getURI(eObject2).fragment();

      return fragement1.equals(fragement2);
    }

    return false;
  }

  public static boolean listContainsElements(final Collection<? extends EObject> listOfObjects, final EObject eObject) {

    if ((listOfObjects != null) && (eObject != null) && !listOfObjects.isEmpty() && !eObject.eIsProxy()) {

      Optional<? extends EObject> result =
          listOfObjects.stream().filter(obj -> isSameElement(obj, eObject)).findFirst();

      return result.isPresent();

    }

    return false;
  }

  /**
   * @param iProject IProject
   * @param type Class<T>
   * @param excludeDir String
   * @return <T>
   */
  public static <T> List<T> getListOfEObject(final IProject iProject, final Class<T> type, final String excludeDir) {


    final TransactionalEditingDomain currentEditingDomain =
        WorkspaceEditingDomainUtil.getEditingDomain(iProject, Autosar40ReleaseDescriptor.INSTANCE);
    ResourceSet resourceSet = currentEditingDomain.getResourceSet();


    List<Resource> resourcesOnly = new ArrayList<Resource>();

    for (Resource resource : resourceSet.getResources()) {
      String devicePath = resource.getURI().devicePath();
      devicePath = devicePath.substring(0, devicePath.lastIndexOf("/"));

      excludeResources(devicePath, excludeDir, resourcesOnly, resource);

    }
    return EObjectUtil.getAllInstancesOf(resourcesOnly, type, false);


  }


  // /**
  // * @author nir4kor
  // * @param project to fetch all resources
  // * @param includereference true if reference projects needs to be considered
  // * @return returns list all the autosar resources within editing domain
  // */
  // public static List<Resource> getAllResourcesFromProject(final IProject project, final boolean includereference,
  // final boolean isSync) {
  // List<Resource> packagelist = new ArrayList<Resource>();
  // AutosarReleaseDescriptor autosarRelease = IAutosarWorkspacePreferences.AUTOSAR_RELEASE.get(project);
  // TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(project, autosarRelease);
  // // if (!AATPlatformUtil.isProjectLoaded(project)) {
  // // AATPlatformUtil.synchronizedLoadProject(project, true, isSync);
  // // }
  // if (editingDomain != null) {
  // // AAT-966
  // ResourceSet resourceSet = editingDomain.getResourceSet();
  // List<Object> resources = new ArrayList<Object>();
  // resources.addAll(resourceSet.getResources());
  // for (Object element : resources) {
  // Resource eObject = (Resource) element;
  // EList<EObject> contents = eObject.getContents();
  //
  // if (CollectionUtils.isNotEmpty(contents)) {
  // EObject eObject2 = contents.get(0);
  // if ((eObject2 != null)) {
  // try {
  // IFile file = EcorePlatformUtil.getFile(eObject2);
  // if (eObject2.eResource() instanceof AutosarXMLResourceImpl) {
  // if ((file != null) && /* isArXmlFile(file) && */project.equals(file.getProject())) {
  // packagelist.add(eObject);
  // }
  //
  // }
  // }
  // catch (Exception e) {
  // // ignore after cattching the exception from Bworx
  // // integration
  // // AatLogUtil.logAsError(Activator.getDefault(), e,
  // // "Class: AATPlatformUtil Method:getAllResourcesFromProject(), Message: Error while fetching packages
  // // from project");
  // }
  // }
  // }
  // }
  // if (includereference) {
  // IProject[] referencedProjects = ExtendedPlatform.getReferencedProjectsSafely(project);
  // for (IProject referencedProject : referencedProjects) {
  // if (!project.equals(referencedProject)) {
  // packagelist.addAll(getAllResourcesFromProject(referencedProject, false, true));
  // }
  // }
  // }
  //
  // }
  // return packagelist;
  // }


  /**
   * @param releaseVersion String
   * @return IMetaModelDescriptor
   */
  public static IMetaModelDescriptor getMetaModelDescriptorByAutosarReleaseVersion(final String releaseVersion) {
    return MetaModelDescriptorRegistry.INSTANCE.getDescriptor(java.net.URI.create(releaseVersion));
  }

  /**
   * @param descriptor IMetaModelDescriptor
   * @param resourceVersion String
   * @return IMetaModelDescriptor
   */
  public static IMetaModelDescriptor getMetaModelDescriptorByAutosarResoureVersion(
      final IMetaModelDescriptor descriptor, final String resourceVersion) {

    Map<String, IMetaModelDescriptor> map = new HashMap<String, IMetaModelDescriptor>();
    for (IMetaModelDescriptor mmd : descriptor.getCompatibleResourceVersionDescriptors()) {
      map.put(mmd.getName(), mmd);
    }
    return map.get(resourceVersion);
  }

  /**
   * @param arPartRoot AUTOSAR
   * @param arPkgpath String
   * @return ARPackage
   */
  public static ARPackage getARArPackage(final AUTOSAR arPartRoot, final String arPkgpath) {

    ARPackage arPkg = null;

    String arpkgPath[] = arPkgpath.split("/");

    for (String path : arpkgPath) {
      if (!path.isEmpty()) {
        ARPackage arPackage = ArpackageFactory.eINSTANCE.createARPackage();
        arPackage.setShortName(path);

        if (arPkg == null) {
          arPartRoot.getArPackages().add(arPackage);
        }
        else {
          arPkg.getArPackages().add(arPackage);
        }
        arPkg = arPackage;
      }
    }

    return arPkg;

  }

  /**
   * @param arPartRoot AUTOSAR
   * @param arPkgpath String
   * @return ARPackage
   */
  public static ARPackage getExistingARArPackage(final AUTOSAR arPartRoot, final String arPkgpath) {

    ARPackage arPkg = null;

    String arpkgPath[] = arPkgpath.split("/");

    for (String path : arpkgPath) {
      if (!path.isEmpty()) {


        ARPackage arPackage =
            getArPackageFromName(arPkg == null ? arPartRoot.getArPackages() : arPkg.getArPackages(), path);

        if (arPackage == null) {
          arPackage = ArpackageFactory.eINSTANCE.createARPackage();
          arPackage.setShortName(path);
        }


        if (arPkg == null) {
          arPartRoot.getArPackages().add(arPackage);
        }
        else {
          arPkg.getArPackages().add(arPackage);
        }
        arPkg = arPackage;
      }
    }

    return arPkg;

  }


  // public static void mergeSplittableComponent(final SwComponentType swct) {
  //
  //
  // TransactionalEditingDomain arEditingDomain = WorkspaceEditingDomainUtil.getEditingDomain(swct.eResource());
  //
  // // Resource eResource = compositionSwComponentType.eResource();
  // AbstractEMFOperation abstractEmfOperation = new AbstractEMFOperation(arEditingDomain, "Merging SwComponentType") {
  //
  // @Override
  // protected IStatus doExecute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
  //
  //
  // List<EObject> eObjs = getAllSplittedObjects(swct, ComponentsPackage.eINSTANCE.getSwComponentType_Ports());
  //
  //
  // swct.getPorts().clear();
  //
  // for (EObject eObj : eObjs) {
  //
  // if ((eObj instanceof PortPrototype) && !eObj.eIsProxy()) {
  //
  // PortPrototype p = (PortPrototype) eObj;
  // if (!swct.getPorts().contains(p)) {
  // // p.setSwComponentType(swct);
  // swct.getPorts().add(EcoreUtil.copy(p));
  // }
  //
  // }
  //
  // }
  //
  //
  // eObjs = getAllSplittedObjects(swct, ComponentsPackage.eINSTANCE.getAtomicSwComponentType_InternalBehaviors());
  //
  // ((AtomicSwComponentType) swct).getInternalBehaviors().clear();
  //
  // for (EObject eObj : eObjs) {
  //
  // if ((eObj instanceof SwcInternalBehavior) && !eObj.eIsProxy()) {
  //
  // SwcInternalBehavior i = (SwcInternalBehavior) eObj;
  // if (!((AtomicSwComponentType) swct).getInternalBehaviors().contains(i)) {
  //
  // ((AtomicSwComponentType) swct).getInternalBehaviors().add(EcoreUtil.copy(i));
  // }
  //
  // }
  //
  // }
  //
  //
  // return Status.OK_STATUS;
  // }
  // };
  //
  // try {
  // WorkspaceTransactionUtil.getOperationHistory(arEditingDomain).execute(abstractEmfOperation,
  // new NullProgressMonitor(), null);
  // }
  // catch (ExecutionException e) {
  // RteConfigGeneratorLogger.logErrormessage(LOGGER,"MM_DCS_RTECONFGEN_399 : ***Exception : " +
  // e.getLocalizedMessage());
  // }
  //
  //
  // }


  // private static void mergeSplittableSystem(final System system) {
  //
  //
  // TransactionalEditingDomain arEditingDomain = WorkspaceEditingDomainUtil.getEditingDomain(system.eResource());
  //
  // // Resource eResource = compositionSwComponentType.eResource();
  // AbstractEMFOperation abstractEmfOperation = new AbstractEMFOperation(arEditingDomain, "Merging System") {
  //
  // @Override
  // protected IStatus doExecute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
  //
  //
  // List<EObject> eObjs = getAllSplittedObjects(system, SystemPackage.eINSTANCE.getSystem_FibexElements());
  //
  //
  // for (EObject eObj : eObjs) {
  //
  // if ((eObj instanceof FibexElementRefConditional) && !eObj.eIsProxy()) {
  //
  // FibexElementRefConditional ferc = (FibexElementRefConditional) eObj;
  // if (!system.getFibexElements().contains(ferc)) {
  // ferc.setSystem(system);
  // system.getFibexElements().add(EcoreUtil.copy(ferc));
  // }
  //
  // }
  //
  // }
  //
  //
  // eObjs = getAllSplittedObjects(system, SystemPackage.eINSTANCE.getSystem_Mappings());
  //
  //
  // for (EObject eObj : eObjs) {
  //
  // if ((eObj instanceof SystemMapping) && !eObj.eIsProxy()) {
  //
  // SystemMapping sm = (SystemMapping) eObj;
  // if (!system.getMappings().contains(sm)) {
  // sm.setSystem(system);
  // system.getMappings().add(EcoreUtil.copy(sm));
  // }
  //
  // }
  //
  // }
  //
  // eObjs = getAllSplittedObjects(system, SystemPackage.eINSTANCE.getSystem_RootSoftwareCompositions());
  //
  //
  // for (EObject eObj : eObjs) {
  //
  // if ((eObj instanceof RootSwCompositionPrototype) && !eObj.eIsProxy()) {
  //
  // RootSwCompositionPrototype rsc = (RootSwCompositionPrototype) eObj;
  // if (!system.getRootSoftwareCompositions().contains(rsc)) {
  // rsc.setSystem(system);
  // system.getRootSoftwareCompositions().add(EcoreUtil.copy(rsc));
  // }
  //
  // }
  //
  // }
  //
  //
  // return Status.OK_STATUS;
  // }
  // };
  //
  // try {
  // WorkspaceTransactionUtil.getOperationHistory(arEditingDomain).execute(abstractEmfOperation,
  // new NullProgressMonitor(), null);
  // }
  // catch (ExecutionException e) {
  // RteConfigGeneratorLogger.logErrormessage(LOGGER,"MM_DCS_RTECONFGEN_399 : ***Exception : " +
  // e.getLocalizedMessage());
  // }
  //
  //
  // }


  // /**
  // * @param referrables referrables
  // * @return GAUTOSAR
  // */
  // public static void mergeSplittableFlatmap(final FlatMap flatMap) {
  //
  //
  // TransactionalEditingDomain arEditingDomain = WorkspaceEditingDomainUtil.getEditingDomain(flatMap.eResource());
  //
  // // Resource eResource = compositionSwComponentType.eResource();
  // AbstractEMFOperation abstractEmfOperation = new AbstractEMFOperation(arEditingDomain, "Merging flatmap") {
  //
  // @Override
  // protected IStatus doExecute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
  //
  //
  // List<EObject> eObjs = getAllSplittedObjects(flatMap, FlatmapPackage.eINSTANCE.getFlatMap_Instances());
  //
  //
  // for (EObject eObj : eObjs) {
  //
  // if ((eObj instanceof FlatInstanceDescriptor) && !eObj.eIsProxy()) {
  //
  // FlatInstanceDescriptor fid = (FlatInstanceDescriptor) eObj;
  // if (!flatMap.getInstances().contains(fid)) {
  // fid.setFlatMap(flatMap);
  // flatMap.getInstances().add(EcoreUtil.copy(fid));
  // }
  //
  // }
  //
  // }
  //
  // return Status.OK_STATUS;
  // }
  // };
  //
  // try {
  // WorkspaceTransactionUtil.getOperationHistory(arEditingDomain).execute(abstractEmfOperation,
  // new NullProgressMonitor(), null);
  // }
  // catch (ExecutionException e) {
  // RteConfigGeneratorLogger.logErrormessage(LOGGER,"MM_DCS_RTECONFGEN_399 : ***Exception : " +
  // e.getLocalizedMessage());
  // }
  //
  //
  // }


  // /**
  // * @param referrables referrables
  // * @return GAUTOSAR
  // */
  // public static void updateRootSwCompositionPrototype(final RootSwCompositionPrototype roorCompositionPrototype) {
  //
  //
  // CompositionSwComponentType compositionSwComponentType = roorCompositionPrototype.getSoftwareComposition();
  //
  //
  // TransactionalEditingDomain arEditingDomain =
  // WorkspaceEditingDomainUtil.getEditingDomain(roorCompositionPrototype.eResource());
  //
  //
  // AbstractEMFOperation abstractEmfOperation =
  // new AbstractEMFOperation(arEditingDomain, "Merging CompositionSwComponentType") {
  //
  // @Override
  // protected IStatus doExecute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
  //
  //
  // List<EObject> comps = getAllSplittedObjects(compositionSwComponentType,
  // CompositionPackage.eINSTANCE.getCompositionSwComponentType_Components());
  //
  //
  // for (EObject swcComponent : comps) {
  //
  // if ((swcComponent instanceof SwComponentPrototype) && !swcComponent.eIsProxy()) {
  //
  // SwComponentPrototype swc = (SwComponentPrototype) swcComponent;
  // if (!compositionSwComponentType.getComponents().contains(swc)) {
  // compositionSwComponentType.getComponents().add(EcoreUtil.copy(swc));
  // }
  //
  // }
  //
  // }
  //
  //
  // List<EObject> connectors = getAllSplittedObjects(compositionSwComponentType,
  // CompositionPackage.eINSTANCE.getCompositionSwComponentType_Connectors());
  //
  //
  // for (EObject connector : connectors) {
  //
  // if ((connector instanceof SwConnector) && !connector.eIsProxy()) {
  //
  // SwConnector swc = (SwConnector) connector;
  // if (!compositionSwComponentType.getConnectors().contains(swc)) {
  // compositionSwComponentType.getConnectors().add(EcoreUtil.copy(swc));
  // }
  // }
  //
  // }
  //
  //
  // return Status.OK_STATUS;
  // }
  // };
  //
  // try {
  // WorkspaceTransactionUtil.getOperationHistory(arEditingDomain).execute(abstractEmfOperation,
  // new NullProgressMonitor(), null);
  // }
  // catch (ExecutionException e) {
  // RteConfigGeneratorLogger.logErrormessage(LOGGER,"MM_DCS_RTECONFGEN_399 : ***Exception : " +
  // e.getLocalizedMessage());
  // }
  //
  //
  // }


  // public static List<EObject> getAllSplittedObjects(final EObject arObject, final EStructuralFeature feature) {
  //
  // Iterable<EObject> equivalentARElements = getElementsFromSplitableResources(arObject);
  // List<EObject> childObjects = new ArrayList<>();
  //
  // if ((equivalentARElements != null)) {
  //
  // List<EObject> eObjList = new ArrayList<EObject>();
  // for (EObject eObject : equivalentARElements) {
  //
  // eObjList.add(eObject);
  // }
  //
  // if (eObjList.size() > 1) {
  //
  // for (EObject eObject : eObjList) {
  // if ((feature != null) && eObject.eIsSet(feature)) {
  // Object eGet = eObject.eGet(feature);
  // if (eGet instanceof List<?>) {
  // List values = (List<?>) eGet;
  // childObjects.addAll(values);
  // }
  // }
  // }
  // }
  //
  //
  // }
  //
  // return childObjects;
  // }

  // /**
  // * To get the list of AR Elements with the same qualified path
  // *
  // * @param arObject - AR Element Object
  // * @return Iterable
  // */
  // public static Iterable<EObject> getElementsFromSplitableResources(final EObject arObject) {
  // Iterable<EObject> filter = null;
  // List<EObject> instances = new ArrayList<>();
  // if (arObject != null) {
  // if (arObject.eResource() != null) {
  // final IFile file = EcorePlatformUtil.getFile(arObject);
  //
  // AutosarReleaseDescriptor autosarRelease = IAutosarWorkspacePreferences.AUTOSAR_RELEASE.get(file.getProject());
  // TransactionalEditingDomain editingDomain =
  // WorkspaceEditingDomainUtil.getEditingDomain(file.getProject(), autosarRelease);
  //
  // List<Resource> resources = editingDomain.getResourceSet().getResources();
  //
  // @SuppressWarnings("unchecked")
  // List<EObject> allInstancesAsList =
  // (List<EObject>) EObjectUtil.getAllInstancesOf(resources, arObject.eClass().getInstanceClass(), false);
  // instances.addAll(allInstancesAsList);
  // filter = getSplittableElements(arObject, instances);
  // }
  // else {
  // instances.add(arObject);
  // filter = Iterables.filter(instances, Predicates.alwaysTrue());
  // }
  // }
  // return filter;
  // }


  public static String getShortName(final Object eObject) {
    String gGetShortName;
    if (eObject instanceof GReferrable) {
      gGetShortName = ((GReferrable) eObject).gGetShortName();
    }
    else {
      gGetShortName = IdentifiableUtil.getShortName(eObject);
    }
    return gGetShortName;
  }

  // private static <T extends EObject> Iterable<T> getSplittableElements(final EObject arObject,
  // final List<T> instances) {
  // final String absoluteQualifiedName1 = AutosarURIFactory.getAbsoluteQualifiedName(arObject);
  // final String shortName1 = getShortName(arObject);
  //
  // Iterable<T> filter;
  // Predicate<T> predicate = new Predicate<T>() {
  //
  // @Override
  // public boolean apply(final T arg0) {
  // final String shortName = getShortName(arg0);
  // if (shortName1.equals(shortName)) {
  // String absoluteQualifiedName = AutosarURIFactory.getAbsoluteQualifiedName(arg0);
  // if (absoluteQualifiedName.equals(absoluteQualifiedName1)) {
  // return true;
  // }
  // }
  // return false;
  // }
  // };
  //
  // filter = Iterables.filter(instances, predicate);
  //
  //
  // return filter;
  // }

  /**
   * @param arPartRoot AUTOSAR
   * @param arPkgpath String
   * @return ARPackage
   */
  public static ARPackage getARArPackageFromRoot(final AUTOSAR arPartRoot, final String arPkgpath) {

    ARPackage arPkg = null;

    String arpkgPath[] = arPkgpath.split("/");

    for (String path : arpkgPath) {
      if (!path.isEmpty()) {
        ARPackage arPackage =
            getArPackageFromName(arPkg == null ? arPartRoot.getArPackages() : arPkg.getArPackages(), path);
        if (arPackage != null) {
          arPkg = arPackage;
        }
        else {
          return null;
        }
      }
    }

    return arPkg;

  }

  private static ARPackage getArPackageFromName(final List<ARPackage> arPackages, final String name) {

    for (ARPackage arp : arPackages) {

      if (arp.getShortName().equals(name)) {
        return arp;
      }

    }
    return null;

  }


  public static void addDefaultvalueForMandatoryParamDefs(final EcucModuleDef ecuCModuleDef,
      final EcucModuleConfigurationValues createEcucModuleConfigurationValue, final List<String> paramdefList)
      throws Exception {

    // List<String> list = Arrays.asList(this.paramdefs);

    Map<String, List<EObject>> paramConDefMap = new HashMap<>();
    collectAllParamContainerDefinitions(ecuCModuleDef.getContainers(), paramConDefMap, paramdefList);

    Map<String, List<EObject>> paramValuesMap = new HashMap<>();
    collectAllParamContainerValues(createEcucModuleConfigurationValue.getContainers(), paramValuesMap, paramdefList);

    for (Entry<String, List<EObject>> entry : paramConDefMap.entrySet()) {

      List<EObject> vList = paramValuesMap.get(entry.getKey());

      List<EObject> values = entry.getValue();

      if ((vList != null) && (values != null) && (values.size() > 0)) {

        for (EObject eObjCV : vList) {

          EcucContainerValue ecucValue = (EcucContainerValue) eObjCV;

          for (EObject eObjPD : values) {

            String fUri = EcoreUtil.getURI(eObjPD).fragment();
            final String f = fUri.substring(0, fUri.lastIndexOf("?type="));

            if (eObjPD instanceof EcucParameterDef) {

              EList<EcucParameterValue> parameterValues = ecucValue.getParameterValues();

              boolean exist = false;

              for (EcucParameterValue ecucParamV : parameterValues) {

                EcucParameterDef definition = ecucParamV.getDefinition();

                if ((definition != null) && !definition.eIsProxy()) {

                  String fUri1 = EcoreUtil.getURI(definition).fragment();
                  final String f1 = fUri1.substring(0, fUri1.lastIndexOf("?type="));

                  if (f1.equals(f)) {
                    exist = true;
                    break;
                  }
                }
              }

              if (!exist) {
                EcucParameterDef ecucRef = (EcucParameterDef) eObjPD;
                if (ecucRef instanceof EcucBooleanParamDef) {
                  EcucBooleanParamDef ecucBooleanParamDef = (EcucBooleanParamDef) ecucRef;
                  BooleanValueVariationPoint defaultValue = ecucBooleanParamDef.getDefaultValue();
                  if (defaultValue != null) {
                    EcucTextualParamValue createEcucTextualParamValue =
                        Autosar40Factory.eINSTANCE.createEcucTextualParamValue();
                    createEcucTextualParamValue.setDefinition(ecucRef);
                    createEcucTextualParamValue.setValue(defaultValue.getMixedText());
                    ecucValue.getParameterValues().add(createEcucTextualParamValue);
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("258_0", ecucValue.getShortName(), f).trim());

                  }
                  else {
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("259_0", ecucValue.getShortName(), f).trim());

                  }
                }
                else if (ecucRef instanceof EcucEnumerationParamDef) {
                  EcucEnumerationParamDef ecucEnumerationParamDef = (EcucEnumerationParamDef) ecucRef;
                  String defaultValue = ecucEnumerationParamDef.getDefaultValue();
                  if (defaultValue != null) {
                    EcucTextualParamValue createEcucTextualParamValue =
                        Autosar40Factory.eINSTANCE.createEcucTextualParamValue();
                    createEcucTextualParamValue.setDefinition(ecucRef);
                    createEcucTextualParamValue.setValue(defaultValue);
                    ecucValue.getParameterValues().add(createEcucTextualParamValue);
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("258_0", ecucValue.getShortName(), f).trim());

                  }
                  else {
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("259_0", ecucValue.getShortName(), f).trim());

                  }
                }
                else if (ecucRef instanceof EcucFloatParamDef) {
                  EcucFloatParamDef ecucFloatParamDef = (EcucFloatParamDef) ecucRef;
                  FloatValueVariationPoint defaultValue = ecucFloatParamDef.getDefaultValue();

                  if (defaultValue != null) {
                    EcucNumericalParamValue createEcucNumericalParamValue =
                        Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
                    createEcucNumericalParamValue.setDefinition(ecucRef);
                    createEcucNumericalParamValue
                        .setValue(GenerateArxmlUtil.createNumericalValueVariationPoint(defaultValue.getMixedText()));
                    ecucValue.getParameterValues().add(createEcucNumericalParamValue);
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("258_0", ecucValue.getShortName(), f).trim());

                  }
                  else {
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("259_0", ecucValue.getShortName(), f).trim());

                  }
                }
                else if (ecucRef instanceof EcucIntegerParamDef) {
                  EcucIntegerParamDef ecucIntegerParamDef = (EcucIntegerParamDef) ecucRef;
                  UnlimitedIntegerValueVariationPoint defaultValue = ecucIntegerParamDef.getDefaultValue();

                  if (defaultValue != null) {
                    EcucNumericalParamValue createEcucNumericalParamValue =
                        Autosar40Factory.eINSTANCE.createEcucNumericalParamValue();
                    createEcucNumericalParamValue.setDefinition(ecucRef);
                    createEcucNumericalParamValue
                        .setValue(GenerateArxmlUtil.createNumericalValueVariationPoint(defaultValue.getMixedText()));
                    ecucValue.getParameterValues().add(createEcucNumericalParamValue);
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("258_0", ecucValue.getShortName(), f).trim());

                  }
                  else {
                    LOGGER.warn(
                        RteConfGenMessageDescription.getFormattedMesssage("259_0", ecucValue.getShortName(), f).trim());

                  }
                }
                else {
                  LOGGER.warn(
                      RteConfGenMessageDescription.getFormattedMesssage("259_0", ecucValue.getShortName(), f).trim());

                }
              }


            }
            else if (eObjPD instanceof EcucAbstractReferenceDef) {

              EList<EcucAbstractReferenceValue> refValues = ecucValue.getReferenceValues();

              boolean exist = false;

              for (EcucAbstractReferenceValue refValue : refValues) {

                EcucAbstractReferenceDef definition = refValue.getDefinition();

                if ((definition != null) && !definition.eIsProxy()) {

                  String fUri1 = EcoreUtil.getURI(definition).fragment();
                  final String f1 = fUri1.substring(0, fUri1.lastIndexOf("?type="));

                  if (f1.equals(f)) {
                    exist = true;
                    break;
                  }
                }
              }

              if (!exist) {
                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("259_0", ecucValue.getShortName(), f));
              }
            }
          }
        }
      }
    }

  }

  private static void collectAllParamContainerValues(final List<EcucContainerValue> containers,
      final Map<String, List<EObject>> paramValuesMap, final List<String> paramDeflist) {


    for (EcucContainerValue ecuConVa : containers) {

      if (!ecuConVa.eIsProxy()) {

        if ((ecuConVa.getDefinition() != null) && !ecuConVa.getDefinition().eIsProxy()) {
          String fUri = EcoreUtil.getURI(ecuConVa.getDefinition()).fragment();
          final String f = fUri.substring(0, fUri.lastIndexOf("?type="));

          if (paramDeflist.stream().anyMatch(p -> f.endsWith(p))) {
            List<EObject> list = paramValuesMap.get(f);
            if (list != null) {
              list.add(ecuConVa);
            }
            else {
              List<EObject> listOfEObjs = new ArrayList<>();
              listOfEObjs.add(ecuConVa);
              paramValuesMap.put(f, listOfEObjs);
            }
          }
        }

        if (!ecuConVa.getSubContainers().isEmpty()) {
          collectAllParamContainerValues(ecuConVa.getSubContainers(), paramValuesMap, paramDeflist);
        }
      }
    }

  }

  private static int getLowerMultiplicity(final PositiveIntegerValueVariationPoint positiveIntValueVariationPoint) {


    int value = -1;

    String mixedText = positiveIntValueVariationPoint.getMixedText();
    if ((mixedText != null) && !mixedText.isEmpty()) {

      try {
        value = Integer.parseInt(mixedText.trim());
      }
      catch (Exception ex) {
        value = -1;
      }

    }
    return value;

  }


  private static void collectAllParamContainerDefinitions(final List<EcucContainerDef> containers,
      final Map<String, List<EObject>> paramConDefMap, final List<String> paramDeflist) {


    for (EcucContainerDef ecuConDef : containers) {

      if (ecuConDef instanceof EcucParamConfContainerDef) {
        EcucParamConfContainerDef ecuParamConDef = (EcucParamConfContainerDef) ecuConDef;

        String fUri = EcoreUtil.getURI(ecuParamConDef).fragment();
        final String f = fUri.substring(0, fUri.lastIndexOf("?type="));

        if (paramDeflist.stream().anyMatch(p -> f.endsWith(p))) {
          List<EObject> listOfEObjs = new ArrayList<>();

          for (EcucParameterDef paramDef : ecuParamConDef.getParameters()) {
            if (!paramDef.eIsProxy() && (getLowerMultiplicity(paramDef.getLowerMultiplicity()) > 0)) {
              listOfEObjs.add(paramDef);
            }
          }
          for (EcucAbstractReferenceDef ecucRefDef : ecuParamConDef.getReferences()) {
            if (!ecucRefDef.eIsProxy() && (getLowerMultiplicity(ecucRefDef.getLowerMultiplicity()) > 0)) {
              listOfEObjs.add(ecucRefDef);
            }
          }

          paramConDefMap.put(f, listOfEObjs);

          if (!ecuParamConDef.getSubContainers().isEmpty()) {
            collectAllParamContainerDefinitions(ecuParamConDef.getSubContainers(), paramConDefMap, paramDeflist);
          }
        }
      }
    }
  }

  /**
   * @param orgshortName
   * @param eObject
   * @return
   */
  public final static String getShortenedNameOfElements(final String orgshortName, final Map<String, ?> elementMap) {
    String changedShortName = orgshortName;
    if (changedShortName != null) {
      int cnt = 0;
      if (changedShortName.length() > 127) {
        // if (changedShortName.length() > 92) {
        changedShortName = changedShortName.substring(0, 120);
        // changedShortName = changedShortName.substring(0, 85);
        String tempName = changedShortName + "_" + cnt;
        if ((elementMap != null) && !elementMap.isEmpty()) {
          while (elementMap.get(tempName) != null) {
            cnt++;
            tempName = changedShortName + "_" + cnt;
          }
        }
        changedShortName = tempName;
      }

    }
    return changedShortName;
  }

  /**
   * For handling shortname of swAddrName, startonandevent and handleref values
   *
   * @param elementName
   * @param elementMap
   * @param elemntNameRefMap
   * @return
   */
  public final static String getShortenedNameOfElements(final String elementName, final Map<String, ?> elementMap,
      final Map<String, String> elemntNameRefMap) {
    String elementShortName = elementName;
    if (elementShortName != null) {
      int cnt = 0;
      // if (elementShortName.length() > 127) {
      if (elementShortName.length() > 92) {
        // elementShortName = elementShortName.substring(0, 120);
        elementShortName = elementShortName.substring(0, 85);
        String tempName = elementShortName + "_" + cnt;

        while (elementMap.get(tempName) != null) {
          cnt++;
          tempName = elementShortName + "_" + cnt;
        }
        elementShortName = tempName;
        elemntNameRefMap.put(elementName, elementShortName);
      }

    }
    return elementShortName;

  }

  /**
   * @param containerValueName
   * @param createEcucContainerValueList
   * @param containerValueNameMap
   * @return
   */
  public static String getShortenedNameOfElements(final String containerValueName,
      final List<EcucContainerValue> createEcucContainerValueList) {
    String shortcontainerValueName = containerValueName;
    if (shortcontainerValueName != null) {
      int cnt = 0;
      if (shortcontainerValueName.length() > 127) {
        shortcontainerValueName = shortcontainerValueName.substring(0, 120);
        String tempName = shortcontainerValueName + "_" + cnt;

        if ((createEcucContainerValueList != null) && !createEcucContainerValueList.isEmpty()) {
          while (isContainerValueExist(createEcucContainerValueList, tempName)) {
            cnt++;
            tempName = shortcontainerValueName + "_" + cnt;
          }
        }
        shortcontainerValueName = tempName;

      }

    }
    return shortcontainerValueName;
  }


  /**
   * @param createEcucContainerValueList
   * @param containerValueName
   * @return
   */
  public static boolean isContainerValueExist(final List<EcucContainerValue> createEcucContainerValueList,
      final String containerValueName) {

    return createEcucContainerValueList.stream().anyMatch(m -> m.getShortName().equals(containerValueName));

  }

  /**
   * @param eObject
   * @return
   */
  public static String getFragmentURI(final EObject eObject) {
    String uri = null;
    if (eObject != null) {
      URI uri2 = EcoreUtil.getURI(eObject);
      if (uri2 != null) {
        String fragment = uri2.fragment();
        if (fragment.contains("?type=")) {
          return fragment.substring(0, fragment.lastIndexOf("?type="));
        }
        return fragment;
      }
    }
    return uri;
  }


  /**
   * @return
   */
  public static String getShortNameOfURI(final String obj) {
    String shortName = obj.substring(obj.lastIndexOf("/") + 1, obj.length()).trim();
    return shortName;

  }

  /**
   * @param triggers1
   * @param triggers2
   * @return
   */
  public static boolean isTriggersSame(final EList<Trigger> triggers1, final EList<Trigger> triggers2) {

    boolean isSame = false;

    for (Trigger t1 : triggers1) {

      isSame = false;
      for (Trigger t2 : triggers2) {

        if (t1.getShortName().equals(t2.getShortName())) {
          isSame = true;
        }
      }

      if (!isSame) {
        return false;
      }
    }
    return isSame;
  }

  /**
   * @param eObject
   * @param crossReferenceAdapter
   * @param features
   * @return
   */
  public static List<EObject> getReferencers(final EObject eObject, final ECrossReferenceAdapter crossReferenceAdapter,
      final EReference[] features) {


    Collection<Setting> settings = crossReferenceAdapter.getInverseReferences(eObject, true);


    if (settings.isEmpty() == false) {

      List<EObject> referencers = new ArrayList<EObject>();

      int count;

      if ((features != null) && ((count = features.length) != 0)) {

        Iterator<Setting> it = settings.iterator();

        while (it.hasNext()) {

          Setting setting = it.next();

          EStructuralFeature feature = setting.getEStructuralFeature();

          for (int i = 0; i < count; ++i) {
            if (feature == features[i]) {
              referencers.add(setting.getEObject());
              break;
            }
          }
        }
      }
      else {
        Iterator<Setting> it = settings.iterator();
        while (it.hasNext()) {
          referencers.add(it.next().getEObject());
        }
      }
      return referencers;
    }
    return new ArrayList<>();
  }

  /**
   * @param eRef
   * @param object
   * @return
   */
  public static List<EStructuralFeature> getEStructuralFeature(final EObject parent, final EObject child) {
    List<EStructuralFeature> refEstructures = new ArrayList<>();
    EList<EStructuralFeature> eAllStructuralFeatures = (parent.eClass().getEAllStructuralFeatures());
    for (EStructuralFeature es : eAllStructuralFeatures) {
      if (parent.eGet(es) instanceof List) {
        List l = (List) parent.eGet(es);
        if (l.contains(child)) {
          refEstructures.add(es);
        }
      }
      else if ((parent.eGet(es) != null) && (parent.eGet(es) instanceof EObject) && parent.eGet(es).equals(child)) {
        refEstructures.add(es);
      }
    }
    return refEstructures;
  }

}
