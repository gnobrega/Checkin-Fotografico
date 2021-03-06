package layout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import br.com.plux.checkinfotografico.App;
import br.com.plux.checkinfotografico.Connection;
import br.com.plux.checkinfotografico.DataBase;
import br.com.plux.checkinfotografico.ImageItem;
import br.com.plux.checkinfotografico.R;
import br.com.plux.checkinfotografico.S3Client;
import br.com.plux.checkinfotografico.Util;
import br.com.plux.checkinfotografico.bean.UserBean;

public class Sync extends Fragment {
    ViewGroup rootView;
    Context context;
    LinearLayout syncContainer;
    int totalFotos = 0;
    int fotoAtual = 0;
    private HashMap<Integer, HashMap> listImages = Checkin.listImages;
    public static Boolean syncPhotosFinished = false;

    public static Fragment newInstance(Context context) {
        Sync f = new Sync();
        f.context = context;

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_sync, null);
        syncContainer = (LinearLayout)rootView.findViewById(R.id.syncContent);

        //Seta o título da tela
        getActivity().setTitle("Sincronia");

        //Mantém a comunicação com a interface
        setStatus("\nVerificando a conexão com a internet...");

        //Verifica se tem conexão com a internet
        if (Connection.checkConnection(getActivity().getApplicationContext())) {

            //Sincroniza os dados
            syncData();

            //Sincroniza as fotos
            if ( Util.isWifi() ) {
                syncPhotos();
            } else {
                Util.toast(App.MAIN_ACTIVITY.getApplicationContext(), "As fotos só serão sincronizadas em uma conexão wifi");
            }
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View myView = getView();

        myView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                //Se as dimensões do Fragment
                syncContainer.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                syncContainer.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
                syncContainer.requestLayout();
            }
        });
    }

    /**
     * Sincroniza os dados com a base local
     */
    public void syncData() {

        //Permite o acesso à interface através de Thread
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                setStatus((String) msg.obj);
            }
        };

        //Executa a atividade em background
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Recupera o usuário da sessão
                SharedPreferences sharedpreferences = App.MAIN_ACTIVITY.getSharedPreferences("user", Context.MODE_PRIVATE);
                Integer userId = sharedpreferences.getInt("id", 0);

                //Busca a lista de pontos no servidor
                sendMessage("\nBaixando informações do servidor...", handler);
                String urlSync = App.SERVER_GET_ROUTE + "/id_usuario/" + userId + ".json?nocache=" + Math.random();
                String sResp = Connection.get(urlSync);
                Util.log(App.SERVER_GET_ROUTE + "/" + userId + ".json?+nocache=" + Math.random());
                if (sResp != null) {

                    try {
                        //Limpa a base local de pontos e campanhas
                        DataBase db = new DataBase(App.MAIN_ACTIVITY.getApplicationContext());
                        db.clearLocations();
                        db.clearCampaigns();
                        db.clearStations();
                        JSONObject jResp = null;

                        //Pontos
                        jResp = new JSONObject(sResp);
                        JSONObject jData = (JSONObject) jResp.get("data");
                        JSONArray aLocations = (JSONArray) jData.get("locations");
                        for (int i = 0; i < aLocations.length(); i++) {
                            JSONObject jLocation = aLocations.getJSONObject(i);
                            int locationId = jLocation.getInt("id");
                            String locationName = jLocation.getString("name");
                            //locationName = new String(locationName.getBytes("ISO-8859-1"));
                            int locationIdRoute = jLocation.getInt("id_route");
                            String locationLatitude = jLocation.getString("latitude");
                            String locationLongitude = jLocation.getString("longitude");

                            //Insere no banco
                            db.insertLocation(locationId, locationName, locationIdRoute, locationLatitude, locationLongitude);
                        }
                        sendMessage("\nSincronia de localizações - SUCESSO", handler);

                        //Campanhas
                        if( !jData.get("campaigns").getClass().getName().equals("org.json.JSONObject") ) {
                            JSONArray aCampaigns = (JSONArray) jData.get("campaigns");
                            for (int i = 0; i < aCampaigns.length(); i++) {
                                JSONObject jCampaign = aCampaigns.getJSONObject(i);
                                Integer campaignId = jCampaign.getInt("id");
                                if( campaignId != null ) {
                                    String campaignName = jCampaign.getString("name");

                                    //Insere no banco
                                    db.insertCampaign(campaignId, campaignName);
                                }
                            }
                        }
                        sendMessage("\nSincronia de campanhas - SUCESSO", handler);

                        //Estações
                        if( !jData.get("stations").getClass().getName().equals("org.json.JSONObject") ) {
                            JSONArray aStations = (JSONArray) jData.get("stations");
                            for (int i = 0; i < aStations.length(); i++) {
                                JSONObject jStation = aStations.getJSONObject(i);
                                int stId = jStation.getInt("id");
                                String stationText = jStation.getString("text");
                                int locationId = jStation.getInt("locationId");

                                //Insere no banco
                                db.insertStation(stId, stationText, locationId);
                            }
                        }
                        sendMessage("\nSincronia de estações DS - SUCESSO", handler);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        sendMessage("\nERRO - " + e.getMessage(), handler);
                    }
                }
            }
        }).start();
    }

    public void setStatus(String msg) {
        if( Looper.myLooper() == Looper.getMainLooper() ) {
            TextView syncStatus = (TextView) rootView.findViewById(R.id.syncStatus);
            syncStatus.append(msg);
        }
    }

    public void syncPhotos() {
        final Sync thisObj = this;

        //Permite o acesso à interface através de Thread
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if( msg.obj != null ) {
                    setStatus((String) msg.obj);
                }
            }
        };
        //Permite o acesso à interface através de Thread
        final Handler handlerProgress = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if( msg.obj != null ) {
                    Intent intent = (Intent)msg.obj;
                    Long progress = intent.getLongExtra("progress", Long.valueOf(0));
                    String imageKey = intent.getStringExtra("imageKey");

                    if( imageKey != null) {
                        thisObj.removerItem(imageKey);
                    }

                    sendProgress(progress, imageKey);

                }
            }
        };

        //Executa a atividade em background
        new Thread(new Runnable() {
            @Override
            public void run() {

                Calendar c = Calendar.getInstance();
                SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat curFormater2 = new SimpleDateFormat("yyyy/MM/dd");
                String formattedDate = curFormater.format(c.getTime());
                String formattedDateFolder = curFormater2.format(c.getTime());

                //Recarrega as imagens do banco de dados
                Checkin.loadPhotoDb(App.MAIN_ACTIVITY.getApplicationContext(), null);

                //Recupera o usuario da sessao
                UserBean user = Util.getUserSession(App.MAIN_ACTIVITY.getApplicationContext());

                //Recupera as fotografias
                S3Client s3 = new S3Client();

                //Calcula o numero de fotos
                for( Integer locationId : listImages.keySet() ) {
                    HashMap<String, ImageItem> listImageItem = listImages.get(locationId);
                    for( String imageKey : listImageItem.keySet() ) {
                        ImageItem imageItem = listImageItem.get(imageKey);
                        File file = new File(imageItem.getRealFile());
                        if( file.exists() ) {
                            totalFotos ++;
                        }
                    }
                }

                //Percorre a lista de pontos
                for( Integer locationId : listImages.keySet() ) {
                    HashMap<String, ImageItem> listImageItem = listImages.get(locationId);

                    //Percorre a lista fotografias
                    for( String imageKey : listImageItem.keySet() ) {
                        ImageItem imageItem = listImageItem.get(imageKey);

                        File file = new File(imageItem.getRealFile());
                        if( file.exists() ) {
                            fotoAtual ++;
                            sendMessage("\nEnviando " + fotoAtual + " de " + totalFotos, handler);
                            String s3FileKey = "photos-plux"  + "/location_" + locationId + "/" + formattedDateFolder + "/" + formattedDate + "_user_" + user.getId() + "_"
                                    + "station_" + imageItem.getKeyGrid() + "_" + file.getName();

                            //Executa o upload
                            Util.log("Uploading: " + s3FileKey);
                            s3.upload(file, s3FileKey, handlerProgress, App.MAIN_ACTIVITY.getApplicationContext(), imageKey);
                        }
                    }
                }
            }
        }).start();


    }

    void sendMessage(String msg, Handler handler) {
        Message message = new Message();
        message.obj = msg;
        handler.sendMessage(message);
    }

    private ArrayList<String> fotosUp = new ArrayList<>();
    void sendProgress(Long val, String imageKey) {
        if( Looper.myLooper() == Looper.getMainLooper() ) {
            ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.syncProgress);
            if( val >= 100 ) {
                if( !fotosUp.contains(imageKey) && imageKey != null ) {
                    fotosUp.add(imageKey);

                    //Progresso
                    Integer progress = 100 * fotosUp.size() / totalFotos;
                    bar.setProgress(progress);

                    //Upload finalizado
                    if( fotosUp.size() >= totalFotos && !syncPhotosFinished ) {
                        setStatus("\nSincronia de fotos - SUCESSO");
                        syncPhotosFinished = true;
                    }
                }
            }
        }
    }

    public final void removerItem(String imageKey) {
        for( Integer locationId : listImages.keySet() ) {
            HashMap<String, ImageItem> listImageItem = listImages.get(locationId);

            //Armazena os itens que serão removidos pois não é possível remover no meio da iteração da lista
            ArrayList<String> imagesKeyRemove = new ArrayList<>();

            //Percorre a lista fotografias buscando as que serão removidas
            for (String key : listImageItem.keySet()) {
                imagesKeyRemove.add(key);
            }

            //Executa a exclusão
            for (String key : imagesKeyRemove) {
                if( key.equals(imageKey) ) {
                    ImageItem imageItem = listImageItem.get(imageKey);
                    if (imageItem != null) {
                        imageItem.remover(getActivity());
                    }
                }
            }
        }
    }
}