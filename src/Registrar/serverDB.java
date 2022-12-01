package Registrar;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class serverDB {



    //lijsten stellen kolommen in databank voor
    //private HashMap<Business, byte[]> secretKeys;

    private HashMap<String, LocalDateTime> timestamps;
    private HashMap<String, byte[]> pseudonyms;

    private HashMap<Integer, Integer> days;

    private ArrayList<String> registeredPhonenumbers;

    //tokenmapping by phone number
    private HashMap<byte[], String> tokenMappings;

    public serverDB() {
        //secretKeys = new HashMap<>();
        timestamps = new HashMap<>();
        pseudonyms = new HashMap<>();
        tokenMappings = new HashMap<>();
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

    public ArrayList<String> getRegisteredPhonenumbers(){
        return registeredPhonenumbers;
    }

    public LocalDateTime getTimestamp(String bNaam) {
        return timestamps.get(bNaam);
    }

    public void setLocalDateTime(String bNaam, LocalDateTime d) {
        timestamps.put(bNaam, d);
    }

    public byte[] getPseudonym(String bNaam) {
        return pseudonyms.get(bNaam);
    }

    public void setPseudonym(String bNaam, byte[] p) {
        pseudonyms.put(bNaam, p);
    }

    public Map<byte[], String> getTokenMappings(){
        return tokenMappings;
    }

    public void setDays(int btw, int day){
        days.put(btw, day);
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
