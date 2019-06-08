package com.example.magicbox.magicbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.magicbox.magicbox.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends Activity{

    private ListView productListView;
    List<String> list = new ArrayList<String>();
    ListAdapter listAdapter;

    List<Product> listaProductos;

     // Clase que guarda los productos en un arraylist y les devuelve a la activity producto
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.producto_list);


        productListView = (ListView) findViewById(R.id.lv_productos);


        this.cargarListaProductos();
        listAdapter = new ListAdapter(this, R.layout.list_item_product, listaProductos);

        productListView.setAdapter(listAdapter);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(ProductListActivity.this, ProductoActivity.class);
                intent.putExtra("nombre", ((Product)adapterView.getAdapter().getItem(position)).getName());
                intent.putExtra("peso", ((Product)adapterView.getAdapter().getItem(position)).getPeso());
                intent.putExtra("temperaturaIdeal", ((Product)adapterView.getAdapter().getItem(position)).getTemperaturaIdeal());
                startActivity(intent);
            }
        });

    }

    // TODO: Se podrian leer de la BD
    public void cargarListaProductos() {

        this.listaProductos = new ArrayList<Product>();

        Product p1 = new Product("1", "Salchichas", "200gr", "13ºC");
        Product p2 = new Product("2", "Huevos", "120gr", "16ºC");
        Product p3 = new Product("3", "Patys", "340gr", "9ºC");
        Product p4 = new Product("4", "Manzanas", "280gr", "14ºC");
        Product p5 = new Product("5", "Paquetes de merca", "400gr", "12ºC");

        this.listaProductos.add(p1);
        this.listaProductos.add(p2);
        this.listaProductos.add(p3);
        this.listaProductos.add(p4);
        this.listaProductos.add(p5);
    }
}
