package ru.app.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiServerInterface extends Remote {
    public String send(String command) throws RemoteException;
}