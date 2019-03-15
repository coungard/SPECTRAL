import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

class Manager {
    private SerialPort serialPort;
    private byte[] received;

    Manager(String portName) throws SerialPortException {
        serialPort = new SerialPort(portName);
        serialPort.openPort();

        serialPort.setParams(SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPort.addEventListener(new PortReader());

        System.out.println("Initialization port " + portName + " was succesfull!");
    }

    synchronized void sendMessage(CCTalkCommand command) {
        byte[] crcPacket = formPacket(command.commandType.getCode(), command.getData());
        logPacket(crcPacket, "output >> ");

        try {
            serialPort.writeBytes(crcPacket);

            byte[] encrypt = encryptPacket(crcPacket);
            logPacket(encrypt, "output(encrypted) >> ");
            serialPort.writeBytes(encrypt);

            long start = Calendar.getInstance().getTimeInMillis();
            do {
                if (received == null) Thread.sleep(10);
            } while (Calendar.getInstance().getTimeInMillis() - start < 1200 && received == null);
        } catch (SerialPortException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    void sendBytes(byte[] bytes) {
        try {
            serialPort.writeBytes(bytes);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    private byte[] encryptPacket(byte[] packet) {
        byte[] toEncrypt = Arrays.copyOfRange(packet, 2, packet.length);
        BNVEncode.BNV_encrypt(toEncrypt);
        System.arraycopy(toEncrypt, 0, packet, 2, toEncrypt.length);

        return packet;
    }

    private byte[] decryptPacket(byte[] packet) {
        byte[] toDecrypt = Arrays.copyOfRange(packet, 2, packet.length);
        BNVEncode.BNV_decrypt(toDecrypt);
        System.arraycopy(toDecrypt, 0, packet, 2, toDecrypt.length);

        return packet;
    }

    private byte[] formPacket(int command, byte[] data) {
        if (data == null) data = new byte[]{};
        Crc16 crc16 = calcCrc16(command, data);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        raw.write((byte) 0x28); // destination address
        raw.write((byte) data.length);
        raw.write((byte) (crc16.getCRC() & 0xff));// LSB
        raw.write((byte) command);
        try {
            raw.write(data);
        } catch (IOException ignored) {
        }
        raw.write((byte) ((crc16.getCRC() >> 8) & 0xff)); // MSB

        return raw.toByteArray();
    }

    private Crc16 calcCrc16(int command, byte[] data) {
        Crc16 crc16 = new Crc16();
        crc16.update((byte) 0x28); // destination address
        crc16.update((byte) data.length); // data length
        crc16.update((byte) command); // command
        crc16.update(data); // data

        return crc16;
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR) {
                try {
                    Thread.sleep(800);
                    received = serialPort.readBytes();
                    logPacket(received, "input << ");
                    byte[] decrypt = decryptPacket(received);
                    logPacket(decrypt, "input(decrypted) << ");
                } catch (InterruptedException | SerialPortException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    synchronized private void logPacket(byte[] buffer, String description) {
        System.out.println((description) + Arrays.toString(buffer));
        App.textArea.setText(App.textArea.getText() + description + Utils.byteArray2HexString(buffer) + "\n");
    }
}
