package com.salesmanager.core.model.payments;

public class Przelewy24Payment extends Payment {
    private String sessionId;
    private String token;
    private String orderId;

    public Przelewy24Payment() {
        super.setPaymentType(PaymentType.PRZELEWY24);
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}
