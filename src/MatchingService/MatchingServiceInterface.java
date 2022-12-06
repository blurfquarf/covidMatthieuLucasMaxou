package MatchingService;

import java.rmi.RemoteException;
import java.time.LocalDateTime;

public interface MatchingServiceInterface {

    void send(LocalDateTime time, String hash, byte[] token, byte[] signature) throws RemoteException;








    }
