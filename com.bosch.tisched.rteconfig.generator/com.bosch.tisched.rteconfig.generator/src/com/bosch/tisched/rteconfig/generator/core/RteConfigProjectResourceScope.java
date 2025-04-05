/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.core;

import org.artop.aal.workspace.natures.AutosarNature;
import org.artop.aal.workspace.scoping.AutosarProjectResourceScope;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;

import com.bosch.tisched.rteconfig.generator.util.MetadataUtil;


/**
 * @author shk1cob
 */
public class RteConfigProjectResourceScope extends AutosarProjectResourceScope {


  /**
   * @param resource IResource
   */
  public RteConfigProjectResourceScope(final IResource resource) {
    super(resource);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean belongsTo(final IFile file, final boolean includeReferencedScopes) {
    if (MetadataUtil.getInstance().isFileLoadable(file)) {
      return super.belongsTo(file, includeReferencedScopes);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean belongsTo(final Resource resource, final boolean includeReferencedScopes) {


    if (MetadataUtil.getInstance().isFileLoadable(EcorePlatformUtil.getFile(resource))) {
      return super.belongsTo(resource, includeReferencedScopes);
    }
    return false;

  }

  /**
   * @param iFile IFile
   * @return boolean
   */
  public static boolean hasProjectAutosarNature(final IFile iFile) {
    boolean retVal = false;
    try {
      retVal = (iFile.getProject().getNature(AutosarNature.ID) != null) ? true : false;
    }
    catch (final CoreException e) {
      // fail silently
      retVal = false;
    }
    return retVal;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean belongsTo(final URI uri, final boolean includeReferencedScopes) {

    if (MetadataUtil.getInstance().isFileLoadable(EcorePlatformUtil.getFile(uri))) {
      return super.belongsTo(uri, includeReferencedScopes);
    }
    return false;

  }

}
