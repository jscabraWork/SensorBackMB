package com.arquitectura.rol.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.rol.entity.Role;
import com.arquitectura.rol.service.RoleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
public class RoleController extends CommonController<Role, RoleService> {
}
