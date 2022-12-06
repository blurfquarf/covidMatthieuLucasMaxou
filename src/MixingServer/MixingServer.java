package MixingServer;

import Registrar.RegistrarInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        JLabel flushLabel = new JLabel("Press button to flush capsules to matching server!");
        JFrame frame = new JFrame();
        JButton flush = new JButton("Flush!");
        flush.setEnabled(true);
        flushLabel.setPreferredSize(new Dimension(200, 50));
        JPanel mixingPanel = new JPanel(new GridLayout(10, 1, 100, 5));

        frame.setTitle("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);
        frame.setSize(1280,500);
        frame.setLocationRelativeTo(null);

        mixingPanel.add(flush);
        mixingPanel.add(flushLabel);

        frame.setVisible(true);

        flush.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mixingServerImpl.flushCapsules(matchingServiceImpl);
                } catch (RemoteException ex) {
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
