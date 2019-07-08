package ly.rqmana.huia.java.security;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

public class SecurityUtils {

    // TODO: add more algorithms (see: https://www.owasp.org/index.php/Using_the_Java_Cryptographic_Extensions)

    public static String encrypt(String plainText, String password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        return new String(encrypt(plainText.getBytes(StandardCharsets.UTF_8), password), StandardCharsets.UTF_8);
    }

    public static byte[] encrypt(byte[] plainData, String password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        SecretKey key = getSecretKey(password);
//        System.out.println("key = " + Arrays.toString(key.getEncoded()));

        /* Encrypt the message. */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

//        System.out.println("cipher.doFinal(plainData) = " + Arrays.toString(cipher.doFinal(plainData)));

        return cipher.doFinal(plainData);
    }


    public static String decrypt(String cipherText, String password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
        return new String(decrypt(cipherText.getBytes(StandardCharsets.UTF_8), password), StandardCharsets.UTF_8);
    }

    public static byte[] decrypt(byte[] cipherData, String password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
        SecretKey key = getSecretKey(password);
//        System.out.println("key = " + Arrays.toString(key.getEncoded()));

        /* Decrypt the message, given derived key and initialization vector. */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(cipherData);
    }

    private static SecretKey getSecretKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Random r = new SecureRandom();
        byte[] salt = new byte[8];
        r.nextBytes(salt);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);

        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

}
