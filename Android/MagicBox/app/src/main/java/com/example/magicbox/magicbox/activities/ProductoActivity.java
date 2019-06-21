package com.example.magicbox.magicbox.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductoActivity extends MainActivity {

    // ------------------------------------------------------------
    //          VIEWS
    // ------------------------------------------------------------
    private TextView pesoView;
    private TextView volumenView;
    private TextView nombreView;
    private TextView temperaturaView;
    private TextView estadoView;
    private ImageView imagenView;
    private Button btnCambiarProducto;
    private Button btnProveedores;
    private ImageButton btnMicrofono;

    // ------------------------------------------------------------
    //          SENSORES ANDROID
    // ------------------------------------------------------------
    private SensorProximidad sensorProximidad;
    private Giroscopio giroscopio;

    private static final int REQ_CODE_SPEECH_INPUT = 100;

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
        estadoView = (TextView) findViewById(R.id.text_estado);

        btnCambiarProducto = (Button) findViewById(R.id.btnCambiarProducto);
        btnProveedores = (Button) findViewById(R.id.btnProveedores);
        btnMicrofono = (ImageButton) findViewById(R.id.btnMicrofono);

        imagenView.setImageResource(productoActual.getIdRecursoImagen());
        nombreView.setText(productoActual.getName());

        btnCambiarProducto.setOnClickListener(btnVerListadoProductosListener);
        btnProveedores.setOnClickListener(btnVerProveedoresListener);
        btnMicrofono.setOnClickListener(btnMicrofonoListener);


        log = new ArrayList<>();

        address = bundle.getString("deviceAddress");

        handlerBluetoothIn = createHandlerBluetooth(getApplicationContext());
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

    private View.OnClickListener btnMicrofonoListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startVoiceInput();
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
    private Handler createHandlerBluetooth (final Context context) {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == 0)
                {
                    String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                    String readMessage = (String) msg.obj;
                    Log.i("HANDLER", readMessage);

                    char magicboxCommand = readMessage.charAt(0);
                    String medicion = readMessage.substring(1, readMessage.indexOf('|')).replace("-", "");

                    String logString = timestamp + ": " + medicion;

                    boolean mensajeCorrecto = true;

                    switch (magicboxCommand) {
                        case 'T': temperaturaView.setText(medicion + " ºC");
                        logString += " ºC";
                        break;

                        case 'P': pesoView.setText(medicion + " kg");
                        logString += " kg";
                        break;

                        case 'V': volumenView.setText(medicion + " cm" + Html.fromHtml("<sup>3</sup>"));
                        logString += " cm3";
                        break;

                        case 'E':
                            String estadoContenedor = medicion.equals('0')? "Apagado" : medicion.equals('1')? "Enfriando" : "Calentando";
                            estadoView.setText(estadoContenedor);
                            logString += " Estado: " + estadoContenedor;
                        break;

                        case 'Z':
                            String estadoPuerta = medicion.equals('A')? "Abierta" : "Cerrada";
                            Toast.makeText(context, "La puerta se encuentra " + estadoPuerta , Toast.LENGTH_LONG).show();
                            logString += estadoPuerta;
                            break;

                        default: mensajeCorrecto = false;
                        break;
                    }

                    if(mensajeCorrecto) {
                        log.add(logString);
                    }
                }
            }
        };

    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hola, hablele al Magicbox");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && null != data) {

            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            for (String s : result) {
                Log.i("MICROFONO", s);
                if (s.contains("alarma")) {
                    Log.i("MICROFONO", "Apagando alarma");
                    btMagicboxService.write("B");
                } else if (s.contains("estado")) {
                    Log.i("MICROFONO", "Estado contenedor");
                    btMagicboxService.write("E");
                } else if(s.contains("puerta")) {
                    btMagicboxService.write("Z");
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
