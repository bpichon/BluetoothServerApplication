package edu.hm.network;

/**
 * Created by Bernd on 23.05.2015.
 */
public class PositionGPS {
    public final double longitude;
    public final double latitude;

    public PositionGPS(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
