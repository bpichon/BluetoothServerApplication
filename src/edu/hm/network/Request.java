package edu.hm.network;

import java.util.ArrayList;

/**
 * Wrapperklasse.
 */
public abstract class Request {

    final int id;

    public String deviceId;

    public Request(int id, String deviceId) {
        this.id = id;
        this.deviceId = deviceId;
    }
}


class GetPositionRequest extends Request {

    /**
     * Request-Code
     */
    public final static int REQUEST_CODE = 0;

    /**
     * Geräte-Ids
     */
    public ArrayList<String> ids = new ArrayList<>();

    public GetPositionRequest(int id, String deviceId) {
        super(id, deviceId);
    }

    public void addDeviceId(final String id) {
        ids.add(id);
    }
}

class SavePositionRequest extends Request {
    /**
     * Request-Code
     */
    public final static int REQUEST_CODE = 1;


    /**
     * Breitengrad
     */
    public PositionGPS position;

     public SavePositionRequest(int id, String deviceId, PositionGPS position) {
        super(id, deviceId);
        this.position = position;
    }
}
