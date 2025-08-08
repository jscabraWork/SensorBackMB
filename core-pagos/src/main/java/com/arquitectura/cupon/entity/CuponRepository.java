package com.arquitectura.cupon.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuponRepository extends JpaRepository<Cupon, String> {
    public Cupon findByCodigoAndTarifaLocalidadIdAndTarifaEstadoAndEstado(String codigo, Long pLocalidadId, Integer estadoTarifa, Integer estado);

    public List<Cupon> findByTarifaId(Long tarifaId);

    //Util para validar no crear codigos duplicados antes de persistir un cupón
    public boolean existsByCodigoAndTarifaId(String codigo, Long tarifaId);
}
