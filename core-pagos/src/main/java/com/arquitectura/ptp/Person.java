package com.arquitectura.ptp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    private String document;
    private String documentType;
    private String name;
    private String email;
    private String mobile;
}
