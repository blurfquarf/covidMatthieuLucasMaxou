//import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RegistrarImpl extends UnicastRemoteObject implements RegistrarInterface{

    ArrayList<Business> messages = new ArrayList<>();
    ArrayList<String> testMessages = new ArrayList<>();


    public RegistrarImpl() throws Exception {
        super(1099/*, new RMISSLClientSocketFactory(),
                new RMISSLServerSocketFactory()*/);
    }

    public void sendInfo(Business b) throws RemoteException{
        messages.add(b);
        System.out.println(messages);
    }

    public void sendString(String s) throws RemoteException {
        testMessages.add(s);
        System.out.println(testMessages);
    }

        public static void main(String args[]) throws Exception {
        try {
            //System.setProperty("java.rmi.server.hostname", "192.168.1.51");

            Registry registry = LocateRegistry.createRegistry(1099/*, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory()*/);
            registry.rebind("RegistrarService", new RegistrarImpl());
            System.out.println("system is ready");
        }
         catch (Exception e) {
            e.printStackTrace();
        }
    }
}

