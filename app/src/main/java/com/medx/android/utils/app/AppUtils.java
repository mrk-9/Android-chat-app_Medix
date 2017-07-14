package com.medx.android.utils.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;

import com.medx.android.aws.AWSConstants;
import com.medx.android.models.chat.MXMessage;
import com.medx.android.utils.chat.MXMessageUtil;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Encoder;

/**
 * Created by alexey on 9/6/16.
 */

public class AppUtils {

    /**
     * JSON Methods
     */
    public static String getStringFromJSON(JSONObject object, String key)   {
        if (object != null && object.has(key)) {
            try {
                String result = object.getString(key);
                if (result.equals("null"))
                    return "";
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        } else return "";
    }

    public static JSONArray reverseObjectEnumerator(JSONArray sArray)   {
        JSONArray tArray = new JSONArray();
        for (int i = sArray.length() - 1; i > -1; i --)  {
            JSONObject child = getJSONFromJSONArray(sArray, i);
            tArray.put(child);
        }

        return tArray;
    }

    public static JSONObject getJSONFromString(String info) {
        if (!info.isEmpty()){
            try {
                JSONObject result = new JSONObject(info);
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    public static String getStringFromJSONArray(JSONArray array, int position)  {
        if (array.length() > position) {
            try {
                String result = array.getString(position);
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static JSONObject getJSONFromJSONArray(JSONArray array, int position)    {
        if (array.length() > position) {
            try {
                JSONObject result = array.getJSONObject(position);
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return new JSONObject();
    }

    public static JSONArray getJSONArrayFromJSON(JSONObject object, String key) {
        if (object.has(key)) {
            try {
                JSONArray result = object.getJSONArray(key);
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONArray();
    }

    public static JSONObject getJSONFromJSON(JSONObject object, String key) {
        if (object.has(key)) {
            try {
                JSONObject result = object.getJSONObject(key);
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return new JSONObject();
    }

    public static ArrayList<String> getArrayFromJSON(JSONObject object, String key) {
        ArrayList<String> result = new ArrayList<>();
        if (object.has(key)) {
            try {
                JSONArray dataArray = object.getJSONArray(key);
                for (int i = 0; i < dataArray.length(); i++) {
                    result.add(dataArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }


    public static void setJSONObjectWithObject(JSONObject object, String key, Object value)  {
        try {
            object.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray insertObjectToJSONArray(JSONArray array, int position, JSONObject object)   {
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        for (int i = 0; i < array.length(); i ++)   {
            arrayList.add(AppUtils.getJSONFromJSONArray(array, i));
        }

        arrayList.add(position, object);

        JSONArray newarray = new JSONArray();
        for (JSONObject child : arrayList)  {
            newarray.put(child);
        }

        return newarray;
    }

    public static void insertObjectToJSONArray(JSONArray array, int position, String object)   {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < position; i ++)   {
            arrayList.add(AppUtils.getStringFromJSONArray(array, i));
        }

        arrayList.add(object);

        for (int i = position + 1; i < array.length(); i ++)    {
            arrayList.add(AppUtils.getStringFromJSONArray(array, i));
        }

        array = new JSONArray();
        for (String  child : arrayList)  {
            array.put(child);
        }
    }

    /**
     * Validation Methods
     */
    public static boolean isEmptyobject(Object object)  {
        if (object == null)
            return true;

        return false;
    }

    public static boolean isNotEmptyObject(Object object)   {
        if (object == null)
            return false;

        return true;
    }

    public static boolean isEmptyString(String string)  {
        if (isEmptyobject(string))
            return true;

        String result = string.trim();

        return result.length() == 0;
    }

    public static boolean isNotEmptyString(String string)   {
        if (isEmptyobject(string))
            return false;

        String result = string.trim();

        return result.length() > 0;
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidPhone(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.PHONE.matcher(target).matches();
    }

    /**
     * Phone number methods
     */

    public static String formatOfficePhoneNumber(String mobileNumber)   {
        mobileNumber = mobileNumber.replace("(", "");
        mobileNumber = mobileNumber.replace(")", "");
        mobileNumber = mobileNumber.replace(" ", "");
        mobileNumber = mobileNumber.replace("_", "");
        mobileNumber = mobileNumber.replace("+", "");

        if (mobileNumber.length() == 9) {
            mobileNumber = "0" + mobileNumber;
        }

        if (mobileNumber.length() == 10)    {
            mobileNumber = "(" + mobileNumber.substring(0, 2) + ") " + mobileNumber.substring(2, 6) + " " + mobileNumber.substring(6, 10);
        }

        return mobileNumber;
    }

    /**
     * Date Methods
     */

    public static String printDay(Date time){
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy EEEE");
        return format.format(time);
    }

    public static String getUTCDate(Date date)  {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        String time = format.format(date);
        return time;
    }

    public static Date getUTCDate(String date)  {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Date();
    }

    public static String getLocalDate(String date)  {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = calendar.getTimeZone();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date1 = getUTCDate(date);
        format.setTimeZone(tz);
        return format.format(date1);
    }

    public static String printDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        Date date1 = DateFromString(date);
        return format.format(date1);
    }

    public static String StringFromDate(Date date)  {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return formatter.format(date);
    }

    public static Date DateFromString(String date)  {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public static long MillionsFromDate(Date date)  {
        date.getTime();
        return date.getTime();
    }

    public static Date DateFromMillions(Long milliSeconds)  {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return calendar.getTime();
    }

    public static String DateStringWithFormatter(Date date, String formatString)  {
        SimpleDateFormat formatter = new SimpleDateFormat(formatString);
        return formatter.format(date);
    }

    public static int compareDate(Date date_t1, Date date_t2)   {

        if (date_t1.getTime() < date_t2.getTime())
            return 1;
        else if (date_t1.getTime() == date_t2.getTime())
            return 0;
        else return -1;
    }

    /**
     * manage shared preference methods
     */
    public static String getStringFromSharedPreferences(Context context, String key, String defaultValue) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        String value = sharedPreferences.getString(key, defaultValue);

        return value;
    }

    public static void saveStringInSharedPreferences(Context context, String key, String value) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value)
                .commit();
    }

    /**
     * read/write file methods
     */

    public static byte[] readFileByByte(String filePath)    {
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static String writeImageToFile(ImageView imageView, String fileName)   {
        imageView.buildDrawingCache();
        Bitmap bm = imageView.getDrawingCache();

        OutputStream fOut = null;
        Uri outputFileUri = null;
        File root = new File(Environment.getExternalStorageDirectory() + File.separator + "temp" + File.separator);
        root.mkdirs();
        File sdImageMainDirectory = new File(root, fileName + ".jpg");
        outputFileUri = Uri.fromFile(sdImageMainDirectory);
        try {
            fOut = new FileOutputStream(sdImageMainDirectory);
            bm.compress(Bitmap.CompressFormat.JPEG, 70, fOut);
            fOut.flush();
            fOut.close();

            return Environment.getExternalStorageDirectory() + File.separator + "temp" + File.separator + File.separator + fileName + ".jpg";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String copyImageToTempFolder(Context context, Uri sourceUri, String fileName) {
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
        return result ? tempPath : "";
    }

    public static void removeTemporaryFile(String fileName) {
        File root = new File(FileUtils.getTempDirectoryPath() + File.separator + "temp" + File.separator);
        File sdImageMainDirectory = new File(root, fileName + ".jpg");
        sdImageMainDirectory.delete();
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void writeToFile(Context context, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("user.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    public static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("user.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public static String imagesPath()   {
        String dir = FileUtils.getTempDirectoryPath() + "/Documents/Images";
        boolean isCreated;
        if (!new File(dir).exists()) {
            isCreated = new File(dir).mkdirs();
            if (isCreated)  {
                Log.d("status", "folder created");
            }
        }
        return dir;
    }

    public static void createImagesDirectory()  {
        File imageDirectory = new File(imagesPath());
        imageDirectory.mkdirs();
    }

    public static String imagePathWithFilName(String filename)  {
        return imagesPath() + File.separator + filename;
    }

    public static boolean addSkipBackupAttributeToItemAtPath(String filePathString) {
        File file = new File(filePathString);
        return file.exists();
    }

    public static void dumpFileInDirectoryPath(String pathToDirectory)  {
        String directory = pathToDirectory + "/";
        ArrayList<String> fileNameList = getFileNames(GetFiles(directory));

        for (String file : fileNameList)    {
            String filePath = pathToDirectory + "/" + file;
            new File(filePath).delete();
        }
    }

    public static File[] GetFiles(String DirectoryPath) {
        File f = new File(DirectoryPath);
        f.mkdirs();
        File[] file = f.listFiles();
        return file;
    }

    public static ArrayList<String> getFileNames(File[] file){
        ArrayList<String> arrayFiles = new ArrayList<String>();
        if (file.length == 0)
            return null;
        else {
            for (int i=0; i<file.length; i++)
                arrayFiles.add(file[i].getName());
        }

        return arrayFiles;
    }

    public static String[] getSpecialities()   {
        File file = new File("specialty_list.txt");
        String filePath = file.getAbsolutePath();
        return filePath.split("\n");
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        int i;
        byte[] b = new byte[1024];
        while ((i = is.read(b)) != -1)    {
            os.write(b, 0, i);
        }
    }

    /**
     * Secret key Methods
     */

    public static String convertSecretKeyToString(SecretKey key)    {
        return Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP);
    }

    public static SecretKey convertStringToSecretKey(String stringKey)  {
        byte[] encodedKey = Base64.decode(stringKey, Base64.NO_WRAP);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    public static String convertPublicKeyToString(PublicKey key)    {
        byte[] publicKeyBytes = key.getEncoded();

        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(publicKeyBytes);
    }

    public static PublicKey convertStringToPublicKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = Base64.decode(key, Base64.NO_WRAP);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = factory.generatePublic(keySpec);

        return publicKey;
    }

    /**
     * bitmap methods
     */

    public static BitmapFactory.Options getOptionsFromFile(String mPath)   {
        byte[] byteArray = null;

        try {
            byteArray = FileUtils.readFileToByteArray(new File(mPath));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArray, 0,byteArray.length, options);
            return options;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getImageWidthFromFile(String file)    {
        return getOptionsFromFile(file) != null ? getOptionsFromFile(file).outWidth : 0;
    }

    public static int getImageHeightFromFile(String file)    {
        return getOptionsFromFile(file) != null ? getOptionsFromFile(file).outHeight : 0;
    }

    /**
     * Utility Methods
     */

    public static String StringWithComponentsJoinedByStringFromJSON(JSONArray array, String symbol) {
        ArrayList<String> arrayList = ArrayFromJSON(array);
        return StringWithComponentsJoinedByString(arrayList, symbol);
    }

    public static String StringWithComponentsJoinedByString(ArrayList<String> list, String symbol)    {
        String str = "";
        for (String child : list) {
            str += child;
            if (list.indexOf(child) < list.size() - 1)   {
                str += symbol;
            }
        }

        return str;
    }

    public static ArrayList<String> ArrayFromJSON(JSONArray array)  {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i ++)   {
            String child = getStringFromJSONArray(array, i);
            list.add(child);
        }

        return list;
    }

    public static JSONArray JSONFromArray(ArrayList<String> arrayList)  {
        JSONArray array = new JSONArray();
        for (String str : arrayList)    {
            array.put(str);
        }
        return array;
    }

    public static JSONArray JSONFromArrayWithMessage(ArrayList<MXMessage> arrayList)  {
        JSONArray array = new JSONArray();
        for (MXMessage m : arrayList)    {
            array.put(MXMessageUtil.dictionaryWithValuesFromMessage(m));
        }
        return array;
    }

    public static String awsDownloadRequestKeyByFileUrl(String url) {
        String n1 = String.format("https://%s.s3.amazonaws.com/", AWSConstants.AWS_S3_BUCKET);
        String k1 =  String.format("http://%s.s3.amazonaws.com/", AWSConstants.AWS_S3_BUCKET);
        String n2 = url.replaceFirst(n1, "");

        n2 = n2.replaceFirst(k1, "");

        int location = n2.indexOf("?");

        String key = n2.substring(0, location);
        return key;
    }
}
