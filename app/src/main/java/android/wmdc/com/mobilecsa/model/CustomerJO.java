package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 7/4/2018.
 */

public class CustomerJO {

    private int cId;
    private String source;
    private String customer;

    public CustomerJO(int cId, String source, String customer) {
        this.cId = cId;
        this.source = source;
        this.customer = customer;
    }

    public int getcId() {
        return cId;
    }

    public String getSource() {
        return source;
    }

    public String getCustomer() {
        return customer;
    }
}