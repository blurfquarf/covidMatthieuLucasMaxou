import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrarInterface extends Remote {


    String sendInfo(Business b) throws RemoteException;

    //String request() throws RemoteException;

}
