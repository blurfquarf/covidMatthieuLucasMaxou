package MatchingService;
import MixingServer.Capsule;
import Registrar.ByteArrayHolder;
import Registrar.RegistrarInterface;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MatchingServiceImpl extends UnicastRemoteObject implements MatchingServiceInterface {
    ArrayList<Capsule> mixingServerCapsuleList;
    ArrayList<Capsule> doctorCapsuleList;
    ArrayList<ByteArrayHolder> criticalTuples;

    ArrayList<byte[]> newHashes;
    ArrayList<byte[]> newTokens;
    ArrayList<LocalDateTime> newTimes;




    public MatchingServiceImpl() throws Exception{
        mixingServerCapsuleList = new ArrayList<>();
        doctorCapsuleList = new ArrayList<>();
        criticalTuples = new ArrayList<>();
        newHashes = new ArrayList<>();
        newTokens = new ArrayList<>();
        newTimes = new ArrayList<>();
    }

                                                 //QR hash        //registrar token and signature
    public void sendFromMixing(LocalDateTime time, byte[] hash, byte[] token, byte[] signature)throws RemoteException{
        mixingServerCapsuleList.add(new Capsule(token, signature, hash, time));
    }

    public void sendFromDoctor(LocalDateTime time, byte[] hash, byte[] token, byte[] signature, int random, byte[] completePacket, byte[] completePacketSignature, String doctor) throws RemoteException, NotBoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        PublicKey doctorPK = getCorrectPK(doctor);

        //only add to list if valid!
        if (checkValidity(time, hash, token, signature, random, completePacket, completePacketSignature, doctorPK)) {
            System.out.println("data from doctor valid!");
            System.out.println("RANDOM: " + random);
            doctorCapsuleList.add(new Capsule(token, signature, hash, random, time));
        }
    }

    public PublicKey getCorrectPK(String doctor) throws RemoteException, NotBoundException {
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");
        return registrarImpl.getDoctorPK(doctor);
    }


    public boolean checkValidity(LocalDateTime time, byte[] hash, byte[] token, byte[] signature, int random, byte[] completePacket, byte[] completePacketSignature, PublicKey doctorPK) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] total = concatenate(time.toString().getBytes(), hash, token, signature, String.valueOf(random).getBytes());
        if (!Arrays.equals(total, completePacket)){
            return false;
        }

        //signature check on completePacket

        Signature signatureEngine = Signature.getInstance("SHA1withRSA");
        signatureEngine.initVerify(doctorPK);
        signatureEngine.update(completePacket);
        return signatureEngine.verify(completePacketSignature);
    }

    public byte[] concatenate(byte[] time, byte[] hash, byte[] token, byte[] signature, byte[] random){
        ByteBuffer concatenation = ByteBuffer.allocate(time.length + hash.length + token.length + signature.length + random.length);
        concatenation.put(time);
        concatenation.put(hash);
        concatenation.put(token);
        concatenation.put(signature);
        concatenation.put(random);
        return concatenation.array();
    }



    public ArrayList<byte[]> getMixingServerCapsuleListToken() throws RemoteException{
        ArrayList<byte[]> tokens = new ArrayList<>();
        for (Capsule c:mixingServerCapsuleList) {
            tokens.add(c.getToken());
        }
        return tokens;
    }

    public ArrayList<byte[]> getMixingServerCapsuleListSignature() throws RemoteException{
        ArrayList<byte[]> signatures = new ArrayList<>();
        for (Capsule c:mixingServerCapsuleList) {
            signatures.add(c.getSignature());
        }
        return signatures;
    }


    public ArrayList<byte[]> getMixingServerCapsuleListHash() throws RemoteException{
        ArrayList<byte[]> hashes = new ArrayList<>();
        for (Capsule c:mixingServerCapsuleList) {
            hashes.add(c.getHash());
        }
        return hashes;
    }


    public ArrayList<LocalDateTime> getMixingServerCapsuleListTime() throws RemoteException{
        ArrayList<LocalDateTime> times= new ArrayList<>();
        for (Capsule c:mixingServerCapsuleList) {
            times.add(c.getTime());
        }
        return times;
    }



    //////////////////////DOCTOR////////////////

    public ArrayList<byte[]> getDoctorCapsuleListToken() throws RemoteException{
        ArrayList<byte[]> tokens = new ArrayList<>();
        for (Capsule c:doctorCapsuleList) {
            tokens.add(c.getToken());
        }
        return tokens;
    }

    public ArrayList<byte[]> getDoctorCapsuleListSignature() throws RemoteException{
        ArrayList<byte[]> signatures = new ArrayList<>();
        for (Capsule c:doctorCapsuleList) {
            signatures.add(c.getSignature());
        }
        return signatures;
    }


    public ArrayList<byte[]> getDoctorCapsuleListHash() throws RemoteException{
        ArrayList<byte[]> hashes = new ArrayList<>();
        for (Capsule c:doctorCapsuleList) {
            hashes.add(c.getHash());
        }
        return hashes;
    }


    public ArrayList<LocalDateTime> getDoctorCapsuleListTime() throws RemoteException{
        ArrayList<LocalDateTime> times= new ArrayList<>();
        for (Capsule c:doctorCapsuleList) {
            times.add(c.getTime());
        }
        return times;
    }


    public ArrayList<Integer> getDoctorCapsuleListRandom() throws RemoteException{
        ArrayList<Integer> randoms = new ArrayList<>();
        for (Capsule c:doctorCapsuleList) {
            randoms.add(c.getRandom());
        }
        return randoms;
    }

    public void sendCriticalTuples(byte[] hash, LocalDateTime time) throws RemoteException{
        criticalTuples.add(new ByteArrayHolder(time, hash));
    }


    public ArrayList<byte[]> getHashes() throws RemoteException{
        ArrayList<byte[]> hashList = new ArrayList<>();
        for (int i = 0; i < criticalTuples.size(); i++) {
            hashList.add(criticalTuples.get(i).getByteArray());
        }
        return hashList;
    }

    public ArrayList<LocalDateTime> getTimes() throws RemoteException{
        ArrayList<LocalDateTime> times = new ArrayList<>();
        for (ByteArrayHolder b: criticalTuples) {
            times.add(b.getTime());
        }
        return times;
    }

    public void sendHashesTokensTimes(byte[] hash, byte[] token, LocalDateTime localDateTime) throws RemoteException, NotBoundException {
        newHashes.add(hash);
        newTokens.add(token);
        newTimes.add(localDateTime);
    }


    public ArrayList<LocalDateTime> getNewTimes() throws RemoteException{
        return newTimes;
    }
    public ArrayList<byte[]> getNewHashes() throws RemoteException{
        return newHashes;
    }
    public ArrayList<byte[]> getNewTokens() throws RemoteException{
        return newTokens;
    }

}
