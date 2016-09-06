package layout;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.plux.checkinfotografico.DataBase;
import br.com.plux.checkinfotografico.ImageItem;
import br.com.plux.checkinfotografico.MainActivity;
import br.com.plux.checkinfotografico.R;
import br.com.plux.checkinfotografico.bean.CampaignBean;
import br.com.plux.checkinfotografico.custom.CustomAdpter;

/**
 * Created by gustavonobrega on 28/06/2016.
 */
public class CampaignDialog extends DialogFragment {
    ViewGroup rootView;
    ListView listCampaign;
    LinkedHashMap<Integer, String> mapCampaigns;
    LinkedHashMap<Integer, String> mapCampaignsBkp;
    MainActivity mainActivity = null;

    public CampaignDialog() {
        //this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_campaign, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        //Popula a lista
        populateCampaign();

        return rootView;
    }

    /**
     * Alimenta a lista de pontos
     */
    public void populateCampaign() {
        listCampaign = (ListView) rootView.findViewById(R.id.listCampaign);
        final CampaignDialog parentDialog = this;

        //Alimenta o adaptador da listView
        mapCampaigns = new LinkedHashMap<Integer, String>();
        mapCampaignsBkp = new LinkedHashMap<Integer, String>();

        //Carrega campanhas do banco de dados
        DataBase db = new DataBase(getActivity());
        ArrayList<CampaignBean> aLstCampaigns = db.loadCampaigns();
        for( int i = 0; i < aLstCampaigns.size(); i ++ ) {
            CampaignBean campaignBean = aLstCampaigns.get(i);
            if( campaignBean != null ) {
                mapCampaigns.put(campaignBean.getId(), campaignBean.getName());
                mapCampaignsBkp.put(campaignBean.getId(), campaignBean.getName());
            }
        }

        //Cria o adaptador
        CustomAdpter customAdapter = new CustomAdpter(mapCampaigns, R.layout.listview_campaign);
        listCampaign.setAdapter(customAdapter);
        listCampaign.deferNotifyDataSetChanged();

        //Evento click
        listCampaign.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map.Entry<Integer, String> locationEntry = (Map.Entry<Integer, String>) parent.getItemAtPosition(position);

                //Id da campanha selecionada
                Integer campaignId = locationEntry.getKey();
                String campaignName = locationEntry.getValue();

                //Recupera as imagens selecionadas
                HashMap<String, ImageItem> listItens = Checkin.getItensSelected(getActivity());

                //Inicia a conexão com o banco
                DataBase db = new DataBase(getContext());

                //Seta a campanha dos itens
                for (String tagId : listItens.keySet()) {
                    if (listItens.containsKey(tagId)) {
                        ImageItem imageItem = listItens.get(tagId);
                        Integer lastCampaign = imageItem.getCampaignId();
                        imageItem.setCampaignId(campaignId);
                        imageItem.setCampaignName(campaignName);
                        imageItem.showImgStar();

                        //Atualiza o registro no banco
                        if (imageItem.getIdDb() != null) {
                            db.setCampaignPhoto(campaignId, campaignName, imageItem.getIdDb());
                        }
                    }
                }

                //Oculta o dialog
                parentDialog.dismiss();
            }
        });

        //Filtro da lista
        EditText filterText = (EditText) rootView.findViewById(R.id.textFilterCampaign);
        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mapCampaigns.clear();
                for (Map.Entry<Integer, String> entry : mapCampaignsBkp.entrySet()) {
                    Integer key = entry.getKey();
                    String value = entry.getValue();
                    if (value.toLowerCase().indexOf(s.toString().toLowerCase()) != -1) {
                        mapCampaigns.put(key, value);
                    }
                }
                CustomAdpter customAdapter = new CustomAdpter(mapCampaigns, R.layout.listview_campaign);
                listCampaign.setAdapter(customAdapter);
                listCampaign.deferNotifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
