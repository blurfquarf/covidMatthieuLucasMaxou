import MixingServer.MixingServerInterface;
import Registrar.RegistrarInterface;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

public class UserClient implements ActionListener {
    //TODO: functie om naar dokter te gaan als besmette patient : scchrijf alle logs (Token, Hash(Ri, pseudonym), Ri) + tijdstippen naar een file die de dokter dan kan uitlezen
    // fase4: elke dag moet user lijst met critical tuples[hash(Ri, pseudonym), interval] opvragen
    // checken of match met lokaal opgeslagen tuples
    // if match: tokens uit die tuples naar mixing proxy sturen die ze dan doorstuurt nr de matching service die deze tokens dan als "informed" markeert

    private static User u;
    private static Map<byte[], byte[]> newtokens;
    private static AtomicBoolean scanned = new AtomicBoolean(false);
    private static AtomicBoolean numberGiven = new AtomicBoolean(false);

    private static QROutput q;
    private static String qr = "";
    private static String number;
    private static LocalDateTime start;
    private static LocalDateTime end;



    public static void main(String[] args) throws RemoteException, NotBoundException, InvalidAlgorithmParameterException, SignatureException, InvalidKeyException {
        Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);

        Registry matchingRegistry = LocateRegistry.getRegistry("localhost", 1100);

        Registry mixingRegistry = LocateRegistry.getRegistry("localhost", 1101);


        RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");
        MixingServerInterface mixingServerImpl = (MixingServerInterface) mixingRegistry.lookup("MixingService");

        JFrame frame = new JFrame();
        JButton enroll = new JButton("Enroll");
        JButton visit = new JButton("Visit");


        JButton valid = new JButton("Still valid?");

        //scan QR
        JButton scan = new JButton("Scan");

        //send enroll
        JButton send = new JButton("Send");


        JLabel enterQR = new JLabel("QR-code");
        JLabel numberField = new JLabel("Enter Phone number to enroll!");

        enterQR.setPreferredSize(new Dimension(200, 50));
        JTextField qrField = new JTextField();
        JTextField numberTextField = new JTextField();

        numberField.setPreferredSize(new Dimension(200,50));
        numberTextField.setPreferredSize(new Dimension(200,50));
        qrField.setPreferredSize(new Dimension(200,50));

        enroll.setEnabled(false);
        visit.setEnabled(false);
        valid.setEnabled(false);
        scan.setEnabled(false);
        send.setEnabled(true);

        JPanel userPanel = new JPanel(new GridLayout(10, 1, 100, 5));
        userPanel.setPreferredSize(new Dimension(100,100));
        userPanel.setBackground(Color.lightGray);

        frame.setTitle("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);
        frame.setSize(1280,500);
        frame.setLocationRelativeTo(null);

        frame.add(userPanel,BorderLayout.CENTER);

        userPanel.add(enroll);
        userPanel.add(visit);
        userPanel.add(leave);
        userPanel.add(enterQR);
        userPanel.add(qrField);
        userPanel.add(scan);
        userPanel.add(numberField);
        userPanel.add(numberTextField);
        userPanel.add(send);

        frame.setVisible(true);

        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)  {
                boolean userExists = true;
                try {
                    number = numberTextField.getText();


                    //System.out.println(number);
                    userExists = registrarImpl.getUserByPhone(number);


                    if(!userExists) {
                        numberGiven.set(true);
                        enroll.setEnabled(true);
                    }else numberField.setText("Number is already enrolled! Retry!");
                } catch (RemoteException | NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


        enroll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                send.setEnabled(false);
                enroll.setEnabled(false);
                try {
                    if(numberGiven.get()){
                        u = enrollUser(registrarImpl, number);
                        scan.setEnabled(true);
                    }

                } catch (RemoteException | NotBoundException | NoSuchAlgorithmException | SignatureException |
                         InvalidKeyException | InvalidAlgorithmParameterException ex) {
                    throw new RuntimeException(ex);
                    }
                }
        });


        scan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    start = LocalDateTime.now();
                    visit.setEnabled(true);

                    enterQR.setText("Enter QR-code below");
                    qr = qrField.getText();
                    scanned.set(true);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


        visit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                send.setEnabled(false);
                scan.setEnabled(false);
                visit.setEnabled(false);
                leave.setEnabled(false);
                try {
                    if(scanned.get()){
                        String qrCode = new String(qr);
                        //System.out.println(qrCode);
                        u.addToScanned(qrCode);


                        u.parseQRCodes();
                        leave.setEnabled(true);
                        visit(u, registrarImpl, mixingServerImpl, qr);
                    }
                    scan.setEnabled(true);

                } catch (RemoteException | InterruptedException | SignatureException | InvalidKeyException |
                         NoSuchAlgorithmException | NotBoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


        leave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                end = LocalDateTime.now();
                try {
                    leave(u, mixingServerImpl);
                } catch (NotBoundException | NoSuchAlgorithmException | SignatureException | RemoteException | InvalidKeyException ex) {
                    ex.printStackTrace();
                }
                visit.setEnabled(true);
            }
        });


/*
        Registry myRegistry = LocateRegistry.getRegistry("localhost",
                1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");*/

    }
    public static User enrollUser(RegistrarInterface registrarImpl, String number) throws RemoteException, NotBoundException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, SignatureException, InvalidKeyException {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
        KeyPair pair = kpg.generateKeyPair();

        User u = new User(number, pair.getPrivate(), pair.getPublic());
        newtokens = registrarImpl.generateTokens(u.getPhoneNr());
        u.addTokens(newtokens);
        return u;
    }

    public static void visit(User u, RegistrarInterface registrarImpl, MixingServerInterface mixingServerImpl, String qr)
            throws RemoteException, NotBoundException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InterruptedException{
        System.out.println(qr);
        QROutput q = new QROutput(qr);
        u.addQROutput(q);

        System.out.println("In visit terechtgekomen!");

        try {
            Thread visitThread = new Thread(()->{
                try{
                    System.out.println("start visit thread");

                    //links = token, rechts = signature registrar
                    Map.Entry<byte[], byte[]> token;

                    //tokens can be empty
                    if (u.getMapSize() <= 0) {
                        newtokens = registrarImpl.generateTokens(u.getPhoneNr());
                        u.addTokens(newtokens);
                    }

                    //token is an entry here!                     //links = token, rechts = signature registrar
                    token = u.getToken();
                    System.out.println("token: "+ token);

                    byte[] signedHash = new byte[1];

                    //new byte[1] is default value returned when invalid!
                    while (Arrays.equals(signedHash, new byte[1])) {
                        //signedhash is signature reseived from mixingservice

                        System.out.println("token: "+ token);

                        LocalDateTime now = LocalDateTime.now();

                        //gaat maar door als token valid is
                        //send capsules to mixing server
                        signedHash = mixingServerImpl.addCapsule(now.toString(), token.getKey(), token.getValue(), q.getHash());
                        System.out.println("signed hash: "+ Arrays.toString(signedHash));
                        //System.out.println("new signed hash: "+ signedHash[0]);

                        //get token verwijdert telkens opgevraagde token
                        if(Arrays.equals(signedHash, new byte[1])) token = u.getToken();
                    }
                    System.out.println("signature of hash: "+ signedHash.toString());
                    //token = u.getToken();

                    //method to periodically send capsules to mixing server
                    /*while(visiting.get()){
                        System.out.println("visiting");
                        //if a day = 120.000, this is once every 2 hours
                        Thread.sleep(10000);
                        String time = LocalDateTime.now().toString();
                        signedHash = mixingServerImpl.addCapsule(time, token.getKey(), token.getValue(), q.getHash());
                        if(signedHash.equals(new byte[1])){
                            System.out.println("new QR necessary, revisit with new QR-code!");
                        }
                        token = u.getToken();
                    }*/
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            visitThread.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void leave(User u, MixingServerInterface mixingServerImpl) throws NotBoundException, NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException {
        System.out.println("start: "+start.toString()+", end: "+end.toString());
        while(start.isBefore(end)){
            System.out.println("sending capsule");
            String time = start.toString();
            Map.Entry<byte[], byte[]> token = u.getToken();
            byte[] signedHash;
            signedHash = mixingServerImpl.addCapsule(time, token.getKey(), token.getValue(), q.getHash());
            if(signedHash.equals(new byte[1])){
                System.out.println("new QR necessary, revisit with new QR-code!");
                start = end.plusMinutes(2);
            }
            else {
                System.out.println("signature of hash: "+ signedHash.toString());
                start = start.plusSeconds(5);
            }
        }
        System.out.println("User left");
    }


    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
