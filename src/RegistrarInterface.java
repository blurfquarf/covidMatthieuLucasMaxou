import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrarInterface extends Remote {


    void send(String s) throws RemoteException;

    String request() throws RemoteException;




}
