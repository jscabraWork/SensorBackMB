package com.arquitectura.mail.service;

import com.arquitectura.codigo_traspaso.entity.CodigoTraspaso;

import java.io.File;
import java.util.List;

public interface SendEmailAmazonService {

    public void mandarCorreo(String numero, String to, List<File> adjuntos) throws Exception;

    public void mandarCorreoCederTicket(CodigoTraspaso codigo) throws Exception;
}
