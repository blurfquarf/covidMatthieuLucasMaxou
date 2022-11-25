//import java.rmi.RMISecurityManager;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDate;

public class RegistrarImpl extends UnicastRemoteObject implements RegistrarInterface {

    //String[] keyTime;

    //HashMap<Business, String[]> identifier = new HashMap<>();
    serverDB registrarDB;
    SecretKey mk;

    public RegistrarImpl(serverDB db, SecretKey mk) throws Exception {
        this.registrarDB = db;
        this.mk = mk;
        /* super(1099*//*, new RMISSLClientSocketFactory(),
                new RMISSLServerSocketFactory()*//*);*/
    }


    public ArrayList<byte[]> makeInitialSecretsForCF(String name, int btw, String adress) throws NoSuchAlgorithmException, InvalidKeySpecException {

        //registrarDB.getTimestamps().put(name, LocalDateTime.now().toString());

        System.out.println("entered Initial Make Secrets");
        //LocalDateTime dateTime = registrarDB.getTimestamp(name);
        //System.out.println(dateTime);

        //SecretKey masterSecret = registrarDB.getSecretKey(b);
        ArrayList<byte[]> derivedKeys = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            derivedKeys.add(generateSecretKey(btw, i).getEncoded());
        }

        for (byte[] b: derivedKeys) {
            System.out.println(Arrays.toString(b));
        }

        //System.out.println(Arrays.toString(derivedKeys));

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

        if (dateTime.isBefore(LocalDateTime.now().minusMinutes((long) 1.16))){
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


    public byte[] generateCFPseudonym(String  name, byte[] s, String location, LocalDateTime d) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String sb = s + location + d;
        byte[] pseudonym = digest.digest(sb.getBytes(StandardCharsets.UTF_8));
        registrarDB.setPseudonym(name, pseudonym);
        return pseudonym;
    }
}

