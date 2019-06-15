package com.example.magicbox.magicbox.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.InputStream;


public class ProductoActivity extends MainActivity {

    // ------------------------------------------------------------
    //          VIEWS
    // ------------------------------------------------------------
    private TextView pesoView;
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

    // AssetManager
   private AssetManager am;


    // ------------------------------------------------------------
    //          PRODUCTO ACTUAL EN EL CONTENEDOR
    // ------------------------------------------------------------
   private Product productoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);

        imagenView = (ImageView) findViewById(R.id.product_image);
        nombreView = (TextView) findViewById(R.id.product_nombre);
        pesoView = (TextView) findViewById(R.id.product_peso);
        temperaturaIdealView = (TextView) findViewById(R.id.product_temperaturaIdeal);
        temperaturaView = (TextView) findViewById(R.id.text_temperatura);

        btnCambiarProducto = (Button) findViewById(R.id.btnCambiarProducto);
        btnProveedores = (Button) findViewById(R.id.btnProveedores);


        am = getAssets();

        Bundle bundle = getIntent().getExtras();

        productoActual = new Product();
        productoActual.setName(bundle.getString("nombre"));
        productoActual.setPeso(bundle.getString("peso"));
        productoActual.setTemperaturaIdeal(bundle.getString("temperaturaIdeal"));
        productoActual.setIdRecursoImagen(bundle.getInt("idRecursoImagen"));
        productoActual.setUrlProveedores(bundle.getString("urlProveedores"));

        imagenView.setImageResource(productoActual.getIdRecursoImagen());
        nombreView.setText(productoActual.getName());
        pesoView.setText(productoActual.getPeso());
        temperaturaIdealView.setText(productoActual.getTemperaturaIdeal());

        btnCambiarProducto.setOnClickListener(btnVerListadoProductosListener);
        btnProveedores.setOnClickListener(btnVerProveedoresListener);

        actualizarTemperaturaTask = new ActualizarTemperaturaTask(temperaturaView);
        actualizarTemperaturaTask.execute();

        sensorProximidad = new SensorProximidad();
        sensorProximidad.iniciar(this);

        giroscopio= new Giroscopio();
        giroscopio.iniciar(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorProximidad.continuar();
        giroscopio.continuar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        actualizarTemperaturaTask.cancel(true);
        sensorProximidad.pausar();
        giroscopio.pausar();
    }

    // ------------------------------------------------------------
    //          Listeners de los ButtonView
    // ------------------------------------------------------------
    private View.OnClickListener btnVerListadoProductosListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ProductoActivity.this, ProductListActivity.class);
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
}
