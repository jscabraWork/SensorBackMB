package com.arquitectura.cliente.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.events.UsuarioEvent;

public interface UsuarioEventAdapter extends EventAdapter<Cliente, UsuarioEvent> {
}
