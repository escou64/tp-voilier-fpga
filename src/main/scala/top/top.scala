/*
 * File: top.scala
 * Created Date: 2022-11-14 01:36:00 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-15 03:01:39 pm
 * Modified By: Mathieu Escouteloup
 * -----
 * License: See LICENSE.md
 * Copyright (c) 2022 ISARD
 * -----
 * Description: 
 */


package tp.top

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum
import scala.math._

import tp.uart.{Uart, UartIO, IRQ => UART_IRQ}
import tp.spi.{SpiMaster, SpiIO, IRQ => SPI_IRQ, MODE => SPI_MODE, CMD => SPI_CMD}


object TopFSM extends ChiselEnum {
  val s0IDLE, s1CMD, s2ADDR, s3READ = Value
}

class Top(p: TopParams) extends Module {  
  import tp.top.TopFSM._

  def ADDR_INIT: Int = 0x400000
  def ADDR_MAX: Int = 0x400100
  
  val io = IO(new Bundle {
    val i_trig = Input(Bool())
    val i_sel = Input(Bool())

    val b_uart = new UartIO()
    val b_spi = new SpiIO(p.pSpiMaster.nSlave)
  })

  val m_uart = Module(new Uart(p.pUart))
  val m_spi = Module(new SpiMaster(p.pSpiMaster))

  val r_fsm = RegInit(s0IDLE)
  val r_addr = RegInit(ADDR_INIT.U(24.W))
  val r_cnt = RegInit(0.U(4.W))

  // ******************************
  //             UART
  // ******************************
  m_uart.io.i_cfg.get.is8bit := true.B
  m_uart.io.i_cfg.get.parity := true.B
  m_uart.io.i_cfg.get.stop := 1.U
  m_uart.io.i_cfg.get.irq := UART_IRQ.B1
  m_uart.io.i_cfg.get.ncycle := 1042.U

  m_uart.io.b_uart <> io.b_uart

  for (b <- 0 until p.nDataByte) {
    m_uart.io.b_write.get(b) := DontCare
    m_uart.io.b_write.get(b).valid := false.B
    m_uart.io.b_read.get(b) := DontCare
    m_uart.io.b_read.get(b).ready := true.B
  }

  // ******************************
  //             SPI
  // ******************************
  // ------------------------------
  //           DEFAULT
  // ------------------------------
  m_spi.io.i_cfg.get.cpol := false.B
  m_spi.io.i_cfg.get.cpha := false.B
  m_spi.io.i_cfg.get.mode := SPI_MODE.BASE
  m_spi.io.i_cfg.get.slave := 0.U
  m_spi.io.i_cfg.get.big := true.B
  m_spi.io.i_cfg.get.irq := SPI_IRQ.B1
  m_spi.io.i_cfg.get.ncycle := 2.U

  for (b <- 0 until p.nDataByte) {
    m_spi.io.b_creq.get(b) := DontCare
    m_spi.io.b_creq.get(b).valid := false.B
    m_spi.io.b_dreq.get(b) := DontCare
    m_spi.io.b_dreq.get(b).valid := false.B
    m_spi.io.b_read.get(b) := DontCare
    m_spi.io.b_read.get(b).ready := true.B
  }

  m_spi.io.b_spi <> io.b_spi

  // ------------------------------
  //             FSM
  // ------------------------------
  switch(r_fsm) {
    is (s0IDLE) {
      r_cnt := 0.U
      when (m_spi.io.o_idle & m_uart.io.b_write.get(0).ready) {
        r_fsm := s1CMD
      }
    }

    is (s1CMD) {
      r_cnt := 0.U
      when (m_spi.io.b_creq.get(0).ready & m_spi.io.b_dreq.get(0).ready) {
        r_fsm := s2ADDR
      }      
    }

    is (s2ADDR) {
      when (m_spi.io.b_creq.get(0).ready & m_spi.io.b_dreq.get(0).ready) {
        when (r_cnt === 2.U) {
          r_fsm := s3READ
          r_cnt := 0.U
        }.otherwise {
          r_cnt := r_cnt + 1.U 
        }
      }
      
    }

    is (s3READ) {
      when (m_spi.io.b_creq.get(0).ready & m_spi.io.b_dreq.get(0).ready) {
        when ((r_addr + 1.U) >= ADDR_MAX.U) {
          r_fsm := s0IDLE
          r_addr := ADDR_INIT.U
          r_cnt := 0.U
        }.elsewhen(r_cnt === 4.U) {
          r_fsm := s0IDLE
          r_addr := r_addr + 9.U
          r_cnt := 0.U
        }.otherwise {
          r_addr := r_addr + 1.U
          r_cnt := r_cnt + 1.U 
        }
      }      
    }
  }

  // ------------------------------
  //           CONNECT
  // ------------------------------
  switch(r_fsm) {
    is (s1CMD)  {
      m_spi.io.b_creq.get(0).valid := m_spi.io.b_dreq.get(0).ready
      m_spi.io.b_creq.get(0).ctrl.get.cmd := SPI_CMD.W
      m_spi.io.b_creq.get(0).ctrl.get.mb := false.B
      m_spi.io.b_dreq.get(0).valid := m_spi.io.b_creq.get(0).ready
      m_spi.io.b_dreq.get(0).data.get := 3.U
    }
    is (s2ADDR) {
      m_spi.io.b_creq.get(0).valid := m_spi.io.b_dreq.get(0).ready
      m_spi.io.b_creq.get(0).ctrl.get.cmd := SPI_CMD.W
      m_spi.io.b_creq.get(0).ctrl.get.mb := true.B
      m_spi.io.b_dreq.get(0).valid := m_spi.io.b_creq.get(0).ready
      switch(r_cnt) {
        is (0.U) {m_spi.io.b_dreq.get(0).data.get := r_addr(23,  16)}
        is (1.U) {m_spi.io.b_dreq.get(0).data.get := r_addr(15,  8)}
        is (2.U) {m_spi.io.b_dreq.get(0).data.get := r_addr(7,   0)}
      }
    }
    is (s3READ) {
      m_spi.io.b_creq.get(0).valid := true.B
      m_spi.io.b_creq.get(0).ctrl.get.cmd := SPI_CMD.R
      m_spi.io.b_creq.get(0).ctrl.get.mb := true.B
      m_spi.io.b_dreq.get(0).valid := false.B
      m_spi.io.b_dreq.get(0).data.get := 0.U
    }
  }
  
  // ******************************
  //         SELECT OUTPUT
  // ******************************
  when (io.i_sel) {
    m_uart.io.b_write.get(0).valid := m_spi.io.b_read.get(0).valid
    m_uart.io.b_write.get(0).data.get := m_spi.io.b_read.get(0).data.get   
  }.otherwise {
    m_uart.io.b_write.get(0).valid := m_uart.io.b_read.get(0).valid
    m_uart.io.b_write.get(0).data.get := m_uart.io.b_read.get(0).data.get + 1.U     
  }
}

object Top extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Top(TopConfigBase), args)
}