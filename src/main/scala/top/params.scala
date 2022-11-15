/*
 * File: params.scala
 * Created Date: 2022-11-01 06:27:51 pm                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2022-11-14 01:59:44 pm
 * Modified By: Mathieu Escouteloup
 * -----                                                                       *
 * License: See LICENSE.md                                                     *
 * Copyright (c) 2022 ISARD                                                    *
 * -----                                                                       *
 * Description:                                                                *
 */


package tp.top

import chisel3._
import chisel3.experimental.IO
import chisel3.util._

import tp.common.gen._
import tp.uart._
import tp.spi._


trait TopParams extends GenParams {
  def debug: Boolean
  def nDataByte: Int
  def nDataBit: Int = nDataByte * 8

  def pUart: UartParams = new UartConfig (
    debug = debug,
    nDataByte = nDataByte,
    useRegMem = false,
    nBufferDepth = 8
  )

  def pSpiMaster: SpiMasterParams = new SpiMasterConfig (
    debug = debug,
    nDataByte = nDataByte,
    useRegMem = false,
    nSlave = 1,
    nBufferDepth = 8
  )
}

case class TopConfig (
  debug: Boolean,
  nDataByte: Int
) extends TopParams
