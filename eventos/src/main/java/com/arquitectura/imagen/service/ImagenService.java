package com.arquitectura.imagen.service;

import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.services.CommonService;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface ImagenService extends CommonService<Imagen> {

	public Imagen crear(MultipartFile file, Long pIdEvento, int pTipo)throws Exception;

	public Imagen saveKafka(Imagen imagen);

	public List<Imagen> findByEventoId(Long pIdEvento);

	public Imagen editar(Imagen pImagen, Long pId) throws Exception;
}
