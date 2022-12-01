import Registrar.RegistrarInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class UserClient {

    public static void main(String[] args) throws RemoteException, NotBoundException {


        Registry myRegistry = LocateRegistry.getRegistry("localhost",
                1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");

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

                    User u = new User(phoneNr);


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
