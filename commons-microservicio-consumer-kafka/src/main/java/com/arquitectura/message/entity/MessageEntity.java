package com.arquitectura.message.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="messages-proccessed")
public class MessageEntity {

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(nullable=false,unique=true)
	private String messageId;
	
	@Column(nullable=false)
	private String entitiyId;
}
