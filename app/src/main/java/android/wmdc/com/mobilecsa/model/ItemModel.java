package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 4/4/2018.
 */

public class ItemModel {

    private String item;
    private int status;

    public ItemModel(String item, int status)
    {
        this.item = item;
        this.status = status;
    }

    public String getItem() {
        return item;
    }

    public int getStatus() {
        return status;
    }

    public String toString() {
        return item + " " +status;
    }
}