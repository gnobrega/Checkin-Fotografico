package layout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.plux.checkinfotografico.App;
import br.com.plux.checkinfotografico.DataBase;
import br.com.plux.checkinfotografico.MainActivity;
import br.com.plux.checkinfotografico.R;
import br.com.plux.checkinfotografico.bean.LocationBean;
import br.com.plux.checkinfotografico.custom.CustomAdpter;

public class Location extends Fragment {
    ViewGroup rootView;
    LinkedHashMap<Integer, String> mapLocations;
    LinkedHashMap<Integer, String> mapLocationsBkp;
    ViewStub viewStub;
    MainActivity mainActivity;
    Context context;

    public static Fragment newInstance(Context context) {
        Location f = new Location();
        f.context = context;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_location, null);

        //Alimenta a lista de pontos
        populateLocation();

        return rootView;
    }

    /**
     * Alimenta a lista de pontos
     */
    public void populateLocation() {
        final ListView listLocation = (ListView) rootView.findViewById(R.id.listLocation);

        //Seta o título da tela
        getActivity().setTitle(getString(R.string.title_location));

        //Alimenta o adaptador da listView
        mapLocations = new LinkedHashMap<Integer, String>();
        mapLocationsBkp = new LinkedHashMap<Integer, String>();

        //Carrega os pontos do banco de dados
        DataBase db = new DataBase(getActivity());
        ArrayList<LocationBean> aLstLocations = db.loadLocations();
        for( int i = 0; i < aLstLocations.size(); i ++ ) {
            LocationBean locationBean = aLstLocations.get(i);
            if( locationBean != null ) {
                mapLocations.put(locationBean.getId(), locationBean.getName());
                mapLocationsBkp.put(locationBean.getId(), locationBean.getName());
            }
        }

        //Cria o adaptador
        CustomAdpter customAdapter = new CustomAdpter(mapLocations, R.layout.listview_location);
        listLocation.setAdapter(customAdapter);

        //Evento click
        listLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map.Entry<Integer, String> locationEntry = (Map.Entry<Integer, String>)parent.getItemAtPosition(position);

                //Armazena o ponto na sessão
                SharedPreferences sharedpreferences = getActivity().getSharedPreferences("location", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("id", locationEntry.getKey());
                editor.putString("name", locationEntry.getValue());
                editor.commit();

                //Redireciona para a tela de checkin
                NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(App.MENU_CHECKIN_INDEX).setChecked(true);
                MainActivity activity = (MainActivity) getActivity();
                activity.onNavigationItemSelected(navigationView.getMenu().getItem(App.MENU_CHECKIN_INDEX));

                Checkin.currentGrid = -1;
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

                mapLocations.clear();
                for ( Map.Entry<Integer, String> entry : mapLocationsBkp.entrySet() ) {
                    Integer key = entry.getKey();
                    String value = entry.getValue();
                    if( value.toLowerCase().indexOf(s.toString().toLowerCase()) != -1 ) {
                        mapLocations.put(key, value);
                    }
                }
                CustomAdpter customAdapter = new CustomAdpter(mapLocations, R.layout.listview_location);
                listLocation.setAdapter(customAdapter);
                listLocation.deferNotifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
