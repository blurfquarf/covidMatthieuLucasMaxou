import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class User {

    private String phoneNr;
    //Lists with scanned QR codes, and the random number and id's of the visited catering facilties.
    //These will be used when a user is contaminated, to search in the matching service.
    private ArrayList<String> scannedQRCodes;
    private ArrayList<Integer> randomNumbers;
    private ArrayList<Integer> idOfCateringFacilities;

    private PrivateKey privk;
    private PublicKey pubk;
    private HashMap<String, QROutput> visitEntries = new HashMap<>();

    //hoe lang elke capsule bewaard moet worden
    private int duration = 7;

    private HashMap<byte[], byte[]> tokens = new HashMap<>();

    User(String nr, PrivateKey a, PublicKey b) {
        this.phoneNr = nr;
        this.privk = a;
        this.pubk = b;
        scannedQRCodes = new ArrayList<>();
        randomNumbers = new ArrayList<>();
        idOfCateringFacilities = new ArrayList<>();
    }

    public String getPhoneNr() {
        return this.phoneNr;
    }

    public void addTokens(Map<byte[], byte[]> t) {
        tokens.putAll(t);
    }

    public void addQROutput(QROutput q) {
        this.visitEntries.put(LocalDateTime.now().toString(), new QROutput(q));
    }

    public void addToScanned(String QRCode){
        scannedQRCodes.add(QRCode);
    }

    public Map.Entry<byte[], byte[]> getToken() {
        Iterator<Map.Entry<byte[], byte[]>> it = tokens.entrySet().iterator();
        Map.Entry<byte[], byte[]> entry = null;
        if(it.hasNext()){
            entry = tokens.entrySet().iterator().next();
            tokens.remove(entry.getKey());
        }
        return entry;
    }

    public int getMapSize() {
        return tokens.keySet().size();
    }

    public PrivateKey getPrivk() {
        return privk;
    }

    public PublicKey getPubk() {
        return pubk;
    }


    public void parseQRCodes() {
        String QRCode = scannedQRCodes.get(scannedQRCodes.size()-1);
        int Ri = Integer.parseInt(QRCode.substring(0,3));
        randomNumbers.add(Ri);
        int btwNumber = Integer.parseInt(QRCode.substring(4,9));
        idOfCateringFacilities.add(btwNumber);
    }
}
