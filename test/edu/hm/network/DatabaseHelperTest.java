package edu.hm.network;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Bernd on 24.05.2015.
 */
public class DatabaseHelperTest {

    private static DatabaseHelper dbHelper = DatabaseHelper.getInstance("jdbc:sqlite:test_database.db");

    @Test
    public void testGetInstance() throws Exception {
        Assert.assertNotNull(dbHelper);
        DatabaseHelper secondDbHelper = DatabaseHelper.getInstance();
        Assert.assertEquals(dbHelper, secondDbHelper);
    }

    @Test
    public void testInsertDevice() throws Exception {
        final String deviceId = "test_0123";
        final int locationClass = 1;
        final int time = 1;

        /* Prepare. Entry should not be in DB. */
        ArrayList<String> devices = new ArrayList<>();
        devices.add(deviceId);
        Map<String, ArrayList<Integer>> result = dbHelper.getDeviceOccurance(devices, time);
        Assert.assertEquals("Device already in DB", 0, result.size());

        /* Insert Data */
        int row = dbHelper.insertDevice(deviceId, time, locationClass);

        /* Test if Insert was successfully */
        result = dbHelper.getDeviceOccurance(devices, time);
        Assert.assertEquals("Device should be in DB.", 1, result.size());

        /* Clean up. Delete Entry from DB. */
        dbHelper.removeEntry(row);
    }

}