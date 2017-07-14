package com.medx.android.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.medx.android.utils.crypto5.AES256Cipher;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class ImageHelper {
    public static boolean copyImageToTempFolder(Context context, Uri sourceUri, String fileName) {
        boolean result = false; //copy result
        InputStream is = null;
        OutputStream out = null;
        String tempPath = FileUtils.getTempDirectoryPath() + File.separator + fileName;
        try {
            is = context.getContentResolver().openInputStream(sourceUri);
            out = new FileOutputStream(tempPath);
            byte[] buffer = new byte[1024];
            int length = 0;
            if (is != null){
                while((length = is.read(buffer)) > 0){
                    out.write(buffer, 0, length);
                }
            }
            result = true;

        } catch (IOException e) {
            e.printStackTrace();
        }  finally{
            //close input
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //close output
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static byte[] encodeImage(String mPathSource, String filenameDest){
        // generate key and iv
        byte[] key = new byte[32];
        new Random().nextBytes(key);

        byte[] iv = new byte[16];
        new Random().nextBytes(iv);

        byte[] byteArray = null;
        byte[] encryptedArray = null;
        byte[] byteCipher = null;

        try {
            byteArray = FileUtils.readFileToByteArray(new File(mPathSource));
            String strKey = bytesToHex(key);
            String strIv = bytesToHex(iv);
            String cipher = "key=" + strKey + "," + "iv=" + strIv;
            byteCipher = Base64.encode(cipher.getBytes(), Base64.NO_WRAP);
            encryptedArray = AES256Cipher.encrypt(iv, key, byteArray);

            // write encryptedArray to temp file
            FileUtils.writeByteArrayToFile(new File(filenameDest), encryptedArray);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return byteCipher;
    }


    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

}
