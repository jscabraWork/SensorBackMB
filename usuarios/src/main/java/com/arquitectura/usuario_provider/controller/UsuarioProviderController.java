package com.arquitectura.usuario_provider.controller;

import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario_provider.service.UsuarioProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/usuario-provider")
public class UsuarioProviderController {

    @Autowired
    private UsuarioProviderService usuarioProviderService;

    @GetMapping("/provider/{providerId}/{tipoProvider}")
    public Usuario getUsuarioByProviderId(@PathVariable String providerId, @PathVariable int tipoProvider) {
        return usuarioProviderService.getUsuarioByProviderId(providerId, tipoProvider);
    }

    @PostMapping("/registro-provider")
    public ResponseEntity<?> registroConProvider(@RequestBody Usuario pCliente,
                                                 @RequestParam int tipoProvider,
                                                 @RequestParam String providerId,
                                                 @RequestParam String accessToken,
                                                 @RequestParam(required = false) String refreshToken) {
        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = usuarioProviderService.crearClienteConProvider(pCliente, tipoProvider, providerId, accessToken, refreshToken);
            response.put("mensaje", "Usuario registrado exitosamente");
            response.put("usuario", usuario);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalStateException e) {
            response.put("mensaje", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } catch (Exception e) {
            response.put("mensaje", "Error procesando registro");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/asociar-provider")
    public ResponseEntity<?> asociarProviderAUsuarioExistente(@RequestParam String correo,
                                                              @RequestParam String providerId,
                                                              @RequestParam String accessToken,
                                                              @RequestParam int tipoProvider) {
        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuarioAsociado = usuarioProviderService.asociarProviderAUsuarioExistente(correo, tipoProvider, providerId, accessToken);
            response.put("mensaje", "Tu cuenta se ha asociado exitosamente");
            response.put("usuario", usuarioAsociado);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            response.put("mensaje", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.put("mensaje", "Error procesando asociación");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
