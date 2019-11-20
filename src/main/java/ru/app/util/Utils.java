package ru.app.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class Utils {
    private static final Logger LOGGER = Logger.getLogger(Utils.class);

    /**
     * Переводим значение байта в лонг
     */
    private static long getByteValue(byte b) {
        return 0xff & b;
    }

    /**
     * Sun property pointing the main class and its arguments.
     * Might not be defined on non Hotspot VM implementations.
     */
    private static final String SUN_JAVA_COMMAND = "sun.java.command";

    /**
     * Перевод массива байтов в строку
     */
    public static String byteArray2String(byte[] buf, int offset, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < count; i++)
            sb.append(getByteValue(buf[i])).append(" ");

        return sb.toString();
    }

    /**
     * Перевод массива байтов в строку в шеснадцатиричном виде
     */
    static String bytes2hex(byte[] buf) {
        if (buf == null) return null;
        Formatter formatter = new Formatter();
        for (byte b : buf) formatter.format("%2X ", b);
        return formatter.toString();
    }

    /**
     * Инвертирование строки (по 2 символам - 16ричные значения)<p>Пример {"1B442C" ==> "2C441B"}
     *
     * @param s упорядоченный набор 16-ричных значений, идущих по 2 символа слитно друг за другом
     * @return обратный набор 16-ричных значений в виде String
     */
    public static String inverse(String s) {
        if ((null == s) || (s.length() <= 1)) {
            return s;
        }
        return inverse(s.substring(2)) + s.charAt(0) + s.charAt(1);
    }

    /**
     * Restart the current Java application
     *
     * @param runBeforeRestart some custom code to be run before restarting
     */
    public static void restartApplication(Runnable runBeforeRestart) throws IOException {
        try {
            // java binary
            String java = System.getProperty("java.home") + "/bin/java";
            // vm arguments
            List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
            StringBuilder vmArgsOneLine = new StringBuilder();
            for (String arg : vmArguments) {
                // if it's the agent argument : we ignore it otherwise the
                // address of the old application and the new one will be in conflict
                if (!arg.contains("-agentlib")) {
                    vmArgsOneLine.append(arg);
                    vmArgsOneLine.append(" ");
                }
            }
            // init the command to execute, add the vm args
            final StringBuffer cmd = new StringBuffer("" + java + " " + vmArgsOneLine);

            // program main and program arguments
            String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
            // program main is a jar
            if (mainCommand[0].endsWith(".jar")) {
                // if it's a jar, add -jar mainJar
                cmd.append("-jar ").append(new File(mainCommand[0]).getPath());
            } else {
                // else it's a .class, add the classpath and mainClass
                cmd.append("-cp ").append(System.getProperty("java.class.path")).append(" ").append(mainCommand[0]);
            }
            // finally add program arguments
            for (int i = 1; i < mainCommand.length; i++) {
                cmd.append(" ");
                cmd.append(mainCommand[i]);
            }
            // execute the command in a shutdown hook, to be sure that all the
            // resources have been disposed before restarting the application
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        Runtime.getRuntime().exec(cmd.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            // execute some custom code before restarting
            if (runBeforeRestart != null) {
                runBeforeRestart.run();
            }
            // exit
            System.exit(0);
        } catch (Exception e) {
            // something went wrong
            throw new IOException("Error while trying to restart the application", e);
        }
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()), ex);
        }
    }

    static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    static void reverse(byte[] array) {
        if (array != null) {
            int i = 0;

            for (int j = array.length - 1; j > i; ++i) {
                byte tmp = array[j];
                array[j] = array[i];
                array[i] = tmp;
                --j;
            }
        }
    }

    /**
     * concatinate two byte arrays
     *
     * @param first  array A
     * @param second array B
     * @return new byte array
     */
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] res = new byte[first.length + second.length];
        System.arraycopy(first, 0, res, 0, first.length);
        System.arraycopy(second, 0, res, first.length, second.length);
        return res;
    }

    /**
     * Деление платежа, приходящего с сервера(предпочтительно для эмулятора ccnet), куда включены монеты и бумажные купюры, исключая 200 и 2000 номиналы,
     * так как для этих банкнот необходима новая прошивка, отсутствующая на некоторых устройствах.<br/>
     * Пример: sum = 188 rub. return list {100, 50, 10, 10, 10, 5, 2, 1}
     *
     * @param sum общая сумма платежа
     * @return список распределенных номиналов
     */
    public static List<Integer> calculatePayment(int sum) {
        int[] nominals = new int[]{1, 2, 5, 10, 50, 100, 500, 1000, 5000};
        List<Integer> result = new ArrayList<>();
        int rest = sum;
        long start = System.currentTimeMillis();
        while (rest > 0 && System.currentTimeMillis() - start < 10000) {
            for (int i = nominals.length - 1; i >= 0; i--) {
                if (rest >= nominals[i]) {
                    result.add(nominals[i]);
                    rest = rest - nominals[i];
                    i++;
                }
            }
        }
        if (rest > 0) {
            LOGGER.warn("Warn! Rest from payment: " + rest + " rub");
        }
        return result;
    }

    /**
     * Алгоритм вычисления контрольной суммы. Исключительное ИЛИ всех байтов сообщения.
     *
     * @param buf передаваемый буффер с сообщением
     * @return байт lrc
     */
    public static byte getLRC(byte[] buf) {
        byte lrc = 0;
        int i;

        for (i = 0; i < buf.length; i++)
            lrc ^= buf[i];
        return lrc;
    }
}
