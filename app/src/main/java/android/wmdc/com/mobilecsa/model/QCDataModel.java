package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 3/9/2018.
 */

public class QCDataModel implements Comparable<QCDataModel> {

    private int joId;
    private String serialNum;
    private String dateCommit;
    private String dateReceived;
    private String joNumber;
    private String customerId;
    private String model;
    private String category;
    private String make;
    private String customer;
    private boolean isPending;

    public QCDataModel(
            int joId,
            String serialNum,
            String dateCommit,
            String dateReceived,
            String joNumber,
            String customerId,
            String model,
            String category,
            String make,
            String customer,
            boolean isPending) {

        this.joNumber = joNumber;
        this.joId = joId;
        this.customer = customer;
        this.customerId = customerId;
        this.isPending= isPending;
        this.serialNum = serialNum;
        this.dateCommit = dateCommit;
        this.dateReceived = dateReceived;
        this.model = model;
        this.category = category;
        this.make = make;
    }

    @Override
    public int compareTo(QCDataModel other) {
        return other.joNumber.compareTo(joNumber);
    }

    public String getSerialNum() {
        return serialNum;
    }

    public String getDateCommit() {
        return dateCommit;
    }

    public String getDateReceived() {
        return dateReceived;
    }

    public String getModel() {
        return model;
    }

    public String getCategory() {
        return category;
    }

    public String getMake() {
        return make;
    }

    public String getJoNumber(){
        return joNumber;
    }

    public int getJoId() {
        return joId;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String toString() {
        return joNumber+' '+joId+' '+customer;
    }

    public boolean isPending() {
        return isPending;
    }
}
