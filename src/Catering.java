import Registrar.RegistrarInterface;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;

public class Catering {

    private ArrayList<SecretKey> secretkeys;

    public static String generateQRCode(Business b, byte[] pseudoniem) throws NoSuchAlgorithmException{
        int randomNumber=(int) (1000*Math.random());
        int CF= b.getBtw();
        String pseudoniemstring= new String(pseudoniem, StandardCharsets.UTF_8);

        //hash maken van random number en pseudoniem
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String s = randomNumber + pseudoniemstring;
        byte[] QRHash = digest.digest(s.getBytes(StandardCharsets.UTF_8));


        //geen probleem van string parseable te maken
        String finalQRCode=  randomNumber + CF + new String(QRHash, StandardCharsets.UTF_8);
        System.out.println(finalQRCode);
        return finalQRCode;
    }

    public static void main(String[] args) {

        Business testBusiness = new Business("bert", 123321, "Gent");
        int newKeyInterval = 7;

        String name = "YAKINOODDDLLL";
        int btw = 69;
        String adress = "kakastraat";


        try{
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");

/*
            Thread mk = new Thread() {
                public void run(){
                    try {
                        registrarImpl.makeMasterKey(testBusiness);
                    } catch (RemoteException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            mk.start();*/


            ArrayList<String> QRCodes = new ArrayList<>();


            Thread req = new Thread(() -> {
                try {
                    //registrarImpl.makeMasterKey(testBusiness);
                    ArrayList<byte[]> derivedKeys = new ArrayList<>();
                    ArrayList<byte[]> oldKeys = new ArrayList<>();

                    derivedKeys = registrarImpl.makeInitialSecretsForCF(name, btw, adress);

                    for (byte[] b: derivedKeys) {
                        System.out.println(Arrays.toString(b));
                    }*/








                    //should be 840000ms
                    Thread.sleep(840000);

                    while(true) {
                        //System.out.println(testBusiness.getBtw());

                        //System.out.println(testBusiness);
                        derivedKeys = registrarImpl.makeSecretsForCF(name, btw, adress);

                        for (byte[] b: derivedKeys) {
                            System.out.println(Arrays.toString(b));
                        }

                        //if request is sent to early, an empty list is returned
                        //check if te returned list is bigger than the current list
                        //if bigger add the keys to the derivedkey list
                        //if smaller we assume that not all the previous keys have been used
                        // and that there are still keys in the list available
                        if (derivedKeys.size() > oldKeys.size()) {
                            oldKeys = new ArrayList<>(derivedKeys);
                        }
                        else derivedKeys = new ArrayList<>(oldKeys);

                        for (byte[] s : derivedKeys) {
                            //for every key in the list we use 1 every minute (read day)
                            //when a key is used we remove it from the list, so it cannot be reused
                            byte[] pseudonym = registrarImpl.generateCFPseudonym(name, s, adress, day);

                            System.out.println(pseudonym);

                            QRCodes.add(generateQRCode(testBusiness, pseudonym));
                            oldKeys.remove(s);
                            Thread.sleep(10000);
                        }
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
