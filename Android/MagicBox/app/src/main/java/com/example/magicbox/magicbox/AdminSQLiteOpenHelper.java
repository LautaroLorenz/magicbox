package com.example.magicbox.magicbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    public AdminSQLiteOpenHelper(Context context, String nombre, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, nombre, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table producto(nombre text not null primary key, peso float not null, imagen text not null)");
        db.execSQL("insert into producto(nombre, peso, imagen) values ('Salchichas', 5000.00, 'img/salchichas.jpg')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int version1, int version2) {
        db.execSQL("drop table if exists producto");
        db.execSQL("create table producto(peso text not null primary key, peso float not null, imagen text not null)");
    }
}
