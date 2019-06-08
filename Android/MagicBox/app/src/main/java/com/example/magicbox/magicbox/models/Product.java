package com.example.magicbox.magicbox.models;

public class Product {

    String id;
    String name;
    String peso;
    String temperaturaIdeal;

    public Product(String id, String name, String peso, String temperaturaIdeal) {
        this.id = id;
        this.name = name;
        this.peso = peso;
        this.temperaturaIdeal = temperaturaIdeal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
