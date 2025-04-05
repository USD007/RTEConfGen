/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.rtememmap.xpt;


/**
 * @author DTB1KOR
 */

public enum SECTION_KEYWORD {


                             SHORT_NAME,
                             OPTIONAL_KEYWORD_1,
                             OPTIONAL_KEYWORD_2,
                             INITIAL_KEYWORD_1,
                             FINAL_KEYWORD_1,
                             INITIAL_KEYWORD_2,
                             LINKER_FLAG_KEYWORD,
                             FINAL_KEYWORD_2,
                             DEFAULT_INITIAL_KEYWORD_1,
                             DEFAULT_INITIAL_KEYWORD_2;


  /**
   * @return Strings
   */
  public String getLiteral() {
    return toString();
  }

}

