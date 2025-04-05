package com.bosch.tisched.rteconfig.generator.service;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.emf.ecore.proxymanagement.AbstractProxyResolverService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

import autosar40.genericstructure.generaltemplateclasses.identifiable.Referrable;

/**
 * @author SHK1COB
 */
public class AutosarProxyResolver extends AbstractProxyResolverService {

  private static StringBuilder proxyURIs = new StringBuilder();

  private static boolean bufferProxyURIs = false;

  private static Map<String, Referrable> uriEObjMap = new HashMap<>();


  /**
   * @return the uriEObjMap
   */
  public static Map<String, Referrable> getUriEObjMap() {
    return uriEObjMap;
  }


  /**
   * @param uriEObjMap the uriEObjMap to set
   */
  public static void setUriEObjectMap(final Map<String, Referrable> uriEObjMap) {
    AutosarProxyResolver.uriEObjMap = uriEObjMap;
  }

  /**
   * @param mmDescriptors bufferProxyURIs
   */
  public AutosarProxyResolver(final Collection<IMetaModelDescriptor> mmDescriptors) {
    super(mmDescriptors);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void initProxyResolvers() {
    //

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EObject getEObject(final EObject proxy, final EObject contextObject, final boolean loadOnDemand) {

    if (!uriEObjMap.isEmpty()) {
      String fragment = EcoreUtil.getURI(proxy).fragment();
      Referrable referrable = uriEObjMap.get(fragment);
      if ((referrable != null) && !referrable.eIsProxy()) {
        return referrable;
      }
    }

    EObject resolve = EcoreUtil.resolve(proxy, contextObject);

    if (AutosarProxyResolver.bufferProxyURIs && (resolve != null) && resolve.eIsProxy()) {
      int indexOf = AutosarProxyResolver.proxyURIs.indexOf(((InternalEObject) proxy).eProxyURI().toString());
      if (indexOf < 0) {
        AutosarProxyResolver.proxyURIs.append(((InternalEObject) proxy).eProxyURI().toString());
        AutosarProxyResolver.proxyURIs.append("<->");
      }

    }
    return resolve;
  }

  /**
   * @param enableBuffer boolean
   */
  public static void enableBuffering(final boolean enableBuffer) {
    AutosarProxyResolver.bufferProxyURIs = enableBuffer;
    if (!AutosarProxyResolver.bufferProxyURIs) {
      AutosarProxyResolver.proxyURIs = new StringBuilder();
    }

  }

  /**
   * @return StringBuilder
   */
  public static StringBuilder getURIBuffer() {
    return AutosarProxyResolver.proxyURIs;
  }

  /**
   *
   */
  private void canResolve() {
    // TODO Auto-generated method stub

  }

}
