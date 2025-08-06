/**
Interfaz genérica que define las operaciones básicas comunes para cualquier servicio de entidad que utilice identificadores de tipo String.
Esta interfaz proporciona métodos para guardar, buscar por ID, eliminar por ID y obtener todas las entidades.
*/

package com.arquitectura.services;

import java.util.List;

// @param <E> es un tipo genérico que representa la entidad.
public interface CommonServiceString<E> {

	/**
	 - save() Guarda la entidad especificada.
	 - @param pEntity: entidad que se va a guardar.
	 */
	public E save(E pEntity);

	/**
	 - Busca y devuelve la entidad con el ID especificado.
	 - @param pId: ID de la entidad que se desea buscar.
	 */
	public E findById(String pId);

	/**
	 - Elimina la entidad con el ID especificado.
	 - @param pId: El ID de la entidad que se desea eliminar.
	 */
	public void deleteById(String pId);

	//Devuelve una lista de todas las entidades
	public List<E> findAll();

	public List<E> findAllById(List<String> pIds);

}
