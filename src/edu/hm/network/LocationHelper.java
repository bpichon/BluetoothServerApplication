package edu.hm.network;

/**
 * Created by Bernd on 23.05.2015.
 */
public class LocationHelper {


    public static void main(String... args) {

        double d = getDistance(new PositionGPS(48.250173, 11.525404), new PositionGPS(48.235368, 11.505019));

        System.out.println("Distance: " + d);
    }


    public static int getLocationClassFromGPS(final PositionGPS position) {
        // abweichender Längengrad
        double deltaLong = position.longitude - ServerConfig.startLongDouble;
        // abweichender Breitengrad
        double deltaLat = position.latitude - ServerConfig.startLatDouble;

        // Breitengrad -_-_-_
        int locationClassRow = (int) (((ServerConfig.endLatDouble - ServerConfig.startLatDouble) / ServerConfig.rowAmount) * deltaLat);

        // Längengrad ||||||
        int locationClassColumn = (int) (((ServerConfig.endLongDouble - ServerConfig.startLongDouble) / ServerConfig.columnAmount) * deltaLong);

        // Nummerierung Aufsteigend der Reihe nach.
        int locationClass = locationClassColumn + locationClassRow * ServerConfig.columnAmount;

        return locationClass;
    }

    public static PositionGPS getGPSFromLocationClass(final int locationClass) {

        int row = locationClass / ServerConfig.columnAmount;
        int column = locationClass % ServerConfig.columnAmount;

        double latitude = ServerConfig.startLatDouble + row * ((ServerConfig.endLatDouble - ServerConfig.startLatDouble) / ServerConfig.rowAmount);
        double longitude = ServerConfig.startLongDouble + column * ((ServerConfig.endLongDouble - ServerConfig.startLongDouble) / ServerConfig.columnAmount);
        return new PositionGPS(longitude, latitude);
    }

    // Not correct!!!!
    @Deprecated
    public static double getDistance(final PositionGPS pos1, final PositionGPS pos2) {
        double R = 6371000; // meters
        double z1 = pos1.latitude * Math.PI / 180;
        double z2 = pos2.latitude * Math.PI / 180;
        double dz = (pos2.latitude-pos1.latitude) * Math.PI / 180;
        double dA = (pos2.longitude-pos1.longitude) * Math.PI / 180;

        double a = Math.sin(dz/2) * Math.sin(dz/2) +
                Math.cos(z1) * Math.cos(z2) *
                        Math.sin(dA/2) * Math.sin(dA/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double d = R * c;
        return d;
    }

    public static class LocationClassGridPosition {
        public final double column;
        public final double row;

        public LocationClassGridPosition(int column, int row) {
            this.column = column;
            this.row = row;
        }

        public LocationClassGridPosition(double column, double row) {
            this.column = column;
            this.row = row;
        }

        public static LocationClassGridPosition fromLocationClass(int locationClass) {
            int column = locationClass % ServerConfig.columnAmount;
            int row = locationClass / ServerConfig.columnAmount;
            return new LocationClassGridPosition(column, row);
        }

        public int toLocationClass() {
            return ((int) column) + ((int) row)*ServerConfig.columnAmount;
        }
    }

}
