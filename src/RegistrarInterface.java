import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface RegistrarInterface extends Remote {


    void sendInfo(Business b) throws RemoteException;


    void sendString(String s) throws RemoteException;
    //String request() throws RemoteException;


    void makeMasterKey(Business b) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException;

}
