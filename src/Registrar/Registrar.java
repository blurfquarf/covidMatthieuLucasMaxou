package Registrar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;

public class Registrar {
    private void run() { try {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
        KeyPair pair = kpg.generateKeyPair();

        PrivateKey serverSK = pair.getPrivate();
        PublicKey serverPK = pair.getPublic();

        Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("RegistrarService", new RegistrarImpl(serverSK, serverPK));
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