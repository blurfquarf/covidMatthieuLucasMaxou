import javax.crypto.SecretKey;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.ArrayList;

public interface RegistrarInterface extends Remote {

    void makeMasterKey(Business b) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException;

    ArrayList<SecretKey> makeSecretsForCF(Business b) throws NoSuchAlgorithmException, InvalidKeySpecException;

    byte[] generateCFPseudonym(Business b, SecretKey s, String location, LocalDate d) throws NoSuchAlgorithmException;
}
