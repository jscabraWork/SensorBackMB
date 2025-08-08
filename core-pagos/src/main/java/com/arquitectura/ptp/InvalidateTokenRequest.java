package com.arquitectura.ptp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvalidateTokenRequest {

    private String locale;

    private AuthEntity auth;

    private Instrument instrument;
}
