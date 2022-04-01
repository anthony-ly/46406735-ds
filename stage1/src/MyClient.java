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

    public static void main(String args[]) {
        // arguments supply hostname of destination
        serverPort = 50000;

        if (!openConnection(args[0], serverPort)) {
            return;
        }

        try {
            // HANDSHAKE
            writeMessage("HELO");

            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage); // OK

            writeMessage("AUTH " + username);

            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage); // OK

            writeMessage("REDY");

            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage); // JOBN

            String jobn = serverMessage; // store the first job

            writeMessage("GETS All");

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
            int serverLargestMax = largestServerNumber(serverList, largestServer);

            writeMessage("OK");

            serverMessage = input.readLine(); // .
            System.out.println("SERVER: " + serverMessage); // .

            int LRRServerIncrement = 0;
            

            while (true) {

                if (serverMessage.contains("JCPL")) {
                    writeMessage("REDY");

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
                Job job = new Job(jobn);
                writeMessage("SCHD " + job.jobID + " " + largestServer.serverType + " " + LRRServerIncrement);
                
                serverMessage = input.readLine();
                System.out.println("SERVER: " + serverMessage); // OK

                LRRServerIncrement += 1;

                writeMessage("REDY");

                serverMessage = input.readLine();
                System.out.println("SERVER: " + serverMessage); // JOBN, JCPL etc.

                jobn = serverMessage;
            }

            writeMessage("QUIT");

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