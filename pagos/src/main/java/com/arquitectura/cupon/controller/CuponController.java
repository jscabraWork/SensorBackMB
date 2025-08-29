package com.arquitectura.cupon.controller;
import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.cupon.entity.Cupon;
import com.arquitectura.cupon.service.CuponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cupones")
public class CuponController extends CommonControllerString<Cupon, CuponService> {

    @GetMapping("/tarifa/{pTarifaId}")
    @Transactional("transactionManager")
    public ResponseEntity<?> findByTarifaId(@PathVariable Long pTarifaId) throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Cupon> cupones = service.findByTarifaId(pTarifaId);
        response.put("cupones", cupones);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/editar")
    @Transactional("transactionManager")
    public ResponseEntity<?> actualizar(@RequestParam String pCuponId, @RequestBody Cupon cupon) throws Exception {
        Map<String, Object> response = new HashMap<>();
        Cupon cuponBD = service.actualizar(pCuponId, cupon);
        response.put("cupones", cuponBD);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @PostMapping("")
    public ResponseEntity<?> crear(@Valid @RequestBody Cupon pE, BindingResult result) {
        if (result.hasErrors()) {
            return validar(result);
        }
        Cupon entityDB = service.crear(pE);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityDB);
    }

}
