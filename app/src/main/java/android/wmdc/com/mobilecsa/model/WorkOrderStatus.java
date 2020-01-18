package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 8/31/2018.
 */

public class WorkOrderStatus {

    private String workItem;
    private boolean isCompleted;

    public WorkOrderStatus(String workItem, boolean isCompleted)
    {
        this.workItem = workItem;
        this.isCompleted = isCompleted;
    }

    public String getWorkItem() {
        return workItem;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }
}
