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
import java.util.ArrayList;
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
        setStatus("Verificando a conexão com a internet...");

        //Verifica se tem conexão com a internet
        if (Connection.checkConnection(getActivity().getApplicationContext())) {

            //Sincroniza os dados
            syncData();

            //Sincroniza as fotos
            syncPhotos();
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
                SharedPreferences sharedpreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                Integer userId = sharedpreferences.getInt("id", 0);

                //Busca a lista de pontos no servidor
                String sResp = Connection.get(App.SERVER_GET_ROUTE + "/" + userId + ".json");
                if (sResp != null) {

                    try {
                        //Limpa a base local de pontos e campanhas
                        DataBase db = new DataBase(getActivity().getApplicationContext());
                        db.clearLocations();
                        db.clearCampaigns();
                        JSONObject jResp = null;

                        //Pontos
                        jResp = new JSONObject(sResp);
                        JSONObject jData = (JSONObject) jResp.get("data");
                        JSONArray aLocations = (JSONArray) jData.get("locations");
                        for (int i = 0; i < aLocations.length(); i++) {
                            JSONObject jLocation = aLocations.getJSONObject(i);
                            int locationId = jLocation.getInt("id");
                            String locationName = jLocation.getString("name");
                            int locationIdRoute = jLocation.getInt("id_route");

                            //Insere no banco
                            db.insertLocation(locationId, locationName, locationIdRoute);
                        }

                        //Campanhas
                        if( !jData.get("campaigns").getClass().getName().equals("org.json.JSONObject") ) {
                            JSONArray aCampaigns = (JSONArray) jData.get("campaigns");
                            for (int i = 0; i < aCampaigns.length(); i++) {
                                JSONObject jCampaign = aCampaigns.getJSONObject(i);
                                int campaignId = jCampaign.getInt("id");
                                String campaignName = jCampaign.getString("campanha");

                                //Insere no banco
                                db.insertCampaign(campaignId, campaignName);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    public void setStatus(String msg) {
        if( Looper.myLooper() == Looper.getMainLooper() ) {
            TextView syncStatus = (TextView) rootView.findViewById(R.id.syncStatus);
            syncStatus.setText(msg);
        }
    }

    public void syncPhotos() {
        final Sync thisObj = this;

        //Status
        setStatus("Sincronizando as fotografias...");

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

                    sendProgress(progress);

                }
            }
        };

        //Executa a atividade em background
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Recarrega as imagens do banco de dados
                Checkin.loadPhotoDb(getContext(), null);

                //Recupera o usuario da sessao
                UserBean user = Util.getUserSession(getContext());

                //Recupera as fotografias
                S3Client s3 = new S3Client();

                //Calcula o numero de fotos
                for( Integer locationId : listImages.keySet() ) {
                    HashMap<String, ImageItem> listImageItem = listImages.get(locationId);
                    for( String imageKey : listImageItem.keySet() ) {
                        ImageItem imageItem = listImageItem.get(imageKey);
                        File file = new File(imageItem.getRealFile());
                        if( file.exists() && imageItem.getCampaignId() != null ) {
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
                        if( file.exists() && imageItem.getCampaignId() != null ) {
                            fotoAtual ++;
                            sendMessage("Enviando " + fotoAtual + " de " + totalFotos, handler);
                            String s3FileKey = "photos"  + "/campaign_" + imageItem.getCampaignId() + "/location_" + locationId + "/user_" + user.getId() + "_" + file.getName();

                            //Executa o upload
                            s3.upload(file, s3FileKey, handlerProgress, thisObj.context, imageKey);
                        }
                    }

                    //Remove a lista de imagens sincronizadas
                    /*for( String imageKey : removeImages ) {
                        ImageItem imageItem = listImageItem.get(imageKey);
                        if( imageItem != null ) {
                            imageItem.remover(getActivity());
                        }
                    }*/
                }

                //Fim da sincronia
                sendMessage("Sincronia finalizada", handler);
            }
        }).start();


    }

    void sendMessage(String msg, Handler handler) {
        Message message = new Message();
        message.obj = msg;
        handler.sendMessage(message);
    }

    void sendProgress(Long val) {
        if( Looper.myLooper() == Looper.getMainLooper() ) {
            ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.syncProgress);
            bar.setProgress(val.intValue());

            if( totalFotos == fotoAtual && val == 100 ) {
                setStatus("Sincronia finalizada");
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