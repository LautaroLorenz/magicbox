package com.example.magicbox.magicbox.sensores;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.example.magicbox.magicbox.bluetooth.MagicboxBluetoothService;

import static android.content.Context.SENSOR_SERVICE;

public class Giroscopio {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private SensorEventListener gyroscopeSensorListener;

    private TextView pesoView;
    private TextView volumenView;

    private long timestampEventoAnterior = 0;
    private long timestampActual;

    public Giroscopio(TextView pesoView, TextView volumenView) {
        this.pesoView = pesoView;
        this.volumenView = volumenView;
    }

    public void iniciar(final Activity activity, final MagicboxBluetoothService btMagicbox) {
        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);

        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if(gyroscopeSensor == null) {
            activity.finish();
        }

        gyroscopeSensorListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                timestampActual = System.currentTimeMillis();

                if (timestampActual - timestampEventoAnterior > 300) {
                    if (sensorEvent.values[2] > 0.8f) {
                        btMagicbox.write("V".getBytes());
                        activity.getWindow().getDecorView().setBackgroundColor(Color.rgb(178, 235, 242));
                    } else if (sensorEvent.values[2] < -0.8f) {
                        btMagicbox.write("P".getBytes());
                        activity.getWindow().getDecorView().setBackgroundColor(Color.rgb(0, 188, 212));
                    }
                    timestampEventoAnterior = System.currentTimeMillis();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorManager.registerListener(gyroscopeSensorListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void pausar() {
        sensorManager.registerListener(gyroscopeSensorListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void continuar() {
        sensorManager.registerListener(gyroscopeSensorListener, gyroscopeSensor, 2 * 1000 * 1000);
    }
}