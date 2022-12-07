package User;

import java.math.BigInteger;
import java.util.Arrays;

public class QROutput {

    private int random;
    private int CF;
    private String hash;

    QROutput(String qrCode) {
        String[] temp = qrCode.split(",");
        this.random = Integer.parseInt(temp[0]);
        this.CF = Integer.parseInt(temp[1]);
        this.hash = temp[2];
        System.out.println("random: "+this.random);
        System.out.println("CF: "+this.CF);
        byte[] bytes = new BigInteger(hash, 2).toByteArray();
        System.out.println("hash: "+ Arrays.toString(bytes));
    }

    QROutput(QROutput q) {
        this.random = q.random;
        this.CF = q.CF;
        this.hash = q.hash;
    }

    public String getHash() {
        return this.hash;
    }

    public int getRandom() {
        return this.random;
    }

    public int getCF() {
        return CF;
    }
}
