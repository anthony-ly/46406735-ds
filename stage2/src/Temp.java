import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Temp {

    private static Socket socket;
    private static BufferedReader input;
    private static DataOutputStream output;
    private static String username = System.getProperty("user.name");
    private static String serverMessage;
    private static int serverPort;
    private static String hostID;

    /**
     * 
     * @param message This is the message that the client-side simulator will
     *                send to ds-server
     * @throws IOException
     * 
     *                     Writes message to ds-server
     */
    public static void writeMessage(String message) throws IOException {
        String formattedMessage = message + "\n";
        output.write((formattedMessage).getBytes());
        output.flush();
    }

    /**
     * 
     * @return The input received from ds-server as a String
     * @throws IOException
     * 
     *                     Receives message sent from ds-server
     */
    public static String receiveMessage() throws IOException {
        String message = "";
        message = input.readLine();

        return message;
    }

    /**
     * 
     * @param serverList List containing all the servers queried by client's GETS
     *                   request
     * @return Server object that holds the data of the first server of the first
     *         largest type
     * 
     *         Finds the first server of the largest type from a given ArrayList
     */
    public static Server findLargestServer(ArrayList<Server> serverList) {
        Server largestServer = serverList.get(0);
        int largestCore = largestServer.core;

        for (Server s : serverList) {
            if (s.core > largestCore) {
                largestCore = s.core;
                largestServer = s;
            }
        }

        return largestServer;
    }

    /**
     * 
     * @param serverList List containing all the servers queried by client's GETS
     *                   request
     * @param server     Server object that is used to count the number of servers
     *                   of the same type
     * @return
     * 
     *         Find the number of servers in an ArrayList that have the same
     *         serverType as the parameter server
     */
    public static int findServerCount(ArrayList<Server> serverList, Server server) {
        int counter = 0;
        for (Server s : serverList) {
            if (s.serverType.equals(server.serverType)) {
                counter++;
            }
        }

        // subtract 1 from counter since indexing starts at 0
        counter -= 1;
        return counter;
    }

    /**
     * 
     * @param hostID     IP address
     * @param serverPort Port number
     * @return true if connection with ds-server has been successful, false
     *         otherwise
     * 
     *         Creates socket connection with ds-server and opens input and output
     *         streams
     */
    public static boolean openConnection(String hostID, int serverPort) {
        try {
            socket = new Socket(hostID, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new DataOutputStream(socket.getOutputStream());

            return true;

        } catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }

    /**
     * 
     * Closes socket connection with ds-server and closes input and output streams
     */
    public static void closeConnection() {
        try {
            input.close();
            output.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * 
     * @param hostID     IP address
     * @param serverPort Port number
     * 
     *                   Constructor for MyClient
     *                   Responsible for scheduling jobs in LRR
     */
    public Temp(String hostID, int serverPort) {
        if (!openConnection(hostID, serverPort)) {
            return;
        }

        try {
            // HANDSHAKE
            writeMessage("HELO");
            serverMessage = receiveMessage(); // OK

            writeMessage("AUTH " + username);
            serverMessage = receiveMessage(); // OK

            writeMessage("REDY");
            serverMessage = receiveMessage(); // JOBN

            String jobn = serverMessage; // store the first job
            Job job = new Job(jobn);

            while (true) {
                // JOB SCHEDULING
                if (serverMessage.contains("JCPL")) {
                    writeMessage("REDY");
                    serverMessage = receiveMessage(); // JOBN

                    jobn = serverMessage;

                    continue;
                }

                if (serverMessage.contains("NONE")) {
                    break;
                }
                writeMessage("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
                serverMessage = receiveMessage(); // DATA
    

                int serverNums = 0;
                if (serverMessage.contains("DATA")) {
                    String[] serverData = serverMessage.split(" ");
    
                    String nRecs = serverData[1]; // store the number of servers
    
                    if (!nRecs.equals(".")) {
                        serverNums = Integer.parseInt(nRecs); // store number of server records as int
                    } else {
                        serverNums = -1;
                    }
                }

                writeMessage("OK");

                serverMessage = receiveMessage();
                Server first = new Server(serverMessage);
                // server will send all the data
                for (int i = 1; i < serverNums; i++) {
                    serverMessage = receiveMessage();
                }
                writeMessage("OK");

                serverMessage = receiveMessage(); // .
    
                // Schedule jobs
                job = new Job(jobn);
                writeMessage("SCHD " + job.jobID + " " + first.serverType + " " + 0);
                serverMessage = receiveMessage(); // OK

                writeMessage("REDY");
                serverMessage = receiveMessage(); // JOBN, JCPL etc.

                jobn = serverMessage;
            }

            writeMessage("QUIT");
            serverMessage = receiveMessage(); // QUIT

        } catch (Exception e) {
            System.out.println(e);
        }

        closeConnection();
    }

    public static void main(String args[]) {
        hostID = "127.0.0.1";
        serverPort = 50000;

        Temp client = new Temp(hostID, serverPort);

    }
}