package com.example.magicbox.magicbox.sensores;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import static android.content.Context.SENSOR_SERVICE;

public class SensorProximidad {

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximitySensorListener;

    public void iniciar(final Activity activity) {
    // Obtenemos acceso al sensor de proximidad
        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);

    // Instancio un sensor de proximidad
     proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

    // Verifico que el sensor este disponible
        if(proximitySensor == null) {
            activity.finish(); // Sensor no disponible
    }

    // Creo listener para atender los eventos del sensor
    proximitySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.values[0] < proximitySensor.getMaximumRange()) {
                // Detected something nearby
                activity.getWindow().getDecorView().setBackgroundColor(Color.RED);
            } else {
                // Nothing is nearby
                activity.getWindow().getDecorView().setBackgroundColor(Color.GREEN);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    // Registro el listener, especificando el intervalo entre mediciones en ms
        sensorManager.registerListener(proximitySensorListener, proximitySensor, 2 * 1000 * 1000);
    }

    // Deja de capturar eventos del sensor
    public void pausar() {
        sensorManager.unregisterListener(proximitySensorListener);
    }

    // Continuo capturando eventos del sensor
    public void continuar() {
        sensorManager.registerListener(proximitySensorListener, proximitySensor, 2 * 1000 * 1000);
    }
}


