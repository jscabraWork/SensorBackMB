package com.arquitectura.promotor.consumer;


import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.promotor.entity.Promotor;

public interface PromotorEventAdaparter extends EventAdapter<Promotor, UsuarioEvent> {

}
