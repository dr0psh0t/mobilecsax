package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 4/6/2018.
 */

public class JobOrder {

    private String joNumber;
    private int joId;
    private String serialNum;
    private String dateCommit;
    private String dateReceived;
    private String customerId;
    private Object model;
    private Object make;
    private String customer;

    public JobOrder(String joNumber,
                    int joId,
                    String serialNum,
                    String dateCommit,
                    String dateReceived,
                    String customerId,
                    String model,
                    String make,
                    String customer) {

        this.joNumber = joNumber;
        this.joId = joId;
        this.serialNum = serialNum;
        this.dateCommit = dateCommit;
        this.dateReceived = dateReceived;
        this.customerId = customerId;
        this.model = model;
        this.make = make;
        this.customer = customer;
    }


    public String getJoNumber() {
        return joNumber;
    }

    public int getJoId() {
        return joId;
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

    public String getCustomerId() {
        return customerId;
    }

    public Object getModel() {
        return model;
    }

    public Object getMake() {
        return make;
    }

    public String getCustomer() {
        return customer ;
    }
}