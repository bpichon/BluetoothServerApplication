package edu.hm.network;

/**
 * Created by Bernd on 23.05.2015.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class DatabaseHelper {

    private static DatabaseHelper dbHelper;
    private Connection c;

    public void init() throws SQLException {
        final Statement s = c.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS DEVICE_LOCATION " +
                "(ID            INTEGER PRIMARY KEY     AUTOINCREMENT," +
                " device_id     TEXT        NOT NULL, " +
                " time          INTEGER     NOT NULL, " +
                " location_class     INTEGER)";
        s.executeUpdate(createQuery);
    }

    public void drop() throws SQLException {
        final Statement s = c.createStatement();
        String dropQuery = "DROP TABLE IF EXISTS DEVICE_LOCATION";
        s.executeUpdate(dropQuery);
    }

    public static DatabaseHelper getInstance() {
        return getInstance("jdbc:sqlite:database.db");
    }

    public static DatabaseHelper getInstance(String name) {
        if (dbHelper == null) {
            try {
                dbHelper = new DatabaseHelper(name);
            } catch (SQLException e) {
                e.printStackTrace();
                return null; // Wenn Connector nicht init werden kann return null.
            }
        }
        try {
            dbHelper.init();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return dbHelper;
    }

    private DatabaseHelper(String name) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        c = DriverManager.getConnection(name);
        init();
    }

    public int insertDevice(String deviceId, int time, int locationClass) {
        // ID autoincrement
        String query = "INSERT INTO `DEVICE_LOCATION` (`device_id`, `time`, `location_class`) VALUES ('" + deviceId + "', " + time + ", " + locationClass + ");";
        System.out.println(query);
        try {
            final Statement s = c.createStatement();
            s.executeUpdate(query);
            return s.getGeneratedKeys().getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Debugging Method. Prints all devices
     */
    public void printAll() {
        try {
            final Statement s = c.createStatement();
            ResultSet resultSet;
            resultSet = s.executeQuery(""
                    + "SELECT * "
                    + "FROM `DEVICE_LOCATION`");

            while (resultSet.next()) {
                System.out.printf(
                        "DevId: '%s' | Class: %s | Time: %s\n",
                        resultSet.getString("device_id"),
                        resultSet.getInt("location_class"),
                        resultSet.getInt("time"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, ArrayList<Integer>> getDeviceOccurance(List<String> deviceIds, int time) {
        final Map<String, ArrayList<Integer>> data = new TreeMap<>();

        final String query = ""
                + "SELECT * "
                + "FROM `DEVICE_LOCATION` "
                + "WHERE `device_id` IN ('" + implode("', '", deviceIds) + "') "
                + "AND time = " + time + ";";

        try {
            final Statement s = c.createStatement();
            final ResultSet resultSet;
            resultSet = s.executeQuery(query);
            while (resultSet.next()) {
                final String deviceId = resultSet.getString("device_id");
                final int locationClass = resultSet.getInt("location_class");

                // Wenn noch keine LocationClass zum Device gespeichert wurde, erstelle einen neuen Map-Eintrag
                if (!data.containsKey(deviceId)) {
                    ArrayList<Integer> value = new ArrayList<>();
                    value.add(locationClass);
                    data.put(deviceId, value);
                }
                // Ansonsten speichere die locationClass einfach zum vorhandenen.
                else {
                    data.get(deviceId).add(locationClass);
                }
            }
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int removeEntry(int id) {
        final String query = "DELETE FROM `DEVICE_LOCATION` WHERE `id` =" + id + ";";
        try {
            final Statement s = c.createStatement();
            return s.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int removeEntries(String deviceId, int time) {
        final String query = "DELETE FROM `DEVICE_LOCATION` WHERE `device_id` = '" + deviceId + "' AND `time` =" + time + ";";
        try {
            final Statement s = c.createStatement();
            return s.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static String implode(String separator, List<String> list) {
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            b.append(list.get(i));
            if (i < list.size() - 1) {
                b.append(separator);
            }
        }
        return b.toString();

    }
}
