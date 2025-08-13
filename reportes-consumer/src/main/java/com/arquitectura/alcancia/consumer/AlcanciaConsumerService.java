package com.arquitectura.alcancia.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.AlcanciaEvent;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;

public interface AlcanciaConsumerService extends CommonsConsumer<BaseEvent, AlcanciaEvent, EntityDeleteEventLong>{

	
}
