/*
 * File: params.scala                                                          *
 * Created Date: 2022-11-01 06:27:51 pm                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2022-11-01 08:04:09 pm                                       *
 * Modified By: Mathieu Escouteloup                                            *
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

import tp.common.gen._


trait SpiParams {
  def debug: Boolean
  def nDataBit: Int
  def nSlave: Int 
}

case class SpiConfig (
  debug: Boolean,
  nDataBit: Int,
  nSlave: Int 
) extends SpiParams
