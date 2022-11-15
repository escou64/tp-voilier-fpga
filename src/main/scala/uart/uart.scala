/*
 * File: uart.scala
 * Created Date: 2022-09-17 07:44:30 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-14 02:01:57 pm
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


class Uart(p: UartParams) extends Module {
  require ((p.nDataByte <= 8), "Uart allows to simultaneously read or write only 8 or less bytes.")
  
  val io = IO(new Bundle {
    val o_idle = Output(Bool())
    val o_irq = Output(Bool())
    val i_cfg = if (!p.useRegMem) Some(Input(new UartConfigBus())) else None

    val b_regmem = if (p.useRegMem) Some(new UartRegMemIO(p, p.nDataByte)) else None
    val b_write = if (!p.useRegMem) Some(Vec(p.nDataByte, Flipped(new GenRVIO(p, UInt(0.W), UInt(8.W))))) else None
    val b_read = if (!p.useRegMem) Some(Vec(p.nDataByte, new GenRVIO(p, Bool(), UInt(8.W)))) else None

    val b_uart = new UartIO()
  })

  val m_write = Module (new GenFifo(p, UInt(0.W), UInt(8.W), 3, p.nBufferDepth, p.nDataByte, 1))
  val m_tx = Module(new Tx(p))
  val m_rx = Module(new Rx(p))
  val m_read = Module (new GenFifo(p, Bool(), UInt(8.W), 3, p.nBufferDepth, 1, p.nDataByte))

  val w_cfg = Wire(new UartConfigBus())

  // ******************************
  //            CONFIG
  // ******************************
  val init_cfg = Wire(new UartConfigBus())

  init_cfg.is8bit := true.B
  init_cfg.parity := true.B
  init_cfg.stop := 1.U
  init_cfg.irq := IRQ.B1
  init_cfg.ncycle := DontCare

  val r_cfg = RegInit(init_cfg)

  if (p.useRegMem) {
    w_cfg := r_cfg
  } else {
    w_cfg := io.i_cfg.get
  }

  if (p.useRegMem) {
    // ------------------------------
    //             READ
    // ------------------------------
    val w_sent = ~m_write.io.b_out(0).valid & m_tx.io.o_idle
    val w_full = Wire(Vec(8, Bool()))
    val w_av = Wire(Vec(8, Bool()))
  
    for (b <- 0 until 8) {
      w_full(b) := false.B
      w_av(b) := false.B
    }
  
    for (b <- 0 until p.nDataByte) {
      w_full(b) := ~m_write.io.b_in(b).ready
    }
  
    for (rb <- 0 until p.nDataByte) {
      w_av(rb) := m_read.io.o_val(rb).valid
    }

    io.b_regmem.get.status := Cat(  0.U(8.W),
                                    0.U(7.W), w_sent,
                                    w_full.asUInt,
                                    w_av.asUInt)

    io.b_regmem.get.config := Cat(  0.U((8 - IRQ.NBIT).W), r_cfg.irq,
                                    0.U(8.W),
                                    0.U(8.W),
                                    0.U(4.W), r_cfg.stop, r_cfg.parity, r_cfg.is8bit)
    io.b_regmem.get.ncycle := r_cfg.ncycle
  
    // ------------------------------
    //             WRITE
    // ------------------------------
    when (io.b_regmem.get.wen(0)) {
      r_cfg.is8bit := io.b_regmem.get.wdata(0)
      r_cfg.parity := io.b_regmem.get.wdata(1)
      r_cfg.stop := io.b_regmem.get.wdata(3, 2)
      r_cfg.irq := io.b_regmem.get.wdata(24 + IRQ.NBIT - 1, 24)
    }
  
    when (io.b_regmem.get.wen(1)) {
      r_cfg.ncycle := io.b_regmem.get.wdata
    }  
  }

  // ******************************
  //              TX
  // ******************************
  // ------------------------------
  //             FIFO
  // ------------------------------
  m_write.io.i_flush := false.B

  if (p.useRegMem) {
    io.b_regmem.get.write <> m_write.io.b_in
  } else {
    io.b_write.get <> m_write.io.b_in
  } 

  // ------------------------------
  //           CONNECT
  // ------------------------------
  io.o_idle := ~m_write.io.b_in(0).valid & m_tx.io.o_idle
  m_tx.io.i_cfg := w_cfg
  
  m_write.io.b_out(0) <> m_tx.io.b_in

  io.b_uart.tx := m_tx.io.o_tx

  // ******************************
  //              RX
  // ******************************  
  // ------------------------------
  //           CONNECT
  // ------------------------------
  m_rx.io.i_cfg := w_cfg

  m_rx.io.i_rx := io.b_uart.rx

  m_read.io.b_in(0) <> m_rx.io.b_out

  // ------------------------------
  //             FIFO
  // ------------------------------
  m_read.io.i_flush := false.B

  if (p.useRegMem) {
    m_read.io.b_out <> io.b_regmem.get.read
  } else {
    m_read.io.b_out <> io.b_read.get
  }

  // ******************************
  //           INTERRUPT
  // ******************************
  io.o_irq := false.B

  switch (w_cfg.irq) {
    is (IRQ.B1)   { io.o_irq := m_read.io.o_val(0).valid}
    is (IRQ.B2)   { io.o_irq := m_read.io.o_val(1).valid}
    is (IRQ.B4)   { io.o_irq := m_read.io.o_val(3).valid}
    is (IRQ.B8)   {
      if (p.nDataByte >= 8) {
                    io.o_irq := m_read.io.o_val(7).valid
      }      
    }
  }
}

object Uart extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Uart(UartConfigBase), args)
}
