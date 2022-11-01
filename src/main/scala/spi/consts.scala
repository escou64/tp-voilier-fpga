/*
 * File: consts.scala                                                          *
 * Created Date: 2022-11-01 06:34:51 pm                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2022-11-01 06:36:33 pm                                       *
 * Modified By: Mathieu Escouteloup                                            *
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


object CMD {
  def NBIT  = 0
  def X     = 0

  def NO    = 0.U(NBIT.W)
  def R     = 1.U(NBIT.W)
  def W     = 2.U(NBIT.W)
  def RW    = 3.U(NBIT.W)
}