/*
 * File: params.scala
 * Created Date: 2022-11-01 06:27:51 pm                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2022-11-14 10:53:58 am
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

import tp.common.gen._


trait SpiMasterParams extends GenParams {
  def debug: Boolean
  def nDataByte: Int
  def nDataBit: Int = nDataByte * 8
  def nSlave: Int 
  def useRegMem: Boolean
  def nBufferDepth: Int
}

case class SpiMasterConfig (
  debug: Boolean,
  nDataByte: Int,
  nSlave: Int,
  useRegMem: Boolean,
  nBufferDepth: Int
) extends SpiMasterParams
