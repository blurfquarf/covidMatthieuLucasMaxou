import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class Registrar {

    public static SecretKey makeMasterKey() throws NoSuchAlgorithmException {
        //existsBusiness checks if a business with the same btw Nr already exists if it already exists then the masterkey won't be created
        //if(!registrarDB.existsBusiness(b.getBtw())) {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey S = kg.generateKey();
        return S;
    }
    private void run() { try {
            //System.setProperty("java.rmi.server.hostname", "192.168.1.51");

        serverDB registrarDB = new serverDB();
        SecretKey serverSK = makeMasterKey();

        Registry registry = LocateRegistry.createRegistry(1099/*, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory()*/);
            registry.rebind("RegistrarService", new RegistrarImpl(registrarDB, serverSK));
            System.out.println("system is ready");
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