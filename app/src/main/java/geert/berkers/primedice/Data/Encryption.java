package geert.berkers.primedice.Data;

/**
 * Primedice Application Created by Geert on 2-3-2016.
 */
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static javax.crypto.Cipher.getInstance;


public class Encryption {

    private Cipher cipher;
    private SecretKeySpec secretKeySpec;

    public Encryption(String privateKey) throws Exception {
        byte[] bytesOfMessage = privateKey.getBytes("UTF-8");
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        byte[] digest = messageDigest.digest(bytesOfMessage);
        secretKeySpec = new SecretKeySpec(digest, "AES");

        cipher = getInstance("AES/ECB/PKCS5Padding");
    }

    // Return encrypted byte array
    public byte[] encrypt(byte[] plaintext) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(plaintext);
    }

    // Return decrypted byte array
    public byte[] decrypt(byte[] ciphertext) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(ciphertext);
    }
}