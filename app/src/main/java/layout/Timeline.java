package layout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.plux.checkinfotografico.App;
import br.com.plux.checkinfotografico.DataBase;
import br.com.plux.checkinfotografico.R;
import br.com.plux.checkinfotografico.Util;
import br.com.plux.checkinfotografico.bean.StationBean;
import br.com.plux.checkinfotografico.custom.CustomAdpter;

public class Timeline extends Fragment {
    ViewGroup rootView;
    LinkedHashMap<Integer, String> mapStations;
    LinkedHashMap<Integer, String> mapStationsBkp;
    Context context;
    Integer locationId = null;

    public static Fragment newInstance(Context context) {
        Timeline f = new Timeline();
        f.context = context;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_timeline, null);

        //Alimenta a lista de pontos
        populateStation();

        return rootView;
    }

    /**
     * Alimenta a lista de pontos
     */
    public void populateStation() {
        final ListView listStation = (ListView) rootView.findViewById(R.id.listStation);

        //Seta o título da tela
        getActivity().setTitle(getString(R.string.title_timeline));

        //Recupera os dados da localização
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("location", Context.MODE_PRIVATE);
        Integer locationId = sharedpreferences.getInt("id", 0);
        if( sharedpreferences != null && locationId != 0 ) {
            this.locationId = locationId;
        }

        //Alimenta o adaptador da listView
        mapStations = new LinkedHashMap<Integer, String>();
        mapStationsBkp = new LinkedHashMap<Integer, String>();

        //Carrega os pontos do banco de dados
        DataBase db = new DataBase(getActivity());
        ArrayList<StationBean> aLstStations = db.loadStatoins();
        for( int i = 0; i < aLstStations.size(); i ++ ) {
            StationBean stationBean = aLstStations.get(i);
            if( stationBean != null && (this.locationId == null || this.locationId == stationBean.getLocationId()) ) {
                mapStations.put(stationBean.getId(), stationBean.getText());
                mapStationsBkp.put(stationBean.getId(), stationBean.getText());
            }
        }

        //Cria o adaptador
        CustomAdpter customAdapter = new CustomAdpter(mapStations, R.layout.listview_location);
        listStation.setAdapter(customAdapter);

        //Evento click
        listStation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map.Entry<Integer, String> locationEntry = (Map.Entry<Integer, String>)parent.getItemAtPosition(position);

                //Executa a troca da timeline
                changeTimeline();
            }
        });

        //Filtro da lista
        EditText filterText = (EditText) rootView.findViewById(R.id.textFilterLocation);
        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mapStations.clear();
                for ( Map.Entry<Integer, String> entry : mapStationsBkp.entrySet() ) {
                    Integer key = entry.getKey();
                    String value = entry.getValue();
                    if( value.toLowerCase().indexOf(s.toString().toLowerCase()) != -1 ) {
                        mapStations.put(key, value);
                    }
                }
                CustomAdpter customAdapter = new CustomAdpter(mapStations, R.layout.listview_location);
                listStation.setAdapter(customAdapter);
                listStation.deferNotifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Exibe a timeline de publicidade
     */
    private void changeTimeline() {

        //Permite o acesso à interface através de Thread
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if( msg.obj != null ) {
                    Util.toast(App.MAIN_ACTIVITY.getApplicationContext(), (String)msg.obj);
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String userPass = App.DS_USER + "," + App.DS_PASS;
                    byte[] data = userPass.getBytes("UTF-8");
                    String dsPass64 = Base64.encodeToString(data, Base64.DEFAULT).replace("=", ".").replace("\n", "");
                    String url = "https://" + App.DS_DOMAIN + "/WebService/sendCommand.ashx?i_userpass=" + dsPass64 + "&i_stationId=" + locationId  + "&i_command=event" + "&i_param1=checking&i_param2=1&callback=";
                    String resp = Util.requestHttp(url, "GET").replace("(", "").replace(")", "");
                    JSONObject jsonResp = new JSONObject(resp);
                    String ret = jsonResp.getString("ret");
                    Message message = new Message();
                    if( ret.equals("success") ) {
                        message.obj = new String("Sucesso!");
                        handler.sendMessage(message);
                    } else {
                        message.obj = new String("Falhou!");
                        handler.sendMessage(message);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
