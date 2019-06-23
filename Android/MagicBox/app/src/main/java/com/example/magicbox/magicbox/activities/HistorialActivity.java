package com.example.magicbox.magicbox.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.magicbox.magicbox.R;
import com.example.magicbox.magicbox.models.HistorialItem;
import com.example.magicbox.magicbox.adapters.HistorialListAdapter;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private ListView historialListView;
    private HistorialListAdapter historialListAdapter;

    private List<HistorialItem> listaHistorial;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        historialListView = (ListView) findViewById(R.id.listView_historial);

        listaHistorial = (ArrayList<HistorialItem>) getIntent().getExtras().get("lista-historial");

        historialListAdapter = new HistorialListAdapter(this, R.layout.list_item_historial, listaHistorial);
        historialListView.setAdapter(historialListAdapter);
    }

}
