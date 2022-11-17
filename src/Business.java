public class Business {
    private final String name;
    private final int btw;
    private final String adress;

    public Business(String name, int btw, String adress) {
        this.name = name;
        this.btw = btw;
        this.adress = adress;
    }

    public String getName() {
        return name;
    }

    public int getBtw() {
        return btw;
    }

    public String getAdress() {
        return adress;
    }



}
