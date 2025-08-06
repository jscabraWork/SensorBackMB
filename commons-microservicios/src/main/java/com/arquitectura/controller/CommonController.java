package com.arquitectura.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.arquitectura.services.CommonService;

import jakarta.validation.Valid;

/**
 * Controlador genérico que proporciona operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * para una entidad genérica E. Utiliza un servicio genérico S que extiende CommonService<E>.
 * @param <E> El tipo de la entidad genérica
 * @param <S> El tipo del servicio genérico que extiende CommonService<E>
 */
public abstract class CommonController<E, S extends CommonService<E>> {

	/**
	 * Instancia del servicio genérico S
	 */
	@Autowired
	protected S service;

	/**
	 * Devuelve una lista de todas las entidades E
	 * @return ResponseEntity con el código de estado 200 (OK) y el cuerpo que contiene la lista de entidades
	 */
	@GetMapping
	public ResponseEntity<?> listar() {
		return ResponseEntity.ok().body(service.findAll());
	}

	/**
	 * Devuelve una entidad E por su ID
	 * @param pId El ID de la entidad
	 * @return ResponseEntity con el código de estado 200 (OK) y el cuerpo que contiene la entidad, o 404 (Not Found) si no se encuentra la entidad
	 */
	@GetMapping("/{pId}")
	public ResponseEntity<?> verPorId(@PathVariable Long pId) {
		E e = service.findById(pId);
		if (e == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(e);
	}

	/**
	 * Crea una nueva entidad E
	 * @param pE     La entidad a crear
	 * @param result Objeto BindingResult para manejar errores de validación
	 * @return ResponseEntity con el código de estado 201 (Created) y el cuerpo que contiene la entidad creada, o 400 (Bad Request) si hay errores de validación
	 */
	@PostMapping
	public ResponseEntity<?> crear(@Valid @RequestBody E pE, BindingResult result) {
		if (result.hasErrors()) {
			return validar(result);
		}
		E entityDB = service.save(pE);
		return ResponseEntity.status(HttpStatus.CREATED).body(entityDB);
	}

	/**
	 * Elimina una entidad E por su ID
	 * @param pId El ID de la entidad a eliminar
	 * @return ResponseEntity con el código de estado 204 (No Content)
	 */
	@DeleteMapping("/{pId}")
	public ResponseEntity<?> borrar(@PathVariable Long pId) {
		service.deleteById(pId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Genera un mapa de errores a partir de los errores de validación en el objeto BindingResult
	 * @param result Objeto BindingResult que contiene los errores de validación
	 * @return ResponseEntity con el código de estado 400 (Bad Request) y el cuerpo que contiene el mapa de errores
	 */
	protected ResponseEntity<?> validar(BindingResult result) {
		Map<String, Object> errores = new HashMap<>();
		result.getFieldErrors().forEach(err -> {
			errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
		});
		return ResponseEntity.badRequest().body(errores);
	}


}