package com.arquitectura.ptp;

import lombok.Data;

@Data
public class Token {

    private String token;
    private String subtoken;
    private String franchise;
    private String franchiseName;
    private String lastDigits;
    private String validUntil;
    private String installments;
}
