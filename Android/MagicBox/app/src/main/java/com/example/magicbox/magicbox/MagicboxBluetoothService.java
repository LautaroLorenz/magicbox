package com.example.magicbox.magicbox;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MagicboxBluetoothService extends Thread{

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter btAdapter;
        private BluetoothDevice btDevice;
        private BluetoothSocket btSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private Handler handlerBluetoothIn;
        private int handlerState = 0;


    //Constructor de la clase del hilo secundario
        public MagicboxBluetoothService(String deviceAddress, Handler handlerBluetoothIn)
        {
            this.handlerBluetoothIn = handlerBluetoothIn;

            btAdapter = BluetoothAdapter.getDefaultAdapter();
            btDevice = btAdapter.getRemoteDevice(deviceAddress);

            try
            {
                btSocket = btDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
            }
            catch (IOException e)
            {
                // catch exception
            }
            // Establish the Bluetooth socket connection.
            try
            {
                btSocket.connect();
            }
            catch (IOException e)
            {
                try
                {
                    btSocket.close();
                }
                catch (IOException e2)
                {
                    //insert code to deal with this
                }
            }


            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = btSocket.getInputStream();
                tmpOut = btSocket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;
            int cantMensajes = 0;
            StringBuilder readMessage = new StringBuilder();
            //el hilo secundario se queda esperando mensajes del HC05
            while (true)
            {


                try
                {
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    cantMensajes++;
                    readMessage.append(new String(buffer, 0, bytes));
                    Log.i("SERVICIO", readMessage + " " + bytes);

                    if (cantMensajes == 2) {
                        handlerBluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage.toString()).sendToTarget();
                        cantMensajes = 0;
                        readMessage = new StringBuilder();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
            }
        }
}
