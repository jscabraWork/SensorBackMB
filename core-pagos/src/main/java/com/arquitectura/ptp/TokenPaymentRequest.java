package com.arquitectura.ptp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenPaymentRequest {

    private String locale;

    private AuthEntity auth;

    private Payer payer;

    private Payment payment;

    private Instrument instrument;

    private String ipAddress;

    private String userAgent;

    private String returnUrl;

    private String expiration;

}
