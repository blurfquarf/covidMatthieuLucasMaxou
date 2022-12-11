package MatchingService;
import Registrar.ByteArrayHolder;
import Registrar.RegistrarInterface;
import MixingServer.Capsule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MatchingService {
    private ArrayList<Capsule> mixingServerCapsuleList = new ArrayList<>();
    //all infected
    private ArrayList<Capsule> doctorCapsuleList = new ArrayList<>();


    private ArrayList<Capsule>  uninformedTokens = new ArrayList<>();
    private ArrayList<Capsule> informedTokens = new ArrayList<>();

    //lijst van users die op eenzelfde moment eenzelfde zaak hebben bezocht
    //key = hash van CF + Time | value = ArrayList van usertokens
    HashMap<ByteArrayHolder, ArrayList<Capsule>> allEntries = new HashMap<>();
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
    JButton refreshNewcomers = new JButton("Refresh newcomers");

    JButton updateService = new JButton("Update critical tuples! (make available to users)");

    JButton forwardToRegistrar = new JButton("Forward remaining uninformed tuples to registrar! These people haven't responded within 2 days of being critical!");

    JButton clearDB = new JButton("Remove expired entries");

    JLabel infoLabel = new JLabel("");


    private void run() { try {

        Registry registry = LocateRegistry.createRegistry(1100);
        registry.rebind("MatchingService", new MatchingServiceImpl());
        System.out.println("Matching Service is ready");

        Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");

        Registry matchingServerRegistry = LocateRegistry.getRegistry("localhost", 1100);
        MatchingServiceInterface matchingServerImpl = (MatchingServiceInterface) matchingServerRegistry.lookup("MatchingService");

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

        userPanel.add(updateService);
        updateService.setEnabled(true);

        userPanel.add(refreshNewcomers);
        refreshNewcomers.setEnabled(true);

        userPanel.add(forwardToRegistrar);
        forwardToRegistrar.setEnabled(true);

        userPanel.add(clearDB);
        clearDB.setEnabled(true);

        userPanel.add(infoLabel);

        frame.add(userPanel,BorderLayout.CENTER);
        frame.setVisible(true);

        clearDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infoLabel.setText("Expired entries have been removed from the database");
                removeExpiredEntries();
            }
        });

        forwardToRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    infoLabel.setText("2 day old uninformed users available for registrar to contact! (see registrar terminal)");
                    sendRemainingUninformedTokensTimes();
                } catch (RemoteException | NotBoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        updateService.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ByteArrayHolder b: criticalEntries) {
                    try {
                        infoLabel.setText("Info pushed to users!");
                        matchingServerImpl.sendCriticalTuples(b.getByteArray(), b.getTime());
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

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
                    infoLabel.setText("Infections checked!");
                    checkHash();
                } catch (RemoteException | NotBoundException | NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        showUninformed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<String> list = showCapsules(uninformedTokens);
                contentScrollPane.setViewportView(list);
            }
        });
        showInformed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<String> list = showCapsules(informedTokens);
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


        //enkel plaats en tijd (hash CF + time)
        showCriticalEntries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JList<String> list = showCriticalEntries();
                contentScrollPane.setViewportView(list);
            }
        });

        refreshNewcomers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    infoLabel.setText("User awareness check performed!");
                    addNewComersToInformed();
                } catch (RemoteException ex) {
                    System.out.println("Something went wrong with the refreshing of the uninformed tokens!");
                } catch (NotBoundException ex) {
                    throw new RuntimeException(ex);
                }
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

    public void removeExpiredEntries() {
        ArrayList<ByteArrayHolder> toRemove = new ArrayList<>();
        ArrayList<ByteArrayHolder> toRemoveCE = new ArrayList<>();
        ArrayList<Capsule> toRemoveMSC = new ArrayList<>();
        ArrayList<Capsule> toRemoveDC = new ArrayList<>();
        LocalDateTime treshold  = LocalDateTime.now().minusMinutes(10);
        for (ByteArrayHolder b : allEntries.keySet()) {
            if(b.getTime().isBefore(treshold)){
                toRemove.add(b);
            }
        }
        for (ByteArrayHolder ce: criticalEntries) {
            if (ce.getTime().isBefore(treshold)) {
                toRemoveCE.add(ce);
            }
        }
        for (Capsule mc:mixingServerCapsuleList) {
            if (mc.getTime().isBefore(treshold)) {
                toRemoveMSC.add(mc);
            }
        }
        for (Capsule dc: doctorCapsuleList) {
            if (dc.getTime().isBefore(treshold)) {
                toRemoveDC.add(dc);
            }
        }
        for (ByteArrayHolder b: toRemove) {
            for (Capsule c: allEntries.get(b)) {
                uninformedTokens.remove(c);
                informedTokens.remove(c);
            }
            allEntries.remove(b);
        }
        for (Capsule c:toRemoveDC) {
            doctorCapsuleList.remove(c);
        }
        for(Capsule c: toRemoveMSC) {
            mixingServerCapsuleList.remove(c);
        }
        for (ByteArrayHolder b: toRemoveCE) {
            criticalEntries.remove(b);
        }
    }

    public void sendRemainingUninformedTokensTimes() throws RemoteException, NotBoundException {
        Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");
        ArrayList<Capsule> toRemove = new ArrayList<>();

        for (Capsule c: uninformedTokens) {
            //if uninformed is >= 2 days
            if(c.getUninformedTime().isBefore(LocalDateTime.now().minusMinutes(2))) {
                registrarImpl.sendRemainingUninformedTokens(c.getToken());
                toRemove.add(c);
                informedTokens.add(new Capsule(c));
            }
        }

        for (Capsule c: toRemove) {
            uninformedTokens.remove(c);
        }
    }


    public void checkHash() throws RemoteException, NoSuchAlgorithmException, NotBoundException {
        Registry matchingServerRegistry = LocateRegistry.getRegistry("localhost", 1100);
        MatchingServiceInterface matchingServerImpl = (MatchingServiceInterface) matchingServerRegistry.lookup("MatchingService");

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //inlezen van arraylist met capsules ingezonden door Mixingserver vanuit de MatchingServerImpl klasse
        ArrayList<byte[]> capsuleTokensMIX = matchingServerImpl.getMixingServerCapsuleListToken();

        ArrayList<byte[]> capsuleSignaturesMIX = matchingServerImpl.getMixingServerCapsuleListSignature();

        ArrayList<byte[]> capsulesHashesMIX = matchingServerImpl.getMixingServerCapsuleListHash();

        ArrayList<LocalDateTime> capsuleTimesMIX = matchingServerImpl.getMixingServerCapsuleListTime();

        for (int i = 0; i < capsuleTokensMIX.size(); i++) {
            if (!mixingServerCapsuleList.contains(new Capsule(capsuleTokensMIX.get(i), capsuleSignaturesMIX.get(i), capsulesHashesMIX.get(i), capsuleTimesMIX.get(i)))) {
                mixingServerCapsuleList.add(new Capsule(capsuleTokensMIX.get(i), capsuleSignaturesMIX.get(i), capsulesHashesMIX.get(i), capsuleTimesMIX.get(i)));
            }
        }


        //inlezen van arraylist met capsules ingezonden door Dokter vanuit de MatchingServerImpl klasse
        ArrayList<byte[]> capsuleTokensDOC = matchingServerImpl.getDoctorCapsuleListToken();

        ArrayList<byte[]> capsuleSignaturesDOC = matchingServerImpl.getDoctorCapsuleListSignature();

        ArrayList<byte[]> capsulesHashesDOC = matchingServerImpl.getDoctorCapsuleListHash();

        ArrayList<LocalDateTime> capsuleTimesDOC = matchingServerImpl.getDoctorCapsuleListTime();

        ArrayList<Integer> capsuleRandomsDOC = matchingServerImpl.getDoctorCapsuleListRandom();

        for (int i = 0; i < capsuleTokensDOC.size(); i++) {
            if (!doctorCapsuleList.contains(new Capsule(capsuleTokensDOC.get(i), capsuleSignaturesDOC.get(i), capsulesHashesDOC.get(i), capsuleRandomsDOC.get(i), capsuleTimesDOC.get(i)))){
                doctorCapsuleList.add(new Capsule(capsuleTokensDOC.get(i), capsuleSignaturesDOC.get(i), capsulesHashesDOC.get(i), capsuleRandomsDOC.get(i), capsuleTimesDOC.get(i)));
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");
        //for all capsules sent from the mixingserver
        //check if there is already an entry for that time at that CF (hash)
        // if no entry yet => create a new one + add the token of that user
        // if there is already an entrye for that time & place => add token of user to that entry
        for(Capsule c : mixingServerCapsuleList) {
            System.out.println(mixingServerCapsuleList.size());
            ByteArrayHolder temp = new ByteArrayHolder(c.getTime(), c.getHash());

            //tijd en zaak er nog niet in => nieuwe entry
            if(allEntries.get(temp) == null) {
                allEntries.put(temp, new ArrayList<>());
                allEntries.get(temp).add(new Capsule(c));
            }
            else if(!allEntries.get(temp).contains(c)){
                allEntries.get(temp).add(new Capsule(c));
            }
        }

        //has user been there
        for(Capsule c : doctorCapsuleList) {
            ArrayList<byte[]> pseudonymsForDayC = registrarImpl.getPseudonymsPerDay(c.getTime());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] temp = new byte[0];
            while (!Arrays.equals(c.getHash(), temp) && pseudonymsForDayC.size()!=0){
                String pseudoniemstring = new String(pseudonymsForDayC.get(0), StandardCharsets.UTF_8);
                int random = c.getRandom();
                System.out.println("random: "+c.getRandom());
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
                markEntries(c);
            }
        }
        for (ByteArrayHolder b : allEntries.keySet()) {
            ArrayList<Capsule> temp = allEntries.get(b);
            for (Capsule a: temp) {
                System.out.print(a.toString());
            }
            System.out.println();
        }
    }

    public void markEntries(Capsule c) {
        ByteArrayHolder temp2 = new ByteArrayHolder(c.getTime(), c.getHash());
        if(!criticalEntries.contains(temp2)) criticalEntries.add(new ByteArrayHolder(c.getTime(), c.getHash()));
        //alle keys overlopen
        //checken of key (QR-code) gescand in in een periode van 5 sec nadat besmette client de QRcode scande en checken of beide entries dezelfde CF bezochtten
        //indien beide clients dezelfde CF bezochten in een tijdspanne van 5s => de (nog) niet besmette client(s) toevoegen aan de uninformed list
        for (ByteArrayHolder b : allEntries.keySet()) {
            //tijd van mixing capsule (allEntries) is binnen interval van tijd van doktercapsule, tokens bij entry uninformed zetten (marked!)
            if((b.getTime().isEqual(temp2.getTime())||(b.getTime().isBefore(temp2.getTime().plusSeconds(5)) && b.getTime().isAfter(temp2.getTime())) || (b.getTime().isAfter(temp2.getTime().minusSeconds(5)) && b.getTime().isBefore(temp2.getTime()))) && Arrays.equals(b.getByteArray(), temp2.getByteArray())) {
                for (Capsule a : allEntries.get(b)) {
                    if(!Arrays.equals(a.getToken(), c.getToken()) && !uninformedTokens.contains(a) && !informedTokens.contains(a)) {
                                                                                                //uninformed time
                        uninformedTokens.add(new Capsule(a.getToken(), a.getHash(), a.getTime(), LocalDateTime.now()));
                    }
                }
            }
        }
        if (!informedTokens.contains(c)) {
            informedTokens.add(new Capsule(c.getToken(), c.getHash(), c.getTime(), LocalDateTime.now()));
        }
    }
    //methodes om de inhoud van de matching service weer te geven
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
        for(Map.Entry<ByteArrayHolder, ArrayList<Capsule>> entry : allEntries.entrySet()){
            StringBuilder sb = new StringBuilder();
            ByteArrayHolder holder = entry.getKey();
            ArrayList<Capsule> array = entry.getValue();
            sb.append(holder.toString());
            for (int j = 0; j < array.size(); j++) {
                sb.append(array.get(j).toString());
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

    public void addNewComersToInformed() throws RemoteException, NotBoundException {
        Registry matchingServerRegistry = LocateRegistry.getRegistry("localhost", 1100);
        MatchingServiceInterface matchingServerImpl = (MatchingServiceInterface) matchingServerRegistry.lookup("MatchingService");

        ArrayList<byte[]> hashes = matchingServerImpl.getNewHashes();

        ArrayList<byte[]> tokens = matchingServerImpl.getNewTokens();

        ArrayList<LocalDateTime> times= matchingServerImpl.getNewTimes();

        ArrayList<Capsule> toRemove = new ArrayList<>();

        for (int i = 0; i < hashes.size(); i++) {

            for (Capsule temp : uninformedTokens) {
                System.out.println("temp: " + temp.toString());
                System.out.println("newcomer time: " + times.get(i) + ", token: " + Arrays.toString(tokens.get(i)) + ", hash: " + Arrays.toString(hashes.get(i)));

                if (temp.getTime().equals(times.get(i).truncatedTo(ChronoUnit.SECONDS)) && Arrays.equals(temp.getToken(), tokens.get(i)) && Arrays.equals(temp.getHash(), hashes.get(i))) {
                    toRemove.add(temp);
                    informedTokens.add(new Capsule(temp));
                }
            }
        }
        for (Capsule i: toRemove) {
            uninformedTokens.remove(i);
        }
    }
}
