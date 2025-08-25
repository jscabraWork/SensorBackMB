package com.arquitectura.views.resumen_organizador;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * DTO para manejar las estadísticas de un organizador
 * Basado en la consulta de resumen_evento_organizador
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ResumenOrganizadorDTO {
    private String organizadorNumeroDocumento;
    private String organizador;
    private Long totalEventos;
    private Double dineroTotalRecaudado;
    private Double totalRecaudadoTransacciones;
    private Long totalAsistentes;
    private Double promedioDineroRecaudado;
    private Double promedioAsistentes;
    private Double maximoRecaudadoEvento;
    private Double minimoRecaudadoEvento;
    private Long maximoAsistentesEvento;
    private Long minimoAsistentesEvento;
    private Long totalTransaccionesProcesadas;
    private Long totalCompradoresUnicos;
    private Double promedioCompradoresPorEvento;
    private Double ticketsPromedioPorComprador;
    private Long totalCortesias;

    private static Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
    
    private static Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    public static ResumenOrganizadorDTO fromArray(Object[] result) {
        if (result == null || result.length < 17) {
            return new ResumenOrganizadorDTO();
        }

        return new ResumenOrganizadorDTO(
                (String) result[0],                    // organizador_numero_documento
                (String) result[1],                    // organizador
                toLong(result[2]),                     // total_eventos
                toDouble(result[3]),                   // dinero_total_recaudado
                toDouble(result[4]),                   // total_recaudado_transacciones
                toLong(result[5]),                     // total_asistentes
                toDouble(result[6]),                   // promedio_dinero_recaudado
                toDouble(result[7]),                   // promedio_asistentes
                toDouble(result[8]),                   // maximo_recaudado_evento
                toDouble(result[9]),                   // minimo_recaudado_evento
                toLong(result[10]),                    // maximo_asistentes_evento
                toLong(result[11]),                    // minimo_asistentes_evento
                toLong(result[12]),                    // total_transacciones_procesadas
                toLong(result[13]),                    // total_compradores_unicos
                toDouble(result[14]),                  // promedio_compradores_por_evento
                toDouble(result[15]),                  // tickets_promedio_por_comprador
                toLong(result[16])                     // total_cortesias
        );
    }
}