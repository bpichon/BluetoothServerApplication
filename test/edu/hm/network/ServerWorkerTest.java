package edu.hm.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.*;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Bernd on 24.05.2015.
 */
public class ServerWorkerTest {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private static DatabaseHelper dbHelper = DatabaseHelper.getInstance("jdbc:sqlite:test_database.db");

    private static Server server;

    @BeforeClass
    public static void setUp() throws Exception {
        server = new Server();
        server.start();
    }

    @Test
    public void testHandleRequestGetReturnCode2() throws IOException {
        createSocketAndStreams(); // Before

        // Geräte "scannedDevice01", "scannedDevice02", "scannedDevice03" gibts noch nicht.
        String request = "{" +
                "\"requestCode\": 0, " +
                "\"deviceId\": \"testDevice01\", " +
                "\"deviceIds\": [" +
                "   \"scannedDevice01\"," +
                "   \"scannedDevice02\"," +
                "   \"scannedDevice03\"" +
                "   ]" +
                "}";
        out.write(request);
        out.newLine();
        out.flush();

        // Es wird nur eine Zeile gelesen.
        String response = in.readLine();

        JsonObject result = new JsonParser().parse(response).getAsJsonObject();
        int errorCode = result.get("returnCode").getAsInt();

        // ErrorCode
        Assert.assertEquals(2, errorCode);

        closeSocketAndStreams(); // After
    }

    @Test
    public void testHandleRequestGetSuccess() throws IOException {
        createSocketAndStreams();
        ArrayList<Integer> insertIds = new ArrayList<>();
        final int time = TimeHelper.getTime();
        // Gertät 1
        insertIds.add(dbHelper.insertDevice("test_01", time, 5));
        insertIds.add(dbHelper.insertDevice("test_01", time, 5));
        insertIds.add(dbHelper.insertDevice("test_01", time, 5));
        // Gerät 2
        insertIds.add(dbHelper.insertDevice("test_02", time, 5));
        insertIds.add(dbHelper.insertDevice("test_02", time, 5));
        insertIds.add(dbHelper.insertDevice("test_02", time, 5));
        // Gertät 3
        insertIds.add(dbHelper.insertDevice("test_03", time, 5));
        insertIds.add(dbHelper.insertDevice("test_03", time, 5));
        insertIds.add(dbHelper.insertDevice("test_03", time, 5));
        // Gerät 4
        insertIds.add(dbHelper.insertDevice("test_04", time, 5));
        insertIds.add(dbHelper.insertDevice("test_04", time, 5));
        insertIds.add(dbHelper.insertDevice("test_04", time, 5));


        // Geräte "test_00" gibt es vor dem Test noch nicht
        String request = "{" +
                "\"requestCode\": 0, " +
                "\"deviceId\": \"test_00\", " +
                "\"deviceIds\": [" +
                "   \"test_01\"," +
                "   \"test_02\"," +
                "   \"test_03\"" +
                "   ]" +
                "}";
        out.write(request);
        out.newLine();
        out.flush();

        // Es wird nur eine Zeile gelesen.
        String response = in.readLine();

        JsonObject result = new JsonParser().parse(response).getAsJsonObject();
        int errorCode = result.get("returnCode").getAsInt();
        Assert.assertEquals(0, errorCode);

        double long_ = result.get("long").getAsDouble();
        double lat = result.get("lat").getAsDouble();

        // Ein Eintrag dazugekommen.
        ArrayList<String> entry = new ArrayList<>();
        entry.add("test_00");
        Map<String, ArrayList<Integer>> insertResult = dbHelper.getDeviceOccurance(entry, time);
        ArrayList<Integer> locationClasses = insertResult.get("test_00");
        Assert.assertNotNull(locationClasses);
        Assert.assertEquals(1, locationClasses.size());
        // Weil alle k-Nachbarn die LocationClass 5 besitzen muss auch das eingefügt Element LC 5 haben.
        Assert.assertEquals(new Integer(5), locationClasses.get(0));


        // Aufräumen Einträge aus DB löschen
        for (int id : insertIds) {
            dbHelper.removeEntry(id);
        }
        dbHelper.removeEntries("test_00", time);
        closeSocketAndStreams();
    }

    @Test
    public void testHandleRequestGetSuccess2() throws IOException {
        createSocketAndStreams();
        ArrayList<Integer> insertIds = new ArrayList<>();
        final int time = TimeHelper.getTime();
        // Gertät 1
        insertIds.add(dbHelper.insertDevice("test_01", time, 5));
        insertIds.add(dbHelper.insertDevice("test_01", time, 6));
        insertIds.add(dbHelper.insertDevice("test_01", time, 8));
        insertIds.add(dbHelper.insertDevice("test_01", time, 8));
        // Gerät 2
        insertIds.add(dbHelper.insertDevice("test_02", time, 6));
        insertIds.add(dbHelper.insertDevice("test_02", time, 6));
        insertIds.add(dbHelper.insertDevice("test_02", time, 6));
        // Gertät 3
        insertIds.add(dbHelper.insertDevice("test_03", time, 7));
        insertIds.add(dbHelper.insertDevice("test_03", time, 7));
        insertIds.add(dbHelper.insertDevice("test_03", time, 7));
        // Gerät 4
        insertIds.add(dbHelper.insertDevice("test_04", time, 5));
        insertIds.add(dbHelper.insertDevice("test_04", time, 6));
        insertIds.add(dbHelper.insertDevice("test_04", time, 8));

        // NR: G1, G2, G4 als KNN
        // 5+5+6+6+6+6+6+8+8+8=64
        // 64/10 = 6.4 => 6 () Weil alle in einer Reihe liegen.

        // Geräte "test_00" gibt es vor dem Test noch nicht
        String request = "{" +
                "\"requestCode\": 0, " +
                "\"deviceId\": \"test_00\", " +
                "\"deviceIds\": [" +
                "   \"test_01\"," +
                "   \"test_02\"," +
                "   \"test_04\"," +
                "   \"test_03\"" +
                "   ]" +
                "}";
        out.write(request);
        out.newLine();
        out.flush();

        // Es wird nur eine Zeile gelesen.
        String response = in.readLine();

        JsonObject result = new JsonParser().parse(response).getAsJsonObject();
        int errorCode = result.get("returnCode").getAsInt();
        Assert.assertEquals(0, errorCode);

        double long_ = result.get("long").getAsDouble();
        double lat = result.get("lat").getAsDouble();

        // Ein Eintrag dazugekommen.
        ArrayList<String> entry = new ArrayList<>();
        entry.add("test_00");
        Map<String, ArrayList<Integer>> insertResult = dbHelper.getDeviceOccurance(entry, time);
        ArrayList<Integer> locationClasses = insertResult.get("test_00");
        Assert.assertNotNull(locationClasses);
        Assert.assertEquals(1, locationClasses.size());
        // Weil alle k-Nachbarn die LocationClass 5 besitzen muss auch das eingefügt Element LC 5 haben.
        Assert.assertEquals(new Integer(6), locationClasses.get(0));


        // Aufräumen Einträge aus DB löschen
        for (int id : insertIds) {
            dbHelper.removeEntry(id);
        }
        dbHelper.removeEntries("test_00", time);
        closeSocketAndStreams();
    }

    @Test
    public void testHandleRequestGetSuccess3() throws IOException {
        createSocketAndStreams();
        ArrayList<Integer> insertIds = new ArrayList<>();
        final int time = TimeHelper.getTime();
        // Gertät 1
        insertIds.add(dbHelper.insertDevice("test_01", time, 6));
        insertIds.add(dbHelper.insertDevice("test_01", time, 12));
        insertIds.add(dbHelper.insertDevice("test_01", time, 11));
        insertIds.add(dbHelper.insertDevice("test_01", time, 7));
        // Gerät 2
        insertIds.add(dbHelper.insertDevice("test_02", time, 6));
        insertIds.add(dbHelper.insertDevice("test_02", time, 7));
        insertIds.add(dbHelper.insertDevice("test_02", time, 8));
        // Gertät 3
        insertIds.add(dbHelper.insertDevice("test_03", time, 11));
        insertIds.add(dbHelper.insertDevice("test_03", time, 12));
        insertIds.add(dbHelper.insertDevice("test_03", time, 13));
        // Gerät 4
        insertIds.add(dbHelper.insertDevice("test_04", time, 6));
        insertIds.add(dbHelper.insertDevice("test_04", time, 7));
        insertIds.add(dbHelper.insertDevice("test_04", time, 2));

        // NR: G1, G2, G4 als KNN

        // Geräte "test_00" gibt es vor dem Test noch nicht
        String request = "{" +
                "\"requestCode\": 0, " +
                "\"deviceId\": \"test_00\", " +
                "\"deviceIds\": [" +
                "   \"test_01\"," +
                "   \"test_02\"," +
                "   \"test_04\"," +
                "   \"test_03\"" +
                "   ]" +
                "}";
        out.write(request);
        out.newLine();
        out.flush();

        // Es wird nur eine Zeile gelesen.
        String response = in.readLine();

        JsonObject result = new JsonParser().parse(response).getAsJsonObject();
        int errorCode = result.get("returnCode").getAsInt();
        Assert.assertEquals(0, errorCode);

        double long_ = result.get("long").getAsDouble();
        double lat = result.get("lat").getAsDouble();

        // Ein Eintrag dazugekommen.
        ArrayList<String> entry = new ArrayList<>();
        entry.add("test_00");
        Map<String, ArrayList<Integer>> insertResult = dbHelper.getDeviceOccurance(entry, time);
        ArrayList<Integer> locationClasses = insertResult.get("test_00");
        Assert.assertNotNull(locationClasses);
        Assert.assertEquals(1, locationClasses.size());
        // Weil alle k-Nachbarn die LocationClass 5 besitzen muss auch das eingefügt Element LC 5 haben.
        Assert.assertEquals(new Integer(7), locationClasses.get(0));


        // Aufräumen Einträge aus DB löschen
        for (int id : insertIds) {
            dbHelper.removeEntry(id);
        }
        dbHelper.removeEntries("test_00", time);
        closeSocketAndStreams();
    }

    @After
    public void clearDB() throws SQLException {
        dbHelper.drop();
        dbHelper.init();
    }

    @AfterClass
    public static void clear() {
        server.interrupt();
    }

    private void createSocketAndStreams() throws IOException {
        socket = new Socket("localhost", Server.PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private void closeSocketAndStreams() throws IOException {
        in.close();
        out.close();
        socket.close();

        in = null;
        out = null;
        socket = null;
    }

}