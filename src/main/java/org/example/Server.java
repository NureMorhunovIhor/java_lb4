package org.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Server {
    private static final int PORT = 12345;
    private static final String USERS_FILE = "users.properties";
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private static Map<String, String> users = new HashMap<>();
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        loadUsers();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOGGER.info("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server error", e);
        }
    }

    private static void loadUsers() {
        try (InputStream input = Server.class.getClassLoader().getResourceAsStream("users.properties")) {
            if (input == null) {
                throw new FileNotFoundException("users.properties not found in resources.");
            }
            Properties prop = new Properties();
            prop.load(input);
            for (String key : prop.stringPropertyNames()) {
                users.put(key, prop.getProperty(key));
            }
        } catch (IOException ex) {
            System.out.println("Error loading users.properties: " + ex.getMessage());
        }
    }

    public static boolean authenticate(String login, String password) {
        return password.equals(users.get(login));
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }


    public static void shutdown() {
        broadcast("Server is shutting down...");
        clients.forEach(ClientHandler::disconnect);
        LOGGER.info("Server shutdown complete.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.exit(0);
    }


    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String login;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SYSTEM:Enter login:");
                    String inputLogin = in.readLine();

                    if (inputLogin == null) {
                        disconnect();
                        return;
                    }

                    if ("shutdown".equalsIgnoreCase(inputLogin)) {
                        if (login == null) {
                            Server.shutdown();
                        }
                        return;
                    }

                    out.println("SYSTEM:Enter password:");
                    String inputPassword = in.readLine();

                    if (inputPassword == null) {
                        disconnect();
                        return;
                    }

                    if (Server.authenticate(inputLogin, inputPassword)) {
                        login = inputLogin;
                        out.println("SYSTEM:Authentication successful!");
                        Server.broadcast(login + " has joined the chat!", this);
                        break;
                    } else {
                        out.println("SYSTEM:Authentication failed. Try again.");
                    }
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(message)) {
                        Server.broadcast(login + " has left the chat.", this);
                        break;
                    }
                    Server.broadcast(login + ": " + message, this); // Передаем отправителя
                }
            } catch (IOException e) {
                Server.LOGGER.log(Level.WARNING, "Connection error", e);
            } finally {
                disconnect();
            }
        }








        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        public void disconnect() {
            try {
                socket.close();
                clients.remove(this);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }
}
