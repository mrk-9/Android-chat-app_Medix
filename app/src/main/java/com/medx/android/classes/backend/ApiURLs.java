package com.medx.android.classes.backend;

/**
 * Created by alexey on 9/8/16.
 */

public class ApiURLs {
    public static final String BASE_URL = "http://192.168.1.102:4035/api/v1/";

    public static final String PUBLIC_URL = "http://192.168.1.102:4040/api/";

    public static final String RESET = "reset";

    public static final String USERS = "users/";
    public static final String MESSAGES = "messages/";
    public static final String AUTH = "auth/";

    public static final String URL_ALL_DIALOGS = "check/all_dialogs";
    public static final String URL_USERS_INFO = USERS + "info";
    public static final String URL_USERS_SEARCH = USERS + "search";
    public static final String URL_USERS_GEOCODE = USERS + "geocode";

    public static final String URL_USERS_UNSET_WIPE = USERS + "unset_wipe";
    public static final String URL_USERS_UPDATE = USERS + "update";
    public static final String URL_USERS_BLOCK = USERS + "block";
    public static final String URL_USERS_UNBLOCK = USERS + "unblock";
    public static final String URL_USERS_DEVICE = USERS + "device";
    public static final String URL_USERS_PUBLIC_KEY = USERS + "public_key";
    public static final String URL_USERS_INVITE = USERS + "invite";

    public static final String URL_MESSAGES_SEND = MESSAGES + "send";
    public static final String URL_MESSAGES_TRANSFER_PHOTO = MESSAGES + "transfer_photo";
    public static final String URL_MESSAGES_DELETE = MESSAGES + "delete";
    public static final String URL_MESSAGES_UPDATE_STATUS = MESSAGES + "update_status";

    public static final String URL_AUTH_VERIFY_PIN = AUTH + "verify_pin";
    public static final String URL_AUTH_VERIFY_REGISTRATION_CODE = AUTH + "verify_registration_code";
    public static final String URL_AUTH_PIN = AUTH + "pin";
    public static final String URL_AUTH_REGISTER = AUTH + "register";

    public static final String URL_UPDATE_DIALOG = USERS + "update_dialog";
}
