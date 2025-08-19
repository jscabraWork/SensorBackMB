package com.arquitectura.views.graficas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraficaLineasDTO {

    private Integer periodo; //Puede ser meses o dias

    private String nombrePeriodo; // Nombre del periodo (ej. "Enero", "Miercoles")

    private Double recaudado;

    private Integer asistentes;

    private Double precioRecaudado;

    private Double servicioRecaudado;

    private Double ivaRecaudado;

    private Double recaudadoTrx;

    /**
     * Transforma una lista de Object[] de la consulta SQL a una lista de GraficaLineasDTO
     * @param data Lista de Object[] de la consulta SQL nativa
     * @return Lista de GraficaLineasDTO convertida
     */
    public static List<GraficaLineasDTO> fromObjectArray(List<Object[]> data) {
        return data.stream()
                .map(row -> new GraficaLineasDTO(
                    ((Number) row[0]).intValue(),     // periodo
                    (String) row[1],                  // nombrePeriodo
                    ((Number) row[2]).doubleValue(),  // recaudado
                    ((Number) row[3]).intValue(),     // asistentes
                    ((Number) row[4]).doubleValue(),  // precioRecaudado
                    ((Number) row[5]).doubleValue(),  // servicioRecaudado
                    ((Number) row[6]).doubleValue(),  // ivaRecaudado
                    ((Number) row[7]).doubleValue()   // recaudadoTrx
                ))
                .collect(Collectors.toList());
    }
}
