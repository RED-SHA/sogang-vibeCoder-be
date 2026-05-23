package com.kmedical.domain.entity;

/** C02 — Admin «entity» extends User */
public class Admin extends User {

    private String agencyId;
    private String userRoleId;
    private String displayName;

    public Admin() { super(); }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getUserRoleId() { return userRoleId; }
    public void setUserRoleId(String userRoleId) { this.userRoleId = userRoleId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
