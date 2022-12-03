import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class User {

    private String phoneNr;
    private PrivateKey privk;
    private PublicKey pubk;
    private HashMap<String, QROutput> visitEntries = new HashMap<>();
    //hoe lang elke capsule bewaard moet worden
    private int duration = 7;

    private ArrayList<byte[]> tokens = new ArrayList<>();

    User(String nr, PrivateKey a, PublicKey b) {
        this.phoneNr = nr;
        this.privk = a;
        this.pubk = b;
    }

    public String getPhoneNr() {
        return this.phoneNr;
    }

    public void addTokens(Set<byte[]> t) {
        tokens.addAll(t);
    }

    public void addQROutput(QROutput q) {
        this.visitEntries.put(LocalDateTime.now().toString(), new QROutput(q));
    }


    public byte[] getToken() {
        LocalDate today = LocalDate.now();
        byte[] token = null;
        boolean found = false;
        while (!found) {
            byte[] temp = Arrays.copyOfRange(tokens.get(0), 0, 9);
            if(Arrays.equals(temp, today.toString().getBytes(StandardCharsets.UTF_8))) {
                token = temp;
                found = true;
            }
            //if token is invalid or used -> remove token
            tokens.remove(0);
        }
        return token;
    }

    public PrivateKey getPrivk() {
        return privk;
    }

    public PublicKey getPubk() {
        return pubk;
    }
}
