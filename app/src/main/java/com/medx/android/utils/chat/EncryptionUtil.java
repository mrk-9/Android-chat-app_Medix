package com.medx.android.utils.chat;

import com.medx.android.models.chat.MXUser;
import com.medx.android.models.chat.db.sqlite.MXUserDBHelper;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

/**
 * Created by alexey on 9/19/16.
 */

public class EncryptionUtil {

    protected static final String ALGORITHM = "RSA";
    protected static final String ENCODED_ALGIRITHM = "RSA/ECB/PKCS1Padding";
    protected static final String AES_ALGIRITHM = "AES/CBC/PKCS5PAdding";

    /**
     * Key Generation
     */

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyGen.initialize(4096);
        KeyPair key = keyGen.generateKeyPair();
        MXUserUtil.saveKeyPair(key);
        return key;
    }

    public static SecretKey generateSharedAESKey() throws NoSuchAlgorithmException  {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(256);  // or 192 or 256
        SecretKey skey = kgen.generateKey();
        return skey;
    }

    /**
     * Basic Encryption/Decryption
     */

    public static byte[] encrypt(byte[] text, PublicKey key) throws Exception {
        byte[] cipherText = null;
        //
        // get an RSA cipher object and print the provider
        Cipher cipher = Cipher.getInstance(ENCODED_ALGIRITHM);

        // encrypt the plaintext using the public key
        cipher.init(Cipher.ENCRYPT_MODE, key);
        cipherText = cipher.doFinal(text);
        return cipherText;
    }

    public static byte[] decrypt(byte[] text, PrivateKey key) throws Exception
    {
        byte[] dectyptedText = null;
        // decrypt the text using the private key
        Cipher cipher = Cipher.getInstance(ENCODED_ALGIRITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        dectyptedText = cipher.doFinal(text);
        return dectyptedText;

    }

    /**
     * Text Encryption
     */

    public static String encryptText(String text, PublicKey publicKey)  {
        String encryptedText;
        byte[] cipherText = new byte[0];
        try {
            cipherText = encrypt(text.getBytes("UTF-8"), publicKey);
            encryptedText = encodeBASE64(cipherText);

            return encryptedText;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String encodeBASE64(byte[] bytes)
    {
        BASE64Encoder b64 = new BASE64Encoder();
        //return b64.encode(bytes, false);
        return b64.encode(bytes);
        //return Base64.encodeBase64String(bytes);
    }

    /**
     * Text Decryption
     */

    public static String decrypt(String text, PrivateKey key) throws Exception
    {
        String result;
        // decrypt the text using the private key
        byte[] dectyptedText = decrypt(decodeBASE64(text), key);
        result = new String(dectyptedText, "UTF-8");
        return result;

    }

    private static byte[] decodeBASE64(String text) throws IOException
    {
        BASE64Decoder b64 = new BASE64Decoder();
        return b64.decodeBuffer(text);
        //return Base64.decodeBase64(text);
    }

    /**
     * Photo Encryption
     */

    public static void encryptAttachmentWithKey(String sourcePath, String targetPath, String sharedKeyString)  throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        SecretKeySpec aesKeySpec = new SecretKeySpec(AppUtils.convertStringToSecretKey(sharedKeyString).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance(AES_ALGIRITHM);
        byte iv[] = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        FileInputStream is = new FileInputStream(new File(sourcePath));
        FileOutputStream fos = new FileOutputStream(targetPath);
        CipherOutputStream os = new CipherOutputStream(fos, cipher);

        AppUtils.copy(is, os);

        os.close();
    }

    public static void encryptAttachmentWithMessage(String sourcePath, String targetPath, JSONObject message_info) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException   {
        MXUser recipient = MXUserDBHelper.getInstance().getUser(AppUtils.getStringFromJSON(message_info, "recipient_id"));
        if (AppUtils.getStringFromJSON(message_info, "is_encrypted").equals("1") && AppUtils.isNotEmptyString(recipient.public_key))    {
            // Encrypts the attachment data by shared AES key
            encryptAttachmentWithKey(sourcePath, targetPath, AppUtils.getStringFromJSON(message_info, "text"));
        }
    }

    /**
     * Photo Decryption
     */

    public static void decryptLocalAttachment(File in, File out, String sharedKeyString) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException   {
        if (AppUtils.isEmptyobject(sharedKeyString))
            return;

        MedXUser currentUser = MedXUser.CurrentUser();
        if (currentUser.privateKey == null) return;

        // Decrypts encrypted attachment data by shared key
        SecretKey sharedKey = AppUtils.convertStringToSecretKey(sharedKeyString);
        Cipher cipher = Cipher.getInstance(AES_ALGIRITHM);
        SecretKeySpec aesKeySpec = new SecretKeySpec(sharedKey.getEncoded(), "AES");
        byte iv[] = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        try {
            cipher.init(Cipher.DECRYPT_MODE, aesKeySpec, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        CipherInputStream is = new CipherInputStream(new FileInputStream(in), cipher);
        FileOutputStream os = new FileOutputStream(out);

        AppUtils.copy(is, os);

        is.close();
        os.close();
    }
}
