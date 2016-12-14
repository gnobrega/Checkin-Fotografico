package br.com.plux.checkinfotografico;

import android.app.Activity;
import android.os.Environment;

/**
 * Created by gustavonobrega on 01/06/2016.
 */
public class App {
    public static String SERVER_HOST = "https://corporativo.3midia.com.br/";
    public static String SERVER_API_HOST = App.SERVER_HOST + "api/";
    public static String SERVER_GET_USERS = App.SERVER_API_HOST + "get-users-checkin.json";
    public static String SERVER_GET_ROUTE = App.SERVER_API_HOST + "get-route";
    public static Activity MAIN_ACTIVITY = null;
    public static Integer THUMB_WIDTH = 100;

    //AWS
    public static String AWS_S3_BUCKET_DEFAULT = "checkin-fotografico";
    public static final String COGNITO_POOL_ID = "us-east-1:3f38eec3-a157-4280-b529-8cefa0c565fa";

    //Path photos
    public static String PATH_PHOTOS = Environment.getExternalStorageDirectory() + "/Checkin";

    //Digital Signage
    public static String DS_USER = "sandrade";
    public static String DS_PASS = "Senha01";
    public static String DS_DOMAIN = "pluto.signage.me";
}
