/*
 * File: bus.scala
 * Created Date: 2022-11-01 06:27:51 pm                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2022-11-14 10:52:51 am
 * Modified By: Mathieu Escouteloup
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

import tp.common.bus._
import tp.common.gen._


// ******************************
//              SPI
// ******************************
class SpiConfigBus(nSlave: Int) extends Bundle {
  val cpol = Bool()
  val cpha = Bool()
  val mode = UInt(MODE.NBIT.W)
  val ncycle = UInt(32.W)
  val slave = UInt(log2Ceil(nSlave).W)
  val big = Bool()
  val irq = UInt(IRQ.NBIT.W)
}

class SpiIO (nSlave: Int) extends Bundle {
  val csn = Output(Vec(nSlave , Bool()))
  val sclk = Output(Bool())
  val data = new BiDirectIO(UInt(4.W))
}

// ******************************
//        MEMORY REGISTER
// ******************************
class SpiRegMemIO(p: GenParams, nDataByte: Int) extends Bundle {
  val wen = Input(Vec(3, Bool()))  
  val wdata = Input(UInt(32.W))    

  val status = Output(UInt(32.W))
  val config = Output(UInt(32.W))
  val ncycle = Output(UInt(32.W))
  
  val creq = Vec(nDataByte, Flipped(new GenRVIO(p, new SpiMasterCtrlBus(), UInt(0.W))))
  val dreq = Vec(nDataByte, Flipped(new GenRVIO(p, UInt(0.W), UInt(8.W))))
  val read = Vec(nDataByte, new GenRVIO(p, UInt(0.W), UInt(8.W)))
}

// ******************************
//             MASTER
// ******************************
class SpiMasterCtrlBus extends Bundle {
  val cmd = UInt(CMD.NBIT.W)
  val mb = Bool()
}

class SpiMasterIO(p: GenParams) extends Bundle {
  val req = Flipped(new GenRVIO(p, new SpiMasterCtrlBus(), UInt(8.W)))
  val ack = new GenRVIO(p, UInt(0.W), UInt(8.W))
}