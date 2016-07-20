package edu.iit.bluetoothbeacon;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import edu.iit.bluetoothbeacon.models.Masterpiece;
import edu.iit.bluetoothbeacon.models.Translation;

public class MainActivity extends AppCompatActivity implements OnResponseReceivedListener, MasterpieceFragment.OnLanguageSelectedListener {
    private final static int MIN_RSSI = -70;
    private final static int NEARBY_RSSI = -40;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    private Controller controller;

    private BluetoothAdapter mAdapter;
    private BluetoothDevice mActiveDevice;
    private HashMap<BluetoothDevice, Integer> mDevicesList; //key: Beacon | value: RSSI (sinal strength)
    private ArrayList<String> mRegisteredDevices;
    private String mCurrentLanguage;

    private long mStart = 0;
    private long mCurrentStart = 0;


    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevicesList = new HashMap<>();
        mRegisteredDevices = new ArrayList<>();
        mCurrentLanguage = "pt-br";
        mActiveDevice = null;
        controller = Controller.getInstance(this, this);
        switchToFragment(new WelcomeFragment().newInstance(), true);
        mHandler = new Handler();
        startRepeatingTask();
    }

    private LeScanCallback scanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            long delta = (SystemClock.elapsedRealtime() - mStart);
            if (mActiveDevice != null &&  delta > 2101546475){
//                Log.d("TEST", "Elapsed seconds: " + delta);
                mActiveDevice = null;
                switchToFragment(new WelcomeFragment().newInstance(), false);
            }
//            Log.d("SCANNING", "Name: " + bluetoothDevice.getName());
            if (bluetoothDevice.getName() == null || !mRegisteredDevices.contains(bluetoothDevice.getName())) return;
//            Log.d("SCANNING", "Name: " + bluetoothDevice.getName()  + "| Address: " + bluetoothDevice + " | RSSI: "+ rssi);
            mDevicesList.put(bluetoothDevice, rssi);

            if (mActiveDevice == null && rssi > NEARBY_RSSI){
                mActiveDevice = bluetoothDevice;
                controller.requestMasterpieceInfo(bluetoothDevice.getName());
                return;
            }

            if (mActiveDevice != null && rssi - mDevicesList.get(mActiveDevice) > 20){
                mActiveDevice = bluetoothDevice;
                controller.requestMasterpieceInfo(bluetoothDevice.getName());
            } else if (mActiveDevice != null && bluetoothDevice.getAddress().equals(mActiveDevice.getAddress()) && rssi < MIN_RSSI){
                mActiveDevice = null;
                switchToFragment(new WelcomeFragment().newInstance(), false);
            }
            if (mActiveDevice != null && bluetoothDevice.getAddress().equals(mActiveDevice.getAddress())){
                mStart = SystemClock.elapsedRealtime();
//                Log.d("Test", "Start time: " + mStart);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TEST", mAdapter.isEnabled() + "");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            startScanning();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning();
                } else {
                    Log.d("TEST", "We need this permission :(");
                }
                return;
            }
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
        stopRepeatingTask();
    }

    private void startScanning(){
        String url = "http://floating-journey-50760.herokuapp.com/getArtworks";
        RequestQueue queue = Volley.newRequestQueue(this);
        final JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                mRegisteredDevices.clear();
                for (int i=0;i<response.length();i++){
                    try {
                        JSONObject json = (JSONObject) response.get(i);
                        mRegisteredDevices.add(json.getString("dvcKey"));
                        Log.d("RDEVICES", json.getString("dvcKey"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (!mAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    Log.d("Test", "Scanning for devices...");
                    mAdapter.startLeScan(scanCallback);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("RDEVICES", error.getMessage());
            }
        });
        queue.add(request);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshDevices() {
        mAdapter.stopLeScan(scanCallback);
        Log.d("REFRESH", "Refreshing...");
        startScanning();
    }
//
//    private void about() {
//        //switchToFragment(new AboutFragment().newInstance(), true);
//    }

    @Override
    public void OnResponseReceived(Masterpiece mp, boolean error) {
        if(!error){
            switchToFragment(new MasterpieceFragment().newInstance(mp, mCurrentLanguage), false);
            Log.d("Response", mp.getDvcName());
        } else { // Unsuccessful response
//            switchToFragment(new ErrorFragment().newInstance());
            Log.d("Response", "Error");
        }
    }

    private void switchToFragment(Fragment f, boolean add){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (add){
            fragmentTransaction.add(R.id.fragment_container, f);
        } else {
            fragmentTransaction.replace(R.id.fragment_container, f);
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onLanguageSelected(String language) {
        mCurrentLanguage = language;
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (mCurrentStart == mStart){
                    mActiveDevice = null;
                    switchToFragment(new WelcomeFragment().newInstance(), false);
                } else {
                    mCurrentStart = mStart;
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
