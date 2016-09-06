package br.com.plux.checkinfotografico;

/**
 * Created by gustavonobrega on 01/06/2016.
 */
public class App {
    public static String SERVER_HOST = "https://corporativo.3midia.com.br/";
    public static String SERVER_API_HOST = App.SERVER_HOST + "api/";
    public static String SERVER_GET_USERS = App.SERVER_API_HOST + "get-users-checkin.json";
    public static String SERVER_GET_ROUTE = App.SERVER_API_HOST + "get-route";

    //AWS
    public static String AWS_S3_BUCKET_DEFAULT = "checkin-fotografico";
    public static String AWS_ACCESS_KEY_ID = "AKIAJBAVLURHNNMEZMUQ";
    public static String AWS_SECRET_KEY = "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";

    //Path photos
    public static String PATH_PHOTOS = "/storage/sdcard0/Checkin";
}
