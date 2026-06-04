package com.itss.importorder.model;

import java.time.LocalDateTime;

public class WarehouseReport {
    private final String reportCode;
    private final String requestCode;
    private final String checker;
    private final LocalDateTime checkedAt;
    private final String result;
    private final String note;

    public WarehouseReport(String reportCode, String requestCode, String checker, LocalDateTime checkedAt,
            String result, String note) {
        this.reportCode = reportCode;
        this.requestCode = requestCode;
        this.checker = checker;
        this.checkedAt = checkedAt;
        this.result = result;
        this.note = note;
    }

    public String getReportCode() {
        return reportCode;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public String getChecker() {
        return checker;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public String getResult() {
        return result;
    }

    public String getNote() {
        return note;
    }
}

