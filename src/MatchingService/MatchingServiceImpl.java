package MatchingService;
import MixingServer.Capsule;
import Registrar.RegistrarInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class MatchingServiceImpl extends UnicastRemoteObject implements MatchingServiceInterface {


    ArrayList<Capsule> capsuleList;

    public MatchingServiceImpl() throws Exception{
        capsuleList=new ArrayList<>();

    }
    public void addToCapsuleList(Capsule c){
        capsuleList.add(c);
    }

    public boolean checkIfMatchingCapsule(ArrayList<Capsule>infectedUserList){
        for(Capsule c : infectedUserList){
            for (Capsule match : capsuleList){
                int ctime= Integer.parseInt(c.getTime());
                int matchtime = Integer.parseInt(match.getTime());    
                boolean overlap = (ctime <= (matchtime+1)) && (matchtime <= ctime);
                boolean overlap2 = (matchtime <= (ctime+1)) && (ctime <= matchtime);
                if (c.getHash().equals(match.getHash()) && (overlap || overlap2)){
                    //now we have a match, we need to check if it is in the same business
                    //by checking Ri.

                }
            }
        }
        return false;
    }
}
