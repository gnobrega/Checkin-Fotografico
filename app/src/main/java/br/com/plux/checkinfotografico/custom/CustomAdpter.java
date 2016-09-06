package br.com.plux.checkinfotografico.custom;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.plux.checkinfotografico.R;
import br.com.plux.checkinfotografico.Util;

/**
 * Created by gustavonobrega on 08/06/2016.
 */
public class CustomAdpter extends BaseAdapter {
    private final ArrayList mData;
    private @LayoutRes int layoutItemId;

    public CustomAdpter(LinkedHashMap<Integer, String> map, @LayoutRes int layoutItemId) {
        map = Util.sortMap(map);
        this.layoutItemId = layoutItemId;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
        System.out.println("teste");
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<Integer, String> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;
        ItemList itemHolder;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(layoutItemId, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<Integer, String> item = getItem(position);

        // TODO replace findViewById by ViewHolder
        itemHolder = new ItemList();
        itemHolder.name = ((TextView) result.findViewById(R.id.itemName));
        itemHolder.id = ((TextView) result.findViewById(R.id.itemId));
        itemHolder.name.setText(item.getValue());
        itemHolder.id.setText(String.valueOf(item.getKey()));

        return result;
    }

    private class ItemList {
        TextView name;
        TextView id;
    }
}
