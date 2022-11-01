/*
 * File: bus.scala                                                             *
 * Created Date: 2022-11-01 06:27:51 pm                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2022-11-01 09:33:03 pm                                       *
 * Modified By: Mathieu Escouteloup                                            *
 * -----                                                                       *
 * License: See LICENSE.md                                                     *
 * Copyright (c) 2022 ISARD                                                    *
 * -----                                                                       *
 * Description:                                                                *
 */


package tp.spi

import chisel3._
import chisel3.util._
import scala.math._

import tp.common.gen._


// ******************************
//              SPI
// ******************************
class SpiConfigBus(nBit: Int, nSlave: Int) extends Bundle {
  val cpol = Bool()
  val cpha = Bool()
  val ncycle = UInt(nBit.W)
  val slave = UInt(log2Ceil(nSlave).W)
  val big = Bool()
}

class SpiIO (nSlave: Int) extends Bundle {
  val csn = Output(Vec(nSlave , Bool()))
  val sclk = Output(Bool())
  val miso = Input(Bool())
  val mosi = Output(Bool())
}

// ******************************
//             MASTER
// ******************************
class SpiMasterCtrlBus extends Bundle {
  val cmd = UInt(CMD.NBIT.W)
  val mul = Bool()
}

class SpiMasterIO(p: GenParams) extends Bundle {
  val req = Flipped(new GenRVIO(p, UInt(CMD.NBIT.W), UInt(8.W)))
  val ack = new GenRVIO(p, UInt(0.W), UInt(8.W))
}