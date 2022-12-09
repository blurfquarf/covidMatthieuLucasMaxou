package MixingServer;

import MatchingService.MatchingServiceImpl;
import MatchingService.MatchingServiceInterface;
import Registrar.RegistrarInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

public class MixingServer {

    private void run() { try {
        Registry registry = LocateRegistry.createRegistry(1101);
        registry.rebind("MixingService", new MixingServerImpl());
        System.out.println("Mixing system  is ready");

        Registry mixingRegistry = LocateRegistry.getRegistry("localhost", 1101);
        MixingServerInterface mixingServerImpl = (MixingServerInterface) mixingRegistry.lookup("MixingService");

        JLabel flushLabel = new JLabel("Press button to flush capsules to matching server!");
        JFrame frame = new JFrame();
        JButton flush = new JButton("Flush!");
        JButton showContents = new JButton("Show content of Mixing server");
        JButton showUsedTokens = new JButton("Show used tokens");
        JButton showCapsules = new JButton("Show list of capsules");
        JButton close = new JButton("Close");
        JFrame contentFrame = new JFrame();
        JPanel contentPanel = new JPanel(new GridLayout(10, 1, 100, 5));
        JScrollPane contentScrollPane = new JScrollPane();

        flush.setEnabled(true);
        flushLabel.setPreferredSize(new Dimension(200, 50));
        JPanel mixingPanel = new JPanel(new GridLayout(10, 1, 100, 5));

        frame.setTitle("Mixing Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);
        frame.setSize(1280,500);
        frame.setLocationRelativeTo(null);

        mixingPanel.add(flush);
        mixingPanel.add(flushLabel);
        mixingPanel.add(showContents);
        showContents.setEnabled(true);

        frame.add(mixingPanel,BorderLayout.CENTER);

        frame.setVisible(true);

        flush.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mixingServerImpl.flushCapsules();
                } catch (RemoteException | NotBoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        showContents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                contentPanel.setPreferredSize(new Dimension(100,100));
                contentPanel.setBackground(Color.lightGray);
                contentFrame.setTitle("Content of mixing service");
                contentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                contentFrame.setLayout(new BorderLayout());
                contentFrame.setResizable(true);
                contentFrame.setSize(1280,500);
                contentFrame.setLocationRelativeTo(null);
                contentFrame.revalidate();

                contentPanel.add(showUsedTokens);
                contentPanel.add(showCapsules);
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

        showUsedTokens.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    JList<String> list = mixingServerImpl.showTokens();
                    contentScrollPane.setViewportView(list);
                } catch (RemoteException ex) {
                    System.out.println("problem with showing used tokens");
                    throw new RuntimeException(ex);
                }

            }
        });
        showCapsules.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JList<String> list = mixingServerImpl.showCapsules();
                    contentScrollPane.setViewportView(list);
                } catch (RemoteException ex) {
                    System.out.println("problem with showing capsules");
                    throw new RuntimeException(ex);
                }
            }
        });


    }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        MixingServer main =  new MixingServer();
        main.run();
    }
}
