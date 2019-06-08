package com.example.magicbox.magicbox;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class ProductoActivity extends MainActivity{

   private Button b;
   private TextView pesoMax;
   private TextView nombre;
   private ImageView imagen;
   private AssetManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.producto);

        nombre = (TextView) findViewById(R.id.textView5);
        pesoMax = (TextView) findViewById(R.id.textView6);
        imagen = (ImageView) findViewById(R.id.imageView2);
        am = getAssets();

        nombre.setText(getIntent().getExtras().getString("nombre"));

    }

    public void seleccionar(View v){

        Intent i = new Intent(ProductoActivity.this, ProductListActivity.class);
        startActivity(i);


    }

    public void consulta(View v) throws IOException {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "admin", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        String n = nombre.getText().toString();
        String im;
        Cursor fila = bd.rawQuery("select peso, imagen from producto where nombre='" + n + "'", null);
        System.out.print(n);
        if(fila.moveToFirst()){
            pesoMax.setText(fila.getString(0));

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
