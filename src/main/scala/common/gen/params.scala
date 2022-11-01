/*
 * File: params.scala                                                          *
 * Created Date: 2022-08-31 01:50:07 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-01 08:03:48 pm                                       *
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
import scala.math._


trait GenParams {
  def debug: Boolean
}

case class GenConfig (
  debug: Boolean
) extends GenParams