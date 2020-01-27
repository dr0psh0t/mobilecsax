package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 9/15/2018.
 */

public class WorkOrders {

    private int ctr;
    private String scopeWork;
    private boolean qcCsa;
    private boolean qcSup;
    private String status;
    private boolean isSelected;

    public WorkOrders(int ctr, String scopeWork, boolean qcCsa, boolean qcSup, String status) {
        this.ctr = ctr;
        this.scopeWork = scopeWork;
        this.qcCsa = qcCsa;
        this.qcSup = qcSup;
        this.status = status;
        this.isSelected = false;
    }

    public String getScopeWork() {
        return scopeWork;
    }

    public String getStatus() {
        return status;
    }

    public boolean getQcCsa() {
        return qcCsa;
    }

    public boolean getQcSup() {
        return qcSup;
    }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean isSelected) { this.isSelected = isSelected; }
}