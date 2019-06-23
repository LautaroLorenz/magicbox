package com.example.magicbox.magicbox.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;


import android.widget.ListView;

import com.example.magicbox.magicbox.adapters.PairedDeviceListAdapter;
import com.example.magicbox.magicbox.R;

public class PairedDeviceListActivity extends Activity {
    private ListView mListView;
    private PairedDeviceListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_devices);

        mListView = (ListView) findViewById(R.id.lv_paired);

        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");

        mAdapter = new PairedDeviceListAdapter(this);

        mAdapter.setData(mDeviceList);

        mAdapter.setListener(listenerPairedDeviceClick);
        mListView.setAdapter(mAdapter);
    }

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


