/*
 * File: bidirect.scala
 * Created Date: 2022-11-12 11:17:51 am
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-12 05:33:38 pm
 * Modified By: Mathieu Escouteloup
 * -----
 * License: See LICENSE.md
 * Copyright (c) 2022 ISARD
 * -----
 * Description: 
 */


package tp.common.bus

import chisel3._
import chisel3.util._


class BiDirectIO[T <: Data](gen: T) extends Bundle {
  val in = Input(gen.cloneType)
  val out = Output(gen.cloneType)
  val eno = Output(gen.cloneType)
}

class BiDirectBus[T <: Data](gen: T) extends Bundle {
  val in = gen.cloneType
  val out = gen.cloneType
  val eno = gen.cloneType
}