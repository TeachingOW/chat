import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// Chat server with multiple clients and broadcasting
public class ChatServer {
    // Thread-safe list of all client output streams
    private static final Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Server running on port 5000...");

        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            while (true) {
                // Accept new client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a thread to handle the new client
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast message to all connected clients
    public static void broadcast(String message, PrintWriter excludeWriter) {
        for (PrintWriter writer : clientWriters) {
            if (writer != excludeWriter) { // don't send back to sender
                writer.println(message);
                writer.flush();
            }
        }
    }

    // Inner class for handling each client connection
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Set up streams
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Add this client's writer to the list
                clientWriters.add(out);

                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("Received: " + msg);
                    // Broadcast to other clients
                    Server.broadcast("Client says: " + msg, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Clean up when client disconnects
                try {
                    if (out != null) clientWriters.remove(out);
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Client disconnected: " + socket);
            }
        }
    }
}
