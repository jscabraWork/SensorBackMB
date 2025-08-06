package com.arquitectura.message.service;

public interface MessageService {

	public boolean existeMessage(String pMessageId); 
	
	public void crearMensaje(String pMssageId, String pEntityId);
}
