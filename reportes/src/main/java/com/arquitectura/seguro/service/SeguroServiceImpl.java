package com.arquitectura.seguro.service;

import com.arquitectura.seguro.entity.Seguro;
import com.arquitectura.seguro.entity.SeguroRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SeguroServiceImpl extends CommonServiceImpl<Seguro, SeguroRepository> implements SeguroService {

}
