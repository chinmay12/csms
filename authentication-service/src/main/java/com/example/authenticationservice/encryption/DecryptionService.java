package com.example.authenticationservice.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.example.authenticationservice.kafka.service.AuthEventsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class DecryptionService {

    private static final Logger logger = LoggerFactory.getLogger(DecryptionService.class);

    public String decrypt(String encryptedText) throws Exception {
        String secretKey = getPasswordFromConfig();
        return getDecryptedString(encryptedText, secretKey);
    }

    private String getDecryptedString(String encryptedText, String secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);


        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    private String getPasswordFromConfig() {
        return System.getenv("ENCRYPTION_PASSWORD");
    }
}