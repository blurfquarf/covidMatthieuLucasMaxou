import java.io.Serial;
import java.io.Serializable;

public class Business implements Serializable {
    private static String name;
    private static int btw;
    private static String adress;

    private static int reqCounter;


    public Business(String name, int btw, String adress) {
        this.name = name;
        this.btw = btw;
        this.adress = adress;
        reqCounter = 1;
    }

    public static String getName() {
        return name;
    }

    public int getBtw() {
        return btw;
    }

    public String getAdress() {
        return adress;
    }


    public int getReqCounter() {
        return reqCounter;
    }

    public void setReqCounter(int reqCounter) {
        Business.reqCounter = reqCounter;
    }


}
