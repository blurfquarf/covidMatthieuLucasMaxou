package Registrar;

import java.time.LocalDateTime;
import java.util.Arrays;

public class ByteArrayHolder {
    private LocalDateTime time;


    //hash van de zaak
    private byte[] byteArray;

    public ByteArrayHolder(LocalDateTime t, byte[] b){
        this.time = t;
        this.byteArray = b;
    }


    public LocalDateTime getTime(){
        return this.time;
    }

    public byte[] getByteArray(){
        return this.byteArray;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ByteArrayHolder b) {
            return this.time.equals(b.getTime()) && Arrays.equals(this.byteArray, b.getByteArray());
        } else return false;
    }

    @Override
    public String toString() {
        return "ByteArrayHolder{" +
                "time=" + time +
                ", byteArray=" + Arrays.toString(byteArray) +
                '}';
    }
}
