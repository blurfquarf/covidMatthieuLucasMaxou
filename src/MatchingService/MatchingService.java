package MatchingService;
import MixingServer.MixingServer;
import MixingServer.MixingServerImpl;
import Registrar.RegistrarInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MatchingService {

    private void run() { try {

        Registry registry = LocateRegistry.createRegistry(1100);
        registry.rebind("MatchingService", new MatchingServiceImpl());
        System.out.println("Matching Service is ready");


        Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrarImpl = (RegistrarInterface) registrarRegistry.lookup("RegistrarService");



        int timer = 0;
        while (true) {
            if(timer%10==0) {

            }
            timer++;
        }



    }catch (Exception e){
        e.printStackTrace();
    }
    }

    public static void main(String[] args) {

        MatchingService main =  new MatchingService();
        main.run();
    }
}
