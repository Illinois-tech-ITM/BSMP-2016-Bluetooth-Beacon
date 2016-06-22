package edu.iit.bluetoothbeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

    public final int DISTANCE_DIFF = 20;

    private LinearLayout rootLayout;
    private ScrollView scrollView;
    private LinearLayout inScroll;
    private BluetoothAdapter adapter;
    private String activeDevice;
    private HashMap<String, Integer> devicesRssi;
    private HashMap<String, Integer> devicesColors;
    private HashMap<String, TextView> devicesViews;
    final private int[] colors = {Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GREEN, Color.MAGENTA, Color.LTGRAY};
    private int colorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(rootLayout);
        scrollView = new ScrollView(this);
        inScroll = new LinearLayout(this);
        inScroll.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(inScroll);
        rootLayout.addView(scrollView);

        adapter = BluetoothAdapter.getDefaultAdapter();

        devicesRssi = new HashMap<>();
        devicesColors = new HashMap<>();
        devicesViews = new HashMap<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startLeScan(scanCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopLeScan(scanCallback);
    }

    private void addTextView(TextView view, String text, String deviceAddress){
        TextView deviceInfoView = view;
        deviceInfoView.setText(text);
        inScroll.addView(deviceInfoView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));
        devicesViews.put(deviceAddress, deviceInfoView);
    }

    private void updateViews(){
        rootLayout.invalidate();
        /*for (String address : devicesViews.keySet()) {
            devicesViews.get(address).invalidate();
        }*/
    }

    private LeScanCallback scanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            if(!devicesColors.containsKey(bluetoothDevice.getAddress())){
                devicesColors.put(bluetoothDevice.getAddress(), colors[colorIndex % colors.length]);
                colorIndex++;
                addTextView(new TextView(MainActivity.this), bluetoothDevice.getAddress() + ": " + rssi, bluetoothDevice.getAddress());
            }
            devicesRssi.put(bluetoothDevice.getAddress(), rssi);
            devicesViews.get(bluetoothDevice.getAddress()).setText(bluetoothDevice.getAddress() + ": " + rssi);

            if(activeDevice == null){
                activeDevice = bluetoothDevice.getAddress();
            } else {
                if(devicesRssi.get(bluetoothDevice.getAddress()) > devicesRssi.get(activeDevice) + DISTANCE_DIFF) {
                    inScroll.setBackgroundColor(devicesColors.get(bluetoothDevice.getAddress()));
                    activeDevice = bluetoothDevice.getAddress();
                }
            }
            updateViews();
        }
    };
}
