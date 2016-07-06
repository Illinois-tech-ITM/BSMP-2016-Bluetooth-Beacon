package edu.iit.bluetoothbeacon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.HashMap;

import edu.iit.bluetoothbeacon.models.Masterpiece;
import edu.iit.bluetoothbeacon.models.Translation;

public class MainActivity extends AppCompatActivity implements OnResponseReceivedListener, MasterpieceFragment.OnLanguageSelectedListener {
    private final static int MIN_RSSI = -70;
    private final static int NEARBY_RSSI = -55;
    private static final int REQUEST_ENABLE_BT = 1;

    private Controller controller;

    private BluetoothAdapter mAdapter;
    private BluetoothDevice mActiveDevice;
    private HashMap<BluetoothDevice, Integer> mDevicesList; //key: Beacon | value: RSSI (sinal strength)
    private String mCurrentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevicesList = new HashMap<>();
        mCurrentLanguage = "pt-br";
        controller = Controller.getInstance(this, this);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new WelcomeFragment().newInstance());
        fragmentTransaction.commit();
    }

    private LeScanCallback scanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            if (bluetoothDevice.getName() == null || !bluetoothDevice.getName().matches("DVC\\d\\d\\d\\d")) return;
//            Log.d("Test", "Address: " + bluetoothDevice + " | RSSI: "+ rssi);
            mDevicesList.put(bluetoothDevice, rssi);
            if (mActiveDevice == null && rssi > NEARBY_RSSI){
                mActiveDevice = bluetoothDevice;
                controller.requestMasterpieceInfo(bluetoothDevice.getName().toLowerCase());
                return;
            }

            if (mActiveDevice != null && rssi - mDevicesList.get(mActiveDevice) > 20){
                mActiveDevice = bluetoothDevice;
                controller.requestMasterpieceInfo(bluetoothDevice.getName().toLowerCase());
            } else if (mActiveDevice != null && bluetoothDevice.getAddress().equals(mActiveDevice.getAddress()) && rssi < MIN_RSSI){
                mActiveDevice = null;
                switchToFragment(new WelcomeFragment().newInstance());
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TEST", mAdapter.isEnabled() + "");
        if (!mAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Log.d("Test", "Scanning for devices...");
            mAdapter.startLeScan(scanCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.d("Test", "Scanning for devices...");
                mAdapter.startLeScan(scanCallback);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopLeScan(scanCallback);
    }

    @Override
    public void OnResponseReceived(Masterpiece mp, boolean error) {
        if(!error){
            switchToFragment(new MasterpieceFragment().newInstance(mp, mCurrentLanguage));
            Log.d("Response", mp.getDvcName());
        } else { // Unsuccessful response
//            switchToFragment(new ErrorFragment().newInstance());
            Log.d("Response", "Error");
        }
    }

    private void switchToFragment(Fragment f){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, f);
        fragmentTransaction.commit();
    }

    @Override
    public void onLanguageSelected(String language) {
        mCurrentLanguage = language;
    }
}
