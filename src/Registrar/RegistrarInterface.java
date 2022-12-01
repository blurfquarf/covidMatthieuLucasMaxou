package Registrar;

import javax.crypto.SecretKey;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public interface RegistrarInterface extends Remote {

    ArrayList<byte[]> makeSecretsForCF(String name, int btw, String adress) throws NoSuchAlgorithmException, InvalidKeySpecException, RemoteException;

    byte[] generateCFPseudonym(String name, byte[] s, String location, int d) throws NoSuchAlgorithmException, RemoteException;

    ArrayList<byte[]> makeInitialSecretsForCF(String name, int btw, String adress) throws NoSuchAlgorithmException, InvalidKeySpecException, RemoteException;

    Map<byte[], byte[]> generateTokens(String telefoonnr) throws NoSuchAlgorithmException, RemoteException, InvalidKeyException, SignatureException;

}


