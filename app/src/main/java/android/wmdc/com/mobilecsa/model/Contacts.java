package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 12/27/2017.
 */

public class Contacts {

    private String name;
    private String imageUrl;
    private String csa;
    private int csaId;
    private int id;
    private int isTransferred;

    public Contacts(String name, String imageUrl, String csa,
                    int csaId, int id, int isTransferred) {

        this.name = name;
        this.imageUrl = imageUrl;
        this.csa = csa;
        this.csaId = csaId;
        this.id = id;
        this.isTransferred = isTransferred;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCsa() {
        return csa;
    }

    public int getCsaId() {
        return csaId;
    }

    public int getId() {
        return id;
    }

    public int getIsTransferred() {
        return isTransferred;
    }

    @Override
    public String toString() {
        return name+", "+imageUrl+", "+id;
    }
}
