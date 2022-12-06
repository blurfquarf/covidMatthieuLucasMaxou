package User;

import MixingServer.Capsule;
import MixingServer.MixingServerInterface;
import Registrar.RegistrarInterface;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
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

    private static String phoneNr;
    //Lists with scanned QR codes, and the random number and id's of the visited catering facilties.
    //These will be used when a user is contaminated, to search in the matching service.
    private ArrayList<String> scannedQRCodes = new ArrayList<>();
    private ArrayList<Integer> randomNumbers = new ArrayList<>();
    private ArrayList<Integer> idOfCateringFacilities = new ArrayList<>();

    private static PrivateKey privk;
    private static PublicKey pubk;
    private static HashMap<String, QROutput> visitEntries = new HashMap<>();

    private static HashMap<LocalDateTime, Capsule> validUserCapsules = new HashMap<>();

    //hoe lang elke capsule bewaard moet worden
    private int duration = 7;

    private static HashMap<byte[], byte[]> tokens = new HashMap<>();
    /*********/

    //private static User u;
    private static Map<byte[], byte[]> newtokens;
    private static AtomicBoolean scanned = new AtomicBoolean(false);
    private static AtomicBoolean numberGiven = new AtomicBoolean(false);

    private static QROutput q;
    private static String qr = "";
    private static String number;
    private static LocalDateTime lastCapsuleSent;

    private static LocalDateTime lastTokenTime;

    static JLabel NewTokensLabel = new JLabel("");

    static JButton visit = new JButton("Visit");

    static JButton writeLogs = new JButton("Write out logs for doctor");

    static JLabel writeLogsLabel = new JLabel("");

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NotBoundException, SignatureException, RemoteException, InvalidKeyException {
        UserClient u = new UserClient();
        u.run();
    }

    public void run() throws RemoteException, NotBoundException, InvalidAlgorithmParameterException, SignatureException, InvalidKeyException {
        try {
            Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);

            Registry matchingRegistry = LocateRegistry.getRegistry("localhost", 1100);

            Registry mixingRegistry = LocateRegistry.getRegistry("localhost", 1101);

            RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");
            MixingServerInterface mixingServerImpl = (MixingServerInterface) mixingRegistry.lookup("MixingService");

            JFrame frame = new JFrame();
            JButton enroll = new JButton("Enroll");


            //JButton valid = new JButton("Still valid?");

            //scan QR
            JButton scan = new JButton("Scan");

            //send enroll
            JButton send = new JButton("Send");

            //ask tokens from registrar for a day
            JButton getTokens = new JButton("Get Tokens for today");

            JLabel enterQR = new JLabel("QR-code");
            JLabel numberField = new JLabel("Enter Phone number to enroll!");

            enterQR.setPreferredSize(new Dimension(90, 50));
            JTextField qrField = new JTextField();
            JTextField numberTextField = new JTextField();

            numberField.setPreferredSize(new Dimension(90,50));
            numberTextField.setPreferredSize(new Dimension(90,50));
            qrField.setPreferredSize(new Dimension(90,50));
            NewTokensLabel.setPreferredSize(new Dimension(90,50));
            NewTokensLabel.setForeground(Color.green);


            writeLogsLabel.setPreferredSize(new Dimension(90,50));


            writeLogs.setEnabled(true);
            enroll.setEnabled(false);
            visit.setEnabled(false);
            //valid.setEnabled(false);
            scan.setEnabled(false);
            send.setEnabled(true);
            getTokens.setEnabled(true);

            JPanel userPanel = new JPanel(new GridLayout(10, 1, 100, 5));
            userPanel.setPreferredSize(new Dimension(100,100));
            userPanel.setBackground(Color.red);

            frame.setTitle("Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.setResizable(true);
            frame.setSize(1280,500);
            frame.setLocationRelativeTo(null);

            frame.add(userPanel,BorderLayout.CENTER);

            userPanel.add(enroll);
            userPanel.add(visit);
            //userPanel.add(valid);
            userPanel.add(enterQR);
            userPanel.add(qrField);
            userPanel.add(scan);
            userPanel.add(numberField);
            userPanel.add(numberTextField);
            userPanel.add(send);
            userPanel.add(getTokens);
            userPanel.add(NewTokensLabel);
            userPanel.add(writeLogs);
            userPanel.add(writeLogsLabel);

            frame.setVisible(true);

            writeLogs.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        writeLogs();
                        writeLogsLabel.setText("Logs written to file for doctor! Get inside and stay clear from others!");
                        writeLogsLabel.setBackground(Color.red);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });


            getTokens.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LocalDateTime temp = LocalDateTime.now();
                    if(temp.isAfter(lastTokenTime.plusMinutes(2))){
                        try {
                            newtokens = registrarImpl.generateTokens(getPhoneNr());
                            lastTokenTime = temp;
                            addTokens(newtokens);
                            visit.setEnabled(true);
                        } catch (NoSuchAlgorithmException | RemoteException | InvalidKeyException | SignatureException ex) {
                            throw new RuntimeException(ex);
                        }
                        NewTokensLabel.setText("New tokens have been added");
                    } else {
                        NewTokensLabel.setText("24 hours haven't passed yet");
                    }
                }
            });

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
                            enrollUser(registrarImpl, number);
                            scan.setEnabled(true);
                        }
                        enterQR.setText("Enter QR-code below");
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
                        qr = qrField.getText();
                        scanned.set(true);
                        visit.setEnabled(true);
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
                    //valid.setEnabled(false);
                    try {
                        if(scanned.get()){
                            String qrCode = new String(qr);
                            //System.out.println(qrCode);
                            addToScanned(qrCode);
                            parseQRCodes();
                            visit(registrarImpl, mixingServerImpl, qr);
                            //valid.setEnabled(true);
                        }
                        scan.setEnabled(true);

                    } catch (RemoteException | InterruptedException | SignatureException | InvalidKeyException |
                             NoSuchAlgorithmException | NotBoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enrollUser(RegistrarInterface registrarImpl, String number) throws RemoteException, NotBoundException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, SignatureException, InvalidKeyException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
        KeyPair pair = kpg.generateKeyPair();

        privk = pair.getPrivate();
        pubk = pair.getPublic();
        phoneNr = number;
        lastTokenTime = LocalDateTime.now();
        newtokens = registrarImpl.generateTokens(getPhoneNr());
        addTokens(newtokens);
    }

    public static void visit(RegistrarInterface registrarImpl, MixingServerInterface mixingServerImpl, String qr)
            throws RemoteException, NotBoundException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InterruptedException{
        System.out.println(qr);
        q = new QROutput(qr);
        addQROutput(q);

        System.out.println("In visit terechtgekomen!");

        try {
            Thread visitThread = new Thread(()->{
                try{
                    System.out.println("start visit thread");

                    //links = token, rechts = signature registrar
                    Map.Entry<byte[], byte[]> token;

                    //tokens can be empty, new visit will generate new tokens
                    if (getMapSize() <= 0) {
                        newtokens = registrarImpl.generateTokens(getPhoneNr());
                        addTokens(newtokens);
                    }

                    //token is an entry here!                     //links = token, rechts = signature registrar
                    token = getToken();
                    System.out.println("token: "+ token);

                    byte[] signedHash = new byte[1];

                    //new byte[1] is default value returned when invalid!
                    while (Arrays.equals(signedHash, new byte[1])) {
                        //signedhash is signature reseived from mixingservice

                        System.out.println("token: "+ token);

                        LocalDateTime now = LocalDateTime.now();

                        //gaat maar door als token valid is
                        //send capsules to mixing server          //when sent       //token         //signature   //hash of QR code
                        signedHash = mixingServerImpl.addCapsule(now.toString(), token.getKey(), token.getValue(), q.getHash());
                        System.out.println("signed hash: "+ Arrays.toString(signedHash));

                        //capsules this user has sent to the mixing server (for tracing later)
                        addValidUserCapsules(new Capsule(token.getKey(), token.getValue(), q.getHash(), q.getRandom(), now));

                        //get token verwijdert telkens opgevraagde token
                        if(Arrays.equals(signedHash, new byte[1])) token = getToken();
                    }
                    System.out.println("signature of hash: "+ signedHash.toString());
                    lastCapsuleSent = LocalDateTime.now();


                    //probleem wanneer je nog geen tokens hebt opgevraagd voor nieuwe dag
                }catch (Exception e) {
                    visit.setEnabled(false);
                    NewTokensLabel.setText("Vraag eerst nieuwe tokens op en druk dan terug op visit!");
                    throw new RuntimeException(e);
                }
            });
            visitThread.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void writeLogs() throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter("UserLogFile"+phoneNr));
        for (Map.Entry<LocalDateTime,Capsule> e : validUserCapsules.entrySet()) {

        }

    }

    public static void addValidUserCapsules(Capsule capsule){
        validUserCapsules.put(capsule.getTime(), capsule);
    }

    public static String getPhoneNr() {
        return phoneNr;
    }

    public static void addTokens(Map<byte[], byte[]> t) {
        tokens = new HashMap<>();
        tokens.putAll(t);
    }

    public static void addQROutput(QROutput q) {
        boolean doesNotContain = true;
        for (QROutput qrcoode : visitEntries.values()) {
            if (q.getHash().equals(qrcoode.getHash()) && q.getRandom() == qrcoode.getRandom() && q.getCF() == qrcoode.getCF()) {
                doesNotContain = false;
                break;
            }
        }
        if(doesNotContain) visitEntries.put(LocalDateTime.now().toString(), new QROutput(q));
    }

    public void addToScanned(String QRCode){
        scannedQRCodes.add(QRCode);
    }

    public static Map.Entry<byte[], byte[]> getToken() {
        Iterator<Map.Entry<byte[], byte[]>> it = tokens.entrySet().iterator();
        Map.Entry<byte[], byte[]> entry = null;
        if(it.hasNext()){
            entry = tokens.entrySet().iterator().next();
            tokens.remove(entry.getKey());
        }
        return entry;
    }

    public static int getMapSize() {
        return tokens.keySet().size();
    }

    public PrivateKey getPrivk() {
        return privk;
    }

    public PublicKey getPubk() {
        return pubk;
    }

    public void parseQRCodes() {

        //te testen
        String QRCode = scannedQRCodes.get(scannedQRCodes.size()-1);
        int Ri = Integer.parseInt(QRCode.substring(0,3));
        randomNumbers.add(Ri);
        int btwNumber = Integer.parseInt(QRCode.substring(4,9));
        idOfCateringFacilities.add(btwNumber);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
