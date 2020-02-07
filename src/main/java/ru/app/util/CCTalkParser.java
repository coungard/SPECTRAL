package ru.app.util;

import org.apache.log4j.Logger;
import ru.app.protocol.cctalk.CCTalkCommand;
import ru.app.protocol.cctalk.Command;
import ru.app.protocol.cctalk.hopper.HopperCommand;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public abstract class CCTalkParser {
    private static final Logger LOGGER = Logger.getLogger(CCTalkParser.class.getName());
    private static final byte DESTINATION_ADDRESS = 0x07;
    private static final byte SOURCE_ADDRESS = 0x01;
    private static final byte ACK = 0x00;
    private static final byte NAK = 0x05;
    private static final int DEFAULT_LENGTH = 5;

    public static String parseCC2(Command command, byte[] message) {
        String wrong = checkValid(message);
        if (wrong != null)
            return wrong;

        byte type = message[3];
        byte[] data = getData(message);
        if (type == ACK) {
            if (data.length > 0) {
                return parseData(command, data);
            } else {
                return "ACK";
            }
        }
        if (type == NAK) {
            if (data.length > 0) {
                return parseNAK(data);
            } else {
                return "NAK";
            }
        }
        return "unknown";
    }

    private static String parseData(Command command, byte[] data) {
        CCTalkCommand ccTalkCommand = command.getCommandType();
        byte[] commandData = command.getData();

        HopperCommand hope = HopperCommand.valueOf(ccTalkCommand.getCode());
        if (hope == HopperCommand.MC_GET_NOTE_AMOUNT) {
            if (data.length == 2) {
                byte[] tmp = new byte[4];
                System.arraycopy(data, 0, tmp, 0, data.length);
                Utils.reverse(tmp);
                int amount = ByteBuffer.wrap(tmp).getInt();

                return "Amount [" + amount + "]";
            } else {
                LOGGER.warn("Invalid note length amount!");
                return "Invalid note length amount!";
            }
        }
        return null;
    }

    private static String parseNAK(byte[] data) {
        return null;
    }

    private static byte[] getData(byte[] message) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        for (int i = 0; i < message[1]; i++) {
            data.write(message[i + 4]);
        }
        return data.toByteArray();
    }

    /**
     * Проверка базовой валидации массива на адресацию, контрольную сумму и длину.
     *
     * @param msg cctalk buffer
     * @return null if buffer is valid, others if something wrong
     */
    private static String checkValid(byte[] msg) {
        if (msg.length < DEFAULT_LENGTH) {
            LOGGER.warn("Invalid message length!");
            return "Invalid message length!";
        }
        if (msg[0] != SOURCE_ADDRESS || msg[2] != DESTINATION_ADDRESS) {
            LOGGER.warn("Invalid Device Address!");
            return "Invalid Device Address!";
        }
        if (msg.length - DEFAULT_LENGTH != msg[1]) {
            LOGGER.warn("Invalid! Сurrent length differs from declared!");
            return "Invalid! Сurrent length differs from declared!";
        }
        byte[] content = new byte[msg.length - 1];
        System.arraycopy(msg, 0, content, 0, msg.length - 1);
        byte checksum = Utils.checksum(content);
        if (checksum != msg[msg.length - 1]) {
            LOGGER.warn("Invalid checksum!");
            return "Invalid checksum!";
        }
        return null;
    }


}
