package com.itss.importorder.service;

import com.itss.importorder.model.ImportRequest;
import com.itss.importorder.model.RequestStatus;
import com.itss.importorder.model.WarehouseReport;
import com.itss.importorder.repository.DataStore;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WarehouseService {
    private final DataStore store;

    public WarehouseService(DataStore store) {
        this.store = store;
    }

    public List<ImportRequest> findOrdersForChecking() {
        return store.getImportRequests().stream()
                .filter(request -> request.getStatus() == RequestStatus.ORDERED
                        || request.getStatus() == RequestStatus.RECEIVED
                        || request.getStatus() == RequestStatus.PLANNING)
                .sorted(Comparator.comparing(ImportRequest::getCreatedDate).reversed())
                .collect(Collectors.toList());
    }

    public WarehouseReport createReport(ImportRequest request, String checker, String result, String note) {
        if (checker == null || checker.isBlank()) {
            throw new ValidationException("Người kiểm tra không được để trống.");
        }
        if (result == null || result.isBlank()) {
            throw new ValidationException("Kết quả kiểm tra không được để trống.");
        }
        WarehouseReport report = new WarehouseReport(nextReportCode(), request.getRequestCode(), checker,
                LocalDateTime.now(), result, note == null ? "" : note);
        
        try {
            store.saveWarehouseReport(report);
            store.getWarehouseReports().add(report);
            request.setStatus(RequestStatus.RECEIVED);
            store.saveImportRequest(request);
        } catch (SQLException e) {
            System.err.println("Lỗi lưu báo cáo kho: " + e.getMessage());
            throw new ValidationException("Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return report;
    }

    public List<WarehouseReport> findReports() {
        return store.getWarehouseReports();
    }

    private String nextReportCode() {
        return "WHR-" + String.format("%03d", store.getWarehouseReports().size() + 1);
    }
}


