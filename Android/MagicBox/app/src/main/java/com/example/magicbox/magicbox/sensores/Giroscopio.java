package com.example.magicbox.magicbox.sensores;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.Html;
import android.widget.TextView;

import com.example.magicbox.magicbox.MagicboxBluetoothService;
import com.example.magicbox.magicbox.activities.BluetoothMagicbox;

import java.io.IOException;

import static android.content.Context.SENSOR_SERVICE;

public class Giroscopio {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    SensorEventListener gyroscopeSensorListener;

    private TextView pesoView;
    private TextView volumenView;

    public Giroscopio(TextView pesoView, TextView volumenView) {
        this.pesoView = pesoView;
        this.volumenView = volumenView;
    }

    public void iniciar(final Activity activity, final MagicboxBluetoothService btMagicbox) {
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

                if(sensorEvent.values[2] > 0.8f) { // anticlockwise
                    activity.getWindow().getDecorView().setBackgroundColor(Color.rgb(178, 235, 242));
                    btMagicbox.write("V");
                } else if(sensorEvent.values[2] < -0.8f) { // clockwise
                    activity.getWindow().getDecorView().setBackgroundColor(Color.rgb(0, 188, 212));
                    btMagicbox.write("P");
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