import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.HashMap;

public class serverDB {

    //lijsten stellen kolommen in databank voor

    private HashMap<Business, SecretKey> secretKeys;

    private HashMap<Business, LocalDateTime> timestamps;
    private HashMap<Business, byte[]> pseudonyms;

    private HashMap<User, Integer> users;

    public serverDB() {
        secretKeys = new HashMap<>();
        timestamps = new HashMap<>();
        pseudonyms = new HashMap<>();
    }

    public void addIdentifiers(Business b, SecretKey k, LocalDateTime t) {
        secretKeys.put(b,k);
        timestamps.put(b,t);
    }


    public void deleteCF(Business b){
        secretKeys.remove(b);
        timestamps.remove(b);
        if (pseudonyms.containsKey(b)) pseudonyms.remove(b);
    }

    public SecretKey getSecretKey(Business b) {
        return secretKeys.get(b);
    }

    public LocalDateTime getTimestamp(Business b) {
        return timestamps.get(b);
    }

    public void setLocalDateTime(Business b, LocalDateTime d) {
        timestamps.put(b, d);
    }

    public byte[] getPseudonym(Business b) {
        return pseudonyms.get(b);
    }

    public void setPseudonym(Business b, byte[] p) {
        pseudonyms.put(b, p);
    }

    public boolean existsBusiness(int btw) {
        for (Business b: secretKeys.keySet()) {
            if (b.getBtw() == btw) return true;
        }
        return false;
    }

    public boolean existsUser(int nr) {
        for (User u : users.keySet()) {
            if(u.getPhoneNr()==nr) return true;
        }
        return false;
    }

   /* public ArrayList<Business> getCfs() {
        return Cfs;
    }

    public void setCfs(ArrayList<Business> cfs) {
        Cfs = cfs;
    }

    public ArrayList<SecretKey> getSecretKeys() {
        return secretKeys;
    }

    public void setSecretKeys(ArrayList<SecretKey> secretKeys) {
        this.secretKeys = secretKeys;
    }

    public ArrayList<LocalDate> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(ArrayList<LocalDate> timestamps) {
        this.timestamps = timestamps;
    }*/



}
