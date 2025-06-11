import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class Server {


    // private static PrintWriter out;
    // private static BufferedReader in;

    private static final int PORT = 12345;
    private static final long INACTIVITY_LIMIT_MS = 20 * 60 * 1000; // 20 minutes

    private static ConcurrentHashMap<String, ClientHandler> clientMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<String>> offlineMessages = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Server started... on PORT = 12345 ");

        // Start the watchdog to check client activity
        //new Thread(Server::checkInactiveClients).start(); // ⬅️ NEW

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ⬅️ NEW: Watchdog thread to check for inactive clients
    private static void checkInactiveClients() {
        while (true) {
            try {
                Thread.sleep(5 * 60 * 1000); // Check every 5 minute
                long now = System.currentTimeMillis();
                for (ClientHandler handler : clientMap.values()) {
                    if (now - handler.getLastActiveTime() > INACTIVITY_LIMIT_MS) {
                        System.out.println("Disconnecting inactive user: " + handler.getUsername());
                        //handler.sendMessage(">> Disconnected due to inactivity.");
                        handler.disconnect();
                    }
                }
            } catch (InterruptedException ignored) {}
        }
    }

    static class ClientHandler extends Thread {
        private PrintWriter out;
        private BufferedReader in;
        private Socket socket;
        private String username;
        private volatile long lastActive; // ⬅️ NEW
        private String currentRecipient;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.lastActive = System.currentTimeMillis(); // ⬅️ NEW
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        public long getLastActiveTime() { // ⬅️ NEW
            return lastActive;
        }

        public String getUsername() {
            return username;
        }

        public void disconnect() { 
            try {
                socket.close();
            } catch (IOException ignored) {}
            clientMap.remove(username);
            //broadcast(">> " + username + " was disconnected due to inactivity.");
            System.out.println(">> " + username + " was disconnected due to inactivity.");
            interrupt();
        }

        public void sendFile(File file) throws IOException {
            dataOut.writeUTF("@file");
            dataOut.writeUTF(file.getName());
            dataOut.writeLong(file.length());

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    dataOut.write(buffer, 0, read);
                }
            }
            dataOut.flush();
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());

                username = in.readLine();
                if (username == null || username.isEmpty()) return;

                if (clientMap.putIfAbsent(username, this) != null) {
                    System.out.println("Username already taken.");
                    out.println("Username already taken.");
                    socket.close();
                    return;
                }

                clientMap.put(username, this);
                //broadcast(">> " + username + " has joined the chat");
                System.out.println(">> " + username + " has joined the chat");

                // List<String> pending = offlineMessages.get(username);
                // if (pending != null && !pending.isEmpty()) {
                //     //out.println("[You have " + pending.size() + " offline messages]");
                //     for (String msg : pending) {
                //         out.println(msg);
                //     }
                //     offlineMessages.remove(username);
                // }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/list")) {
                        System.out.println("Online users: " + String.join(", ", clientMap.keySet()));
                        //out.println("Online users: " + String.join(", ", clientMap.keySet()));
                    } else if (message.startsWith("/to ")) {
                        currentRecipient = message.substring(4).trim();
                        System.out.println("--------------Switched recipient to " + currentRecipient);
                        //out.println("Recipient set to: " + currentRecipient);
                    } else if (message.startsWith("/status ")) {
                        String targetUser = message.substring(8).trim();
                        boolean isOnline = clientMap.containsKey(targetUser);
                        out.println("status:" + targetUser + ":" + (isOnline ? "online" : "offline"));
                    } 
                    



                    // else if (message.startsWith("@File")) {
                    //     if (currentRecipient == null || !clientMap.containsKey(currentRecipient)) {
                    //         out.println("Select a valid recipient with /to <username> before sending a file.");
                    //         continue;
                    //     }

                    //     String fileName = dataIn.readUTF();     // Read file name
                    //     long fileSize = dataIn.readLong();      // Read file size

                    //     FileOutputStream fos = new FileOutputStream("received_" + fileName);

                    //     byte[] buffer = new byte[4096];
                    //     int bytesRead;
                    //     long totalRead = 0;

                    //     byte[] fileBytes = new byte[(int) fileSize];
                    //     dataIn.readFully(fileBytes);
                        
                    //     // while (totalRead < fileSize && (bytesRead = dataIn.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
                    //     //     fos.write(buffer, 0, bytesRead);
                    //     //     totalRead += bytesRead;
                    //     // }
                        

                    //     System.out.println("File received: " + fileName);

                    //     out.println("Sending file to " + currentRecipient);

                    //     // Send the file to the recipient
                    //     dataOut.writeUTF("@file");             // Protocol tag
                    //     dataOut.writeUTF(currentRecipient);        // Who sent the file
                    //     dataOut.writeUTF(fileName);                // File name
                    //     dataOut.writeLong(fileBytes.length);       // File size
                    //     dataOut.write(fileBytes);                  // File content

                    //     dataOut.flush();

                    //     System.out.println("File sent to " + username);


                    //     // String filePath = message.substring(6).trim();
                    //     // File file = new File(filePath);

                    //     // if (!file.exists() || !file.isFile()) {
                    //     //     out.println("File not found: " + filePath);
                    //     //     continue;
                    //     // }

                    //     // ClientHandler recipient = clientMap.get(currentRecipient);
                    //     // out.println("Sending file: " + file.getName() + " to " + currentRecipient);
                    //     // recipient.sendMessage("Incoming file from " + username + ": " + file.getName());

                    //     // try {
                    //     //     recipient.sendFile(file);
                    //     //     out.println("File sent successfully.");
                    //     // } catch (IOException e) {
                    //     //     out.println("Failed to send file.");
                    //     //     e.printStackTrace();
                    //     // }
                    
                    // }












                    // else if (message.startsWith("@File")) {
                    //     try {
                    //         String fileName = dataIn.readUTF();
                    //         long fileSize = dataIn.readLong();
                            
                    //         if (currentRecipient == null || !clientMap.containsKey(currentRecipient)) {
                    //             out.println("Select a valid recipient with /to <username> before sending a file.");
                    //             continue;
                    //         }

                    //         // Read file content
                    //         byte[] fileBytes = new byte[(int) fileSize];
                    //         dataIn.readFully(fileBytes);

                    //         System.out.println("File received: " + fileName);

                    //         // Forward to recipient
                    //         ClientHandler recipient = clientMap.get(currentRecipient);
                    //         if (recipient != null) {
                    //             recipient.dataOut.writeUTF("@file");
                    //             recipient.dataOut.writeUTF(username); // sender
                    //             recipient.dataOut.writeUTF(fileName);
                    //             recipient.dataOut.writeLong(fileBytes.length);
                    //             recipient.dataOut.write(fileBytes);
                    //             recipient.dataOut.flush();
                                
                    //             out.println("File sent to " + currentRecipient);
                    //         } else {
                    //             out.println("Recipient is offline");
                    //         }
                    //     } catch (IOException e) {
                    //         System.out.println("Error handling file transfer: " + e.getMessage());
                    //     }
                    // }














                    // In the ClientHandler class, modify the file handling section:

                    else if (message.startsWith("@File")) {
                        try {
                            String fileName = dataIn.readUTF();
                            long fileSize = dataIn.readLong();
                            
                            if (currentRecipient == null || !clientMap.containsKey(currentRecipient)) {
                                out.println("Select a valid recipient with /to <username> before sending a file.");
                                continue;
                            }

                            // Create buffer for file content
                            byte[] fileBytes = new byte[(int) fileSize];
                            dataIn.readFully(fileBytes);

                            System.out.println("File received from " + username + ": " + fileName);

                            // Forward to recipient
                            ClientHandler recipient = clientMap.get(currentRecipient);
                            if (recipient != null) {
                                try {
                                    recipient.dataOut.writeUTF("@File");  // Consistent with client
                                    recipient.dataOut.writeUTF(username); // sender
                                    recipient.dataOut.writeUTF(fileName);
                                    recipient.dataOut.writeLong(fileBytes.length);
                                    recipient.dataOut.write(fileBytes);
                                    recipient.dataOut.flush();
                                    
                                    out.println("File sent to " + currentRecipient);
                                } catch (IOException e) {
                                    out.println("Failed to send file to recipient");
                                    System.err.println("File transfer error: " + e.getMessage());
                                }
                            } else {
                                out.println("Recipient is offline");
                            }
                        } catch (IOException e) {
                            System.out.println("Error handling file transfer: " + e.getMessage());
                            out.println("File transfer failed: " + e.getMessage());
                        }
                    }














                    
                    else {
                    if (currentRecipient != null) {
                            ClientHandler recipientHandler = clientMap.get(currentRecipient);
                    if (recipientHandler != null) {
                            String formatMessage = "From " + username + ": "+ message;
                            recipientHandler.sendMessage(formatMessage);
                            System.out.println(formatMessage);
                    } else {
                            // String formatMessage = "From " + username + ": " + message;
                            // offlineMessages.computeIfAbsent(currentRecipient, k -> new ArrayList<>()).add(formatMessage);
                            // System.out.println("User '" + currentRecipient + "' is offline. Message stored.");
                            
                            //out.println("User '" + currentRecipient + "' is offline. Message stored.");
                    }
                    } else {
                            System.out.println("No recipient selected. Use /to <username>");
                            //out.println("No recipient selected. Use /to <username>");
            }
        }

                }
            } catch (IOException e) {
                System.out.println("Connection with " + username + " lost.");
            } finally {
                clientMap.remove(username);
                //broadcast(">> " + username + " has left the chat");
                System.out.println(">> " + username + " has left the chat");
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void broadcast(String message) {
            for (ClientHandler client : clientMap.values()) {
                client.sendMessage(message);
            }
        }

    }
        
}
