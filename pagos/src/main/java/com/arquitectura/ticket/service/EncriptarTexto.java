package com.arquitectura.ticket.service;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
public class EncriptarTexto {

    private static final String ALGORITHM = "AES";
    private static final String FIXED_KEY = "BJQ53dtJu67M"; // Your fixed key

    // Generate a SecretKey from a fixed key string
    public static SecretKey getKeyFromPassword(String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 16); // Use only first 128 bits (16 bytes)
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    // Encrypt a text using AES
    public String encrypt(String plainText) throws Exception {
        SecretKey secretKey = getKeyFromPassword(FIXED_KEY);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt an encrypted text using AES
    public String decrypt(String encryptedText) throws Exception {
        SecretKey secretKey = getKeyFromPassword(FIXED_KEY);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }
}
