package com.arquitectura.imagen.service;

import com.arquitectura.aws.AWSS3Service;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.ImagenEvent;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.imagen.entity.ImagenRepository;
import com.arquitectura.services.CommonServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ImagenServiceImpl extends CommonServiceImpl<Imagen, ImagenRepository> implements ImagenService{

	private final Path root= Paths.get("uploads");
	
	@Value("${imagenes.topic}")
	private String imagenTopic;
	
	 @Autowired
	 private AWSS3Service awsService;
	 
	 @Autowired
	 private EventoService eventoService;
	 
	 @Autowired
	 private KafkaTemplate<String,Object> kafkaTemplate;

	@Transactional("transactionManager")
	@Override
	public Imagen crear(MultipartFile file, Long pIdEvento, int pTipo) throws Exception {

		String nombre = awsService.uploadFile(file);

        Evento evento = eventoService.findById(pIdEvento);

        Imagen imagen = new Imagen(
            nombre,pTipo,evento
        );

        Imagen imagenBd = this.save(imagen);

        // Publicar en kafka solo imagenes Principal, Banner y QR
        if(pTipo <= 3) {
            saveKafka(imagenBd);
        }
        return imagenBd;

    }

	@Transactional("transactionManager")
	@Override
	public Imagen saveKafka(Imagen imagen) {
		Imagen imagenSaved = this.save(imagen);

		ImagenEvent imagenEvent = new ImagenEvent(
				imagenSaved.getId(),
				imagenSaved.getNombre(),
				imagenSaved.getUrl(),
				imagenSaved.getTipo(),
				imagenSaved.getEvento().getId()
		);

		try {
			ProducerRecord<String, Object> record = new ProducerRecord<>(imagenTopic, "Imagen-" + imagenEvent.getId(), imagenEvent);
			record.headers().add("messageId", Uuid.randomUuid().toString().getBytes());
			kafkaTemplate.send(record).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		return imagenSaved;
	}

	@Override
	public List<Imagen> findByEventoId(Long pIdEvento) {
		return repository.findByEventoId(pIdEvento);
	}

	@Transactional("transactionManager")
	@Override
	public Imagen editar(Imagen pImagen, Long pId) throws Exception {
		Imagen imagen = repository.findById(pId)
				.orElseThrow(() -> new EntityNotFoundException("No se encontró ninguna imagen con el id proporcionado"));

		imagen.setTipo(pImagen.getTipo());

		//Actualizar nombre de imagen de acuerdo al tipo y nombre del evento
		imagen.setNombre(imagen.setNombreImagen(imagen.getTipo(), imagen.getEvento().getNombre()));
		Imagen imagenBd = new Imagen();

		//Si es una imagen de tipo 1, 2 o 3 publicar el evento en Kafka
		if(imagen.getTipo() <= 3) {
			imagenBd = saveKafka(imagen);
		}
		// si no simplemente guardar la imagen
		else {
			imagenBd = save(imagen);
		}

		return imagenBd;
	}

	@Transactional("transactionManager")
	@Override
	public void deleteById(Long pId) {
		Imagen imagen = repository.findById(pId)
				.orElseThrow(() -> new EntityNotFoundException("No se encontró ninguna imagen con el id proporcionado"));

		EntityDeleteEventLong imagenDelete = new EntityDeleteEventLong(imagen.getId());
		try {
			ProducerRecord<String, Object> record = new ProducerRecord<>(imagenTopic, "Imagen-" + imagen.getId(), imagenDelete);
			record.headers().add("messageId", Uuid.randomUuid().toString().getBytes());
			kafkaTemplate.send(record).get();

			//Eliminar el archivo de S3
			// Extraer la clave del S3 desde la URL
			String s3Key = imagen.getUrl().substring(imagen.getUrl().lastIndexOf("/") + 1);
			awsService.deleteFile(s3Key);

			//Eliminar la imagen de la base de datos
			repository.deleteById(imagen.getId());

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void init() {
		try {
			Files.createDirectory(root);
		} catch (IOException e) {
			throw new RuntimeException("No se puede inciar el storage");
		}
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(root.toFile());

	}
}
