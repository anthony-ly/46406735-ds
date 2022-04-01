import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class MyClient {

    private static Socket s;
    private static BufferedReader input;
    private static DataOutputStream output;
    private static String username = System.getProperty("user.name");
    private static String serverMessage;

    // Writes message to ds-server
    public static void writeMessage(String message) throws IOException {
        String formattedMessage = message + "\n";
        output.write((formattedMessage).getBytes());
        output.flush();
    }

    // Receives message from ds-server
    public static String receiveMessage() throws IOException {
        String message = "";
        message = input.readLine();

        return message;
    }

    // Find the first server of the largest type
    public static Server findLargestServer(ArrayList<Server> serverList) {
        // null checks??
        // check for null, if null, return null, then once you return to main program
        // if the return value is null, exit the simulation
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
        // null check?? maybe not needed because if this gets run then both lserver and slist will != null
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

    public static void main(String args[]) {
        // arguments supply hostname of destination
        try {
            int serverPort = 50000;
            s = new Socket(args[0], serverPort);
            BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream output = new DataOutputStream(s.getOutputStream());

            // HANDSHAKE
            output.write(("HELO\n").getBytes());
            output.flush();
            System.out.println("CLIENT: HELO");
            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage); // OK

            output.write(("AUTH " + username + "\n").getBytes());
            output.flush();
            System.out.println("CLIENT: AUTH "+ username);
            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage); // OK

            output.write(("REDY\n").getBytes());
            output.flush();
            System.out.println("CLIENT: REDY");
            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage); // JOBN

            String jobn = serverMessage; // store the first job

            // Get server information
            output.write(("GETS All\n").getBytes());
            output.flush();
            System.out.println("CLIENT: GETS All");
            serverMessage = input.readLine(); // DATA
            System.out.println("SERVER: " + serverMessage); // DATA

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

            output.write(("OK\n").getBytes());
            output.flush();
            System.out.println("CLIENT: OK");
            // serverMessage = input.readLine();

            ArrayList<Server> serverList = new ArrayList<Server>();

            // find the largest server type
            // loop iterates through all the servers
            // if the current server has more cores than the current value of largestCores
            // update the LargestServer object to now refer to the new highest server
            for (int i = 0; i < serverNums; i++) { 
                // get the next server info
                serverMessage = input.readLine();

                // add the server to the array list
                serverList.add(new Server(serverMessage));
                
                // System.out.println("SERVER: " + serverMessage); // Server info
            }

            // now at this point, we have an arraylist that contains all the available servers
            // lets pass this to a helper function which will determine which server is of the largest type
            Server largestServer = findLargestServer(serverList);

            // now that we know the largest server, we need to find out how many servers of that type exist
            int serverLargestMax = largestServerNumber(serverList, largestServer); // TODO: set serverLargestMax to now equal the last serverID of largest type

            // Respond with OK
            output.write(("OK\n").getBytes());
            output.flush();
            System.out.println("CLIENT: OK");
            serverMessage = input.readLine(); // .
            System.out.println("SERVER: " + serverMessage); // .

            int LRRServerIncrement = 0;
            

            while (true) {

                if (serverMessage.contains("JCPL")) {
                    output.write(("REDY\n").getBytes());
                    output.flush();
                    System.out.println("CLIENT: REDY");
                    serverMessage = input.readLine();
                    jobn = serverMessage;
                    System.out.println("SERVER: " + serverMessage); // JOBN

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
                String[] jobInfo = jobn.split(" "); // TODO change to Job Object

                output.write(("SCHD " + jobInfo[2] + " " + largestServer.serverType + " " + LRRServerIncrement + "\n").getBytes());
                output.flush();
                System.out.println("CLIENT: " + "SCHD " + jobInfo[2] + " " + largestServer.serverType + " " + LRRServerIncrement);
                serverMessage = input.readLine(); // TODO, check if needed
                System.out.println("SERVER: " + serverMessage); // OK

                LRRServerIncrement += 1;

                output.write(("REDY\n").getBytes());
                output.flush();
                System.out.println("CLIENT: REDY");
                serverMessage = input.readLine();
                System.out.println("SERVER: " + serverMessage); // JOBN, JCPL etc.

                jobn = serverMessage;
            }

            output.write(("QUIT\n").getBytes());
            output.flush();
            System.out.println("CLIENT: QUIT");
            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage);

        } catch (Exception e) {
            System.out.println(e);
        }

        finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
            }
        }
    }
}