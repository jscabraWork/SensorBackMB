package com.arquitectura.consumer;


public interface CommonsConsumer<E, E1 extends E,E2 extends E> {

	public void handleEvent(E baseEvent,String messageId,String messageKey) ;
	
	public void handleCreateEvent(E1 event, String messageId, String messageKey);
	
	public void handleDeleteEvent(E2 eventDelete, String messageId, String messageKey);
}
