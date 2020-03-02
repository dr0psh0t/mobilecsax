package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 3/9/2018.
 */

public class DateCommitModel {

    private int joId;
    private String joNumber;
    private String customerId;
    private String customer;
    private boolean isCsaApproved;
    private boolean isPnmApproved;
    private String dateCommit;
    private String dateReceive;

    public DateCommitModel(int joId, String joNumber, String customerId, String customer,
                           boolean isCsaApproved, boolean isPnmApproved, String dateCommit,
                           String dateReceive) {

        this.joId = joId;
        this.joNumber = joNumber;
        this.customerId = customerId;
        this.customer = customer;
        this.isCsaApproved = isCsaApproved;
        this.isPnmApproved = isPnmApproved;
        this.dateCommit = dateCommit;
        this.dateReceive = dateReceive;
    }

    public int getJoId() {
        return joId;
    }

    public String getJoNumber() {
        return joNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomer() {
        return customer;
    }

    public boolean getCsaApproved() {
        return this.isCsaApproved;
    }

    public void setCsaApproved(boolean csaApproved) {
        isCsaApproved = csaApproved;
    }

    public boolean getPnmApproved() {
        return this.isPnmApproved;
    }

    public String getDateCommit() {
        return dateCommit;
    }

    public String getDateReceive() {
        return dateReceive;
    }
}
