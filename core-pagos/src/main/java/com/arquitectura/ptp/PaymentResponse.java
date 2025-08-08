package com.arquitectura.ptp;

import lombok.Data;

@Data
public class PaymentResponse {
    private Status status;
    private String receipt;
    private boolean refunded;
    private String franchise;
    private String reference;
    private String issuerName;
    private String authorization;
    private String paymentMethod;
    private int internalReference;
    private String paymentMethodName;
}
