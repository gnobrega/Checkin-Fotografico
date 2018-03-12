package br.com.plux.checkinfotografico;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import br.com.plux.checkinfotografico.bean.CampaignBean;
import br.com.plux.checkinfotografico.bean.LocationBean;
import br.com.plux.checkinfotografico.bean.PhotoBean;
import br.com.plux.checkinfotografico.bean.StationBean;
import br.com.plux.checkinfotografico.bean.UserBean;

/**
 * Created by gustavonobrega on 31/05/2016.
 */
public class DataBase extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private static String DB_NAME = "checkin";
    private static int VERSAO = 3;

    //Table user
    public static String TABLE_USER = "user";
    public static String TB_USER_ID = "id";
    public static String TB_USER_NAME = "name";
    public static String TB_USER_LOGIN = "login";
    public static String TB_USER_PASS = "password";

    //Table location
    public static String TABLE_LOCATION = "location";
    public static String TB_LOCATION_ID = "id";
    public static String TB_LOCATION_NAME = "name";
    public static String TB_LOCATION_ID_ROUTE = "id_route";
    public static String TB_LOCATION_LATITUDE = "latitude";
    public static String TB_LOCATION_LONGITUDE = "longitude";

    //Table campaign
    public static String TABLE_CAMPAIGN = "campaign";
    public static String TB_CAMPAIGN_ID = "id";
    public static String TB_CAMPAIGN_NAME = "name";

    //Table photo
    public static String TABLE_PHOTO = "photo";
    public static String TB_PHOTO_ID = "id";
    public static String TB_PHOTO_FILE = "file";
    public static String TB_PHOTO_ID_CAMPAIGN = "id_campaign";
    public static String TB_PHOTO_CAMPAIGN = "campaign";
    public static String TB_PHOTO_ID_USER = "id_user";
    public static String TB_PHOTO_ID_LOCATION = "id_location";
    public static String TB_PHOTO_KEY_GRID = "key_grid";

    //Table station
    public static String TABLE_STATION = "station";
    public static String TB_STATION_ID = "id";
    public static String TB_STATION_TEXT = "text";
    public static String TB_STATION_LOCATION_ID = "locationId";
    public static String TB_STATION_ST_ID = "stationId";

    /**
     * Construtor
     */
    public DataBase(Context context) {
        super(context, DataBase.DB_NAME, null, DataBase.VERSAO);
        createTables();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    void createTables() {
        db = this.getWritableDatabase();
        createTableUser(db);
        createTableLocation(db);
        createTableCampaign(db);
        createTablePhoto(db);
        createTableStation(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAMPAIGN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATION);
        onCreate(db);
    }

    /**
     * Cria a tabela de usuário
     */
    public void createTableUser(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "("
                + "'" + TB_USER_ID + "'" + " integer primary key autoincrement,"
                + "'" + TB_USER_NAME + "'"  + "text,"
                + "'" + TB_USER_LOGIN + "'"  + "text,"
                + "'" + TB_USER_PASS + "'"  + "text"
                +")";
        db.execSQL(sql);
    }

    /**
     * Cria a tabela de pontos
     */
    public void createTableLocation(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_LOCATION + "("
                + "'" + TB_LOCATION_ID + "'" + " integer primary key autoincrement,"
                + "'" + TB_LOCATION_NAME + "'"  + " text,"
                + "'" + TB_LOCATION_ID_ROUTE + "'"  + " int,"
                + "'" + TB_LOCATION_LATITUDE + "'"  + " text,"
                + "'" + TB_LOCATION_LONGITUDE + "'"  + " text"
                +")";
        db.execSQL(sql);
    }

    /**
     * Cria a tabela de campanhas
     */
    public void createTableCampaign(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_CAMPAIGN + "("
                + "'" + TB_CAMPAIGN_ID + "'" + " integer primary key autoincrement,"
                + "'" + TB_CAMPAIGN_NAME+ "'"  + " text"
                +")";
        db.execSQL(sql);
    }

    /**
     * Cria a tabela de estações
     */
    public void createTableStation(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_STATION + "("
                + "'" + TB_STATION_ID + "'" + " integer,"
                + "'" + TB_STATION_TEXT+ "'"  + " text,"
                + "'" + TB_STATION_LOCATION_ID + "'"  + " integer,"
                + "'" + TB_STATION_ST_ID + "'"  + " integer"
                +")";
        db.execSQL(sql);
    }

    /**
     * Cria a tabela de fotos
     */
    public void createTablePhoto(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_PHOTO + "("
                + "'" + TB_PHOTO_ID + "'" + " integer primary key autoincrement,"
                + "'" + TB_PHOTO_FILE+ "'"  + " text,"
                + "'" + TB_PHOTO_CAMPAIGN+ "'"  + " text,"
                + "'" + TB_PHOTO_ID_CAMPAIGN+ "'"  + " integer,"
                + "'" + TB_PHOTO_ID_USER+ "'"  + " integer,"
                + "'" + TB_PHOTO_ID_LOCATION+ "'"  + " integer,"
                + "'" + TB_PHOTO_KEY_GRID+ "'"  + " integer"
                +")";
        db.execSQL(sql);
    }

    /**
     * Remove a tabela de usuário
     */
    public void dropTableUser() {
        db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
    }

    /**
     * Remove a tabela de estações
     */
    public void dropTableStation() {
        db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATION);
    }
    /**
     * Remove a tabela de usuário
     */
    public void dropTablePhoto() {
        db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO);
    }

    /**
     * Limpa a tabela de usuários
     */
    public void clearUsers() {
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_USER);
    }

    /**
     * Limpa a tabela de pontos
     */
    public void clearLocations() {
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_LOCATION);
    }

    /**
     * Limpa a tabela de pontos
     */
    public void clearCampaigns() {
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CAMPAIGN);
    }

    /**
     * Limpa a tabela de estações
     */
    public void clearStations() {
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_STATION);
    }

    /**
     * Limpa a tabela de fotos
     */
    public void clearPhotos() {
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PHOTO);
    }

    /**
     * Exclusão genérica
     */
    public void delete(String table, Long id) {
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + table + " WHERE id = " + id);
    }

    /**
     * Atualiza a campanha da foto
     */
    public void setCampaignPhoto(Integer campaignId, String campaignName, Long photoId) {
        db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PHOTO + " SET " +
                    TB_PHOTO_ID_CAMPAIGN + " = " + campaignId + ", " +
                    TB_PHOTO_CAMPAIGN + " = '" + campaignName + "' " +
                    "WHERE " + TB_PHOTO_ID + " = " + photoId
                  );
    }

    /**
     * Insere um novo usuário
     */
    public Long insertUser(int id, String name, String login, String password){

        db = this.getWritableDatabase();
        ContentValues user = new ContentValues();
        user.put(this.TB_USER_ID, id);
        user.put(this.TB_USER_NAME, name);
        user.put(this.TB_USER_LOGIN, login);
        user.put(this.TB_USER_PASS, password);
        Long userId = db.insert(this.TABLE_USER, null, user);
        db.close();

        if (userId ==-1) {
            //Erro
            return null;
        } else {
            return userId;
        }
    }

    /**
     * Insere um novo ponto
     */
    public Long insertLocation(int id, String name, int routeId, String latitude, String longitude){

        db = this.getWritableDatabase();
        ContentValues location = new ContentValues();
        location.put(this.TB_LOCATION_ID, id);
        location.put(this.TB_LOCATION_NAME, name);
        location.put(this.TB_LOCATION_ID_ROUTE, routeId);
        location.put(this.TB_LOCATION_LATITUDE, latitude);
        location.put(this.TB_LOCATION_LONGITUDE, longitude);
        Long locationId = db.insert(this.TABLE_LOCATION, null, location);
        db.close();

        if (locationId ==-1) {
            //Erro
            return null;
        } else {
            return locationId;
        }
    }

    /**
     * Insere um novo ponto
     */
    public Long insertCampaign(int id, String name){

        db = this.getWritableDatabase();
        ContentValues campaign = new ContentValues();
        campaign.put(this.TB_CAMPAIGN_ID, id);
        campaign.put(this.TB_CAMPAIGN_NAME, name.trim());
        Long campaignId = db.insert(this.TABLE_CAMPAIGN, null, campaign);
        db.close();

        if (campaignId ==-1) {
            //Erro
            return null;
        } else {
            return campaignId;
        }
    }

    /**
     * Insere um novo ponto
     */
    public Long insertStation(int stationId, String text, int locationId){

        db = this.getWritableDatabase();
        ContentValues station = new ContentValues();
        station.put(this.TB_STATION_ID, stationId);
        station.put(this.TB_STATION_TEXT, text.trim());
        station.put(this.TB_STATION_LOCATION_ID, locationId);
        Long id = db.insert(this.TABLE_STATION, null, station);
        db.close();

        if (id ==-1) {
            //Erro
            return null;
        } else {
            return id;
        }
    }

    /**
     * Insere uma nova foto
     */
    public Long insertPhoto(String uri, Integer campaignId, String campaign, Integer userId, Integer locationId, Integer keyGrid){

        db = this.getWritableDatabase();
        ContentValues photo = new ContentValues();
        photo.put(this.TB_PHOTO_FILE, uri);
        photo.put(this.TB_PHOTO_ID_CAMPAIGN, campaignId);
        photo.put(this.TB_PHOTO_CAMPAIGN, campaign);
        photo.put(this.TB_PHOTO_ID_USER, userId);
        photo.put(this.TB_PHOTO_ID_LOCATION, locationId);
        photo.put(this.TB_PHOTO_KEY_GRID, keyGrid);

        Long photoId = db.insert(this.TABLE_PHOTO, null, photo);
        db.close();

        if (photoId == -1) {
            //Erro
            return null;
        } else {
            return photoId;
        }
    }
    public UserBean getUserAuth(String login, String pass) {
        String passHash = Util.sha1(Util.md5(pass));
        String[] campos =  {"*"};
        String selection = this.TB_USER_LOGIN + " = '"+ login +
                            "' AND " + this.TB_USER_PASS + " = '" + passHash + "'";
        if( db == null ) {
            db = this.getWritableDatabase();
        }
        String sql = "SELECT * FROM TABLE " + TABLE_USER +
                     " WHERE " + this.TB_USER_LOGIN + " = '" + login + "'" +
                     " AND " + this.TB_USER_PASS + " = '" + passHash + "'";

        Cursor cursor = db.query(this.TABLE_USER, campos, selection, null, null, null, null, null);
        if( cursor.getCount() > 0 ) {
            cursor.moveToNext();
            UserBean userBean = toUserBean(cursor);
            return userBean;
        } else {
            return null;
        }
    }

    public LocationBean getLocation(Integer id) {
        String[] campos =  {"*"};
        String selection = this.TB_LOCATION_ID + " = "+ id;
        if( db == null ) {
            db = this.getWritableDatabase();
        }

        Cursor cursor = db.query(this.TABLE_LOCATION, campos, selection, null, null, null, null, null);
        if( cursor.getCount() > 0 ) {
            cursor.moveToNext();
            LocationBean locationBean = toLocationBean(cursor);
            return locationBean;
        } else {
            return null;
        }
    }

    /**
     * Converte Cursor em UserBean
     */
    public UserBean toUserBean(Cursor cursor) {
        int userId = cursor.getInt(cursor.getColumnIndex("id"));
        String userName = cursor.getString(cursor.getColumnIndex("name"));
        String userLogin = cursor.getString(cursor.getColumnIndex("login"));
        String userPass = cursor.getString(cursor.getColumnIndex("password"));
        UserBean userBean = new UserBean();
        userBean.setId(userId);
        userBean.setName(userName);
        userBean.setLogin(userLogin);
        userBean.setPassword(userPass);
        return userBean;
    }

    /**
     * Converte Cursor em LocationBean
     */
    public LocationBean toLocationBean(Cursor cursor) {
        int locationId = cursor.getInt(cursor.getColumnIndex("id"));
        String locationName = cursor.getString(cursor.getColumnIndex("name"));
        int locationIdRoute = cursor.getInt(cursor.getColumnIndex("id_route"));
        String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
        String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
        LocationBean locationBean = new LocationBean();
        locationBean.setId(locationId);
        locationBean.setName(locationName);
        locationBean.setId_route(locationIdRoute);
        locationBean.setLatitude(latitude);
        locationBean.setLongitude(longitude);
        return locationBean;
    }

    /**
     * Converte Cursor em CampaignBean
     */
    public CampaignBean toCampaignBean(Cursor cursor) {
        int campId = cursor.getInt(cursor.getColumnIndex("id"));
        String campName = cursor.getString(cursor.getColumnIndex("name"));
        CampaignBean campaignBean = new CampaignBean();
        campaignBean.setId(campId);
        campaignBean.setName(campName);
        return campaignBean;
    }

    /**
     * Converte Cursor em StationBean
     */
    public StationBean toStationBean(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex("id"));
        String stText = cursor.getString(cursor.getColumnIndex("text"));
        int stLocId = cursor.getInt(cursor.getColumnIndex("locationId"));

        StationBean stationBean = new StationBean();
        stationBean.setId(id);
        stationBean.setText(stText);
        stationBean.setLocationId(stLocId);
        return stationBean;
    }

    /**
     * Converte Cursor em PhotoBean
     */
    public PhotoBean toPhotoBean(Cursor cursor) {
        Long photoId = cursor.getLong(cursor.getColumnIndex(TB_PHOTO_ID));
        String photoFile = cursor.getString(cursor.getColumnIndex(TB_PHOTO_FILE));
        int photoIdLoc = cursor.getInt(cursor.getColumnIndex(TB_PHOTO_ID_LOCATION));
        int photoIdCam = cursor.getInt(cursor.getColumnIndex(TB_PHOTO_ID_CAMPAIGN));
        String photoCam = cursor.getString(cursor.getColumnIndex(TB_PHOTO_CAMPAIGN));
        int photoIdUser = cursor.getInt(cursor.getColumnIndex(TB_PHOTO_ID_USER));
        int keyGridUser = cursor.getInt(cursor.getColumnIndex(TB_PHOTO_KEY_GRID));

        PhotoBean photoBean = new PhotoBean();
        photoBean.setId(photoId);
        photoBean.setFile(photoFile);
        photoBean.setId_location(photoIdLoc);
        photoBean.setCampaign(photoCam);
        photoBean.setId_campaign(photoIdCam);
        photoBean.setId_user(photoIdUser);
        photoBean.setKey_grid(keyGridUser);

        return photoBean;
    }
    /**
     * Carrega a lista de usuários
     */
    public ArrayList loadUsers(){
        Cursor cursor;
        String[] campos =  {"*"};
        db = this.getReadableDatabase();
        cursor = db.query(this.TABLE_USER, campos, null, null, null, null, null, null);
        ArrayList<UserBean> lstUsers = new ArrayList<UserBean>();
        while (cursor.moveToNext()) {
            UserBean userBean = toUserBean(cursor);
            lstUsers.add(userBean);
        }
        db.close();
        return lstUsers;
    }

    /**
     * Carrega a lista de pontos
     */
    public ArrayList<LocationBean> loadLocations(){
        Cursor cursor;
        String[] campos =  {"*"};
        db = this.getReadableDatabase();
        cursor = db.query(this.TABLE_LOCATION, campos, null, null, null, null, null, null);
        ArrayList<LocationBean> lstLocations = new ArrayList<LocationBean>();
        while (cursor.moveToNext()) {
            LocationBean locationBean = toLocationBean(cursor);
            lstLocations.add(locationBean);
        }
        db.close();
        return lstLocations;
    }

    /**
     * Carrega a lista de campanhas
     */
    public ArrayList<CampaignBean> loadCampaigns(){
        Cursor cursor;
        String[] campos =  {"*"};
        db = this.getReadableDatabase();
        cursor = db.query(this.TABLE_CAMPAIGN, campos, null, null, null, null, TB_CAMPAIGN_NAME, null);
        ArrayList<CampaignBean> lstCampaigns = new ArrayList<CampaignBean>();
        while (cursor.moveToNext()) {
            CampaignBean campaignBean = toCampaignBean(cursor);
            lstCampaigns.add(campaignBean);
        }
        db.close();
        return lstCampaigns;
    }

    /**
     * Carrega a lista de campanhas
     */
    public ArrayList<StationBean> loadStatoins(){
        Cursor cursor;
        String[] campos =  {"*"};
        db = this.getReadableDatabase();
        cursor = db.query(this.TABLE_STATION, campos, null, null, null, null, TB_STATION_TEXT, null);
        ArrayList<StationBean> lstStations = new ArrayList<StationBean>();
        while (cursor.moveToNext()) {
            StationBean stationBean = toStationBean(cursor);
            lstStations.add(stationBean);
        }
        db.close();
        return lstStations;
    }

    /**
     * Carrega a lista de fotos
     */
    public ArrayList<PhotoBean> loadPhotos(){
        Cursor cursor;
        String[] campos =  {"*"};
        db = this.getReadableDatabase();
        cursor = db.query(this.TABLE_PHOTO, campos, null, null, null, null, TB_PHOTO_ID + " ASC", null);
        ArrayList<PhotoBean> lstPhotos = new ArrayList<PhotoBean>();
        while (cursor.moveToNext()) {
            PhotoBean photoBean = toPhotoBean(cursor);
            lstPhotos.add(photoBean);
        }
        db.close();
        return lstPhotos;
    }
}