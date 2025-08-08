package com.arquitectura.aws;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AWSS3ServicesImpl implements AWSS3Service {

	private static final Logger  LOGGER = LoggerFactory.getLogger(AWSS3ServicesImpl.class);
	
	@Autowired
	private AmazonS3 amazonS3;
	
	@Value("${aws.s3.bucket}")
	private String bucketName;
	
	@Override
	public String uploadFile(MultipartFile file) {

		File mainFile = new File (file.getOriginalFilename());

		try (FileOutputStream stream = new FileOutputStream(mainFile)) {
			stream.write(file.getBytes());
			
			String nombreFinal = mainFile.getName().replaceAll(" ","");

			String newFileName = System.currentTimeMillis() + "_" + nombreFinal;

			LOGGER.info("Subiendo archivo con el nombre... " + newFileName);

			PutObjectRequest request = new PutObjectRequest(bucketName, newFileName, mainFile);

			amazonS3.putObject(request);

			return newFileName;

		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return "";
		
	}
	@Override
	public List<String> getObjectsFromS3() {
		ListObjectsV2Result result = amazonS3.listObjectsV2(bucketName);
		List<S3ObjectSummary> objects = result.getObjectSummaries();
		List<String> list = objects.stream().map(item -> {
			return item.getKey();
		}).collect(Collectors.toList());
		return list;
	}

	@Override
	public InputStream downloadFile(String key) {
		S3Object object = amazonS3.getObject(bucketName, key);
		return object.getObjectContent();
	}

	@Override
	public void deleteFile(String key) {
		try {
			// Verificar si el archivo existe antes de eliminarlo
			if (amazonS3.doesObjectExist(bucketName, key)) {
				// Eliminar el archivo del bucket de S3
				amazonS3.deleteObject(bucketName, key);
			} else {
				LOGGER.warn("El archivo con clave " + key + " no existe en el bucket");
			}
		} catch (AmazonServiceException e) {
			LOGGER.error("Error al eliminar el archivo: " + e.getErrorMessage(), e);
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error al eliminar el archivo", e);
		}
	}

}
