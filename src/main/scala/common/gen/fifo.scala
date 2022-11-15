/*
 * File: fifo.scala
 * Created Date: 2022-08-31 01:50:07 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-14 08:24:35 am
 * Modified By: Mathieu Escouteloup
 * -----
 * License: See LICENSE.md
 * Copyright (c) 2022 ISARD
 * -----
 * Description: FIFO with optional registers:                                  
 * nSeqLvl = 0: cross read, combinatorial free, combinatorial individual full   
 * nSeqLvl = 1: sync. read, combinatorial free, combinatorial individual full     
 * nSeqLvl = 2: sync. read, synchronous free, combinatorial individual full        
 * nSeqLvl = 3: sync. read, synchronous free, synchronous individual full          
 * nSeqLvl = 4: sync. read, synchronous free, synchronous global full           
 */


package tp.common.gen

import chisel3._
import chisel3.util._


class GenFifo[TC <: Data, TD <: Data](p: GenParams, tc: TC, td: TD, nSeqLvl: Int, depth: Int, nInPort: Int, nOutPort: Int) extends Module {
  // ******************************
  //             I/Os
  // ******************************
  val io = IO(new Bundle {
    val i_flush = Input(Bool())

    val b_in = Vec(nInPort, Flipped(new GenRVIO(p, tc, td)))

    val o_pt = Output(UInt((log2Ceil(depth + 1)).W))
    val o_val = Output(Vec(depth, new GenVBus(p, tc, td)))
    
    val b_out = Vec(nOutPort, new GenRVIO(p, tc, td))
  })

  val r_pt = RegInit(0.U((log2Ceil(depth + 1)).W))
  val r_fifo = Reg(Vec(depth, new GenBus(p, tc, td)))
  
  val w_out_pt = Wire(Vec(nOutPort + 1, UInt((log2Ceil(depth + 1)).W)))
  val w_out_in_pt = Wire(Vec(nOutPort + 1, UInt((log2Ceil(nInPort + 1)).W)))

  val w_in_pt = Wire(Vec(nInPort + 1, UInt((log2Ceil(depth + 1)).W)))
  val w_full_pt = Wire(Vec(nInPort + 1, UInt((log2Ceil(depth + 1)).W)))

  // ******************************
  //              READ
  // ******************************
  w_out_pt(0) := 0.U
  w_out_in_pt(0) := nInPort.U
  if (nSeqLvl <= 0) {
    for (i <- 0 until nInPort) {
      when (io.b_in(nInPort - 1 - i).valid) {
        w_out_in_pt(0) := (nInPort - 1 - i).U
      }
    }
  }

  // ------------------------------
  //       SUPPORT CROSS READ
  // ------------------------------
  if (nSeqLvl <= 0) {
    for (o <- 0 until nOutPort) {
      io.b_out(o).valid := (w_out_pt(o) < r_pt) | (w_out_in_pt(o) < nInPort.U)
      if (tc.getWidth > 0)  io.b_out(o).ctrl.get := r_fifo(0).ctrl.get
      if (td.getWidth > 0)  io.b_out(o).data.get := r_fifo(0).data.get
      w_out_pt(o + 1) := w_out_pt(o)
      w_out_in_pt(o + 1) := w_out_in_pt(o)

      for (d <- 0 until depth) {
        when((w_out_pt(o) < r_pt) & (d.U === w_out_pt(o))) {
          if (tc.getWidth > 0)  io.b_out(o).ctrl.get := r_fifo(d).ctrl.get
          if (td.getWidth > 0)  io.b_out(o).data.get := r_fifo(d).data.get
        }
      }

      for (i <- 0 until nInPort) {
        when((w_out_pt(o) >= r_pt) & (w_out_in_pt(o) < nInPort.U) & i.U === w_out_in_pt(o)) {
          if (tc.getWidth > 0)  io.b_out(o).ctrl.get := io.b_in(i).ctrl.get
          if (td.getWidth > 0)  io.b_out(o).data.get := io.b_in(i).data.get
        }
      }

      when(io.b_out(o).ready & (w_out_pt(o) < r_pt)) {
        w_out_pt(o + 1) := w_out_pt(o) + 1.U
      }

      when(io.b_out(o).ready & (w_out_pt(o) >= r_pt) & (w_out_in_pt(o) < nInPort.U)) {
        w_out_in_pt(o + 1) := nInPort.U
        for (i <- 0 until nInPort) {
          when (io.b_in(nInPort - 1 - i).valid & ((nInPort - 1 - i).U > w_out_in_pt(o))) {
            w_out_in_pt(o + 1) := (nInPort - 1 - i).U
          }
        }
      }
    }

  // ------------------------------
  //         NO CROSS READ
  // ------------------------------
  } else {
    for (o <- 0 until nOutPort) {
      io.b_out(o).valid := (w_out_pt(o) < r_pt)
      if (tc.getWidth > 0)  io.b_out(o).ctrl.get := r_fifo(0).ctrl.get
      if (td.getWidth > 0)  io.b_out(o).data.get := r_fifo(0).data.get
      w_out_pt(o + 1) := w_out_pt(o)
      w_out_in_pt(o + 1) := w_out_in_pt(o)

      for (d <- 0 until depth) {
        when(d.U === w_out_pt(o)) {
          if (tc.getWidth > 0)  io.b_out(o).ctrl.get := r_fifo(d).ctrl.get
          if (td.getWidth > 0)  io.b_out(o).data.get := r_fifo(d).data.get
        }
      }

      when(io.b_out(o).ready & (w_out_pt(o) < r_pt)) {
        w_out_pt(o + 1) := w_out_pt(o) + 1.U
      }
    }
  }

  // ******************************
  //             SHIFT
  // ******************************
  for (o <- 0 to nOutPort) {
    when (o.U === w_out_pt(nOutPort)) {
      for (s <- 0 until depth - o) {
        r_fifo(s) := r_fifo(s + o)
      }
    }
  }

  // ******************************
  //             WRITE
  // ******************************
  w_in_pt(0) := r_pt - w_out_pt(nOutPort)
  if (nSeqLvl <= 1) {
    w_full_pt(0) := r_pt - w_out_pt(nOutPort)
  } else if ((nSeqLvl == 2) || (nSeqLvl == 3)) {
    w_full_pt(0) := r_pt
  } else {
    when((depth.U - r_pt) >= nInPort.U) {
      w_full_pt(0) := r_pt
    }.otherwise {
      w_full_pt(0) := depth.U
    }
  }

  for (i <- 0 until nInPort) {
    w_in_pt(i + 1) := w_in_pt(i)

    if (nSeqLvl <= 0) {
      io.b_in(i).ready := (w_full_pt(i) < depth.U) | (w_out_in_pt(nOutPort) > i.U)

      w_full_pt(i + 1) := w_full_pt(i)

      when(io.b_in(i).valid & (w_full_pt(i) < depth.U) & (w_out_in_pt(nOutPort) <= i.U)) {
        for (d <- 0 until depth) {
          when(d.U === w_in_pt(i)) {
            if (tc.getWidth > 0)  r_fifo(d).ctrl.get := io.b_in(i).ctrl.get
            if (td.getWidth > 0)  r_fifo(d).data.get := io.b_in(i).data.get
          }
        }
        w_in_pt(i + 1) := w_in_pt(i) + 1.U
        w_full_pt(i + 1) := w_full_pt(i) + 1.U
      }
    } else {
      io.b_in(i).ready := (w_full_pt(i) < depth.U)

      if (nSeqLvl == 3) {
        w_full_pt(i + 1) := w_full_pt(i) + 1.U
      } else if (nSeqLvl >= 4) {
        w_full_pt(i + 1) := w_full_pt(0)
      } else {
        w_full_pt(i + 1) := w_full_pt(i)
      }

      when(io.b_in(i).valid & (w_full_pt(i) < depth.U)) {
        for (d <- 0 until depth) {
          when(d.U === w_in_pt(i)) {
            if (tc.getWidth > 0)  r_fifo(d).ctrl.get := io.b_in(i).ctrl.get
            if (td.getWidth > 0)  r_fifo(d).data.get := io.b_in(i).data.get
          }
        }
        w_in_pt(i + 1) := w_in_pt(i) + 1.U
        if (nSeqLvl <= 2) w_full_pt(i + 1) := w_full_pt(i) + 1.U
      }
    }
  }

  // ******************************
  //           REGISTER
  // ******************************
  when(io.i_flush) {
    r_pt := 0.U
  }.otherwise {
    r_pt := w_in_pt(nInPort)
  }

  // ******************************
  //        EXTERNAL ACCESS
  // ******************************
  io.o_pt := r_pt
  for (d <- 0 until depth) {
    io.o_val(d).valid := (d.U < r_pt)
    if (tc.getWidth > 0)  io.o_val(d).ctrl.get := r_fifo(d).ctrl.get
    if (td.getWidth > 0)  io.o_val(d).data.get := r_fifo(d).data.get
  }
}

object GenFifo extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new GenFifo(GenConfigBase, UInt(4.W), UInt(8.W), 2, 4, 2, 2), args)
}