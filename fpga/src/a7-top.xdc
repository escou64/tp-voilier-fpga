## Clock
set_property -dict { PACKAGE_PIN E3    IOSTANDARD LVCMOS33 } [get_ports { i_clk }];
create_clock -add -name i_clk -period 10.00 -waveform {0 5} [get_ports { i_clk }];

## Reset
set_property -dict { PACKAGE_PIN C2    IOSTANDARD LVCMOS33 } [get_ports { i_rst_n }]; 

## LEDs
set_property -dict { PACKAGE_PIN H5    IOSTANDARD LVCMOS33 } [get_ports { o_led[0] }]; 
set_property -dict { PACKAGE_PIN J5    IOSTANDARD LVCMOS33 } [get_ports { o_led[1] }]; 
set_property -dict { PACKAGE_PIN T9    IOSTANDARD LVCMOS33 } [get_ports { o_led[2] }]; 
set_property -dict { PACKAGE_PIN T10   IOSTANDARD LVCMOS33 } [get_ports { o_led[3] }]; 

## RGB LEDs
# set_property -dict { PACKAGE_PIN E1    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_0[0] }];
# set_property -dict { PACKAGE_PIN F6    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_0[1] }];
# set_property -dict { PACKAGE_PIN G6    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_0[2] }];
# set_property -dict { PACKAGE_PIN G4    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_1[0] }];
# set_property -dict { PACKAGE_PIN J4    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_1[1] }];
# set_property -dict { PACKAGE_PIN G3    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_1[2] }];
# set_property -dict { PACKAGE_PIN H4    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_2[0] }];
# set_property -dict { PACKAGE_PIN J2    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_2[1] }];
# set_property -dict { PACKAGE_PIN J3    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_2[2] }];
# set_property -dict { PACKAGE_PIN K2    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_3[0] }];
# set_property -dict { PACKAGE_PIN H6    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_3[1] }];
# set_property -dict { PACKAGE_PIN K1    IOSTANDARD LVCMOS33 } [get_ports { o_rgb_3[2] }];

## Switches
set_property -dict { PACKAGE_PIN A8    IOSTANDARD LVCMOS33 } [get_ports { i_switch[0] }];
set_property -dict { PACKAGE_PIN C11   IOSTANDARD LVCMOS33 } [get_ports { i_switch[1] }];
set_property -dict { PACKAGE_PIN C10   IOSTANDARD LVCMOS33 } [get_ports { i_switch[2] }];
set_property -dict { PACKAGE_PIN A10   IOSTANDARD LVCMOS33 } [get_ports { i_switch[3] }];

## Buttons
set_property -dict { PACKAGE_PIN D9    IOSTANDARD LVCMOS33 } [get_ports { i_btn[0] }]; 
set_property -dict { PACKAGE_PIN C9    IOSTANDARD LVCMOS33 } [get_ports { i_btn[1] }]; 
set_property -dict { PACKAGE_PIN B9    IOSTANDARD LVCMOS33 } [get_ports { i_btn[2] }]; 
set_property -dict { PACKAGE_PIN B8    IOSTANDARD LVCMOS33 } [get_ports { i_btn[3] }]; 

## UART
set_property -dict { PACKAGE_PIN D10   IOSTANDARD LVCMOS33 } [get_ports { o_uart_tx }];
set_property -dict { PACKAGE_PIN A9    IOSTANDARD LVCMOS33 } [get_ports { i_uart_rx }];

## Digital I/Os
#set_property -dict { PACKAGE_PIN T14   IOSTANDARD LVCMOS33 } [get_ports { o_dbg[0] }];   #IO5
#set_property -dict { PACKAGE_PIN T15   IOSTANDARD LVCMOS33 } [get_ports { o_dbg[1] }];   #IO6
#set_property -dict { PACKAGE_PIN T16   IOSTANDARD LVCMOS33 } [get_ports { o_dbg[2] }];   #IO7
#set_property -dict { PACKAGE_PIN N15   IOSTANDARD LVCMOS33 } [get_ports { o_dbg[3] }];   #IO8

set_property -dict { PACKAGE_PIN V17   IOSTANDARD LVCMOS33 } [get_ports { o_gpio[0] }];   #IO10
set_property -dict { PACKAGE_PIN U18   IOSTANDARD LVCMOS33 } [get_ports { o_gpio[1] }];   #IO11
set_property -dict { PACKAGE_PIN R17   IOSTANDARD LVCMOS33 } [get_ports { o_gpio[2] }];   #IO12 
set_property -dict { PACKAGE_PIN P17   IOSTANDARD LVCMOS33 } [get_ports { o_gpio[3] }];   #IO13

## PMOD JA: UART
#set_property -dict { PACKAGE_PIN G13   IOSTANDARD LVCMOS33 } [get_ports { ja[1] }]; 
#set_property -dict { PACKAGE_PIN B11   IOSTANDARD LVCMOS33 } [get_ports { ja[2] }]; 
#set_property -dict { PACKAGE_PIN A11   IOSTANDARD LVCMOS33 } [get_ports { ja[3] }]; 
#set_property -dict { PACKAGE_PIN D12   IOSTANDARD LVCMOS33 } [get_ports { ja[4] }]; 
#set_property -dict { PACKAGE_PIN D13   IOSTANDARD LVCMOS33 } [get_ports { o_uart_rts_n }]; 
#set_property -dict { PACKAGE_PIN B18   IOSTANDARD LVCMOS33 } [get_ports { o_uart_tx }]; 
#set_property -dict { PACKAGE_PIN A18   IOSTANDARD LVCMOS33 } [get_ports { i_uart_rx }]; 
#set_property -dict { PACKAGE_PIN E15   IOSTANDARD LVCMOS33 } [get_ports { i_uart_cts_n }];


# SPI Flash
# set_property -dict { PACKAGE_PIN K17   IOSTANDARD LVCMOS33 } [get_ports { t_spi_dq[0] }]; 
# set_property -dict { PACKAGE_PIN K18   IOSTANDARD LVCMOS33 } [get_ports { t_spi_dq[1] }]; 
# set_property -dict { PACKAGE_PIN L14   IOSTANDARD LVCMOS33 } [get_ports { t_spi_dq[2] }]; 
# set_property -dict { PACKAGE_PIN M14   IOSTANDARD LVCMOS33 } [get_ports { t_spi_dq[3] }]; 
# set_property -dict { PACKAGE_PIN L13   IOSTANDARD LVCMOS33 } [get_ports { o_spi_csn }]; 
# set_property -dict { PACKAGE_PIN L16   IOSTANDARD LVCMOS33 } [get_ports { o_spi_sclk }]; 

set_property -dict { PACKAGE_PIN V15   IOSTANDARD LVCMOS33 } [get_ports { o_spi_csn }];     # IO0 
set_property -dict { PACKAGE_PIN U16   IOSTANDARD LVCMOS33 } [get_ports { o_spi_sclk }];    # IO1
set_property -dict { PACKAGE_PIN P14   IOSTANDARD LVCMOS33 } [get_ports { t_spi_dq[0] }];   # IO2 
set_property -dict { PACKAGE_PIN T11   IOSTANDARD LVCMOS33 } [get_ports { t_spi_dq[1] }];   # IO3 
set_property -dict { PACKAGE_PIN R12   IOSTANDARD LVCMOS33 } [get_ports { t_spi_dq[2] }];   # IO4 
set_property -dict { PACKAGE_PIN T14   IOSTANDARD LVCMOS33 } [get_ports { t_spi_dq[3] }];   # IO5 