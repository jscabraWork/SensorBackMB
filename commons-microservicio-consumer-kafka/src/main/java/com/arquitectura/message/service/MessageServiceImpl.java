package com.arquitectura.message.service;

import com.arquitectura.message.entity.MessageEntity;
import com.arquitectura.message.entity.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageServiceImpl implements MessageService{

	@Autowired
	private MessageRepository messageRepository;
	
	@Override
	public boolean existeMessage(String pMessageId) {
		MessageEntity messageBD = messageRepository.findByMessageId(pMessageId);

        if (messageBD != null) {
            return true;
        }
		return false;
	}

	@Override
	public void crearMensaje(String pMessageId, String pEntityId) {
        MessageEntity messageCrear = new MessageEntity();
        messageCrear.setMessageId(pMessageId);
        messageCrear.setEntitiyId(pEntityId);
        messageRepository.save(messageCrear);
		
	}

}
