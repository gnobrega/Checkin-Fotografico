package br.com.plux.checkinfotografico;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;


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

    public static String get(final String sUrl){
        final String[] resp = {null};

        RequestQueue queue = Volley.newRequestQueue(App.context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, sUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        resp[0] = response;
                        Util.log("Request response: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Util.logError("Erro na requisição: " + sUrl);
                resp[0] = "";
            }
        });
        queue.add(stringRequest);

        //Aguarda a resposta
        while (resp[0] == null) {
            Util.sleep(1000);
        }
        return resp[0];
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
