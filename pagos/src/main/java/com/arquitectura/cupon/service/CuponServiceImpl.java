package com.arquitectura.cupon.service;

import com.arquitectura.cupon.entity.Cupon;
import com.arquitectura.cupon.entity.CuponRepository;
import com.arquitectura.services.CommonServiceImplString;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.ticket.service.TicketService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CuponServiceImpl extends CommonServiceImplString<Cupon, CuponRepository> implements CuponService {


    @Autowired
    private TicketService ticketService;

    public Cupon validarCupon(String codigo, Tarifa tarifa, Integer cantidadTickets){
        Cupon cupon = repository.findByCodigoAndTarifaLocalidadIdAndTarifaEstadoAndEstado(
            codigo, tarifa.getLocalidad().getId(), 3, 1);

        if (cupon == null) {
            return null;
        }

        Integer ventasActuales = ticketService.validarVentasCupon(tarifa.getId());
        
        return cupon.isValido(cantidadTickets, ventasActuales) ? cupon : null;
    }


    @Override
    public List<Cupon> findByTarifaId(Long tarifaId) throws Exception {

        return repository.findByTarifaId(tarifaId);

    }

    @Override
    public Cupon actualizar(String pCuponId, Cupon pCupon) throws Exception {
        Cupon cupon = this.findById(pCuponId);

        if(cupon==null){throw new Exception("El cupón no existe");}

        cupon.setEstado(pCupon.getEstado());
        cupon.setCodigo(pCupon.getCodigo());
        cupon.setVigencia(pCupon.getVigencia());
        cupon.setCantidadMinima(pCupon.getCantidadMinima());
        cupon.setCantidadMaxima(pCupon.getCantidadMaxima());
        cupon.setVentaMaxima(pCupon.getVentaMaxima());
        return this.save(cupon);
    }

    @Override
    public Cupon crear(Cupon cupon) {
        // Validar que el cupón no exista con el mismo código y tarifa
        if (repository.existsByCodigoAndTarifaId(cupon.getCodigo(), cupon.getTarifa().getId())) {
            throw new IllegalArgumentException("Ya existe un cupón con el mismo código para esta tarifa.");
        }

        return save(cupon);
    }
}
