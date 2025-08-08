package com.arquitectura.ptp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {

    private String status;

    private String reason;

    private String message;

    private String date;
}
