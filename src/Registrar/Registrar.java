package Registrar;

import MixingServer.MixingServerInterface;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;

public class Registrar {


    JButton showPseudonyms = new JButton("Show pseudonyms");
    JButton showTokenMappings = new JButton("Show token mappings");
    JButton callUsers = new JButton("Call users");

    JLabel callText = new JLabel("");

    private void run() { try {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
        KeyPair pair = kpg.generateKeyPair();

        PrivateKey serverSK = pair.getPrivate();
        PublicKey serverPK = pair.getPublic();


        Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("RegistrarService", new RegistrarImpl(serverSK, serverPK));
            System.out.println("system is ready");

        Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");


        JFrame frame = new JFrame();
        JPanel userPanel = new JPanel(new GridLayout(10, 1, 100, 5));
        JScrollPane contentScrollPane = new JScrollPane();

        userPanel.setPreferredSize(new Dimension(100,100));
        userPanel.setBackground(Color.lightGray);

        frame.setTitle("Registrar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);
        frame.setSize(1280,500);
        frame.setLocationRelativeTo(null);

        userPanel.add(showPseudonyms);
        showPseudonyms.setEnabled(true);

        userPanel.add(showTokenMappings);
        showTokenMappings.setEnabled(true);

        userPanel.add(contentScrollPane);

        userPanel.add(callUsers);
        callUsers.setEnabled(true);

        userPanel.add(callText);

        frame.add(userPanel,BorderLayout.CENTER);
        frame.setVisible(true);

        showPseudonyms.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JList<String> list = registrarImpl.showPseudonyms();
                    contentScrollPane.setViewportView(list);
                } catch (RemoteException ex) {
                    System.out.println("Problem with showing pseudonyms");
                    throw new RuntimeException(ex);
                }

            }
        });

        showTokenMappings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JList<String> list = registrarImpl.showTokenMappings();
                    contentScrollPane.setViewportView(list);
                } catch (RemoteException ex) {
                    System.out.println("Problem with showing token mappings");
                    throw new RuntimeException(ex);
                }

            }
        });

        callUsers.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String message = registrarImpl.getMessage();
                    callText.setText(message);
                } catch (RemoteException ex) {
                    System.out.println("Problem with calling users");

                    throw new RuntimeException(ex);
                }
            }
        });

    }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Registrar main = new Registrar();
        main.run();
    }

}