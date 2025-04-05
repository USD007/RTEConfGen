/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.core;

import org.eclipse.emf.ecore.EObject;

/**
 * @author shk1cob
 */
public class TischedEvent {

  private String shortName;

  private String packagePath;

  private TischedTask tischedTask;

  private int position;

  private EventEnum eventTypeEnum;

  private RteEventEnum rteEventEnum = RteEventEnum.NONE;

  private BswEventEnum bswEventEnum = BswEventEnum.NONE;

  private EObject event;

  private boolean isCoOperative;

  private String offset;

  private EObject instance;


  /**
   * @return the instance
   */
  public EObject getInstance() {
    return this.instance;
  }


  /**
   * @param instance the instance to set
   */
  public void setInstance(final EObject instance) {
    this.instance = instance;
  }


  /**
   * @return the offset
   */
  public String getOffset() {
    return this.offset;
  }


  /**
   * @param offset the offset to set
   */
  public void setOffset(final String offset) {
    this.offset = offset;
  }


  /**
   * @param rteEventEnum the rteEventEnum to set
   */
  public void setRteEventEnum(final RteEventEnum rteEventEnum) {
    this.rteEventEnum = rteEventEnum;
  }


  /**
   * @param bswEventEnum the bswEventEnum to set
   */
  public void setBswEventEnum(final BswEventEnum bswEventEnum) {
    this.bswEventEnum = bswEventEnum;
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
   * @return the tischedTask
   */
  public TischedTask getTischedTask() {
    return this.tischedTask;
  }


  /**
   * @param tischedTask the tischedTask to set
   */
  public void setTischedTask(final TischedTask tischedTask) {
    this.tischedTask = tischedTask;
  }


  /**
   * @return the poition
   */
  public int getPosition() {
    return this.position;
  }


  /**
   * @param poition the poition to set
   */
  public void setPosition(final int poition) {
    this.position = poition;
  }


  /**
   * @return the eventTypeEnum
   */
  public EventEnum getEventTypeEnum() {
    return this.eventTypeEnum;
  }


  /**
   * @param eventTypeEnum the eventTypeEnum to set
   */
  public void setEventTypeEnum(final EventEnum eventTypeEnum) {
    this.eventTypeEnum = eventTypeEnum;
  }


  /**
   * @return the rteEventEnum
   */
  public RteEventEnum getRteEventEnum() {
    return this.rteEventEnum;
  }


  /**
   * @return the bswEventEnum
   */
  public BswEventEnum getBswEventEnum() {
    return this.bswEventEnum;
  }


  /**
   * @return the event
   */
  public EObject getEvent() {
    return this.event;
  }


  /**
   * @param event the event to set
   */
  public void setEvent(final EObject event) {
    this.event = event;
  }

  /**
   * @return the isCoOperative
   */
  public boolean isCoOperative() {
    return this.isCoOperative;
  }


  /**
   * @param isCoOperative the isCoOperative to set
   */
  public void setCoOperative(final boolean isCoOperative) {
    this.isCoOperative = isCoOperative;
  }


}
