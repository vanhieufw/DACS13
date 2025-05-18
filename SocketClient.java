package com.movie.network;

import java.io.*;
import java.net.*;
import java.util.*;

public class SocketClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String host;
    private int port;
    private List<MessageListener> listeners = new ArrayList<>();
    private boolean running;
    private boolean connected = false;

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running = true;
            connected = true;
            System.out.println("SocketClient connected to " + host + ":" + port + " at " + new java.util.Date());

            ThreadManager.execute(() -> {
                try {
                    String message;
                    while (running && (message = in.readLine()) != null) {
                        System.out.println("Received message: " + message);
                        for (MessageListener listener : listeners) {
                            listener.onMessageReceived(message);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from socket: " + e.getMessage());
                    e.printStackTrace();
                    connected = false;
                } finally {
                    stop();
                }
            });
        } catch (IOException e) {
            System.err.println("Failed to connect to " + host + ":" + port + " - Error: " + e.getMessage());
            e.printStackTrace();
            connected = false;
        }
    }

    public void sendMessage(String message) {
        if (!connected || out == null) {
            System.err.println("Cannot send message: SocketClient not connected or out is null for " + host + ":" + port);
            return;
        }
        try {
            System.out.println("Sending message: " + message);
            out.println(message);
            out.flush();
        } catch (Exception e) {
            System.err.println("Error sending message to " + host + ":" + port + ": " + e.getMessage());
            e.printStackTrace();
            connected = false;
            stop();
        }
    }

    public void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    public void stop() {
        running = false;
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("SocketClient stopped for " + host + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }
}