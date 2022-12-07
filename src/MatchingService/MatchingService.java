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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MatchingService {
    private ArrayList<Capsule> mixingServerCapsuleList;

    //all infected
    private ArrayList<Capsule> doctorCapsuleList;

    private ArrayList<byte[]>  uninformedTokens = new ArrayList<>();
    private ArrayList<byte[]> informedTokens = new ArrayList<>();
    //key: string vd hash, value: object met hash & time
    ArrayList< ByteArrayHolder> allEntries = new ArrayList<>();
    ArrayList<ByteArrayHolder> criticalEntries = new ArrayList<>();


    JButton check = new JButton("Warn users");


    private void run() { try {

        Registry registry = LocateRegistry.createRegistry(1100);
        MatchingServiceImpl matchingServerImpl = new MatchingServiceImpl();
        registry.rebind("MatchingService", matchingServerImpl);

        MatchingServiceInterface mSI = matchingServerImpl;

        System.out.println("Matching Service is ready");


        Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");


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

        userPanel.add(check);
        check.setEnabled(true);
        frame.add(userPanel,BorderLayout.CENTER);
        frame.setVisible(true);


        check.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

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
        for(Capsule c : mixingServerCapsuleList) {
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
            if(Arrays.equals(c.getHash(), temp)) markEntries(c);
        }
    }

    public void markEntries(Capsule c) {

        if(!informedTokens.contains(c.getToken())){
            informedTokens.add(c.getToken());
        }
        uninformedTokens.remove(c.getToken());
    }






}
