package android.wmdc.com.mobilecsa.model;

import android.wmdc.com.mobilecsa.utils.Util;

/**
 * Created by wmdcprog on 5/7/2018.
 */

public class WorkOrderModel {

    private String scopeWork;
    private boolean isSupervisorId;
    private boolean isCsaQc;
    private int workOrderId;
    private int joId;
    private int isCompleted;

    public WorkOrderModel(String scopeWork, boolean isSupervisorId,
                          boolean isCsaQc, int workOrderId, int joId,
                          int isCompleted) {

        this.scopeWork = Util.getDecodedHTMLEntity(scopeWork, "");
        this.isSupervisorId = isSupervisorId;
        this.isCsaQc = isCsaQc;
        this.workOrderId = workOrderId;
        this.joId = joId;
        this.isCompleted = isCompleted;
    }

    public String getScopeWork() {
        return scopeWork;
    }

    public boolean getIsSupervisorId() {
        return isSupervisorId;
    }

    public boolean getIsCsaQc() {
        return isCsaQc;
    }

    public int getWorkOrderId() {
        return workOrderId;
    }

    public int getJoId() {
        return joId;
    }

    public int getIsCompleted() {
        return isCompleted;
    }
}
