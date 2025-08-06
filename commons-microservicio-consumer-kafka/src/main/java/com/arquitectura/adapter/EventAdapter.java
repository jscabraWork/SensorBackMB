package com.arquitectura.adapter;



public interface EventAdapter<E,EV> {
	public E creacion(E entity, EV event);
}
