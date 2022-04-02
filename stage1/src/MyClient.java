import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class MyClient {

    private static Socket s;
    private static BufferedReader input;
    private static DataOutputStream output;
    private static String username = System.getProperty("user.name");
    private static String serverMessage;
    private static int serverPort;

    // Writes message to ds-server
    public static void writeMessage(String message) throws IOException {
        String formattedMessage = message + "\n";
        output.write((formattedMessage).getBytes());
        output.flush();
        System.out.print("CLIENT: " + formattedMessage);
    }

    // Receives message from ds-server
    public static String receiveMessage(int printPrefix) throws IOException {
        String message = "";
        message = input.readLine();
        
        // Value of printResponse determines if console output is prefixed with "SERVER"
        // 0 -> print with prefix
        // 1 -> print without prefix
        if (printPrefix == 0) {
            System.out.println("SERVER: " + message);
        } else if (printPrefix == 1) {
            System.out.println(message);
        }

        return message;
    }

    // Find the first server of the largest type
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

    // Find the number of servers of that type
    public static int largestServerNumber(ArrayList<Server> serverList, Server largestServer) {
        int counter = 0;
        for (Server s : serverList) {
            if (s.serverType.equals(largestServer.serverType)) {
                counter++;
            }
        }

        // subtract 1 from counter since indexing starts at 0
        counter-=1;
        return counter;
    }

    // Creates socket connection with ds-server and opens input and output streams
    public static boolean openConnection(String hostID, int serverPort) {
        try {
            s = new Socket(hostID, serverPort);
            input = new BufferedReader(new InputStreamReader(s.getInputStream()));
            output = new DataOutputStream(s.getOutputStream());

            return true;
            
        }
        catch(Exception e) {
            System.out.println(e);
        }

        return false;
    }

    // Closes socket connection with ds-server and closes input and output streams
    public static void closeConnection() {
        try {
            input.close();
            output.close();
            s.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    // Constructor
    public MyClient(String hostID, int serverPort) {
        if (!openConnection(hostID, serverPort)) {
            return;
        }

        try {
            // HANDSHAKE
            writeMessage("HELO");

            // serverMessage = input.readLine();
            // System.out.println("SERVER: " + serverMessage); // OK
            serverMessage = receiveMessage(0);

            writeMessage("AUTH " + username);

            // serverMessage = input.readLine();
            // System.out.println("SERVER: " + serverMessage); // OK
            serverMessage = receiveMessage(0);

            writeMessage("REDY");

            // serverMessage = input.readLine();
            // System.out.println("SERVER: " + serverMessage); // JOBN
            serverMessage = receiveMessage(0);

            String jobn = serverMessage; // store the first job

            writeMessage("GETS All");

            // serverMessage = input.readLine(); // DATA
            // System.out.println("SERVER: " + serverMessage); // DATA
            serverMessage = receiveMessage(0);

            // Split the DATA response
            int serverNums = 0;
            if (serverMessage.contains("DATA")) {
                String[] serverData = serverMessage.split(" ");

                String nRecs = serverData[1]; // store the number of servers

                if (!nRecs.equals(".")) {
                    serverNums = Integer.parseInt(nRecs); // store number of servers as int
                } else {
                    serverNums = -1;
                }
            }

            // TODO: ERROR HANDLING FOR IF NO RECORDS
            writeMessage("OK");
            // serverMessage = input.readLine();

            ArrayList<Server> serverList = new ArrayList<Server>();

            // find the largest server type
            // loop iterates through all the servers
            // if the current server has more cores than the current value of largestCores
            // update the LargestServer object to now refer to the new highest server
            for (int i = 0; i < serverNums; i++) {
                // get the next server info
                // serverMessage = input.readLine();
                serverMessage = receiveMessage(1);

                // add the server to the array list
                serverList.add(new Server(serverMessage));

                // System.out.println("SERVER: " + serverMessage); // Server info
            }

            // now at this point, we have an arraylist that contains all the available
            // servers
            // lets pass this to a helper function which will determine which server is of
            // the largest type
            Server largestServer = findLargestServer(serverList);

            // now that we know the largest server, we need to find out how many servers of
            // that type exist
            int serverLargestMax = largestServerNumber(serverList, largestServer);

            writeMessage("OK");

            // serverMessage = input.readLine(); // .
            // System.out.println("SERVER: " + serverMessage); // .
            serverMessage = receiveMessage(0);

            int LRRServerIncrement = 0;

            while (true) {

                if (serverMessage.contains("JCPL")) {
                    writeMessage("REDY");

                    // serverMessage = input.readLine();
                    serverMessage = receiveMessage(0);
                    jobn = serverMessage;
                    // System.out.println("SERVER: " + serverMessage); // JOBN

                    continue;
                }

                if (serverMessage.contains("NONE")) {
                    break;
                }

                // Check if the loop has exceeded the maximum number of available servers
                // of largest type
                // If so, reset to 0
                if (LRRServerIncrement > serverLargestMax) {
                    LRRServerIncrement = 0;
                }

                // Schedule jobs
                Job job = new Job(jobn);
                writeMessage("SCHD " + job.jobID + " " + largestServer.serverType + " " + LRRServerIncrement);

                // serverMessage = input.readLine();
                // System.out.println("SERVER: " + serverMessage); // OK
                serverMessage = receiveMessage(0);

                LRRServerIncrement += 1;

                writeMessage("REDY");

                // serverMessage = input.readLine();
                // System.out.println("SERVER: " + serverMessage); // JOBN, JCPL etc.
                serverMessage = receiveMessage(0);

                jobn = serverMessage;
            }

            writeMessage("QUIT");

            // serverMessage = input.readLine();
            // System.out.println("SERVER: " + serverMessage);
            serverMessage = receiveMessage(0);

        } catch (Exception e) {
            System.out.println(e);
        }

        closeConnection();
    }

    public static void main(String args[]) {
        // arguments supply hostname of destination
        serverPort = 50000;

        MyClient client = new MyClient(args[0], serverPort);

    }
}