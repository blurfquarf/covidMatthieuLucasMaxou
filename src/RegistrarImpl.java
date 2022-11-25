//import java.rmi.RMISecurityManager;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDate;

public class RegistrarImpl extends UnicastRemoteObject implements RegistrarInterface{

    //String[] keyTime;

    //HashMap<Business, String[]> identifier = new HashMap<>();

    serverDB registrarDB = new serverDB();



    public RegistrarImpl() throws Exception {
        super(1099/*, new RMISSLClientSocketFactory(),
                new RMISSLServerSocketFactory()*/);
    }

    public void sendInfo(Business b) throws RemoteException{
    }

    public void makeMasterKey(Business b) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey S = kg.generateKey();

        registrarDB.addIdentifiers(b, S, LocalDate.now());

        ArrayList<SecretKey> derivedKeys = makeSecretsForCF(b);
    }

    public ArrayList<SecretKey> makeSecretsForCF(Business b) throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKey masterSecret = registrarDB.getSecretKey(b);
        ArrayList<SecretKey> derivedKeys = new ArrayList<>();
        SecretKeyFactory keyFac;

        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = LocalDate.now().plusDays(i);
            String tempDay = currentDay.toString();
            byte[] day = tempDay.getBytes();
            char[] ID = (masterSecret + String.valueOf(b.getBtw())).toCharArray();
            PBEKeySpec pbeKeySpec = new PBEKeySpec(ID, day, 1000);
            keyFac = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_256");
            SecretKey finalSecret = keyFac.generateSecret(pbeKeySpec);
            derivedKeys.add(finalSecret);
        }
        return derivedKeys;
    }

    public void sendString(String s) throws RemoteException {
        /*testMessages.add(s);
        System.out.println(testMessages);*/
    }

    public static void main(String args[]) throws Exception {
        try {
            //System.setProperty("java.rmi.server.hostname", "192.168.1.51");

            Registry registry = LocateRegistry.createRegistry(1099/*, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory()*/);
            registry.rebind("RegistrarService", new RegistrarImpl());
            System.out.println("system is ready");
        }
         catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateCFPseudonym(Business b, SecretKey s, String location, LocalDate d) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String sb = s + location + d;
        byte[] pseudonym = digest.digest(sb.getBytes(StandardCharsets.UTF_8));
        registrarDB.setPseudonym(b, pseudonym);
    }


    public String generateQRCode(Business b) throws NoSuchAlgorithmException{
        int randomNumber=(int) (1000*Math.random());
        int CF= b.getBtw();
        byte[] pseudoniem= registrarDB.getPseudonym(b);
        String pseudoniemstring= new String(pseudoniem, StandardCharsets.UTF_8);

        //hash maken van random number en pseudoniem
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String s = randomNumber + pseudoniemstring;
        byte[] QRHash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
        String finalQRCode=  randomNumber+CF+new String(QRHash, StandardCharsets.UTF_8);
        System.out.println(finalQRCode);
        return finalQRCode;
    }

}

