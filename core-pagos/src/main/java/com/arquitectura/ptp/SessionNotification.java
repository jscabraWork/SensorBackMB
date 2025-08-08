package com.arquitectura.ptp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionNotification {

    private Status status;

    private Long requestId;

    private String reference;

    private String signature;
}
