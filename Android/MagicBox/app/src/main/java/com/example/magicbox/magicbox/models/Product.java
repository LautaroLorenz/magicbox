package com.example.magicbox.magicbox.models;

public class Product {

    String id;
    String name;
    String peso;
    String temperaturaIdeal;
    int idRecursoImagen;
    String urlProveedores;

    public Product() {

    }

    public Product(String name, String peso, String temperaturaIdeal, int idRecursoImagen, String urlProveedores) {
        this.name = name;
        this.peso = peso;
        this.temperaturaIdeal = temperaturaIdeal;
        this.idRecursoImagen = idRecursoImagen;
        this.urlProveedores = urlProveedores;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getTemperaturaIdeal() {
        return temperaturaIdeal;
    }

    public void setTemperaturaIdeal(String temperaturaIdeal) {
        this.temperaturaIdeal = temperaturaIdeal;
    }

    public int getIdRecursoImagen() {
        return idRecursoImagen;
    }

    public void setIdRecursoImagen(int idRecursoImagen) {
        this.idRecursoImagen = idRecursoImagen;
    }

    public String getUrlProveedores() {
        return urlProveedores;
    }

    public void setUrlProveedores(String urlProveedores) {
        this.urlProveedores = urlProveedores;
    }
}
