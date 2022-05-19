import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.DataOutputStream;

public class MyAlgorithm extends Algorithm {

    public MyAlgorithm(BufferedReader in, DataOutputStream out) {
        super(in, out);
    }

    /**
     * Schedules jobs in a first fit fashion.
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

        String jobn = getServerMessage(); // store the first job
        Job job = new Job(jobn);

        ArrayList<Server> serverList = new ArrayList<Server>();

        while (true) {
            // JOB SCHEDULING
            if (getServerMessage().contains("JCPL")) {
                writeMessage("REDY");
                setServerMessage(receiveMessage()); // JOBN

                jobn = getServerMessage();

                continue;
            }

            if (getServerMessage().contains("NONE")) {
                break;
            }

            job = new Job(jobn);

            writeMessage("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
            setServerMessage(receiveMessage()); // DATA

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

            writeMessage("OK");

            // setServerMessage(receiveMessage());
            // Server first = new Server(getServerMessage()); // stores the first capable server in case there are no active/booting servers found
            // // server will send all the data
            // for (int i = 1; i < serverNums; i++) {
            //     setServerMessage(receiveMessage());
            // }
            
            serverList.clear();

            // Loop iterates through all the servers and adds them as Server objects to an
            // ArrayList
            for (int i = 0; i < serverNums; i++) {
                // get the next server info
                setServerMessage(receiveMessage()); // server information

                // add the server to the array list
                serverList.add(new Server(getServerMessage()));
            }

            writeMessage("OK");

            setServerMessage(receiveMessage()); // .

            Server first = null; // stores the first capable server in case there are no active/booting servers found
            // determine which server to send the job to
            // 1. must not have any running jobs and waiting jobs at the same time
            for(int i = 0; i < serverList.size(); i++) {
                if(serverList.get(i).noJobs()) {
                    first = serverList.get(i);
                    break;
                } 
            }

            if (first == null) {
                for(int i = 0; i < serverList.size(); i++) {
                    if(serverList.get(i).getState().equals("active") || serverList.get(i).getState().equals("booting")) {
                        first = serverList.get(i);
                        break;
                    } 
                }
            }

            // Schedule jobs
            // job = new Job(jobn);
            writeMessage("SCHD " + job.jobID + " " + first.serverType + " " + first.serverID);
            setServerMessage(receiveMessage()); // OK

            writeMessage("REDY");
            setServerMessage(receiveMessage()); // JOBN, JCPL etc.

            jobn = getServerMessage();
        }

        writeMessage("QUIT");
        setServerMessage(receiveMessage()); // QUIT
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

}
