package com.itss.importorder.controller;

import com.itss.importorder.entity.DiaDiemNhap;
import com.itss.importorder.entity.TrangThaiDiaDiem;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.util.ValidationException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DiaDiemNhapController {
    private final DataStore store;

    public DiaDiemNhapController(DataStore store) {
        this.store = store;
    }

    public List<DiaDiemNhap> findAll() {
        return store.getDiaDiemNhaps().stream()
                .sorted(Comparator.comparing(DiaDiemNhap::getSiteCode))
                .collect(Collectors.toList());
    }

    public List<DiaDiemNhap> findActiveSites() {
        return findAll().stream()
                .filter(s -> s.getStatus() == TrangThaiDiaDiem.ACTIVE)
                .collect(Collectors.toList());
    }

    public DiaDiemNhap add(String code, String name, String taiKhoan, int shipDays, int airDays, String otherInfo) {
        validate(code, name, taiKhoan, shipDays, airDays);
        if (findByCode(code).isPresent()) {
            throw new ValidationException("Mã Site đã tồn tại.");
        }
        DiaDiemNhap diaDiem = new DiaDiemNhap(code, name, taiKhoan, shipDays, airDays, otherInfo, TrangThaiDiaDiem.ACTIVE);
        try {
            store.saveDiaDiemNhap(diaDiem);
        } catch (SQLException e) {
            System.err.println("Lỗi lưu Site: " + e.getMessage());
            throw new ValidationException("Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return diaDiem;
    }

    public void update(DiaDiemNhap diaDiem, String name, String taiKhoan, int shipDays, int airDays, String otherInfo) {
        validate(diaDiem.getSiteCode(), name, taiKhoan, shipDays, airDays);
        diaDiem.setName(name);
        diaDiem.setTaiKhoan(taiKhoan);
        diaDiem.setDeliveryDaysByShip(shipDays);
        diaDiem.setDeliveryDaysByAir(airDays);
        diaDiem.setOtherInformation(otherInfo);
        try {
            store.saveDiaDiemNhap(diaDiem);
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật Site: " + e.getMessage());
            throw new ValidationException("Lỗi cập nhật dữ liệu: " + e.getMessage());
        }
    }

    public void setTrangThai(DiaDiemNhap diaDiem, TrangThaiDiaDiem trangThai) {
        diaDiem.setStatus(trangThai);
        try {
            store.saveDiaDiemNhap(diaDiem);
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật trạng thái Site: " + e.getMessage());
            throw new ValidationException("Lỗi cập nhật dữ liệu: " + e.getMessage());
        }
    }

    public Optional<DiaDiemNhap> findByCode(String code) {
        return store.getDiaDiemNhaps().stream()
                .filter(s -> s.getSiteCode().equalsIgnoreCase(code))
                .findFirst();
    }

    public Optional<DiaDiemNhap> findByTaiKhoan(String taiKhoan) {
        return store.getDiaDiemNhaps().stream()
                .filter(s -> s.getTaiKhoan().equals(taiKhoan))
                .findFirst();
    }

    private void validate(String code, String name, String taiKhoan, int shipDays, int airDays) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Mã Site không được để trống.");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Tên Site không được để trống.");
        }
        if (taiKhoan == null || taiKhoan.isBlank()) {
            throw new ValidationException("Tài khoản Site không được để trống.");
        }
        if (shipDays <= 0 || airDays <= 0) {
            throw new ValidationException("Số ngày vận chuyển phải lớn hơn 0.");
        }
    }
}
