'''
File: uart.py
Created Date: 2022-01-04 09:17:50 am
Author: Mathieu Escouteloup
-----
Last Modified: 2022-01-06 11:25:31 am
Modified By: Mathieu Escouteloup
-----
License: See LICENSE.md
Copyright (c) 2022 IPPy
-----
Description: 
'''

import serial 

#ser = serial.Serial('/dev/ttyUSB0', 19200)

#while (true):
#  ser.write(b'\x01')
#  print(ser.read())

#val = input("New value: ")
#valbyte = bytes.fromhex(val)

#print(val)
#print(valbyte)


serial_port = serial.Serial()

serial_port.baudrate = 9600
serial_port.port = '/dev/ttyUSB1'
serial_port.timeout = 1
serial_port.bytesize = serial.EIGHTBITS
serial_port.stopbits = serial.STOPBITS_ONE
serial_port.parity = serial.PARITY_EVEN

serial_port.open()

if (serial_port.is_open):
  serial_port.reset_input_buffer()

  while True:
    print("Ecriture d'une nouvelle valeur :")
    value = input("Entrez la valeur :") #de la forme "1122" "abcd" ...
    if value == "reset":
      serial_port.reset_input_buffer()
    else:
      comm = bytes.fromhex(value)
      print(comm)
      serial_port.write(comm)
      print(serial_port.read())

    #print(serial_port.read())

  serial_port.close()
