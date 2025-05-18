package com.movie.network;

import java.io.*;
import java.net.*;
import java.util.*;

public class SocketServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private int port;

    public SocketServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Socket server started on port " + port + " at " + new java.util.Date());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                if (clientHandler.isInitialized()) {
                    synchronized (clients) {
                        clients.add(clientHandler);
                        System.out.println("ClientHandler added to clients list: " + clientSocket);
                    }
                    ThreadManager.execute(clientHandler::run);
                } else {
                    System.err.println("Failed to initialize ClientHandler for socket: " + clientSocket);
                    clientHandler.stop();
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        synchronized (clients) {
            Iterator<ClientHandler> iterator = clients.iterator();
            while (iterator.hasNext()) {
                ClientHandler client = iterator.next();
                if (client.isInitialized() && client.isConnected()) {
                    client.sendMessage(message);
                } else {
                    System.err.println("Removing invalid ClientHandler from broadcast list: " + client.socket);
                    iterator.remove();
                    client.stop();
                }
            }
        }
    }

    public void stop() {
        try {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.stop();
                }
                clients.clear();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private boolean initialized = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                if (socket == null || socket.isClosed()) {
                    throw new IOException("Socket is null or closed during initialization");
                }
                System.out.println("Attempting to initialize PrintWriter for client: " + socket);
                out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                initialized = true;
                System.out.println("ClientHandler initialized successfully for socket: " + socket);
            } catch (IOException e) {
                System.err.println("Failed to initialize ClientHandler for socket: " + socket + " - Error: " + e.getMessage());
                e.printStackTrace();
                stop();
            }
        }

        public boolean isInitialized() {
            return initialized && out != null && in != null;
        }

        public boolean isConnected() {
            return socket != null && socket.isConnected() && !socket.isClosed() && !socket.isOutputShutdown();
        }

        public void run() {
            try {
                String message;
                while (isConnected() && (message = in.readLine()) != null) {
                    System.out.println("Received message from " + socket + ": " + message);
                    if (message.startsWith("LOCK_SEATS")) {
                        broadcast(message.replace("LOCK_SEATS", "SEAT_UPDATE"));
                    } else if (message.startsWith("SEAT_UPDATE")) {
                        broadcast(message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error in client handler for socket " + socket + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                stop();
            }
        }

        public void sendMessage(String message) {
            try {
                if (isInitialized() && isConnected()) {
                    System.out.println("Sending to " + socket + ": " + message);
                    out.println(message);
                    out.flush(); // Đảm bảo tin nhắn được gửi ngay lập tức
                } else {
                    System.err.println("Cannot send message: ClientHandler invalid for socket " + socket);
                    stop();
                }
            } catch (Exception e) {
                System.err.println("Error sending message to " + socket + ": " + e.getMessage());
                e.printStackTrace();
                stop();
            }
        }

        public void stop() {
            try {
                initialized = false; // Đánh dấu là không còn khởi tạo
                if (in != null) {
                    in.close();
                    System.out.println("Closed BufferedReader for " + socket);
                }
                if (out != null) {
                    out.close();
                    System.out.println("Closed PrintWriter for " + socket);
                    out = null; // Đặt out về null để tránh sử dụng sau khi đóng
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    System.out.println("Closed socket: " + socket);
                }
                synchronized (clients) {
                    clients.remove(this);
                    System.out.println("Removed ClientHandler from clients list: " + socket);
                }
            } catch (IOException e) {
                System.err.println("Error closing resources for socket " + socket + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}