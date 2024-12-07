package org.example;

import java.io.*;
import java.net.*;

public class Chat {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            System.out.println("Connected to server chat view");
            String message;
            while ((message = in.readLine()) != null) {
                if (!message.startsWith("SYSTEM:")) {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Chat error: " + e.getMessage());
        }
    }
}
