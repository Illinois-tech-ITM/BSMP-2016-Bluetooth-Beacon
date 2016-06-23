package edu.iit.bluetoothbeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final static int MIN_RSSI = -70;
    private final static int NEARBY_RSSI = -40;

    private BluetoothAdapter mAdapter;
    private BluetoothDevice mActiveDevice;
    private HashMap<BluetoothDevice, Integer> mDevicesList; //key: Beacon | value: RSSI (sinal strength)

    private TextView mTitleTextView;
    private TextView mDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevicesList = new HashMap<>();
        mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mDescriptionTextView = (TextView) findViewById(R.id.descTextView);
        mDescriptionTextView.setVisibility(View.GONE);

    }

    private LeScanCallback scanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            if (bluetoothDevice.getName() == null || !bluetoothDevice.getName().matches("DVC\\d\\d\\d\\d")) return;
            Log.d("Test", "Address: " + bluetoothDevice + " | RSSI: "+ rssi);
            mDevicesList.put(bluetoothDevice, rssi);
            if (mActiveDevice == null && rssi > NEARBY_RSSI){
                mActiveDevice = bluetoothDevice;
                updateView(bluetoothDevice.getName());
                return;
            }

            if (mActiveDevice != null && rssi - mDevicesList.get(mActiveDevice) > 20){
                mActiveDevice = bluetoothDevice;
                updateView(bluetoothDevice.getName());
            } else if (mActiveDevice != null && bluetoothDevice.getAddress().equals(mActiveDevice.getAddress()) && rssi < MIN_RSSI){
                mActiveDevice = null;
                updateView(":(");
                mDescriptionTextView.setVisibility(View.GONE);
            }


        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Test", "Scanning for devices...");
        mAdapter.startLeScan(scanCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopLeScan(scanCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private void updateView(String title){
        mTitleTextView.setText(title);
        mDescriptionTextView.setVisibility(View.VISIBLE);
    }

}
