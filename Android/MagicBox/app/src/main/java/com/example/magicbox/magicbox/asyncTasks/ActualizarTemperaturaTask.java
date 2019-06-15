package com.example.magicbox.magicbox.asyncTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magicbox.magicbox.activities.ProductoActivity;

import java.util.Random;

public class ActualizarTemperaturaTask extends AsyncTask<Void, Integer, Boolean> {
    private TextView textViewTemperatura;

    public ActualizarTemperaturaTask(TextView textViewTemperatura) {
        super();
        this.textViewTemperatura = textViewTemperatura;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        int temperaturaObtenida;
        for(int i=1; i<=5; i++) {
            temperaturaObtenida = obtenerTemperatura();
            publishProgress(temperaturaObtenida);

            try {
                Thread.sleep(3000);
            } catch(InterruptedException e) {}

            if (isCancelled()) {
                break;
            }

        }

        return true;
    }

    public int obtenerTemperatura() {
        return new Random().nextInt(25);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int temperaturaObtenida = values[0].intValue();
        this.textViewTemperatura.setText(temperaturaObtenida + " ºC");
    }

    @Override
    protected void onPreExecute() {
        Log.i("Task", "Iniciada");
        textViewTemperatura.setText("0 ºC");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.i("Task", "Terminada");
    }

    @Override
    protected void onCancelled() {
        Log.i("Task", "Cancelada");
    }
}
