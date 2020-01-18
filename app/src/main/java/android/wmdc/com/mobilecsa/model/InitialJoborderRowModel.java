package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 6/6/2018.
 */

public class InitialJoborderRowModel {

    private int quotationId;
    private String customer;
    private String dateAdded;
    private String serial;
    private String model;
    private String make;
    private int isAdded;
    private String joNumber;

    public InitialJoborderRowModel(int quotationId, String customer, String dateAdded,
                                   String serial, String model, String make,
                                   int isAdded, String joNumber) {

        this.quotationId = quotationId;
        this.customer = customer;
        this.dateAdded = dateAdded;
        this.serial = serial;
        this.model = model;
        this.make = make;
        this.isAdded = isAdded;
        this.joNumber = joNumber;
    }

    public int getQuotationId() {
        return quotationId;
    }

    public String getCustomer() {
        return customer;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public String getModel() {
        return model;
    }

    public String getMake() {
        return make;
    }

    public String getSerial() {
        return serial;
    }

    public int getIsAdded() {
        return isAdded;
    }

    public String getJoNumber() {
        return joNumber;
    }
}
