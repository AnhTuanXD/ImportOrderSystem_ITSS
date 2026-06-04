package com.itss.importorder.model;

public class ImportSite {
    private final String siteCode;
    private String name;
    private String password;
    private int deliveryDaysByShip;
    private int deliveryDaysByAir;
    private String otherInformation;
    private SiteStatus status;

    public ImportSite(String siteCode, String name, String password, int deliveryDaysByShip,
            int deliveryDaysByAir, String otherInformation, SiteStatus status) {
        this.siteCode = siteCode;
        this.name = name;
        this.password = password;
        this.deliveryDaysByShip = deliveryDaysByShip;
        this.deliveryDaysByAir = deliveryDaysByAir;
        this.otherInformation = otherInformation;
        this.status = status;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDeliveryDaysByShip() {
        return deliveryDaysByShip;
    }

    public void setDeliveryDaysByShip(int deliveryDaysByShip) {
        this.deliveryDaysByShip = deliveryDaysByShip;
    }

    public int getDeliveryDaysByAir() {
        return deliveryDaysByAir;
    }

    public void setDeliveryDaysByAir(int deliveryDaysByAir) {
        this.deliveryDaysByAir = deliveryDaysByAir;
    }

    public String getOtherInformation() {
        return otherInformation;
    }

    public void setOtherInformation(String otherInformation) {
        this.otherInformation = otherInformation;
    }

    public SiteStatus getStatus() {
        return status;
    }

    public void setStatus(SiteStatus status) {
        this.status = status;
    }
}

