package com.example.magicbox.magicbox.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magicbox.magicbox.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private TextView txtEstado;
    private Button btnActivar;
    private Button btnEmparejar;
    private Button btnBuscar;


    private ProgressDialog mProgressDlg;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter mBluetoothAdapter;

    public static final int MULTIPLE_PERMISSIONS = 10;
    String[] permissions= new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    txtEstado = (TextView) findViewById(R.id.txtEstado);
    btnActivar = (Button) findViewById(R.id.btnActivar);
    btnEmparejar = (Button) findViewById(R.id.btnEmparejar);
    btnBuscar = (Button) findViewById(R.id.btnBuscar);

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    mProgressDlg = new ProgressDialog(this);

    mProgressDlg.setMessage("Buscando dispositivos...");
    mProgressDlg.setCancelable(false);

    mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", btnCancelarDialogListener);

    if (checkPermissions()) {
    enableComponent();
}
}

    protected  void enableComponent() {
        if (mBluetoothAdapter == null) {
            showUnsupported();
        }
        else {
            btnEmparejar.setOnClickListener(btnEmparejarListener);

            btnBuscar.setOnClickListener(btnBuscarListener);

            btnActivar.setOnClickListener(btnActivarListener);

            if (mBluetoothAdapter.isEnabled()) {
                showEnabled();
            }
            else {
                showDisabled();
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON)
                {
                    showToast("Bluetooth activado");
                    showEnabled();
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();

                mProgressDlg.show();
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                showToast("Dispositivo Encontrado:" + device.getName());
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();

                if (mDeviceList.size() > 0) {
                    Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                    newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                    startActivity(newIntent);
                } else {
                    showToast("No se encontraron dispositivos");
                }
            }
        }
    };

    private View.OnClickListener btnEmparejarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices == null || pairedDevices.size() == 0) {
                showToast("No se encontraron dispositivos emparejados");
            }
            else {
                ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
                list.addAll(pairedDevices);

                Intent intent = new Intent(MainActivity.this, PairedDeviceListActivity.class);
                intent.putParcelableArrayListExtra("device.list", list);
                startActivity(intent);
            }
        }
    };

    private View.OnClickListener btnBuscarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBluetoothAdapter.startDiscovery();

        }
    };


    private View.OnClickListener btnActivarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();

                showDisabled();
            } else {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(intent, 1000);
            }
        }
    };


    private DialogInterface.OnClickListener btnCancelarDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            mBluetoothAdapter.cancelDiscovery();
        }
    };

    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (listPermissionsNeeded.isEmpty()) {
            return true;
        }
        ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    String perStr = "";
                    for (String per : permissions) {
                        perStr += "\n" + per;
                    }
                    Toast.makeText(this,"ATENCION: La aplicacion no funcionara " +
                            "correctamente debido a la falta de Permisos", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void showEnabled() {
        txtEstado.setText("Bluetooth habilitado");
        txtEstado.setTextColor(Color.BLUE);

        btnActivar.setText("Desactivar");
        btnActivar.setEnabled(true);

        btnEmparejar.setEnabled(true);
        btnBuscar.setEnabled(true);
    }

    private void showDisabled() {
        txtEstado.setText("Bluetooth Deshabilitado");
        txtEstado.setTextColor(Color.RED);

        btnActivar.setText("Activar");
        btnActivar.setEnabled(true);

        btnEmparejar.setEnabled(false);
        btnBuscar.setEnabled(false);
    }

    private void showUnsupported() {
        txtEstado.setText("Bluetooth no es soportado por el dispositivo movil");

        btnActivar.setText("Activar");
        btnActivar.setEnabled(false);

        btnEmparejar.setEnabled(false);
        btnBuscar.setEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
