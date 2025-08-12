package com.arquitectura.imagen.service;

import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.imagen.entity.ImagenRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class ImagenServiceImpl extends CommonServiceImpl<Imagen, ImagenRepository> implements ImagenService{


}
