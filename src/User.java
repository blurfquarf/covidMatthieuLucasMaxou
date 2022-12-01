import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class User {

    private String phoneNr;
    private HashMap<String, QROutput> visitEntries = new HashMap<>();
    //hoe lang elke capsule bewaard moet worden
    private int duration = 7;

    private ArrayList<byte[]> tokens = new ArrayList<>();

    User(String nr) {
        this.phoneNr = nr;
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
            else {
                tokens.remove(0);
            }
        }
        return token;
    }





}
