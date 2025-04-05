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
public class TischedTask {

  private String shortName;

  private String packagePath;

  private TaskActivationEnum taskActivationEnum;

  private TischedTaskEventTypeEnum tischedTaskEventTypeEnum;

  private String mappedOsApplication;

  private String ecuCPartition;

  private String taskCycle;

  private EObject taskChain;

  private List<TischedEvent> eventList;

  private boolean osrtetaskhooks;

  private String osPPortPrototype;

  private String osBSWReleasedTrigger;

  private TischedTaskTypeEnum tischedTaskTypeEnum;


  /**
   * @return the tischedTaskTypeEnum
   */
  public TischedTaskTypeEnum getTischedTaskTypeEnum() {
    return this.tischedTaskTypeEnum;
  }


  /**
   * @param tischedTaskTypeEnum the tischedTaskTypeEnum to set
   */
  public void setTischedTaskTypeEnum(final TischedTaskTypeEnum tischedTaskTypeEnum) {
    this.tischedTaskTypeEnum = tischedTaskTypeEnum;
  }


  /**
   * @return the osPPortPrototype
   */
  public String getOsPPortPrototype() {
    return this.osPPortPrototype;
  }


  /**
   * @param osPPortPrototype the osPPortPrototype to set
   */
  public void setOsPPortPrototype(final String osPPortPrototype) {
    this.osPPortPrototype = osPPortPrototype;
  }


  /**
   * @return the osBSWReleasedTrigger
   */
  public String getOsBSWReleasedTrigger() {
    return this.osBSWReleasedTrigger;
  }


  /**
   * @param osBSWReleasedTrigger the osBSWReleasedTrigger to set
   */
  public void setOsBSWReleasedTrigger(final String osBSWReleasedTrigger) {
    this.osBSWReleasedTrigger = osBSWReleasedTrigger;
  }


  /**
   * @return the ostaskhooks
   */
  public boolean isOsRtetaskhooks() {
    return this.osrtetaskhooks;
  }


  /**
   * @param osrtetaskhooks the ostaskhooks to set
   */
  public void setOsRtetaskhooks(final boolean osrtetaskhooks) {
    this.osrtetaskhooks = osrtetaskhooks;
  }

  /**
   * @return
   */
  public List<TischedEvent> getEvents() {
    if (this.eventList == null) {
      this.eventList = new ArrayList<>();
    }
    return this.eventList;

  }

  /**
   * @return the taskCycle
   */
  public String getTaskCycle() {
    return this.taskCycle;
  }


  /**
   * @param taskCycle the taskCycle to set
   */
  public void setTaskCycle(final String taskCycle) {
    this.taskCycle = taskCycle;
  }


  private TaskPreemptiveEnum taskPreemptiveEnum;

  private EObject taskInstance;

  private EObject alarmInstance;

  private EObject osAppInstance;

  private EObject osScheduleTableInstance;


  /**
   * @return the osScheduleTableInstance
   */
  public EObject getOsScheduleTableInstance() {
    return this.osScheduleTableInstance;
  }


  /**
   * @param osScheduleTableInstance the osScheduleTableInstance to set
   */
  public void setOsScheduleTableInstance(final EObject osScheduleTableInstance) {
    this.osScheduleTableInstance = osScheduleTableInstance;
  }


  /**
   * @return the osAppInstance
   */
  public EObject getOsAppInstance() {
    return this.osAppInstance;
  }


  /**
   * @param osAppInstance the osAppInstance to set
   */
  public void setOsAppInstance(final EObject osAppInstance) {
    this.osAppInstance = osAppInstance;
  }


  /**
   * @return the alarmInstance
   */
  public EObject getAlarmInstance() {
    return this.alarmInstance;
  }


  /**
   * @param alarmInstance the alarmInstance to set
   */
  public void setAlarmInstance(final EObject alarmInstance) {
    this.alarmInstance = alarmInstance;
  }


  /**
   * @return the taskInstance
   */
  public EObject getTaskInstance() {
    return this.taskInstance;
  }


  /**
   * @param taskInstance the taskInstance to set
   */
  public void setTaskInstance(final EObject taskInstance) {
    this.taskInstance = taskInstance;
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
   * @return the taskActivationEnum
   */
  public TaskActivationEnum getTaskActivationEnum() {
    return this.taskActivationEnum;
  }


  /**
   * @return the tischedTaskeventtypeenum
   */
  public TischedTaskEventTypeEnum getTischedTaskeventtypeenum() {
    return this.tischedTaskEventTypeEnum;
  }


  /**
   * @param tischedTaskeventtypeenum the tischedTaskeventtypeenum to set
   */
  public void setTischedTaskeventtypeenum(final TischedTaskEventTypeEnum tischedTaskeventtypeenum) {
    this.tischedTaskEventTypeEnum = tischedTaskeventtypeenum;
  }


  /**
   * @param taskActivationEnum the taskActivationEnum to set
   */
  public void setTaskActivationEnum(final TaskActivationEnum taskActivationEnum) {
    this.taskActivationEnum = taskActivationEnum;
  }


  /**
   * @return the mappedOsApplication
   */
  public String getMappedOsApplication() {
    return this.mappedOsApplication;
  }


  /**
   * @param mappedOsApplication the mappedOsApplication to set
   */
  public void setMappedOsApplication(final String mappedOsApplication) {
    this.mappedOsApplication = mappedOsApplication;
  }


  /**
   * @return the ecuCPartition
   */
  public String getEcuCPartition() {
    return this.ecuCPartition;
  }


  /**
   * @param ecuCPartition the ecuCPartition to set
   */
  public void setEcuCPartition(final String ecuCPartition) {
    this.ecuCPartition = ecuCPartition;
  }


  /**
   * @return the taskPreemptiveEnum
   */
  public TaskPreemptiveEnum getTaskPreemptiveEnum() {
    return this.taskPreemptiveEnum;
  }


  /**
   * @param taskPreemptiveEnum the taskPreemptiveEnum to set
   */
  public void setTaskPreemptiveEnum(final TaskPreemptiveEnum taskPreemptiveEnum) {
    this.taskPreemptiveEnum = taskPreemptiveEnum;
  }

  /**
   * @return the taskChain
   */
  public EObject getTaskChain() {
    return this.taskChain;
  }

  /**
   * @param taskChain the taskChain to set
   */
  public void setTaskChain(final EObject taskChain) {
    this.taskChain = taskChain;
  }


}
