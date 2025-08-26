package com.arquitectura.administradores.service;

import com.arquitectura.views.localidades_acabar.LocalidadesPorAcabar;
import com.arquitectura.views.localidades_acabar.LocalidadesPorAcabarRepository;
import com.arquitectura.views.resumen_admin.ResumenAdminDTO;
import com.arquitectura.views.resumen_admin.ResumenAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService{

    @Autowired
    private ResumenAdminRepository adminRepository;

    @Autowired
    private LocalidadesPorAcabarRepository localidadesPorAcabarRepository;


    @Override
    public ResumenAdminDTO getResumenAdmin(Long eventoId, Integer anio, Integer mes, Integer dia) {
        return adminRepository.getResumenAdminDTO(eventoId, anio, mes, dia);
    }

    @Override
    public List<LocalidadesPorAcabar> getLocalidadesPorAcabar() {
        return localidadesPorAcabarRepository.findAll();
    }
}
