package edu.hm.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Bernd on 23.05.2015.
 */
public class Server extends Thread {

    public static final int PORT = 80;
    ServerSocket serverSocket;

    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                // Nicht zuviel Zeit au�erhalb des accepts blockieren.
                Socket socket = serverSocket.accept();

                // An ServerWorker weitergeben und Platz f�r den n�chsten Client machen.
                new Thread(new ServerWorker(socket)).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
