package org.example;

import java.io.*;
import java.net.*;

public class Shutdown {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("shutdown");
        } catch (IOException e) {
            System.err.println("Shutdown error: " + e.getMessage());
        }
    }
}
