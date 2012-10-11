
package com.manuelnaranjo.btle.test1;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.broadcom.bt.le.api.BleCharacteristic;
import com.broadcom.bt.le.api.BleClientProfile;
import com.broadcom.bt.le.api.BleClientService;
import com.broadcom.bt.le.api.BleGattID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class GenericClientProfile extends BleClientProfile {
    interface Listener {
        public void gotCharacteristic(BleCharacteristic c);

        public void connected(String addr);

        public void refreshed(String addr);

        public void disconnected(String addr);
    }

    private static String TAG = "GCP";

    // Service(s) used by this profile
    private ArrayList<BleClientService> mServices = new ArrayList<BleClientService>();
    private Listener mListener;
    private Timer mTimer = new Timer();

    public GenericClientProfile(Context ctxt, BleGattID myUuid,
            List<BleClientService> services, Listener l) {
        super(ctxt, myUuid);

        mServices.addAll(services);
        init(mServices, null);

        mListener = l;
    }

    public static GenericClientProfile buildProfile(Context ctxt, List<String> uuids, Listener l) {
        BleGattID myUuid = new BleGattID(UUID.randomUUID());
        List<BleClientService> services = new ArrayList<BleClientService>();
        for (String uuid : uuids) {
            services.add(GenericClientService.createService(uuid));
        }
        GenericClientProfile out = new GenericClientProfile(ctxt, myUuid, services, l);

        return out;
    }

    class refreshTimer extends TimerTask {
        BluetoothDevice dev;

        public refreshTimer(BluetoothDevice d) {
            dev = d;
        }

        public void run() {
            GenericClientProfile.this.refresh(dev);
        }
    }

    private Map<BluetoothDevice, TimerTask> mTasks = new HashMap<BluetoothDevice, TimerTask>();

    @Override
    public synchronized void onDeviceConnected(BluetoothDevice device) {
        super.onDeviceConnected(device);
        mListener.connected(device.getName());

        TimerTask t = new refreshTimer(device);
        mTimer.scheduleAtFixedRate(t, 0, 5000);
        mTasks.put(device, t);

        for (BleClientService s : this.mServices) {
            if (GenericClientService.class.isInstance(s)) {
                GenericClientService gs = (GenericClientService) s;
                s.registerForNotification(device, 0, gs.getServiceId());
            }
        }
    }

    public void writeCharacteristic(BluetoothDevice r, BleCharacteristic c, int value) {
        if (this.mServices.size() == 0)
            return;
        GenericClientService s = (GenericClientService) this.mServices.get(0);
        if (c != null)
            c.setValue(value);
        s.writeCharacteristic(r, 0, c);
    }

    public void onRefreshed(BluetoothDevice device) {
        // get the services
        mListener.refreshed(device.getName());

        for (BleClientService s : this.mServices) {
            List<BleCharacteristic> chars = s.getAllCharacteristics(device);
            for (BleCharacteristic c : chars) {
                Log.d(TAG, "Characteristic " + c);
                s.readCharacteristic(device, c);
                if (mListener != null)
                    mListener.gotCharacteristic(c);
                c.getValue();
            }
        }

        // // Assign a new value
        // byte[] value = { FindMeProfileClient.ALERT_LEVEL_HIGH };
        // alertLevelCharacteristic.setValue(value);

        // Write the characteristic
        // mImmediateAlertService.writeCharacteristic(device, 0,
        // alertLevelCharacteristic);

    }

    @Override
    public synchronized void onDeviceDisconnected(BluetoothDevice device) {
        super.onDeviceDisconnected(device);

        mListener.disconnected(device.getName());
        mTasks.remove(device);
        mTimer.cancel();
        mTimer = new Timer();
        for (TimerTask t : mTasks.values())
            mTimer.scheduleAtFixedRate(t, 0, 1);
    }

}
