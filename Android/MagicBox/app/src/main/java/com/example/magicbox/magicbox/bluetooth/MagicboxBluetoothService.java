package com.example.magicbox.magicbox.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MagicboxBluetoothService extends Thread {

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;
    private BluetoothSocket btSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    private Handler handlerBluetoothIn;
    private int handlerState = 0;
    private String item;
    private boolean finalizado = false;

        public MagicboxBluetoothService(String deviceAddress, final String item) {
            this.item = item;

            btAdapter = BluetoothAdapter.getDefaultAdapter();

            btDevice = btAdapter.getRemoteDevice(deviceAddress);
            try {
                btSocket = btDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
            }
            catch (IOException e) {
                Log.i("SOCKET","fallo dispositivo "+ item);
            }
            try {
                btSocket.connect();
                Log.i("SOCKET","conectado "+ item);
            }
            catch (IOException e) {
                Log.i("SOCKET","fallo " + item + " " + e.toString());
            }

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = btSocket.getInputStream();
                tmpOut = btSocket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void setHandler(Handler handlerBluetoothIn) {
            this.handlerBluetoothIn = handlerBluetoothIn;
        }

        public  void setItem(String item) {
                this.item = item;
        }

        public void run() {
            Log.i("THREAD", "Iniciado " + item);
            byte[] buffer = new byte[256];
            int bytesLeidos = 0;
            int bytesTotales = 0;
            StringBuilder readMessage = new StringBuilder();

            while (!finalizado) {
                try {

                    while(readMessage.indexOf("|") == -1) {
                        bytesLeidos = mmInStream.read(buffer);
                        bytesTotales += bytesLeidos;
                        readMessage.append(new String(buffer, 0, bytesLeidos));
                    }

                    if(readMessage.indexOf("|") != readMessage.length() - 1) {
                        while ((mmInStream.read(buffer)) != -1) {
                        }
                    }

                    Log.i("SERVICE " + item, readMessage + "");
                    handlerBluetoothIn.obtainMessage(handlerState, bytesTotales, -1, readMessage.toString()).sendToTarget();
                    readMessage = new StringBuilder();
                    buffer = new byte[256];
                    bytesTotales = 0;

                } catch (IOException e) {
                }
            }

            try {
                this.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }

        public void write(byte[] input) {
            try {
                Log.i("SERVICE " + item, "Escribiendo bluetooth");
                mmOutStream.write(input);
            } catch (IOException e) {
            }
        }
}
