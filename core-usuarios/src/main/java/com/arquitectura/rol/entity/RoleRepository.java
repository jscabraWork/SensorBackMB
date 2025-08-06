package com.arquitectura.rol.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,Long>{

	public Role findByNombre(String pName);
}
