package ru.app.util;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Formatter;
import java.util.List;

public class Utils {
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
    static String byteArray2HexString(byte[] buf) {
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
}
