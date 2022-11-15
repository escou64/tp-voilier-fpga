/*
 * File: master.scala
 * Created Date: 2022-11-01 06:27:51 pm                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2022-11-14 04:59:16 pm
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

import tp.common.gen._
import tp.common.tools.{Counter}


class SpiMasterCtrl (p: SpiMasterParams) extends Module {
  import tp.spi.SpiMasterFSM._

  val io = IO(new Bundle {
    val i_cfg = Input(new SpiConfigBus(p.nSlave))
    val o_idle = Output(Bool())

    val b_port = new SpiMasterIO(p)

    val b_spi = new SpiIO(p.nSlave)
  })

  val init_cfg = Wire(new SpiConfigBus(p.nSlave))

  init_cfg.cpol := 0.B
  init_cfg.cpha := 0.B
  init_cfg.mode := MODE.BASE
  init_cfg.ncycle := DontCare
  init_cfg.slave := 0.U
  init_cfg.big := true.B
  init_cfg.irq := DontCare

  val r_fsm = RegInit(s0IDLE)
  val r_cfg = RegInit(init_cfg)
  val r_ctrl = Reg(new SpiMasterCtrlBus())
  
  val r_csn = RegInit(VecInit(Seq.fill(p.nSlave)(1.B)))
  val r_sclk = RegInit(0.B)
  val r_rdata = Reg(UInt(8.W))
  val r_wdata = Reg(UInt(8.W))

  // ******************************
  //            COUNTERS
  // ******************************
  val m_ccnt = Module(new Counter(32))
  val m_dcnt = Module(new Counter(3))

  m_ccnt.io.i_limit := r_cfg.ncycle
  m_ccnt.io.i_init := (r_fsm === s0IDLE) | (r_fsm === s3END)
  m_ccnt.io.i_en := (r_fsm =/= s0IDLE) & (r_fsm =/= s3END)

  m_dcnt.io.i_limit := 0.U
  m_dcnt.io.i_init := true.B
  m_dcnt.io.i_en := false.B

  // ******************************
  //             SCLK
  // ******************************
  val w_half = (m_ccnt.io.o_val === ((r_cfg.ncycle >> 1.U) - 1.U))
  val w_full = (m_ccnt.io.o_val === (r_cfg.ncycle - 1.U))

  when ((r_fsm =/= s0IDLE) & (r_fsm =/= s1SYNC)) {
    when (w_half | w_full) {
      r_sclk := ~r_sclk
    }
  } .otherwise {
    r_sclk := 0.B
  }

  // ******************************
  //              FSM
  // ******************************
  switch(r_fsm){
    // ------------------------------
    //             IDLE
    // ------------------------------
    is(s0IDLE) {
      when (io.b_port.req.valid) {
        r_fsm := s1SYNC
        r_cfg := io.i_cfg
        r_ctrl := io.b_port.req.ctrl.get

        r_csn(io.i_cfg.slave) := false.B
        when (io.i_cfg.big) {
          r_wdata := Reverse(io.b_port.req.data.get)
        }.otherwise {
          r_wdata := io.b_port.req.data.get
        }
        
      }
    }

    // ------------------------------
    //          SYNCHRONIZE
    // ------------------------------
    is(s1SYNC) {
      when (m_ccnt.io.o_flag){
        r_fsm := s2DATA
      }
    }

    // ------------------------------
    //             DATA
    // ------------------------------
    is(s2DATA) {
      m_dcnt.io.i_init := false.B
      m_dcnt.io.i_en := false.B

      when (w_half) {
        switch (r_cfg.mode) {
          is (MODE.BASE) {
            when (r_cfg.big) {
              r_rdata := Cat(r_rdata(6, 0), io.b_spi.data.in(0))
            }.otherwise {
              r_rdata := Cat(io.b_spi.data.in(0), r_rdata(7, 1))
            }            
          }
          is (MODE.DUAL) {
            when (r_cfg.big) {
              r_rdata := Cat(r_rdata(5, 0), io.b_spi.data.in(1, 0))
            }.otherwise {
              r_rdata := Cat(io.b_spi.data.in(1, 0), r_rdata(7, 2))
            }            
          }
          is (MODE.QUAD) {
            when (r_cfg.big) {
              r_rdata := Cat(r_rdata(3, 0), io.b_spi.data.in(3, 0))
            }.otherwise {
              r_rdata := Cat(io.b_spi.data.in(3, 0), r_rdata(7, 4))
            }            
          }
        }
      }

      when (m_dcnt.io.o_val === 7.U) {
        when (m_ccnt.io.o_val === (r_cfg.ncycle - 2.U)) {
          m_dcnt.io.i_init := true.B
          r_fsm := s3END
        }
      }.otherwise {
        when (m_ccnt.io.o_flag) {
          m_dcnt.io.i_en := true.B
          switch (r_cfg.mode) {
            is (MODE.BASE) {  r_wdata := (r_wdata >> 1.U)}
            is (MODE.DUAL) {  r_wdata := (r_wdata >> 2.U)}
            is (MODE.QUAD) {  r_wdata := (r_wdata >> 4.U)}
          }
        }
      }
    }

    // ------------------------------
    //             END
    // ------------------------------
    is (s3END) {
      when (io.b_port.req.valid & (io.i_cfg.slave === r_cfg.slave) & io.b_port.req.ctrl.get.mb) {
        r_fsm := s2DATA
        r_ctrl := io.b_port.req.ctrl.get
        when (r_cfg.big) {
          r_wdata := Reverse(io.b_port.req.data.get)
        }.otherwise {
          r_wdata := io.b_port.req.data.get
        }
      }.elsewhen(io.b_port.req.valid & (io.i_cfg.slave =/= r_cfg.slave)) {
        r_fsm := s1SYNC
        r_cfg := io.i_cfg
        r_ctrl := io.b_port.req.ctrl.get

        r_csn(r_cfg.slave) := true.B
        r_csn(io.i_cfg.slave) := false.B
        when (io.i_cfg.big) {
          r_wdata := Reverse(io.b_port.req.data.get)
        }.otherwise {
          r_wdata := io.b_port.req.data.get
        }
      }.otherwise {
        r_fsm := s4DELAY
        
        r_csn(r_cfg.slave) := true.B
      }
    }

    // ------------------------------
    //            DELAY
    // ------------------------------
    is (s4DELAY) {
      when (io.b_port.req.valid & (io.i_cfg.slave === r_cfg.slave)) {
        r_fsm := s1SYNC
        r_cfg := io.i_cfg
        r_ctrl := io.b_port.req.ctrl.get

        r_csn(r_cfg.slave) := true.B
        r_csn(io.i_cfg.slave) := false.B
        when (io.i_cfg.big) {
          r_wdata := Reverse(io.b_port.req.data.get)
        }.otherwise {
          r_wdata := io.b_port.req.data.get
        }
      }.otherwise {
        when (m_ccnt.io.o_flag) {
          r_fsm := s0IDLE
        }
      }
    }
  }

  // ******************************
  //              I/Os
  // ******************************
  io.o_idle := (r_fsm === s0IDLE)
  io.b_spi.csn := r_csn
  io.b_spi.sclk := r_sclk

  io.b_spi.data.eno := 0.U
  io.b_spi.data.out := r_wdata
  switch (r_cfg.mode) {
    is (MODE.BASE) {  io.b_spi.data.eno := 1.U}
    is (MODE.DUAL) {  io.b_spi.data.eno := Mux(((r_ctrl.cmd === CMD.W) | (r_ctrl.cmd === CMD.RW)), 3.U,   0.U)}
    is (MODE.QUAD) {  io.b_spi.data.eno := Mux(((r_ctrl.cmd === CMD.W) | (r_ctrl.cmd === CMD.RW)), 15.U,  0.U)}
  }

  io.b_port.req.ready := (r_fsm === s0IDLE) | ((r_fsm === s3END) & (io.i_cfg.slave === r_cfg.slave) & io.b_port.req.ctrl.get.mb) | (((r_fsm === s3END) | (r_fsm === s4DELAY)) & (io.i_cfg.slave =/= r_cfg.slave))
  io.b_port.ack.valid := (r_fsm === s3END) & ((r_ctrl.cmd === CMD.R) | (r_ctrl.cmd === CMD.RW))
  io.b_port.ack.data.get := r_rdata

  // ******************************
  //             DEBUG
  // ******************************
  if (p.debug) {

  }
}

object SpiMasterCtrl extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SpiMasterCtrl(SpiMasterConfigBase), args)
}

class SpiMaster (p: SpiMasterParams) extends Module {
  require((p.nSlave <= 8), "Only 8 SPI salves can be connected to SpiMaster.")
  val io = IO(new Bundle {
    val o_idle = Output(Bool())
    val o_irq = Output(Bool())
    val i_cfg = if (!p.useRegMem) Some(Input(new SpiConfigBus(p.nSlave))) else None

    val b_regmem = if (p.useRegMem) Some(new SpiRegMemIO(p, p.nDataByte)) else None    
    val b_creq = if (!p.useRegMem) Some(Vec(p.nDataByte, Flipped(new GenRVIO(p, new SpiMasterCtrlBus(), UInt(0.W))))) else None
    val b_dreq = if (!p.useRegMem) Some(Vec(p.nDataByte, Flipped(new GenRVIO(p, UInt(0.W), UInt(8.W))))) else None
    val b_read = if (!p.useRegMem) Some(Vec(p.nDataByte, new GenRVIO(p, UInt(0.W), UInt(8.W)))) else None

    val b_spi = new SpiIO(p.nSlave)
  })

  val m_creq = Module(new GenFifo(p, new SpiMasterCtrlBus(), UInt(0.W), 4, p.nBufferDepth, p.nDataByte, 1))
  val m_dreq = Module(new GenFifo(p, UInt(0.W), UInt(8.W), 4, p.nBufferDepth, p.nDataByte, 1))
  val m_spi = Module(new SpiMasterCtrl(p))
  val m_read = Module(new GenFifo(p, UInt(0.W), UInt(8.W), 4, p.nBufferDepth, 1, p.nDataByte))

  val w_cfg = Wire(new SpiConfigBus(p.nSlave))

  // ******************************
  //            CONFIG
  // ******************************
  val init_cfg = Wire(new SpiConfigBus(p.nSlave))

  init_cfg.cpol := 0.B
  init_cfg.cpha := 0.B
  init_cfg.mode := MODE.BASE
  init_cfg.ncycle := DontCare
  init_cfg.slave := DontCare
  init_cfg.big := true.B
  init_cfg.irq := IRQ.B1

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
    val w_sent = ~m_creq.io.b_in(0).valid & ~m_dreq.io.b_in(0).valid & m_spi.io.o_idle
    val w_full = Wire(Vec(8, Bool()))
    val w_av = Wire(Vec(8, Bool()))
  
    for (b <- 0 until 8) {
      w_full(b) := false.B
      w_av(b) := false.B
    }
  
    for (b <- 0 until p.nDataByte) {
      w_full(b) := ~m_creq.io.b_in(b).ready | ~m_dreq.io.b_in(b).ready
    }
  
    for (b <- 0 until p.nDataByte) {
      w_av(b) := m_read.io.o_val(b).valid
    }
  
    io.b_regmem.get.status := Cat(  0.U(8.W),
                                    0.U(7.W), w_sent,
                                    w_full.asUInt,
                                    w_av.asUInt)
    io.b_regmem.get.config := Cat(  0.U((8 - IRQ.NBIT).W), r_cfg.irq,
                                    0.U(8.W),
                                    0.U(8.W),
                                    (r_cfg.slave + 0.U(3.W)), r_cfg.big, r_cfg.mode, r_cfg.cpha, r_cfg.cpol)
    io.b_regmem.get.ncycle := r_cfg.ncycle
  
    // ------------------------------
    //             WRITE
    // ------------------------------
    when (io.b_regmem.get.wen(1)) {
      r_cfg.cpol := io.b_regmem.get.wdata(0)
      r_cfg.cpha := io.b_regmem.get.wdata(1)
      r_cfg.mode := io.b_regmem.get.wdata(3, 2)
      r_cfg.big := io.b_regmem.get.wdata(4)
      r_cfg.slave := io.b_regmem.get.wdata(7, 5)
      r_cfg.irq := io.b_regmem.get.wdata(24 + IRQ.NBIT - 1, 24)
    }
  
    when (io.b_regmem.get.wen(2)) {
      r_cfg.ncycle := io.b_regmem.get.wdata
    }  
  }

  // ******************************
  //            MASTER
  // ******************************
  // ------------------------------
  //           REQUEST
  // ------------------------------
  m_creq.io.i_flush := false.B
  m_dreq.io.i_flush := false.B

  if (p.useRegMem) {
    m_creq.io.b_in <> io.b_regmem.get.creq
    m_dreq.io.b_in <> io.b_regmem.get.dreq
  } else {
    m_creq.io.b_in <> io.b_creq.get
    m_dreq.io.b_in <> io.b_dreq.get
  }

  // ------------------------------
  //            GLOBAL
  // ------------------------------
  val w_is_write = ((m_creq.io.b_out(0).ctrl.get.cmd === CMD.W) | (m_creq.io.b_out(0).ctrl.get.cmd === CMD.RW))
  m_spi.io.i_cfg := w_cfg
  io.o_idle := ~m_creq.io.b_in(0).valid & ~m_dreq.io.b_in(0).valid & m_spi.io.o_idle

  m_creq.io.b_out(0).ready := m_spi.io.b_port.req.ready & (~w_is_write | m_dreq.io.b_out(0).valid)
  m_dreq.io.b_out(0).ready := m_spi.io.b_port.req.ready & m_creq.io.b_out(0).valid & w_is_write
  m_spi.io.b_port.req.valid := m_creq.io.b_out(0).valid & (~w_is_write | m_dreq.io.b_out(0).valid)
  m_spi.io.b_port.req.ctrl.get := m_creq.io.b_out(0).ctrl.get
  m_spi.io.b_port.req.data.get := m_dreq.io.b_out(0).data.get

  m_spi.io.b_spi <> io.b_spi

  m_spi.io.b_port.ack <> m_read.io.b_in(0)

  // ------------------------------
  //             READ
  // ------------------------------  
  m_read.io.i_flush := false.B

  if (p.useRegMem) {
    io.b_regmem.get.read <> m_read.io.b_out
  } else {
    io.b_read.get <> m_read.io.b_out
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

object SpiMaster extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SpiMaster(SpiMasterConfigBase), args)
}

