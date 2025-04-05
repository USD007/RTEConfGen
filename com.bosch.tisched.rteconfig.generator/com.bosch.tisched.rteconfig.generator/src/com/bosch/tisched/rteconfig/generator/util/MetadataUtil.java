/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;


/**
 * @author shk1cob
 */
public class MetadataUtil {

  private static final Logger LOGGER = RteConfigGeneratorLogger.getLogger(MetadataUtil.class.getName());


  private final List<IFile> files = new ArrayList<IFile>();

  private static MetadataUtil metadataUtil = null;

  /**
   * @return MetadataUtil
   */
  public static MetadataUtil getInstance() {
    if (metadataUtil == null) {
      metadataUtil = new MetadataUtil();
    }
    return metadataUtil;
  }

  private MetadataUtil() {
    //
  }

  /**
   * @param iProject IProject
   * @param allFileList List<String>
   */
  public void filterFilesToLoad(final IProject iProject, final List<String> allFileList) {
    int i = 0;
    for (String filepath : allFileList) {

      IFile file = iProject.getFile(filepath);
      if ((file != null) && file.exists()) {
        System.out.println(i + "> Added for model loading : " + file.getName());
        this.files.add(file);
        i++;
      }
      else {
        System.out.println("File is missing : " + filepath);
      }


    }

  }


  /**
   * @param file IFile
   * @return boolean
   */
  public boolean isFileLoadable(final IFile file) {
    return (this.files != null) ? this.files.contains(file) : false;
  }

  /**
   *
   */
  public void clearList() {
    this.files.clear();
  }


  /**
   * @return int
   */
  public List<IFile> getListOfFilesAdded() {
    return this.files;
  }


  /**
   * @param file as IFile
   */
  private static void deleteFile(final IFile file) {

    try {
      file.delete(true, new NullProgressMonitor());
    }
    catch (CoreException e) {
      //
    }

  }


}
