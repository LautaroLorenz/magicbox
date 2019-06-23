package com.example.magicbox.magicbox.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.magicbox.magicbox.ProductListAdapter;
import com.example.magicbox.magicbox.R;
import com.example.magicbox.magicbox.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends Activity{

    private ListView productListView;
    List<String> list = new ArrayList<String>();
    ProductListAdapter productListAdapter;

    List<Product> listaProductos;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.producto_list);
        productListView = (ListView) findViewById(R.id.lv_productos);


        final String deviceAddress = getIntent().getExtras().getString("deviceAddress");

        this.cargarListaProductos();

        productListAdapter = new ProductListAdapter(this, R.layout.list_item_product, listaProductos);
        productListView.setAdapter(productListAdapter);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(ProductListActivity.this, ProductoActivity.class);
                intent.putExtra("nombre", ((Product)adapterView.getAdapter().getItem(position)).getName());
                intent.putExtra("peso", ((Product)adapterView.getAdapter().getItem(position)).getPeso());
                intent.putExtra("temperaturaIdeal", ((Product)adapterView.getAdapter().getItem(position)).getTemperaturaIdeal());
                intent.putExtra("idRecursoImagen", ((Product)adapterView.getAdapter().getItem(position)).getIdRecursoImagen());
                intent.putExtra("urlProveedores", ((Product)adapterView.getAdapter().getItem(position)).getUrlProveedores());
                intent.putExtra("deviceAddress", deviceAddress);
                startActivity(intent);
            }
        });
    }

    // TODO: Se podrian leer de la BD
    public void cargarListaProductos() {

        final String URL_SUPERMERCADOS = "https://www.google.com.ar/maps/search/supermercados/@-34.6801812,-58.5658106,15z/data=!3m1!4b1";
        final String URL_VERDULERIAS = "https://www.google.com.ar/maps/search/verduleria/@-34.6801453,-58.5658106,15z/data=!3m1!4b1";

        this.listaProductos = new ArrayList<Product>();

        Product p1 = new Product("Alfajores", "60gr", "16", R.drawable.lechuga, URL_VERDULERIAS);
        Product p2 = new Product("Huevos", "120gr", "20", R.drawable.huevos, URL_SUPERMERCADOS);
        Product p3 = new Product("Atún", "340gr", "19", R.drawable.atun, URL_SUPERMERCADOS);
        Product p4 = new Product("Manzanas", "280gr", "15", R.drawable.lechuga, URL_VERDULERIAS);
        Product p5 = new Product("Paquetes de merca", "400gr", "14", R.drawable.lechuga, URL_SUPERMERCADOS);

        this.listaProductos.add(p1);
        this.listaProductos.add(p2);
        this.listaProductos.add(p3);
        this.listaProductos.add(p4);
        this.listaProductos.add(p5);
    }
}
