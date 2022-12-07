package Doctor;

import MatchingService.MatchingServiceInterface;
import MixingServer.Capsule;
import Registrar.RegistrarInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class DoctorClient {

    private static PrivateKey privk;
    private static PublicKey pubk;

    ArrayList<Capsule> userLogCapsules = new ArrayList<>();

    JLabel readField = new JLabel("");

    JButton read = new JButton("Read");

    JTextField sendField = new JTextField("");

    JButton send = new JButton("Send");

    String name = "Docter Bert";



    public static void main(String[] args) throws InvalidAlgorithmParameterException, NotBoundException, SignatureException, RemoteException, InvalidKeyException {
        DoctorClient d = new DoctorClient();
        d.run();
    }

    public void run() throws RemoteException, NotBoundException, InvalidAlgorithmParameterException, SignatureException, InvalidKeyException {
        try{
            Registry matchingRegistry = LocateRegistry.getRegistry("localhost", 1100);
            MatchingServiceInterface matchingServiceImpl = (MatchingServiceInterface) matchingRegistry.lookup("MatchingService");

            Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);
            RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
            KeyPair pair = kpg.generateKeyPair();

            privk = pair.getPrivate();
            pubk = pair.getPublic();

            //REGISTRAR WILL BE MIDDLE MAN FOR PUBLIC KEY, DOCTOR SHOULDNT SEND PUBLIC KEY HIMSELF (UNSAFE)!
            registrarImpl.setPKForDoctor(name, pubk);


            /////////GUI/////////
            JFrame frame = new JFrame();


            JPanel userPanel = new JPanel(new GridLayout(10, 1, 100, 5));
            userPanel.setPreferredSize(new Dimension(100,100));
            userPanel.setBackground(Color.lightGray);


            frame.setTitle("Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.setResizable(true);
            frame.setSize(1280,500);
            frame.setLocationRelativeTo(null);

            userPanel.add(readField);
            userPanel.add(read);
            userPanel.add(sendField);
            userPanel.add(send);

            send.setEnabled(false);
            read.setEnabled(true);

            frame.add(userPanel,BorderLayout.CENTER);

            frame.setVisible(true);

            read.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    send.setEnabled(true);
                    try {
                        readUserLogs(sendField.getText());
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    read.setEnabled(false);
                }
            });


            send.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    read.setEnabled(true);
                    for (Capsule userLogCapsule : userLogCapsules) {
                        LocalDateTime time = userLogCapsule.getTime();
                        byte[] hash = userLogCapsule.getHash();
                        byte[] token = userLogCapsule.getToken();
                        byte[] signature = userLogCapsule.getSignature();
                        int random = userLogCapsule.getRandom();

                        byte[] completePacket = concatenate(time.toString().getBytes(), hash, token, signature, String.valueOf(random).getBytes());

                        //signing with the private doctorKey
                        Signature signatureEngine = null;
                        try {
                            signatureEngine = Signature.getInstance("SHA1withRSA");
                            signatureEngine.initSign(privk);
                            signatureEngine.update(completePacket);
                        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException ex) {
                            throw new RuntimeException(ex);
                        }

                        byte[] completePacketSignature = new byte[0];
                        try {
                            completePacketSignature = signatureEngine.sign();
                        } catch (SignatureException ex) {
                            throw new RuntimeException(ex);
                        }

                        try {
                            matchingServiceImpl.sendFromDoctor(time, hash, token, signature, random, completePacket, completePacketSignature, name);
                            System.out.println("Sent hash: " + Arrays.toString(hash));


                        } catch (RemoteException | NotBoundException | NoSuchAlgorithmException | SignatureException |
                                 InvalidKeyException ex){
                            throw new RuntimeException(ex);
                        }
                        send.setEnabled(false);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /////////GUI/////////

    public void readUserLogs(String path) throws FileNotFoundException {
        System.out.println(path);
        File text = new File(path);
        Scanner sc = new Scanner(text);
        //time,token,signature,hash,random
        String[] line = sc.nextLine().split(",");
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime time = LocalDateTime.parse(line[0].substring(0, line[0].length()-7), f);
        byte[] token = new BigInteger(line[1], 2).toByteArray();
        byte[] signature = new BigInteger(line[2], 2).toByteArray();
        byte[] hash = new BigInteger(line[3], 2).toByteArray();
        int random = Integer.parseInt(line[4]);

        userLogCapsules.add(new Capsule(token, signature, hash, random, time));
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
}
