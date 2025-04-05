package com.bosch.tisched.rteconfig.generator;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.bosch.tisched.rteconfig.generator.util.MetadataUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator
    extends Plugin /* extends AbstractUIPlugin implements IMetadataChangeListener, IResourceChangeListener */ {

  // private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(Activator.class.getName());

  /**
   *
   */
  public static final String PLUGIN_ID = "com.bosch.tisched.rteconfig.generator";

  // The shared instance
  private static Activator plugin;

  /**
   * The constructor
   */
  public Activator() {}

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    // ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_DELETE);
    // MetadataPlugin.getMetadataWorkspace().addListener(this);
    // MetadataPlugin.getMetadataWorkspace().addListener(ResourceScopeValueRegistry.INSTANCE);
    // ResourcesPlugin.getWorkspace().addResourceChangeListener(ResourceScopeValueRegistry.INSTANCE);
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
    // ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    MetadataUtil.getInstance().clearList();
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  /*
   * @Override public void metadataChanged(final IMetadataChangeEvent event) { // only access if change type is a post
   * change if (MetadataChangeType.POST_CHANGE.equals(event.getType())) { final IMetadataDelta metadataDelta =
   * event.getDelta(); if ((metadataDelta != null)) { final List<IFile> files = new ArrayList<IFile>();
   * IMetadataDeltaVisitor visitor = new IMetadataDeltaVisitor() {
   * @SuppressWarnings("restriction")
   * @Override public boolean visit(final IMetadataDelta delta) throws CoreException { IMetadata metadata =
   * delta.getMetadata(); if (metadata instanceof RootMetadata) {
   * MetadataUtil.getInstance().filterFilesToLoad(metadata); } if ((metadata != null) &&
   * metadata.isType(MetadataType.FILE_ELEMENT)) { if (MetadataUtil.isFileArtifactClassAUTOSAR(metadata)) { final IFile
   * file = (IFile) metadata.getResource(); if ((file != null) &&
   * RteConfigProjectResourceScope.hasProjectAutosarNature(file)) { files.add(file); } } } return true; } }; try {
   * metadataDelta.accept(visitor, EnumSet.of(MetadataDeltaKind.ADD)); ModelLoadManager.INSTANCE.loadFiles(files, true,
   * new NullProgressMonitor()); } catch (Exception e) { LOGGER.error(e.getMessage()); } } } }
   * @Override public void resourceChanged(final IResourceChangeEvent event) { // TODO Auto-generated method stub }
   */

}
