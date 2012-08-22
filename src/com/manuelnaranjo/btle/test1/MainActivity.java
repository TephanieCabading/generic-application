
package com.manuelnaranjo.btle.test1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.broadcom.bt.le.api.BleAdapter;
import com.broadcom.bt.le.api.BleCharacteristic;
import com.stericson.RootTools.CommandCapture;
import com.stericson.RootTools.RootTools;

import java.util.ArrayList;

public class MainActivity extends Activity implements GenericClientProfile.Listener {
    private static boolean D = true;
    private static final String TAG = "BTLE-test";

    private BluetoothAdapter mAdapter;
    private TextView mTextView;
    private EditText mSendText;
    private ScrollView mScroll;
    private BluetoothDevice mTarget;
    private GenericClientProfile mService;
    private BleCharacteristic mChar;

    private static final int DEVICE_SELECT = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int SERVICE_SELECT = 3;

    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        if (!mAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!BleAdapter.checkAPIAvailability()) {
            CommandCapture cmd = new CommandCapture(0,
                    "CLASSPATH='/system/framework/btle-framework.jar:/system/framework/am.jar' " +
                            "/system/bin/app_process /system/bin " +
                            "--nice-name=btle-framework " +
                            "android.bluetooth.le.server.Main");
            try {
                RootTools.getShell(true).add(cmd);
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!BleAdapter.checkAPIAvailability()) {
                Toast.makeText(getApplicationContext(), "BTLE API not available", Toast.LENGTH_LONG)
                        .show();
                Log.e(TAG, "no btle api!");
            } else {
                Toast.makeText(getApplicationContext(), "BTLE API started", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "BTLE API ready", Toast.LENGTH_SHORT)
                    .show();
        }

        Button scanButton = (Button) findViewById(R.id.btnInquiry);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InquiryActivity.class);
                startActivityForResult(intent, DEVICE_SELECT);
            }
        });

        mTextView = (TextView) findViewById(R.id.txtLog);
        mScroll = (ScrollView) findViewById(R.id.scroll);

        mSendText = (EditText) findViewById(R.id.editSend);

        Button sendButton = (Button) findViewById(R.id.btnSend);
        sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mService == null)
                    return;
                String t = mSendText.getText().toString().trim();
                if (t.length()==0)
                    return;
                mService.writeCharacteristic(mTarget, mChar,
                        Integer.parseInt(t));

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    private void resolveServices(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(InquiryActivity.EXTRA_DEVICE_ADDRESS);
        mTarget = mAdapter.getRemoteDevice(address);
        Intent intent = new Intent(MainActivity.this, UuidActivity.class);
        intent.putExtra(UuidActivity.EXTRA_DEVICE_ADDRESS, address);
        startActivityForResult(intent, SERVICE_SELECT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case DEVICE_SELECT:
                // When InquiryActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    resolveServices(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {

                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
                break;
            case SERVICE_SELECT:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Something failed with uuid resolving "
                            + resultCode, Toast.LENGTH_LONG).show();
                    break;
                }

                String addr = data.getStringExtra(UuidActivity.EXTRA_DEVICE_ADDRESS);
                String uuid = data.getStringExtra(UuidActivity.EXTRA_DEVICE_UUID);

                if (D)
                    Log.v(TAG, "using " + uuid + " on address " + addr);

                Toast.makeText(getApplicationContext(), "using " + uuid + " on address " + addr,
                        Toast.LENGTH_LONG).show();

                BluetoothDevice btDev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);

                ArrayList<String> uuids = new ArrayList<String>();
                uuids.add(uuid);
                mService = GenericClientProfile.buildProfile(getApplicationContext(), uuids, this);
                mService.connect(btDev);
        }
    }

    private void appendText(final String t) {
        runOnUiThread(new Runnable() {
            public void run()
            {
                mTextView.append(t);
                mScroll.pageScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void gotCharacteristic(final BleCharacteristic c) {
        appendText("got characteristic: " + c.getValueInt() + "\n");
        mChar = c;
    }

    @Override
    public void refreshed(final String addr) {
        appendText("refreshed: " + addr + "\n");
    }

    @Override
    public void connected(String addr) {
        appendText("connected: " + addr + "\n");

    }

    @Override
    public void disconnected(String addr) {
        appendText("disconnected: " + addr + "\n");
    }
}
