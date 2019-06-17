package com.example.magicbox.magicbox.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;


import android.widget.ListView;

import com.example.magicbox.magicbox.PairedDeviceListAdapter;
import com.example.magicbox.magicbox.R;

public class PairedDeviceListActivity extends Activity
{
    private ListView mListView;
    private PairedDeviceListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_devices);

        //defino los componentes de layout
        mListView = (ListView) findViewById(R.id.lv_paired);

        //obtengo por medio de un Bundle del intent la lista de dispositivos encontrados
        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");

        //defino un adaptador para el ListView donde se van mostrar en la activity los dispositovs encontrados
        mAdapter = new PairedDeviceListAdapter(this);

        //asocio el listado de los dispositovos pasado en el bundle al adaptador del Listview
        mAdapter.setData(mDeviceList);

        mAdapter.setListener(listenerPairedDeviceClick);
        mListView.setAdapter(mAdapter);
    }

    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    private PairedDeviceListAdapter.OnConnectClickListener listenerPairedDeviceClick = new PairedDeviceListAdapter.OnConnectClickListener() {
        @Override
        public void onConnectClickListener(int position) {
            BluetoothDevice device = mDeviceList.get(position);
            Intent intent = new Intent(PairedDeviceListActivity.this, ProductListActivity.class);
            intent.putExtra("deviceName", device.getName());
            intent.putExtra("deviceAddress", device.getAddress());
            startActivity(intent);
        }
    };


}


