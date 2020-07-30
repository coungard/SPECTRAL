package ru.app.network.rmi;

import ru.app.main.Settings;

import java.io.UnsupportedEncodingException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.util.Scanner;

public class RmiClient {
    // "obj" is the reference of the remote object
    RmiServerInterface obj = null;
    static Scanner scanner = new Scanner(System.in);

    private String send(String command) {
        try {
            obj = (RmiServerInterface) Naming.lookup("//127.0.0.1/RmiServer");
            return obj.send(command);
        } catch (Exception e) {
            System.err.println("RmiClient exception: " + e);
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        RmiClient cli = new RmiClient();
        System.out.println("RmiClient v." + Settings.VERSION + " started!");
        while (true) {
            System.out.println("Enter the command for emulator:");
            String command = scanner.nextLine();
            System.out.println("wait...");
            if (command.equals("exit"))
                break;
            String answer = cli.send(command);
            System.out.println("answer: " + answer);
        }
    }
}