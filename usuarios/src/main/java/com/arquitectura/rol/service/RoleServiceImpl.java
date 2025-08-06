package com.arquitectura.rol.service;

import com.arquitectura.rol.entity.Role;
import com.arquitectura.rol.entity.RoleRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends CommonServiceImpl<Role, RoleRepository> implements RoleService {
}
