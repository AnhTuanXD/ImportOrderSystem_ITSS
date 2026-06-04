package com.itss.importorder.entity;

public class PhanBo {
    private final String siteCode;
    private final String merchandiseCode;
    private final int quantityOrdered;
    private final String unit;
    private final PhuongThucGiaoHang phuongThucGiaoHang;

    public PhanBo(String siteCode, String merchandiseCode, int quantityOrdered, String unit,
                  PhuongThucGiaoHang phuongThucGiaoHang) {
        this.siteCode = siteCode;
        this.merchandiseCode = merchandiseCode;
        this.quantityOrdered = quantityOrdered;
        this.unit = unit;
        this.phuongThucGiaoHang = phuongThucGiaoHang;
    }

    public String getSiteCode() { return siteCode; }
    public String getMerchandiseCode() { return merchandiseCode; }
    public int getQuantityOrdered() { return quantityOrdered; }
    public String getUnit() { return unit; }
    public PhuongThucGiaoHang getPhuongThucGiaoHang() { return phuongThucGiaoHang; }
}
