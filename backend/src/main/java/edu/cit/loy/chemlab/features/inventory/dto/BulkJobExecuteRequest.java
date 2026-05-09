package edu.cit.loy.chemlab.features.inventory.dto;

public class BulkJobExecuteRequest {

    private boolean executeOnlyValidRows = true;

    public boolean isExecuteOnlyValidRows() { return executeOnlyValidRows; }
    public void setExecuteOnlyValidRows(boolean executeOnlyValidRows) { this.executeOnlyValidRows = executeOnlyValidRows; }
}