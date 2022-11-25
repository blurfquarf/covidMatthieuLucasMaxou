import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Catering {

    public static void main(String[] args) {

        Business testBusiness = new Business("bert", 123321, "Gent");

        try{
            Registry myRegistry = LocateRegistry.getRegistry("localhost",
                    1099/*, new RMISSLClientSocketFactory()*/);
            RegistrarInterface registrarImpl = (RegistrarInterface) myRegistry.lookup("RegistrarService");


            Thread t = new Thread() {
                public void run(){
                    try {
                        registrarImpl.makeMasterKey(testBusiness);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    } catch (InvalidKeySpecException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            t.start();

            }   catch (Exception e){
                    e.printStackTrace();
        }
    }
}
