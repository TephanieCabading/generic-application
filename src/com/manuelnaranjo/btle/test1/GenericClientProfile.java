
package com.manuelnaranjo.btle.test1;

import android.content.Context;

import com.broadcom.bt.le.api.BleClientProfile;
import com.broadcom.bt.le.api.BleClientService;
import com.broadcom.bt.le.api.BleGattID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenericClientProfile extends BleClientProfile {
    // Service(s) used by this profile
    private ArrayList<BleClientService> mServices = new ArrayList<BleClientService>();


    public GenericClientProfile(Context ctxt, BleGattID myUuid, List<BleClientService> services) {
        super(ctxt, myUuid);

        mServices.addAll(services);
        init(mServices, null);
    }

    public static GenericClientProfile buildProfile(Context ctxt, List<String> uuids) {
        BleGattID myUuid = new BleGattID(UUID.randomUUID());
        List<BleClientService> services = new ArrayList<BleClientService>();
        for(String uuid: uuids){
            services.add(GenericClientService.createService(uuid));
        }
        GenericClientProfile out = new GenericClientProfile(ctxt, myUuid, services);
        
        return out;
    }

}
