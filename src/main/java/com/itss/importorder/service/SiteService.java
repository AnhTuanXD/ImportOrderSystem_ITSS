package com.itss.importorder.service;

import com.itss.importorder.model.ImportSite;
import com.itss.importorder.model.SiteStatus;
import com.itss.importorder.repository.DataStore;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SiteService {
    private final DataStore store;

    public SiteService(DataStore store) {
        this.store = store;
    }

    public List<ImportSite> findAll() {
        return store.getImportSites().stream()
                .sorted(Comparator.comparing(ImportSite::getSiteCode))
                .collect(Collectors.toList());
    }

    public List<ImportSite> findActiveSites() {
        return findAll().stream()
                .filter(site -> site.getStatus() == SiteStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    public ImportSite add(String code, String name, String password, int shipDays, int airDays, String otherInfo) {
        validate(code, name, password, shipDays, airDays);
        if (findByCode(code).isPresent()) {
            throw new ValidationException("Mã Site đã tồn tại.");
        }
        ImportSite site = new ImportSite(code, name, password, shipDays, airDays, otherInfo, SiteStatus.ACTIVE);
        
        try {
            store.saveImportSite(site);
            store.getImportSites().add(site);
        } catch (SQLException e) {
            System.err.println("Lỗi lưu Site: " + e.getMessage());
            throw new ValidationException("Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return site;
    }

    public void update(ImportSite site, String name, String password, int shipDays, int airDays, String otherInfo) {
        validate(site.getSiteCode(), name, password, shipDays, airDays);
        site.setName(name);
        site.setPassword(password);
        site.setDeliveryDaysByShip(shipDays);
        site.setDeliveryDaysByAir(airDays);
        site.setOtherInformation(otherInfo);
        
        try {
            store.saveImportSite(site);
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật Site: " + e.getMessage());
            throw new ValidationException("Lỗi cập nhật dữ liệu: " + e.getMessage());
        }
    }

    public void setStatus(ImportSite site, SiteStatus status) {
        site.setStatus(status);
        
        try {
            store.saveImportSite(site);
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật trạng thái Site: " + e.getMessage());
            throw new ValidationException("Lỗi cập nhật dữ liệu: " + e.getMessage());
        }
    }

    public Optional<ImportSite> findByCode(String code) {
        return store.getImportSites().stream()
                .filter(site -> site.getSiteCode().equalsIgnoreCase(code))
                .findFirst();
    }

    private void validate(String code, String name, String password, int shipDays, int airDays) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Mã Site không được để trống.");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Tên Site không được để trống.");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("Mật khẩu Site tối thiểu 6 ký tự.");
        }
        if (shipDays <= 0 || airDays <= 0) {
            throw new ValidationException("Số ngày vận chuyển phải lớn hơn 0.");
        }
    }
}

