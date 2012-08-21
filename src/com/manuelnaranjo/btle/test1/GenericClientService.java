
package com.manuelnaranjo.btle.test1;

import android.bluetooth.BluetoothDevice;

import com.broadcom.bt.le.api.BleCharacteristic;
import com.broadcom.bt.le.api.BleClientService;
import com.broadcom.bt.le.api.BleGattID;

import java.util.HashMap;
import java.util.Map;

public class GenericClientService extends BleClientService {
    static private Map<BleGattID, GenericClientService> services = new
            HashMap<BleGattID, GenericClientService>();

    public GenericClientService(BleGattID id) {
        super(id);
    }

    public static GenericClientService createService(String uuid) {
        BleGattID gattId = new BleGattID(uuid);
        
        if (services.containsKey(gattId))
            return services.get(gattId);
        
        GenericClientService out = new GenericClientService(gattId);
        services.put(gattId, out);
        return out;
    }

    @Override
    public void onWriteCharacteristicComplete(int status, BluetoothDevice remoteDevice,
            BleCharacteristic characteristic) {
        // TODO Auto-generated method stub
        super.onWriteCharacteristicComplete(status, remoteDevice, characteristic);
    }

    @Override
    public void onCharacteristicChanged(BluetoothDevice remoteDevice,
            BleCharacteristic characteristic) {
        // TODO Auto-generated method stub
        super.onCharacteristicChanged(remoteDevice, characteristic);
    }

    @Override
    public void onRefreshComplete(BluetoothDevice remoteDevice) {
        // TODO Auto-generated method stub
        super.onRefreshComplete(remoteDevice);
    }

    @Override
    public void onSetCharacteristicAuthRequirement(BluetoothDevice remoteDevice,
            BleCharacteristic characteristic, int instanceID) {
        // TODO Auto-generated method stub
        super.onSetCharacteristicAuthRequirement(remoteDevice, characteristic, instanceID);
    }

    @Override
    public void onReadCharacteristicComplete(BluetoothDevice remoteDevice,
            BleCharacteristic characteristic) {
        // TODO Auto-generated method stub
        super.onReadCharacteristicComplete(remoteDevice, characteristic);
    }

    @Override
    public void onReadCharacteristicComplete(int status, BluetoothDevice remoteDevice,
            BleCharacteristic characteristic) {
        // TODO Auto-generated method stub
        super.onReadCharacteristicComplete(status, remoteDevice, characteristic);
    }

    @Override
    protected void onServiceRefreshed(int connID) {
        // TODO Auto-generated method stub
        super.onServiceRefreshed(connID);
    }

}
