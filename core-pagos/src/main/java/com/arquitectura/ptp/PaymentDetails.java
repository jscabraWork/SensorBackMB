package com.arquitectura.ptp;

import lombok.Data;

@Data
public class PaymentDetails {

    private String reference;
    private String description;
    private Amount amount;
    private boolean allowPartial;

    //Pago con suscripción. Un pago se puede convertir en pago y suscripción al mismo tiempo
    private boolean subscribe;
}
