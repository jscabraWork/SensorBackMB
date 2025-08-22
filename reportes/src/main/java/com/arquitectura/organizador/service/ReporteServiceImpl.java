package com.arquitectura.organizador.service;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.entity.AlcanciaRepository;
import com.arquitectura.alcancia.service.AlcanciaService;
import com.arquitectura.ticket.entity.TicketRepository;
import com.arquitectura.views.detalle_evento.DetalleEventoView;
import com.arquitectura.views.detalle_evento.DetalleEventoViewRepository;
import com.arquitectura.views.historial_transacciones.HistorialDTO;
import com.arquitectura.views.historial_transacciones.HistorialRepository;
import com.arquitectura.views.historial_transacciones.HistorialView;
import com.arquitectura.views.resumen_evento.ResumenEventoView;
import com.arquitectura.views.resumen_evento.ResumenEventoViewRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService{

    @Autowired
    private ResumenEventoViewRepository vistaResumenEvento;

    @Autowired
    private DetalleEventoViewRepository detalleRepository;

    @Autowired
    private HistorialRepository historialRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AlcanciaService alcanciaService;


    @Override
    public ResumenEventoView getResumenByEventoId(Long id) {
        return vistaResumenEvento.findByEventoId(id).orElse(null);
    }

    @Override
    public List<DetalleEventoView> getDetalleEvento(Long eventoId, Long tarifaId, Long localidadId, Long diaId) {
        return detalleRepository.findDetalleFiltrado(eventoId, tarifaId, localidadId, diaId);
    }

    @Override
    public Page<HistorialDTO> getHistorialByEventoAndStatus(Long eventoId, Integer status, LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer tipo, int page, int size) {

        Page<HistorialView> historial = historialRepository.findByFiltrosOrderByFechaDesc(eventoId, status, tipo, fechaInicio, fechaFin, PageRequest.of(page, size));

        List<HistorialDTO> historialDTO = historial.map(venta -> {
            HistorialDTO dto = new HistorialDTO();
            dto.setVenta(venta);
            if (venta.getTipo() != 4) {
                dto.setTickets(ticketRepository.findByOrdenesId(dto.getVenta().getOrdenId()));
            }
            return dto;
        }).getContent();

        return new PageImpl<>(historialDTO, historial.getPageable(), historial.getTotalElements());
    }


@Override
public byte[] generarExcelHistorialByEventoAndEstado(Long pEventoId, Integer status) {

    List<HistorialView> transacciones = historialRepository.findByEventoIdAndStatusOrderByFechaDesc(pEventoId, status);

    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Transacciones");

        // Crear estilos para el encabezado
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        String[] columns = {
                "No. de venta",
                "Fecha",
                "Tipo de venta",
                "Etapa",
                "Localidad",
                "Cantidad Tickets",
                "Método de pago",
                "Valor",
                "No documento",
                "Nombre",
                "Correo",
                "Celular",
                "Promotor"
        };

        // Crear encabezados con estilo
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Llenar datos
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (HistorialView transaccion : transacciones) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(transaccion.getOrdenId());
            row.createCell(1).setCellValue(transaccion.getFecha() != null ?
                transaccion.getFecha().format(formatter) : "");
            row.createCell(2).setCellValue(transaccion.getTipoNombre());
            row.createCell(3).setCellValue(transaccion.getTarifa());
            row.createCell(4).setCellValue(transaccion.getLocalidad());
            row.createCell(5).setCellValue(transaccion.getCantidad());
            row.createCell(6).setCellValue(transaccion.getMetodo());
            row.createCell(7).setCellValue(transaccion.getValorOrden());
            row.createCell(8).setCellValue(transaccion.getDocumento());
            row.createCell(9).setCellValue(transaccion.getNombre());
            row.createCell(10).setCellValue(transaccion.getCorreo());
            row.createCell(11).setCellValue(transaccion.getTelefono());
            row.createCell(12).setCellValue(transaccion.getPromotor());
        }
        // Ajustar ancho de columnas
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        // Convertir a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();

    } catch (IOException e) {
        throw new RuntimeException("Error al generar el archivo Excel: " + e.getMessage());
    }
}

    @Override
    public List<Alcancia> findAlcanciasByEventoIdAndEstado(Long eventoId, Integer estado) {

        List<Alcancia> alcancias = alcanciaService.findByEventoIdAndEstado(eventoId, estado);

        alcancias.forEach(Alcancia::setLocalidad);

        return alcancias;
    }


}