package MixingServer;

public class Capsule {

    private byte[] token;
    private byte[] signature;
    private String hash;
    private String time;
    
    public Capsule(byte[] t, byte[] s, String h, String time){
        this.token = t;
        this.signature = s;
        this.hash = h;
        this.time = time;
    }

    public Capsule(byte[] t, byte[] s, String h, LocalDateTime time){
        this.token = t;
        this.signature = s;
        this.hash = h;
        this.time = time;
    }

    public String getHash() {
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
