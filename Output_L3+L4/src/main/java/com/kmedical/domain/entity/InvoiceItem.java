package com.kmedical.domain.entity;

import java.math.BigDecimal;

/** C25 — InvoiceItem «entity» (금액 USD BigDecimal) */
public class InvoiceItem {

    private String invoiceItemId;
    private String invoiceId;
    private String serviceDescription;
    private BigDecimal amountUSD;
    private Integer sortOrder;

    public InvoiceItem() {}

    public String getInvoiceItemId() { return invoiceItemId; }
    public void setInvoiceItemId(String invoiceItemId) { this.invoiceItemId = invoiceItemId; }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getServiceDescription() { return serviceDescription; }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; }

    public BigDecimal getAmountUSD() { return amountUSD; }
    public void setAmountUSD(BigDecimal amountUSD) { this.amountUSD = amountUSD; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
