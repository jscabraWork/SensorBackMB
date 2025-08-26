package com.arquitectura.views.resumen_organizador;


import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * DTO para manejar las estadísticas de un organizador
 * Basado en la consulta de resumen_evento_organizador
 */
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

    // Constructor personalizado que maneja conversión de tipos desde MySQL
    public ResumenOrganizadorDTO(
            Object organizador_numero_documento, Object organizador, Object total_eventos,
            Object dinero_total_recaudado, Object total_recaudado_transacciones, Object total_asistentes,
            Object promedio_dinero_recaudado, Object promedio_asistentes, Object maximo_recaudado_evento,
            Object minimo_recaudado_evento, Object maximo_asistentes_evento, Object minimo_asistentes_evento,
            Object total_transacciones_procesadas, Object total_compradores_unicos, Object promedio_compradores_por_evento,
            Object tickets_promedio_por_comprador, Object total_cortesias
    ) {
        // Campos String
        this.organizadorNumeroDocumento = organizador_numero_documento != null ? organizador_numero_documento.toString() : null;
        this.organizador = organizador != null ? organizador.toString() : null;
        
        // Campos Long (conteos)
        this.totalEventos = total_eventos instanceof Number ? ((Number) total_eventos).longValue() : 0L;
        this.totalAsistentes = total_asistentes instanceof Number ? ((Number) total_asistentes).longValue() : 0L;
        this.maximoAsistentesEvento = maximo_asistentes_evento instanceof Number ? ((Number) maximo_asistentes_evento).longValue() : 0L;
        this.minimoAsistentesEvento = minimo_asistentes_evento instanceof Number ? ((Number) minimo_asistentes_evento).longValue() : 0L;
        this.totalTransaccionesProcesadas = total_transacciones_procesadas instanceof Number ? ((Number) total_transacciones_procesadas).longValue() : 0L;
        this.totalCompradoresUnicos = total_compradores_unicos instanceof Number ? ((Number) total_compradores_unicos).longValue() : 0L;
        this.totalCortesias = total_cortesias instanceof Number ? ((Number) total_cortesias).longValue() : 0L;
        
        // Campos Double (valores monetarios y promedios)
        this.dineroTotalRecaudado = dinero_total_recaudado instanceof Number ? ((Number) dinero_total_recaudado).doubleValue() : 0.0;
        this.totalRecaudadoTransacciones = total_recaudado_transacciones instanceof Number ? ((Number) total_recaudado_transacciones).doubleValue() : 0.0;
        this.promedioDineroRecaudado = promedio_dinero_recaudado instanceof Number ? ((Number) promedio_dinero_recaudado).doubleValue() : 0.0;
        this.promedioAsistentes = promedio_asistentes instanceof Number ? ((Number) promedio_asistentes).doubleValue() : 0.0;
        this.maximoRecaudadoEvento = maximo_recaudado_evento instanceof Number ? ((Number) maximo_recaudado_evento).doubleValue() : 0.0;
        this.minimoRecaudadoEvento = minimo_recaudado_evento instanceof Number ? ((Number) minimo_recaudado_evento).doubleValue() : 0.0;
        this.promedioCompradoresPorEvento = promedio_compradores_por_evento instanceof Number ? ((Number) promedio_compradores_por_evento).doubleValue() : 0.0;
        this.ticketsPromedioPorComprador = tickets_promedio_por_comprador instanceof Number ? ((Number) tickets_promedio_por_comprador).doubleValue() : 0.0;
    }
}