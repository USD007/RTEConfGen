/*
 ************************************************************************* 
 *                                                                       * 
 *                      ROBERT BOSCH GMBH                                * 
 *                          STUTTGART                                    * 
 *                                                                       * 
 *          Alle Rechte vorbehalten - All rights reserved                * 
 *                                                                       *
 *                  Generated by RTEConfGen                              * 
 ************************************************************************* 
*/
   
#if defined <COMPONENT_UC>_START_SEC_CODE
#undef <COMPONENT_UC>_START_SEC_CODE
#define RTECONFGEN_START_SEC_<OS_APPLICATION>_CODE
#include "RTEConfGen_MemMap.h"

#elif defined <COMPONENT_UC>_STOP_SEC_CODE
#undef <COMPONENT_UC>_STOP_SEC_CODE
#define RTECONFGEN_STOP_SEC_<OS_APPLICATION>_CODE
#include "RTEConfGen_MemMap.h"

#elif defined <COMPONENT_CC>_START_SEC_CODE
#undef <COMPONENT_CC>_START_SEC_CODE
#define RTECONFGEN_START_SEC_<OS_APPLICATION>_CODE
#include "RTEConfGen_MemMap.h"

#elif defined <COMPONENT_CC>_STOP_SEC_CODE
#undef <COMPONENT_CC>_STOP_SEC_CODE
#define RTECONFGEN_STOP_SEC_<OS_APPLICATION>_CODE
#include "RTEConfGen_MemMap.h"

#else
#error "<COMPONENT_CC>_MemMap.h, wrong pragma command" 
#endif 
