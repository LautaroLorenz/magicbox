package com.example.magicbox.magicbox.models;

import java.io.Serializable;

public class HistorialItem implements Serializable {

    String timestamp;
    String medicion;

    public HistorialItem() {

    }

    public HistorialItem(String timestamp, String medicion) {
        this.timestamp = timestamp;
        this.medicion = medicion;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMedicion() {
        return medicion;
    }

    public void setMedicion(String medicion) {
        this.medicion = medicion;
    }
}
