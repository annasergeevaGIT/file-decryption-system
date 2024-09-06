# File decryption system.

Two applications utilizing a client-server model communicate through sockets, employing the classes java.net.ServerSocket and java.net.Socket. The communication protocol is text-based rather than serialized Java objects. Initially, all files with encrypted content are loaded from a designated folder. Upon successful decryption, the files are compressed into a zip file and stored in a specified location determined at server startup. After the entire process is complete, the user is notified of the action's status (success or failure). Decryption supports .txt and .xls formats, with the solution being extendable to support additional formats. The system's input consists of files with encrypted content, and the output is a zip file containing all corresponding decrypted files. The application uses keys stored in a database to decrypt input files, and all actions performed by the application (such as decryption and zipping) are logged in the database.

## Calling the Application:

### Command example for launching the server application with 3 different parameters:

java -jar server_2.jar portNumber pathToDataBase pathToOutputDir

portNumber - Integer value representing the number of the port on which the server listens.

pathToDatabase - String that contains the path to the sqlite3 database file (e.g. ./inputDir/database.sqlite3).

pathToOutputDir - String that contains path to specific output location where zip file should be stored.

example: java -jar server_2.jar 8080 \database.sqlite3 \output

### While server application is running, you can launch the client application:

java -jar client_2.jar serverAddress portNumber pathToInputDir

serverAddress - String that describes the server computer address.

portNumber - Integer value representing the port number on which the application will expect requests.

pathToInputDir - String that contains the path to the input directory (e.g. ./input/path/inputDir). The input directory will always be provided. This directory will contain one or more input files whose format might be excel, text… (.xlsx, .txt).

example: java -jar client_2.jar 127.0.0.1 8080 \input
