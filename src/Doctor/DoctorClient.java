package Doctor;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

public class DoctorClient {

    //TODO: functie get users log + timeinterval (uitlezen van file die gegenereerd is door user)
    // alle logs van een user signen en naar matching service sturen


    public static



    public static void main(String[] args) throws InvalidAlgorithmParameterException, NotBoundException, SignatureException, RemoteException, InvalidKeyException {
        DoctorClient d = new DoctorClient();
        d.run();
    }

    public void run() throws RemoteException, NotBoundException, InvalidAlgorithmParameterException, SignatureException, InvalidKeyException {




    }

}
