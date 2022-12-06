package MixingServer;

import java.time.LocalDateTime;

public class Capsule {

    private byte[] token;
    private byte[] signature;
    private String hash;
    private LocalDateTime time;
    private int Ri;
    
    public Capsule(byte[] t, byte[] s, String h, int Ri, LocalDateTime time){
        this.token = t;
        this.signature = s;
        this.hash = h;
        this.time = time;
        this.Ri = Ri;
    }


    //time is when user sent this/visited
    public Capsule(byte[] t, byte[] s, String h, LocalDateTime time){
        this.token = t;
        this.signature = s;
        this.hash = h;
        this.time = time;
    }

    public String getHash() {
        return hash;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getToken() {
        return token;
    }

    public int getRandom() {
        return this.Ri;
    }
}
