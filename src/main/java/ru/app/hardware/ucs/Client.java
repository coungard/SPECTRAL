package ru.app.hardware.ucs;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.protocol.ucs.UCSCommand;
import ru.app.protocol.ucs.UCSMessage;
import ru.app.util.LogCreator;
import ru.app.util.ResponseHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Client implements SerialPortEventListener {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private static final byte[] TERMINAL_ID = new byte[]{0x00, 0x00, 0x01, 0x09, 0x09, 0x09, 0x09, 0x03, 0x00, 0x03}; // 10 bytes
    private SerialPort serialPort;
    private byte[] received;
    private static final long delayENQ = 3000;
    private static final long delaySTX = 200;

    private static final byte DLE = (byte) 0x10;    // каждое сообщение начинается с DLE/STX и заканчивается DLE/ETX
    private static final byte STX = (byte) 0X02;
    private static final byte ETX = (byte) 0x03;
    private static final byte EOT = (byte) 0x04;    // завершение сессии передачи данных
    private static final byte ENQ = (byte) 0x05;    // инициация сессии передачи данных
    private static final byte ACK = (byte) 0x06;    // успех
    private static final byte NAC = (byte) 0x15;    // неудача

    private String currentCommand;
    private String currentResponse;

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

    private synchronized void sendPacket(byte[] packet) {
        try {
            currentCommand = ResponseHandler.parseUCS(packet);
            LOGGER.debug(LogCreator.logOutput(packet));
            serialPort.writeBytes(packet);
        } catch (SerialPortException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()), ex);
        }
    }

    byte[] sendMessage(UCSCommand command) {
        LOGGER.info(LogCreator.console(command.toString()));
        try {
            // start session command begin
            long started = System.currentTimeMillis();
            String response;
            sendPacket(new byte[]{ENQ});
            do {
                Thread.sleep(20);
                response = ResponseHandler.parseUCS(received);
            } while (!UCSMessage.ACK.toString().equals(response) && System.currentTimeMillis() - started < delayENQ);
            if (!UCSMessage.ACK.toString().equals(response)) {
                LOGGER.error(LogCreator.console("NO ACK FROM EFTPOS!"));
                return null;
            }
            // start session command end
            Thread.sleep(400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(new byte[]{DLE, ETX});       // start session
            byte[] message = formPacket(command);
            baos.write(message);                    // message
            baos.write(new byte[]{DLE, STX});       // end session
            baos.write(getLRC(message)); // lrc

            received = null;
            sendPacket(baos.toByteArray());

            started = System.currentTimeMillis();
            do {
                Thread.sleep(20);
                response = ResponseHandler.parseUCS(received);
            } while (!UCSMessage.ACK.toString().equals(response) && System.currentTimeMillis() - started < 30000);

            if (UCSMessage.ACK.toString().equals(response)) {
                LOGGER.error(LogCreator.console("NO ACK. SEND NAC!"));
                sendPacket(new byte[]{NAC});
                return null;
            }
            sendPacket(new byte[]{EOT});

        } catch (IOException | InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()), ex);
        }
        return null;
    }

    private byte getLRC(byte[] buf) {
        byte lrc = 0;
        int i;

        for (i = 0; i < buf.length; i++)
            lrc ^= buf[i];
        return lrc;
    }

    // Формируем message для отправки сообщения на EFTPOS сессии
    private byte[] formPacket(UCSCommand command) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        byte classType = command.getClassType().getOperationClass();
        byte operationCode = command.getClassType().getOperationCode();
        result.write(classType);
        result.write(operationCode);
        result.write(TERMINAL_ID);
        byte[] len = toByteArray(command.getData().length);
        result.write(len);
        result.write(command.getData());

        return result.toByteArray();
    }

    private byte[] toByteArray(int value) {
        return new byte[]{
                (byte) (value >> 8),
                (byte) value};
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.RXCHAR && event.getEventValue() > 0) {
            try {
                received = serialPort.readBytes();
                currentResponse = ResponseHandler.parseUCS(received);
                LOGGER.debug(LogCreator.logInput(received));
            } catch (SerialPortException ex) {
                LOGGER.error(LogCreator.console(ex.getMessage()), ex);
            }
        }
    }

    void close() throws SerialPortException {
        if (serialPort.isOpened()) {
            serialPort.closePort();
        }
    }

    String getCurrentCommand() {
        return currentCommand;
    }

    String getCurrentResponse() {
        return currentResponse;
    }
}
