import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.HashMap;

public class serverDB {



    //lijsten stellen kolommen in databank voor
    //private HashMap<Business, byte[]> secretKeys;

    private HashMap<String, LocalDateTime> timestamps;
    private HashMap<String, byte[]> pseudonyms;

    private HashMap<Integer, Integer> users;

    public serverDB() {
        //secretKeys = new HashMap<>();
        timestamps = new HashMap<>();
        pseudonyms = new HashMap<>();
    }

    public HashMap<String, LocalDateTime> getTimestamps() {
        return timestamps;
    }

    //enkel te gebruiken voor initialisatie
/*    public void addIdentifiers(String bNaam, SecretKey k) {
        //byte[] encoded = k.getEncoded();
        byte[] encKey = new byte[k.getEncoded().length];
        for (int i = 0; i < k.getEncoded().length; i++) {
            encKey[i] = k.getEncoded()[i];
        }

        //secretKeys.put(b, encKey);
        timestamps.put(bNaam, String.valueOf(LocalDateTime.now()));
    }*/


    public void deleteCF(String bNaam){
        //secretKeys.remove(b);
        timestamps.remove(bNaam);
        if (pseudonyms.containsKey(bNaam)) pseudonyms.remove(bNaam);
    }

   /* public byte[] getSecretKey(Business b) {
        return secretKeys.get(b);
    }*/

    public LocalDateTime getTimestamp(String bNaam) {
        return timestamps.get(bNaam);
    }

    public void setLocalDateTime(String bNaam, LocalDateTime d) {
        timestamps.put(bNaam, d);
    }

    public byte[] getPseudonym(Business b) {
        return pseudonyms.get(b);
    }

    public void setPseudonym(Business b, byte[] p) {
        pseudonyms.put(b, p);
    }

 /*   public boolean existsBusiness(int btw) {
        for (Business b: secretKeys.keySet()) {
            if (b.getBtw() == btw) return true;
        }
        return false;
    }*/

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
