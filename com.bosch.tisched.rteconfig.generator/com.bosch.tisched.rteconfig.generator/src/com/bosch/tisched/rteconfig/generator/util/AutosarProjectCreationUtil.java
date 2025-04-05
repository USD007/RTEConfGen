/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.artop.aal.common.metamodel.AutosarReleaseDescriptor;
import org.artop.aal.workspace.natures.AutosarNature;
import org.artop.aal.workspace.preferences.IAutosarWorkspacePreferences;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;

import com.bosch.tisched.rteconfig.generator.service.AutosarProxyResolver;

import autosar40.genericstructure.generaltemplateclasses.identifiable.Referrable;


/**
 * @author shk1cob
 */
public class AutosarProjectCreationUtil {

  private static final String EXT_POINT_DOMAINS = "editingDomains"; //$NON-NLS-1$
  private static final String E_DOMAIN = "editingDomain"; //$NON-NLS-1$
  private static final String A_ID = "id"; //$NON-NLS-1$
  private static final String A_FACTORY = "factory"; //$NON-NLS-1$

  private static final String EXT_POINT_LISTENERS = "listeners"; //$NON-NLS-1$
  private static final String E_LISTENER = "listener"; //$NON-NLS-1$
  private static final String A_CLASS = "class"; //$NON-NLS-1$

  // private static IMetadata metadata = null;
  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(AutosarProjectCreationUtil.class.getName());
  private static final String Autosar40ResourceImpl = null;
  private final String[] natureIds = new String[] { AutosarNature.ID };

  // private final String[] natureIds = new String[] { "org.artop.aal.workspace.autosarnature" };
  // private final String[] natureIds =
  // new String[] { "org.artop.aal.workspace.autosarnature", "com.bosch.blueworx.core.blueworxnature" };
  private final String autosarReleaseVersion;
  private final String autosarResourceVersion;
  private final List<String> allFileList;
  private String rteConfGenlogDir;
  private final String ecuInstancePkgPath;
  private String logdir = "";
  private final String excludeDir;
  // private final String swComponentToBeReferred;


  /**
   * @param props
   * @param allFileList2
   */
  public AutosarProjectCreationUtil(final Map<String, String> props, final List<String> allFileList) {
    this.autosarReleaseVersion = props.get(RteConfigGeneratorConstants.AUTOSAR_RELEASE_VERSION);
    this.autosarResourceVersion = props.get(RteConfigGeneratorConstants.AUTOSAR_RESOURCE_VERSION);

    this.logdir = props.get(RteConfigGeneratorConstants.RTE_CONFIG_GEN_LOG);
    this.rteConfGenlogDir = this.logdir.substring(1, this.logdir.length()).substring(0, this.logdir.lastIndexOf("/"));
    this.rteConfGenlogDir = this.rteConfGenlogDir.endsWith("/")
        ? this.rteConfGenlogDir.substring(0, this.rteConfGenlogDir.length() - 1) : this.rteConfGenlogDir;
    this.allFileList = allFileList;
    this.ecuInstancePkgPath = props.get(RteConfigGeneratorConstants.ECU_INSTANCE_PKG_PATH);
    this.excludeDir = props.get(RteConfigGeneratorConstants.EXCLUDE_ARXML_PATH);
//    this.swComponentToBeReferred =
//        props.get(RteConfigGeneratorConstants.ECU_EXTRACT_REFER_COMPOSITION_SW_COMPONENT_TYPE);
  }


  /**
   * @param projectPath String
   * @param aswcToSwcpMap
   * @param pPortToRPortsMap
   * @param rPortToPPortsMap
   * @return IProject
   * @throws Exception
   */
  public IProject createAutosarProject(final String projectPath, final AssemblySwConnectionUtil assemblySwConnUtil)
      throws Exception {


    IProject iProject = null;
    AutosarProxyResolver.enableBuffering(true);

    // if (!loadWithBdomNature) {
    // this.natureIds = new String[] { AutosarNature.ID };
    // }

    IPath path = new Path(projectPath);
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    // String projectName = projectPath.substring(projectPath.lastIndexOf("\\") + 1, projectPath.length());
    String projectName = new File(projectPath).getName();
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

    for (final IProject project : projects) {
      LOGGER.info("*** Deleting existing projects");
      project.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
    }


    try {
      TimeUnit.SECONDS.sleep(1);
    }
    catch (InterruptedException e) {
      // do noting
    }


    IProjectDescription projectDescription = getProjectDescription(projectPath);

    if (projectDescription != null) {

      if (!((projectDescription.getBuildSpec().length == 0) && (projectDescription.getNatureIds().length == 1) &&
          projectDescription.getNatureIds()[0].equals(AutosarNature.ID))) {

        File file2 = path.append("/.projectrteconfgen").toFile();
        if (file2.exists()) {
          file2.delete();
        }
        projectDescription = createProjectDescription(projectName, projectPath, workspace, path);
      }

    }
    else {

      projectDescription = createProjectDescription(projectName, projectPath, workspace, path);
    }


    iProject = workspace.getRoot().getProject(projectDescription.getName());
    IStatus creationStatus = createProject(iProject, projectDescription);

    if (creationStatus.isOK()) {

      waitForProjectBuild(new NullProgressMonitor(), iProject);


      File file2 = path.append("/.projectrteconfgen").toFile();
      if (!file2.exists()) {

        try {
          InputStream resourceAsStream = getClass().getResource(".projectrteconfgen").openStream();

          byte[] targetArray = new byte[resourceAsStream.available()];
          resourceAsStream.read(targetArray);
          resourceAsStream.close();

          BufferedOutputStream bos =
              new BufferedOutputStream(new FileOutputStream(path.append("/.projectrteconfgen").toOSString()));

          byte[] bytes = new String(targetArray).replace("PVER", projectName).getBytes();
          bos.write(bytes);
          bos.flush();
          bos.close();


        }
        catch (Exception ex) {
          // do nothing
        }

      }

      LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : *** Computing proxy list");

      StringBuilder uriBuffer = AutosarProxyResolver.getURIBuffer();

      AutosarReleaseDescriptor autosarRelease = IAutosarWorkspacePreferences.AUTOSAR_RELEASE.get(iProject);
      TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(iProject, autosarRelease);


      EcoreUtil.resolveAll(editingDomain.getResourceSet());

      // wait for resolving proxies
      TimeUnit.SECONDS.sleep(3);


      List<Referrable> allInstancesOf =
          EObjectUtil.getAllInstancesOf(editingDomain.getResourceSet().getResources(), Referrable.class, false);
      Map<String, Referrable> refList = new HashMap<>();

      for (Referrable ref : allInstancesOf) {
        if (!ref.eIsProxy()) {
          refList.put(EcoreUtil.getURI(ref).fragment(), ref);
        }
      }


      if (uriBuffer.length() > 1) {
        LOGGER.info(
            "MM_DCS_RTECONFGEN_DEBUG : *** This PVER consists of proxy URIs which may impact RteConfgen execution");
        String[] split = uriBuffer.toString().split("<->");
        List<String> uriList = Arrays.asList(split);
        Collections.sort(uriList, new Comparator<String>() {

          @Override
          public int compare(final String s1, final String s2) {
            return s1.length() - s2.length();
          }
        });

        StringBuilder uriBuf = new StringBuilder();

        for (String uri : uriList) {
          if (refList.get(uri.substring(5)) == null) {
            uriBuf.append(uri);
            uriBuf.append(System.getProperty("line.separator"));
          }
        }
        LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : *** Storing proxy URIs into RteConfgen_ProxyURIList.txt");
        writeLogs(iProject, uriBuf, this.rteConfGenlogDir + "/" + "RteConfgen_ProxyURIList.txt");
      }

      AutosarProxyResolver.enableBuffering(false);
      refList.clear();

      assemblySwConnUtil.updatePRPortsMap(iProject, this.ecuInstancePkgPath, this.excludeDir);
      assemblySwConnUtil.updaterunnableToPortMap();
      StringBuilder log = AutosarMergeUtil.mergeAutosarElements(iProject, new ArrayList<Resource>());
      if (log.length() > 1) {
        LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : *** Storing splitable elments list into RteConfgen_MergedElements.txt");
        writeLogs(iProject, log, this.rteConfGenlogDir + "/" + "RteConfgen_MergedElements.txt");
      }

      LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("120_0").trim());


    }
    else

    {
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_3").trim());

      return null;
    }


    return iProject;

  }


  /**
   * @param project
   * @param uriBuffer
   * @param dirPath
   * @throws Exception
   */
  public static void writeLogs(final IProject project, final StringBuilder uriBuffer, final String dirPath)
      throws Exception {
    BufferedWriter writer = null;
    FileWriter fileWriter = null;
    try {

      String pathh = project.getLocation().toOSString() + "/" + dirPath;
//      String pathh = dirPath + "/" + fileName;
      String diPath = pathh.substring(0, pathh.lastIndexOf('/'));
      if (!new File(diPath).exists()) {

        boolean mkdir = new File(diPath).mkdirs();
        if (!mkdir) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("309_0", dirPath).trim());

        }
      }

      if (!new File(pathh).exists()) {

        boolean mkdir = new File(pathh).createNewFile();
        if (!mkdir) {
          RteConfigGeneratorLogger.logErrormessage(LOGGER,
              RteConfGenMessageDescription.getFormattedMesssage("310_0", pathh).trim());

        }

      }


      fileWriter = new FileWriter(pathh, false);
      writer = new BufferedWriter(fileWriter);
      writer.append(new StringBuilder(uriBuffer));
      writer.flush();
      LOGGER.info("MM_DCS_RTECONFGEN_DEBUG : *** " + pathh + " created. ");
    }
    catch (IOException ex) {
      LOGGER.error("IOException occured " + ex.getMessage());
      RteConfigGeneratorLogger.logErrormessage(LOGGER,
          RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());

    }
    finally {
      try {
        if (writer != null) {
          writer.close();
        }
        if (fileWriter != null) {
          fileWriter.close();
        }
      }
      catch (IOException ex) {
        LOGGER.error("IOException occured " + ex.getMessage());
        RteConfigGeneratorLogger.logErrormessage(LOGGER,
            RteConfGenMessageDescription.getFormattedMesssage("399_0", AutosarUtil.getFormattedMessage(ex)).trim());

      }
    }
  }


  private IProjectDescription createProjectDescription(final String projectName, final String projectPath,
      final IWorkspace workspace, final IPath path) {
    IProjectDescription projectDescription = workspace.newProjectDescription(projectName);
    projectDescription.setNatureIds(this.natureIds);
    projectDescription.setComment("RteConfgen tool specific project description");
    projectDescription.setLocation(path);
    return projectDescription;
  }


  private IStatus createProject(final IProject project, final IProjectDescription projectDescription) {
    IWorkspaceRunnable wsRun = new IWorkspaceRunnable() {

      @Override
      public void run(final IProgressMonitor monitor) throws CoreException {

        LOGGER.info("*** Creating Autosar project");
        project.create(projectDescription, monitor);
        project.open(monitor);


        ((Project) project).internalGetDescription().setBuildSpec(new ICommand[0]);
        ((Project) project).internalGetDescription().setNatureIds(AutosarProjectCreationUtil.this.natureIds);


        if (!AutosarProjectCreationUtil.this.allFileList.isEmpty()) {

          MetadataUtil.getInstance().filterFilesToLoad(project, AutosarProjectCreationUtil.this.allFileList);

          LOGGER.info("*** Metadata framework initialized");
          ModelLoadManager.INSTANCE.loadFiles(MetadataUtil.getInstance().getListOfFilesAdded(), true,
              new NullProgressMonitor());
        }


      }
    };

    try {
      ResourcesPlugin.getWorkspace().run(wsRun, project, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
    }
    catch (CoreException e) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER, e.getLocalizedMessage(), e);
      return Status.CANCEL_STATUS;

    }


    return Status.OK_STATUS;
  }


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
      RteConfigGeneratorLogger.logErrormessage(LOGGER, e.getLocalizedMessage(), e);

    }
    catch (InterruptedException e) {
      RteConfigGeneratorLogger.logErrormessage(LOGGER, e.getLocalizedMessage(), e);
    }
    finally {
      if (bulBuildStartListener != null) {
        Job.getJobManager().removeJobChangeListener(bulBuildStartListener);
        LOGGER.info(RteConfGenMessageDescription.getFormattedMesssage("199_1").trim());


      }
    }
    monitor.done();
  }


  private BuildStartListener waitForBuildJobToStart(final IProject project) {

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
        RteConfigGeneratorLogger.logErrormessage(LOGGER, e.getLocalizedMessage(), e);
      }
    }
    if (startListener.isBuildJobScheduled()) {
      LOGGER.info("*** Build Started");
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
      // else {
      // Job.getJobManager().cancel(event);
      // }
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

  private IProjectDescription getProjectDescription(final String projPath) throws CoreException {
    IProjectDescription projectDescription = null;
    try {
      projectDescription =
          ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(projPath + "/.projectrteconfgen"));

      if (projectDescription != null) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectDescription.getName());
        IProject[] array = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (int count = 0; count <= (array.length - 1); count++) {
          if (project.equals(array[count])) {
            array[count].close(new NullProgressMonitor());
            array[count].delete(true, new NullProgressMonitor());
          }
        }

      }
    }
    catch (Exception ex) {
      LOGGER.warn(
          "*** Exception is thrown while reading project description file hence recreating the project description file");
    }

    return projectDescription;
  }


}
