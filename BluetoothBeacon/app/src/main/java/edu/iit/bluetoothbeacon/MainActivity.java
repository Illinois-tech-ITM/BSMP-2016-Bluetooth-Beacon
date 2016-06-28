package edu.iit.bluetoothbeacon;

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
import android.widget.TextView;

import java.util.HashMap;

import edu.iit.bluetoothbeacon.models.Masterpiece;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements OnResponseReceivedListener {
    private final static int MIN_RSSI = -70;
    private final static int NEARBY_RSSI = -55;
    private static final int REQUEST_ENABLE_BT = 1;

    private Controller controller;

    private BluetoothAdapter mAdapter;
    private BluetoothDevice mActiveDevice;
    private HashMap<BluetoothDevice, Integer> mDevicesList; //key: Beacon | value: RSSI (sinal strength)

    private TextView mTitleTextView;
    private TextView mDescriptionTextView;

    private String mCurrentLanguage = "pt-br";
    private MenuItem mLanguageMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        mDevicesList = new HashMap<>();
        mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mDescriptionTextView = (TextView) findViewById(R.id.descTextView);
        mDescriptionTextView.setVisibility(GONE);

        controller = Controller.getInstance(this, this);
    }

    private LeScanCallback scanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            if (bluetoothDevice.getName() == null || !bluetoothDevice.getName().matches("DVC\\d\\d\\d\\d")) return;
            Log.d("Test", "Address: " + bluetoothDevice + " | RSSI: "+ rssi);
            mDevicesList.put(bluetoothDevice, rssi);
            if (mActiveDevice == null && rssi > NEARBY_RSSI){
                mActiveDevice = bluetoothDevice;
                updateView("Requesting data", null);
                controller.requestMasterpieceInfo(bluetoothDevice.getName().toLowerCase(), mCurrentLanguage);
                return;
            }

            if (mActiveDevice != null && rssi - mDevicesList.get(mActiveDevice) > 20){
                mActiveDevice = bluetoothDevice;
                //updateView(bluetoothDevice.getTitle());
                updateView("Requesting data", null);
                controller.requestMasterpieceInfo(bluetoothDevice.getName().toLowerCase(), mCurrentLanguage);
            } else if (mActiveDevice != null && bluetoothDevice.getAddress().equals(mActiveDevice.getAddress()) && rssi < MIN_RSSI){
                mActiveDevice = null;
                updateView(":(", null);
                mDescriptionTextView.setVisibility(GONE);
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
        }
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
        mLanguageMenuItem = menu.findItem(R.id.languageMenu);
        updateMenuTitle(mCurrentLanguage);
        return true;
    }

    @Override
    public void OnResponseReceived(Masterpiece mp, boolean error) {
        if(!error){
            updateView(mp.getTitle(), mp.getContent());
            Log.d("Response", mp.getTitle() + ": " + mp.getContent());
        } else { // Unsuccessful response
            updateView(":/", null);
            mDescriptionTextView.setVisibility(View.GONE);
            Log.d("Response", "Error");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.languageMenu) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select your language: ");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    MainActivity.this,
                    android.R.layout.select_dialog_singlechoice);
            arrayAdapter.add("pt-br");
            arrayAdapter.add("en-us");

            builder.setNegativeButton("Cancel", null);

            builder.setAdapter(
                    arrayAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mCurrentLanguage = arrayAdapter.getItem(which);
                            updateView("Requesting data", null);
                            controller.requestMasterpieceInfo(mActiveDevice.getName().toLowerCase(), mCurrentLanguage);
                            updateMenuTitle(mCurrentLanguage);
                            mDescriptionTextView.setVisibility(GONE);
                        }
                    });
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateView(String title, String content){
        mTitleTextView.setText(title);
        if(content != null) {
            mDescriptionTextView.setText(content);
            mDescriptionTextView.setVisibility(View.VISIBLE);
        }
    }

    private void updateMenuTitle(String language) {
        mLanguageMenuItem.setTitle(language);
    }
}
