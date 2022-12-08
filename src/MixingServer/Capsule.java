package MixingServer;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Capsule {

    private byte[] token;
    private byte[] signature;
    private byte[] hash;
    private LocalDateTime time;
    private int Ri;


    //moment the capsule gets set to uninformed
    private LocalDateTime uninformedTime;
    
    public Capsule(byte[] t, byte[] s, byte[] h, int Ri, LocalDateTime time){
        this.token = t;
        this.signature = s;
        this.hash = h;
        this.time = time;
        this.Ri = Ri;
    }

    //time is when user sent this/visited
    public Capsule(byte[] t, byte[] s, byte[] h, LocalDateTime time){
        this.token = t;
        this.signature = s;
        this.hash = h;
        this.time = time;
    }

    public Capsule(byte[] t, byte[] h, LocalDateTime time, LocalDateTime uninformedTime){
        this.token = t;
        this.hash = h;
        this.time = time;
        this.uninformedTime = uninformedTime;
    }

    public Capsule(Capsule c){
        this.token = c.getToken();
        this.signature = c.getSignature();
        this.hash = c.getHash();
        this.time = c.getTime();
        this.Ri = c.getRandom();
        this.uninformedTime = c.getUninformedTime();
    }

    public byte[] getHash() {
        return hash;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public byte[] getSignature() {
        return signature;
    }


    //get token in capsule
    public byte[] getToken() {
        return token;
    }

    public int getRandom() {
        return this.Ri;
    }

    @Override
    public String toString() {
        return "Capsule{" +
                "token=" + Arrays.toString(token) +
                ", signature=" + Arrays.toString(signature) +
                ", hash=" + Arrays.toString(hash) +
                ", time=" + time +
                ", Ri=" + Ri +
                ", uninformedTime=" + uninformedTime +
                '}';
    }
}
