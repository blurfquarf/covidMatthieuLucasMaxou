/*
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Registrar {


    private void run() { try {

        Registry registry = LocateRegistry.createRegistry(1099);


// create a new service named CounterService
        registry.rebind("RegistrarService", new RegistrarImpl()); } catch (Exception e) {
        e.printStackTrace(); }
        System.out.println("system is ready");
    }


    public static void main(String[] args) {
        Registrar Registrar = new Registrar();
        Registrar.run();
    }
}
*/
