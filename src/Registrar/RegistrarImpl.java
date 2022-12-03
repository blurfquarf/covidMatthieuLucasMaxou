package Registrar;//import java.rmi.RMISecurityManager;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDate;
import java.nio.ByteBuffer;


public class RegistrarImpl extends UnicastRemoteObject implements RegistrarInterface {

    //String[] keyTime;

    //HashMap<Business, String[]> identifier = new HashMap<>();
    serverDB registrarDB;
    PrivateKey mk;
    PublicKey pk;

    public RegistrarImpl(serverDB db, PrivateKey mk, PublicKey pk) throws Exception {
        this.registrarDB = db;
        this.mk = mk;
        this.pk = pk;
    }


    public PublicKey getServerPK(){
        return pk;
    }



    public boolean getUserByPhone(String PhoneNR){
        if (registrarDB.getRegisteredPhonenumbers().contains(PhoneNR)) {
            return true;
        }
        return false;
    }



    public ArrayList<byte[]> makeInitialSecretsForCF(String name, int btw, String adress) throws NoSuchAlgorithmException, InvalidKeySpecException {

        System.out.println("entered Initial Make Secrets");

        ArrayList<byte[]> derivedKeys = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            derivedKeys.add(generateSecretKey(btw, i).getEncoded());
        }

        registrarDB.setLocalDateTime(name, LocalDateTime.now());
        return derivedKeys;
    }

    public SecretKey generateSecretKey(int btw, int i) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_256");
        LocalDateTime currentMoment = LocalDateTime.now().plusMinutes(i);
        String tempDay = currentMoment.toString();


        char[] moment = tempDay.toCharArray();

        //System.out.println(moment);

        byte[] ID = (mk + String.valueOf(btw)).getBytes();


        PBEKeySpec pbeKeySpec = new PBEKeySpec(moment, ID, 1000);

        //SecretKey finalSecret = keyFac.generateSecret(pbeKeySpec);
        return keyFac.generateSecret(pbeKeySpec);

}

        public ArrayList<byte[]> makeSecretsForCF(String name, int btw, String adress) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //check if request is not too soon

        System.out.println("entered normal Make Secrets");
        LocalDateTime dateTime = registrarDB.getTimestamp(name);


        //should be 14
        if (dateTime.isBefore(LocalDateTime.now().minusMinutes((long) 13.9))){
            //byte[] ms = registrarDB.getSecretKey(b);
            //SecretKey masterSecret = new SecretKeySpec(ms, 0, ms.length, "AES");

            //SecretKey masterSecret = registrarDB.getSecretKey(b);
            ArrayList<byte[]> derivedKeys = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                derivedKeys.add(generateSecretKey(btw, i).getEncoded());
            }
            registrarDB.setLocalDateTime(name, LocalDateTime.now());
            return derivedKeys;
    }
        ArrayList<byte[]> emptyList = new ArrayList<>();
        return emptyList;
    }


    public byte[] generateCFPseudonym(String name, byte[] s, String location, int day) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String sb = Arrays.toString(s) + location + day;
        byte[] pseudonym = digest.digest(sb.getBytes(StandardCharsets.UTF_8));
        registrarDB.setPseudonym(name, pseudonym);

        registrarDB.setDays(name, day);

        return pseudonym;
    }

    public Map<byte[], byte[]> generateTokens(String telefoonnr) throws NoSuchAlgorithmException,
            RemoteException, InvalidKeyException, SignatureException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        Map<byte[], byte[]> ts = new HashMap<>();
        for (int i = 0; i < 48; i++) {
            double randomNumber = Math.random()*1000;
            LocalDate today = LocalDate.now();
            byte[] todayByteArray = today.toString().getBytes(StandardCharsets.UTF_8);
            //maak tokens en voeg deze toe aan de tokenMapping in de Registrar.serverDB
            byte[] date = today.toString().getBytes(StandardCharsets.UTF_8);
            date.toString();
            String sb = Double.toString(randomNumber);
            byte[] randomByteArray = digest.digest(sb.getBytes(StandardCharsets.UTF_8));
            byte[] token = joinByteArray(todayByteArray, randomByteArray);


            //signing with the private masterkey
            Signature signatureEngine = Signature.getInstance("SHA1withRSA");
            signatureEngine.initSign(mk);
            signatureEngine.update(token);


            //TODO ADD CURRENT DAY (2min period)
            byte[] signature = signatureEngine.sign();



            ts.put(token, signature);
            registrarDB.getTokenMappings().put(token, telefoonnr);
        }
        return ts;
    }


    public static byte[] joinByteArray(byte[] byte1, byte[] byte2) {

        return ByteBuffer.allocate(byte1.length + byte2.length)
                .put(byte1)
                .put(byte2)
                .array();

    }

    public PublicKey getPK() throws RemoteException{
        return pk;
    }
}
