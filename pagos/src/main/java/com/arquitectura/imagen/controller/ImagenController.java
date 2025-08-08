package com.arquitectura.imagen.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.imagen.service.ImagenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/imagenes")
public class ImagenController extends CommonController<Imagen, ImagenService> {


	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/evento/{pIdEvento}")
	public ResponseEntity<?> crear(@RequestParam("files") List<MultipartFile> files, @PathVariable Long pIdEvento, @RequestParam("tipos") List<Integer> tipos) {
		try {
			if (files.size() != tipos.size()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La cantidad de archivos y tipos no coincide");
			}

			for (int i = 0; i < files.size(); i++) {
				service.crear(files.get(i), pIdEvento, tipos.get(i));
			}

			Map<String, Object> response = new HashMap<>();

			response.put("message", "Se subieron los archivos correctamente");

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
		}
	}


	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/evento/{pIdEvento}")
	public ResponseEntity<?> getByEventoId(@PathVariable Long pIdEvento) {
		Map<String, Object> response = new HashMap<>();
		List<Imagen> imagenes = service.findByEventoId(pIdEvento);
		response.put("lista", imagenes);
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}


	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/editar/{pId}")
	public ResponseEntity<?> editar(@RequestBody Imagen pImagen, @PathVariable Long pId) {

		try {
			Imagen imagen = service.editar(pImagen, pId);
			return ResponseEntity.status(HttpStatus.OK).body(imagen);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
		}
	}



}
