import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrarInterface extends Remote {


    void sendInfo(Business b) throws RemoteException;


    void sendString(String s) throws RemoteException;
    //String request() throws RemoteException;

}
