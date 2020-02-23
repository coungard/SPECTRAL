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
import java.util.Arrays;

public class Client implements SerialPortEventListener {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private static final String TERMINAL_ID = "0019999303"; // 10 chars
    private SerialPort serialPort;
    private byte[] received;
    private static final long delayENQ = 3000;
    private static final long delaySTX = 200;

    /*
        State during which we don't process incoming messages (ENQ -> EOT)
     */
    private boolean transaction = false;

    public static final byte DLE = (byte) 0x10;    // каждое сообщение начинается с DLE/STX и заканчивается DLE/ETX
    public static final byte STX = (byte) 0X02;
    public static final byte ETX = (byte) 0x03;
    private static final byte EOT = (byte) 0x04;    // завершение сессии передачи данных
    private static final byte ENQ = (byte) 0x05;    // инициация сессии передачи данных
    private static final byte ACK = (byte) 0x06;    // успех
    private static final byte NAC = (byte) 0x15;    // неудача

    private String currentCommand;
    private String currentResponse;

    private String response;
    private String currentMessage;

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

        LOGGER.info(LogCreator.console("Port " + portName + " initialized"));
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

    private synchronized void sendACK() {
        received = null;
        currentCommand = "ACK";
        try {
            LOGGER.debug(LogCreator.logOutput(new byte[]{ACK}));
            serialPort.writeByte(ACK);
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
        transaction = true;
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
            transaction = false;

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
        } while (!UCSMessage.ACK.toString().equals(response) && System.currentTimeMillis() - started < 10000);

        if (!UCSMessage.ACK.toString().equals(response)) {
            LOGGER.error(LogCreator.console("\n\n\nERROR TIMEOUT!"));
            sendPacket(new byte[]{NAC});
            return true;
        }
        return false;
    }

    /**
     * <p>Формирование сообщения по протоколу EFT-POS, в который не входят дефолтные DLE/ETX/STX/ENQ/ACK/LRC символы, использующиеся
     * для инициации начала/конца сообщения, очереди, контрольной суммы и т.д.</p><br> Сообщение делится на 5 частей:
     * <ol><li>Class | 1 Char | 1 байт. Класс операции </li>
     * <li>Code | 1 Char | 1 байт. Код операции </li>
     * <li>Terminal ID | 10 Numeric | 10 байт. Идентификатор терминала, присваивается эквайрером</li>
     * <li>Length | 2 Char | 2 байта. Длина последующего поля с данными</li>
     * <li>Data | Length Alpha | n bytes. Поле с данными</li></ol>
     *
     * @param command UCS команда
     * @return сформированный пакет данных
     * @throws IOException if shit happens
     */
    private byte[] formPacket(UCSCommand command) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte classType = command.getClassType().getOperationClass();
        byte operationCode = command.getClassType().getOperationCode();
        result.write(classType);
        result.write(operationCode);
        byte[] tid = TERMINAL_ID.getBytes();
        result.write(tid);
        byte[] len = Utils.getASCIIlength(command.getData().length);
        result.write(len);
        result.write(command.getData());

        return result.toByteArray();
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.RXCHAR && event.getEventValue() > 0) {
            try {
                received = serialPort.readBytes();
                currentResponse = ResponseHandler.parseUCS(received);
                LOGGER.debug(LogCreator.logInput(received));
                if (received != null) parsing(received);
            } catch (SerialPortException ex) {
                LOGGER.error(LogCreator.console(ex.getMessage()), ex);
            }
        }
    }

    private void parsing(byte[] received) {
        if (received.length == 1) {
            switch (received[0]) {
                case ENQ:
                    sendACK();
                    break;
                case EOT:
                    break;
                case ACK:
                    break;
                case NAC:
                    break;
                default:
                    LOGGER.error("INVALID BYTE FROM POST TERMINAL!");
            }
        } else {
            boolean incorrect = false;
            byte[] start = new byte[]{received[0], received[1]};
            if (!Arrays.equals(start, new byte[]{DLE, STX})) {
                LOGGER.error("Wrong starting DLE/STX bytes from POST Terminal!");
                incorrect = true;
            }

            byte[] end = new byte[]{received[received.length - 3], received[received.length - 2]};
            if (!Arrays.equals(end, new byte[]{DLE, ETX})) {
                LOGGER.error("Wrong ending DLE/ETX bytes from POST Terminal!");
                incorrect = true;
            }

            ByteArrayOutputStream msg = new ByteArrayOutputStream();
            for (int i = 2; i < received.length - 3; i++) {
                msg.write(received[i]);
            }
            byte lrc = Utils.getLRC(msg.toByteArray());

            if (lrc != received[received.length - 1]) {
                LOGGER.error("Wrong lrc from POST Terminal!");
                incorrect = true;
            }

            ByteArrayOutputStream logic = new ByteArrayOutputStream();
            if (msg.size() > 14) {
                byte[] logicArray = msg.toByteArray();
                for (int i = 14; i < logicArray.length; i++) {
                    logic.write(logicArray[i]);
                }
                String temp = Utils.getAsciiFromBuffer(logic.toByteArray());
                if (temp != null && !temp.startsWith("1"))
                    currentMessage = temp;
            }

            if (received.length > 4)
                response = "" + (char) received[2] + (char) received[3];
            if (!incorrect && response != null && !response.equals("")) {
                LOGGER.info(LogCreator.console("response = " + response + (currentMessage == null ? "" : " , message = " + currentMessage)));
                sendACK();
            }
        }


//        switch (response) { // todo...
//            case "":
//                break;
//            case "31":
//            case "51":
//            case "54":
//                LOGGER.info("Timeout 3 sec");
//                Thread.sleep(3000);
//                break;
//            case "60":
//            case "32":
//                LOGGER.info("Timeout 10 sec");
//                Thread.sleep(10000);
//                break;
//            case "5X":
//                LOGGER.info("Timeout 15 sec");
//                Thread.sleep(15000);
//                break;
//        }
        response = "";
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

    @Override
    public String toString() {
        return "UCS-THREAD";
    }
}
