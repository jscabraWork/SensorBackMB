/**
Esta clase proporciona una implementación genérica de la interfaz CommonService.
Utiliza un repositorio JPA para realizar operaciones CRUD básicas en entidades.
*/

package com.arquitectura.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

public class CommonServiceImpl <E, R extends JpaRepository<E,Long>> implements CommonService<E>{

	// Repositorio JPA utilizado para acceder a los datos de la entidad.
	@Autowired
	protected R repository;

	//Guarda la entidad especificada.
	@Override
	public E save(E pEntity) {
		return repository.save(pEntity);
	}

	//Busca y devuelve la entidad con el ID especificado.
	@Override
	public E findById(Long pId) {
		return repository.findById(pId).orElse(null);
	}

	//Elimina la entidad con el ID especificado.
	@Override
	public void deleteById(Long pId) {
		repository.deleteById(pId);		
	}

	//Devuelve una lista de todas las entidades.
	@Override
	public List<E> findAll() {
		return repository.findAll();
	}

	@Override
	public List<E> findAllById(List<Long> pIds) {
		return repository.findAllById(pIds);
	}

	@Override
	public List<E> saveAll(List<E> pEntity) {
		return repository.saveAll(pEntity);
	}

}
