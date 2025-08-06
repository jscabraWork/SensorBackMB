package com.arquitectura.adapter;

public class EventAdapterImpl<E,EV> implements EventAdapter<E, EV>{

	@Override
	public E creacion(E entity, EV event) {

		return entity;
	}

}
