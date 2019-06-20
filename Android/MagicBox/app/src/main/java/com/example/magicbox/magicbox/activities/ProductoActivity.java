package com.example.magicbox.magicbox.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magicbox.magicbox.MagicboxBluetoothService;
import com.example.magicbox.magicbox.R;
import com.example.magicbox.magicbox.models.Product;
import com.example.magicbox.magicbox.sensores.Giroscopio;
import com.example.magicbox.magicbox.sensores.SensorProximidad;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProductoActivity extends MainActivity {

    // ------------------------------------------------------------
    //          VIEWS
    // ------------------------------------------------------------
    private TextView pesoView;
    private TextView volumenView;
    private TextView nombreView;
    private TextView temperaturaView;
    private ImageView imagenView;
    private Button btnCambiarProducto;
    private Button btnProveedores;

    // ------------------------------------------------------------
    //          SENSORES ANDROID
    // ------------------------------------------------------------
    private SensorProximidad sensorProximidad;
    private Giroscopio giroscopio;



    // ------------------------------------------------------------
    //          PRODUCTO ACTUAL EN EL CONTENEDOR
    // ------------------------------------------------------------
   private Product productoActual;


    // ------------------------------------------------------------
    //          COMUNICACION
    // ------------------------------------------------------------
    MagicboxBluetoothService btMagicboxService;
    Handler handlerBluetoothIn;
    String address;

    List<String> log;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);

        Bundle bundle = getIntent().getExtras();

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
        temperaturaView = (TextView) findViewById(R.id.text_temperatura);

        btnCambiarProducto = (Button) findViewById(R.id.btnCambiarProducto);
        btnProveedores = (Button) findViewById(R.id.btnProveedores);

        imagenView.setImageResource(productoActual.getIdRecursoImagen());
        nombreView.setText(productoActual.getName());
        pesoView.setText(productoActual.getPeso());

        btnCambiarProducto.setOnClickListener(btnVerListadoProductosListener);
        btnProveedores.setOnClickListener(btnVerProveedoresListener);

        log = new ArrayList<>();

        address = bundle.getString("deviceAddress");

        handlerBluetoothIn = createHandlerBluetooth();
        btMagicboxService = new MagicboxBluetoothService(address, handlerBluetoothIn);

        btMagicboxService.start();

        // Le mando la temperatura que necesita el producto actual del contenedor
        // btMagicboxService.write("d");

        sensorProximidad = new SensorProximidad();
        sensorProximidad.iniciar(this, btMagicboxService);



        giroscopio = new Giroscopio(pesoView, volumenView);
        giroscopio.iniciar(this, btMagicboxService);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //Handler que permite mostrar datos en el Layout al hilo secundario
    private Handler createHandlerBluetooth () {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == 0)
                {
                    String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                    String readMessage = (String) msg.obj;
                    Log.i("DATOS", readMessage);

                    char magicboxCommand = readMessage.charAt(0);
                    String medicion = readMessage.substring(1);

                    String logString = timestamp + ": " + medicion;

                    switch (magicboxCommand) {
                        case 'T': temperaturaView.setText(medicion + " ºC");
                        logString += " ºC";
                        break;

                        case 'P': pesoView.setText(medicion + " kg");
                        logString += " kg";
                        break;

                        case 'V': volumenView.setText(medicion); // + Html.fromHtml(" cm<sup>3</sup>"));
                        logString += "cm3";
                        break;
                    }

                    log.add(logString);
                }
            }
        };

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
