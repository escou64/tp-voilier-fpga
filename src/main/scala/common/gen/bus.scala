/*
 * File: bus.scala                                                             *
 * Created Date: 2022-08-31 01:50:07 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-01 08:03:43 pm                                       *
 * Modified By: Mathieu Escouteloup                                            *
 * -----
 * License: See LICENSE.md
 * Copyright (c) 2022 ISARD
 * -----
 * Description: 
 */


package tp.common.gen

import chisel3._
import chisel3.util._


// ******************************
//            FLAT BUS
// ******************************
class FlatVBus extends Bundle {
  val valid = Bool()
}

class FlatRVBus extends FlatVBus {
  val ready = Bool()
}
// ******************************
//            FLAT IO
// ******************************
class FlatVIO extends Bundle {
  val valid = Output(Bool())  
}

class FlatRVIO extends FlatVIO {
  val ready = Input(Bool())
}

// ******************************
//          GENERIC BUS
// ******************************
class GenBus[TC <: Data, TD <: Data](p: GenParams, tc: TC, td: TD) extends Bundle {
  val ctrl = if (tc.getWidth > 0) Some(tc) else None
  val data = if (td.getWidth > 0) Some(td) else None
}

class GenVBus[TC <: Data, TD <: Data](p: GenParams, tc: TC, td: TD) extends GenBus[TC, TD](p, tc, td) {
  val valid = Bool()
}

class GenRVBus[TC <: Data, TD <: Data](p: GenParams, tc: TC, td: TD) extends GenVBus[TC, TD](p, tc, td) {
  val ready = Bool()
}

// ******************************
//           GENERIC IO
// ******************************
class GenIO[TC <: Data, TD <: Data](p: GenParams, tc: TC, td: TD) extends Bundle {
  val ctrl = if (tc.getWidth > 0) Some(Output(tc)) else None
  val data = if (td.getWidth > 0) Some(Output(td)) else None
}
class GenVIO[TC <: Data, TD <: Data](p: GenParams, tc: TC, td: TD) extends GenIO[TC, TD](p, tc, td) {
  val valid = Output(Bool())
}

class GenRVIO[TC <: Data, TD <: Data](p: GenParams, tc: TC, td: TD) extends GenVIO[TC, TD](p, tc, td) {
  val ready = Input(Bool())
}