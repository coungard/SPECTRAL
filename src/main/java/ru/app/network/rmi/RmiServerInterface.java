package ru.app.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiServerInterface extends Remote {
    public void encashment() throws RemoteException;
}