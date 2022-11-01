/*
 * File: master.scala                                                          *
 * Created Date: 2022-11-01 06:27:51 pm                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2022-11-01 09:04:10 pm                                       *
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


object SpiMasterFSM extends ChiselEnum {
  val s0IDLE, s1SYNC, s2DATA, s3END = Value
}

class SpiMaster (p: SpiParams) extends Module {
  import tp.spi.SpiMasterFSM._

  val io = IO(new Bundle {
    val i_config = Input(new SpiConfigBus(p.nDataBit))
    val o_idle = Output(Bool())

    val b_port = new SpiMasterIO()

    val b_spi = new SpiIO(p.nSlave)
  })

  val r_fsm = RegInit(s0IDLE)
  val r_cmd = Reg(UInt(CMD.NBIT.W))
  val r_csn = RegInit(VecInit(Seq.fill(p.nSlave)(1.B)))
  val r_sclk = RegInit(0.B)
  val r_rdata = Reg(Vec(8, Bool()))
  val r_wdata = Reg(Vec(8, Bool()))

  // ******************************
  //            COUNTERS
  // ******************************
  val m_ccnt = Module(new Counter(nCycleBit))
  val m_dcnt = Module(new Counter(3))

  m_ccnt.io.i_limit := r_config.ncycle
  m_ccnt.io.i_init := (r_fsm === s0IDLE) | (r_fsm === s3END)
  m_ccnt.io.i_en := (r_fsm =/= s0IDLE) & (r_fsm =/= s3END)

  m_dcnt.io.i_limit := 0.U
  m_dcnt.io.i_init := true.B
  m_dcnt.io.i_en := false.B

  // ******************************
  //             SCLK
  // ******************************
  when (r_fsm =/= s0IDLE) {
    switch (m_ccnt.io.o_val) {
      is ((r_config.ncycle - 1.U) >> 1.U) {
        r_sclk := ~r_sclk
      }
      is (r_config.ncycle - 1.U) {
        r_sclk := ~r_sclk
      }
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
        r_csn(io.i_config.slave) := false.B
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
      when  (r_count === ((io.i_config.nCycle - 1.U) >> 1.U))  {
        r_rdata(r_count_bit) := io.b_spi.miso
      }

      when (r_count === (io.i_config.nCycle - 1.U)) {
        r_count_bit := r_count_bit + 1.U
        when (r_count_bit === 7.U) {
          r_fsm := s3END
        }
      }
    }

    // ------------------------------
    //             END
    // ------------------------------
    is (s3END){
      io.b_send.ready := 1.U
      io.b_rec.valid := 1.U
      io.b_spi.cs(io.i_config.SlaveID) := 0.B

      when(io.b_send.valid === 0.B){
        r_fsm := s0IDLE
      } .otherwise {
        r_fsm := s2DATA
      }
    }
  }



  // ******************************
  //              I/Os
  // ******************************
  io.o_idle := (r_fsm === s0IDLE)
  io.b_spi.csn := r_csn
  io.b_spi.sclk := r_sclk
  io.b_spi.mosi := r_wdata(0)

  when ((r_fsm === s2DATA) & (m_ccnt.io.o_val === ((r_config.ncycle - 1.U) >> 1.U))) {
    r_rdata(0) := io.b_spi.miso
    for (b <- 1 until 8) {
      r_rdata(b) := r_rdata(b - 1)
    }    
  }

  io.b_port.req.ready := (r_fsm === s0IDLE) | (r_fsm === s3END)
  io.b_port.ack.valid := (r_fsm === s3END) & ((r_cmd === CMD.W) | (r_cmd === CMD.RW))
  io.b_port.ack.data.get := r_rdata.asUInt






  val r_count     = RegInit(0.U(p.nDataBit.W))
  val r_count_bit = RegInit(0.U(3.W))
  val r_rdata  = Reg(Vec(8, Bool()))
  val r_wdata = Reg(Vec(8, Bool()))
  val r_fsm       = RegInit(s0IDLE)
  val r_sclk      = RegInit(0.B)
  val r_req       = RegInit(0.B)

  //init
  io.o_idle := (r_fsm === s0IDLE)
  io.b_send.ready := 1.U
  for (id <- 0 until p.nSlave) {
    io.b_spi.cs(id) := 1.B
  }
  io.b_spi.mosi := 0.U

  io.b_spi.sclk := r_sclk

  io.b_rec.valid := 0.B
  io.b_rec.req  := r_req  
  io.b_rec.data := Mux(io.i_config.big,  Reverse(r_rdata.asUInt()), r_rdata.asUInt())

  when ((r_fsm === s0IDLE) || (r_fsm === s3END)) {
    r_req := io.b_send.req
    when (io.i_config.big){
      r_wdata := Reverse(io.b_send.data).asBools()
    }.otherwise{
      r_wdata := io.b_send.data.asBools()
    }
  }

  // ******************************
  //             SCLK
  // ******************************
  //when ((r_fsm === s2DATA) | (r_fsm === s3END)) {
  when (r_fsm === s2DATA) {
    //generating serial clock
    when (r_count === (io.i_config.nCycle - 1.U)) {
      r_sclk := ~r_sclk
    }.otherwise {
      when (r_count === ((io.i_config.nCycle - 1.U) >> 1.U)) {
        r_sclk := ~r_sclk
      }
    }
  } .otherwise {
    r_sclk := 0.B
  }

  // ******************************
  //              FSM
  // ******************************
  // ------------------------------
  //            COUNTER
  // ------------------------------
  r_count  := 0.U
  //when ((r_fsm === s2DATA) | (r_fsm === s3END) | (r_fsm === s1SYNC)){  
  when ((r_fsm === s2DATA) | (r_fsm === s1SYNC)){  
    when (r_count === (io.i_config.nCycle - 1.U)) {
      r_count := 0.U
    }.otherwise {
      r_count := r_count + 1.U
    }
  }

  switch(r_fsm){
    // ------------------------------
    //             IDLE
    // ------------------------------
    is(s0IDLE) {
      when (io.b_send.valid) {
        r_fsm   := s1SYNC
      }
      io.o_idle := 1.B
    }

    // ------------------------------
    //          SYNCHRONIZE
    // ------------------------------
    is(s1SYNC) {
      io.b_send.ready := 0.U
      io.b_spi.cs(io.i_config.SlaveID) := 0.B

      when (r_count === (io.i_config.nCycle-1.U)){
        r_fsm := s2DATA
      }
    }

    // ------------------------------
    //             BUSY
    // ------------------------------
    is(s2DATA) {
      io.b_send.ready := 0.U
      io.b_spi.cs(io.i_config.SlaveID) := 0.B
      io.b_spi.mosi := r_wdata(r_count_bit)

      when  (r_count === ((io.i_config.nCycle - 1.U) >> 1.U))  {
        r_rdata(r_count_bit) := io.b_spi.miso
      }

      when (r_count === (io.i_config.nCycle - 1.U)) {
        r_count_bit := r_count_bit + 1.U
        when (r_count_bit === 7.U) {
          r_fsm := s3END
        }
      }
    }

    // ------------------------------
    //             END
    // ------------------------------
    is (s3END){
      io.b_send.ready := 1.U
      io.b_rec.valid := 1.U
      io.b_spi.cs(io.i_config.SlaveID) := 0.B

      when(io.b_send.valid === 0.B){
        r_fsm := s0IDLE
      } .otherwise {
        r_fsm := s2DATA
      }
    }
  }

  // ******************************
  //             DEBUG
  // ******************************
  io.o_dbg_rec := r_rdata.asUInt
}

object SpiMaster extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SpiMaster(SpiConfigBase), args)
}

class SpiMasterUnit (nBit : Int , nSlave : Int, nWriteByte: Int, nReadByte: Int, nBufferDepth: Int) extends Module {
  val io = IO(new Bundle {
    val i_config = Input(new SpiConfigBus(nBit))

    val b_in = Vec(nWriteByte, Flipped(new GenRVIO(Bool(), UInt(8.W))))

    val b_spi = new SpiIO(nBit , nSlave)

    val b_out = Vec(nReadByte, new GenRVIO(Bool(), UInt(8.W)))

    val o_idle = Output(Bool())
  })

  val m_in  = Module(new GenFifo(4, Bool(), UInt(8.W), nBufferDepth, nWriteByte, 1))
  val m_spi = Module(new SpiMaster(SpiConfigBase))
  val m_out = Module(new GenFifo(4, Bool(), UInt(8.W), nBufferDepth, 1, nReadByte))

  // ******************************
  //              IN
  // ******************************
  m_in.io.i_flush := false.B

  m_in.io.b_write <> io.b_in

  // ******************************
  //             SPI
  // ******************************
  m_spi.io.i_config := io.i_config

  io.o_idle := m_spi.io.o_idle 

  m_in.io.b_read(0).ready := m_spi.io.b_send.ready
  m_spi.io.b_send.valid := m_in.io.b_read(0).valid
  m_spi.io.b_send.req := m_in.io.b_read(0).ctrl.get
  m_spi.io.b_send.data := m_in.io.b_read(0).data.get

  m_spi.io.b_spi <> io.b_spi

  m_spi.io.b_rec.ready := m_out.io.b_write(0).ready
  m_out.io.b_write(0).valid := m_spi.io.b_rec.valid
  m_out.io.b_write(0).ctrl.get := m_spi.io.b_rec.req
  m_out.io.b_write(0).data.get := m_spi.io.b_rec.data

  // ******************************
  //              OUT
  // ******************************
  m_out.io.i_flush := false.B

  m_out.io.b_read <> io.b_out
  for (b <- 0 until nReadByte) {
    io.b_out(b).valid := m_out.io.o_val(b).valid
  }
}

object SpiMasterUnit extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SpiMasterUnit(32 , 1, 4, 4, 8), args)
}

