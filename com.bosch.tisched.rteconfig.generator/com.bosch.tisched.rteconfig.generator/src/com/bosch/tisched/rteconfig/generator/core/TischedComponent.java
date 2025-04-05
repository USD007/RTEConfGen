/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

/**
 * @author shk1cob
 */
public class TischedComponent {


  private String shortName;

  private String packagePath;

  private List<TischedEvent> listOfTischedEvents;

  private SwComponentTypeEnum swComponentTypeEnum;

  private EObject component;

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
   * @return the swComponentTypeEnum
   */
  public SwComponentTypeEnum getSwComponentTypeEnum() {
    return this.swComponentTypeEnum;
  }


  /**
   * @param swComponentTypeEnum the swComponentTypeEnum to set
   */
  public void setSwComponentTypeEnum(final SwComponentTypeEnum swComponentTypeEnum) {
    this.swComponentTypeEnum = swComponentTypeEnum;
  }


  /**
   * @return the listOfTischedEvents
   */
  public List<TischedEvent> getListOfTischedEvents() {

    if (this.listOfTischedEvents == null) {
      this.listOfTischedEvents = new ArrayList<TischedEvent>();
    }
    return this.listOfTischedEvents;
  }


  /**
   * @return the component
   */
  public EObject getComponent() {
    return this.component;
  }


  /**
   * @param component the component to set
   */
  public void setComponent(final EObject component) {
    this.component = component;
  }


}
