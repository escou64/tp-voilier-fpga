/*
 * File: bus.scala
 * Created Date: 2022-09-17 07:44:30 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-14 10:54:33 am
 * Modified By: Mathieu Escouteloup
 * -----
 * License: See LICENSE.md
 * Copyright (c) 2022 ISARD
 * -----
 * Description: 
 */


package tp.uart

import chisel3._
import chisel3.util._
import scala.math._

import tp.common.gen._


// ******************************
//           MAIN BUS
// ******************************
class UartIO extends Bundle {
  val tx = Output(Bool())
  val rx = Input(Bool())
}

// ******************************
//        MEMORY REGISTER
// ******************************
class UartRegMemIO(p: GenParams, nDataByte: Int) extends Bundle {
  val wen = Input(Vec(3, Bool()))  
  val wdata = Input(UInt(32.W))    

  val status = Output(UInt(32.W))
  val config = Output(UInt(32.W))
  val ncycle = Output(UInt(32.W))

  val write = Vec(nDataByte, Flipped(new GenRVIO(p, UInt(0.W), UInt(8.W))))
  val read = Vec(nDataByte, new GenRVIO(p, Bool(), UInt(8.W)))
}

// ******************************
//         INTERFACE BUS
// ******************************
class UartConfigBus extends Bundle {
  val is8bit = Bool()
  val parity = Bool()
  val stop = UInt(2.W)
  val irq = UInt(IRQ.NBIT.W)
  val ncycle = UInt(32.W)
}

// ******************************
//           DEBUG BUS
// ******************************
class TxDbgBus (nCycleBit: Int) extends Bundle {
  val in_valid = Bool()
  val in_data = UInt(8.W)
  val fsm = UartFSM()
  val send_change = Bool()
  val cycle = UInt(nCycleBit.W)
  val end = Bool()
}

class RxDbgBus extends Bundle {
  val tbit = Bool()     // Toggle bit
  val tword = Bool()    // Toggle word
  val fsm = UartFSM()
}

class UartDbgBus (nCycleBit: Int) extends Bundle {
  val tx = new TxDbgBus(nCycleBit)
  val rx = new RxDbgBus()
}
