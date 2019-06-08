package com.example.magicbox.magicbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends Activity{

    private ListView productList;
    List<String> list = new ArrayList<String>();
    ArrayAdapter<String> adapter;



     // Clase que guarda los productos en un arraylist y les devuelve a la activity producto
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.producto_list);

        productList = (ListView) findViewById(R.id.lv_productos);
        String[] productos = {"Salchichas", "Huevos"};  // Agregar mas productos

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, productos);
        productList.setAdapter(adapter);

        productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(ProductListActivity.this, ProductoActivity.class);
                intent.putExtra("nombre", String.valueOf(productList.getSelectedItem()));
                startActivity(intent);

            }
        });

    }
}
