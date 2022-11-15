/*
 * File: test0.scala
 * Created Date: 2022-11-14 04:33:04 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-14 04:50:54 pm
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
import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import scala.math._

class TopTest0(dut: Top) extends  PeekPokeTester(dut) {
  step(500)
}


// SPI execution (object)
object TopTest0 extends App {
  iotesters.Driver.execute(args, () => new Top(TopConfigBase)) {
    dut => new TopTest0(dut)
  }
}