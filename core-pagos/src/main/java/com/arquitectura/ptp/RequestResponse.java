package com.arquitectura.ptp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RequestResponse {

    private Long requestId;
    private Status status;
    private Request request;
    private List<PaymentResponse> payment;
    private Subscription subscription;
}
