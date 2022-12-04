package MixingServer;

import Registrar.RegistrarInterface;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;

public interface MixingServerInterface extends Remote {
    boolean isValidDay(byte[] token) throws RemoteException;
    boolean isUnused(byte[] token) throws RemoteException;
    boolean isValidToken(byte[] token, byte[] signature ) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException, NotBoundException;
    byte[] addCapsule(String time, byte[] token, byte[] signature, byte[] hash) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException, NotBoundException;
}
