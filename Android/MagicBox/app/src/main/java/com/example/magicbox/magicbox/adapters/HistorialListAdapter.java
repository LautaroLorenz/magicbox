package com.example.magicbox.magicbox.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.magicbox.magicbox.R;
import com.example.magicbox.magicbox.models.HistorialItem;

import java.util.List;

public class HistorialListAdapter extends ArrayAdapter<HistorialItem> {

    private int resourceLayout;
    private Context mContext;

    public HistorialListAdapter(Context context, int resource, List<HistorialItem> items) {
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

        HistorialItem p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.historial_item_timestamp);
            TextView tt2 = (TextView) v.findViewById(R.id.historial_item_medicion);

            if (tt1 != null) {
                tt1.setText(p.getTimestamp());
            }

            if (tt2 != null) {
                tt2.setText(p.getMedicion());
            }
        }

        return v;
    }
}
