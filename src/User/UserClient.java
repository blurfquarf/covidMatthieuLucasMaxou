package User;

import MatchingService.MatchingServiceInterface;
import MixingServer.Capsule;
import MixingServer.MixingServerInterface;
import Registrar.ByteArrayHolder;
import Registrar.RegistrarInterface;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.html.HTMLDocument;

public class UserClient implements ActionListener {

    private static String phoneNr;


    private static PrivateKey privk;
    private static PublicKey pubk;
    private static HashMap<String, QROutput> visitEntries = new HashMap<>();


    //timestamp and corresponding capsules
    private static HashMap<LocalDateTime, Capsule> validUserCapsules = new HashMap<>();

    private static ArrayList<ByteArrayHolder> criticalTuples = new ArrayList<>();

    //hoe lang elke capsule bewaard moet worden
    private int duration = 7;

    private static HashMap<byte[], byte[]> tokens = new HashMap<>();
    private static Map<byte[], byte[]> newtokens;
    private static AtomicBoolean scanned = new AtomicBoolean(false);
    private static AtomicBoolean numberGiven = new AtomicBoolean(false);


    private static ArrayList<byte[]> hashes;

    private static ArrayList<LocalDateTime> times;

    private static String time;


    private static QROutput q;
    private static String qr = "";
    private static String number;
    private static LocalDateTime lastCapsuleSent;

    private static LocalDateTime lastTokenTime;

    static JLabel NewTokensLabel = new JLabel("");

    static JButton visit = new JButton("Visit");

    static JButton writeLogs = new JButton("Write out logs for doctor!");

    static JTextArea writeLogsLabel = new JTextArea("");

    static JLabel icon = new JLabel("");

    static JButton fetchCriticalTuples = new JButton("Fetch critical tuples");

    static JLabel warningField = new JLabel("");


    public static void main(String[] args) throws InvalidAlgorithmParameterException, NotBoundException, SignatureException, RemoteException, InvalidKeyException {
        UserClient u = new UserClient();
        u.run();
    }

    public void run() throws RemoteException, NotBoundException, InvalidAlgorithmParameterException, SignatureException, InvalidKeyException {
        try {
            Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);
            Registry mixingRegistry = LocateRegistry.getRegistry("localhost", 1101);



            RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");
            MixingServerInterface mixingServerImpl = (MixingServerInterface) mixingRegistry.lookup("MixingService");

            JFrame frame = new JFrame();
            JButton enroll = new JButton("Enroll");

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
            scan.setEnabled(false);
            send.setEnabled(true);
            getTokens.setEnabled(true);

            fetchCriticalTuples.setEnabled(true);

            writeLogsLabel.setLineWrap(true);
            icon.setOpaque(true);

            JPanel userPanel = new JPanel(new GridLayout(10, 1, 100, 5));
            userPanel.setPreferredSize(new Dimension(100,100));
            userPanel.setBackground(Color.lightGray);

            frame.setTitle("User client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.setResizable(true);
            frame.setSize(1280,700);
            frame.setLocationRelativeTo(null);

            frame.add(userPanel,BorderLayout.CENTER);



            /*1*/userPanel.add(numberField);
            /*2*/userPanel.add(enterQR);
            /*4*/userPanel.add(numberTextField);
            /*3*/userPanel.add(qrField);
            /*5*/userPanel.add(send);
            /*6*/userPanel.add(scan);
            /*8*/userPanel.add(enroll);
            /*7*/userPanel.add(visit);
            /*9*/userPanel.add(writeLogs);
            /*10*/userPanel.add(getTokens);
            /*11*/userPanel.add(NewTokensLabel);
            /*12*/userPanel.add(writeLogsLabel);
            userPanel.add(icon);
            userPanel.add(fetchCriticalTuples);
            userPanel.add(warningField);


            frame.setVisible(true);


            fetchCriticalTuples.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        fetchTuples();

                        //compare valid user capsules and critical tuples
                        ArrayList<Capsule> userTokens = compareTuples();
                        if (!userTokens.isEmpty()){
                            warningField.setText("You might be infected, get inside and stay clear of others!");
                            for (int i = 0; i < userTokens.size(); i++) {
                                mixingServerImpl.sendHashesTokensTimes(userTokens.get(i).getHash(), userTokens.get(i).getToken(), userTokens.get(i).getTime());
                            }
                        }else {
                            warningField.setText("You are safe, no need to worry!");
                            warningField.setBackground(Color.green);
                        }

                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    } catch (NotBoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });



            writeLogs.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        writeLogs();
                        writeLogsLabel.setText("Logs written to file for doctor! Get inside and stay clear from others! File for doctor: " + System.getProperty("user.dir") + "/" +  "UserLogFile_" + phoneNr + time);
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
                        NewTokensLabel.setText("New tokens have been added!");
                    } else {
                        NewTokensLabel.setText("24 hours haven't passed yet!");
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
                        enterQR.setText("Enter QR-code below:");
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
                    try {
                        if(scanned.get()){
                            String qrCode = new String(qr);
                            visit(registrarImpl, mixingServerImpl, qr);
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

    public static void fetchTuples() throws RemoteException, NotBoundException {
        Registry matchingRegistry = LocateRegistry.getRegistry("localhost", 1100);
        MatchingServiceInterface matchingServiceImpl = (MatchingServiceInterface) matchingRegistry.lookup("MatchingService");

        hashes = new ArrayList<>(matchingServiceImpl.getHashes());
        times = new ArrayList<>(matchingServiceImpl.getTimes());

        for (int i = 0; i < hashes.size(); i++) {
            criticalTuples.add(new ByteArrayHolder(times.get(i), hashes.get(i)));
        }
    }

    public static ArrayList<Capsule> compareTuples() {
        ArrayList<Capsule> userTokens = new ArrayList<>();
        //hier
        //opgevraagd
        for (ByteArrayHolder tuple: criticalTuples) {
            for (Capsule c: validUserCapsules.values()) {
                if ((c.getTime().isEqual(tuple.getTime()) || (c.getTime().isBefore(tuple.getTime().plusSeconds(5)) && c.getTime().isAfter(tuple.getTime())) ||
                        (c.getTime().isAfter(tuple.getTime().minusSeconds(5)) && c.getTime().isBefore(tuple.getTime()))) && Arrays.equals(c.getHash(), tuple.getByteArray())) {
                    userTokens.add(new Capsule(c));
                }
            }
        }
        return userTokens;

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
                    System.out.println("used token: "+ Arrays.toString(token.getKey()));

                    byte[] signedHash = new byte[1];

                    //new byte[1] is default value returned when invalid!
                    while (Arrays.equals(signedHash, new byte[1])) {

                        //signedhash is signature reseived from mixingservice
                        LocalDateTime now = LocalDateTime.now();

                        //gaat maar door als token valid is
                        //send capsules to mixing server          //when sent       //token         //signature   //hash of QR code
                        signedHash = mixingServerImpl.addCapsule(now.toString(), token.getKey(), token.getValue(), q.getHash());
                        //capsules this user has sent to the mixing server (for tracing later)

                        byte[] hash = new BigInteger(q.getHash(), 2).toByteArray();


                        //used for comparison later on!
                        addValidUserCapsules(new Capsule(token.getKey(), token.getValue(), hash, q.getRandom(), now));

                        //get token verwijdert telkens opgevraagde token
                        if(Arrays.equals(signedHash, new byte[1])) token = getToken();
                    }
                    System.out.println("signature of hash: "+ Arrays.toString(signedHash));
                    lastCapsuleSent = LocalDateTime.now();

                    //visuele representatie maken
                    makeVisualisation(signedHash);

                    //probleem wanneer je nog geen tokens hebt opgevraagd voor nieuwe dag
                }catch (Exception e) {
                    visit.setEnabled(false);
                    NewTokensLabel.setText("Tokens were not valid! Ask new tokens and/or press visit!");
                    System.out.println("Tokens cleared, ready for revisit!");
                }
            });
            visitThread.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void writeLogs() throws IOException {
        time = LocalDateTime.now().toString();
        BufferedWriter w = new BufferedWriter(new FileWriter("UserLogFile_"+ phoneNr + time));
        for (Map.Entry<LocalDateTime,Capsule> e : validUserCapsules.entrySet()) {
            //enkel de logs van de laatste 5 dagen doorsturen naar de dokter
            if(e.getKey().isAfter(LocalDateTime.now().minusMinutes(10)) && e.getKey().isBefore(LocalDateTime.now()) ) {
                //time,token,signature,hash,random
                w.write(e.getKey().toString()+","+ toBinary(e.getValue().getToken()) +","+toBinary(e.getValue().getSignature())+","+toBinary(e.getValue().getHash())+","+e.getValue().getRandom());
                w.newLine();
            }
        }
        w.close();
    }

    public static void addValidUserCapsules(Capsule capsule){
        //handig voor latere checks
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

    static String toBinary(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
    }


    static void makeVisualisation(byte[] signedHash) throws IOException{
        byte[] values = new byte[] {signedHash[5], signedHash[10], signedHash[15]};
        Color color = new Color(values[0] & 0xFF, values[1] & 0xFF, values[2] & 0xFF);
        icon.setBackground(color);
    }
}
