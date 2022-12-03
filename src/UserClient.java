import MixingServer.MixingServerInterface;
import Registrar.RegistrarInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.LocalDateTime;
import java.util.*;

public class UserClient {

    public static void main(String[] args) throws RemoteException, NotBoundException {

        Registry myRegistry = LocateRegistry.getRegistry("localhost",
                1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");

        MixingServerInterface mixingServerImpl = (MixingServerInterface) myRegistry.lookup("MixingService");

        try{
            Thread req = new Thread(() -> {
                try {
                    Scanner sc = new Scanner(System.in);

                    boolean userExists = true;
                    String phoneNr;
                    String temp = "";
                    while (userExists) {
                        System.out.println("enter a phoneNr:");
                        temp = sc.nextLine();
                        try{
                            userExists = registrarImpl.getUserByPhone(temp);
                        } catch (NullPointerException e) {
                            userExists = false;
                        }
                    }
                    phoneNr = temp;
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                    kpg.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
                    KeyPair pair = kpg.generateKeyPair();

                    User u = new User(phoneNr, pair.getPrivate(), pair.getPublic());

                    //token-signatureBytes
                    Map<byte[], byte[]> newtokens  = registrarImpl.generateTokens(u.getPhoneNr());

                /*    Iterator<Map.Entry<byte[], byte[]>> itr = newtokens.entrySet().iterator();
                    Signature signatureEngine = Signature.getInstance("SHA1withRSA");

                    while(itr.hasNext())
                    {
                        Map.Entry<byte[], byte[]> entry = itr.next();
                        byte[] s = entry.getValue();
                        signatureEngine.initVerify(registrarImpl.getPK());
                        signatureEngine.update(entry.getKey());
                        boolean valid = signatureEngine.verify(s);
                        System.out.println(valid);
                    }*/

                    u.addTokens(newtokens.keySet());

                    System.out.println("enter QR code:");
                    String QRcode = sc.nextLine();

                    QROutput q = new QROutput(QRcode);

                    u.addQROutput(q);

                    byte[] token = u.getToken();
                    while (token == null) {
                        newtokens  = registrarImpl.generateTokens(u.getPhoneNr());
                        u.addTokens(newtokens.keySet());
                        token = u.getToken();
                    }

                    Signature signatureEngine = Signature.getInstance("SHA1withRSA");
                    signatureEngine.initSign(u.getPrivk());
                    signatureEngine.update(token);


                    //TODO ADD CURRENT DAY (2min period)
                    byte[] signature = signatureEngine.sign();


                    Map<byte[], byte[]> signedHash = mixingServerImpl.addCapsule(LocalDateTime.now().toString(), u.getToken(), signature, q.getHash());
                    //System.out.println(signedHash.toString());



                } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        req.start();
    }   catch (Exception e){
        e.printStackTrace();
    }


    }

}
