package com.arquitectura.ptp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amount {

    private String currency;

    private Double total;

    //se pueden incluir los taxes en el amount segun la documentacion
    private List<Tax> taxes;

    public Amount (String currency, Double total){
        this.currency = currency;
        this.total = total;
    }

}
