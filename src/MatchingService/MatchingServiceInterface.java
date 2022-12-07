package MatchingService;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.time.LocalDateTime;

public interface MatchingServiceInterface extends Remote {

    void sendFromMixing(LocalDateTime time, byte[] hash, byte[] token, byte[] signature) throws RemoteException;



    void sendFromDoctor(LocalDateTime time, byte[] hash, byte[] token, byte[] signature, int random, byte[] completePacket, byte[] completePacketSignature, String name) throws RemoteException, NotBoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException;





    }
