package MatchingService;

import MixingServer.Capsule;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public interface MatchingServiceInterface extends Remote {

    void sendFromMixing(LocalDateTime time, byte[] hash, byte[] token, byte[] signature) throws RemoteException;

    void sendFromDoctor(LocalDateTime time, byte[] hash, byte[] token, byte[] signature, int random, byte[] completePacket, byte[] completePacketSignature, String name) throws RemoteException, NotBoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException;

    ArrayList<byte[]> getMixingServerCapsuleListToken() throws RemoteException;

    ArrayList<byte[]> getMixingServerCapsuleListSignature() throws RemoteException;

    ArrayList<byte[]> getMixingServerCapsuleListHash() throws RemoteException;

    ArrayList<LocalDateTime> getMixingServerCapsuleListTime() throws RemoteException;

    ArrayList<Integer> getDoctorCapsuleListRandom() throws RemoteException;

    ArrayList<byte[]> getDoctorCapsuleListToken() throws RemoteException;

    ArrayList<byte[]> getDoctorCapsuleListSignature() throws RemoteException;

    ArrayList<byte[]> getDoctorCapsuleListHash() throws RemoteException;

    ArrayList<LocalDateTime> getDoctorCapsuleListTime() throws RemoteException;

    void sendCriticalTuples(byte[] hash, LocalDateTime time) throws RemoteException;

    ArrayList<LocalDateTime> getTimes() throws RemoteException;

    ArrayList<byte[]> getHashes() throws RemoteException;

    void sendHashesTokensTimes(byte[] hash, byte[] token, LocalDateTime localDateTime) throws RemoteException, NotBoundException;

    ArrayList<LocalDateTime> getNewTimes() throws RemoteException;

    ArrayList<byte[]> getNewHashes() throws RemoteException;

    ArrayList<byte[]> getNewTokens() throws RemoteException;


    }
