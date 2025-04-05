/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.artop.aal.common.metamodel.AutosarReleaseDescriptor;
import org.artop.aal.workspace.preferences.IAutosarWorkspacePreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;

import com.bosch.tisched.rteconfig.generator.service.AutosarProxyResolver;

import autosar40.bswmodule.bswbehavior.BswEvent;
import autosar40.bswmodule.bswbehavior.BswModuleEntity;
import autosar40.ecucdescription.EcucAbstractReferenceValue;
import autosar40.ecucdescription.EcucContainerValue;
import autosar40.ecucdescription.EcucReferenceValue;
import autosar40.genericstructure.generaltemplateclasses.arpackage.PackageableElement;
import autosar40.genericstructure.generaltemplateclasses.identifiable.Referrable;
import autosar40.swcomponent.swcinternalbehavior.RunnableEntity;
import autosar40.swcomponent.swcinternalbehavior.rteevents.RTEEvent;
import autosar40.util.Autosar40Package;
import gautosar.ggenericstructure.ginfrastructure.GIdentifiable;
import gautosar.ggenericstructure.ginfrastructure.GReferrable;

/**
 * @author SHK1COB
 */
public class AutosarMergeUtil {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(AutosarMergeUtil.class.getName());

  private static int getCount(final String pkgPath, final char ch) {
    int count = 0;
    for (int i = 0; i < pkgPath.length(); i++) {
      if (pkgPath.charAt(i) == ch) {
        count++;
      }
    }
    return count;
  }

  private static int getPosition(final String type, final List<Object> list) {
    int position = 0;
    for (Object o : list) {
      String s = (String) o;
      if (s.endsWith(type)) {
        return position;
      }
      position++;
    }
    return position;
  }

  private static Map<String, String> getPathFragmentFragmentMap(final TransactionalEditingDomain editingDomain,
      final List<Resource> listOfResources) {
    Map<String, String> pathFragmentFragmentMap = new HashMap<>();

    Map<String, String> fragmentPathMap = new HashMap<>();
    String pattern = "/[A-Za-z0-9_]*\\?type=[a-zA-Z0-9_]*";
    if ((listOfResources != null) && !listOfResources.isEmpty()) {
      for (Resource resource : editingDomain.getResourceSet().getResources()) {
        TreeIterator<EObject> iterator = resource.getAllContents();
        while (iterator.hasNext()) {
          EObject currentObject = iterator.next();
          String fragment = EcoreUtil.getURI(currentObject).fragment();
          int lastIndexOf = fragment.lastIndexOf("/");
          if (lastIndexOf >= 0) {
            String lastfragment = fragment.substring(fragment.lastIndexOf("/"), fragment.length());
            if (lastfragment.matches(pattern) && !lastfragment.endsWith("/?type=AUTOSAR") &&
                !lastfragment.endsWith("?type=ARPackage")) {
              if (fragmentPathMap.keySet().contains(fragment) && listOfResources.contains(resource)) {
                if (!pathFragmentFragmentMap.values().contains(fragment)) {
                  pathFragmentFragmentMap.put(fragmentPathMap.get(fragment) + "<->" + fragment, fragment);
                }
                if (fragmentPathMap.get(fragment).equalsIgnoreCase(resource.getURI().path())) {
                  pathFragmentFragmentMap.put(resource.getURI().path().toString() + "<->" + fragment + "<->" + fragment,
                      fragment);
                }
                pathFragmentFragmentMap.put(resource.getURI().path().toString() + "<->" + fragment, fragment);
              }
              fragmentPathMap.put(fragment, resource.getURI().path().toString());
            }
          }
        }
      }
    }
    else {
      for (Resource resource : editingDomain.getResourceSet().getResources()) {
        TreeIterator<EObject> iterator = resource.getAllContents();
        while (iterator.hasNext()) {
          EObject currentObject = iterator.next();
          String fragment = EcoreUtil.getURI(currentObject).fragment();
          int lastIndexOf = fragment.lastIndexOf("/");
          if (lastIndexOf >= 0) {
            String lastfragment = fragment.substring(fragment.lastIndexOf("/"), fragment.length());
            if (lastfragment.matches(pattern) && !lastfragment.endsWith("/?type=AUTOSAR") &&
                !lastfragment.endsWith("?type=ARPackage")) {
              if (fragmentPathMap.keySet().contains(fragment)) {
                if (!pathFragmentFragmentMap.values().contains(fragment)) {
                  pathFragmentFragmentMap.put(fragmentPathMap.get(fragment) + "<->" + fragment, fragment);
                }
                if (fragmentPathMap.get(fragment).equalsIgnoreCase(resource.getURI().path())) {
                  pathFragmentFragmentMap.put(resource.getURI().path().toString() + "<->" + fragment + "<->" + fragment,
                      fragment);
                }
                pathFragmentFragmentMap.put(resource.getURI().path().toString() + "<->" + fragment, fragment);
              }
              fragmentPathMap.put(fragment, resource.getURI().path().toString());
            }
          }
        }
      }
    }
    return pathFragmentFragmentMap;
  }


  private static Map<String, List<String>> getFragmentResourcesMap(final Map<String, String> pathFragmentFragmentMap) {
    Map<String, List<String>> fragmentResourcesMap = new HashMap<String, List<String>>();
    for (String pathFragment : pathFragmentFragmentMap.keySet()) {
      String fragment = pathFragmentFragmentMap.get(pathFragment);
      List<String> pathList = fragmentResourcesMap.get(fragment);

      if (pathList != null) {
        String path =
            pathFragment.contains("<->") ? pathFragment.substring(0, pathFragment.indexOf("<->")) : pathFragment;
        pathList.add(path);
      }
      else {
        List<String> newPathList = new ArrayList<String>();
        String path =
            pathFragment.contains("<->") ? pathFragment.substring(0, pathFragment.indexOf("<->")) : pathFragment;
        newPathList.add(path);
        fragmentResourcesMap.put(fragment, newPathList);
      }
    }
    return fragmentResourcesMap;
  }

  private static Map<String, List<Map<String, List<String>>>> getTypeFragmentResourcesMap(
      final Map<String, String> pathFragmentFragmentMap, final Map<String, List<String>> fragmentResourcesMap) {
    // type grouping
    Map<String, List<Map<String, List<String>>>> typeFragmentResourcesMap = new HashMap<>();
    for (String fragment : fragmentResourcesMap.keySet()) {
      String type = fragment.substring(fragment.lastIndexOf("?type=") + 6, fragment.length());
      List<Map<String, List<String>>> fragmentResourcesMapList = typeFragmentResourcesMap.get(type);
      if (fragmentResourcesMapList != null) {
        Map<String, List<String>> newFragmentResourcesMap = new HashMap<>();
        newFragmentResourcesMap.put(fragment, fragmentResourcesMap.get(fragment));
        fragmentResourcesMapList.add(newFragmentResourcesMap);
      }
      else {
        List<Map<String, List<String>>> newTtypeFragmentResourcesMap = new ArrayList<>();
        Map<String, List<String>> newFragmentResourcesMap = new HashMap<>();
        newFragmentResourcesMap.put(fragment, fragmentResourcesMap.get(fragment));
        newTtypeFragmentResourcesMap.add(newFragmentResourcesMap);
        typeFragmentResourcesMap.put(type, newTtypeFragmentResourcesMap);
      }
    }
    return typeFragmentResourcesMap;
  }

  private static List<Object> getSortedList(final Object fragments[]) {
    List<Object> sequenceList = Arrays.asList(fragments);
    Collections.sort(sequenceList, new Comparator<Object>() {

      @Override
      public int compare(final Object o1, final Object o2) {
        String fragment1 = (String) o1;
        String uri1 = fragment1.substring(0, fragment1.lastIndexOf("?type="));
        int count1 = getCount(uri1, '/');
        String fragment2 = (String) o2;
        String uri2 = fragment2.substring(0, fragment2.lastIndexOf("?type="));
        int count2 = getCount(uri2, '/');
        return count2 - count1;
      }
    });
    return sequenceList;
  }

  private static List<Object> getReSortedList(final Object types[], final List<Object> sequenceList) {
    // sequencing
    List<Object> reSequencedList = Arrays.asList(types);
    Collections.sort(reSequencedList, new Comparator<Object>() {

      @Override
      public int compare(final Object o1, final Object o2) {
        String type1 = (String) o1;
        int position1 = getPosition(type1, sequenceList);
        String type2 = (String) o2;
        int position2 = getPosition(type2, sequenceList);
        return position1 - position2;
      }
    });
    return reSequencedList;
  }

  private static void sortFragmentResourcesMap(final List<Map<String, List<String>>> fragmentResourcesMapList) {
    Collections.sort(fragmentResourcesMapList, new Comparator<Map<String, List<String>>>() {

      @Override
      public int compare(final Map<String, List<String>> m1, final Map<String, List<String>> m2) {
        String fragment1 = (String) m1.keySet().toArray()[0];
        String uri1 = fragment1.substring(0, fragment1.lastIndexOf("?type="));
        int count1 = getCount(uri1, '/');
        String fragment2 = (String) m2.keySet().toArray()[0];
        String uri2 = fragment2.substring(0, fragment2.lastIndexOf("?type="));
        int count2 = getCount(uri2, '/');
        return count2 - count1;
      }
    });
  }

  private static Map<String, List<EObject>> getURIEObjListMap(final Map<String, List<String>> fragmentResourceListMap,
      final Map<String, EObject> urifragmentEObjectsMap) {
    Map<String, List<EObject>> uriEObjListMap = new HashMap<>();
    for (Object uriObj : fragmentResourceListMap.keySet()) {
      String uri = (String) uriObj;
      boolean hasDuplicates =
          fragmentResourceListMap.get(uriObj).stream().distinct().count() != fragmentResourceListMap.get(uriObj).size();
      for (String frgment : fragmentResourceListMap.get(uriObj)) {
        if ((fragmentResourceListMap.get(uriObj).size() > 1) && hasDuplicates) {
          EObject eObject = urifragmentEObjectsMap.get(frgment + "<->" + uri + "<->" + uri);
          if (eObject != null) {
            List<EObject> eObjList = uriEObjListMap.get(uri);
            if (eObjList != null) {
              eObjList.add(eObject);
            }
            else {
              List<EObject> newEObjectList = new ArrayList<>();
              newEObjectList.add(eObject);
              uriEObjListMap.put(uri, newEObjectList);
            }
          }
          else {
            LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : " + frgment + "<->" + uri + " is missing");
          }
        }
        EObject eObject = urifragmentEObjectsMap.get(frgment + "<->" + uri);
        if (eObject != null) {
          List<EObject> eObjList = uriEObjListMap.get(uri);
          if (eObjList != null) {
            eObjList.add(eObject);
          }
          else {
            List<EObject> newEObjectList = new ArrayList<>();
            newEObjectList.add(eObject);
            uriEObjListMap.put(uri, newEObjectList);
          }
        }
        else {
          LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : " + frgment + "<->" + uri + " is missing");
        }
      }
    }
    return uriEObjListMap;
  }

  private static void printMessage(final StringBuilder strBuffer, final String msg, final String lineSeparator) {
    System.out.println(msg);
    strBuffer.append(msg);
    strBuffer.append(lineSeparator);
  }

  /**
   * @param iProject IProject
   * @param listOfResources List<Resource>
   * @return StringBuilder
   * @throws Exception
   */
  public static StringBuilder mergeAutosarElements(final IProject iProject, final List<Resource> listOfResources)
      throws Exception {
    StringBuilder strBuffer = new StringBuilder();
    String lineBreaker = System.getProperty("line.separator");
    AutosarReleaseDescriptor autosarRelease = IAutosarWorkspacePreferences.AUTOSAR_RELEASE.get(iProject);
    TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(iProject, autosarRelease);
    Map<String, EObject> pathFragmentEcucRefsMap = new HashMap<>();
    Map<String, String> ecucRefsMap = new HashMap<>();
    Map<String, EObject> mergedURIEObjectsMap = new HashMap<>();
    List<String> unsetURIs = new ArrayList<String>();
    Map<EcucReferenceValue, String> eRefProxyURIMap = new HashMap<>();
    try {
      List<EcucReferenceValue> allInstancesOf =
          EObjectUtil.getAllInstancesOf(editingDomain.getResourceSet().getResources(), EcucReferenceValue.class, false);
      for (EcucReferenceValue ecucReference : allInstancesOf) {
        Referrable value = ecucReference.getValue();
        if ((value != null) && !value.eIsProxy()) {
          URI uri = EcoreUtil.getURI(value);
          URI containerUri = EcoreUtil.getURI(ecucReference.eContainer());
          pathFragmentEcucRefsMap.put(uri.path() + "<->" + uri.fragment() + "<->" + containerUri.fragment(),
              ecucReference);
          ecucRefsMap.put(uri.fragment(), ecucReference.getDefinition().getShortName());
        }
        if ((value != null)) {
          String fragment = EcoreUtil.getURI(value).fragment();
          if ((fragment != null) && fragment.contains("?type=")) {
            eRefProxyURIMap.put(ecucReference, fragment);
          }
        }
      }
    }
    catch (Exception ex) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());
    }
    Map<Map<String, EStructuralFeature>, String> crossRefMap = new HashMap<>();
    List<RTEEvent> allInstancesOf2 =
        EObjectUtil.getAllInstancesOf(editingDomain.getResourceSet().getResources(), RTEEvent.class, false);
//    Map<String, String> rteEventRunnableEntityMap = allInstancesOf2.stream().collect(
//        Collectors.toMap(k -> EcoreUtil.getURI(k).fragment(), k -> EcoreUtil.getURI(k.getStartOnEvent()).fragment()));
    Map<String, String> rteEventRunnableEntityMap = new HashMap<>();
    allInstancesOf2.stream().forEach(k -> {
      String key = EcoreUtil.getURI(k).fragment();
      if (rteEventRunnableEntityMap.get(key) == null) {
        rteEventRunnableEntityMap.put(key, EcoreUtil.getURI(k.getStartOnEvent()).fragment());
      }
    });
    List<BswEvent> allInstancesOf3 =
        EObjectUtil.getAllInstancesOf(editingDomain.getResourceSet().getResources(), BswEvent.class, false);
//    Map<String, String> bswEventModuleEntityMap = allInstancesOf3.stream().collect(
//        Collectors.toMap(k -> EcoreUtil.getURI(k).fragment(), k -> EcoreUtil.getURI(k.getStartsOnEvent()).fragment()));
    Map<String, String> bswEventModuleEntityMap = new HashMap<>();
    allInstancesOf3.stream().forEach(k -> {
      String key = EcoreUtil.getURI(k).fragment();
      if (bswEventModuleEntityMap.get(key) == null) {
        bswEventModuleEntityMap.put(key, EcoreUtil.getURI(k.getStartsOnEvent()).fragment());
      }
    });
    ChangeCommand merge = new ChangeCommand(new ArrayList<Notifier>()) {

      @Override
      protected void doExecute() {
        Map<String, String> pathFragmentFragmentMap = getPathFragmentFragmentMap(editingDomain, listOfResources);
        Map<String, List<String>> fragmentResourcesMap = getFragmentResourcesMap(pathFragmentFragmentMap);
        Map<String, List<Map<String, List<String>>>> typeFragmentResourcesMap =
            getTypeFragmentResourcesMap(pathFragmentFragmentMap, fragmentResourcesMap);

        List<Object> sequenceList = getSortedList(fragmentResourcesMap.keySet().toArray());

        List<Object> reSequencedList = getReSortedList(typeFragmentResourcesMap.keySet().toArray(), sequenceList);
        try {
          for (Object o : reSequencedList) {
            String type = (String) o;
            List<String> duplicateUri = new ArrayList<String>();
            List<Map<String, List<String>>> fragmentResourcesMapList = typeFragmentResourcesMap.get(type);
            checkForDuplicateValues(fragmentResourcesMapList, duplicateUri);
            EClass eClass = Autosar40Package.eINSTANCE.getEClass(type);
            Class<?> classType = Class.forName(eClass.getInstanceTypeName());
            Map<String, EObject> urifragmentEObjectsMap = new HashMap<>();
            @SuppressWarnings("unchecked")
            List<EObject> allInstancesOf2 = (List<EObject>) EObjectUtil
                .getAllInstancesOf(editingDomain.getResourceSet().getResources(), classType, false);
            for (EObject eobj : allInstancesOf2) {
              URI uri = EcoreUtil.getURI(eobj);
              if (uri != null) {
                if (duplicateUri.contains(uri.path().toString()) &&
                    urifragmentEObjectsMap.containsKey(uri.path().toString() + "<->" + uri.fragment())) {
                  urifragmentEObjectsMap.put(uri.path().toString() + "<->" + uri.fragment() + "<->" + uri.fragment(),
                      eobj);
                }
                else {
                  urifragmentEObjectsMap.put(uri.path().toString() + "<->" + uri.fragment(), eobj);
                }
              }
            }
            sortFragmentResourcesMap(fragmentResourcesMapList);
            for (Map<String, List<String>> fragmentResourceListMap : fragmentResourcesMapList) {
              Map<String, List<EObject>> uriEObjListMap =
                  getURIEObjListMap(fragmentResourceListMap, urifragmentEObjectsMap);
              for (String fragment : uriEObjListMap.keySet()) {
                Map<EStructuralFeature, Object> eStructObjectMap = new HashMap<>();
                String path = fragment.substring(0, fragment.lastIndexOf("?type="));
                List<EObject> objectSet = uriEObjListMap.get(fragment);
                if ((objectSet == null) || objectSet.isEmpty()) {
                  printMessage(strBuffer, "MM_DCS_RTECONFGEN_DEBUG : Empty List", lineBreaker);
                  continue;
                }
                printMessage(strBuffer,
                    "MM_DCS_RTECONFGEN_DEBUG : " + path + " splitted into " + objectSet.size() + " files", lineBreaker);
                printMessage(strBuffer, "MM_DCS_RTECONFGEN_DEBUG : Merging " + fragment, lineBreaker);
                EList<EStructuralFeature> eAllStructuralFeatures =
                    ((EObject) objectSet.toArray()[0]).eClass().getEAllStructuralFeatures();
                Map<String, EObject> featureEObjectMap = new HashMap<>();
                for (EObject eObj : objectSet) {
                  printMessage(strBuffer,
                      "MM_DCS_RTECONFGEN_DEBUG : " + EcorePlatformUtil.getResource(eObj).getURI().path(), lineBreaker);
                  if (!eObj.eContents().isEmpty()) {
                    for (EStructuralFeature eStructuralFeature : eAllStructuralFeatures) {
                      if (isSplitable(eStructuralFeature.getEAnnotations())) {
                        Object eGet = eObj.eGet(eStructuralFeature);
                        if (eGet != null) {
                          if ((eStructuralFeature.getUpperBound() < 0) && (eGet instanceof List)) {
                            Object object = eStructObjectMap.get(eStructuralFeature);
                            if ((object != null) && (object instanceof List)) {
                              List<Object> existingList = (List<Object>) object;
                              List<?> eList = (List<?>) eGet;
                              if (!eList.isEmpty() && !featureEObjectMap.isEmpty() &&
                                  ((eList.get(0) instanceof GReferrable) || (eList.get(0) instanceof GIdentifiable))) {
                                for (Object eO : eList) {
                                  String shortName = GenerateArxmlUtil.getShortName(eO);
                                  if (featureEObjectMap.get(eStructuralFeature.getName() + "_" + shortName) == null) {
                                    featureEObjectMap.put(eStructuralFeature.getName() + "_" + shortName, (EObject) eO);
                                    existingList.add(eO);
                                  }
                                }
                              }
                              else {
                                existingList.addAll(eList);
                              }
                            }
                            else {
                              List<?> eList = (List<?>) eGet;
                              boolean isReferrable = false;
                              for (Object eO : eList) {
                                if (isReferrable) {
                                  featureEObjectMap.put(
                                      eStructuralFeature.getName() + "_" + GenerateArxmlUtil.getShortName(eO),
                                      (EObject) eO);
                                  continue;
                                }
                                if ((eO instanceof GReferrable) || (eO instanceof GIdentifiable)) {
                                  isReferrable = true;
                                  featureEObjectMap.put(
                                      eStructuralFeature.getName() + "_" + GenerateArxmlUtil.getShortName(eO),
                                      (EObject) eO);
                                }
                                else {
                                  break;
                                }
                              }
                              // intentionally set the boolean flag
                              isReferrable = false;
                              eStructObjectMap.put(eStructuralFeature, EcoreUtil.copyAll((List<?>) eList));
                            }
                          }
                        }
                      }
                    }
                  }
                }
                for (EObject eObject : objectSet) {
                  for (EStructuralFeature eStructuralFeature : eStructObjectMap.keySet()) {
                    eObject.eUnset(eStructuralFeature);
                  }
                  for (EStructuralFeature eStructuralFeature : eStructObjectMap.keySet()) {
                    eObject.eSet(eStructuralFeature,
                        EcoreUtil.copyAll((List<?>) eStructObjectMap.get(eStructuralFeature)));
                  }
                }
                mergedURIEObjectsMap.put(fragment, objectSet.get(0));
                // addContainerObjectsforEcucReferenceResolve(mergedURIEObjectsMap, fragment, objectSet.get(0));
              }
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
            e.printStackTrace();
          }
          strBuffer.setLength(0);
        }
      }
    };
    editingDomain.getCommandStack().execute(merge);
    try {
      TimeUnit.SECONDS.sleep(1);
    }
    catch (InterruptedException e) {
      // do noting
    }
    List<Referrable> allInstancesOf1 =
        EObjectUtil.getAllInstancesOf(editingDomain.getResourceSet().getResources(), Referrable.class, false);
    Map<String, Referrable> refList1 = new HashMap<>();
    for (Referrable ref : allInstancesOf1) {
      if (!ref.eIsProxy()) {
        String u = EcoreUtil.getURI(ref).fragment();
        refList1.put(u, ref);
      }
    }
    AutosarProxyResolver.setUriEObjectMap(refList1);
    List<Map<String, EStructuralFeature>> collect = crossRefMap.keySet().stream()
        .filter(k -> !unsetURIs.contains(k.keySet().toArray()[0])).collect(Collectors.toList());
    for (Map<String, EStructuralFeature> entry : collect) {
      crossRefMap.remove(entry);
    }
    resolveEcuCRefernceValues(iProject, editingDomain, mergedURIEObjectsMap, pathFragmentEcucRefsMap, ecucRefsMap,
        eRefProxyURIMap, crossRefMap, rteEventRunnableEntityMap, bswEventModuleEntityMap);
    LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : Merging done");
    return strBuffer;
  }

  /**
   * @param fragmentResourcesMapList
   * @param duplicateUri
   */
  protected static void checkForDuplicateValues(final List<Map<String, List<String>>> fragmentResourcesMapList,
      final List<String> duplicateUri) {
    Map<String, Set<String>> valueTracker = new HashMap<>();
    for (Map<String, List<String>> map : fragmentResourcesMapList) {
      for (Map.Entry<String, List<String>> entry : map.entrySet()) {
        String key = entry.getKey();
        List<String> values = entry.getValue();

        valueTracker.putIfAbsent(key, new HashSet<>());

        for (String value : values) {
          if (!valueTracker.get(key).add(value)) { // If value already exists, it's a duplicate
            duplicateUri.add(value.toString());
          }
        }
      }
    }
  }

  private static void addContainerObjectsforEcucReferenceResolve(final Map<String, EObject> mergedURIEObjectsMap,
      final String fragment, final EObject eObject) {
    mergedURIEObjectsMap.put(fragment, eObject);
    if ((eObject.eContainer() != null) && !(eObject.eContainer() instanceof PackageableElement)) {
      String uri = EcoreUtil.getURI(eObject.eContainer()).fragment();
      if (uri.contains("?type=")) {
        String path = uri.substring(0, fragment.lastIndexOf("?type="));
        mergedURIEObjectsMap.put(path, eObject.eContainer());
      }
    }
  }

  private static void resolveEcuCRefernceValues(final IProject iProject, final TransactionalEditingDomain editingDomain,
      final Map<String, EObject> mergedURIEObjectsMap, final Map<String, EObject> pathFragmentEcucRefsMap,
      final Map<String, String> ecucRefsMap, final Map<EcucReferenceValue, String> eRefProxyURIMap,
      final Map<Map<String, EStructuralFeature>, String> crossRefMap,
      final Map<String, String> rteEventRunnableEntityMap, final Map<String, String> bswEventModuleEntityMap) {
    ChangeCommand resolveEcucReferenceValue = new ChangeCommand(new ArrayList<Notifier>()) {

      @Override
      protected void doExecute() {
        try {
          LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : Resolving EcuCReference values");
          List<Referrable> allInstancesOf =
              EObjectUtil.getAllInstancesOf(editingDomain.getResourceSet().getResources(), Referrable.class, false);
          Map<String, Referrable> refList = new HashMap<>();
          Map<String, List<Referrable>> refList1 = new HashMap<>();
          for (Referrable ref : allInstancesOf) {
            if (!ref.eIsProxy()) {
              refList.put(EcoreUtil.getURI(ref).fragment(), ref);
              String u = EcoreUtil.getURI(ref).fragment();
              if (refList1.get(u) != null) {
                refList1.get(u).add(ref);
              }
              else {
                List<Referrable> l = new ArrayList<>();
                l.add(ref);
                refList1.put(u, l);
              }
            }
          }
          for (String fragment : mergedURIEObjectsMap.keySet()) {
            EObject eObject = mergedURIEObjectsMap.get(fragment);
            String uri = fragment.substring(0, fragment.lastIndexOf("?type="));
            List<EObject> ecucReferenceValues =
                getEcucReferenceValues(uri, pathFragmentEcucRefsMap, mergedURIEObjectsMap, refList);
            if ((eObject != null) && (!ecucReferenceValues.isEmpty())) {
              Referrable referrable = refList.get(fragment);
              for (EObject eObj : ecucReferenceValues) {
                EcucReferenceValue ecucr = (EcucReferenceValue) eObj;
                if (ecucr.eIsProxy()) {
                  continue;
                }
                ecucr.setValue(referrable);
              }
            }
          }
          for (String pathFragment : pathFragmentEcucRefsMap.keySet()) {
            String ecuCRefUri =
                pathFragment.substring(pathFragment.indexOf("<->") + 3, pathFragment.lastIndexOf("<->"));
            String ecuCConUri = pathFragment.substring(pathFragment.lastIndexOf("<->") + 3, pathFragment.length());
            Referrable ecucConObj = refList.get(ecuCConUri);
            Referrable ecucRefObj = refList.get(ecuCRefUri);
            String def = ecucRefsMap.get(ecuCRefUri);
            if ((ecucConObj != null) && (ecucRefObj != null) && (ecucConObj instanceof EcucContainerValue) &&
                (ecucRefObj instanceof EcucContainerValue) && (def != null)) {
              EcucContainerValue ecuContainerValue = (EcucContainerValue) ecucConObj;
              EcucContainerValue ecucRefValue = (EcucContainerValue) ecucRefObj;
              EList<EcucAbstractReferenceValue> referenceValues = ecuContainerValue.getReferenceValues();
              boolean isSet = false;
              EcucReferenceValue ecucRef1 = null;
              for (EcucAbstractReferenceValue ecucRef : referenceValues) {
                if ((ecucRef instanceof EcucReferenceValue) && (ecucRef.getDefinition() != null) &&
                    (ecucRef.getDefinition().getShortName().equals(def))) {
                  EcucReferenceValue ecucr = (EcucReferenceValue) ecucRef;
                  Referrable value = ecucr.getValue();
                  if ((value != null) && !value.eIsProxy() &&
                      value.getShortName().equals(ecucRefValue.getShortName())) {
                    isSet = true;
                    break;
                  }
                  else if ((value != null) && value.eIsProxy()) {
                    ecucRef1 = ecucr;
                  }
                }
              }
              if (!isSet && (ecucRef1 != null)) {
                ecucRef1.setValue(ecucRefValue);
              }
            }
          }
          for (Entry<EcucReferenceValue, String> entry : eRefProxyURIMap.entrySet()) {
            EcucReferenceValue key = entry.getKey();
            if (((key.getValue() != null) && key.getValue().eIsProxy()) || (key.getValue() == null)) {
              Referrable ecucConObj = refList.get(entry.getValue());
              if ((ecucConObj != null) && !ecucConObj.eIsProxy()) {
                key.setValue(ecucConObj);
              }
            }
          }
          for (String eventURI : rteEventRunnableEntityMap.keySet()) {
            List<Referrable> eventlist = refList1.get(eventURI);
            if (eventlist != null) {
              int pos = 0;
              for (Referrable r : eventlist) {
                RTEEvent re = (RTEEvent) r;
                if ((re.getStartOnEvent() == null) || re.getStartOnEvent().eIsProxy()) {
                  List<Referrable> runnablelist = refList1.get(rteEventRunnableEntityMap.get(eventURI));
                  if ((runnablelist != null) && (eventlist.size() == runnablelist.size())) {
                    re.setStartOnEvent((RunnableEntity) runnablelist.get(pos));
                  }
                }
                pos++;
              }
            }
          }
          for (String eventURI : bswEventModuleEntityMap.keySet()) {
            List<Referrable> eventlist = refList1.get(eventURI);
            if (eventlist != null) {
              int pos = 0;
              for (Referrable r : eventlist) {
                BswEvent re = (BswEvent) r;
                if ((re.getStartsOnEvent() == null) || re.getStartsOnEvent().eIsProxy()) {
                  List<Referrable> runnablelist = refList1.get(bswEventModuleEntityMap.get(eventURI));
                  if ((runnablelist != null) && (eventlist.size() == runnablelist.size())) {
                    re.setStartsOnEvent((BswModuleEntity) runnablelist.get(pos));
                  }
                }
                pos++;
              }
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
            e.printStackTrace();
          }
        }
      }
    };
    editingDomain.getCommandStack().execute(resolveEcucReferenceValue);
  }

  private static boolean isSplitable(final EList<EAnnotation> eAnnotations) {
    boolean isSplitable = false;
    if ((eAnnotations != null) && !eAnnotations.isEmpty()) {
      for (EAnnotation eAnnotation : eAnnotations) {
        String source = eAnnotation.getSource();
        if ((source != null) && "Stereotype".equals(source)) {
          String value = eAnnotation.getDetails().get(source);
          return (value != null) && "atpSplitable".equals(value);
        }
      }
    }
    return isSplitable;
  }

  private static List<EObject> getEcucReferenceValues(final String fragment, final Map<String, EObject> map,
      final Map<String, EObject> mergedURIEObjectsMap, final Map<String, Referrable> refList) {
    List<EObject> lists = new ArrayList<>();
    for (String pathFragment : map.keySet()) {
      String pathStr = pathFragment.substring(pathFragment.indexOf("<->") + 3, pathFragment.lastIndexOf("<->"));
      String ecucRefUri = pathStr.substring(0, pathStr.lastIndexOf("?type="));
      if (ecucRefUri.equals(fragment)) {
        EObject eObject = map.get(pathFragment);
        if (eObject.eIsProxy()) {
          ecucRefUri = pathFragment.substring(pathFragment.lastIndexOf("<->") + 3, pathFragment.length());
          EObject eo = mergedURIEObjectsMap.get(ecucRefUri);
          if ((eo != null) && (eo instanceof EcucContainerValue)) {
            if (eo.eIsProxy()) {
              URI uri = EcoreUtil.getURI(eo);
              eo = uri != null ? refList.get(uri.fragment()) : eo;
            }
            if ((eo != null) && !eo.eIsProxy()) {
              EcucContainerValue ecucv = (EcucContainerValue) eo;
              EcucAbstractReferenceValue erv = (EcucAbstractReferenceValue) eObject;
              EList<EcucAbstractReferenceValue> referenceValues = ecucv.getReferenceValues();
              loop: for (EcucAbstractReferenceValue refValue : referenceValues) {
                if ((refValue instanceof EcucReferenceValue) &&
                    refValue.getDefinition().getShortName().equals(erv.getDefinition().getShortName())) {
                  Referrable ref = ((EcucReferenceValue) refValue).getValue();
                  URI uri = EcoreUtil.getURI(ref);
                  String uriStr = uri != null ? uri.fragment() : "";
                  if (uriStr.isEmpty()) {
                    break loop;
                  }
                  if (uriStr.equals(pathStr) || uriStr.equals("//")) {
                    lists.add(refValue);
                    break loop;
                  }
                }
              }
            }
          }
        }
        else {
          lists.add(eObject);
        }
      }
    }
    return lists;
  }
}