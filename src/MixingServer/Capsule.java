package MixingServer;

public class Capsule {

    private byte[] token;
    private byte[] signature;
    private byte[] hash;
    private String time;
    
    public Capsule(byte[] t, byte[] s, byte[] h, String time){
        token=t;
        signature=s;
        hash=h;
        time.equals(t);
    }

    public byte[] getHash() {
        return hash;
    }

    public String getTime() {
        return time;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getToken() {
        return token;
    }
}
