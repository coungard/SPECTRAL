import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

class Manager {
    private SerialPort serialPort;
    private StringBuilder response = new StringBuilder();

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

    synchronized String sendMessage(CCTalkCommand command) {
        String result = null;
        byte[] crcPacket = formPacket(command.commandType.getCode(), command.getData());
        logPacket(crcPacket, false);

        try {
            serialPort.writeBytes(crcPacket);

            byte[] encrypt = encryptPacket(crcPacket);
            logPacket(encrypt, true);
            serialPort.writeBytes(encrypt);

            long start = Calendar.getInstance().getTimeInMillis();
            do {
                if (response.length() == 0) Thread.sleep(10);
            } while (Calendar.getInstance().getTimeInMillis() - start < 1200 && response.length() == 0);
            result = response.length() == 0 ? null : response.toString();
            response.setLength(0);
        } catch (SerialPortException | InterruptedException ex) {
            ex.printStackTrace();
        }
        return result == null ? "null\n" : result;
    }

    private byte[] encryptPacket(byte[] crcPacket) {
        byte[] toEncrypt = java.util.Arrays.copyOfRange(crcPacket, 2, crcPacket.length);
        BNVEncode.BNV_encrypt(toEncrypt);
        System.arraycopy(toEncrypt, 0, crcPacket, 2, toEncrypt.length);

        return crcPacket;
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
                StringBuilder builder = new StringBuilder();
                try {
                    Thread.sleep(400);
                    byte[] received = serialPort.readBytes();
                    System.out.println("input <<  " + Utils.byteArray2HexString(received));

                    builder.append("input <<  ").append(Utils.byteArray2HexString(received)).append("\n");
                    App.textArea.setText(App.textArea.getText() + "input <<  " + Utils.byteArray2HexString(received) + "\n");
                } catch (InterruptedException | SerialPortException ex) {
                    ex.printStackTrace();
                }
                response = builder;
            }
        }
    }

    synchronized private void logPacket(byte[] buffer, boolean encrypted) {
        System.out.println((encrypted ? "output (encrypted) >> " : "output >> ") + Utils.byteArray2HexString(buffer));
        App.textArea.setText(App.textArea.getText() + "output (encrypted) >>  " +Utils.byteArray2HexString(buffer) + "\n");
    }
}
