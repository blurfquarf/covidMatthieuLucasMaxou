import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RegistrarImpl extends UnicastRemoteObject implements RegistrarInterface{
    public RegistrarImpl () throws RemoteException{}
    ArrayList<String> messages = new ArrayList<>();


    @Override
    synchronized public void send(String s) throws RemoteException{
        messages.add(s);
        notifyAll();
    }

    @Override
    synchronized public String request() throws RemoteException {
        while(true){
            try{
                wait();
                return messages.get(messages.size()-1);
            }
            catch (InterruptedException e){
                System.out.println("no messages?");
            }
        }
    }

}