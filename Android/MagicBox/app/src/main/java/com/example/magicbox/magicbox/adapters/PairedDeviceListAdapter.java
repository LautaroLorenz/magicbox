package com.example.magicbox.magicbox.adapters;

import java.util.List;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.magicbox.magicbox.R;

public class PairedDeviceListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;
    private OnConnectClickListener mListener;

    public PairedDeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public void setListener(OnConnectClickListener listener) {
        mListener = listener;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_paired_device, null);

            holder = new ViewHolder();

            holder.nameTv = (TextView) convertView.findViewById(R.id.paired_device_name);
            holder.addressTv = (TextView) convertView.findViewById(R.id.paired_device_adress);
            holder.connectBtn = (Button) convertView.findViewById(R.id.btn_conectar);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device	= mData.get(position);

        holder.nameTv.setText(device.getName());
        holder.addressTv.setText(device.getAddress());
        holder.connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onConnectClickListener(position);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv;
        TextView addressTv;
        TextView connectBtn;
    }

    public interface OnConnectClickListener {
        public abstract void onConnectClickListener(int position);
    }
}