public class Business {
    private static String name;
    private static int btw;
    private static String adress;

    public Business(String name, int btw, String adress) {
        this.name = name;
        this.btw = btw;
        this.adress = adress;
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



}
