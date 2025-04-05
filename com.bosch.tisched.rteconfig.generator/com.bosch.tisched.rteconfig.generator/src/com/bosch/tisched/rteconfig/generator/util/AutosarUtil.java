/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;

/**
 * @author shk1cob
 */
public class AutosarUtil {


  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(AutosarUtil.class.getName());
  private static String currentEObject = "";

  /**
   * @param releaseVersion String
   * @return IMetaModelDescriptor
   */
  public static IMetaModelDescriptor getMetaModelDescriptorByAutosarReleaseVersion(final String releaseVersion) {
    return MetaModelDescriptorRegistry.INSTANCE.getDescriptor("org.artop.aal.autosar40");
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

  public static String getFormattedMessage(final Exception ex) {

    StringBuilder message = new StringBuilder();
    String property = System.getProperty("line.separator");

    for (StackTraceElement st : ex.getStackTrace()) {
      message.append(st.toString());
      message.append(property);
    }
    return message.toString();
  }


  /**
   * @param eObject
   * @param state
   * @param eobject
   */
  public static void setCurrentProcessingEObject(final EObject eObject) {
    if (eObject != null) {
      String eobjectShortName = GenerateArxmlUtil.getShortName(eObject);
      URI fulluri = EcoreUtil.getURI(eObject);
      if ((fulluri != null) && (fulluri.path() != null) && (fulluri.fragment() != null)) {
        String path = fulluri.path();
        String uri = fulluri.fragment();
        currentEObject =
            "Current processing AUTOSAR element Name : " + eobjectShortName + ", URI : " + uri + ", Path : " + path;
      }
      else {
        currentEObject = "Current processing AUTOSAR element Name : " + eobjectShortName;
      }
    }
    else {
      currentEObject = "Current processing AUTOSAR element Name : NONE ";
    }

  }

  /**
   * @return String
   */
  public static String getCurrentProcessingEObject() {
    return currentEObject;
  }

}
