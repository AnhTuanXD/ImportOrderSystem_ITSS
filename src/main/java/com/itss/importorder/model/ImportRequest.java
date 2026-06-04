package com.itss.importorder.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ImportRequest {
    private final String requestCode;
    private String createdBy;
    private LocalDate createdDate;
    private RequestStatus status;
    private final List<MerchandiseRequest> items = new ArrayList<>();

    public ImportRequest(String requestCode, String createdBy, LocalDate createdDate, RequestStatus status) {
        this.requestCode = requestCode;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.status = status;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public List<MerchandiseRequest> getItems() {
        return items;
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(MerchandiseRequest::getQuantityOrdered).sum();
    }
}

