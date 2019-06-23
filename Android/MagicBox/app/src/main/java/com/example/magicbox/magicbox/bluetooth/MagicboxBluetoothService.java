package com.example.magicbox.magicbox.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

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
                //Log.i("SOCKET","fallo dispositivo "+ item);
            }
            try {
                btSocket.connect();
                //Log.i("SOCKET","conectado "+ item);
            }
            catch (IOException e) {
                //Log.i("SOCKET","fallo " + item + " " + e.toString());
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
            //Log.i("THREAD", "Iniciado " + item);
            byte[] buffer = new byte[256];
            int bytes;
            int cantMensajes = 0;
            StringBuilder readMessage = new StringBuilder();

            while (!finalizado) {
                try {
                    bytes = mmInStream.read(buffer);
                    cantMensajes++;
                    readMessage.append(new String(buffer, 0, bytes));
                    //Log.i("SERVICE", readMessage + " " + item);
                    if (cantMensajes == 2) {

                        if(readMessage.indexOf("|") != readMessage.length() - 1) {
                            while ((mmInStream.read(buffer)) != -1) {
                            }
                        }

                        handlerBluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage.toString()).sendToTarget();
                        cantMensajes = 0;
                        readMessage = new StringBuilder();
                    }
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
                mmOutStream.write(input);
            } catch (IOException e) {
            }
        }
}
