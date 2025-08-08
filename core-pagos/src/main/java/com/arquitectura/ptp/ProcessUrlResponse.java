package com.arquitectura.ptp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessUrlResponse {

    private String processUrl;
    private Long requestId;
    private Status status;
}
