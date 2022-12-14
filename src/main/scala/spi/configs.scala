/*
 * File: configs.scala
 * Created Date: 2022-11-01 06:27:51 pm                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2022-11-14 09:38:54 am
 * Modified By: Mathieu Escouteloup
 * -----                                                                       *
 * License: See LICENSE.md                                                     *
 * Copyright (c) 2022 ISARD                                                    *
 * -----                                                                       *
 * Description:                                                                *
 */


package tp.spi

import chisel3._
import chisel3.experimental.IO
import chisel3.util._


object SpiMasterConfigBase extends SpiMasterConfig (
  debug = true,
  nDataByte = 4,
  nSlave = 1,
  useRegMem = true,
  nBufferDepth = 8
)

