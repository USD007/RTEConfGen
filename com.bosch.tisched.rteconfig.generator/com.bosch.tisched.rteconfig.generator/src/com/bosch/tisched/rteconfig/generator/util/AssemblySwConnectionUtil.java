/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */

// This Class is added as a workaround for resolving issues of merging of splittable contents in pver. This shall be removed once the merging part is fixed.
package com.bosch.tisched.rteconfig.generator.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.EList;

import autosar40.swcomponent.components.AtomicSwComponentType;
import autosar40.swcomponent.components.PortPrototype;
import autosar40.swcomponent.components.SwComponentType;
import autosar40.swcomponent.composition.AssemblySwConnector;
import autosar40.swcomponent.composition.CompositionSwComponentType;
import autosar40.swcomponent.composition.SwComponentPrototype;
import autosar40.swcomponent.composition.SwConnector;
import autosar40.swcomponent.composition.instancerefs.PPortInCompositionInstanceRef;
import autosar40.swcomponent.composition.instancerefs.RPortInCompositionInstanceRef;
import autosar40.swcomponent.swcinternalbehavior.RunnableEntity;
import autosar40.swcomponent.swcinternalbehavior.SwcInternalBehavior;
import autosar40.system.System;
import autosar40.system.SystemMapping;
import autosar40.system.fibex.fibexcore.coretopology.EcuInstance;
import autosar40.system.swmapping.SwcToEcuMapping;

/**
 * @author DTB1KOR
 */
public class AssemblySwConnectionUtil {

  private Map<String, String> aswcToSwcpMap;
  private Map<String, List<String>> rPortToPPortsMap;
  private Map<String, List<String>> pPortToRPortsMap;
  private final Map<String, PortPrototype> uriToPortMap;
  private final Map<String, List<String>> aswcToPortsMap;
  private final List<SwComponentPrototype> components;
  private final Map<String, String> runnableMap;


  /**
   *
   */
  public AssemblySwConnectionUtil() {
    this.aswcToSwcpMap = new HashMap<>();
    this.rPortToPPortsMap = new HashMap<>();
    this.pPortToRPortsMap = new HashMap<>();
    this.uriToPortMap = new HashMap<>();
    this.aswcToPortsMap = new HashMap<>();
    this.components = new ArrayList<>();
    this.runnableMap = new HashMap<>();

  }


  /**
   * @return the aswcToSwcpMap
   */
  public Map<String, String> getAswcToSwcpMap() {
    return this.aswcToSwcpMap;
  }


  /**
   * @param aswcToSwcpMap the aswcToSwcpMap to set
   */
  public void setAswcToSwcpMap(final Map<String, String> aswcToSwcpMap) {
    this.aswcToSwcpMap = aswcToSwcpMap;
  }


  /**
   * @return the rPortToPPortsMap
   */
  public Map<String, List<String>> getrPortToPPortsMap() {
    return this.rPortToPPortsMap;
  }


  /**
   * @param rPortToPPortsMap the rPortToPPortsMap to set
   */
  public void setrPortToPPortsMap(final Map<String, List<String>> rPortToPPortsMap) {
    this.rPortToPPortsMap = rPortToPPortsMap;
  }


  /**
   * @return the pPortToRPortsMap
   */
  public Map<String, List<String>> getpPortToRPortsMap() {
    return this.pPortToRPortsMap;
  }


  /**
   * @param pPortToRPortsMap the pPortToRPortsMap to set
   */
  public void setpPortToRPortsMap(final Map<String, List<String>> pPortToRPortsMap) {
    this.pPortToRPortsMap = pPortToRPortsMap;
  }


  /**
   * @return the uriToPortMap
   */
  public Map<String, PortPrototype> getUriToPortMap() {
    return this.uriToPortMap;
  }


  /**
   * @param components
   */
  public void setURIToPortMap(final EList<SwComponentPrototype> components) {

    components.stream().forEach(swcp -> swcp.getType().getPorts().stream()
        .forEach(p -> this.uriToPortMap.put(GenerateArxmlUtil.getFragmentURI(p), p)));
  }

  /**
   * @param iProject
   * @param excludeDir
   * @param ecuInstancePkgPath
   * @throws Exception
   */
  public void updatePRPortsMap(final IProject iProject, final String ecuInstancePkgPath, final String excludeDir)
      throws Exception {

    List<EcuInstance> ecuInstances =
        GenerateArxmlUtil.getListOfEObject(iProject, ecuInstancePkgPath, EcuInstance.class, excludeDir);
    if ((ecuInstances != null) && !ecuInstances.isEmpty()) {
      List<SwConnector> Connectors = getConnectors(ecuInstances.get(0), iProject, excludeDir);
      if ((Connectors != null) && !Connectors.isEmpty()) {
        for (SwConnector swc : Connectors) {
          if (swc instanceof AssemblySwConnector) {
            AssemblySwConnector aswc = (AssemblySwConnector) swc;
            if ((aswc.getRequester() != null) && (aswc.getRequester().getTargetRPort() != null) &&
                (aswc.getProvider() != null) && (aswc.getProvider().getTargetPPort() != null)) {
              RPortInCompositionInstanceRef requester = aswc.getRequester();
              PPortInCompositionInstanceRef provider = aswc.getProvider();
              SwComponentPrototype pswcp = provider.getContextComponent();
              SwComponentPrototype rswcp = requester.getContextComponent();
              if ((pswcp != null) && (rswcp != null) && !pswcp.eIsProxy() && !rswcp.eIsProxy()) {
                updateASWCToSWCPMap(aswc.getShortName(), provider.getTargetPPort(), requester.getTargetRPort(),
                    pswcp.getShortName(), rswcp.getShortName());
              }
            }
          }
        }
      }
    }
  }


  /**
   * @param aswcname
   * @param pPort
   * @param rPort
   * @param pswcname
   * @param rswcname
   */
  public void updateASWCToSWCPMap(final String aswcname, final PortPrototype pPort, final PortPrototype rPort,
      final String pswcname, final String rswcname) {

    String pKey = aswcname + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(pPort);
    String rKey = aswcname + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(rPort);
    this.aswcToSwcpMap.put(pKey, pswcname);
    this.aswcToSwcpMap.put(rKey, rswcname);

    String requesterKey = rswcname + rPort.getShortName();
    String providerKey = pswcname + pPort.getShortName();
    String requestervalue = rswcname + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(rPort);
    String providervalue = pswcname + "_PortPrototype_" + GenerateArxmlUtil.getFragmentURI(pPort);
    List<String> list = this.rPortToPPortsMap.get(requesterKey);
    if ((list != null) && (!list.contains(providervalue))) {
      list.add(providervalue);
    }
    else if (list == null) {
      List<String> pPorts = new ArrayList<>();
      pPorts.add(providervalue);
      this.rPortToPPortsMap.put(requesterKey, pPorts);
    }

    List<String> rPortlist = this.pPortToRPortsMap.get(providerKey);
    if ((rPortlist != null) && !rPortlist.contains(requestervalue)) {
      rPortlist.add(requestervalue);
    }
    else if (rPortlist == null) {
      List<String> rPorts = new ArrayList<>();
      rPorts.add(requestervalue);
      this.pPortToRPortsMap.put(providerKey, rPorts);
    }

    updateASWCToPortsMap(aswcname, pPort, rPort);
  }


  /**
   * @param ecuInstance EcuInstance
   * @param iProject IProject
   * @param excludeDir String
   * @param excludeDir
   * @return RootSwCompositionPrototype
   */
  private List<SwConnector> getConnectors(final EcuInstance ecuInstance, final IProject iProject,
      final String excludeDir)
      throws Exception {
    List<System> systems = GenerateArxmlUtil.getListOfEObject(iProject, System.class, excludeDir);
    List<SwConnector> connectors = new ArrayList();

    if ((systems != null) && !systems.isEmpty()) {

      Map<String, List<System>> systemMap = new HashMap<String, List<System>>();

      for (System system : systems) {

        List<System> list = systemMap.get(system.getShortName());
        if ((list != null) && (!list.contains(system))) {
          list.add(system);
        }
        else if (list == null) {
          List<System> systemlist = new ArrayList<>();
          systemlist.add(system);
          systemMap.put(system.getShortName(), systemlist);
        }
      }


      stopp: for (List<System> systemList : systemMap.values()) {
        for (System system : systemList) {


          if (system.getCategory().equals("ECU_EXTRACT")) {

            EList<SystemMapping> mappings = system.getMappings();
            for (SystemMapping systemMapping : mappings) {
              EList<SwcToEcuMapping> swMappings = systemMapping.getSwMappings();
              if (swMappings != null) {
                for (SwcToEcuMapping swcEcuMapping : swMappings) {
                  EcuInstance ecuInstance2 = swcEcuMapping.getEcuInstance();
                  if ((ecuInstance2 != null) && ecuInstance2.equals(ecuInstance)) {
                    if ((system.getRootSoftwareCompositions() != null) &&
                        !system.getRootSoftwareCompositions().isEmpty()) {

                      List<CompositionSwComponentType> swCompositions = GenerateArxmlUtil.getEObject(iProject,
                          GenerateArxmlUtil
                              .getFragmentURI(system.getRootSoftwareCompositions().get(0).getSoftwareComposition()),
                          CompositionSwComponentType.class);
                      if (swCompositions != null) {
                        for (CompositionSwComponentType cswct : swCompositions) {
                          if ((cswct != null) && (cswct.getConnectors() != null) && !cswct.getConnectors().isEmpty()) {
                            connectors.addAll(cswct.getConnectors());
                          }

                          if ((cswct != null) && (cswct.getComponents() != null) && !cswct.getComponents().isEmpty()) {
                            this.components.addAll(cswct.getComponents());
                          }
                        }
                      }
                    }
                    break stopp;
                  }
                }
              }
            }
          }
        }
      }
    }
    return connectors;
  }

  private void updateASWCToPortsMap(final String aswcname, final PortPrototype pPort, final PortPrototype rPort) {
    List<String> portList = new ArrayList<>();
    portList.add(GenerateArxmlUtil.getFragmentURI(pPort));
    portList.add(GenerateArxmlUtil.getFragmentURI(rPort));
    this.aswcToPortsMap.put(aswcname, portList);

  }

  /**
   * @return
   */
  public Map<String, List<String>> getASWCToPortsMap() {
    return this.aswcToPortsMap;
  }

  /**
   *
   */
  public void clearPRMaps() {
    this.rPortToPPortsMap.clear();
    this.pPortToRPortsMap.clear();
  }

  /**
   *
   */
  public void clearAll() {

    this.aswcToSwcpMap.clear();
    this.uriToPortMap.clear();
    this.aswcToPortsMap.clear();
    this.components.clear();
    this.runnableMap.clear();
  }


  /**
   *
   */
  public void updaterunnableToPortMap() {
    for (SwComponentPrototype swcp : this.components) {
      SwComponentType type = swcp.getType();

      if (type instanceof AtomicSwComponentType) {
        EList<SwcInternalBehavior> internalBehaviors = ((AtomicSwComponentType) type).getInternalBehaviors();
        internalBehaviors.stream()
            .forEach(swcb -> swcb.getRunnables().stream().forEach(runnable -> updateRunnableMaps(type, runnable)));
      }
    }
  }


  /**
   * @param type
   * @param runnable
   */
  private void updateRunnableMaps(final SwComponentType type, final RunnableEntity runnable) {

    runnable.getServerCallPoints().stream().forEach(scp -> {
      if (scp.getOperation() != null) {
        this.runnableMap.put(type.getShortName() + runnable.getShortName() + "<->" + scp.getShortName(),
            GenerateArxmlUtil.getFragmentURI(scp.getOperation().getContextRPort()));
      }
    });

    runnable.getDataSendPoints().stream().forEach(va -> {
      if ((va.getAccessedVariable() != null) && (va.getAccessedVariable().getAutosarVariable() != null)) {
        this.runnableMap.put(type.getShortName() + runnable.getShortName() + "<->" + va.getShortName(),
            GenerateArxmlUtil.getFragmentURI(va.getAccessedVariable().getAutosarVariable().getPortPrototype()));
      }
    });

    runnable.getDataWriteAccess().stream().forEach(va -> {
      if ((va.getAccessedVariable() != null) && (va.getAccessedVariable().getAutosarVariable() != null)) {
        this.runnableMap.put(type.getShortName() + runnable.getShortName() + "<->" + va.getShortName(),
            GenerateArxmlUtil.getFragmentURI(va.getAccessedVariable().getAutosarVariable().getPortPrototype()));
      }
    });

    runnable.getModeSwitchPoints().stream().forEach(msp -> {
      if ((msp.getModeGroup() != null) && (msp.getModeGroup().getContextPPort() != null)) {
        this.runnableMap.put(type.getShortName() + runnable.getShortName() + "<->" + msp.getShortName(),
            GenerateArxmlUtil.getFragmentURI(msp.getModeGroup().getContextPPort()));
      }

    });

  }

  /**
   * @return the serverCallPointMap
   */
  public Map<String, String> getrunnableMap() {
    return this.runnableMap;
  }


}
