/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.core;

/**
 * @author shk1cob
 */
public class TischedToEcuCValueMapping {
  //
  //
  // private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(TischedToEcuCValueMapping.class.getName());
  //
  // private final Root root;
  //
  // private final IProject iProject;
  //
  // private List<TischedComponent> tischedComponents;
  // private Map<String, TischedComponentInstance> tischedComponentInstances;
  // private List<TischedEvent> tischedEvents;
  // private List<TischedTask> tischedTasks;
  //
  // private List<SwComponentType> listOfComponentTypes;
  //
  // private List<SwComponentPrototype> listOfSWComponentProTypes;
  //
  // private List<EcucContainerValue> listOfOsTasks;
  //
  // private EcucBooleanParamDef osTaskCanCallSchedule;
  //
  //
  // /**
  // * @return the listOfOsTasks
  // */
  // public List<EcucContainerValue> getListOfOsTasks() {
  // if (this.listOfOsTasks == null) {
  // this.listOfOsTasks = new ArrayList<EcucContainerValue>();
  // }
  // return this.listOfOsTasks;
  // }
  //
  // private List<BswModuleDescription> listOfBswmd;
  //
  // private List<BswImplementation> listOfbswi;
  //
  // private final String osTaskarPkgPath;
  //
  // private final String outputPath;
  //
  //
  // /**
  // * @param iProject
  // * @param root
  // */
  // public TischedToEcuCValueMapping(final IProject iProject, final Root root, final String osTaskarPkgPath,
  // final String outputPath) {
  // this.root = root;
  // this.iProject = iProject;
  // this.osTaskarPkgPath = osTaskarPkgPath;
  // this.outputPath = outputPath;
  // }
  //
  // private void init() {
  // // this.listOfComponentTypes = GenerateArxmlUtil.getListOfEObject(this.iProject, SwComponentType.class);
  // this.listOfSWComponentProTypes =
  // GenerateArxmlUtil.getListOfEObject(this.iProject, SwComponentPrototype.class, this.outputPath);
  // this.listOfBswmd = GenerateArxmlUtil.getListOfEObject(this.iProject, BswModuleDescription.class, this.outputPath);
  // this.listOfbswi = GenerateArxmlUtil.getListOfEObject(this.iProject, BswImplementation.class, this.outputPath);
  // updateListOfOsTasksToList(
  // GenerateArxmlUtil.getListOfEObject(this.iProject, EcucContainerValue.class, this.outputPath));
  // EcucBooleanParamDef eObject = GenerateArxmlUtil.getEObject(this.iProject,
  // this.osTaskarPkgPath + "/OsTaskCanCallSchedule", EcucBooleanParamDef.class, this.outputPath);
  // if (eObject != null) {
  // this.osTaskCanCallSchedule = eObject;
  // }
  // }
  //
  // private void clear() {
  // if (this.listOfComponentTypes != null) {
  // this.listOfComponentTypes.clear();
  // }
  // if (this.listOfSWComponentProTypes != null) {
  // this.listOfSWComponentProTypes.clear();
  // }
  // if (this.listOfBswmd != null) {
  // this.listOfBswmd.clear();
  // }
  // if (this.listOfbswi != null) {
  // this.listOfbswi.clear();
  // }
  // }
  //
  // /**
  // * @return the tischedTasks
  // */
  // public List<TischedTask> getTischedTasks() {
  // if (this.tischedTasks == null) {
  // this.tischedTasks = new ArrayList<TischedTask>();
  // }
  // return this.tischedTasks;
  // }
  //
  // /**
  // * @return the tischedEvents
  // */
  // public List<TischedEvent> getTischedEvents() {
  //
  // if (this.tischedEvents == null) {
  // this.tischedEvents = new ArrayList<TischedEvent>();
  // }
  // return this.tischedEvents;
  // }
  //
  // /**
  // * @return the tischedComponentInstances
  // */
  // public Map<String, TischedComponentInstance> getTischedComponentInstances() {
  // if (this.tischedComponentInstances == null) {
  // this.tischedComponentInstances = new HashMap<String, TischedComponentInstance>();
  // }
  // return this.tischedComponentInstances;
  // }
  //
  // /**
  // * @return the tischedComponents
  // */
  // public List<TischedComponent> getTischedComponents() {
  // if (this.tischedComponents == null) {
  // this.tischedComponents = new ArrayList<TischedComponent>();
  // }
  // return this.tischedComponents;
  // }
  //
  // /**
  // *
  // */
  // public void doMapping() {
  //
  // LOGGER.info("*** Mapping tisched elements to rte config elements");
  //
  // init();
  // try {
  //
  // for (Block block : this.root.getBlocks()) {
  //
  // if (block instanceof Component) {
  // doComponentMapping((Component) block);
  // }
  // }
  //
  // for (Block block : this.root.getBlocks()) {
  //
  // if (block instanceof Task) {
  // doTaskMapping((Task) block);
  // }
  // }
  // }
  // finally {
  // clear();
  // }
  // LOGGER.info("*** Mapping tisched elements to rte config elements completed");
  //
  // }
  //
  // /**
  // * @param block Task
  // */
  // private void doTaskMapping(final Task task) {
  //
  //
  // EList<EObject> runnables = task.getRunnables();
  //
  // if (runnables != null) {
  //
  // int position = 1;
  //
  // for (EObject eObject : runnables) {
  //
  // if (eObject instanceof Event) {
  //
  // Event event = (Event) eObject;
  // String module = event.getModule();
  // String eventName = event.getEventName();
  // TischedEvent tischedEvent = new TischedEvent();
  // tischedEvent.setShortName(eventName);
  //
  // for (TischedComponentInstance tischedComponentInstance : getTischedComponentInstances().values()) {
  // if (tischedComponentInstance.getTischedComponent().getShortName().equals(module)) {
  //
  // if (tischedComponentInstance
  // .getSwComponentInstanceTypeEnum() == SwComponentInstanceTypeEnum.SW_COMPONENT_PROTOTYPE) {
  //
  // updateTischedInstanceForRteEvent(tischedEvent, eventName, tischedComponentInstance);
  // }
  // else if (tischedComponentInstance
  // .getSwComponentInstanceTypeEnum() == SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION) {
  // updateTischedInstanceForBswEvent(tischedEvent, eventName, tischedComponentInstance);
  // }
  // }
  // }
  //
  // tischedEvent.setPosition(position);
  // // updateTischedTask(task, tischedEvent);
  // getTischedEvents().add(tischedEvent);
  // }
  // position++;
  // }
  //
  // }
  //
  // }
  //
  // // private void updateTischedTask(final Task task, final TischedEvent tischedEvent) {
  // // for (EcucContainerValue ecucValue : getListOfOsTasks()) {
  // //
  // // if (ecucValue.getShortName().equals(task.getTaskName())) {
  // // TischedTask tischedTask = new TischedTask();
  // // tischedTask.setShortName(task.getTaskName());
  // // tischedTask.setTaskInstance(ecucValue);
  // // tischedEvent.setTischedTask(tischedTask);
  // // if (this.osTaskCanCallSchedule != null) {
  // // setIsCooperative(ecucValue, tischedTask);
  // // }
  // // }
  // // }
  // // }
  //
  // // private void setIsCooperative(final EcucContainerValue ecucValue, final TischedTask tischedTask) {
  // // EList<EcucParameterValue> parameterValues = ecucValue.getParameterValues();
  // //
  // // for (EcucParameterValue pValue : parameterValues) {
  // // if (pValue.getDefinition().equals(this.osTaskCanCallSchedule)) {
  // // if (pValue instanceof EcucNumericalParamValue) {
  // // EcucNumericalParamValue vp = (EcucNumericalParamValue) pValue;
  // // String value = vp.getValue().getMixedText();
  // //
  // // tischedTask.setCoOperative(Boolean.parseBoolean(value));
  // // break;
  // // }
  // // }
  // // }
  // // }
  //
  // private void updateTischedInstanceForRteEvent(final TischedEvent tischedEvent, final String eventName,
  // final TischedComponentInstance tischedComponentInstance) {
  //
  //
  // tischedEvent.setEventTypeEnum(EventEnum.RTEEVENT);
  //
  // SwComponentPrototype componentInstance = (SwComponentPrototype) tischedComponentInstance.getComponentInstance();
  //
  // if (componentInstance.getType() instanceof AtomicSwComponentType) {
  //
  // AtomicSwComponentType aswt = (AtomicSwComponentType) componentInstance.getType();
  //
  // for (SwcInternalBehavior swb : aswt.getInternalBehaviors()) {
  //
  // for (RTEEvent rteEvent : swb.getEvents()) {
  //
  // if (rteEvent.getShortName().equals(eventName)) {
  // tischedEvent.setEvent(rteEvent);
  // tischedComponentInstance.getTischedComponent().getListOfTischedEvents().add(tischedEvent);
  //
  // }
  // }
  // }
  // }
  //
  // }
  //
  // private void updateTischedInstanceForBswEvent(final TischedEvent tischedEvent, final String eventName,
  // final TischedComponentInstance tischedComponentInstance) {
  //
  //
  // tischedEvent.setEventTypeEnum(EventEnum.BSWEVENT);
  //
  // BswModuleDescription bswMd = (BswModuleDescription) tischedComponentInstance.getTischedComponent().getComponent();
  //
  //
  // for (BswInternalBehavior swb : bswMd.getInternalBehaviors()) {
  //
  // for (BswEvent bswEvent : swb.getEvents()) {
  // if (bswEvent.getShortName().equals(eventName)) {
  // tischedEvent.setEvent(bswEvent);
  // tischedComponentInstance.getTischedComponent().getListOfTischedEvents().add(tischedEvent);
  // // if(rteEvent ins) {
  // // tischedEvent.setRteEventEnum(RteEventEnum);
  // }
  // }
  // }
  //
  // }
  //
  // private void doComponentMapping(final Component component) {
  //
  //
  // TischedComponent tischedComponent = new TischedComponent();
  // tischedComponent.setShortName(component.getComponentname());
  // tischedComponent.setPackagePath(component.getPath());
  // SwComponentType swComponentType = getSwComponentType(component.getPath());
  //
  //
  // if (swComponentType != null) {
  // updateTischedComponentInstanceForASWComponent(tischedComponent, swComponentType, component);
  // }
  // else {
  // updateTischedComponentInstanceForBSWComponent(tischedComponent, component);
  // }
  //
  // if (tischedComponent.getSwComponentTypeEnum() != null) {
  // getTischedComponents().add(tischedComponent);
  // }
  //
  //
  // }
  //
  // private void updateTischedComponentInstanceForASWComponent(final TischedComponent tischedComponent,
  // final EObject swComponentType, final Component component) {
  //
  // tischedComponent.setComponent(swComponentType);
  // tischedComponent.setSwComponentTypeEnum(SwComponentTypeEnum.ASW);
  // // for (String name : component.getComponentInstanceNames()) {
  // // SwComponentPrototype swComponentPrototype = getSWComponentPrototype(name);
  // // if (swComponentPrototype != null) {
  // //
  // // TischedComponentInstance tischedComponentInstance = new TischedComponentInstance();
  // // tischedComponentInstance.setShortName(name);
  // // tischedComponentInstance.setPackagePath("");
  // // tischedComponentInstance.setComponentInstance(swComponentPrototype);
  // // tischedComponentInstance.setSwComponentInstanceTypeEnum(SwComponentInstanceTypeEnum.SW_COMPONENT_PROTOTYPE);
  // // tischedComponentInstance.setTischedComponent(tischedComponent);
  // // getTischedComponentInstances().add(tischedComponentInstance);
  // // }
  // // }
  //
  // for (SwComponentPrototype swComponentPrototype : this.listOfSWComponentProTypes) {
  //
  // if ((checkIfSwCompPrototypeExists(swComponentPrototype.getShortName(), component.getComponentInstanceNames())) ||
  // ((swComponentPrototype.getType() != null) && swComponentPrototype.getType().equals(swComponentType))) {
  // //
  // //
  // if (getTischedComponentInstances().get(swComponentPrototype.getShortName()) == null) {
  // TischedComponentInstance tischedComponentInstance = new TischedComponentInstance();
  // tischedComponentInstance.setShortName(swComponentPrototype.getShortName());
  // tischedComponentInstance.setPackagePath("");
  // tischedComponentInstance.setComponentInstance(swComponentPrototype);
  // tischedComponentInstance.setSwComponentInstanceTypeEnum(SwComponentInstanceTypeEnum.SW_COMPONENT_PROTOTYPE);
  // tischedComponentInstance.setTischedComponent(tischedComponent);
  // getTischedComponentInstances().put(swComponentPrototype.getShortName(), tischedComponentInstance);
  // }
  //
  //
  // }
  //
  // }
  //
  //
  // }
  //
  // private boolean checkIfSwCompPrototypeExists(final String shortName, final EList<String> swComponentProtypeNames) {
  //
  // boolean exists = false;
  // for (String name : swComponentProtypeNames) {
  // if (name.equals(shortName)) {
  // return true;
  // }
  //
  // }
  // return exists;
  //
  // }
  //
  // private void updateTischedComponentInstanceForBSWComponent(final TischedComponent tischedComponent,
  // final Component component) {
  //
  //
  // BswModuleDescription bswDesc = getBSWModuleDesc(component.getPath());
  // tischedComponent.setSwComponentTypeEnum(SwComponentTypeEnum.BSW);
  // if (bswDesc != null) {
  // tischedComponent.setComponent(bswDesc);
  //
  // for (BswImplementation bswImplementation : this.listOfbswi) {
  //
  //
  // if ((checkIfSwCompPrototypeExists(bswImplementation.getShortName(), component.getComponentInstanceNames())) ||
  // ((bswImplementation.getBehavior() != null) &&
  // bswImplementation.getBehavior().eContainer().equals(bswDesc))) {
  //
  // if (getTischedComponentInstances().get(bswImplementation.getShortName()) == null) {
  // TischedComponentInstance tischedComponentInstance = new TischedComponentInstance();
  // tischedComponentInstance.setShortName(bswImplementation.getShortName());
  // tischedComponentInstance.setPackagePath("");
  // tischedComponentInstance.setComponentInstance(bswImplementation);
  // tischedComponentInstance.setSwComponentInstanceTypeEnum(SwComponentInstanceTypeEnum.BSW_IMPLEMENTATION);
  // tischedComponentInstance.setTischedComponent(tischedComponent);
  // getTischedComponentInstances().put(bswImplementation.getShortName(), tischedComponentInstance);
  // }
  //
  // }
  //
  // }
  //
  // }
  //
  //
  // }
  //
  // //
  // // private SwComponentPrototype getSWComponentPrototype(final String name) {
  // // SwComponentPrototype swp = null;
  // // if (this.listOfSWComponentProTypes != null) {
  // // for (SwComponentPrototype swc : this.listOfSWComponentProTypes) {
  // // if (swc.getShortName().equals(name)) {
  // // return swc;
  // // }
  // // }
  // // }
  // // return swp;
  // //
  // // }
  // //
  // //
  // // private SwComponentPrototype getSWComponentPrototype(final SwComponentType type) {
  // // SwComponentPrototype swp = null;
  // // if (this.listOfSWComponentProTypes != null) {
  // // for (SwComponentPrototype swc : this.listOfSWComponentProTypes) {
  // // if (swc.getShortName().equals(name)) {
  // // return swc;
  // // }
  // // }
  // // }
  // // return swp;
  // //
  // // }
  // //
  // // private BswImplementation getBSWImplementation(final String name) {
  // // BswImplementation bsi = null;
  // // if (this.listOfbswi != null) {
  // // for (BswImplementation bsw : this.listOfbswi) {
  // // if (bsw.getShortName().equals(name)) {
  // // return bsw;
  // // }
  // // }
  // // }
  // // return bsi;
  // //
  // // }
  //
  //
  // private SwComponentType getSwComponentType(final String pkgPath) {
  // SwComponentType swt = null;
  // if (this.listOfSWComponentProTypes != null) {
  // for (SwComponentPrototype swc : this.listOfSWComponentProTypes) {
  // String path = EcoreResourceUtil.getURI(swc.getType()).fragment().split("\\?")[0];
  // if (path.equals(pkgPath)) {
  // return swc.getType();
  // }
  // }
  // }
  // return swt;
  //
  // }
  //
  // private BswModuleDescription getBSWModuleDesc(final String pkgPath) {
  // BswModuleDescription bswmd = null;
  // if (this.listOfBswmd != null) {
  // for (BswModuleDescription bsw : this.listOfBswmd) {
  // String path = EcoreResourceUtil.getURI(bsw).fragment().split("\\?")[0];
  // if (path.equals(pkgPath)) {
  // return bsw;
  // }
  // }
  // }
  // return bswmd;
  //
  // }
  //
  // private void updateListOfOsTasksToList(final List<EcucContainerValue> listOfEcucContainerValues) {
  //
  // EcucParamConfContainerDef eObject = GenerateArxmlUtil.getEObject(this.iProject, this.osTaskarPkgPath,
  // EcucParamConfContainerDef.class, this.outputPath);
  //
  // if (eObject != null) {
  // for (EcucContainerValue ecucValue : listOfEcucContainerValues) {
  //
  // if (ecucValue.getDefinition().equals(eObject)) {
  // getListOfOsTasks().add(ecucValue);
  // }
  //
  // }
  // }
  //
  // }
  //
  //
}
