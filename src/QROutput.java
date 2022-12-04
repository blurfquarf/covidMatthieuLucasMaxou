import java.nio.charset.StandardCharsets;

public class QROutput {

    private int random;
    private int CF;
    private String hash;

    QROutput(String qrCode) {
        this.random = Integer.parseInt(qrCode.substring(0,3));
        this.CF = Integer.parseInt(qrCode.substring(4,9));
        this.hash = String.valueOf(qrCode.substring(10).getBytes(StandardCharsets.UTF_8));
    }

    QROutput(QROutput q) {
        this.random = q.random;
        this.CF = q.CF;
        this.hash = q.hash;
    }




}
