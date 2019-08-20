package ru.app.util;

import jssc.*;
import ru.app.protocol.ccnet.BillStateType;

import java.util.Arrays;

public class ConnectionResolver {
    private String[] serialPorts;
    private byte[] response = null;
    private byte[] pollCommand = new byte[]{2, 3, 6, (byte) 0x33, (byte) 0xDA, (byte) 0x81};

    public ConnectionResolver() {
        serialPorts = getPorts();
    }

    public String findCCNetPort() {
        String portName = null;
        for (String port : serialPorts) {
            final SerialPort serialPort = new SerialPort(port);
            try {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
                serialPort.addEventListener(new SerialPortEventListener() {
                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        if (event.getEventType() == SerialPortEvent.RXCHAR && event.getEventValue() > 0) {
                            try {
                                response = serialPort.readBytes();
                            } catch (SerialPortException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });

                serialPort.writeBytes(pollCommand);
                long started = System.currentTimeMillis();
                do {
                    if (response == null)
                        Thread.sleep(10);
                } while (System.currentTimeMillis() - started < 1600 && response == null);
                if (response != null && response.length > 5) {
                    System.out.println("Portname = " + port + " Response = " + Arrays.toString(response));

                    portName = port;
                    break;
                }
            } catch (SerialPortException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return portName;
    }

    private String[] getPorts() {
        return SerialPortList.getPortNames();
    }
}
