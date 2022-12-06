package User;

public class QROutput {

    private int random;
    private int CF;
    private String hash;

    QROutput(String qrCode) {
        this.random = Integer.parseInt(qrCode.substring(0,3));
        this.CF = Integer.parseInt(qrCode.substring(4,9));
        this.hash = qrCode.substring(10);
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