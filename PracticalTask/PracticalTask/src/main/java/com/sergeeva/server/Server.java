package com.sergeeva.server;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Server {
    private static final int BUFFER_SIZE = 4096;
    private final int portNumber;
    private final String pathToDatabase;
    private final String pathToOutputDir;
    private final Logger logger;

    public Server(int portNumber, String pathToDatabase, String pathToOutputDir){
        this.portNumber = portNumber;
        this.pathToDatabase = pathToDatabase;
        this.pathToOutputDir = pathToOutputDir;
        this.logger = new Logger(pathToDatabase);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java -jar server_2.jar portNumber pathToDataBase pathToOutputDir");
            return;
        }

        int portNumber = Integer.parseInt(args[0]);
        String pathToDatabase = args[1];
        String pathToOutputDir = args[2];

        Server server = new Server(portNumber, pathToDatabase, pathToOutputDir);
        server.start();
    }

    public FileTypeDecoder getDecoderFromExtension(String extension) {
        switch (extension) {
            case "xlsx":
                return new ExcelDecoder();
            case "txt":
                return new TextDecoder();
            default:
                return null;
        }
    }

    private void start() {

        try (ServerSocket serverSocket = new ServerSocket(portNumber);
             Connection conn = DriverManager.getConnection("jdbc:sqlite:" + pathToDatabase)) {

            logger.log("Server is listening on port " + portNumber,"success");

            while (true) {
                try (Socket socket = serverSocket.accept();
                     DataInputStream dis = new DataInputStream(socket.getInputStream());
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

                    int numberOfFiles = dis.readInt();
                    List<File> decryptedFiles = new ArrayList<>();

                    for (int i = 0; i < numberOfFiles; i++) {
                        byte[] fileContentClient = null;
                        String fileName = null;
                        try {
                            fileName = dis.readUTF();

                            int fileSize = dis.readInt();
                            logger.log("Receiving from client " + fileName + " with file size " + fileSize, "info" );

                            fileContentClient = new byte[fileSize];
                            logger.log("Receiving file content from client for file " + fileName, "debug" );
                            dis.readFully(fileContentClient);

                        } catch (Exception e) {
                            logger.log("Error receiving from client " + fileName, "info" );
                        }
                        if (fileContentClient == null || fileName == null) {
                            logger.log("File content or name is null for file " + fileName, "error" );
                            continue;
                        }

                        // find the file extension
                        int lastIndex = fileName.lastIndexOf(".")+1;
                        String fileExt = fileName.substring(lastIndex);


                        FileTypeDecoder decoder = getDecoderFromExtension(fileExt);

                        String key = getKeyFromDatabase(conn, fileName);
                        byte[] decryptedContent = decoder.decodeFile(fileContentClient, key);

                        File decryptedFile = new File(pathToOutputDir, fileName);
                        try  (FileOutputStream fos = new FileOutputStream(decryptedFile)) {
                            fos.write(decryptedContent);
                        } catch (Exception e) {
                            logger.log("File writing error " + fileName, "error" );
                        }
                        decryptedFiles.add(decryptedFile);
                        logger.log("Decryption " + fileName, "success");
                    }

                    String zipFilePath = pathToOutputDir + "/decrypted_files.zip";
                    zipFiles(decryptedFiles, zipFilePath);

                    // Delete individual decrypted files after zipping
                    for (File file : decryptedFiles) {
                        if (!file.delete()) {
                            logger.log("Failed to delete file: " + file.getName(), "error");
                        } else {
                            logger.log("Deleted file: " + file.getName(), "info");
                        }
                    }

                    dos.writeUTF("Decryption and zipping successful. Output stored at: " + zipFilePath);
                    logger.log("Zipping", "Zipping successful");

                } catch (IOException ex) {
                    ex.printStackTrace();
                    logger.log("Input/Output Stream", "failed");
                }
            }

        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
            logger.log("Server is listening on port " + portNumber,ex.getMessage());
        }
    }

    private String getKeyFromDatabase(Connection conn, String fileName) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT KEY FROM AES_KEYS WHERE FILE = ?")) {
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                logger.log("Key received", "success");
                return rs.getString("KEY");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.log("Key received: failed", ex.getMessage());
        }
        return null;
    }

    private void zipFiles(List<File> files, String zipFilePath) {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] bytes = new byte[BUFFER_SIZE];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.log("Zipping", "Zipping failed"+ ex.getMessage());
        }
    }
}
