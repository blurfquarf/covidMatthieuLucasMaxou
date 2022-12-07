package Registrar;

import java.time.LocalDateTime;

public class ByteArrayHolder {
    private LocalDateTime time;
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

/*    @Override
    public boolean equals(Object o) {
        if (o instanceof ByteArrayHolder) {
            ByteArrayHolder b = (ByteArrayHolder) o;
            return this.time.equals(b.getTime()) && this.
        }
    }*/
}
