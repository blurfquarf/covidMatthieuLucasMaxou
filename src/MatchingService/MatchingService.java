package MatchingService;
import MixingServer.MixingServer;
import MixingServer.MixingServerImpl;
import Registrar.RegistrarInterface;

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

public class MatchingService {

    private void run() { try {

        Registry registry = LocateRegistry.createRegistry(1100);
        registry.rebind("MatchingService", new MatchingServiceImpl());
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



        //registrarImpl.getPseudonymsPerDay();

    }catch (Exception e){
        e.printStackTrace();
    }
    }

    public static void main(String[] args) {
        MatchingService main =  new MatchingService();
        main.run();
    }
}
