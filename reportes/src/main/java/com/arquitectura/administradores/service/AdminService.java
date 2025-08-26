package com.arquitectura.administradores.service;

import com.arquitectura.views.localidades_acabar.LocalidadesPorAcabar;
import com.arquitectura.views.resumen_admin.ResumenAdminDTO;

import java.util.List;

public interface AdminService {


    public ResumenAdminDTO getResumenAdmin(Long eventoId, Integer anio, Integer mes, Integer dia);

    public List<LocalidadesPorAcabar> getLocalidadesPorAcabar();

}
