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
import com.example.magicbox.magicbox.database.AdminSQLiteOpenHelper;
import com.example.magicbox.magicbox.R;
import com.example.magicbox.magicbox.models.Product;

import java.io.IOException;
import java.io.InputStream;


public class ProductoActivity extends MainActivity {

    // ------------------------------------------------------------
    //          Views
    // ------------------------------------------------------------
    private TextView pesoView;
    private TextView nombreView;
    private TextView temperaturaIdealView;
    private TextView temperaturaView;
    private ImageView imagenView;
    private Button btnCambiarProducto;
    private Button btnProveedores;

    // Tarea en segundo plano para actualizar la temp en tiempo real
    ActualizarTemperaturaTask actualizarTemperaturaTask;

    // AssetManager
   private AssetManager am;


    // ------------------------------------------------------------
    //          Producto actual en el contenedor
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

    }

    @Override
    protected void onStop() {
        super.onStop();
        actualizarTemperaturaTask.cancel(true);
    }

    /*
        @Override public void onPause() {
            super.onPause();
            actualizarTemperaturaTask.cancel(true);
        }
        */
/*
    @Override
    protected void onResume() {
        super.onResume();
        actualizarTemperaturaTask.execute();
    }
*/
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


    public void seleccionar(View v){

        Intent i = new Intent(ProductoActivity.this, ProductListActivity.class);
        startActivity(i);
    }

    public void consulta(View v) throws IOException {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "admin", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        String n = nombreView.getText().toString();
        String im;
        Cursor fila = bd.rawQuery("select peso, imagen from producto where nombre='" + n + "'", null);
        System.out.print(n);
        if(fila.moveToFirst()){
            pesoView.setText(fila.getString(0));

            InputStream is = am.open(fila.getString(1));

            String[] imgPath = am.list("img");

            for(int i=0; i<imgPath.length; i++){
                System.out.println(imgPath[i]);
            }

                //Bitmap bm = BitmapFactory.decodeStream(is);
                //imagen.setImageBitmap(bm);

        }
        else{
            Toast.makeText(this, "No existe ningun producto con ese nombre", Toast.LENGTH_SHORT).show();
            bd.close();
        }
    }

}
