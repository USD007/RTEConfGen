/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.core;


/**
 * @author DTB1KOR
 */
public enum TischedTaskTypeEnum {

                                 /**
                                 *
                                 */
                                 SOFTWARE,

                                 /**
                                 *
                                 */
                                 ISR,
                                 /**
                                  *
                                  */
                                 INIT;

  public String value() {
    return name();
  }

  public static TischedTaskTypeEnum fromValue(final String v) {
    return valueOf(v);
  }


}
