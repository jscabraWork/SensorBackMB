package com.arquitectura.message.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity,Long>{

	public MessageEntity findByMessageId(String pMessageId);
}
