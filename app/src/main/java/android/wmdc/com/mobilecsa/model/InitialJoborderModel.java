package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 6/7/2018.
 */

public class InitialJoborderModel {

    private int quotationId;
    private String customer;
    private String mobile;
    private String purchaseOrder;
    private String poDate;
    private String model;
    private String make;
    private String category;
    private String serialNo;
    private String dateReceived;
    private String dateCommitted;
    private String referenceNo;
    private String remarks;

    public InitialJoborderModel(int quotationId, String customer, String mobile,
                                String purchaseOrder, String poDate, String model,
                                String make, String category, String serialNo,
                                String dateReceived, String dateCommitted,
                                String referenceNo, String remarks) {

        this.quotationId = quotationId;
        this.customer = customer;
        this.mobile = mobile;
        this.purchaseOrder = purchaseOrder;
        this.poDate = poDate;
        this.model = model;
        this.make = make;
        this.category = category;
        this.serialNo = serialNo;
        this.dateReceived = dateReceived;
        this.dateCommitted = dateCommitted;
        this.referenceNo = referenceNo;
        this.remarks = remarks;
    }

    public int getQuotationId() {
        return quotationId;
    }

    public String getCustomer() {
        return customer;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public String getPoDate() {
        return poDate;
    }

    public String getModel() {
        return model;
    }

    public String getMake() {
        return make;
    }

    public String getCategory() {
        return category;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public String getDateReceived() {
        return dateReceived;
    }

    public String getDateCommitted() {
        return dateCommitted;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public String getRemarks() {
        return remarks;
    }
}