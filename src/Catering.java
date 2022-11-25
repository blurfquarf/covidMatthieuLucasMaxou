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


            Thread mk = new Thread() {
                public void run(){
                    try {
                        registrarImpl.makeMasterKey(testBusiness);
                    } catch (RemoteException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            mk.start();

            ArrayList<String> QRCodes = new ArrayList<>();


            Thread req = new Thread(() -> {
                try {
                    ArrayList<SecretKey> derivedKeys = new ArrayList<>();
                    ArrayList<SecretKey> oldKeys = new ArrayList<>();

                    while(true) {
                        derivedKeys = registrarImpl.makeSecretsForCF(testBusiness);

                        System.out.println(derivedKeys);

                        //if request is sent to early, an empty list is returned
                        //check if te returned list is bigger than the current list
                        //if bigger add the keys to the derivedkey list
                        //if smaller we assume that not all the previous keys have been used
                        // and that there are still keys in the list available
                        if (derivedKeys.size() > oldKeys.size()) {
                            oldKeys = new ArrayList<>(derivedKeys);
                        }
                        else derivedKeys = new ArrayList<>(oldKeys);

                        for (SecretKey s : derivedKeys) {
                            //for every key in the list we use 1 every minute (read day)
                            //when a key is used we remove it from the list, so it cannot be reused
                            byte[] pseudonym = registrarImpl.generateCFPseudonym(testBusiness,s, testBusiness.getAdress(), LocalDate.from(LocalTime.now()));
                            QRCodes.add(generateQRCode(testBusiness, pseudonym));
                            oldKeys.remove(s);
                            Thread.sleep(3600);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            req.start();
            }   catch (Exception e){
                    e.printStackTrace();
        }
    }
}
