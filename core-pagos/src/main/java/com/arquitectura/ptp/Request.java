package com.arquitectura.ptp;

import lombok.Data;

@Data
public class Request {

    private String locale;
    private Payer payer;
    private PaymentDetails payment;
    private String returnUrl;
    private String ipAddress;
    private String userAgent;
    private String expiration;
}
