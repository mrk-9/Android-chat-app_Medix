package com.medx.android.utils.crypto5;


import android.util.Base64;
import android.util.Log;

import com.medx.android.utils.app.AppUtils;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESImage {

    public static byte[] decrypt(byte[] data, PrivateKey key) {
        byte[] keyData = key.getEncoded();
        byte[] pass = new byte[128];
        new Random().nextBytes(pass);
        try {
            return AES256Cipher.decrypt(data, key);
        } catch (UnsupportedEncodingException e) {
            System.out.println("AESImage.decrypt e = " + e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("AESImage.decrypt e = " + e);
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            System.out.println("AESImage.decrypt e = " + e);
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            System.out.println("AESImage.decrypt e = " + e);
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("AESImage.decrypt e = " + e);
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            System.out.println("AESImage.decrypt e = " + e);
            e.printStackTrace();
        } catch (BadPaddingException e) {
            System.out.println("AESImage.decrypt e = " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("AESImage.decrypt e = " + e);
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt1(byte[] data, String password, PrivateKey key) {
        //byte[] pass = new byte[32];
        //new Random().nextBytes(pass);
        try {
            IvParameterSpec iv = new IvParameterSpec(Base64.decode(password,Base64.DEFAULT));

            SecretKeySpec skeySpec = new SecretKeySpec(key.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(data);

            System.out.println("AESImage.decrypt1 = " + original);
            return original;
        } catch (Exception ex) {
            System.out.println("AESImage.decrypt1 ex = " + ex);
            ex.printStackTrace();
        }

        return null;
    }

    public static void decrypt2(String encryptedPath, String decryptedPath, String password) throws Exception {
        //byte[] b = encrypted;

        byte[] keyStart = Base64.decode(password, Base64.DEFAULT);
        String strKeyStart = new String(keyStart, "UTF-8"); // for UTF-8 encoding
        String[] pairs = strKeyStart.split(",");
        String encodedKey = null;
        String iv = null;
        for (int i = 0; i < pairs.length; i++){
            if (pairs[i].contains("key=")){
                encodedKey = pairs[i].substring(4);
                Log.d("AES", encodedKey);
            }
            if (pairs[i].contains("iv=")){
                iv = pairs[i].substring(3);
            }
        }

        byte[] hexKey = hexStringToByteArray(encodedKey);
        byte[] hexIv = hexStringToByteArray(iv);

        int passwordLength = password.length();
        int keyLength = keyStart.length;

        byte[] encrypted = AppUtils.readFileByByte(encryptedPath);

        byte[] decryptedData = AES256Cipher.decrypt(hexIv, hexKey, encrypted);

        FileUtils.writeByteArrayToFile(new File(decryptedPath), decryptedData);


        decryptedData = null;

      //  return decryptedData;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    private static byte[] decryptT(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    private static byte[] buildKey(byte[] password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digester = MessageDigest.getInstance("SHA-256", new BouncyCastleProvider());
        digester.update(password);
        byte[] key = digester.digest();
        return key;
    }
}
