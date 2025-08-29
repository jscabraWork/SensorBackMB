package com.arquitectura.intento_registro.service;

import com.arquitectura.intento_registro.entity.IntentoRegistro;
import com.arquitectura.intento_registro.entity.IntentoRegistroRepository;
import com.arquitectura.mail.SendEmailAmazonService;
import org.apache.kafka.common.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class IntentoRegistroServiceImpl implements IntentoRegistroService{

    @Autowired
    private IntentoRegistroRepository repository;

    @Autowired
    private SendEmailAmazonService emailAmazonService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public IntentoRegistro crearIntentoRegistro(IntentoRegistro pIntentoRegistro) throws Exception {
        pIntentoRegistro.setContrasena(passwordEncoder.encode(pIntentoRegistro.getContrasena()));
        pIntentoRegistro.setIdBusqueda(Uuid.randomUuid().toString());
        IntentoRegistro intentoRegistroBd = repository.save(pIntentoRegistro);
        emailAmazonService.mandarCorreoUsuario(intentoRegistroBd.getCorreo(), intentoRegistroBd.getIdBusqueda(), intentoRegistroBd.getCorreo());
        return intentoRegistroBd;
    }

    @Override
    public IntentoRegistro getIntentoRegistro(String pIdIntentoRegistro) {
        return repository.findByIdBusqueda(pIdIntentoRegistro);
    }

    @Override
    public IntentoRegistro save(IntentoRegistro intentoRegistro) {
        return repository.save(intentoRegistro);
    }
}
