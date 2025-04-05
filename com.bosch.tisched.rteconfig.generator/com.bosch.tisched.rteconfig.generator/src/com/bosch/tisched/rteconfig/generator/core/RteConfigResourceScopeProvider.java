package com.bosch.tisched.rteconfig.generator.core;

import org.apache.logging.log4j.Logger;
import org.artop.aal.workspace.scoping.AutosarProjectResourceScopeProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.sphinx.emf.scoping.IResourceScopeProvider;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScope;

import com.bosch.tisched.rteconfig.generator.util.RteConfigGeneratorLogger;


/**
 * @author shk1cob
 */
public class RteConfigResourceScopeProvider extends AutosarProjectResourceScopeProvider
    implements IResourceScopeProvider {

  private static final Logger LOGGER =
      RteConfigGeneratorLogger.getLogger(RteConfigResourceScopeProvider.class.getName());


  /**
   * {@inheritDoc}
   */
  @Override
  protected final ProjectResourceScope createScope(final IResource resource) {
    return new RteConfigProjectResourceScope(resource);
  }

  /**
   * FDI-380:ToolId and MessageId is not set for the resource scope markers. So we are overriding the default behaviour
   * and creating the log entries so that for the out of scope resources we can attach message id and toolId.
   *
   * @param file the to be checked.
   * @return A Diagnostic.
   */
  @Override
  public final Diagnostic validate(final IFile file) {
    return super.validate(file);
  }

}
