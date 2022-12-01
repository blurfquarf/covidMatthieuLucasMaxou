package MixingServer;

import Registrar.RegistrarInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface MixingServerInterface extends Remote {
    boolean isValidDay(byte[] token);
    boolean isUnused(byte[] token);
    boolean isValidToken(byte[] token, byte[] signature, RegistrarInterface registrarImpl ) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException;

}
