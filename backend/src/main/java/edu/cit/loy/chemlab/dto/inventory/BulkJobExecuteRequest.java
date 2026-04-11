package edu.cit.loy.chemlab.dto.inventory;

public class BulkJobExecuteRequest {

    private boolean executeOnlyValidRows = true;

    public boolean isExecuteOnlyValidRows() {
        return executeOnlyValidRows;
    }

    public void setExecuteOnlyValidRows(boolean executeOnlyValidRows) {
        this.executeOnlyValidRows = executeOnlyValidRows;
    }
}
