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
    //TODO: rmi fct vooor docter: alle logs van een infected user doorsturen
    // mbv rmi fct van registrar alle pseudonymen van een bepaalde dag op vragen
    // validity van user logs checken door hash te nemen van Ri en pseudonym
    // (kijken of een matcht door hash van RI en een van de pseuddonymen van registrar te hashen met hash gelever door user log)
    // als match gevonden => alle capsules die die hash bevatten en in het interval van de besmette user aanwezig waren markeren als "critical"
    // + Token van de user die dokter bezocht als "informed" markeren`


    // TODO: na bepaald tijdsinterval vb 1 dag, overblijvende tokens ("uninformed") doorsturen naar registrar


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
