package com.medx.android.utils.chat;

import android.util.Log;

import com.medx.android.App;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.chat.db.sqlite.MXUserDBHelper;
import com.medx.android.utils.app.AppUtils;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by alexey on 9/12/16.
 */

public class MXUserUtil {

    static String PRIVATE_KEY_FILE;
    static String PUBLIC_KEY_FILE ;

    /**
     * CRUD Methods
     */

    public static MXUser createUser(JSONObject info)    {
        boolean isNew = false;
        MXUser user = MXUserDBHelper.getInstance().getUser(AppUtils.getStringFromJSON(info, "user_id"));
        if (AppUtils.isEmptyobject(user))   {
            user = new MXUser();
            isNew = true;
        }

        user.updateUserWithInfo(info);

        if (isNew)  {
            MXUserDBHelper.getInstance().addUser(user);
        } else {
            MXUserDBHelper.getInstance().updateUser(user);
        }

        return user;
    }

    public static String saveUserByInfo(JSONObject info)  {
        // Save in local storage
        createUser(info);
        return AppUtils.getStringFromJSON(info, "user_id");
    }

    public static String saveUsersByObject(MXUser object) {
        boolean isNew = false;
        MXUser user = MXUserDBHelper.getInstance().getUser(object.user_id);
        if (AppUtils.isEmptyobject(user))   {
            isNew = true;
        }

        user = object;

        if (isNew)  {
            MXUserDBHelper.getInstance().addUser(user);
        } else {
            MXUserDBHelper.getInstance().updateUser(user);
        }

        return user.user_id;
    }

    public static void saveUsers(JSONArray users_info, String primaryUserId, CompletionListener completionListener) {
        if (users_info.length() == 0)
        {
            completionListener.complete(true, null);
            return;
        }
        // Save in local storage
        for (int i = 0; i < users_info.length(); i ++)  {
            JSONObject info = AppUtils.getJSONFromJSONArray(users_info, i);
            boolean hasToCreateRelation = false;
            if (AppUtils.isEmptyobject(MXUserDBHelper.getInstance().getUser(AppUtils.getStringFromJSON(info, "user_id"))))  {
                hasToCreateRelation = true;
            }

            createUser(info);

            if (hasToCreateRelation && AppUtils.isNotEmptyObject(primaryUserId))    {
                // create relationship
                MXRelationshipUtil.createRelationshipByInfo(info, primaryUserId);
            }
        }

        completionListener.complete(true, null);
    }

    /**
     * Find Methods
     */

    public static MXUser findByUserId(String user_id)   {
        return MXUserDBHelper.getInstance().getUser(user_id);
    }

    /**
     * Shared Preference Methods
     */

    public static boolean checkUserInfoExistsFromUserDefaults() {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));

        if (AppUtils.isEmptyobject(info))   {
            return false;
        } else {
            return info.has("MedXUser");
        }
    }

    public static void updateUserDefaults(JSONObject info, Date lastLogin)  {
        JSONObject params = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        if (params == null)
            params = new JSONObject();
        lastLogin = lastLogin == null ? new Date() : lastLogin;
        try {
            if (AppUtils.isNotEmptyObject(info))
                params.put("MedXUser", info);
            if (AppUtils.isNotEmptyObject(lastLogin))
                params.put("timestamp", AppUtils.StringFromDate(lastLogin));

            AppUtils.writeToFile(App.DefaultContext, params.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getUserInfoFromUserDefaults()  {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        return AppUtils.getJSONFromJSON(info, "MedXUser");
    }

    public static void updateUserDetaults(Date date)    {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        Date lastLogin = AppUtils.isEmptyobject(date) ? new Date() : date;
        try {
            info.put("timestamp", AppUtils.StringFromDate(lastLogin));
            AppUtils.writeToFile(App.DefaultContext, info.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Date getLastLoginFromUserDefaults()   {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        return AppUtils.DateFromString(AppUtils.getStringFromJSON(info, "timestamp"));
    }

    public static void updateUserDefaultswithDeviceToken(String deviceToken)    {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        try {
            info.put("deviceToken", deviceToken);
            AppUtils.writeToFile(App.DefaultContext, info.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getDeviceTokenFromUserDefaults()   {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        return AppUtils.getStringFromJSON(info, "device_Token");
    }

    public static boolean checkEncryptionKeysExistsFromUserDefaults()   {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        if (AppUtils.isEmptyobject(info))
            return false;

        return info.has("MedXKeys");
    }

    public static void updateUserDefaultsWithEncryptionKeys(List<String> keys)  {
        JSONArray keys_arr = new JSONArray();
        for (String key : keys) {
            keys_arr.put(key);
        }

        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        try {
            info.put("MedXKeys", keys_arr);
            AppUtils.writeToFile(App.DefaultContext, info.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getEncryptionKeysFromUserDefaults()  {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        String keys_str = AppUtils.getStringFromJSON(info, "MedXKeys");
        return new ArrayList<>(Arrays.asList(keys_str.split(" ")));
    }

    public static void removeEncryptionKeysFromUserDefaults()   {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        info.remove("MedXKeys");
        AppUtils.writeToFile(App.DefaultContext, info.toString());
    }

    public static void updateUserDefaultsWithLoginExpirePeriod(String expirePeriod) {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        if (info == null)
            info = new JSONObject();
        try {
            info.put("login_expire_period", expirePeriod);
            AppUtils.writeToFile(App.DefaultContext, info.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getLoginExpirePeriod() {
        JSONObject info = AppUtils.getJSONFromString(AppUtils.readFromFile(App.DefaultContext));
        return AppUtils.isEmptyString(AppUtils.getStringFromJSON(info, "login_expire_period")) ? "1 Day" : AppUtils.getStringFromJSON(info, "login_expire_period");
    }

    public static int getLoginExpirePeriodInSeconds()   {
        String period = getLoginExpirePeriod();
        int seconds = 0;

        if (period.equals("1 Hour"))    {
            seconds = 60 * 60;
        } else if (period.equals("1 Day"))  {
            seconds = 24 * 60 * 60;
        } else if (period.equals("3 Days")) {
            seconds = 3 * 24 * 60 * 60;
        } else if (period.equals("1 Week")) {
            seconds = 7 * 24 * 60 * 60;
        } else if (period.equals("1 Minute"))   {
            seconds = 60;
        }

        return seconds;
    }

    public static void removeUserParamsFromUserDefaults()   {
        AppUtils.writeToFile(App.DefaultContext, "");
    }

    /**
     * Utility Methods
     */

    public static String refinOfficePhoneNumberInLocation(String location)  {
        List<String> addresses = new ArrayList<>(Arrays.asList(location.split("\n")));
        String phone = addresses.get(addresses.size() - 1);

        addresses.remove(addresses.size() - 1);
        addresses.add(AppUtils.formatOfficePhoneNumber(phone));

        String fullAddress = "";

        for (String address : addresses)    {
            if (!address.isEmpty()) {
                fullAddress += address;
                if (addresses.indexOf(address) < addresses.size() - 1) {
                    fullAddress = fullAddress + "\n";
                }
            }
        }

        return fullAddress;
    }

    public static void saveKeyPair(KeyPair key) {
        //String str = new Gson().toJson(key);
        //context.getSharedPreferences(APP, Context.MODE_PRIVATE).edit().putString(KEY_PAIR, str).commit();
        PRIVATE_KEY_FILE = FileUtils.getTempDirectoryPath() + File.separator + "private.key";
        PUBLIC_KEY_FILE = FileUtils.getTempDirectoryPath() + File.separator + "public.key";

        try {
            File privateKeyFile = new File(PRIVATE_KEY_FILE);
            File publicKeyFile = new File(PUBLIC_KEY_FILE);

            // Create files to store public and private key
            if (privateKeyFile.getParentFile() != null) {
                privateKeyFile.getParentFile().mkdirs();
            }
            privateKeyFile.createNewFile();

            if (publicKeyFile.getParentFile() != null) {
                publicKeyFile.getParentFile().mkdirs();
            }
            publicKeyFile.createNewFile();

            // Saving the Public key in a file
            /*ObjectOutputStream publicKeyOS = new ObjectOutputStream(
                    new FileOutputStream(publicKeyFile));*/


            RSAPublicKey publicKey = (RSAPublicKey)key.getPublic();
            String keyStringPublic = publicKey.getModulus().toString() + "|" +
                    publicKey.getPublicExponent().toString();

            writeToFile(keyStringPublic, publicKeyFile);

            // Saving the Private key in a file
            ObjectOutputStream privateKeyOS = new ObjectOutputStream(
                    new FileOutputStream(privateKeyFile));

            RSAPrivateKey privateKey = (RSAPrivateKey)key.getPrivate();
            String keyStringPrivate = privateKey.getModulus().toString() + "|" +
                    privateKey.getPrivateExponent().toString();

            writeToFile(keyStringPrivate, privateKeyFile);

         /*   privateKeyOS.writeObject(keyStringPrivate);
            privateKeyOS.flush();
            privateKeyOS.close();*/

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final KeyPair getKeyPair() {
        KeyPair keyPair = null;

        PRIVATE_KEY_FILE = FileUtils.getTempDirectoryPath() + File.separator + "private.key";
        PUBLIC_KEY_FILE = FileUtils.getTempDirectoryPath() + File.separator + "public.key";

        File privateFile = new File(PRIVATE_KEY_FILE);
        File publicFile = new File(PUBLIC_KEY_FILE);

        try {
            String publicKeyString = getStringFromFile(PUBLIC_KEY_FILE);
            publicKeyString = publicKeyString.replaceAll("\n", "");

            String []PartsPublic = publicKeyString.split("\\|");
            RSAPublicKeySpec Spec = new RSAPublicKeySpec(
                    new BigInteger(PartsPublic[0]),
                    new BigInteger(PartsPublic[1]));
            PublicKey publicKey =  KeyFactory.getInstance("RSA").generatePublic(Spec);

            String privateKeyString = getStringFromFile(PRIVATE_KEY_FILE);
            privateKeyString = privateKeyString.replaceAll("\n", "");

            String []PartsPrivate = privateKeyString.split("\\|");
            RSAPrivateKeySpec SpecPrivate = new RSAPrivateKeySpec(
                    new BigInteger(PartsPrivate[0]),
                    new BigInteger(PartsPrivate[1]));
            PrivateKey privateKey =  KeyFactory.getInstance("RSA").generatePrivate(SpecPrivate);

            keyPair = new KeyPair(publicKey, privateKey);


            //Log.d("test", "test");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return keyPair;
    }

    private static void writeToFile(String data, File file) {
        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(data.getBytes());
        }
        catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
}
