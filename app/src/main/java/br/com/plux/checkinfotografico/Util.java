package br.com.plux.checkinfotografico;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import br.com.plux.checkinfotografico.bean.UserBean;

/**
 * Created by gustavonobrega on 03/06/2016.
 */
public class Util {
    public static String md5(String str) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
            m.update(str.getBytes(), 0, str.length());
            return new BigInteger(1,m.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void toast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg,Toast.LENGTH_SHORT);
        toast.show();
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String sha1(String text) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Redimensiona uma imagem a partir do bitmap
     */
    public static Bitmap resizeImage(Context context, Bitmap bmpOriginal, float newWidth, float newHeigth) {
        Bitmap novoBmp = null;

        int w = bmpOriginal.getWidth();
        int h = bmpOriginal.getHeight();

        float densityFactor = context.getResources().getDisplayMetrics().density;
        float novoW = newWidth * densityFactor;
        float novoH = newHeigth * densityFactor;

        float scalaW = novoW / w;
        float scalaH = novoH / h;

        Matrix matrix = new Matrix();
        matrix.postScale(scalaW, scalaH);

        novoBmp = Bitmap.createBitmap(bmpOriginal, 0, 0, w, h, matrix, true);
        return novoBmp;
    }

    /**
     * Redimensiona uma imagem a partir do bitmap e informando apenas a largura
     */
    public static Bitmap resizeImage(Context context, Bitmap bmpOriginal, float newWidth) {
        int newHeigth = ((int) newWidth * bmpOriginal.getHeight() / bmpOriginal.getWidth());
        bmpOriginal = resizeImage(context, bmpOriginal, newWidth, newHeigth);

        return bmpOriginal;
    }

    /**
     * Comprime a imagem
     */
    public static void compressImage(Context context, String src) {

        //Comprime a imagem
        OutputStream imagefile = null;
        Bitmap bitmap = getBitmapFile(src);
        bitmap = resizeImage(context, bitmap, 400);

        //Padroniza posição horizontal
        if( bitmap.getWidth() < bitmap.getHeight() ) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        //bitmap.recycle();
        //bitmap = null;
        try {
            imagefile = new FileOutputStream(src);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, imagefile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * Retorna o bitmap da imagem local
     */
    public static Bitmap getBitmapFile(String src) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(src, options);
        return bitmap;
    }

    public static LinkedHashMap<Integer, String> sortMap(
            HashMap<Integer, String> passedMap) {
        List<Integer> mapKeys = new ArrayList<>(passedMap.keySet());
        List<String> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<Integer, String> sortedMap =
                new LinkedHashMap<>();

        Iterator<String> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            String val = valueIt.next();
            Iterator<Integer> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Integer key = keyIt.next();
                String comp1 = passedMap.get(key);
                String comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Retorna o usuário da sessão
     */
    public static UserBean getUserSession(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        if( sharedpreferences != null ) {
            Integer userId = sharedpreferences.getInt("id", 0);
            String userName = sharedpreferences.getString("name", "");
            String userEmail = sharedpreferences.getString("email", "");

            if( userId == 0 ) {
                return null;
            }

            //Monta o UserBean
            UserBean userBean = new UserBean();
            userBean.setId(userId);
            userBean.setName(userName);
            userBean.setEmail(userEmail);

            return userBean;
        } else {
            return null;
        }
    }

    /**
     * Recupera a largura da tela
     */
    public static int getScreenWidth() {
        Display display = App.MAIN_ACTIVITY.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    /**
     * Envia uma requisição HTTP
     */
    public static String requestHttp(String targetURL, String method)
    {
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(method); // hear you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
            connection.connect();

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream ());
            //wr.writeBytes(jsonParam.toString());
            wr.flush();
            wr.close ();

            InputStream is;
            int response = connection.getResponseCode();
            if (response >= 200 && response <=399){
                InputStream inputStream = connection.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder rs = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    rs.append(line);
                }
                //return is = connection.getInputStream();
                return rs.toString();
            } else {
                //return is = connection.getErrorStream();
                return null;
            }


        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Boolean isWifi() {
        ConnectivityManager connManager = (ConnectivityManager) App.MAIN_ACTIVITY.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static void setSessionString(String key, String value) {
        SharedPreferences sharedpreferences = App.MAIN_ACTIVITY.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getSessionString(String key) {
        SharedPreferences sharedpreferences = App.MAIN_ACTIVITY.getSharedPreferences(key, Context.MODE_PRIVATE);
        String value = sharedpreferences.getString(key, "");
        return value;
    }

    public static Boolean copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e) {
            return false;
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

        //Seta a permissão
        Runtime.getRuntime().exec("chmod -R 777 " + dst.getAbsolutePath());

        return true;
    }

    public static void moveFile(File src, File dst) {
        try {
            copyFile(src, dst);
            if( dst.exists() ) {
                src.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log
     */
    public static void log(String msg) {
        Log.i("CHECKIN", msg);
    }

    /**
     * Log de erro
     */
    public static void logError(String msg) {
        Log.e("CHECKIN", msg);
    }

    /**
     * Aguarda um tempo
     */
    public static void sleep(Integer delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
