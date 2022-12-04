package MixingServer;
import Registrar.RegistrarInterface;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MixingServerImpl extends UnicastRemoteObject implements MixingServerInterface {
    PrivateKey privateKey;
    PublicKey publicKey;

    ArrayList<byte[]> usedTokens;
    HashMap<byte[], String > capsuleList;
    //equivalent van 3 dagen
    int timeToHoldCapsules= 3;

    public MixingServerImpl() throws Exception{
        usedTokens= new ArrayList<>();
        capsuleList=new HashMap<>();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
        KeyPair pair = kpg.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();
    }

    public Map<byte[], byte[]> addCapsule(String time, byte[] token, byte[] signature, byte[] hash, RegistrarInterface registrarImpl) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException {
        boolean isSignatureValid=isValidToken(token, signature, registrarImpl);
        boolean isDayValid = isValidDay(token);
        boolean isunused = isUnused(token);
        Map<byte[], byte[]> signedHash = null;
        if(isDayValid && isSignatureValid && isunused){
            capsuleList.put(token, time);
            usedTokens.add(token);
            signedHash = new HashMap<>(signHash(hash));
        }
        return signedHash;
    }

    public Map<byte[], byte[]> signHash(byte[] hash) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Map<byte[], byte[]> ts = new HashMap<>();

        Signature signatureEngine = Signature.getInstance("SHA1withRSA");
        signatureEngine.initSign(privateKey);
        signatureEngine.update(hash);

        byte[] signature = signatureEngine.sign();

        ts.put(hash, signature);
        return ts;

    }

    public boolean isValidToken(byte[] token, byte[] signature, RegistrarInterface registrarImpl) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException {
        Signature signatureEngine = Signature.getInstance("SHA1withRSA");

        signatureEngine.initVerify(registrarImpl.getPK());
        signatureEngine.update(token);
        boolean valid = signatureEngine.verify(signature);
        System.out.println("tokenIsValid: " + valid);
        return valid;
    }

    public boolean isValidDay(byte[] token) throws RemoteException {
        LocalDate today=LocalDate.now();
        byte[] dateToken = subbytes(token, 0,9);

        if(Arrays.equals(dateToken, today.toString().getBytes(StandardCharsets.UTF_8))){
            return true;
        }
        else return false;
    }

    public boolean isUnused(byte[] token){
        for (int i = 0; i < usedTokens.size(); i++) {
            byte[] temp = usedTokens.get(i);
            if(Arrays.equals(temp, token)) {
                return false;
            }
        }
        System.out.println("Unused!");
        return true;
    }
    //method used to get first 10 bytes
    public static byte[] subbytes(byte[] source, int srcBegin, int srcEnd) {
        byte destination[];

        destination = new byte[srcEnd - srcBegin];
        System.arraycopy(source, srcBegin, destination, 0, srcEnd - srcBegin);
        return destination;
    }

    public PublicKey getPK() {
        return this.publicKey;
    }

}
