package com.example.magicbox.magicbox.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magicbox.magicbox.asyncTasks.ActualizarTemperaturaTask;
import com.example.magicbox.magicbox.R;
import com.example.magicbox.magicbox.models.Product;
import com.example.magicbox.magicbox.sensores.Giroscopio;
import com.example.magicbox.magicbox.sensores.SensorProximidad;

import java.io.IOException;

public class ProductoActivity extends MainActivity {

    // ------------------------------------------------------------
    //          VIEWS
    // ------------------------------------------------------------
    private TextView pesoView;
    private TextView volumenView;
    private TextView nombreView;
    private TextView temperaturaIdealView;
    private TextView temperaturaView;
    private ImageView imagenView;
    private Button btnCambiarProducto;
    private Button btnProveedores;

    // ------------------------------------------------------------
    //          SENSORES ANDROID
    // ------------------------------------------------------------
    private SensorProximidad sensorProximidad;
    private Giroscopio giroscopio;


    // Tarea en segundo plano para actualizar la temp en tiempo real
    ActualizarTemperaturaTask actualizarTemperaturaTask;


    // ------------------------------------------------------------
    //          PRODUCTO ACTUAL EN EL CONTENEDOR
    // ------------------------------------------------------------
   private Product productoActual;


    // ------------------------------------------------------------
    //          COMUNICACION
    // ------------------------------------------------------------
    BluetoothMagicbox btMagicbox;

    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private ActualizarTemperaturaThread mConnectedThread;

    String address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);

        Bundle bundle = getIntent().getExtras();

        address = bundle.getString("deviceAddress");

        try {
            btMagicbox = new BluetoothMagicbox(address);
        } catch (IOException e) {
            handleBtError(e);
        }

        productoActual = new Product();
        productoActual.setName(bundle.getString("nombre"));
        productoActual.setPeso(bundle.getString("peso"));
        productoActual.setTemperaturaIdeal(bundle.getString("temperaturaIdeal"));
        productoActual.setIdRecursoImagen(bundle.getInt("idRecursoImagen"));
        productoActual.setUrlProveedores(bundle.getString("urlProveedores"));

        imagenView = (ImageView) findViewById(R.id.product_image);
        nombreView = (TextView) findViewById(R.id.product_nombre);
        pesoView = (TextView) findViewById(R.id.product_peso);
        volumenView = (TextView) findViewById(R.id.product_volumen);
        temperaturaIdealView = (TextView) findViewById(R.id.product_temperaturaIdeal);
        temperaturaView = (TextView) findViewById(R.id.text_temperatura);

        btnCambiarProducto = (Button) findViewById(R.id.btnCambiarProducto);
        btnProveedores = (Button) findViewById(R.id.btnProveedores);

        imagenView.setImageResource(productoActual.getIdRecursoImagen());
        nombreView.setText(productoActual.getName());
        pesoView.setText(productoActual.getPeso());
        temperaturaIdealView.setText(productoActual.getTemperaturaIdeal());

        btnCambiarProducto.setOnClickListener(btnVerListadoProductosListener);
        btnProveedores.setOnClickListener(btnVerProveedoresListener);

        //actualizarTemperaturaTask = new ActualizarTemperaturaTask(temperaturaView);
       //actualizarTemperaturaTask.execute();



        sensorProximidad = new SensorProximidad(temperaturaView);
        sensorProximidad.iniciar(this, btMagicbox);



        giroscopio = new Giroscopio(pesoView, volumenView);
        giroscopio.iniciar(this, btMagicbox);



        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout a traves de este handler
        //bluetoothIn = Handler_Msg_Hilo_Principal();
    }


    // ------------------------------------------------------------
    //          LISTENERS DE LOS BOTONES
    // ------------------------------------------------------------
    private View.OnClickListener btnVerListadoProductosListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ProductoActivity.this, ProductListActivity.class);
            intent.putExtra("deviceAddress", address);
            startActivity(intent);
        }
    };

    // TODO: Cambiar la URL del Intent por la urlProveedores del productoActual
    private View.OnClickListener btnVerProveedoresListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com.ar/maps/search/supermercados/@-34.6801812,-58.5658106,15z/data=!3m1!4b1"));
            startActivity(browserIntent);
        }
    };


    // ------------------------------------------------------------
    //          MANEJO DE LA COMUNICACION
    // ------------------------------------------------------------

    @Override
    public void onResume() {
        super.onResume();

        try {
            btMagicbox.conectar();

            // LEER TEMP DEL PRODUCTO Y ENVIARLA POR BLUETOOTH
            btMagicbox.write(BluetoothMagicbox.SET_TEMPERATURA_14);

            // THREAD QUE ACTUALIZA LA VIEW DE LA TEMPERATURA DEL CONTENEDOR
            //mConnectedThread = new ActualizarTemperaturaThread(btMagicbox);
            //mConnectedThread.start();

        } catch (IOException e) {
            handleBtError(e);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if (btMagicbox != null) {
            //btMagicbox.close();
        }
    }

    //Handler que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    //voy concatenando el msj
                    String readMessage = (String) msg.obj;
                    //recDataString.append(readMessage);
                   // int endOfLineIndex = recDataString.indexOf("\r\n");

                    //cuando recibo toda una linea la muestro en el layout
                    //if (endOfLineIndex > 0)
                    //{
                        //String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        temperaturaView.setText(readMessage);

                       // recDataString.delete(0, recDataString.length());
                    //}
                }
            }
        };

    }

    private class ActualizarTemperaturaThread extends Thread {
            private BluetoothMagicbox btMagicbox;

            public ActualizarTemperaturaThread(BluetoothMagicbox btMagicbox) throws IOException {
                    this.btMagicbox = btMagicbox;
            }

            public void run() {
                byte[] buffer = new byte[256];
                int bytes;

                while (true)
                {
                    try {
                        this.btMagicbox.write(btMagicbox.GET_TEMPERATURA);
                    } catch (IOException e) {
                        handleBtError(e);
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //bytes = btMagicbox.read(buffer);
                    //String readMessage = new String(buffer, 0, bytes);

                    //bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();

                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
    }


    public void handleBtError(IOException e) {
        e.printStackTrace();
        showToast("Hubo un error con el bluetooth");
        Intent intent = new Intent(ProductoActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
