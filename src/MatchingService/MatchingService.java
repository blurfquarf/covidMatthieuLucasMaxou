package MatchingService;
import MixingServer.MixingServer;
import MixingServer.MixingServerImpl;
import Registrar.ByteArrayHolder;
import Registrar.RegistrarInterface;
import MixingServer.Capsule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;

public class MatchingService {
    private ArrayList<Capsule> mixingServerCapsuleList;
    //all infected
    private ArrayList<Capsule> doctorCapsuleList;

    private ArrayList<byte[]>  uninformedTokens = new ArrayList<>();
    private ArrayList<byte[]> informedTokens = new ArrayList<>();

    //lijst van users die op eenzelfde moment eenzelfde zaak hebben bezocht
    //key = hash van CF + Time | value = ArrayList van usertokens
    HashMap<ByteArrayHolder, ArrayList<byte[]>> allEntries = new HashMap<>();
    ArrayList<ByteArrayHolder> criticalEntries = new ArrayList<>();

    JButton check = new JButton("Check infected users");
    JButton showContents = new JButton("Show content of matching service");
    JButton showUninformed = new JButton("Show uninformed tokens");
    JButton showInformed = new JButton("Show informed tokens");
    JButton showMixingCapsules = new JButton("Show mixing capsules");
    JButton showDoctorCapsules = new JButton("Show doctor capsules");
    JButton showAllEntries = new JButton("Show all entries");
    JButton showCriticalEntries = new JButton("Show critical entries");
    JButton close = new JButton("Close");

    private void run() { try {

        Registry registry = LocateRegistry.createRegistry(1100);
        registry.rebind("MatchingService", new MatchingServiceImpl());

        System.out.println("Matching Service is ready");


        Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");

        JFrame frame = new JFrame();
        JFrame contentFrame = new JFrame();
        JPanel contentPanel = new JPanel(new GridLayout(10, 1, 100, 5));
        JScrollPane contentScrollPane = new JScrollPane();


        JPanel userPanel = new JPanel(new GridLayout(10, 1, 100, 5));
        userPanel.setPreferredSize(new Dimension(100,100));
        userPanel.setBackground(Color.lightGray);

        frame.setTitle("Matching Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);
        frame.setSize(1280,500);
        frame.setLocationRelativeTo(null);

        userPanel.add(check);
        check.setEnabled(true);

        userPanel.add(showContents);
        showContents.setEnabled(true);
        frame.add(userPanel,BorderLayout.CENTER);
        frame.setVisible(true);


        showContents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                contentPanel.setPreferredSize(new Dimension(100,100));
                contentPanel.setBackground(Color.lightGray);
                contentFrame.setTitle("Content of matching service");
                contentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                contentFrame.setLayout(new BorderLayout());
                contentFrame.setResizable(true);
                contentFrame.setSize(1280,500);
                contentFrame.setLocationRelativeTo(null);
                contentFrame.revalidate();
                contentPanel.add(showUninformed);
                contentPanel.add(showInformed);
                contentPanel.add(showMixingCapsules);
                contentPanel.add(showDoctorCapsules);
                contentPanel.add(showAllEntries);
                contentPanel.add(showCriticalEntries);
                contentPanel.add(close);
                contentPanel.add(contentScrollPane);
                contentFrame.add(contentPanel);
                contentFrame.setVisible(true);
            }
        });

        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentFrame.setVisible(false);
            }
        });
        check.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    checkHash();
                } catch (RemoteException | NotBoundException | NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        showUninformed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<String> list = showTokens(uninformedTokens);
                contentScrollPane.setViewportView(list);
            }
        });
        showInformed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<String> list = showTokens(informedTokens);
                contentScrollPane.setViewportView(list);
            }
        });
        showDoctorCapsules.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<String> list = showCapsules(doctorCapsuleList);
                contentScrollPane.setViewportView(list);
            }
        });
        showMixingCapsules.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<String> list = showCapsules(mixingServerCapsuleList);
                contentScrollPane.setViewportView(list);
            }
        });
        showAllEntries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<String> list = showEntries();
                contentScrollPane.setViewportView(list);
            }
        });
        showCriticalEntries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<String> list = showCriticalEntries();
                contentScrollPane.setViewportView(list);
            }
        });






    }catch (Exception e){
        e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MatchingService main =  new MatchingService();
        main.run();
    }


    public void checkHash() throws RemoteException, NoSuchAlgorithmException, NotBoundException {
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");
        //for all capsules sent from the mixingserver
        //check if there is already an entry for that time at that CF (hash)
        // if no entry yet => create a new one + add the token of that user
        // if there is already an entrye for that time & place => add token of user to that entry
        for(Capsule c : mixingServerCapsuleList) {

            System.out.println(mixingServerCapsuleList.size());
            //System.out.println("time: "+c.getTime()+", hash: "+ Arrays.toString(c.getHash()) +", token: "+ Arrays.toString(c.getToken()) +", random:"+c.getRandom());
            ByteArrayHolder temp = new ByteArrayHolder(c.getTime(), c.getHash());

            //tijd en zaak er nog niet in => nieuwe entry
            if(allEntries.get(temp) == null) {
                allEntries.put(temp, new ArrayList<>());
                allEntries.get(temp).add(c.getToken());
            }
            else {
                allEntries.get(temp).add(c.getToken());
            }
        }

        //has user been there
        for(Capsule c : doctorCapsuleList) {
            ArrayList<byte[]> pseudonymsForDayC = registrarImpl.getPseudonymsPerDay(c.getTime());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] temp = new byte[0];
            while (!Arrays.equals(c.getHash(), temp) || pseudonymsForDayC.size()!=0){
                String pseudoniemstring= new String(pseudonymsForDayC.get(0), StandardCharsets.UTF_8);
                int random = c.getRandom();
                String s = String.valueOf(random) + pseudoniemstring;
                temp = digest.digest(s.getBytes(StandardCharsets.UTF_8));

                if (temp.length == c.getHash().length-1) {
                    byte[] b = new byte[c.getHash().length];
                    b[0] = 0;
                    for (int i = 1; i < c.getHash().length; i++) {
                        b[i] = c.getHash()[i];
                    }
                    temp = b;
                }
                pseudonymsForDayC.remove(0);
            }
            if(Arrays.equals(c.getHash(), temp)) {
                System.out.println("hash of infected user is legit");
                //doktercapsules markeren
                markEntries(c);
            }
        }
    }

    public void markEntries(Capsule c) {
        ByteArrayHolder temp2 = new ByteArrayHolder(c.getTime(), c.getHash());
        if(!criticalEntries.contains(temp2)) criticalEntries.add(new ByteArrayHolder(c.getTime(), c.getHash()));
        //alle keys overlopen
        //checken of key (QR-code) gescand in in een periode van 5 sec nadat besmette client de QRcode scande en checken of beide entries dezelfde CF bezochtten
        //indien beide clients dezelfde CF bezochten in een tijdspanne van 5s => de (nog) niet besmette client(s) toevoegen aan de uninformed list
        for (ByteArrayHolder b : allEntries.keySet()) {
            //tijd van mixing entry (allEntries) is binnen interval van tijd van doktercapsule, tokens bij entry uninformed zetten (marked!)
            if((b.getTime().isEqual(temp2.getTime())||(b.getTime().isBefore(temp2.getTime().plusSeconds(5)) && b.getTime().isAfter(temp2.getTime())) || (b.getTime().isAfter(temp2.getTime().minusSeconds(5)) && b.getTime().isBefore(temp2.getTime()))) && Arrays.equals(b.getByteArray(), temp2.getByteArray())) {
                for (byte[] a : allEntries.get(b)) {
                    if(!uninformedTokens.contains(a) && !Arrays.equals(a, c.getToken())) {
                        uninformedTokens.add(a);
                    }
                }
            }
        }

        if(!informedTokens.contains(c.getToken())){
            informedTokens.add(c.getToken());
        }
        uninformedTokens.remove(c.getToken());
    }

    public JList<String> showTokens(ArrayList<byte[]> tokens) {
        JList tokenList = new JList<String>();
        DefaultListModel<String> tokenModel = new DefaultListModel<>();
        for (int i = 0; i < tokens.size(); i++) {
            tokenModel.addElement(Arrays.toString(tokens.get(i)));
        }
        tokenList.setModel(tokenModel);
        return tokenList;
    }

    public JList<String> showCapsules(ArrayList<Capsule> capsules){
        JList capsuleList = new JList<String>();
        DefaultListModel<String> capsuleModel = new DefaultListModel<>();
        for (int i = 0; i < capsules.size(); i++) {
            capsuleModel.addElement(capsules.get(i).toString());
        }
        capsuleList.setModel(capsuleModel);
        return capsuleList;
    }

    public JList<String> showEntries(){
        JList entryList = new JList<String>();
        DefaultListModel<String> entryModel = new DefaultListModel<>();
        for(Map.Entry<ByteArrayHolder, ArrayList<byte[]>> entry : allEntries.entrySet()){
            StringBuilder sb = new StringBuilder();
            ByteArrayHolder holder = entry.getKey();
            ArrayList<byte[]> array = entry.getValue();
            sb.append(holder.toString());
            for (int j = 0; j < array.size(); j++) {
                sb.append(Arrays.toString(array.get(j)));
            }
            entryModel.addElement(sb.toString());
        }
        entryList.setModel(entryModel);
        return entryList;
    }

    public JList<String> showCriticalEntries(){
        JList entryList = new JList<String>();
        DefaultListModel<String>  entryModel = new DefaultListModel<>();
        for (int i = 0; i < criticalEntries.size(); i++) {
            entryModel.addElement(criticalEntries.get(i).toString());
        }
        entryList.setModel(entryModel);
        return entryList;
    }
}
