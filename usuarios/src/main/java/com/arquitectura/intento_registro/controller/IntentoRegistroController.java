package com.arquitectura.intento_registro.controller;

import com.arquitectura.codigo_validacion.entity.Codigo;
import com.arquitectura.intento_registro.entity.IntentoRegistro;
import com.arquitectura.intento_registro.service.IntentoRegistroService;
import com.arquitectura.tipo_documento.entity.TipoDocumento;
import com.arquitectura.tipo_documento.service.TipoDocumentoService;
import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class IntentoRegistroController {

    @Autowired
    private IntentoRegistroService service;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TipoDocumentoService tipoDocumentoService;

    @PostMapping("/registro")
    public ResponseEntity<?> crearCliente(@RequestBody IntentoRegistro pIntentoRegistro) throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Usuario> resultado= usuarioService.validarDatos(pIntentoRegistro.getNumeroDocumento(), pIntentoRegistro.getCorreo(),pIntentoRegistro.getCelular());
        if(resultado!=null && resultado.size()>0) {
            response.put("mensaje","Los datos provisionados ya se encuentran registrados");
        } else {
            IntentoRegistro pIntento = service.crearIntentoRegistro(pIntentoRegistro);
            response.put("mensaje","Revisa tu correo, debió llegar un correo "+pIntento.getCorreo()+" de confirmación para terminar el proceso de registro");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/crear-usuario/{pIdBusqueda}")
    public ResponseEntity<?> crearClienteConfirmado(@PathVariable String pIdBusqueda) throws Exception {
        Map<String, Object> response = new HashMap<>();
        IntentoRegistro intentoRegistro = service.getIntentoRegistro(pIdBusqueda);
        if(intentoRegistro!=null&& intentoRegistro.isActivo()) {
            TipoDocumento tipoDocumento = tipoDocumentoService.findByNombre(
                    intentoRegistro.getTipoDocumento()
            );

            Usuario usuario = new Usuario(intentoRegistro.getNumeroDocumento(),
                    intentoRegistro.getNombre(),
                    intentoRegistro.getContrasena(),
                    intentoRegistro.getCorreo(),
                    intentoRegistro.getCelular(),
                    true,
                    new ArrayList<>(),
                    tipoDocumento,
                    null);

            Usuario usuarioBd=usuarioService.crearCliente(usuario, true);
            intentoRegistro.setUsuario(usuarioBd);
            intentoRegistro.setActivo(false);
            service.save(intentoRegistro);
            response.put("mensaje", "Te has registrado con existo, ya puedes continuar con tus compras");
        } else {
            response.put("mensaje", "Registro no exitoso");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
