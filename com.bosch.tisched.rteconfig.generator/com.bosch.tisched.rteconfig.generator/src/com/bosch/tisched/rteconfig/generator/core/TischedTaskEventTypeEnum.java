/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package com.bosch.tisched.rteconfig.generator.core;

/**
 * @author DTB1KOR
 */
public enum TischedTaskEventTypeEnum {

                                      I,
                                      T,
                                      ITO,
                                      ETO,
                                      BG;

  public String value() {
    return name();
  }

  public static TischedTaskEventTypeEnum fromValue(final String v) {
    return valueOf(v);
  }

}
