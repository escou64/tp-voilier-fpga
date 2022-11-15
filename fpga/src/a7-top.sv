/*
 * File: a7-top.sv
 * Created Date: 2022-01-13 01:12:57 pm
 * Author: Mathieu Escouteloup
 * -----
 * Last Modified: 2022-11-14 04:40:21 pm
 * Modified By: Mathieu Escouteloup
 * -----
 * License: See LICENSE.md
 * Copyright (c) 2022 LAAS
 * -----
 * Description: 
 */


`timescale 1 ns / 1 ns

module A7Top (
  input logic         i_clk,
  input logic         i_rst_n,
  
//  output logic        o_uart_rts_n,
  input logic         i_uart_rx,
  output logic        o_uart_tx,
//  input logic         i_uart_cts_n,

  inout logic [3:0]   t_spi_dq,
  output logic        o_spi_sclk,
  output logic        o_spi_csn,

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
    .CLKOUT0_DIVIDE_F(60.0),
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
  //             TOP
  // ******************************  
  logic       w_uart_rx;
  logic       w_uart_tx;
  logic       w_spi_csn_0;    
  logic       w_spi_sclk;     
  logic [3:0] w_spi_data_in;  
  logic [3:0] w_spi_data_out; 
  logic [3:0] w_spi_data_eno; 

  Top m_top (
    .clock              (clock ),
    .reset              (reset ),
    .io_i_trig          (i_btn[0]),
    .io_i_sel           (i_switch[0]),
    .io_b_uart_tx       (w_uart_tx),
    .io_b_uart_rx       (w_uart_rx),
    .io_b_spi_csn_0     (o_spi_csn),
    .io_b_spi_sclk      (o_spi_sclk),
    .io_b_spi_data_in   (w_spi_data_in),
    .io_b_spi_data_out  (w_spi_data_out),
    .io_b_spi_data_eno  (w_spi_data_eno)
  );

  assign o_uart_tx = w_uart_tx;
  assign w_uart_rx = i_uart_rx;

  IOBUF #(
    .DRIVE(12),             // Specify the output drive strength
    .IBUF_LOW_PWR("TRUE"),  // Low Power - "TRUE", High Performance = "FALSE" 
    .IOSTANDARD("DEFAULT"), // Specify the I/O standard
    .SLEW("SLOW")           // Specify the output slew rate
  ) m_spi_dq0 (
     .O(),                  // Buffer output
     .IO(t_spi_dq[0]),      // Buffer inout port (connect directly to top-level port)
     .I(w_spi_data_out[0]), // Buffer input
     .T(~w_spi_data_eno[0]) // 3-state enable input, high=input, low=output
  );

  IOBUF #(
    .DRIVE(12),             // Specify the output drive strength
    .IBUF_LOW_PWR("TRUE"),  // Low Power - "TRUE", High Performance = "FALSE" 
    .IOSTANDARD("DEFAULT"), // Specify the I/O standard
    .SLEW("SLOW")           // Specify the output slew rate
  ) m_spi_dq1 (
     .O(),                  // Buffer output
     .IO(t_spi_dq[1]),      // Buffer inout port (connect directly to top-level port)
     .I(w_spi_data_out[1]), // Buffer input
     .T(~w_spi_data_eno[1]) // 3-state enable input, high=input, low=output
  );

  IOBUF #(
    .DRIVE(12),             // Specify the output drive strength
    .IBUF_LOW_PWR("TRUE"),  // Low Power - "TRUE", High Performance = "FALSE" 
    .IOSTANDARD("DEFAULT"), // Specify the I/O standard
    .SLEW("SLOW")           // Specify the output slew rate
  ) m_spi_dq2 (
     .O(),                  // Buffer output
     .IO(t_spi_dq[2]),      // Buffer inout port (connect directly to top-level port)
     .I(w_spi_data_out[2]), // Buffer input
     .T(~w_spi_data_eno[2]) // 3-state enable input, high=input, low=output
  );

  IOBUF #(
    .DRIVE(12),             // Specify the output drive strength
    .IBUF_LOW_PWR("TRUE"),  // Low Power - "TRUE", High Performance = "FALSE" 
    .IOSTANDARD("DEFAULT"), // Specify the I/O standard
    .SLEW("SLOW")           // Specify the output slew rate
  ) m_spi_dq3 (
     .O(),                  // Buffer output
     .IO(t_spi_dq[3]),      // Buffer inout port (connect directly to top-level port)
     .I(w_spi_data_out[3]), // Buffer input
     .T(~w_spi_data_eno[3]) // 3-state enable input, high=input, low=output
  );

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
//  assign o_gpio = r_reg;

  assign o_gpio = {2'b00, w_uart_tx, w_uart_rx};
//  assign o_uart_rts_n = 1'b1;
endmodule
