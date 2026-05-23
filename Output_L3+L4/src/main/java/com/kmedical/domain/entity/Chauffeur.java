package com.kmedical.domain.entity;

/** C05 — Chauffeur «entity» extends Staff */
public class Chauffeur extends Staff {

    private String vehicleNumber;
    private String vehicleType;

    public Chauffeur() { super(); }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
}
