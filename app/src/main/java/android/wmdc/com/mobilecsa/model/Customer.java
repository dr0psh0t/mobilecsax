package android.wmdc.com.mobilecsa.model;

import androidx.annotation.NonNull;

/**
 * Created by wmdcprog on 12/26/2017.
 * this class generically represents contacts and customers
 */

public class Customer {

    private String name;
    private String imageUrl;
    private String csa;
    private int csaId;
    private int id;
    private boolean isPerson;
    private int isTransferred;

    public Customer(String name, String imageUrl, String csa, int csaId, int id, boolean isPerson,
                    int isTransferred) {

        this.name = name;
        this.imageUrl = imageUrl;
        this.csa = csa;
        this.csaId = csaId;
        this.id = id;
        this.isPerson = isPerson;
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

    public boolean isAPerson() {
        return isPerson;
    }

    public int getIsTransferred() {
        return isTransferred;
    }

    @Override
    @NonNull
    public String toString() {
        return name + ", " + isPerson;
    }
}
