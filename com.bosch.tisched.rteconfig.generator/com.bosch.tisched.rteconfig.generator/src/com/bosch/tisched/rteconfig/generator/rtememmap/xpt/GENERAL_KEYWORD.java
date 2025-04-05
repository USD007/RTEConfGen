/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.rtememmap.xpt;


/**
 * @author DTB1KOR
 */
public enum GENERAL_KEYWORD {

                             PRAGMA_SECTION,
                             SW_COMPONENT,
                             OS_APPLICATION,
                             BSW_MODULE_DESCRIPTION,
                             OS_TASK_NAME_PATTERN,
                             ALIGNMENT_OPERATOR_8BIT,
                             ALIGNMENT_OPERATOR_BOOLEAN,
                             ALIGNMENT_OPERATOR_16BIT,
                             ALIGNMENT_OPERATOR_32BIT,
                             ALIGNMENT_OPERATOR_64BIT;


  /**
   * @return Strings
   */
  public String getLiteral() {
    return toString();
  }

}
