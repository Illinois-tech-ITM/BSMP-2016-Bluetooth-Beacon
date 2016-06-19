package edu.iit.bluetoothbeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends Activity {

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private TextView messages;
    private BluetoothAdapter adapter;
    private ScrollView scrollView;
    private String activeDevice;
    private HashMap<String, Integer> devicesRssi;
    private HashMap<String, Integer> devicesColors;
    final private int[] colors = {Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GREEN, Color.MAGENTA, Color.LTGRAY};
    private int colorIndex = 0;

    //private HashSet<String> mDevicesFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        messages = (TextView) findViewById(R.id.messages);
        adapter = BluetoothAdapter.getDefaultAdapter();

        devicesRssi = new HashMap<>();
        devicesColors = new HashMap<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        writeLine("Scanning for devices...");
        adapter.startLeScan(scanCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopLeScan(scanCallback);
    }

    private LeScanCallback scanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            //mDevicesFound.add(bluetoothDevice.getAddress());

            if(!devicesColors.containsKey(bluetoothDevice.getAddress())){
                devicesColors.put(bluetoothDevice.getAddress(), colors[colorIndex % colors.length]);
                colorIndex++;
            }
            devicesRssi.put(bluetoothDevice.getAddress(), rssi);

            if(activeDevice == null){
                activeDevice = bluetoothDevice.getAddress();

                for (String address : devicesRssi.keySet()) {
                    writeLine("Address: " + address + " | RSSI: "+ devicesRssi.get(address));
                }
            } else {
                if(devicesRssi.get(bluetoothDevice.getAddress()) > devicesRssi.get(activeDevice)) {
                    scrollView.setBackgroundColor(devicesColors.get(bluetoothDevice.getAddress()));
                    activeDevice = bluetoothDevice.getAddress();

                    for (String address : devicesRssi.keySet()) {
                        writeLine("Address: " + address + " | RSSI: "+ devicesRssi.get(address));
                    }
                }
            }
        }
    };

    private void writeLine(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.append(text);
                messages.append("\n");
            }
        });
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

}
