File decryption system:

Two applications, based on client-server architecture, communicate via sockets, using the classes  java.net.ServerSocket  and java.net.Socket. A communication protocol is textual, not serialized java objects. 
Initially all files with the encrypted content are loaded from a specified folder. After successful decryption of the data, files should be zipped and stored to the location which is specified on server startup. After completion of whole process, user is informed about the status of the action (success/failure). Decryption covers .txt and .xls formats, but solution is extensibile (e.g.,  supporting additional formats). 
Input of this system is  a  set of files with encrypted content and the output is one zip file which contains all corresponding files with the decrypted content. In order to decrypt input files application use keys that are stored in the database. All actions done in application (decryption, zipping…) are logged  in the  database.

Calling the Application:

Command example for launching the server application with 3 different parameters:

java -jar server_2.jar portNumber pathToDataBase pathToOutputDir

portNumber - Integer value representing the number of the port on which the server listens 
pathToDatabase - String that contains the path to the sqlite3 database file (e.g. ./inputDir/database.sqlite3). The SQLite file will always be provided. 
pathToOutputDir - String that contains path to specific output location where zip file should be stored 
 
After the server application, you can launch the client application:

java -jar client_2.jar serverAddress portNumber pathToInputDir

serverAddress - String that describes the server computer address 
portNumber - Integer value representing the port number on which the application will expect requests 
pathToInputDir - String that contains the path to the input directory (e.g. ./input/path/inputDir). The input directory will always be provided. This directory will contain one or more input files whose format might be excel, text… (.xlsx, .txt).
