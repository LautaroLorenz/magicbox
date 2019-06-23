
package com.example.magicbox.magicbox.sensores;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.magicbox.magicbox.bluetooth.MagicboxBluetoothService;

import static android.content.Context.SENSOR_SERVICE;

public class SensorProximidad {

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximitySensorListener;

    public SensorProximidad() {
    }

    public void iniciar(final Activity activity, final MagicboxBluetoothService btMagicbox) {
        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if(proximitySensor == null) {
            activity.finish();
        }

        proximitySensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(sensorEvent.values[0] < proximitySensor.getMaximumRange()) {
                    activity.getWindow().getDecorView().setBackgroundColor(Color.rgb(83, 109, 254));
                    btMagicbox.write("T".getBytes());

                } else {
                    activity.getWindow().getDecorView().setBackgroundColor(Color.rgb(250, 250, 250));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorManager.registerListener(proximitySensorListener, proximitySensor, 2 * 1000 * 1000);
    }

    public void pausar() {
        sensorManager.unregisterListener(proximitySensorListener);
    }

    public void continuar() {
        sensorManager.registerListener(proximitySensorListener, proximitySensor, 2 * 1000 * 1000);
    }
}