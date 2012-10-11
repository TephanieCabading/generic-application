
package com.manuelnaranjo.btle.test1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.broadcom.bt.le.api.BleAdapter;

public class UuidActivity extends Activity {
    // Debugging
    private static final String TAG = "UuidActivity";
    private static final boolean D = true;

    // Return Intent extra
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final String EXTRA_DEVICE_UUID = "device_uuid";
    public static final int RESULT_WRONG_CALL = 100;
    public static final int RESULT_FAILED_DISCOVERY = 101;

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private String mAddress;
    private ArrayAdapter<ParcelUuid> mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAddress = this.getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);

        if (mAddress == null) {
            // oops!
            setResult(RESULT_WRONG_CALL);
            finish();
        }
        
        mAddress = mAddress.toUpperCase();

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.uuid_list);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mArrayAdapter = new ArrayAdapter<ParcelUuid>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.uuids_list);
        pairedListView.setAdapter(mArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BleAdapter.ACTION_UUID);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // make sure BleAdapter is initialized
        new BleAdapter(this.getApplicationContext());

        if (BleAdapter.getRemoteServices(mAddress) == false) {
            // oops!
            Intent out = new Intent();
            out.putExtra(EXTRA_DEVICE_ADDRESS, mAddress);
            setResult(RESULT_FAILED_DISCOVERY, out);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // get the uuid, it's the string before the first space.
            String info = ((TextView) v).getText().toString();
            String uuid = info.split("\\s")[0];

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, mAddress);
            intent.putExtra(EXTRA_DEVICE_UUID, uuid);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // new UUID result?
            if (!BleAdapter.ACTION_UUID.equals(action)) {
                return;
            }

            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BleAdapter.EXTRA_DEVICE);

            if (D)
                Log.v(TAG, "ACTION_UUID");

            if (!mAddress.equals(device.getAddress().toUpperCase())) {
                if (D)
                    Log.v(TAG, "signal from a different address");
                return;
            }

            // get uuid
            ParcelUuid uuid = intent.getParcelableExtra(BleAdapter.EXTRA_UUID);
            if (uuid == null) {
                Toast.makeText(getApplicationContext(), "UUIDs resolving complete",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mArrayAdapter.add(uuid);
            
        }
    };

}
