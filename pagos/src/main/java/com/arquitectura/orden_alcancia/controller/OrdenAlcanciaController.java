package com.arquitectura.orden_alcancia.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.orden_alcancia.entity.OrdenAlcancia;
import com.arquitectura.orden_alcancia.service.OrdenAlcanciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ordenes_alcancias")
public class OrdenAlcanciaController extends CommonController<OrdenAlcancia, OrdenAlcanciaService> {

    @PostMapping("/crear-aporte")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenAporte(@RequestParam Long pAlcanciaId,
                                              @RequestParam Double pAporte) throws Exception {

        Map<String, Object> response = new HashMap<>();
        OrdenAlcancia orden = service.crearOrdenAporte(pAlcanciaId, pAporte);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
