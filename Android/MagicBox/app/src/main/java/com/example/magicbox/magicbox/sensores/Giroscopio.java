package com.example.magicbox.magicbox.sensores;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

public class Giroscopio {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    SensorEventListener gyroscopeSensorListener;

    public void iniciar(final Activity activity) {
        // Obtenemos acceso al giroscopio
        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);

        // Instancio un giroscopio
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Verifico que el giroscopio este disponible
        if(gyroscopeSensor == null) {
            activity.finish(); // Sensor no disponible
        }

        // Creo listener para atender los eventos del sensor
        gyroscopeSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(sensorEvent.values[2] > 0.5f) { // anticlockwise
                    activity.getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                } else if(sensorEvent.values[2] < -0.5f) { // clockwise
                    activity.getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        // Register the listener
        sensorManager.registerListener(gyroscopeSensorListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void pausar() {
        sensorManager.registerListener(gyroscopeSensorListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    public void continuar() {
        sensorManager.registerListener(gyroscopeSensorListener, gyroscopeSensor, 2 * 1000 * 1000);
    }
}