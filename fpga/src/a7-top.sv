/*
 * File: a7-top.sv
 * Created Date: 2022-01-13 01:12:57 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-01 05:18:25 pm
 * Modified By: Mathieu Escouteloup
 * -----
 * License: See LICENSE.md
 * Copyright (c) 2022 LAAS
 * -----
 * Description: 
 */


`timescale 1 ns / 1 ns

module Top (
  input logic         i_clk,
  input logic         i_rst_n,
  
//  output logic        o_uart_rts_n,
  input logic         i_uart_rx,
  output logic        o_uart_tx,
//  input logic         i_uart_cts_n,

  input logic [3:0]   i_btn,
  input logic [3:0]   i_switch,
  output logic [3:0]  o_led,
  output logic [3:0]  o_gpio


//  output logic [2:0]  o_rgb_0, 
//  output logic [2:0]  o_rgb_1,  
//  output logic [2:0]  o_rgb_2,   
//  output logic [2:0]  o_rgb_3 
);

  // ******************************
  //        CLOCK GENERATION
  // ******************************
  logic clock;
  logic clockfb;

  MMCME2_BASE #(
    .BANDWIDTH("OPTIMIZED"),  
    .CLKFBOUT_MULT_F(6.0), 
    .CLKFBOUT_PHASE(0.0), 
    .CLKIN1_PERIOD(10.0), 
    .CLKOUT1_DIVIDE(1),
    .CLKOUT2_DIVIDE(1),
    .CLKOUT3_DIVIDE(1),
    .CLKOUT4_DIVIDE(1),
    .CLKOUT5_DIVIDE(1),
    .CLKOUT6_DIVIDE(1),
    .CLKOUT0_DIVIDE_F(24.0),
    .CLKOUT0_DUTY_CYCLE(0.5),
    .CLKOUT1_DUTY_CYCLE(0.5),
    .CLKOUT2_DUTY_CYCLE(0.5),
    .CLKOUT3_DUTY_CYCLE(0.5),
    .CLKOUT4_DUTY_CYCLE(0.5),
    .CLKOUT5_DUTY_CYCLE(0.5),
    .CLKOUT6_DUTY_CYCLE(0.5),
    .CLKOUT0_PHASE(0.0),
    .CLKOUT1_PHASE(0.0),
    .CLKOUT2_PHASE(0.0),
    .CLKOUT3_PHASE(0.0),
    .CLKOUT4_PHASE(0.0),
    .CLKOUT5_PHASE(0.0),
    .CLKOUT6_PHASE(0.0),
    .CLKOUT4_CASCADE("FALSE"), 
    .DIVCLK_DIVIDE(1),         
    .REF_JITTER1(0.0),         
    .STARTUP_WAIT("FALSE")     
  ) m_MMCME2_BASE (
    .CLKOUT0(clock),    
    .CLKOUT0B(),   
    .CLKOUT1(),     
    .CLKOUT1B(),   
    .CLKOUT2(),     
    .CLKOUT2B(),   
    .CLKOUT3(),     
    .CLKOUT3B(),   
    .CLKOUT4(),     
    .CLKOUT5(),     
    .CLKOUT6(),     
    .CLKFBOUT(clockfb),   
    .CLKFBOUTB(),
    .LOCKED(),      
    .CLKIN1(i_clk),      
    .PWRDWN(1'b0),       
    .RST(!i_rst_n),        
    .CLKFBIN(clockfb)     
  );

  // ******************************
  //        RESET GENERATION
  // ******************************
  logic reset;
  logic resetn;

  logic [5:0] r_reset_count;

  always_ff @(posedge clock, negedge i_rst_n) begin
    if (!i_rst_n) begin
      r_reset_count = 6'h3f;
    end
    else begin
      if (r_reset_count != 6'h0) begin
        r_reset_count = r_reset_count - 1'b1;
      end
    end
  end

  assign reset = !i_rst_n || (r_reset_count != 6'h0);
  assign resetn = !reset;    

  // ******************************
  //             I/Os
  // ******************************  
  logic [3:0] r_reg;
  logic [3:0] r_gpio;

  always_ff @(posedge clock) begin
    if (reset) begin
      r_reg = 4'h0;
      r_gpio = 4'h0;
    end
    else begin
      r_reg = r_reg + 4'h1;
      r_gpio = r_reg;
    end
  end

  assign o_led = 4'b0101;
  assign o_gpio = r_reg;
//  assign o_uart_rts_n = 1'b1;
endmodule
