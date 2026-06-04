package com.itss.importorder.entity;

public enum PhuongThucGiaoHang {
    SHIP("ship delivery", "Tàu"),
    AIR("air delivery", "Hàng không");

    private final String code;
    private final String displayName;

    PhuongThucGiaoHang(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
