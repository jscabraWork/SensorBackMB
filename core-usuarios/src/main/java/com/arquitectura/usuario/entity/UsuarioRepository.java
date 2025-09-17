package com.arquitectura.usuario.entity;

import java.util.List;

import com.arquitectura.rol.entity.Role;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, String>{

	
	@Query(value="SELECT * FROM usuarios where numero_documento=?1 or correo=?2 or celular=?3",
			nativeQuery=true)
	public Usuario buscarPreRegistro(String pNumeroDocumento, String pCorreo, String pCelular);
	
	public Usuario findByCorreo(String pCorreo);
	
	public List<Usuario> findByRolesNombre(String pRoleNombre);

    Page<Usuario> findByRolesId(Long roleId, Pageable pageable);
    
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.correo = :correo")
    Usuario findByCorreoByRoles(@Param("correo") String correo);
    
    @Query(value="SELECT * FROM usuarios where celular =?3 or numero_documento=?1  or correo =?2",
    		nativeQuery=true)
    List<Usuario> validarCorreos(String pNumeroDoc, String pCorreo, String pCelular);

    @Query(value="SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM usuarios WHERE numero_documento = ?1 OR celular = ?2",
    		nativeQuery=true)
    boolean findByIdOCelular(String numeroDocumento, String celular);
}
