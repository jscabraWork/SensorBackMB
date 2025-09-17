package com.arquitectura.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.arquitectura.usuario.entity.Usuario;

@FeignClient(name="microservicio-usuarios")
public interface UsuarioFeignClient {

	@GetMapping("/correo/{pCorreo}")
	public Usuario getUsuarioPorCorreo(@PathVariable("pCorreo") String pCorreo);

    @GetMapping("/usuario-provider/provider/{providerId}/{tipoProvider}")
    public Usuario getUsuarioByProviderId(@PathVariable("providerId") String providerId, @PathVariable("tipoProvider") int tipoProvider);

}
