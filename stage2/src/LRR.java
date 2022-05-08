import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.DataOutputStream;

public class LRR extends Algorithm {

    public LRR(BufferedReader in, DataOutputStream out) {
        super(in, out);
    }

    /**
     * Schedules jobs in a largest-round-robin fashion.
     */
    @Override
    public void run() throws IOException {
        // HANDSHAKE
        writeMessage("HELO");
        setServerMessage(receiveMessage()); // OK

        writeMessage("AUTH " + getUsername());
        setServerMessage(receiveMessage()); // OK

        writeMessage("REDY");
        setServerMessage(receiveMessage()); // JOBN

        // JOB SCHEDULING
        String jobn = getServerMessage(); // store the first job

        writeMessage("GETS All");
        setServerMessage(receiveMessage()); // DATA

        // Split the DATA response
        int serverNums = 0;
        if (getServerMessage().contains("DATA")) {
            String[] serverData = getServerMessage().split(" ");

            String nRecs = serverData[1]; // store the number of servers

            if (!nRecs.equals(".")) {
                serverNums = Integer.parseInt(nRecs); // store number of server records as int
            } else {
                serverNums = -1;
            }
        }

        // ERROR HANDLING FOR IF NO RECORDS
        if (serverNums == -1) {
            writeMessage("QUIT");
        }

        writeMessage("OK");

        ArrayList<Server> serverList = new ArrayList<Server>();

        // Loop iterates through all the servers and adds them as Server objects to an
        // ArrayList
        for (int i = 0; i < serverNums; i++) {
            // get the next server info
            setServerMessage(receiveMessage()); // server information

            // add the server to the array list
            serverList.add(new Server(getServerMessage()));
        }

        // Finding the first largest server
        Server largestServer = findLargestServer(serverList);

        // Finding the amount of servers of the first largest type
        int serverLargestMax = findServerCount(serverList, largestServer);

        writeMessage("OK");
        setServerMessage(receiveMessage()); // .

        int LRRServerIncrement = 0;

        while (true) {

            if (getServerMessage().contains("JCPL")) {
                writeMessage("REDY");
                setServerMessage(receiveMessage()); // JOBN

                jobn = getServerMessage();

                continue;
            }

            if (getServerMessage().contains("NONE")) {
                break;
            }

            // Check if the loop has exceeded the maximum number of available servers of
            // largest type
            // If so, reset to 0
            if (LRRServerIncrement > serverLargestMax) {
                LRRServerIncrement = 0;
            }

            // Schedule jobs
            Job job = new Job(jobn);
            writeMessage("SCHD " + job.jobID + " " + largestServer.serverType + " " + LRRServerIncrement);
            setServerMessage(receiveMessage()); // OK

            LRRServerIncrement += 1;

            writeMessage("REDY");
            setServerMessage(receiveMessage()); // JOBN, JCPL etc.

            jobn = getServerMessage();
        }

        writeMessage("QUIT");
        setServerMessage(receiveMessage()); // QUIT
    }

        /**
     * 
     * @param serverList List containing all the servers queried by client's GETS request
     * @return Server object that holds the data of the first server of the first largest type
     * 
     * Finds the first server of the largest type from a given ArrayList
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
     * @param serverList List containing all the servers queried by client's GETS request
     * @param server Server object that is used to count the number of servers of the same type
     * @return
     * 
     * Find the number of servers in an ArrayList that have the same serverType as the parameter server
     */
    public static int findServerCount(ArrayList<Server> serverList, Server server) {
        int counter = 0;
        for (Server s : serverList) {
            if (s.serverType.equals(server.serverType)) {
                counter++;
            }
        }

        // subtract 1 from counter since indexing starts at 0
        counter-=1;
        return counter;
    }

}
