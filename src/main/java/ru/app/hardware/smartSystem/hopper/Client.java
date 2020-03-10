package ru.app.hardware.smartSystem.hopper;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.protocol.cctalk.CCTalkCommand;
import ru.app.protocol.cctalk.Command;
import ru.app.util.LogCreator;
import ru.app.util.StreamType;
import ru.app.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Общий клиент для Smart Hopper-a, работающий на протоколе CC2 (расширенный cctalk).
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private SerialPort serialPort;
    private final byte DESTINATION_ADDRESS = 0x07;
    private final byte SOURCE_ADDRESS = 0x01;
    private byte[] received;
    private byte[] transmit;

    private byte[] receivedTmp;
    private byte[] transmitTmp;

    private long activityDate;
    private static Command hopperCommand;

    public Client(String portName) throws SerialPortException {
        serialPort = new SerialPort(portName);
        serialPort.openPort();

        serialPort.setParams(SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_2,
                SerialPort.PARITY_NONE);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPort.addEventListener(new PortReader());

        LOGGER.info(LogCreator.console("Initialization port " + portName + " was succesfull!"));
    }

    public synchronized byte[] sendMessage(Command command) {
        hopperCommand = command;
        transmit = formPacket(command.getCommandType().getCode(), command.getData());
        try {
            if (accessLog(transmit, StreamType.OUTPUT))
                LOGGER.info(LogCreator.logOutput(transmit));
            serialPort.writeBytes(transmit);
            long start = Calendar.getInstance().getTimeInMillis();
            do {
                Thread.sleep(10);
            } while (Calendar.getInstance().getTimeInMillis() - start < 1200 && received == null);
        } catch (SerialPortException | InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
        return received;
    }

    private byte[] formPacket(int command, byte[] data) {
        if (data == null) data = new byte[]{};
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        raw.write(DESTINATION_ADDRESS);
        raw.write((byte) data.length);
        raw.write(SOURCE_ADDRESS);
        raw.write((byte) command);
        try {
            raw.write(data);
        } catch (IOException ignored) {
        }
        raw.write(Utils.checksum(raw.toByteArray()));
        return raw.toByteArray();
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR) {
                try {
                    byte[] input = serialPort.readBytes();
                    boolean reply = true;
                    ByteArrayOutputStream msg = new ByteArrayOutputStream();
                    for (int i = 0; i < input.length; i++) {
                        if (i < transmit.length) {
                            if (transmit[i] != input[i])
                                reply = false;
                        } else
                            msg.write(input[i]);
                    }
                    if (!reply)
                        LOGGER.error(LogCreator.console("No reply from hopper!"));

                    received = msg.toByteArray();
                    if (accessLog(received, StreamType.INPUT))
                        LOGGER.debug(LogCreator.logInput(msg.toByteArray()));
                } catch (SerialPortException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                }
            }
        }
    }

    private boolean accessLog(byte[] buffer, StreamType type) {
        if (hopperCommand.getCommandType() != CCTalkCommand.MC_REQUEST_STATUS) {
            return true;
        }
        switch (type) {
            case INPUT:
                if (!Arrays.equals(buffer, receivedTmp)) {
                    receivedTmp = buffer;
                    return true;
                }
                break;
            case OUTPUT:
                if (!Arrays.equals(buffer, transmitTmp)) {
                    transmitTmp = buffer;
                    return true;
                }
                break;
        }

        long timestamp = System.currentTimeMillis();
        if (timestamp - activityDate > 60000) {
            activityDate = timestamp;
            return true;
        }
        return false;
    }

    public static Command getHopperCommand() {
        return hopperCommand;
    }

    public String getCurrentCommand() {
        return hopperCommand.toString();
    }

    public void close() {
        try {
            if (serialPort.isOpened())
                serialPort.closePort();
        } catch (SerialPortException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
    }
}
