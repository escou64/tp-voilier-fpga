/*
 * File: timer.scala
 * Created Date: 2022-10-28 04:10:44 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-07 03:29:12 pm
 * Modified By: Mathieu Escouteloup
 * -----
 * License: See LICENSE.md
 * Copyright (c) 2022 ISARD
 * -----
 * Description: 
 */

package tp.timer

import chisel3._
import chisel3.util._


class Timer(nBit: Int) extends Module {
  val io = IO(new Bundle {
    val i_wen = Input(Bool())
    val i_wdata = Input(UInt(nBit.W))

    val o_irq = Output(Bool())
  })

  val r_cnt = Reg(UInt(nBit.W))
  val r_arr = Reg(UInt(nBit.W))

  when (r_arr > 0.U) {
    r_cnt := r_cnt + 1.U
  }

  when (io.i_wen) {
    r_arr := io.i_wdata
  }

  io.o_irq := (r_cnt >= r_arr)

}

object Timer extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Timer(5), args)
}
