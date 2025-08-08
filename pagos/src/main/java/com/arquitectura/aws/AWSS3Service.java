package com.arquitectura.aws;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface AWSS3Service {

	String uploadFile(MultipartFile file);

	public void deleteFile(String key);
	
	List<String> getObjectsFromS3();
	
	InputStream downloadFile(String key);
}
