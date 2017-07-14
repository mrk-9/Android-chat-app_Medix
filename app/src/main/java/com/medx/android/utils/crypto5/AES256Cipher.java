package com.medx.android.utils.crypto5;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256Cipher {

    private void generateKey(){
        byte[] key = new byte[32];
        new Random().nextBytes(key);

        byte[] iv = new byte[16];
        new Random().nextBytes(iv);

    }

    public static byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    public static byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    public static byte[] decrypt(byte[] data, PrivateKey key) throws Exception {
        final javax.crypto.Cipher rsa = javax.crypto.Cipher.getInstance("RSA");
        rsa.init(javax.crypto.Cipher.ENCRYPT_MODE, key);

        SecureRandom random = new SecureRandom();

        final byte[] secretKey = new byte[32];
        random.nextBytes(secretKey);

        final javax.crypto.Cipher aes = javax.crypto.Cipher.getInstance("AES");
        SecretKeySpec k = new SecretKeySpec(secretKey, "AES");
        aes.init(javax.crypto.Cipher.ENCRYPT_MODE, k);

        final byte[] ciphedKey = rsa.doFinal(secretKey);
        final byte[] ciphedData = aes.doFinal(data);

        byte[] result = new byte[256 + ciphedData.length];

        System.arraycopy(ciphedKey, 0, result, 0, 256);
        System.arraycopy(ciphedData, 0, result, 256, ciphedData.length);

        return result;
    }
}