/*
 * File: consts.scala
 * Created Date: 2022-09-17 07:44:30 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-14 09:44:07 am
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
import chisel3.experimental.ChiselEnum
import scala.math._


object UartFSM extends ChiselEnum {
  val s0IDLE, s1START, s2DATA, s3PARITY, s4STOP0, s5STOP1, s6VALID = Value
}

object BIT {
  def IDLE  = 1
  def START = 0
  def STOP  = 1
}

object ADDR32 {
  def STATUS  = "h00"
  def NCYCLE  = "h04"
  def WDATA   = "h08"
  def RDATA   = "h0c"
}

object IRQ {
  def NBIT  = 2

  def B1    = 0.U(NBIT.W)
  def B2    = 1.U(NBIT.W)
  def B4    = 2.U(NBIT.W)
  def B8    = 3.U(NBIT.W)
}
