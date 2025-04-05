/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.core;

import org.eclipse.emf.ecore.EObject;

/**
 * @author shk1cob
 */
public class TischedComponentInstance {

  private String shortName;

  private String packagePath;

  private TischedComponent tischedComponent;

  private SwComponentInstanceTypeEnum swComponentInstanceTypeEnum;

  private EObject componentInstance;

  private boolean generateMemmapHeader = false;


  /**
   * @return the generateMemmapHeader
   */
  public boolean isGenerateMemmapHeader() {
    return this.generateMemmapHeader;
  }


  /**
   * @param generateMemmapHeader the generateMemmapHeader to set
   */
  public void setGenerateMemmapHeader(final boolean generateMemmapHeader) {
    this.generateMemmapHeader = generateMemmapHeader;
  }


  /**
   * @return the shortName
   */
  public String getShortName() {
    return this.shortName;
  }


  /**
   * @param shortName the shortName to set
   */
  public void setShortName(final String shortName) {
    this.shortName = shortName;
  }


  /**
   * @return the packagePath
   */
  public String getPackagePath() {
    return this.packagePath;
  }


  /**
   * @param packagePath the packagePath to set
   */
  public void setPackagePath(final String packagePath) {
    this.packagePath = packagePath;
  }


  /**
   * @return the swComponentInstanceTypeEnum
   */
  public SwComponentInstanceTypeEnum getSwComponentInstanceTypeEnum() {
    return this.swComponentInstanceTypeEnum;
  }


  /**
   * @param swComponentInstanceTypeEnum the swComponentInstanceTypeEnum to set
   */
  public void setSwComponentInstanceTypeEnum(final SwComponentInstanceTypeEnum swComponentInstanceTypeEnum) {
    this.swComponentInstanceTypeEnum = swComponentInstanceTypeEnum;
  }


  /**
   * @return the tischedComponent
   */
  public TischedComponent getTischedComponent() {
    return this.tischedComponent;
  }


  /**
   * @param tischedComponent the tischedComponent to set
   */
  public void setTischedComponent(final TischedComponent tischedComponent) {
    this.tischedComponent = tischedComponent;
  }


  /**
   * @return the componentInstance
   */
  public EObject getComponentInstance() {
    return this.componentInstance;
  }


  /**
   * @param componentInstance the componentInstance to set
   */
  public void setComponentInstance(final EObject componentInstance) {
    this.componentInstance = componentInstance;
  }


}
