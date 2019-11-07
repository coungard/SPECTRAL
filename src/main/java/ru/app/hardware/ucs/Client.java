package ru.app.hardware.ucs;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.protocol.ucs.UCSCommand;
import ru.app.util.LogCreator;

public class Client implements SerialPortEventListener {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private SerialPort serialPort;
    private byte[] received;

    private static final byte DLE = (byte) 0x10;    // каждое сообщение начинается с DLE/STX и заканчивается DLE/ETX
    private static final byte STX = (byte) 0X02;
    private static final byte ETX = (byte) 0x03;
    private static final byte EOT = (byte) 0x04;    // завершение сессии передачи данных
    private static final byte ENQ = (byte) 0x05;    // инициация сессии передачи данных
    private static final byte ACK = (byte) 0x06;    // знак успеха

    Client(String portName) throws SerialPortException {
        serialPort = new SerialPort(portName);
        serialPort.openPort();

        serialPort.setParams(SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPort.addEventListener(this);

        LOGGER.info(LogCreator.console("Initialization port " + portName + " was succesfull!"));
    }

    synchronized byte[] sendMessage(UCSCommand command) {
        LOGGER.info(LogCreator.console(command.toString()));
        return new byte[0];
    }

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

    void close() throws SerialPortException {
        if (serialPort.isOpened()) {
            serialPort.closePort();
        }
    }
}
