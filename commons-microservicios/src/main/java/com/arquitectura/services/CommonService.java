/**
 Está es una interfaz genérica que define las operaciones básicas comunes para cualquier servicio de entidad.
 Esta interfaz proporciona métodos para guardar, buscar por ID, eliminar por ID y obtener todas las entidades.
 Se utiliza con identificadores de tipo Long como IDs
 */

package com.arquitectura.services;

import java.util.List;

//@param <E> es un tipo genérico que representa la entidad.
public interface CommonService<E> {

	/**
	 - save() Guarda la entidad especificada.
	 - @param pEntity: entidad que se va a guardar.
	 */
	public E save(E pEntity);

	/**
	 - Busca y devuelve la entidad con el ID especificado.
	 - @param pId: ID de la entidad que se desea buscar.
	 */
	public E findById(Long pId);

	/**
	 - Elimina la entidad con el ID especificado.
	 - @param pId: El ID de la entidad que se desea eliminar.
	 */
	public void deleteById(Long pId);

	//Devuelve una lista de todas las entidades
	public List<E> findAll();

	public List<E> findAllById(List<Long> pIds);


	public List<E> saveAll(List<E> pEntity);

}
