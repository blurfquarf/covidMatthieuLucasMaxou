package MixingServer;

import Registrar.RegistrarInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Signature;
import java.util.Iterator;
import java.util.Map;

public class MixingServer {

    private void run() { try {

        Registry registry = LocateRegistry.createRegistry(1101);
        registry.rebind("MixingService", new MixingServerImpl());
        System.out.println("Mixing system  is ready");

/*
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");*/




    }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        MixingServer main =  new MixingServer();
        main.run();
    }
}
