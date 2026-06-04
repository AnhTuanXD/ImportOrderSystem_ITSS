package com.itss.importorder.service;

import com.itss.importorder.model.ImportRequest;
import com.itss.importorder.model.MerchandiseRequest;
import com.itss.importorder.model.RequestStatus;
import com.itss.importorder.repository.DataStore;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImportRequestService {
    private final DataStore store;

    public ImportRequestService(DataStore store) {
        this.store = store;
    }

    public List<ImportRequest> findAll() {
        return store.getImportRequests().stream()
                .sorted(Comparator.comparing(ImportRequest::getCreatedDate).reversed())
                .collect(Collectors.toList());
    }

    public List<ImportRequest> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String normalized = keyword.toLowerCase(Locale.ROOT).trim();
        return findAll().stream()
                .filter(request -> request.getRequestCode().toLowerCase(Locale.ROOT).contains(normalized)
                        || request.getStatus().getDisplayName().toLowerCase(Locale.ROOT).contains(normalized)
                        || request.getItems().stream().anyMatch(item ->
                                item.getMerchandiseCode().toLowerCase(Locale.ROOT).contains(normalized)))
                .collect(Collectors.toList());
    }

    public ImportRequest create(String createdBy, List<MerchandiseRequest> items) {
        validateItems(items);
        String code = "REQ-2025-" + String.format("%03d", store.getImportRequests().size() + 1);
        ImportRequest request = new ImportRequest(code, createdBy, LocalDate.now(), RequestStatus.SENT);
        request.getItems().addAll(items);
        
        try {
            store.saveImportRequest(request);
        } catch (SQLException e) {
            System.err.println("Lỗi lưu yêu cầu vào database: " + e.getMessage());
            throw new ValidationException("Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return request;
    }

    public void updateItems(ImportRequest request, List<MerchandiseRequest> items) {
        validateItems(items);
        request.getItems().clear();
        request.getItems().addAll(items);
        
        try {
            store.saveImportRequest(request);
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật yêu cầu: " + e.getMessage());
            throw new ValidationException("Lỗi cập nhật dữ liệu: " + e.getMessage());
        }
    }

    public void delete(ImportRequest request) {
        try {
            store.deleteImportRequest(request.getRequestCode());
            store.getImportRequests().remove(request);
        } catch (SQLException e) {
            System.err.println("Lỗi xóa yêu cầu: " + e.getMessage());
            throw new ValidationException("Lỗi xóa dữ liệu: " + e.getMessage());
        }
    }

    public Optional<ImportRequest> findByCode(String requestCode) {
        return store.getImportRequests().stream()
                .filter(request -> request.getRequestCode().equals(requestCode))
                .findFirst();
    }

    private void validateItems(List<MerchandiseRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Yêu cầu phải có ít nhất một mặt hàng.");
        }
        for (MerchandiseRequest item : items) {
            if (item.getMerchandiseCode() == null || item.getMerchandiseCode().isBlank()) {
                throw new ValidationException("Mã hàng không được để trống.");
            }
            if (item.getMerchandiseName() == null || item.getMerchandiseName().isBlank()) {
                throw new ValidationException("Tên mặt hàng không được để trống.");
            }
            if (item.getQuantityOrdered() <= 0) {
                throw new ValidationException("Số lượng đặt phải lớn hơn 0.");
            }
            if (item.getUnit() == null || item.getUnit().isBlank()) {
                throw new ValidationException("Đơn vị không được để trống.");
            }
            if (item.getStockLevel() < 0) {
                throw new ValidationException("Mức tồn kho không được âm.");
            }
            if (item.getRequestDate() == null || item.getRequestDate().isBefore(LocalDate.now())) {
                throw new ValidationException("Ngày yêu cầu phải từ hôm nay trở đi.");
            }
            if (item.getDesiredDeliveryDate() == null || item.getDesiredDeliveryDate().isBefore(item.getRequestDate())) {
                throw new ValidationException("Ngày cần hàng phải sau ngày yêu cầu.");
            }
            if (item.getEstimatedPrice() < 0) {
                throw new ValidationException("Giá ước tính không được âm.");
            }
        }
    }
}

