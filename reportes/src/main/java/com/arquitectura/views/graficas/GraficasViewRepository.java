package com.arquitectura.views.graficas;

import com.arquitectura.views.graficas.dto.GraficaDonaDTO;
import com.arquitectura.views.graficas.dto.GraficaLineasDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para consultas de gráficas de reportes de eventos
 * Proporciona datos optimizados para visualizaciones de líneas y dona
 *
 * @author Isaac
 * @version 1.0
 */
@Repository
public interface GraficasViewRepository {

    /**
     * Obtiene datos para gráfica de líneas temporales con períodos completos
     *
     * Funcionalidad:
     * - Si mes = -1: Retorna todos los meses del año (1-12) aunque no tengan ventas
     * - Si mes != -1: Retorna todos los días del mes específico aunque no tengan ventas
     * - Períodos sin ventas aparecen con valores en 0
     * - Nombres de períodos en español (Enero, Lunes, etc.)
     *
     * @param eventoId ID del evento a consultar
     * @param mes Mes específico (1-12) o -1 para consultar todo el año
     * @param anio Año específico a consultar (formato YYYY)
     * @return Lista de GraficaLineasDTO con datos temporales completos
     *
     */
    @Query(value = """
        WITH periodos AS (
            SELECT 1 as numero UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
            UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12
            UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18
            UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
            UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30 UNION SELECT 31
        ), datos_vista AS (
            SELECT CASE WHEN :mes = -1 THEN mes ELSE dia END as periodo,
                   SUM(total_recaudado) as total_recaudado, SUM(total_tickets) as total_asistentes,
                   SUM(total_precio) as total_precio_recaudado, SUM(total_servicio) as total_servicio_recaudado,
                   SUM(total_iva) as total_iva_recaudado, SUM(total_recaudado_transacciones) as total_recaudado_transacciones
            FROM grafica_lineas WHERE evento_id = :eventoId AND anio = :anio AND (:mes = -1 OR mes = :mes)
            GROUP BY CASE WHEN :mes = -1 THEN mes ELSE dia END
        )
        SELECT p.numero as periodo,
               CASE WHEN :mes = -1 THEN 
                   CASE p.numero WHEN 1 THEN 'Enero' WHEN 2 THEN 'Febrero' WHEN 3 THEN 'Marzo' WHEN 4 THEN 'Abril'
                                WHEN 5 THEN 'Mayo' WHEN 6 THEN 'Junio' WHEN 7 THEN 'Julio' WHEN 8 THEN 'Agosto'
                                WHEN 9 THEN 'Septiembre' WHEN 10 THEN 'Octubre' WHEN 11 THEN 'Noviembre' WHEN 12 THEN 'Diciembre' END
               ELSE CASE DAYNAME(CONCAT(:anio, '-', LPAD(:mes, 2, '0'), '-', LPAD(p.numero, 2, '0')))
                        WHEN 'Monday' THEN 'Lunes' WHEN 'Tuesday' THEN 'Martes' WHEN 'Wednesday' THEN 'Miércoles'
                        WHEN 'Thursday' THEN 'Jueves' WHEN 'Friday' THEN 'Viernes' WHEN 'Saturday' THEN 'Sábado' WHEN 'Sunday' THEN 'Domingo' END
               END as nombrePeriodo,
               COALESCE(d.total_recaudado, 0) as totalRecaudado, COALESCE(d.total_asistentes, 0) as totalAsistentes,
               COALESCE(d.total_precio_recaudado, 0) as totalPrecioRecaudado, COALESCE(d.total_servicio_recaudado, 0) as totalServicioRecaudado,
               COALESCE(d.total_iva_recaudado, 0) as totalIvaRecaudado, COALESCE(d.total_recaudado_transacciones, 0) as totalRecaudadoTransacciones
        FROM periodos p LEFT JOIN datos_vista d ON p.numero = d.periodo
        WHERE (:mes = -1 AND p.numero <= 12) OR (:mes != -1 AND p.numero <= DAY(LAST_DAY(CONCAT(:anio, '-', LPAD(:mes, 2, '0'), '-01'))))
        ORDER BY p.numero
        """, nativeQuery = true)
    List<GraficaLineasDTO> getGraficaLineaVentas(@Param("eventoId") Long eventoId,
                                                 @Param("mes") Integer mes,
                                                 @Param("anio") Integer anio);

    /**
     * Obtiene datos para gráfica de dona de recaudación por método de pago
     *
     * Funcionalidad:
     * - Siempre retorna exactamente 3 métodos: punto_fisico, pse, tarjeta
     * - Métodos sin ventas aparecen con valores en 0
     * - Incluye dos tipos de recaudado: por tickets y por transacciones
     * - Si mes = -1: Agrupa todo el año, si mes != -1: Solo ese mes
     *
     * @param eventoId ID del evento a consultar
     * @param mes Mes específico (1-12) o -1 para consultar todo el año
     * @param anio Año específico a consultar (formato YYYY)
     * @return Lista de exactamente 3 GraficaDonaDTO (uno por método de pago)
     *
     */
    @Query(value = """
        WITH metodos_base AS (
            SELECT 'punto_fisico' as metodo UNION SELECT 'tarjeta' UNION SELECT 'pse'
        ), datos_reales AS (
            SELECT metodo_pago, SUM(total_recaudado) as total_recaudado, SUM(total_recaudado_transacciones) as total_recaudado_transacciones
            FROM grafica_dona WHERE evento_id = :eventoId AND anio = :anio AND (:mes = -1 OR mes = :mes)
            GROUP BY metodo_pago
        )
        SELECT mb.metodo as metodoPago, COALESCE(dr.total_recaudado, 0) as totalRecaudado,
               COALESCE(dr.total_recaudado_transacciones, 0) as totalRecaudadoTransacciones
        FROM metodos_base mb LEFT JOIN datos_reales dr ON mb.metodo = dr.metodo_pago
        ORDER BY mb.metodo
        """, nativeQuery = true)
    List<GraficaDonaDTO> graficaRecaudadoByMetodo(@Param("eventoId") Long eventoId,
                                                  @Param("mes") Integer mes,
                                                  @Param("anio") Integer anio);
}