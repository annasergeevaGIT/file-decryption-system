package com.sergeeva.client;
import com.sergeeva.server.Logger;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class Client {
    private static String LOGGER_DATABASE_PATH = "client_log.sqlite3";
    private final String serverAddress;
    private final int portNumber;
    private final String pathToInputDir;
    private final Logger logger;

    public Client(String serverAddress, int portNumber, String pathToInputDir){
        this.serverAddress = serverAddress;
        this.portNumber = portNumber;
        this.pathToInputDir = pathToInputDir;
        this.logger = new Logger(LOGGER_DATABASE_PATH);
    }
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java -jar client_2.jar serverAddress portNumber pathToInputDir");
            return;
        }

        String serverAddress = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String pathToInputDir = args[2];

        Client client = new Client(serverAddress, portNumber, pathToInputDir);
        client.start();
    }

    private void start() {
        try (Socket socket = new Socket(serverAddress, portNumber);



             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());



             DataInputStream dis = new DataInputStream(socket.getInputStream())) {



            File inputDir = new File(pathToInputDir);

            File[] files = inputDir.listFiles();

            if (files == null || files.length == 0) {
                logger.log("No files found in the input directory.", "failed");
                return;
            }

            dos.writeInt(files.length); // write an int with the number of files that it's going to send, communication protocol

            for (File file : files) {

                byte[] fileContent = null;
                logger.log("Processing file " + file.getName(), "info");

                try {
                    fileContent = Files.readAllBytes(file.toPath());
                } catch (Exception e) {
                    logger.log("Error reading file " + file.getName(), "info" );
                }

                if (fileContent == null) continue;

                try {
                    dos.writeUTF(file.getName()); // send filename encoded in UTF as a string
                    dos.writeInt(fileContent.length); // send the length of the content
                    dos.write(fileContent); // send the content
                    logger.log("Content of the file: " + file.getName() + " sent to server " , "info" );
                } catch (Exception e) {
                    logger.log("Error sending to server " + file.getName(), "info" );
                }
            }

            String response = dis.readUTF();
            logger.log("Server response: " + response, "success");


        } catch (IOException ex) {
            logger.log("Client input/output", ex.getMessage());
        }
    }
}