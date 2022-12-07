package Registrar;

import java.time.LocalDateTime;

public class PseudonymHolder {
    private LocalDateTime time;
    private byte[] pseudonym;



    public PseudonymHolder(LocalDateTime t, byte[] p){
        this.time = t;
        this.pseudonym = p;
    }


    public LocalDateTime getTime(){
        return this.time;
    }

    public byte[] getPseudonym(){
        return this.pseudonym;
    }
}
