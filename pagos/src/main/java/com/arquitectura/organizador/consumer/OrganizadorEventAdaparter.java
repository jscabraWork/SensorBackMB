package com.arquitectura.organizador.consumer;


import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.organizador.entity.Organizador;

public interface OrganizadorEventAdaparter extends EventAdapter<Organizador, UsuarioEvent> {

}
