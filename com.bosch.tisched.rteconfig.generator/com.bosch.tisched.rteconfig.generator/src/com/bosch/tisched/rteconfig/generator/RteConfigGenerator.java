package com.bosch.tisched.rteconfig.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;

import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.Logger;
import org.artop.aal.common.metamodel.AutosarReleaseDescriptor;
import org.artop.aal.workspace.preferences.IAutosarWorkspacePreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;

import com.bosch.blueworx.ulc.ILogEntry;
import com.bosch.blueworx.ulc.ILogWriter;
import com.bosch.blueworx.ulc.LogFactory;
import com.bosch.blueworx.ulc.LogLevel;
import com.bosch.blueworx.ulc.entry.Language;
import com.bosch.tisched.rteconfig.generator.core.OsConfigToEcucValueMapping;
import com.bosch.tisched.rteconfig.generator.ecucpartion.CreateFlatInstanceDescriptorForModeGroup;
import com.bosch.tisched.rteconfig.generator.ecucpartion.GenerateEcuCPartitionValue;
import com.bosch.tisched.rteconfig.generator.ecuextract.UpdateExcuExtractFiles;
import com.bosch.tisched.rteconfig.generator.osconfig.CONFType;
import com.bosch.tisched.rteconfig.generator.rips.GenerateRteRipsCSXfrmEcucValues;
import com.bosch.tisched.rteconfig.generator.rips.GenerateRteTimedEcucValue;
import com.bosch.tisched.rteconfig.generator.rte.GenerateRteEcuCValue;
import com.bosch.tisched.rteconfig.generator.rtememmap.xpt.GenerateRteMemmapXptFile;
import com.bosch.tisched.rteconfig.generator.util.ASWTriggerConnectionUtil;
import com.bosch.tisched.rteconfig.generator.util.AssemblySwConnectionUtil;
import com.bosch.tisched.rteconfig.generator.util.AutosarMergeUtil;
import com.bosch.tisched.rteconfig.generator.util.AutosarProjectCreationUtil;
import com.bosch.tisched.rteconfig.generator.util.AutosarUtil;
import com.bosch.tisched.rteconfig.generator.util.BSWTriggerConnectionUtil;
import com.bosch.tisched.rteconfig.generator.util.GenerateArxmlUtil;
import com.bosch.tisched.rteconfig.generator.util.MetadataUtil;
import com.bosch.tisched.rteconfig.generator.util.PostExecutionActionUtil;
import com.bosch.tisched.rteconfig.generator.util.PreExecutionActionUtil;
import com.bosch.tisched.rteconfig.generator.util.RteConfGenMessageDescription;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorConstants;
import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;
import com.bosch.tisched.rteconfig.generator.util.VariationPointUtil;

import autosar40.autosartoplevelstructure.AUTOSAR;
import autosar40.system.RootSwCompositionPrototype;
import autosar40.system.fibex.fibexcore.coretopology.EcuInstance;


/**
 * @author shk1cob
 */
public class RteConfigGenerator implements IApplication {

  private Integer exitCode = IApplication.EXIT_OK;

  private static Logger LOGGER = null;

  private final Map<String, List<EObject>> uriEObjectsMap = new HashedMap();
  private boolean bwritegating = false;

  @Override
  public Object start(final IApplicationContext context) throws Exception {

    final Map<?, ?> args = context.getArguments();
    final String[] appArgs = (String[]) args.get("application.args");
    try {

      if (appArgs.length == 6) {
        generateRteEcucValues(appArgs);
      }
      else {
        for (String arg : appArgs) {
          System.out.println("args " + arg);
        }
        System.out.println("*** Size of arguments is invalid");
        this.exitCode = RteConfigGeneratorConstants.EXIT_FAIL;
      }

    }
    finally {
      WorkspaceJob workspaceJob = new WorkspaceJob("Removing projects") {

        @Override
        public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
          IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
          for (final IProject project : projects) {
            try {
              LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("199_0").trim());
            }
            catch (Exception e) {
              e.printStackTrace();
            }
            project.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
          }
          return Status.OK_STATUS;
        }
      };
      workspaceJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
      workspaceJob.schedule();
      workspaceJob.join();
    }

    if (appArgs[3].equals("0") && appArgs[5].equals("1") && isErrorLogEntryExists()) {
      this.exitCode = 1;
    }
    return this.exitCode;
  }

  @Override
  public void stop() {
    // nothing
  }

  private void generateMemmapXpt(final IProject iProj, final Map<String, String> props, final BufferedWriter bw)
      throws Exception {
    List<EcuInstance> ecuInstances = GenerateArxmlUtil.getListOfEObject(iProj,
        props.get(RteConfigGeneratorConstants.ECU_INSTANCE_PKG_PATH).trim(), EcuInstance.class, "");

    if ((ecuInstances != null) && !ecuInstances.isEmpty()) {

      if (ecuInstances.size() > 1) {
        LOGGER.warn(RteConfGenMessageDescription
            .getFormattedMesssage("222_0", props.get(RteConfigGeneratorConstants.ECU_INSTANCE_PKG_PATH)).trim());
      }

      RootSwCompositionPrototype originalRootSwCompPrototype =
          GenerateArxmlUtil.getRootSwCompositionPrototype(ecuInstances.get(0), iProj, "");

      GenerateRteMemmapXptFile generateMemmapXpt =
          new GenerateRteMemmapXptFile(iProj, props, originalRootSwCompPrototype);

      generateMemmapXpt.generateRteMemmapXptFile();

      IResource findMember =
          iProj.findMember(props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST));

      if ((findMember != null) && findMember.isAccessible()) {
        bw.write(findMember.getProjectRelativePath().toOSString());
        bw.newLine();
        bw.flush();
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void generateRteEcucValues(final String... appArgs) throws Exception {

    boolean checkIfPathExists = new File(appArgs[0]).exists();
    if (!checkIfPathExists) {
      System.out.println("*** Project path '" + appArgs[0] + "' does not exist");
      this.exitCode = RteConfigGeneratorConstants.EXIT_FAIL;
    }
    else {
      Map<String, String> props =
          loadProperties(appArgs[0], appArgs[1], appArgs[3].equals("0") && appArgs[5].equals("0") ? "0" : "1");

      if (!props.get(RteConfigGeneratorConstants.RTECONFGEN_INPUTS_CHANGED).equals("y")) {

        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("193_1").trim());

      }
      else {

        try {

          if (!props.isEmpty()) {


            PreExecutionActionUtil preexaction = new PreExecutionActionUtil(props, appArgs[0]);
            List<String> tempdirList =
                new ArrayList<>(Arrays.asList(preexaction.getTemprteconfgen(), preexaction.getTemprteconfgenmemmap()));
            this.bwritegating = preexaction.createTempOutputDirs(tempdirList);
            if (this.bwritegating) {
              this.bwritegating = preexaction.copyPreviousOutputs();

            }

            Map<String, List<String>> outputmap = preexaction.getExpectedOutputMap();
            Map<String, CRC32> prevCRCMap = new HashMap<String, CRC32>();
            List<String> prevOutputList = preexaction.getListOfExistingOutput(prevCRCMap);

            removeExistingOutput(appArgs[0], props);


            String mode = props.get(RteConfigGeneratorConstants.MODE).trim();

            List<String> allFileList = new ArrayList<String>();

            try (Scanner s =
                new Scanner(new File(appArgs[0] + props.get(RteConfigGeneratorConstants.ALL_AUTOSAR_FILE_LIST)))) {
              while (s.hasNextLine()) {
                allFileList.add(s.nextLine().trim());
              }
            }
            catch (FileNotFoundException e) {
              try {
                LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                LOGGER.error("FileNotFoundException occured " + e.getMessage());
                RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                    .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
              }
              catch (Exception e1) {
                e1.printStackTrace();
              }

              this.exitCode = RteConfigGeneratorConstants.EXIT_FAIL;
              return;
            }

            List<Resource> listOfResourceBefExecution = new ArrayList<>();
            AssemblySwConnectionUtil assemblySwConnUtil = new AssemblySwConnectionUtil();

            IProject createAutosarProject =
                new AutosarProjectCreationUtil(props, allFileList).createAutosarProject(appArgs[0], assemblySwConnUtil);


            if (createAutosarProject == null) {
              this.exitCode = RteConfigGeneratorConstants.EXIT_FAIL;
              return;
            }

            updateUriEObjectsMap();
            VariationPointUtil.getInstance().setUriEObjectsMap(this.uriEObjectsMap);

            String listofRteConfGenFiles =
                appArgs[0] + props.get(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_FILES);
            try (BufferedWriter bw =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(listofRteConfGenFiles))))) {

              AutosarReleaseDescriptor autosarRelease =
                  IAutosarWorkspacePreferences.AUTOSAR_RELEASE.get(createAutosarProject);
              TransactionalEditingDomain editingDomain =
                  WorkspaceEditingDomainUtil.getEditingDomain(createAutosarProject, autosarRelease);
              listOfResourceBefExecution.addAll(editingDomain.getResourceSet().getResources());

              if (mode.equals(RteConfigGeneratorConstants.GENERATE_RTE_ECUC_VALUES)) {

                GenerateRteRipsCSXfrmEcucValues createRteRipsCSXfrmEcucValues =
                    new GenerateRteRipsCSXfrmEcucValues(createAutosarProject, props);

                OsConfigToEcucValueMapping osConfigToEcucValueMapping =
                    new OsConfigToEcucValueMapping(createAutosarProject, props, createRteRipsCSXfrmEcucValues);

                List<CONFType> confTypes = osConfigToEcucValueMapping.loadXmlContent();

                boolean doMapping = osConfigToEcucValueMapping.doMapping(confTypes);


                if (!doMapping) {
                  this.exitCode = RteConfigGeneratorConstants.EXIT_FAIL;
                  return;
                }


                ASWTriggerConnectionUtil aswTriggerConnectionUtil = new ASWTriggerConnectionUtil(
                    osConfigToEcucValueMapping, assemblySwConnUtil, createAutosarProject, props);

                aswTriggerConnectionUtil.validateTriggerPorts();
                aswTriggerConnectionUtil.validateTriggerConnections();

                IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

                  @Override
                  public void run(final IProgressMonitor monitor) throws CoreException {
                    GenerateRteTimedEcucValue createRteTimedEcucValue =
                        new GenerateRteTimedEcucValue(createAutosarProject, props, osConfigToEcucValueMapping);


                    try {
                      new GenerateRteEcuCValue(createRteTimedEcucValue, createAutosarProject, props,
                          osConfigToEcucValueMapping, createRteRipsCSXfrmEcucValues).generateRteEcuCValue();
                    }
                    catch (Exception e) {
                      LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                      e.printStackTrace();
                    }
                  }
                };

                ResourcesPlugin.getWorkspace().run(myRunnable, new NullProgressMonitor());

                IResource findMember =
                    createAutosarProject.findMember(props.get(RteConfigGeneratorConstants.RTE_ECUC_VALUE_OUTPUT_PATH));

                if ((findMember != null) && findMember.isAccessible()) {
                  bw.write(findMember.getProjectRelativePath().toOSString());
                  bw.newLine();
                  bw.flush();
                }

                myRunnable = new IWorkspaceRunnable() {

                  @Override
                  public void run(final IProgressMonitor monitor) throws CoreException {

                    try {
                      new GenerateEcuCPartitionValue(createAutosarProject, props, osConfigToEcucValueMapping)
                          .generateEcuCPartitionEcuCValue();
                    }
                    catch (Exception e) {
                      LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                      e.printStackTrace();
                    }

                  }
                };


                ResourcesPlugin.getWorkspace().run(myRunnable, new NullProgressMonitor());

                findMember = createAutosarProject
                    .findMember(props.get(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH));

                if ((findMember != null) && findMember.isAccessible()) {
                  bw.write(findMember.getProjectRelativePath().toOSString());
                  bw.newLine();
                  bw.flush();
                }


                LOGGER.info("Validating BSW Trigger ports and Connections...");

                BSWTriggerConnectionUtil bswTriggerConnectionUtil =
                    new BSWTriggerConnectionUtil(createAutosarProject, osConfigToEcucValueMapping, props);
                bswTriggerConnectionUtil.validateBSWTriggerPorts();
                bswTriggerConnectionUtil.validateBSWTriggerConnections();


                LOGGER.info("Validation of BSW Trigger ports and Connections completed");

              }
              else if (mode.equals(RteConfigGeneratorConstants.UPDATE_ECU_EXTRACT_FILES)) {

                GenerateRteRipsCSXfrmEcucValues createRteRipsCSXfrmEcucValues =
                    new GenerateRteRipsCSXfrmEcucValues(createAutosarProject, props);

                OsConfigToEcucValueMapping osConfigToEcucValueMapping =
                    new OsConfigToEcucValueMapping(createAutosarProject, props, createRteRipsCSXfrmEcucValues);
                List<CONFType> confTypes = osConfigToEcucValueMapping.loadXmlContent();
                GenerateRteTimedEcucValue createRteTimedEcucValue =
                    new GenerateRteTimedEcucValue(createAutosarProject, props, osConfigToEcucValueMapping);


                UpdateExcuExtractFiles updateExcuExtractFiles = new UpdateExcuExtractFiles(osConfigToEcucValueMapping,
                    createRteTimedEcucValue, createAutosarProject, props);

                updateExcuExtractFiles.UpdateExcuExtractFiles(assemblySwConnUtil);

                osConfigToEcucValueMapping
                    .setRootSwCompositionProtoType(updateExcuExtractFiles.getUpdatedRootSwCompositionPrototype());

                boolean doMapping = osConfigToEcucValueMapping.doMapping(confTypes);

                if (!doMapping) {
                  this.exitCode = RteConfigGeneratorConstants.EXIT_FAIL;
                  return;
                }
                ASWTriggerConnectionUtil aswTriggerConnectionUtil = new ASWTriggerConnectionUtil(
                    osConfigToEcucValueMapping, assemblySwConnUtil, createAutosarProject, props);

                aswTriggerConnectionUtil.validateTriggerPorts();


                if (osConfigToEcucValueMapping.isbAutoTriggerconnection()) {
                  aswTriggerConnectionUtil.generateASWTriggerConnection(updateExcuExtractFiles);
                }

                aswTriggerConnectionUtil.validateTriggerConnections();
                IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

                  @Override
                  public void run(final IProgressMonitor monitor) throws CoreException {
                    GenerateRteEcuCValue generateRteEcucValue = null;
                    try {
                      generateRteEcucValue = new GenerateRteEcuCValue(createRteTimedEcucValue, createAutosarProject,
                          props, osConfigToEcucValueMapping, createRteRipsCSXfrmEcucValues);

                    }
                    catch (Exception e1) {

                      LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                      e1.printStackTrace();
                    }

                    generateRteEcucValue
                        .updateTimedComSWCDVariableAccMap(updateExcuExtractFiles.getTimedComSWCPvariablesAccessMap());


                    try {
                      generateRteEcucValue.generateRteEcuCValue();
                    }
                    catch (Exception e) {
                      LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                      try {
                        RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                            .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
                      }
                      catch (Exception e1) {
                        e1.printStackTrace();
                      }
                    }

                  }
                };

                ResourcesPlugin.getWorkspace().run(myRunnable, new NullProgressMonitor());


                IResource findMember =
                    createAutosarProject.findMember(props.get(RteConfigGeneratorConstants.RTE_ECUC_VALUE_OUTPUT_PATH));

                if ((findMember != null) && findMember.isAccessible()) {
                  bw.write(findMember.getProjectRelativePath().toOSString());
                  bw.newLine();
                  bw.flush();
                }

                myRunnable = new IWorkspaceRunnable() {

                  @Override
                  public void run(final IProgressMonitor monitor) throws CoreException {

                    try {
                      new GenerateEcuCPartitionValue(createAutosarProject, props, osConfigToEcucValueMapping)
                          .generateEcuCPartitionEcuCValue();
                    }
                    catch (Exception e) {
                      LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                      try {
                        RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                            .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
                      }
                      catch (Exception e1) {
                        e1.printStackTrace();
                      }
                    }

                  }
                };


                ResourcesPlugin.getWorkspace().run(myRunnable, new NullProgressMonitor());

                findMember = createAutosarProject
                    .findMember(props.get(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH));

                if ((findMember != null) && findMember.isAccessible()) {
                  bw.write(findMember.getProjectRelativePath().toOSString());
                  bw.newLine();
                  bw.flush();
                }

                LOGGER.info("Validating BSW Trigger ports and Connections...");

                BSWTriggerConnectionUtil bswTriggerConnectionUtil =
                    new BSWTriggerConnectionUtil(createAutosarProject, osConfigToEcucValueMapping, props);
                bswTriggerConnectionUtil.validateBSWTriggerPorts();
                bswTriggerConnectionUtil.validateBSWTriggerConnections();


                LOGGER.info("Validation of BSW Trigger ports and Connections completed");


                if (props.get(RteConfigGeneratorConstants.CREATE_FID_FOR_MODE_GROUPS)
                    .endsWith(RteConfigGeneratorConstants.ENABLE_CREATE_FID_FOR_MODE_GROUPS)) {
                  myRunnable = new IWorkspaceRunnable() {

                    @Override
                    public void run(final IProgressMonitor monitor) throws CoreException {

                      try {
                        new CreateFlatInstanceDescriptorForModeGroup(createAutosarProject, props,
                            osConfigToEcucValueMapping, updateExcuExtractFiles, assemblySwConnUtil)
                                .createFlatInstanceDescriptorForModeGroup();
                      }
                      catch (Exception e) {
                        LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                        try {
                          RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                              .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
                        }
                        catch (Exception e1) {
                          e1.printStackTrace();
                        }
                      }


                    }
                  };

                  ResourcesPlugin.getWorkspace().run(myRunnable, new NullProgressMonitor());
                }


                myRunnable = new IWorkspaceRunnable() {

                  @Override
                  public void run(final IProgressMonitor monitor) throws CoreException {
                    if (osConfigToEcucValueMapping.isValidateTimedCom()) {
                      try {
                        createRteTimedEcucValue.generateTimedValueFile();
                      }
                      catch (Exception e) {
                        LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                        try {
                          RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                              .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
                        }
                        catch (Exception e1) {
                          e1.printStackTrace();
                        }
                      }
                    }
                  }
                };

                ResourcesPlugin.getWorkspace().run(myRunnable, new NullProgressMonitor());


                findMember = createAutosarProject
                    .findMember(props.get(RteConfigGeneratorConstants.RTE_TIMED_ECUC_VALUE_OUTPUT_PATH));

                if ((findMember != null) && findMember.isAccessible()) {
                  bw.write(findMember.getProjectRelativePath().toOSString());
                  bw.newLine();
                  bw.flush();
                }

                myRunnable = new IWorkspaceRunnable() {

                  @Override
                  public void run(final IProgressMonitor monitor) throws CoreException {

                    try {
                      createRteRipsCSXfrmEcucValues.generateRteRipsCSXfrmEcucValuesFile();
                    }
                    catch (Exception e) {
                      LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
                      try {
                        RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                            .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
                      }
                      catch (Exception e1) {
                        e1.printStackTrace();
                      }
                    }

                  }
                };

                ResourcesPlugin.getWorkspace().run(myRunnable, new NullProgressMonitor());

                findMember = createAutosarProject
                    .findMember(props.get(RteConfigGeneratorConstants.RTE_RIPS_CSXFRM_ECUC_VALUE_OUTPUT_PATH));

                if ((findMember != null) && findMember.isAccessible()) {
                  bw.write(findMember.getProjectRelativePath().toOSString());
                  bw.newLine();
                  bw.flush();
                }

              }

              if (props.get(RteConfigGeneratorConstants.ENABLE_MEMMAP_XPT_GENERATION).equals("y")) {
                if (props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_INPUTS_CHANGED).equals("y")) {
                  IResource flatMapFile = createAutosarProject
                      .findMember(props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH));

                  if ((flatMapFile != null) && flatMapFile.exists()) {
                    ModelLoadManager.INSTANCE.loadFile(EcorePlatformUtil.getFile(flatMapFile), false,
                        new NullProgressMonitor());

                    AUTOSAR arRootObj = (autosar40.autosartoplevelstructure.AUTOSAR) EcorePlatformUtil
                        .loadModelRoot(EcorePlatformUtil.getFile(flatMapFile));

                    EcoreUtil.resolveAll(arRootObj);

                    autosarRelease = IAutosarWorkspacePreferences.AUTOSAR_RELEASE.get(createAutosarProject);
                    editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(createAutosarProject, autosarRelease);
                  }


                  TimeUnit.SECONDS.sleep(3);


                  List<Resource> newResources = editingDomain.getResourceSet().getResources().stream()
                      .filter(r -> !listOfResourceBefExecution.contains(r)).collect(Collectors.toList());


                  AutosarMergeUtil.mergeAutosarElements(createAutosarProject, newResources);

                  generateMemmapXpt(createAutosarProject, props, bw);

                }
                else {
                  LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("193_0").trim());
                }

              }

              try {
                runPostExecutionActions(props, preexaction, appArgs[0], outputmap, prevOutputList, prevCRCMap);
              }
              catch (Exception e) {
                LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("270_6"));
              }

            }
            catch (Exception ex) {
              try {
                LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());

                RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                    .getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());
              }
              catch (Exception e1) {
                e1.printStackTrace();
              }
              this.exitCode = RteConfigGeneratorConstants.EXIT_FAIL;
            }
          }
          else {
            try {
              RteConfigGeneratorLogger.logErrormessage(LOGGER,
                  RteConfGenMessageDescription.getFormattedMesssage("301_0", appArgs[0], appArgs[1]).trim());
            }
            catch (Exception e1) {
              e1.printStackTrace();
            }

            this.exitCode = RteConfigGeneratorConstants.EXIT_FAIL;
          }
        }
        finally {
          try {

            List<ILogEntry> logEntries = RteConfigGeneratorLogger.getLogEntries();
            if ((logEntries != null) && !logEntries.isEmpty()) {
              String logpath = props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_ULF);
              File ulfFile = new File(appArgs[0] + logpath);
              if (!ulfFile.exists()) {
                ulfFile.createNewFile();
              }
              String filePath = ulfFile.getAbsolutePath();


              ILogWriter ulfLogWriter = LogFactory.createLogWriter(filePath);
              ulfLogWriter.setRootMessageLanguage(Language.EN);
              ulfLogWriter.setExpectedMessageLanguages(Language.EN, Language.DE);
              for (ILogEntry logEntry : logEntries) {
                ulfLogWriter.submit(logEntry);
              }
              ulfLogWriter.close();
              try {
                LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("118_0", ulfFile.getAbsolutePath()));
              }
              catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }

            }
          }
          catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            this.exitCode = RteConfigGeneratorConstants.EXIT_FAIL;
          }
        }
      }

    }


  }

  /**
   * @param props
   * @param preexaction
   * @param string
   * @param prevOutputList
   * @param outputmap
   * @param prevCRCMap
   * @throws Exception
   */
  private void runPostExecutionActions(final Map<String, String> props, final PreExecutionActionUtil preexaction,
      final String prjPath, final Map<String, List<String>> outputmap, final List<String> prevOutputList,
      final Map<String, CRC32> prevCRCMap)
      throws Exception {
    try {
      if (this.bwritegating) {
        Map<String, CRC32> curCRCMap = new HashMap<String, CRC32>();
        List<String> curOutputList = preexaction.getListOfExistingOutput(curCRCMap);
        PostExecutionActionUtil postexaction = new PostExecutionActionUtil();
        if (prevOutputList.equals(curOutputList)) {
          boolean boutputfileexists = true;
          for (Entry<String, List<String>> entry : outputmap.entrySet()) {
            if (entry.getValue().stream().anyMatch(f -> !new File(f).exists())) {
              boutputfileexists = false;
              break;
            }
          }

          if (boutputfileexists) {

            for (Entry<String, List<String>> entry : outputmap.entrySet()) {
              if (entry.getKey().equals("Ecu Extract")) {
                try {
                  postexaction.compareAndReplaceMandatoryArxmlFiles(preexaction, entry, prevCRCMap, curCRCMap);
                }
                catch (Exception e) {
                  LOGGER.warn(
                      RteConfGenMessageDescription.getFormattedMesssage("270_5", "Ecu Extract", "\n" + e.getMessage()));
                }
              }
              else if (entry.getKey().equals("EcuC Values")) {
                try {
                  postexaction.compareAndReplaceMandatoryArxmlFiles(preexaction, entry, prevCRCMap, curCRCMap);
                  postexaction.compareAndReplaceTextFiles(preexaction, entry, prevCRCMap, curCRCMap);
                }
                catch (Exception e) {
                  LOGGER.warn(
                      RteConfGenMessageDescription.getFormattedMesssage("270_5", "EcuC Values", "\n" + e.getMessage()));
                }
              }
              else if (entry.getKey().equals("Memmap Header")) {
                try {
                  postexaction.compareAndReplaceMemmapHeaders(preexaction, entry, prevCRCMap, curCRCMap);
                }
                catch (Exception e) {
                  LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("270_5", "Memmap Header",
                      "\n" + e.getMessage()));
                }
              }
            }
          }
          try {
            postexaction.compareAndReplaceOptionalFiles(preexaction, props, prjPath, prevCRCMap, curCRCMap);
          }
          catch (Exception e) {
            LOGGER.warn(RteConfGenMessageDescription.getFormattedMesssage("270_5", "CSXFrm/Timed Communication",
                "\n" + e.getMessage()));
          }
        }
      }
    }
    finally {
      PostExecutionActionUtil.deleteTempOutputDirs(preexaction);
    }
  }

  /**
   * @param props
   * @param prjPath
   * @throws Exception
   */
  private void removeExistingOutput(final String prjPath, final Map<String, String> props) throws Exception {
    removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.RTE_ECUC_VALUE_OUTPUT_PATH));
    removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH));
    removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.RTE_TIMED_ECUC_VALUE_OUTPUT_PATH));
    removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.RTE_RIPS_CSXFRM_ECUC_VALUE_OUTPUT_PATH));

    if (props.get(RteConfigGeneratorConstants.MODE).equals(RteConfigGeneratorConstants.UPDATE_ECU_EXTRACT_FILES)) {
      removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FILE_OUTPUT_PATH));
      removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH));
      removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_VIEW_FILE_OUTPUT_PATH));
      removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.RTECONFGEN_TA_CONNECTION_LOG_PATH));
      removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.RTECONFGEN_TA_CONNECTION_CSV_PATH));
      removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.RTECONFGEN_TA_CONNECTION_JSON_PATH));
      String propertyValue = props.get(RteConfigGeneratorConstants.RTECONFGEN_GENERATE_SYSTEM_EXTRACT);
      if ((propertyValue != null) && !propertyValue.isEmpty() && propertyValue.equalsIgnoreCase("y")) {
        removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.SYSTEM_EXTRACT_FILE_OUTPUT_PATH));
      }

    }
  }

  private boolean isErrorLogEntryExists() {


    for (ILogEntry logEntry : RteConfigGeneratorLogger.getLogEntries()) {

      if (logEntry.getLogLevel() == LogLevel.ERROR) {
        return true;
      }

    }

    return false;

  }


  private Map<String, String> loadProperties(final String prjPath, final String optFilepath, final String mode)
      throws Exception {

    Map<String, String> props = new HashMap<String, String>();

    Properties prop = new Properties();

    InputStream input = null;

    STOP: try {

      input = new FileInputStream(prjPath + "\\" + optFilepath);
      prop.load(input);

      String propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG, "/" + propertyValue.trim());
      }
      else {
        System.out.println("*** RteConfigGen log path is missing");
        props.clear();
        break STOP;
      }

      boolean bVersionchanged =
          compareVersionWithPrevious(prjPath + props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG));
      removeExistingFile(prjPath, props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG));

      props.put(RteConfigGeneratorConstants.RTE_CONFGEN_EXECUTION_MODE,
          mode.equals("0") ? RteConfigGeneratorConstants.PRODUCTIVE_MODE : RteConfigGeneratorConstants.TEST_MODE);


      String ulfPath = prop.getProperty(RteConfigGeneratorConstants.RTE_CONFIG_GEN_ULF);
      if ((ulfPath != null) && !ulfPath.isEmpty()) {
        props.put(RteConfigGeneratorConstants.RTE_CONFIG_GEN_ULF, "/" + ulfPath.trim());
      }
      else {
        System.out.println("*** RteConfigGen ULF file path is missing");
        props.clear();
        break STOP;
      }

      new RteConfigGeneratorLogger().updateLog4jConfiguration(prjPath + "\\" + propertyValue.trim(),
          prjPath + props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_ULF), props);
      LOGGER = RteConfigGeneratorLogger.getLogger(RteConfigGenerator.class.getName());
      new RteConfGenMessageDescription();
      LOGGER.info(
          RteConfGenMessageDescription.getFormattedMesssage("101_0", RteConfigGeneratorConstants.TOOL_VERSION).trim());
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("101_1", optFilepath).trim());
      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("101_2", props.get(RteConfigGeneratorConstants.RTE_CONFGEN_EXECUTION_MODE)).trim());
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("101_3", prjPath).trim());
      LOGGER.info(
          RteConfGenMessageDescription.getFormattedMesssage("101_4", RteConfigGeneratorConstants.RTE_CONFIG_GEN_ULF,
              props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_ULF)).trim());


      propertyValue = prop.getProperty(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION, propertyValue);
      }
      else {
        props.put(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION, "http://autosar.org/schema/r4.0");
      }

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION, propertyValue);
      }
      else {
        props.put(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION, "AUTOSAR 00048");
      }


      propertyValue = prop.getProperty(RteConfigGeneratorConstants.ENABLE_MM_DATA_CONSISTENCY);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.ENABLE_MM_DATA_CONSISTENCY, propertyValue);
      }
      else {
        props.put(RteConfigGeneratorConstants.ENABLE_MM_DATA_CONSISTENCY,
            RteConfigGeneratorConstants.DISABLE_MM_DATA_CONSISTENCY_MODE);
      }
      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("101_5", RteConfigGeneratorConstants.ENABLE_MM_DATA_CONSISTENCY,
              props.get(RteConfigGeneratorConstants.ENABLE_MM_DATA_CONSISTENCY))
          .trim());

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.CREATE_FID_FOR_MODE_GROUPS);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.CREATE_FID_FOR_MODE_GROUPS, propertyValue);
      }
      else {
        props.put(RteConfigGeneratorConstants.CREATE_FID_FOR_MODE_GROUPS,
            RteConfigGeneratorConstants.DISABLE_CREATE_FID_FOR_MODE_GROUPS);
      }

      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("101_6", RteConfigGeneratorConstants.CREATE_FID_FOR_MODE_GROUPS,
              props.get(RteConfigGeneratorConstants.CREATE_FID_FOR_MODE_GROUPS))
          .trim());


      propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR, "/" + propertyValue.trim());
        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_7", RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR, propertyValue).trim());

      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("301_1").trim());
        props.clear();
        break STOP;
      }


      propertyValue = prop.getProperty(RteConfigGeneratorConstants.ALL_AUTOSAR_FILE_LIST);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.ALL_AUTOSAR_FILE_LIST, "/" + propertyValue.trim());
        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_8", RteConfigGeneratorConstants.ALL_AUTOSAR_FILE_LIST, propertyValue).trim());

      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("301_2").trim());
        props.clear();
        break STOP;
      }


      propertyValue = prop.getProperty(RteConfigGeneratorConstants.ECU_INSTANCE_PKG_PATH);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.ECU_INSTANCE_PKG_PATH, propertyValue.trim());
        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_9", RteConfigGeneratorConstants.ECU_INSTANCE_PKG_PATH, propertyValue).trim());

      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("301_3").trim());
        props.clear();
        break STOP;
      }


      String rteconfiggenDir = props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR);

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.MODE);

      if ((propertyValue != null) && !propertyValue.isEmpty() &&
          (propertyValue.trim().equals(RteConfigGeneratorConstants.GENERATE_RTE_ECUC_VALUES) ||
              propertyValue.trim().equals(RteConfigGeneratorConstants.UPDATE_ECU_EXTRACT_FILES))) {
        props.put(RteConfigGeneratorConstants.MODE, propertyValue.trim());

        if (propertyValue.trim().equals(RteConfigGeneratorConstants.UPDATE_ECU_EXTRACT_FILES)) {

          LOGGER.info(RteConfGenMessageDescription
              .getFormattedMesssage("101_10", RteConfigGeneratorConstants.MODE, propertyValue).trim());

          propertyValue = prop.getProperty(RteConfigGeneratorConstants.EXCLUDE_ARXML_PATH);

          if ((propertyValue != null) && !propertyValue.isEmpty()) {
            props.put(RteConfigGeneratorConstants.EXCLUDE_ARXML_PATH, propertyValue.trim());
          }
          else {
            props.put(RteConfigGeneratorConstants.EXCLUDE_ARXML_PATH, "");
          }


          props.put(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_VIEW_FILE_OUTPUT_PATH,
              rteconfiggenDir + "/RTEConfGen_FlatView_swcd.arxml");

          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("101_11", rteconfiggenDir.replaceFirst("/", ""))
              .trim());


          props.put(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH,
              rteconfiggenDir + "/RTEConfGen_FlatMap.arxml");

          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("101_12", rteconfiggenDir.replaceFirst("/", ""))
              .trim());


          props.put(RteConfigGeneratorConstants.ECU_EXTRACT_FILE_OUTPUT_PATH,
              rteconfiggenDir + "/RTEConfGen_Ecu_Extract.arxml");
          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("101_13", rteconfiggenDir.replaceFirst("/", ""))
              .trim());


          String logdir = props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG);
          String rteConfGenlogDir = logdir.substring(1, logdir.length()).substring(0, logdir.lastIndexOf("/"));
          rteConfGenlogDir = rteConfGenlogDir.endsWith("/")
              ? rteConfGenlogDir.substring(0, rteConfGenlogDir.length() - 1) : rteConfGenlogDir;

          props.put(RteConfigGeneratorConstants.RTECONFGEN_TA_CONNECTION_LOG_PATH,
              rteConfGenlogDir + "/RTEConfGen_TA_Connection_Report.txt");
          props.put(RteConfigGeneratorConstants.RTECONFGEN_TA_CONNECTION_CSV_PATH,
              rteconfiggenDir + "/RTEConfGen_TA_Connections.csv");
          props.put(RteConfigGeneratorConstants.RTECONFGEN_TA_CONNECTION_JSON_PATH,
              rteconfiggenDir + "/RTEConfGen_TA_Connections.json");


          propertyValue = prop.getProperty(RteConfigGeneratorConstants.LIST_OF_ECU_EXTRACT_FILES);

          if ((propertyValue != null) && !propertyValue.isEmpty()) {
            props.put(RteConfigGeneratorConstants.LIST_OF_ECU_EXTRACT_FILES, "/" + propertyValue.trim());
            LOGGER.info(RteConfGenMessageDescription
                .getFormattedMesssage("101_14", RteConfigGeneratorConstants.LIST_OF_ECU_EXTRACT_FILES, propertyValue)
                .trim());
          }
          else {
            RteConfigGeneratorLogger.logErrormessage(LOGGER,
                RteConfGenMessageDescription.getFormattedMesssage("301_4").trim());
            props.clear();
            break STOP;
          }


        }
        else {
          LOGGER.info(RteConfGenMessageDescription
              .getFormattedMesssage("101_15", RteConfigGeneratorConstants.MODE, propertyValue).trim());
        }
      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("301_5").trim());
        props.clear();
        break STOP;
      }

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.OVER_ALL_OS_CONFIG_INPUT_FILE);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {

        if (!new File(prjPath + "/" + propertyValue.trim()).exists()) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_6", prjPath, propertyValue.trim()).trim());
          props.clear();
          break STOP;
        }
        props.put(RteConfigGeneratorConstants.OVER_ALL_OS_CONFIG_INPUT_FILE, prjPath + "/" + propertyValue.trim());
        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_16", RteConfigGeneratorConstants.OVER_ALL_OS_CONFIG_INPUT_FILE, propertyValue)
            .trim());


      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("301_7").trim());
        props.clear();
        break STOP;
      }


      propertyValue = prop.getProperty(RteConfigGeneratorConstants.ADDITIONAL_INPUT_FILES_LIST);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {

        if (!new File(prjPath + "/" + propertyValue.trim()).exists()) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_8", prjPath, propertyValue.trim()).trim());
          props.clear();
          break STOP;
        }
        props.put(RteConfigGeneratorConstants.ADDITIONAL_INPUT_FILES_LIST, prjPath + "/" + propertyValue.trim());
        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_17", RteConfigGeneratorConstants.ADDITIONAL_INPUT_FILES_LIST, propertyValue)
            .trim());

      }
      else {
        props.put(RteConfigGeneratorConstants.ADDITIONAL_INPUT_FILES_LIST, "");
      }

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.MEMMAP_HEADERFILE_GENERATION);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        if (propertyValue.equalsIgnoreCase("y") || propertyValue.equalsIgnoreCase("n")) {
          props.put(RteConfigGeneratorConstants.MEMMAP_HEADERFILE_GENERATION, propertyValue);
        }
        else {
          props.put(RteConfigGeneratorConstants.MEMMAP_HEADERFILE_GENERATION, "n");
        }
      }
      else {
        props.put(RteConfigGeneratorConstants.MEMMAP_HEADERFILE_GENERATION,
            RteConfigGeneratorConstants.ENABLE_MEMMAP_HEADERFILE_GENERATION);
      }
      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("101_18", RteConfigGeneratorConstants.MEMMAP_HEADERFILE_GENERATION,
              props.get(RteConfigGeneratorConstants.MEMMAP_HEADERFILE_GENERATION))
          .trim());


      props.put(RteConfigGeneratorConstants.RTE_ECUC_VALUE_OUTPUT_PATH,
          rteconfiggenDir + "/RTEConfGen_Rte_EcucValues.arxml");
      LOGGER.info(
          RteConfGenMessageDescription.getFormattedMesssage("101_19", rteconfiggenDir.replaceFirst("/", "")).trim());


      props.put(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH,
          rteconfiggenDir + "/RTEConfGen_EcucPartition_EcucValues.arxml");
      LOGGER.info(
          RteConfGenMessageDescription.getFormattedMesssage("101_20", rteconfiggenDir.replaceFirst("/", "")).trim());


      props.put(RteConfigGeneratorConstants.RTE_TIMED_ECUC_VALUE_OUTPUT_PATH,
          rteconfiggenDir + "/RTEConfGen_Rte_Timed_EcucValues.arxml");
      LOGGER.info(
          RteConfGenMessageDescription.getFormattedMesssage("101_21", rteconfiggenDir.replaceFirst("/", "")).trim());


      props.put(RteConfigGeneratorConstants.RTE_RIPS_CSXFRM_ECUC_VALUE_OUTPUT_PATH,
          rteconfiggenDir + "/RTEConfGen_RteRips_CSXfrm_EcucValues.arxml");
      LOGGER.info(
          RteConfGenMessageDescription.getFormattedMesssage("101_22", rteconfiggenDir.replaceFirst("/", "")).trim());


      propertyValue =
          prop.getProperty(RteConfigGeneratorConstants.CONSIDER_ALL_BSW_EVENTS_FROM_PVER_FOR_RTE_ECUC_VALUE_GENERATION);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.CONSIDER_ALL_BSW_EVENTS_FROM_PVER_FOR_RTE_ECUC_VALUE_GENERATION,
            propertyValue);
      }
      else {
        props.put(RteConfigGeneratorConstants.CONSIDER_ALL_BSW_EVENTS_FROM_PVER_FOR_RTE_ECUC_VALUE_GENERATION, "true");
      }
      LOGGER
          .info(
              RteConfGenMessageDescription
                  .getFormattedMesssage("101_23",
                      RteConfigGeneratorConstants.CONSIDER_ALL_BSW_EVENTS_FROM_PVER_FOR_RTE_ECUC_VALUE_GENERATION,
                      props.get(
                          RteConfigGeneratorConstants.CONSIDER_ALL_BSW_EVENTS_FROM_PVER_FOR_RTE_ECUC_VALUE_GENERATION))
                  .trim());


      propertyValue = prop.getProperty(
          RteConfigGeneratorConstants.CONSIDER_ALL_ASW_COMPONENTS_FROM_SWC_BSW_MAPPINGS_FOR_ECUC_PARTITION_GENERATION);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(
            RteConfigGeneratorConstants.CONSIDER_ALL_ASW_COMPONENTS_FROM_SWC_BSW_MAPPINGS_FOR_ECUC_PARTITION_GENERATION,
            propertyValue);
      }
      else {
        props.put(
            RteConfigGeneratorConstants.CONSIDER_ALL_ASW_COMPONENTS_FROM_SWC_BSW_MAPPINGS_FOR_ECUC_PARTITION_GENERATION,
            "true");
      }
      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("101_24",
          RteConfigGeneratorConstants.CONSIDER_ALL_ASW_COMPONENTS_FROM_SWC_BSW_MAPPINGS_FOR_ECUC_PARTITION_GENERATION,
          props.get(
              RteConfigGeneratorConstants.CONSIDER_ALL_ASW_COMPONENTS_FROM_SWC_BSW_MAPPINGS_FOR_ECUC_PARTITION_GENERATION))
          .trim());


      propertyValue = prop.getProperty(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_FILES);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_FILES, "/" + propertyValue.trim());
        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_25", RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_FILES, propertyValue)
            .trim());
      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("301_9").trim());
        props.clear();
        break STOP;
      }

      if (props.get(RteConfigGeneratorConstants.MEMMAP_HEADERFILE_GENERATION).equalsIgnoreCase("y")) {

        propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR);

        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          props.put(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR, "/" + propertyValue.trim());
        }
        else {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_10").trim());
          props.clear();
          break STOP;
        }

        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_26", RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR,
                props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR))
            .trim());


        propertyValue = prop.getProperty(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_MEMMAP_HEADER_FILES);

        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          props.put(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_MEMMAP_HEADER_FILES, "/" + propertyValue.trim());
        }
        else {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_11").trim());
          props.clear();
          break STOP;
        }
        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_27", RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_MEMMAP_HEADER_FILES,
                props.get(RteConfigGeneratorConstants.LIST_OF_RTE_CONFIG_GEN_MEMMAP_HEADER_FILES))
            .trim());

      }

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTE_CONFIG_GEN_TASK_RECURRENCE_FILE_PATH);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        props.put(RteConfigGeneratorConstants.RTE_CONFIG_GEN_TASK_RECURRENCE_FILE_PATH, "/" + propertyValue.trim());
      }
      else {
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("301_12").trim());
        props.clear();
        break STOP;
      }
      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("101_28", RteConfigGeneratorConstants.RTE_CONFIG_GEN_TASK_RECURRENCE_FILE_PATH,
              props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_TASK_RECURRENCE_FILE_PATH))
          .trim());

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD);

      if ((propertyValue != null) && !propertyValue.isEmpty()) {
        if (propertyValue.equalsIgnoreCase("y") || propertyValue.equalsIgnoreCase("n")) {
          props.put(RteConfigGeneratorConstants.RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD, propertyValue);
        }
        else {
          props.put(RteConfigGeneratorConstants.RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD, "n");
        }
      }
      else {
        props.put(RteConfigGeneratorConstants.RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD,
            RteConfigGeneratorConstants.ENABLE_RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD);
      }
      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("101_39", RteConfigGeneratorConstants.RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD,
              props.get(RteConfigGeneratorConstants.RTECONFGEN_OSAPP_SPECIFIC_SWADDRMETHOD))
          .trim());

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTECONFGEN_TRIGGER_VALIDATION_ERROR_AS_WARNING);
      if ((propertyValue != null) && !propertyValue.isEmpty() &&
          (propertyValue.equalsIgnoreCase("y") || propertyValue.equalsIgnoreCase("n"))) {
        props.put(RteConfigGeneratorConstants.RTECONFGEN_TRIGGER_VALIDATION_ERROR_AS_WARNING, propertyValue);
      }
      else {
        props.put(RteConfigGeneratorConstants.RTECONFGEN_TRIGGER_VALIDATION_ERROR_AS_WARNING, "n");
      }

      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("101_42", RteConfigGeneratorConstants.RTECONFGEN_TRIGGER_VALIDATION_ERROR_AS_WARNING,
              props.get(RteConfigGeneratorConstants.RTECONFGEN_TRIGGER_VALIDATION_ERROR_AS_WARNING))
          .trim());

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTECONFGEN_GENERATE_SYSTEM_EXTRACT);
      if ((propertyValue != null) && !propertyValue.isEmpty() && propertyValue.equalsIgnoreCase("y")) {
        props.put(RteConfigGeneratorConstants.RTECONFGEN_GENERATE_SYSTEM_EXTRACT, propertyValue);
        props.put(RteConfigGeneratorConstants.SYSTEM_EXTRACT_FILE_OUTPUT_PATH,
            rteconfiggenDir + "/RTEConfGen_SystemExtract.arxml");


        String systemExtractFilePath = props.get(RteConfigGeneratorConstants.SYSTEM_EXTRACT_FILE_OUTPUT_PATH);
        LOGGER.info("MM_DCS_RTECONFGEN_101 : System extract file output path: " + systemExtractFilePath);
      }
      else {
        props.put(RteConfigGeneratorConstants.RTECONFGEN_GENERATE_SYSTEM_EXTRACT, "n");
      }
      LOGGER.info(RteConfGenMessageDescription
          .getFormattedMesssage("101_43", RteConfigGeneratorConstants.RTECONFGEN_GENERATE_SYSTEM_EXTRACT,
              props.get(RteConfigGeneratorConstants.RTECONFGEN_GENERATE_SYSTEM_EXTRACT))
          .trim());

      propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_CONFIG_FILEPATH);

      if ((propertyValue != null)) {

        File configfile = new File(prjPath + "/" + propertyValue.trim());
        if (!configfile.isFile() || !configfile.exists()) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_16", prjPath + "/" + propertyValue.trim()).trim());
          props.clear();
          break STOP;
        }
        else if ((configfile.length() == 0L)) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_23", prjPath + "/" + propertyValue.trim()).trim());
          props.clear();
          break STOP;
        }
        else {
          props.put(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_CONFIG_FILEPATH, "/" + propertyValue.trim());
          props.put(RteConfigGeneratorConstants.ENABLE_MEMMAP_XPT_GENERATION, "y");

        }

      }
      else {
        props.put(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_CONFIG_FILEPATH, null);
        props.put(RteConfigGeneratorConstants.ENABLE_MEMMAP_XPT_GENERATION, "n");
      }


      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("101_29", "--enable_memmap_xpt_generation",
          props.get(RteConfigGeneratorConstants.ENABLE_MEMMAP_XPT_GENERATION)).trim());

      if (props.get(RteConfigGeneratorConstants.ENABLE_MEMMAP_XPT_GENERATION).equals("y")) {


        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_34", RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_CONFIG_FILEPATH,
                props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_CONFIG_FILEPATH))
            .trim());

        propertyValue = prop.getProperty(RteConfigGeneratorConstants.MACHINE_FAMILY);

        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          props.put(RteConfigGeneratorConstants.MACHINE_FAMILY, propertyValue.trim());
          LOGGER.info(
              RteConfGenMessageDescription.getFormattedMesssage("101_33", RteConfigGeneratorConstants.MACHINE_FAMILY,
                  props.get(RteConfigGeneratorConstants.MACHINE_FAMILY)).trim());

        }
        else {
          props.put(RteConfigGeneratorConstants.MACHINE_FAMILY, null);
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_15").trim());
          props.clear();
          break STOP;

        }

        propertyValue = prop.getProperty(RteConfigGeneratorConstants.STARTSECTIONPATH);

        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          File startsection = new File(prjPath + "/" + propertyValue.trim());
          if (!startsection.isFile() || !startsection.exists()) {
            RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                .getFormattedMesssage("301_18", prjPath + "/" + propertyValue.trim()).trim());
            props.clear();
            break STOP;
          }
          else if (!props.get(RteConfigGeneratorConstants.MACHINE_FAMILY).toUpperCase().contains("IFX") &&
              (startsection.length() == 0L)) {
            RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                .getFormattedMesssage("301_20", prjPath + "/" + propertyValue.trim()).trim());
            props.clear();
            break STOP;
          }
          else {
            props.put(RteConfigGeneratorConstants.STARTSECTIONPATH, "/" + propertyValue.trim());
            LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("101_31",
                RteConfigGeneratorConstants.STARTSECTIONPATH, props.get(RteConfigGeneratorConstants.STARTSECTIONPATH))
                .trim());
          }
        }
        else {
          props.put(RteConfigGeneratorConstants.STARTSECTIONPATH, null);
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_13").trim());
          props.clear();
          break STOP;

        }


        propertyValue = prop.getProperty(RteConfigGeneratorConstants.ENDSECTIONPATH);
        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          File endsection = new File(prjPath + "/" + propertyValue.trim());
          if (!endsection.isFile() || !endsection.exists()) {
            RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                .getFormattedMesssage("301_19", prjPath + "/" + propertyValue.trim()).trim());
            props.clear();
            break STOP;
          }
          else if (!props.get(RteConfigGeneratorConstants.MACHINE_FAMILY).toUpperCase().contains("IFX") &&
              (endsection.length() == 0L)) {
            RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                .getFormattedMesssage("301_21", prjPath + "/" + propertyValue.trim()).trim());
            props.clear();
            break STOP;
          }
          else {

            props.put(RteConfigGeneratorConstants.ENDSECTIONPATH, "/" + propertyValue.trim());
            LOGGER.info(
                RteConfGenMessageDescription.getFormattedMesssage("101_32", RteConfigGeneratorConstants.ENDSECTIONPATH,
                    props.get(RteConfigGeneratorConstants.ENDSECTIONPATH)).trim());
          }
        }
        else {
          props.put(RteConfigGeneratorConstants.ENDSECTIONPATH, null);
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_14").trim());
          props.clear();
          break STOP;

        }


        propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST);

        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          props.put(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST, "/" + propertyValue.trim());
          LOGGER.info(RteConfGenMessageDescription
              .getFormattedMesssage("101_35", RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST,
                  props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST))
              .trim());
        }
        else {
          props.put(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST, null);
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("301_17").trim());
          props.clear();
          break STOP;
        }


        propertyValue = prop
            .getProperty(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_TASKWISE_SW_COMPONENT_PROTOTYPE_LIST_FILEPATH);

        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          File swclistfile = new File(prjPath + "/" + propertyValue.trim());
          if (!swclistfile.isFile() || !swclistfile.exists()) {
            RteConfigGeneratorLogger.logErrormessage(LOGGER, RteConfGenMessageDescription
                .getFormattedMesssage("301_22", prjPath + "/" + propertyValue.trim()).trim());
            props.clear();
            break STOP;
          }
          props.put(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_TASKWISE_SW_COMPONENT_PROTOTYPE_LIST_FILEPATH,
              "/" + propertyValue.trim());

        }
        else {
          props.put(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_TASKWISE_SW_COMPONENT_PROTOTYPE_LIST_FILEPATH, null);
        }

        LOGGER
            .info(RteConfGenMessageDescription
                .getFormattedMesssage("101_36",
                    RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_TASKWISE_SW_COMPONENT_PROTOTYPE_LIST_FILEPATH,
                    props.get(
                        RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_TASKWISE_SW_COMPONENT_PROTOTYPE_LIST_FILEPATH))
                .trim());

        propertyValue = prop.getProperty(RteConfigGeneratorConstants.ALLOCATETASKASPEROSAPP);

        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          props.put(RteConfigGeneratorConstants.ALLOCATETASKASPEROSAPP, propertyValue.trim());
        }
        else {
          props.put(RteConfigGeneratorConstants.ALLOCATETASKASPEROSAPP, "n");
        }

        LOGGER.info(RteConfGenMessageDescription
            .getFormattedMesssage("101_37", RteConfigGeneratorConstants.ALLOCATETASKASPEROSAPP,
                props.get(RteConfigGeneratorConstants.ALLOCATETASKASPEROSAPP))
            .trim());

        propertyValue = prop.getProperty(RteConfigGeneratorConstants.ADDSWCASPEROSAPP);

        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          props.put(RteConfigGeneratorConstants.ADDSWCASPEROSAPP, propertyValue.trim());
        }
        else {
          props.put(RteConfigGeneratorConstants.ADDSWCASPEROSAPP, "n");
        }

        LOGGER.info(
            RteConfGenMessageDescription.getFormattedMesssage("101_38", RteConfigGeneratorConstants.ADDSWCASPEROSAPP,
                props.get(RteConfigGeneratorConstants.ADDSWCASPEROSAPP)).trim());

        propertyValue = prop.getProperty(RteConfigGeneratorConstants.RTARTE_VERSION);

        if ((propertyValue != null) && !propertyValue.isEmpty()) {
          props.put(RteConfigGeneratorConstants.RTARTE_VERSION, propertyValue.trim());
        }
        else {
          props.put(RteConfigGeneratorConstants.RTARTE_VERSION, "12.0.0");
        }
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("101_41",
            RteConfigGeneratorConstants.RTARTE_VERSION, props.get(RteConfigGeneratorConstants.RTARTE_VERSION)).trim());

      }
      else {
        removeExistingXPTFile(prjPath, rteconfiggenDir);
        removeExistingFile(prjPath, rteconfiggenDir + "/rteconfgen_memmap_xpt_files.lst");
      }

      writeGating(bVersionchanged, prjPath, props, optFilepath, rteconfiggenDir);

    }
    catch (IOException ex) {
      if (LOGGER == null) {
        System.out.println(ex.getMessage());
        props.clear();
      }
      else {
        LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
        LOGGER.error("IOException occured " + ex.getMessage());
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());
        props.clear();
      }
    }
    finally {
      if (input != null) {
        try {
          input.close();
        }
        catch (IOException ex) {
          LOGGER.warn(AutosarUtil.getCurrentProcessingEObject());
          LOGGER.error("IOException occured " + ex.getMessage());
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());
          props.clear();
        }
      }
    }
    return props;
  }


  /**
   * @param string
   * @return
   * @throws IOException
   */
  private boolean compareVersionWithPrevious(final String string) throws IOException {

    File rteconfgenLog = new File(string);
    boolean bVersionChanged = false;
    if (rteconfgenLog.length() != 0) {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(rteconfgenLog));
      try {
        String line = bufferedReader.readLine();
        if ((line == null) || !line.contains("RTEConfGen Tool Version :")) {
          return true;
        }
        line = line.trim();
        line = line.split("RTEConfGen Tool Version : ")[1];
        if (!line.equals(RteConfigGeneratorConstants.TOOL_VERSION)) {
          return true;
        }
      }
      finally {
        bufferedReader.close();
      }

      bVersionChanged = Files.lines(Paths.get(string)).anyMatch(l -> l.contains("MM_DCS_RTECONFGEN_399"));
      return bVersionChanged;

    }

    return true;
  }

  /**
   * @param bVersionchanged
   * @param memmapInputList
   * @param prjPath
   * @param props
   * @param rteconfiggenDir
   * @return
   * @throws Exception
   */
  private void writeGatingForSafeguard(final boolean bVersionchanged, final List<File> memmapInputList,
      final String prjPath, final Map<String, String> props, final String rteconfiggenDir)
      throws Exception {
    boolean bchanged = false;
    for (File inputfile : memmapInputList) {
      bchanged = compareTimeWithOutputFile(inputfile,
          Arrays.asList(prjPath + props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST)));
      if (bchanged) {
        break;
      }
    }

    if (bchanged) {
      props.put(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_INPUTS_CHANGED, "y");
      removeExistingXPTFile(prjPath, rteconfiggenDir);
      removeExistingFile(prjPath, rteconfiggenDir + "/rteconfgen_memmap_xpt_files.lst");
    }
    else {
      props.put(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_INPUTS_CHANGED, "n");
    }
  }

  /**
   * @param inputFileList
   * @param outputfileList
   * @param props
   * @param prjPath
   * @param optFilepath
   * @param rteconfiggenDir
   * @throws Exception
   */
  private void writeGating(final boolean bVersionchanged, final String prjPath, final Map<String, String> props,
      final String optFilepath, final String rteconfiggenDir)
      throws Exception {

    List<File> inputFileList = new ArrayList<>();
    List<File> outputfileList = new ArrayList<>();
    List<File> memmapInputList = new ArrayList<>();

    if (props.get(RteConfigGeneratorConstants.ENABLE_MEMMAP_XPT_GENERATION).equals("y")) {

      memmapInputList.add(new File(prjPath + "\\" + optFilepath));
      memmapInputList
          .add(new File(prjPath + props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_CONFIG_FILEPATH)));
      memmapInputList.add(new File(prjPath +
          props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_TASKWISE_SW_COMPONENT_PROTOTYPE_LIST_FILEPATH)));
      memmapInputList.add(new File(prjPath + props.get(RteConfigGeneratorConstants.ENDSECTIONPATH)));
      memmapInputList.add(new File(prjPath + props.get(RteConfigGeneratorConstants.STARTSECTIONPATH)));
      writeGatingForSafeguard(bVersionchanged, memmapInputList, prjPath, props, rteconfiggenDir);

      inputFileList.addAll(memmapInputList);
      outputfileList
          .add(new File(prjPath + props.get(RteConfigGeneratorConstants.RTECONFGEN_MEMMAP_XPT_OUTPUT_FILEPATH_LIST)));
    }


    long latestModifiedInputFile = getLatestModifiedInputFile(inputFileList, prjPath, props, optFilepath);
    long latestModifiedOutputFile = getLatestModifiedOutputFile(outputfileList, prjPath, props);

    boolean bInputsChanged = false;
    bInputsChanged = !bVersionchanged
        ? (latestModifiedOutputFile == -1 ? true : (latestModifiedInputFile > latestModifiedOutputFile ? true : false))
        : false;

    if (bVersionchanged || bInputsChanged) {
      props.put(RteConfigGeneratorConstants.RTECONFGEN_INPUTS_CHANGED, "y");
    }
    else {
      props.put(RteConfigGeneratorConstants.RTECONFGEN_INPUTS_CHANGED, "n");
    }


  }


  /**
   * @param outputfileList
   * @param prjPath
   * @param props
   * @return
   */
  private long getLatestModifiedOutputFile(final List<File> outputfileList, final String prjPath,
      final Map<String, String> props) {

    List<File> mandatoryOutputFiles = new ArrayList<File>();

    Collections.addAll(mandatoryOutputFiles,
        new File(prjPath + props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FILE_OUTPUT_PATH)),
        new File(prjPath + props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_VIEW_FILE_OUTPUT_PATH)),
        new File(prjPath + props.get(RteConfigGeneratorConstants.ECU_EXTRACT_FLAT_MAP_FILE_OUTPUT_PATH)),
        new File(prjPath + props.get(RteConfigGeneratorConstants.ECUC_PARTITION_ECUC_VALUE_OUTPUT_PATH)),
        new File(prjPath + props.get(RteConfigGeneratorConstants.RTE_ECUC_VALUE_OUTPUT_PATH)),
        new File(prjPath + props.get(RteConfigGeneratorConstants.SYSTEM_EXTRACT_FILE_OUTPUT_PATH)),
        new File(prjPath + props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR) + "/rteconfgen_rteopts.txt"));

    boolean bmandatoryfiledeleted = mandatoryOutputFiles.stream().anyMatch(f -> !f.exists() || (f.lastModified() == 0));
    if (bmandatoryfiledeleted) {
      return -1;
    }

    outputfileList.addAll(mandatoryOutputFiles);

    File csxfrmfile = null;
    if ((csxfrmfile =
        new File(prjPath + props.get(RteConfigGeneratorConstants.RTE_RIPS_CSXFRM_ECUC_VALUE_OUTPUT_PATH))) != null) {
      outputfileList.add(csxfrmfile);
    }

    if (props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR) != null) {
      File memmapDir =
          new File(prjPath + "/" + props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_MEMMAP_HEADER_FILES_DIR));
      File[] listFiles = memmapDir.listFiles();
      Collections.addAll(outputfileList, listFiles);
    }
    Collections.sort(outputfileList, Comparator.comparingLong(File::lastModified));

    return outputfileList.get(outputfileList.size() - 1).lastModified();
  }

  /**
   * @param inputFileList
   * @param prjPath
   * @param props
   * @param optFilepath
   * @return
   * @throws IOException
   */
  private long getLatestModifiedInputFile(final List<File> inputFileList, final String prjpath,
      final Map<String, String> props, final String optFilepath)
      throws IOException {

    Collections.addAll(inputFileList, new File(prjpath + "\\" + optFilepath),
        new File(prjpath + props.get(RteConfigGeneratorConstants.ALL_AUTOSAR_FILE_LIST)),
        new File(props.get(RteConfigGeneratorConstants.OVER_ALL_OS_CONFIG_INPUT_FILE)),
        new File(props.get(RteConfigGeneratorConstants.ADDITIONAL_INPUT_FILES_LIST)),
        new File(prjpath + props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR) + "/adj_autosar_files.lst"));

    addFilesFromLSTtoList(prjpath, prjpath + props.get(RteConfigGeneratorConstants.ALL_AUTOSAR_FILE_LIST),
        inputFileList);
    addFilesFromLSTtoList(prjpath,
        prjpath + props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_DIR) + "/adj_autosar_files.lst", inputFileList);
    addFilesFromLSTtoList(prjpath, props.get(RteConfigGeneratorConstants.ADDITIONAL_INPUT_FILES_LIST), inputFileList);

    Collections.sort(inputFileList, Comparator.comparingLong(File::lastModified));


    return inputFileList.get(inputFileList.size() - 1).lastModified();
  }

  /**
   * @param string
   * @param string
   * @param inputFileList
   */
  private void addFilesFromLSTtoList(final String prjpath, final String path, final List<File> inputFileList) {
    try (Stream<String> lines = Files.lines(Paths.get(path))) {
      lines.forEach(l -> inputFileList.add(new File(prjpath + "/" + l)));
    }
    catch (Exception e) {
      // do nothing
    }


  }

  /**
   * @param file
   * @param outputfileList
   * @return
   */
  private boolean compareTimeWithOutputFile(final File inputfile, final List<String> outputfileList) {
    long inputtimeStamp = inputfile.lastModified();
    boolean bchanged = false;

    for (String outfile : outputfileList) {
      long outtimestamp = new File(outfile).lastModified();
      if (outtimestamp < inputtimeStamp) {
        return true;
      }
    }
    return bchanged;
  }

  private void removeExistingFile(final String prjPath, final String relativepath) throws Exception {

    String path = prjPath + relativepath;
    String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
    path = path.substring(0, path.lastIndexOf("/"));

    File file = new File(path);

    if (file.exists() && file.isDirectory()) {
      File[] listFiles = file.listFiles();
      List<File> files = new ArrayList<File>();
      for (File f : listFiles) {
        if (f.getName().equalsIgnoreCase(fileName)) {
          files.add(f);
        }

      }

      for (File f : files) {
        boolean delete = f.delete();
        if (delete) {
          if (LOGGER != null) {
            LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("119_0", f.getAbsolutePath()).trim());
          }

        }
      }
    }
  }

  private void removeExistingXPTFile(final String prjPath, final String rteconfgendir) throws Exception {

    String path = prjPath + rteconfgendir;

    File file = new File(path);

    if (file.exists() && file.isDirectory()) {
      File[] listFiles = file.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
          return name.toLowerCase().endsWith("ifx.xpt") || name.toLowerCase().endsWith("st.xpt") ||
              name.toLowerCase().endsWith("jdp.xpt") || name.toLowerCase().endsWith("nxp.xpt");
        }
      });
      List<File> files = new ArrayList<File>();
      for (File f : listFiles) {
        files.add(f);
      }

      for (File f : files) {
        boolean delete = f.delete();
        if (delete) {
          LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("119_0", f.getAbsolutePath()).trim());
        }
      }
    }
  }

  // /**
  // * @param srcFolder as IContainer
  // */
  // private void refreshFolder(final IContainer srcFolder) {
  // try {
  // srcFolder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
  // }
  // catch (CoreException e) {
  // //
  // }
  // }
  //
  private void waitForProjectBuild(final IProgressMonitor monitor, final IProject project) throws Exception {

    BuildStartListener bulBuildStartListener = null;
    if (project != null) {
      bulBuildStartListener = waitForBuildJobToStart(project);
    }

    IJobManager jobManager = Job.getJobManager();
    try {
      if ((jobManager.find(ResourcesPlugin.FAMILY_AUTO_BUILD).length > 0) ||
          (jobManager.find(ResourcesPlugin.FAMILY_MANUAL_BUILD).length > 0) ||
          (jobManager.find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING).length > 0)) {


        jobManager.join(IExtendedPlatformConstants.FAMILY_MODEL_LOADING, SubMonitor.convert(monitor, 100));
        jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, SubMonitor.convert(monitor, 100));
        jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, SubMonitor.convert(monitor, 100));
      }
    }
    catch (OperationCanceledException e) {
      LOGGER.error("OperationCanceledException occured ");
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());

    }
    catch (InterruptedException e) {
      LOGGER.error("InterruptedException occured ");
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
    }
    finally {
      if (bulBuildStartListener != null) {
        Job.getJobManager().removeJobChangeListener(bulBuildStartListener);
        LOGGER.info("*** Build completed");
      }
    }
    monitor.done();
  }


  private BuildStartListener waitForBuildJobToStart(final IProject project) throws Exception {

    BuildStartListener startListener = new BuildStartListener(project);
    Job.getJobManager().addJobChangeListener(startListener);

    Timer timer = new Timer();

    timer.schedule(new TimerTask() {

      @Override
      public void run() {
        startListener.setIsBuildJobScheduled(true);
        LOGGER.info("*** Time out");
      }
    }, 60000);


    while (!startListener.isBuildJobScheduled()) {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {

        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(e)).trim());
      }
    }
    if (startListener.isBuildJobScheduled()) {
      LOGGER.info("*** Build started");
      timer.cancel();
    }
    return startListener;
  }

  class BuildStartListener extends JobChangeAdapter {

    /**
     * @param project The project for which the build listener is attached
     */
    public BuildStartListener(final IProject project) {
      this.project = project;
    }

    private volatile boolean buildJobScheduled = false;

    IProject project = null;

    @Override
    public void running(final IJobChangeEvent event) {

      LOGGER.info("*** Running " + event.getJob().getName());

      if (event.getJob().belongsTo(ResourcesPlugin.FAMILY_AUTO_BUILD)) {
        this.buildJobScheduled = true;
        // Job.getJobManager().removeJobChangeListener(this);
      }

    }

    /**
     * Method to indicate if build has started
     *
     * @return True if build has started
     */
    public boolean isBuildJobScheduled() {
      return this.buildJobScheduled;
    }

    public void setIsBuildJobScheduled(final boolean buildJobScheduled) {
      this.buildJobScheduled = buildJobScheduled;
    }
  }


  private void updateUriEObjectsMap() {

    List<IFile> listOfFilesAdded = MetadataUtil.getInstance().getListOfFilesAdded();

    for (IFile iFile : listOfFilesAdded) {
      Resource resource = EcorePlatformUtil.getResource(iFile);
      if (resource != null) {
        TreeIterator<EObject> iterator = resource.getAllContents();
        while (iterator.hasNext()) {
          EObject eObject = iterator.next();
          String fragment = EcoreUtil.getURI(eObject).fragment();
          fragment = fragment.substring(0, fragment.lastIndexOf("?type="));

          if (fragment.length() > 1) {


            if ((this.uriEObjectsMap.get(fragment) != null) && (this.uriEObjectsMap.get(fragment).size() > 0)) {

              if (!this.uriEObjectsMap.get(fragment).contains(eObject)) {
                this.uriEObjectsMap.get(fragment).add(eObject);
              }
            }
            else {
              List<EObject> eObjList = new ArrayList<>();
              eObjList.add(eObject);
              this.uriEObjectsMap.put(fragment, eObjList);
            }
          }
        }
      }
    }

  }

}
