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
    boolean isValidDay(byte[] token);
    boolean isUnused(byte[] token);
    boolean isValidToken(byte[] token, byte[] signature, RegistrarInterface registrarImpl ) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException;

    Map<byte[], byte[]> addCapsule(String time, byte[] token, byte[] signature, byte[] hash) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException, NotBoundException;

}
