package ru.app.hardware.bneS110M;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.protocol.cctalk.Command;
import ru.app.util.LogCreator;

import java.util.Calendar;

public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private SerialPort serialPort;
    private byte[] received;
    static byte counter = 1;

    Client(String portName) throws SerialPortException {
        serialPort = new SerialPort(portName);
        serialPort.openPort();

        serialPort.setParams(SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPort.addEventListener(new PortReader());

        LOGGER.info(LogCreator.console("Initialization port " + portName + " was succesfull!"));
    }

    synchronized public byte[] sendMessage(Command command) {
        counter++;
        return new byte[0];
    }

    synchronized void sendBytes(byte[] bytes) {
        try {
            counter++;
            LOGGER.debug(LogCreator.logOutput(bytes, null));
            serialPort.writeBytes(bytes);
            long start = Calendar.getInstance().getTimeInMillis();
            do {
                if (received == null) Thread.sleep(10);
            } while (Calendar.getInstance().getTimeInMillis() - start < 1200 && received == null);
        } catch (SerialPortException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR) {
                try {
                    Thread.sleep(400);
                    received = serialPort.readBytes();
                    LOGGER.debug(LogCreator.logInput(received, null));
                } catch (InterruptedException | SerialPortException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
