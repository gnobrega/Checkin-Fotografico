package br.com.plux.checkinfotografico;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
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
    public static Context context = null;
    public static Integer THUMB_WIDTH = 100;
    public static Integer GRID_COLS_PORTRAIT = 3;
    public static Integer GRID_COLS_LANDSCAPE = 5;

    //AWS
    public static String AWS_S3_BUCKET_DEFAULT = "checkin-fotografico";
    public static final String COGNITO_POOL_ID = "us-east-1:3f38eec3-a157-4280-b529-8cefa0c565fa";

    //Path photos
    public static String PATH_PHOTOS = Environment.getExternalStorageDirectory() + "/Checkin";

    //Digital Signage
    public static String DS_USER = "checking@plux.com.br";
    public static String DS_PASS = "123456";
    public static String DS_DOMAIN = "pluto.signage.me";

    //Menu keys
    public static Integer MENU_CHECKIN_INDEX = 2;

    public static Integer getGridCols() {
        if( MAIN_ACTIVITY.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ) {
            return GRID_COLS_PORTRAIT;
        } else {
            return GRID_COLS_LANDSCAPE;
        }
    }
}
