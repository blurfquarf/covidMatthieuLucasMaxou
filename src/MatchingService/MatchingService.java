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

    //key = hash van CF + Time | value = ArrayList van usertokens
    HashMap<ByteArrayHolder, ArrayList<byte[]>> allEntries = new HashMap<>();
    ArrayList<ByteArrayHolder> criticalEntries = new ArrayList<>();


    JButton check = new JButton("Warn users");
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
        MatchingServiceImpl matchingServerImpl = new MatchingServiceImpl();
        registry.rebind("MatchingService", matchingServerImpl);

        MatchingServiceInterface mSI = matchingServerImpl;

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

            }
        });

        showUninformed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<byte[]> list = showTokens(uninformedTokens);
                contentScrollPane.setViewportView(list);
            }
        });
        showInformed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<byte[]> list = showTokens(informedTokens);
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




        mixingServerCapsuleList = mSI.getMixingServerCapsuleList();
        doctorCapsuleList = mSI.getDoctorCapsuleList();


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
            ByteArrayHolder temp = new ByteArrayHolder(c.getTime(), c.getHash());
            if(allEntries.get(temp) == null) {
                allEntries.put(temp,new ArrayList<>());
                allEntries.get(temp).add(c.getToken());
            }
            else {
                allEntries.get(temp).add(c.getToken());
            }
            if(!uninformedTokens.contains(c.getToken())) {
                uninformedTokens.add(c.getToken());
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
                pseudonymsForDayC.remove(0);
            }
            if(Arrays.equals(c.getHash(), temp)) {
                markEntries(c);
            }
        }
    }

    public void markEntries(Capsule c) {
        ByteArrayHolder temp2 = new ByteArrayHolder(c.getTime(), c.getHash());
        if(!criticalEntries.contains(temp2))criticalEntries.add(new ByteArrayHolder(c.getTime(), c.getHash()));

        //if(allEntries.get(temp2)!=null) uninformedTokens.addAll(allEntries.get(temp2));
        //alle keys overlopen
        //checken of key (QR-code) gescand in in een periode van 5 sec nadat besmette client de QRcode scande en checken of beide entries dezelfde CF bezochtten
        //indien beide clients dezelfde CF bezochten in een tijdspanne van 5s => de (nog) niet besmette client(s) toevoegen aan de uninformed list
        for (ByteArrayHolder b : allEntries.keySet()) {
            if(b.getTime().isBefore(temp2.getTime().plusSeconds(5)) && b.getTime().isAfter(temp2.getTime()) && Arrays.equals(b.getByteArray(), temp2.getByteArray())) {
                uninformedTokens.addAll(allEntries.get(b));
            }
        }

        if(!informedTokens.contains(c.getToken())){
            informedTokens.add(c.getToken());
        }
        uninformedTokens.remove(c.getToken());
    }

    public JList<byte[]> showTokens(ArrayList<byte[]> tokens) {
        JList tokenList = new JList<byte[]>();
        DefaultListModel<byte[]> tokenModel = new DefaultListModel<>();
        for (int i = 0; i < tokens.size(); i++) {
            tokenModel.addElement(tokens.get(i));
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
        DefaultListModel<String>  entryModel = new DefaultListModel<>();
        for (int i = 0; i < allEntries.size(); i++) {

        }

        return entryList;
    }




}
