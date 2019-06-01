package com.example.magicbox.magicbox;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatosBluetooth {

    private static DatosBluetooth obj;
    private BluetoothSocket btSocket;
    private InputStream in;
    private OutputStream out;
    private static final String TAG = "Datos BT";

    public static DatosBluetooth getInstance() {
        if (obj == null) {
            obj = new DatosBluetooth();
        }
        return obj;
    }

    public void setSocket(BluetoothSocket socket) {
        this.btSocket = socket;
    }

    public BluetoothSocket getSocket() {
        return btSocket;
    }

    public void setIn(InputStream in) {
        this.in = in;
    }

    public InputStream getIn() {
        return this.in;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public OutputStream getOut() {
        return this.out = out;
    }

    public  void cerrarConexiones(){
        try{
            btSocket.close();
            in.close();
            out.close();
        }catch(IOException e){
            Log.e(TAG, "ERROR AL CERRAR SOCKETS O STREAMS");
            Log.e(TAG, e.getMessage());
        }
    }

}
