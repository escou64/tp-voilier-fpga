/*
 * File: tx.scala
 * Created Date: 2022-09-17 07:44:30 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-14 10:58:07 am
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
import tp.common.tools.{Counter}


class Tx (p: UartParams) extends Module {
  import tp.uart.UartFSM._
  
  val io = IO(new Bundle {
    val i_cfg = Input(new UartConfigBus())
    val o_idle = Output(Bool())

    val b_in = Flipped(new GenRVIO(p, UInt(0.W), UInt(8.W)))    

    val o_tx = Output(Bool())
  })

  val r_fsm = RegInit(s0IDLE)

  val r_data = Reg(Vec(8, Bool()))
  val w_parity = r_data.asUInt.xorR

  io.b_in.ready := (r_fsm === s0IDLE)

  // ******************************
  //            CONFIG
  // ******************************
  val r_cfg = Reg(new UartConfigBus())

  when (r_fsm === s0IDLE) {
    r_cfg := io.i_cfg
  }

  io.o_idle := (r_fsm === s0IDLE)

  // ******************************
  //           COUNTERS
  // ******************************
  val m_ccnt = Module(new Counter(32))
  val m_dcnt = Module(new Counter(3))

  m_ccnt.io.i_limit := r_cfg.ncycle
  m_ccnt.io.i_init := true.B
  m_ccnt.io.i_en := false.B

  m_dcnt.io.i_limit := 0.U
  m_dcnt.io.i_init := true.B
  m_dcnt.io.i_en := false.B

  // ******************************
  //              FSM
  // ******************************
  // ------------------------------
  //            DEFAULT
  // ------------------------------
  io.o_tx := BIT.IDLE.U
  r_fsm := s0IDLE

  switch (r_fsm) {
    // ------------------------------
    //             IDLE
    // ------------------------------
    is(s0IDLE) {
      io.o_tx := BIT.IDLE.U

      r_fsm := Mux(io.b_in.valid, s1START, s0IDLE)
      r_data := io.b_in.data.get.asBools
    }
    // ------------------------------
    //             START
    // ------------------------------
    is(s1START) {
      io.o_tx := BIT.START.U
      when (m_ccnt.io.o_flag) {
        r_fsm := s2DATA
        m_ccnt.io.i_init := true.B
        m_ccnt.io.i_en := false.B
      }.otherwise {
        r_fsm := s1START
        m_ccnt.io.i_init := false.B
        m_ccnt.io.i_en := true.B
      }
    }
    // ------------------------------
    //              DATA
    // ------------------------------
    is(s2DATA) {
      io.o_tx := r_data(m_dcnt.io.o_val)
      when (m_ccnt.io.o_flag) {
        m_ccnt.io.i_init := true.B
        m_ccnt.io.i_en := false.B
        when ((r_cfg.is8bit & (m_dcnt.io.o_val === 7.U)) | (~r_cfg.is8bit & (m_dcnt.io.o_val === 6.U))) {
          m_dcnt.io.i_init := true.B
          m_dcnt.io.i_en := false.B
          when (r_cfg.parity) {
            r_fsm := s3PARITY
          }.elsewhen (r_cfg.stop =/= 0.U) {
            r_fsm := s4STOP0
          }.otherwise {
            r_fsm := s6VALID
          }
        }.otherwise {
          r_fsm := s2DATA
          m_dcnt.io.i_init := false.B
          m_dcnt.io.i_en := true.B
        }
      }.otherwise {
        r_fsm := s2DATA
        m_ccnt.io.i_init := false.B
        m_ccnt.io.i_en := true.B
        m_dcnt.io.i_init := false.B
        m_dcnt.io.i_en := false.B
      }
    }
    // ------------------------------
    //             PARITY
    // ------------------------------
    is(s3PARITY) {
      io.o_tx := w_parity
      when (m_ccnt.io.o_flag) {
        r_fsm := Mux(r_cfg.stop =/= 0.U, s4STOP0, s6VALID)
        m_ccnt.io.i_init := true.B
        m_ccnt.io.i_en := false.B
      }.otherwise {
        r_fsm := s3PARITY
        m_ccnt.io.i_init := false.B
        m_ccnt.io.i_en := true.B
      }
    }
    // ------------------------------
    //             STOP 0
    // ------------------------------
    is(s4STOP0) {
      io.o_tx := BIT.STOP.U
      when (m_ccnt.io.o_flag) {
        r_fsm := Mux(r_cfg.stop === 2.U, s5STOP1, s6VALID)
        m_ccnt.io.i_init := true.B
        m_ccnt.io.i_en := false.B
      }.otherwise {
        r_fsm := s4STOP0
        m_ccnt.io.i_init := false.B
        m_ccnt.io.i_en := true.B
      }
    }
    // ------------------------------
    //             STOP 1
    // ------------------------------
    is(s5STOP1) {
      io.o_tx := BIT.STOP.U
      when (m_ccnt.io.o_flag) {
        r_fsm := s6VALID
        m_ccnt.io.i_init := true.B
        m_ccnt.io.i_en := false.B
      }.otherwise {
        r_fsm := s5STOP1
        m_ccnt.io.i_init := false.B
        m_ccnt.io.i_en := true.B
      }
    }
    // ------------------------------
    //             VALID
    // ------------------------------
    is(s6VALID) {
      io.o_tx := BIT.IDLE.U
      r_fsm := s0IDLE
    }
  }
}

object Tx extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Tx(UartConfigBase), args)
}
