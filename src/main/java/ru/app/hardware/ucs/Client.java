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
import ru.app.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Client implements SerialPortEventListener {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private static final String TERMINAL_ID = "0019999303"; // 10 chars
    private SerialPort serialPort;
    private byte[] received;
    private static final long delayENQ = 3000;
    private static final long delaySTX = 200;

    public static final byte DLE = (byte) 0x10;    // каждое сообщение начинается с DLE/STX и заканчивается DLE/ETX
    public static final byte STX = (byte) 0X02;
    public static final byte ETX = (byte) 0x03;
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
        received = null;
        try {
            currentCommand = ResponseHandler.parseUCS(packet);
            LOGGER.debug(LogCreator.logOutput(packet));
            serialPort.writeBytes(packet);
        } catch (SerialPortException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()), ex);
        }
    }

    private synchronized void sendLRC(byte lrc) {
        received = null;
        currentCommand = "LRC";
        try {
            LOGGER.debug(LogCreator.logOutput(new byte[]{lrc}));
            serialPort.writeByte(lrc);
        } catch (SerialPortException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()), ex);
        }
    }

    synchronized void sendBytes(byte[] buffer) {
        try {
            sendPacket(new byte[]{ENQ});
            Thread.sleep(100);
            sendPacket(new byte[]{DLE, STX});
            sendPacket(buffer);
            sendPacket(new byte[]{DLE, ETX});
            sendLRC(Utils.getLRC(buffer));
        } catch (InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()), ex);
        }
    }

    synchronized void sendMessage(UCSCommand command) {
        LOGGER.info(LogCreator.console(command.toString()));
        try {
            sendPacket(new byte[]{ENQ});
            if (ACKfailed()) return;

            Thread.sleep(100);
            sendPacket(new byte[]{DLE, STX});       // start session
            byte[] message = formPacket(command);
            sendPacket(message);                    // message
            sendPacket(new byte[]{DLE, ETX});       // end session
            sendLRC(Utils.getLRC(message)); // lrc

            if (ACKfailed()) return;

            sendPacket(new byte[]{EOT});

        } catch (IOException | InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()), ex);
        }
    }

    private boolean ACKfailed() {
        String response;
        long started = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                LOGGER.error(LogCreator.console(ex.getMessage()), ex);
                return true;
            }
            response = ResponseHandler.parseUCS(received);
        } while (!UCSMessage.ACK.toString().equals(response) && System.currentTimeMillis() - started < delaySTX);

        if (!UCSMessage.ACK.toString().equals(response)) {
            LOGGER.error(LogCreator.console("\n\n\nERROR TIMEOUT!"));
            sendPacket(new byte[]{NAC});
            return true;
        }
        return false;
    }

    // Формируем message для отправки сообщения на EFTPOS сессии
    private byte[] formPacket(UCSCommand command) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        byte classType = command.getClassType().getOperationClass();
        byte operationCode = command.getClassType().getOperationCode();
        result.write(classType);
        result.write(operationCode);

        byte[] tid = TERMINAL_ID.getBytes();
        result.write(tid);
        byte[] len = getASCIIlength(command.getData().length);
        result.write(len);
        result.write(command.getData());

        return result.toByteArray();
    }

    private byte[] getASCIIlength(int length) {
        char[] temp = new char[]{'0', '0'};
        String len = "" + length;

        if (len.length() == 1) {
            temp[1] = len.charAt(0);
        }
        if (len.length() == 2) {
            temp[0] = len.charAt(0);
            temp[1] = len.charAt(1);
        }
        if (len.length() > 2) {
            throw new RuntimeException("Длина не должна превышать 99 (2-значное число)!");
        }
        return new byte[]{
                (byte) temp[0],
                (byte) temp[1]
        };
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
