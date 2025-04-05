/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.ecore.util.FeatureMapUtil;

import autosar40.commonstructure.systemconstant.SwSystemconst;
import autosar40.genericstructure.varianthandling.BindingTimeEnum;
import autosar40.genericstructure.varianthandling.ConditionByFormula;
import autosar40.genericstructure.varianthandling.VarianthandlingFactory;
import autosar40.genericstructure.varianthandling.VariationPoint;

/**
 * @author SHK1COB
 */
public class VariationPointUtil {


  private static VariationPointUtil instance = null;

  private Map<String, List<EObject>> uriEObjectsMap = new HashedMap();

  private VariationPointUtil() {
    //
  }

  public static VariationPointUtil getInstance() {
    if (instance == null) {
      instance = new VariationPointUtil();
    }
    return instance;
  }


  /**
   * @param uriEObjectsMap final Map<String, List<EObject>>
   */
  public void setUriEObjectsMap(final Map<String, List<EObject>> uriEObjectsMap) {
    this.uriEObjectsMap = uriEObjectsMap;
  }


  private VariationPoint getVariationPoint(final EObject eObject) {
    VariationPoint vp = null;

    if (eObject != null) {
      EStructuralFeature eStructuralFeature = eObject.eClass().getEStructuralFeature("variationPoint");

      if (eStructuralFeature != null) {
        return (VariationPoint) eObject.eGet(eStructuralFeature);

      }
    }
    return vp;
  }

  private VariationPoint createVariationPointFromMultipleVPs(final List<VariationPoint> variationPoints) {
    VariationPoint newVp = null;

    if ((variationPoints != null) && !variationPoints.isEmpty()) {
      newVp = VarianthandlingFactory.eINSTANCE.createVariationPoint();
      ConditionByFormula createConditionByFormula = VarianthandlingFactory.eINSTANCE.createConditionByFormula();


      if (variationPoints.size() == 1) {

        if (!variationPoints.get(0).eIsProxy()) {
          addVariationPoint(variationPoints.get(0), createConditionByFormula.getMixed());

          if (!createConditionByFormula.getMixed().isEmpty()) {
            BindingTimeEnum bindingTimeEnum = getBindingTimeEnum(variationPoints);
            if (bindingTimeEnum != null) {
              createConditionByFormula.setBindingTime(bindingTimeEnum);
            }
            newVp.setSwSyscond(createConditionByFormula);
            // newVp.setShortLabel(variationPoints.get(0).getShortLabel());
          }
        }
      }
      else {

        String shortLabel = "";

        for (VariationPoint vp : variationPoints) {
          if (!vp.eIsProxy()) {
            createConditionByFormula.getMixed().add(FeatureMapUtil.createTextEntry("("));
            addVariationPoint(vp, createConditionByFormula.getMixed());
            createConditionByFormula.getMixed().add(FeatureMapUtil.createTextEntry(")"));
            createConditionByFormula.getMixed().add(FeatureMapUtil.createTextEntry(" && "));
            // shortLabel = (shortLabel.isEmpty() ? shortLabel : shortLabel + "_") + vp.getShortLabel();
          }
        }

        if (!createConditionByFormula.getMixed().isEmpty()) {
          createConditionByFormula.getMixed().remove(createConditionByFormula.getMixed().size() - 1);

          BindingTimeEnum bindingTimeEnum = getBindingTimeEnum(variationPoints);
          if (bindingTimeEnum != null) {
            createConditionByFormula.setBindingTime(bindingTimeEnum);
          }

          newVp.setSwSyscond(createConditionByFormula);
        }
      }
    }
    return newVp;
  }

  private BindingTimeEnum getBindingTimeEnum(final List<VariationPoint> variationPoints) {

    BindingTimeEnum bindingTimeEnum = null;

    List<String> listOfBindingTimes = new ArrayList<String>();

    if ((variationPoints != null) && !variationPoints.isEmpty()) {
      for (VariationPoint vp : variationPoints) {
        if (!vp.eIsProxy() && (vp.getSwSyscond() != null)) {
          ConditionByFormula swSyscond = vp.getSwSyscond();
          listOfBindingTimes.add(swSyscond.getBindingTime().getLiteral());
        }
      }

      if (listOfBindingTimes.contains(BindingTimeEnum.LINK_TIME.getLiteral())) {
        return BindingTimeEnum.LINK_TIME;
      }
      else if (listOfBindingTimes.contains(BindingTimeEnum.PRE_COMPILE_TIME.getLiteral())) {
        return BindingTimeEnum.PRE_COMPILE_TIME;
      }
      else if (listOfBindingTimes.contains(BindingTimeEnum.CODE_GENERATION_TIME.getLiteral())) {
        return BindingTimeEnum.CODE_GENERATION_TIME;
      }
      else if (listOfBindingTimes.contains(BindingTimeEnum.SYSTEM_DESIGN_TIME.getLiteral())) {
        return BindingTimeEnum.SYSTEM_DESIGN_TIME;
      }
    }
    return bindingTimeEnum;

  }

  private void addVariationPoint(final VariationPoint variationPoint, final FeatureMap featureMap) {
    if ((variationPoint != null) && (featureMap != null)) {
      ConditionByFormula swSyscond = variationPoint.getSwSyscond();
      if (swSyscond != null) {
        FeatureMap mixed = swSyscond.getMixed();
        Iterator<Entry> iterator = mixed.iterator();
        while (iterator.hasNext()) {
          Entry next = iterator.next();
          if (!next.getEStructuralFeature().getName().equals("comment")) {
            if (next.getValue() instanceof SwSystemconst) {
              SwSystemconst swsisCon = (SwSystemconst) next.getValue();
              if (!swsisCon.eIsProxy()) {
                featureMap.add(FeatureMapUtil.createEntry(
                    autosar40.genericstructure.varianthandling.VarianthandlingPackage.Literals.SW_SYSTEMCONST_DEPENDENT_FORMULA__SYSCS,
                    swsisCon));
              }
            }
            else {
              if (next.getValue() != null) {
                featureMap.add(FeatureMapUtil.createTextEntry((String) next.getValue()));
              }
            }
          }
        }
      }
    }
  }

  private void getVariationPointFromARObject(final List<VariationPoint> vpList, final Map<String, EObject> eObjURIMap,
      final String fragment) {
    if ((fragment != null) && !fragment.isEmpty()) {
      EObject eObject = eObjURIMap.get(fragment);
      if ((eObject != null) && !eObject.eIsProxy()) {
        VariationPoint variationPoint = getVariationPoint(eObject);
        if ((variationPoint != null) && !variationPoint.eIsProxy()) {
          vpList.add(variationPoint);
          String subStr = fragment.substring(0, fragment.lastIndexOf("/"));
          if ((subStr != null) && !subStr.isEmpty() && (subStr.length() > 1)) {
            getVariationPointFromARObject(vpList, eObjURIMap, subStr);
          }
        }
      }
    }
  }

  private void getVariationPointFromARObjectURIMap(final List<VariationPoint> vpList, final String fragment,
      final Resource resource) {
    if ((fragment != null) && !fragment.isEmpty()) {
      List<EObject> eObjects = this.uriEObjectsMap.get(fragment);
      if (eObjects != null) {
        for (EObject eObject : eObjects) {
          if ((eObject != null) && !eObject.eIsProxy() && eObject.eResource().equals(resource)) {
            VariationPoint variationPoint = getVariationPoint(eObject);
            if ((variationPoint != null) && !variationPoint.eIsProxy()) {
              vpList.add(variationPoint);
              String subStr = fragment.substring(0, fragment.lastIndexOf("/"));
              if ((subStr != null) && !subStr.isEmpty() && (subStr.length() > 1)) {
                getVariationPointFromARObjectURIMap(vpList, subStr, resource);
              }
            }
          }
        }
      }
    }
  }


  private List<VariationPoint> getVariationPointFromARObject(final EObject arObject) {
    List<VariationPoint> vpList = new ArrayList<>();
    if ((arObject != null) && !arObject.eIsProxy()) {
      String fragment = EcoreUtil.getURI(arObject).fragment();
      fragment = fragment.substring(0, fragment.lastIndexOf("?type="));
      getVariationPointFromARObjectURIMap(vpList, fragment, arObject.eResource());
    }
    return vpList;
  }


  public VariationPoint getVariationPoint(final EObject... eObjects) {

    VariationPoint vp = null;
    List<VariationPoint> vpList = new ArrayList<>();
    if (eObjects != null) {
      for (EObject eObj : eObjects) {
        List<VariationPoint> variationPointFromARObject =
            VariationPointUtil.getInstance().getVariationPointFromARObject(eObj);

        if (!variationPointFromARObject.isEmpty()) {
          vpList.addAll(variationPointFromARObject);
        }
      }
    }

    if (!vpList.isEmpty()) {
      vp = VariationPointUtil.getInstance().createVariationPointFromMultipleVPs(vpList);
    }
    return vp;
  }


}
