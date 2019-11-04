package ru.app.hardware.ucs;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.protocol.ucs.Command;
import ru.app.util.LogCreator;

public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private SerialPort serialPort;
    private byte[] received;

    Client(String portName) throws SerialPortException {
        serialPort = new SerialPort(portName);
        serialPort.openPort();

        serialPort.setParams(SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPort.addEventListener(new PortReader());

        LOGGER.info(LogCreator.console("Initialization port " + portName + " was succesfull!"));
    }

    synchronized byte[] sendMessage(Command command) {
        return new byte[0];
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR) {
                try {
                    received = serialPort.readBytes();
                    LOGGER.debug(LogCreator.logInput(received));
                } catch (SerialPortException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                }
            }
        }
    }
}
