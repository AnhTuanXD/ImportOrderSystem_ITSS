package com.itss.importorder.model;

public enum DeliveryMeans {
    SHIP("ship delivery", "Tàu"),
    AIR("air delivery", "Hàng không");

    private final String code;
    private final String displayName;

    DeliveryMeans(String code, String displayName) {
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

