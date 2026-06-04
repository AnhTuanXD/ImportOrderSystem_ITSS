package com.itss.importorder.entity;

public class NguoiDung {
    private final String username;
    private final String password;
    private final VaiTro vaiTro;

    public NguoiDung(String username, String password, VaiTro vaiTro) {
        this.username = username;
        this.password = password;
        this.vaiTro = vaiTro;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean matchesPassword(String password) {
        return this.password.equals(password);
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }
}
