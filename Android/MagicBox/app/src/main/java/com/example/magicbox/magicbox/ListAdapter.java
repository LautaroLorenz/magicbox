package com.example.magicbox.magicbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.magicbox.magicbox.models.Product;

import java.util.List;

public class ListAdapter extends ArrayAdapter<Product> {

    private int resourceLayout;
    private Context mContext;

    public ListAdapter(Context context, int resource, List<Product> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        Product p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.productlist_item_name);
            TextView tt2 = (TextView) v.findViewById(R.id.productlist_item_peso);
            TextView tt3 = (TextView) v.findViewById(R.id.productlist_item_temperaturaIdeal);

            if (tt1 != null) {
                tt1.setText(p.getName());
            }

            if (tt2 != null) {
                tt2.setText(p.getPeso());
            }

            if (tt3 != null) {
                tt3.setText(p.getTemperaturaIdeal());
            }
        }

        return v;
    }

}