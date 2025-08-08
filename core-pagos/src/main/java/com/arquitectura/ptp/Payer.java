package com.arquitectura.ptp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payer {
    private String name;
    private String surname;
    private String email;
    private String documentType;
    private String document;
    private String mobile;
}
