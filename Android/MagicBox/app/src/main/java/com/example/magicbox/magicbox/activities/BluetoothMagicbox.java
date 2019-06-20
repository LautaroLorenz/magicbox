package com.example.magicbox.magicbox.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothMagicbox {

    public static final String GET_ESTADO = "E";
    public static final String APAGAR_BUZZER = "B";
    public static final String SET_TEMPERATURA_14 = "r"; // d - } ASCII 100 - 125 (Rango 0 a 25)
    public static final String GET_VOLUMEN = "V";
    public static final String GET_PESO = "P";
    public static final String GET_TEMPERATURA = "T";
    public static final String GET_ALL = "S";

    // TAMAÑO EN BYTES DE LAS RESPUESTAS
    public static final int VOLUMEN_SIZE = 10;
    public static final int PESO_SIZE = 10;
    public static final int TEMPERATURA_SIZE = 10;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean socketBusy = false;

    private BluetoothAdapter btAdapter;
    private BluetoothDevice device;
    private BluetoothSocket btSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    String deviceAddress;
    private boolean connected;

    public BluetoothMagicbox(String deviceAddress) throws IOException {
        this.deviceAddress = deviceAddress;
        this.connected = false;
    }

    public void conectar() throws IOException {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        device = btAdapter.getRemoteDevice(deviceAddress);

        btSocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        btSocket.connect();

        mmInStream = btSocket.getInputStream();
        mmOutStream =  btSocket.getOutputStream();

        connected = true;
    }

    public boolean isBusy() {
        return this.socketBusy;
    }

    public String read(int bytesToRead) {
        int bytesLeidos = 0;

        byte[] buffer = new byte[bytesToRead];

        try {
            bytesLeidos = mmInStream.read(buffer);
            //mmInStream.skip(20);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String readMessage = new String(buffer, 0, bytesLeidos);

        //return bytesLeidos > bytesToRead? readMessage : null;
        return readMessage;
    }

    public void skip(long nBytes) {
        try {
            mmInStream.skip(nBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String input) throws IOException {

        byte[] msgBuffer = input.getBytes();
        try {
            mmOutStream.write(msgBuffer);
        } catch (IOException e) {
            throw new IOException(e);
        }
        finally {
            this.socketBusy = false;
        }
    }

    public void close() {
        try {
            if (mmOutStream != null && mmInStream != null && btSocket != null) {
                mmOutStream.close();
                mmInStream.close();
                btSocket.close();
                connected = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean isConnected() {
        return connected;
    }
}
