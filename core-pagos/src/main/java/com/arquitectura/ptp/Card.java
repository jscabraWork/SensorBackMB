package com.arquitectura.ptp;

import lombok.Data;

import java.util.Date;

@Data
public class Card {
    private Double number;
    private Date expirationDate;
    private int cvv;
    private int installments;
}
