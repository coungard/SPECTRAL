import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.util.Arrays;
import java.util.Calendar;

public class Manager {
    SerialPort serialPort;
    StringBuilder response = new StringBuilder();

    Manager(String portName) throws SerialPortException {
        System.out.println("Инициализация ком порта: " + portName);
        serialPort = new SerialPort(portName);
        serialPort.openPort();

        serialPort.setParams(SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPort.addEventListener(new PortReader());

        System.out.println("Инициализация прошла успешно");
    }

    String sendBytes(int[] output) throws InterruptedException, SerialPortException {
        String result;
        serialPort.writeIntArray(output);
        long start = Calendar.getInstance().getTimeInMillis();
        do {
            if (response.length() == 0) Thread.sleep(10);
        } while (Calendar.getInstance().getTimeInMillis() - start < 1000 && response.length() == 0);
        result = response.length() == 0 ? null : response.toString();
        response.setLength(0);
        return result;
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR) {
                StringBuilder builder = new StringBuilder();
                try {
                    Thread.sleep(400);
                    int[] received = serialPort.readIntArray();
                    builder.append("INPUT:  ").append(Arrays.toString(received));
                    StringBuilder ascii = new StringBuilder();
                    if (received != null) {
                        for (int i = 0; i < received.length - 1; i++) {
                            ascii.append((char) received[i]);
                        }
                        builder.append(" ASCII: ").append(ascii);
                    }
                } catch (InterruptedException | SerialPortException e) {
                    e.printStackTrace();
                }
                response = builder;
            } else {
                System.out.println("какой то другой сигнал");
            }
//            if (event.isRXCHAR() && event.getEventValue() > 0) {
//                try {
//                    Thread.sleep(400);
//                    int[] received = serialPort.readIntArray();
//                    System.out.println("Input: " + Arrays.toString(received));
//                } catch (InterruptedException | SerialPortException e) {
////                } catch (SerialPortException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
}
