package com.itss.importorder.controller;

import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.VaiTro;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.util.ValidationException;
import java.sql.SQLException;
import java.util.List;

public class QuanTriTaiKhoanController {
    private final DataStore store;

    public QuanTriTaiKhoanController(DataStore store) {
        this.store = store;
    }

    public List<NguoiDung> findAll() {
        return store.getNguoiDungs();
    }

    public NguoiDung create(String username, String password, VaiTro vaiTro) {
        validate(username, password);
        try {
            if (store.findNguoiDungByUsername(username) != null) {
                throw new ValidationException("Tài khoản \"" + username + "\" đã tồn tại.");
            }
            NguoiDung nd = new NguoiDung(username, password, vaiTro);
            store.saveNguoiDung(nd);
            return nd;
        } catch (SQLException e) {
            throw new ValidationException("Lỗi lưu tài khoản: " + e.getMessage());
        }
    }

    public void update(String username, String password, VaiTro vaiTro) {
        validate(username, password);
        try {
            store.saveNguoiDung(new NguoiDung(username, password, vaiTro));
        } catch (SQLException e) {
            throw new ValidationException("Lỗi cập nhật tài khoản: " + e.getMessage());
        }
    }

    public void delete(String username, String currentUsername) {
        if (username.equals(currentUsername)) {
            throw new ValidationException("Không thể xóa tài khoản đang đăng nhập.");
        }
        try {
            store.deleteNguoiDung(username);
        } catch (SQLException e) {
            throw new ValidationException("Lỗi xóa tài khoản: " + e.getMessage());
        }
    }

    private void validate(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Tên tài khoản không được để trống.");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Mật khẩu không được để trống.");
        }
    }
}
