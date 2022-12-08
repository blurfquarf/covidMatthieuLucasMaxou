package MatchingService;
import MixingServer.Capsule;
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
    //TODO: rmi fct vooor docter: alle logs van een infected user doorsturen
    // mbv rmi fct van registrar alle pseudonymen van een bepaalde dag op vragen
    // validity van user logs checken door hash te nemen van Ri en pseudonym
    // (kijken of een matcht door hash van RI en een van de pseuddonymen van registrar te hashen met hash gelever door user log)
    // als match gevonden => alle capsules die die hash bevatten en in het interval van de besmette user aanwezig waren markeren als "critical"
    // + Token van de user die dokter bezocht als "informed" markeren`


    // TODO: na bepaald tijdsinterval vb 1 dag, overblijvende tokens ("uninformed") doorsturen naar registrar


    ArrayList<Capsule> mixingServerCapsuleList;
    ArrayList<Capsule> doctorCapsuleList;



    public MatchingServiceImpl() throws Exception{
        mixingServerCapsuleList = new ArrayList<>();
        doctorCapsuleList = new ArrayList<>();
    }

                                                 //QR hash        //registrar token and signature
    public void sendFromMixing(LocalDateTime time, byte[] hash, byte[] token, byte[] signature)throws RemoteException{
        mixingServerCapsuleList.add(new Capsule(token, signature, hash, time));

        System.out.println("inbound hash from mixing server: " + Arrays.toString(mixingServerCapsuleList.get(0).getHash()));
    }

    public void sendFromDoctor(LocalDateTime time, byte[] hash, byte[] token, byte[] signature, int random, byte[] completePacket, byte[] completePacketSignature, String doctor) throws RemoteException, NotBoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        PublicKey doctorPK = getCorrectPK(doctor);

        //only add to list if valid!
        if (checkValidity(time, hash, token, signature, random, completePacket, completePacketSignature, doctorPK)) {
            System.out.println("data from doctor valid!");
            doctorCapsuleList.add(new Capsule(token, signature, hash, random, time));
        }
        System.out.println("inbound hash from doctor: " + Arrays.toString(doctorCapsuleList.get(0).getHash()));
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

    public byte[] concatenate(byte[] time, byte[] hash, byte[] token, byte[] signature, byte[] random) {
        ByteBuffer concatenation = ByteBuffer.allocate(time.length + hash.length + token.length + signature.length + random.length);
        concatenation.put(time);
        concatenation.put(hash);
        concatenation.put(token);
        concatenation.put(signature);
        concatenation.put(random);
        return concatenation.array();
    }

    public ArrayList<Capsule> getMixingServerCapsuleList() throws RemoteException{
        return mixingServerCapsuleList;
    }

    public ArrayList<Capsule> getDoctorCapsuleList() throws RemoteException{
        return doctorCapsuleList;
    }



}
