package br.com.plux.checkinfotografico;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * Created by gustavonobrega on 31/05/2016.
 */
public class Connection {

    public static AppCompatActivity activity;

    /**
     * Verifica se existe conexão
     */
    public static boolean checkConnection(Context ctx) {
        ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null)
            return false;
        if (!i.isConnected())
            return false;
        if (!i.isAvailable())
            return false;
        return true;
    }

    public static String get(String sUrl){

        InputStream is = null;
        int len = 500;

        try {
            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("content-type", "application/json");
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();




            InputStreamReader isw = new InputStreamReader(is, "ISO-8859-1");
            String resp = "";
            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                resp += current;
            }

            return resp;
        } catch (MalformedURLException e) {
            Log.i("Conexão", "Sem conexão com a internet");
            //e.printStackTrace();
        } catch (ProtocolException e) {
            Log.i("Conexão", "Sem conexão com a internet");
            //e.printStackTrace();
        } catch (IOException e) {
            Log.i("Conexão", "Sem conexão com a internet");
            //e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.i("Conexão", "Sem conexão com a internet");
                    //e.printStackTrace();
                }
            }
        }
        return null;
    }

    // Reads an InputStream and converts it to a String.
    public static String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        //reader = new InputStreamReader(stream, "UTF-8");
        reader = new InputStreamReader(stream, "ISO-8859-1");
        char[] buffer = new char[len];
        reader.read(buffer);

        return new String(buffer);
    }



}
