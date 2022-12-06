package MixingServer;
import MatchingService.MatchingServiceImpl;
import Registrar.RegistrarInterface;

import javax.swing.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class MixingServerImpl extends UnicastRemoteObject implements MixingServerInterface {

    PrivateKey privateKey;
    PublicKey publicKey;

    ArrayList<byte[]> usedTokens;
    ArrayList<Capsule> capsuleList;
    //equivalent van 3 dagen
    int timeToHoldCapsules= 3;

    public MixingServerImpl() throws Exception{
        usedTokens= new ArrayList<>();
        capsuleList=new ArrayList<>();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
        KeyPair pair = kpg.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();
    }

    //hash hier is QR code hash die per dag hetzelfde is (ander pseudonym elke dag)
    public byte[] addCapsule(String time, byte[] token, byte[] signature, String hash) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException, NotBoundException {

        //signature op token geldig? check met PK registrar
        boolean isSignatureValid=isValidToken(token, signature);

        //token vandaag geldig?
        boolean isDayValid = isValidDay(token);
        boolean isunused = isUnused(token);

        //time when token was sent to server
        System.out.println(time);

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime d1 = LocalDateTime.parse(time.substring(0, time.length()-7), f);


        //capsule without random, random is no longer necessary
        Capsule capsule = new Capsule(token, signature, hash, d1);

        byte[] signedHash;
        if(isDayValid && isSignatureValid && isunused){
            System.out.println("signing hash");

            //add capsule to mixing server list of capsules
            capsuleList.add(capsule);
            usedTokens.add(token);
            signedHash = signHash(hash).values().iterator().next();
        } else {
            System.out.println("hash not signed");
            signedHash = new byte[1];
        }
        System.out.println("signed hash: "+ Arrays.toString(signedHash));
        return signedHash;
    }

    public Map<byte[], byte[]> signHash(String hash) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Map<byte[], byte[]> ts = new HashMap<>();

        byte[] bytes = new BigInteger(hash, 2).toByteArray();


        System.out.println(Arrays.toString(bytes));


        Signature signatureEngine = Signature.getInstance("SHA1withRSA");
        signatureEngine.initSign(privateKey);
        signatureEngine.update(bytes);

        byte[] signature = signatureEngine.sign();

        ts.put(bytes, signature);
        return ts;

    }

    public boolean isValidToken(byte[] token, byte[] signature) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException, NotBoundException {
        Signature signatureEngine = Signature.getInstance("SHA1withRSA");

        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");

        signatureEngine.initVerify(registrarImpl.getPK());
        signatureEngine.update(token);
        boolean valid = signatureEngine.verify(signature);
        System.out.println("tokenIsValid: " + valid);
        return valid;
    }

    public boolean isValidDay(byte[] token) throws RemoteException {
        LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        System.out.println("Huidige tijd: " + today);
        byte[] dateToken = subbytes(token, 0,22);
        String s = new String(dateToken);
        String s1 = s.substring(0, s.length()-3);
        System.out.println(s1);



        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime d1 = LocalDateTime.parse(s.substring(0, s.length()-3), f);



        LocalDateTime now = LocalDateTime.parse(today.format(f), f);
        System.out.println("now: "+ now);
        System.out.println("bytes: "+d1);

        boolean valid = today.minusMinutes(2).isBefore(d1);
        System.out.println("valid: "+valid);
        if(valid) System.out.println("Day is valid!");
        return valid;
    }

    public boolean isUnused(byte[] token) throws RemoteException {
        for (int i = 0; i < usedTokens.size(); i++) {
            byte[] temp = usedTokens.get(i);
            if(Arrays.equals(temp, token)) {
                return false;
            }
        }
        System.out.println("Unused!");
        return true;
    }

    //method used to get first 10 bytes
    public static byte[] subbytes(byte[] source, int srcBegin, int srcEnd) {
        byte destination[];

        destination = new byte[srcEnd - srcBegin];
        System.arraycopy(source, srcBegin, destination, 0, srcEnd - srcBegin);
        return destination;
    }

    public void flushCapsules(MatchingServiceImpl matchingServiceImpl) throws RemoteException{
        //shuffle the arraylist of capsules
        Collections.shuffle(capsuleList);

        // flush to the MatchingServer and empty the capsuleList
        for (Capsule temp : capsuleList) {
            matchingServiceImpl.send(temp.getTime(), temp.getHash(), temp.getToken(), temp.getSignature());
        }
        capsuleList = new ArrayList<>();
    }

    public PublicKey getPK() {
        return this.publicKey;
    }

}
