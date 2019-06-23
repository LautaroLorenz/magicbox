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
import com.example.magicbox.magicbox.models.HistorialItem;
import com.example.magicbox.magicbox.models.Product;
import com.example.magicbox.magicbox.sensores.Giroscopio;
import com.example.magicbox.magicbox.sensores.SensorProximidad;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private Button btnHistorial;
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

    List<HistorialItem> log;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);

        log = new ArrayList<HistorialItem>();
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
        btnHistorial = (Button) findViewById(R.id.btnHistorial);
        btnMicrofono = (ImageButton) findViewById(R.id.btnMicrofono);

        imagenView.setImageResource(productoActual.getIdRecursoImagen());
        nombreView.setText(productoActual.getName());

        btnCambiarProducto.setOnClickListener(btnVerListadoProductosListener);
        btnProveedores.setOnClickListener(btnVerProveedoresListener);
        btnMicrofono.setOnClickListener(btnMicrofonoListener);
        btnHistorial.setOnClickListener(new ListenerBtnHistorial(log));

        address = bundle.getString("deviceAddress");

        handlerBluetoothIn = createHandlerBluetooth(getApplicationContext());
        btMagicboxService = new MagicboxBluetoothService(address, handlerBluetoothIn);

        btMagicboxService.start();

        // Le mando la temperatura que necesita el producto actual del contenedor
        btMagicboxService.write(convertirTemperaturaAComando(productoActual.getTemperaturaIdeal()));

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
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //si se recibio un msj del hilo secundario
                if (msg.what == 0)
                {
                    HistorialItem historialItem = new HistorialItem();
                    String timestamp = formatter.format(new Date(System.currentTimeMillis()));

                    String readMessage = (String) msg.obj;
                    Log.i("HANDLER", readMessage);

                    char magicboxCommand = readMessage.charAt(0);
                    String medicion = readMessage.substring(1, readMessage.indexOf('|')).replace("-", "");

                    historialItem.setTimestamp(timestamp);

                    boolean mensajeCorrecto = true;

                    switch (magicboxCommand) {
                        case 'T': temperaturaView.setText(medicion + " ºC");
                        historialItem.setMedicion(medicion + " ºC");
                        break;

                        case 'P': pesoView.setText(medicion + " kg");
                        historialItem.setMedicion(medicion + " kg");
                        break;

                        case 'V': volumenView.setText(medicion + " cm" + Html.fromHtml("<sup>3</sup>"));
                        historialItem.setMedicion(medicion + " cm3");
                        break;

                        case 'E':
                            String estadoContenedor = medicion.equals('0')? "Apagado" : medicion.equals('1')? "Enfriando" : "Calentando";
                            estadoView.setText(estadoContenedor);
                            historialItem.setMedicion(estadoContenedor);
                        break;

                        case 'Z':
                            String estadoPuerta = medicion.equals('A')? "Abierta" : "Cerrada";
                            Toast.makeText(context, "La puerta se encuentra " + estadoPuerta , Toast.LENGTH_LONG).show();
                            historialItem.setMedicion(estadoPuerta);
                            break;

                        default: mensajeCorrecto = false;
                        break;
                    }

                    if(mensajeCorrecto) {
                        log.add(historialItem);
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
                    btMagicboxService.write("B".getBytes());
                } else if (s.contains("estado")) {
                    Log.i("MICROFONO", "Estado contenedor");
                    btMagicboxService.write("E".getBytes());
                } else if(s.contains("puerta")) {
                    btMagicboxService.write("Z".getBytes());
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

    private class ListenerBtnHistorial implements View.OnClickListener {
        ArrayList<HistorialItem> log;
        public ListenerBtnHistorial(List<HistorialItem> log) {
            this.log = (ArrayList<HistorialItem>) log;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ProductoActivity.this, HistorialActivity.class);
            intent.putExtra("lista-historial", log);
            startActivity(intent);
        }
    }

    public byte[] convertirTemperaturaAComando(String temperatura) {
        int asciiValue = Integer.parseInt(temperatura) + 100;

        String aChar = new Character((char) asciiValue).toString();
        return aChar.getBytes();
    }
}
