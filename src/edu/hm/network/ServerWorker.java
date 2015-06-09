package edu.hm.network;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Bernd on 23.05.2015.
 */
public class ServerWorker implements Runnable {

    private static int requestCounter = 0;
    private final Socket clientSocket;

    DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    public ServerWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        Request request = null;
        try {
            request = parseRequest();
        } catch (IOException e) {
            // TODO: impl
            e.printStackTrace();
            return;
        } catch (RequestDoesNotExistException e) {
            sendError(3); // Request konnte nicht geparst werden
            e.printStackTrace();
            return;
        }

        try {
            handleRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Request request) throws IOException {
        if(request instanceof GetPositionRequest) {
            // Sendet Position
            handleGetPositionRequest((GetPositionRequest) request);
        }

        if (request instanceof SavePositionRequest) {
            // Hat keine Response
            handleSavePositionRequest((SavePositionRequest) request);
        }
        clientSocket.close();
    }

    /**
     * Behandelt ein {@link SavePositionRequest}.
     * Speichert die Position in der DB.
     * Es wird eine Erfolgs-Rückmeldung an den Client gesendet.
     * @param request
     */
    private void handleSavePositionRequest(SavePositionRequest request) {
        databaseHelper.insertDevice(request.deviceId, TimeHelper.getTime(), LocationHelper.getLocationClassFromGPS(request.position));
        sendSuccess();
    }

    /**
     * Behandelt ein {@link GetPositionRequest}.
     *
     * @param request
     */
    private void handleGetPositionRequest(GetPositionRequest request) throws IOException {
        Map<String, ArrayList<Integer>> occuranceMap = databaseHelper.getDeviceOccurance(request.ids, TimeHelper.getTime());
        int locationClass = -1;
        try {
            locationClass = PositionDeterminationHelper.determinateLocationClass(occuranceMap);
        } catch (NotEnoughDevicesException ex) {
            sendError(2); // Nicht genug Geräte verfügbar
            return;
        }

        databaseHelper.insertDevice(request.deviceId, TimeHelper.getTime(), locationClass);
        sendPositionToSocket(LocationHelper.getGPSFromLocationClass(locationClass));
    }

    private void sendPositionToSocket(PositionGPS position) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        // TODO: evtl noch requestid hinzufügen
        stringBuilder.append("{ ");
        stringBuilder.append("\"returnCode\": 0, ");
        stringBuilder.append("\"lat\": ");
        stringBuilder.append(position.latitude);
        stringBuilder.append(", ");
        stringBuilder.append("\"long\": ");
        stringBuilder.append(position.longitude);
        stringBuilder.append("}");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        writer.write(stringBuilder.toString());
        writer.newLine();
        writer.flush();
        writer.close();
    }

    /*
     Zieht die nötigen Daten aus dem Strom und wandelt diese in einen Request um.
     */
    private Request parseRequest() throws IOException, RequestDoesNotExistException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Es wird nur eine Zeile gelesen.
        String line = reader.readLine();

        // Convert to JSON Object for better handling
        JsonElement jsonRootElement = new JsonParser().parse(line);
        if (!jsonRootElement.isJsonObject()) {
            throw new RequestDoesNotExistException();
        }

        JsonObject jsonObject = jsonRootElement.getAsJsonObject();

        int requestCode = jsonObject.get("requestCode").getAsInt();

        // Request is a GetPositionRequest
        if (requestCode == GetPositionRequest.REQUEST_CODE) {
            GetPositionRequest request = new GetPositionRequest(getNewRequestId(), jsonObject.get("deviceId").getAsString());
            // Look for DeviceIds
            JsonArray deviceIdsArray = jsonObject.get("deviceIds").getAsJsonArray();
            for (int i = 0; i < deviceIdsArray.size(); i++) {
                request.addDeviceId(deviceIdsArray.get(i).getAsString());
            }
            return request;
        }

        // Request is a SavePositionRequest
        if (requestCode == SavePositionRequest.REQUEST_CODE) {
            SavePositionRequest request = new SavePositionRequest(
                    getNewRequestId(),
                    jsonObject.get("deviceId").getAsString(),
                    new PositionGPS(
                            jsonObject.get("lat").getAsDouble(),
                            jsonObject.get("long").getAsDouble())
            );
            return request;
        }

        throw new RequestDoesNotExistException();
    }

    public static int getNewRequestId() {
        return requestCounter++;
    }

    public void sendError(int errorCode) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            writer.write("{\"returnCode\": " + errorCode + "}");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSuccess() {
        sendError(0);
    }
}
