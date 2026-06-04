package com.itss.importorder.entity;

public class TonKho {
    private final String siteCode;
    private final String merchandiseCode;
    private int inStockQuantity;
    private String unit;

    public TonKho(String siteCode, String merchandiseCode, int inStockQuantity, String unit) {
        this.siteCode = siteCode;
        this.merchandiseCode = merchandiseCode;
        this.inStockQuantity = inStockQuantity;
        this.unit = unit;
    }

    public String getSiteCode() { return siteCode; }
    public String getMerchandiseCode() { return merchandiseCode; }

    public int getInStockQuantity() { return inStockQuantity; }
    public void setInStockQuantity(int inStockQuantity) { this.inStockQuantity = inStockQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
