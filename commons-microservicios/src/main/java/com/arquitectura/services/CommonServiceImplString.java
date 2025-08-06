/**
Esta clase proporciona una implementación genérica de la interfaz CommonServiceString.
Utiliza un repositorio JPA para realizar operaciones CRUD básicas en entidades con identificadores de tipo String.
*/

package com.arquitectura.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

public class CommonServiceImplString <E, R extends JpaRepository<E,String>> implements CommonServiceString<E>{

	// Repositorio JPA utilizado para acceder a los datos de la entidad.
	@Autowired
	protected R repository;
	
	@Override
	public E save(E pEntity) {
		return repository.save(pEntity);
	}

	@Override
	public E findById(String pId) {
		return repository.findById(pId).orElse(null);
	}

	@Override
	public void deleteById(String pId) {
		repository.deleteById(pId);
		
	}

	@Override
	public List<E> findAll() {
		return repository.findAll();
	}

	@Override
	public List<E> findAllById(List<String> pIds) {
		return repository.findAllById(pIds);
	}

}
