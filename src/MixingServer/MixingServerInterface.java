package MixingServer;

import MatchingService.MatchingServiceImpl;
import MatchingService.MatchingServiceInterface;
import Registrar.RegistrarInterface;

import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public interface MixingServerInterface extends Remote {
    boolean isValidDay(byte[] token) throws RemoteException, ParseException;
    boolean isUnused(byte[] token) throws RemoteException;
    boolean isValidToken(byte[] token, byte[] signature ) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException, NotBoundException;
    byte[] addCapsule(String time, byte[] token, byte[] signature, String hash) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException, NotBoundException;

    void flushCapsules() throws RemoteException, NotBoundException;
    JList<String> showTokens() throws RemoteException;
    JList<String> showCapsules() throws RemoteException;
    void sendHashesTokensTimes(byte[] hash, byte[] token, LocalDateTime localDateTime) throws RemoteException, NotBoundException;
    }
