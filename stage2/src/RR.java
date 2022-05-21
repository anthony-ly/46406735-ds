import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.DataOutputStream;

public class RR extends Algorithm {

    HashMap<String, int[]> serverNumbers;

    public RR(BufferedReader in, DataOutputStream out) {
        super(in, out);
    }

    @Override
    public void run() throws IOException {
        // Authentication
        // HANDSHAKE
        writeMessage("HELO");
        setServerMessage(receiveMessage()); // OK

        writeMessage("AUTH " + getUsername());
        setServerMessage(receiveMessage()); // OK

        writeMessage("REDY");
        setServerMessage(receiveMessage()); // JOBN

        // Job scheduling
        String jobn = getServerMessage(); // store the first job
        // TODO: error handling in case jobn is not a job
        Job job = new Job(jobn);
        // serverNumbers = getServerNumbers();

        // looping stuff
        while(true) {
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

            // arraylist that stores the capable servers
            ArrayList<Server> capableServers = new ArrayList<Server>();
            writeMessage("GETS Capable "+ job.getRequirements());
            setServerMessage(receiveMessage()); // DATA

            int serverNums = -1; // TODO error handling
            if(getServerMessage().contains("DATA")) {
                String[] serverData = getServerMessage().split(" ");

                String nRecs = serverData[1]; // store the number of servers

                if (!nRecs.equals(".")) {
                    serverNums = Integer.parseInt(nRecs); // store number of server records as int
                } else {
                    serverNums = -1;
                }
            }

            writeMessage("OK");

            // SERVER Sends all the server info
            for (int i = 0; i < serverNums; i++) {
                // get the next server info
                setServerMessage(receiveMessage()); // server information

                // add the server to the array list
                capableServers.add(new Server(getServerMessage()));
            }

            writeMessage("OK");

            setServerMessage(receiveMessage()); // .

            // make scheduling decision here
            // 1. go through the send it to the first server that has no jobs
            // 2. if none exist, send it to the server that:
                // a. has the lowest time to complete its jobs
                // send LSTJ to server (LSTJ serverType serverID)
                // SERVER: DATA numberOfJobs lengthOfMessage
                // CLIENT: OK
                // loop numberOfJobs times
                    // SERVER: jobID jobState submitTime startTime estRunTime core memory disk
                // CLIENT: OK
            boolean allBusy = true;
            for(Server s: capableServers) {
                if(s.noJobs()) { // if no waiting or running jobs
                    allBusy = false;
                    // schedule the thingy
                    // TODO, instead of s.serverID change it to the serverType increment for RR scheduling
                    // writeMessage("SCHD " + job.jobID + " " + s.serverType + " " + findNextServerIncrement(s.serverType));
                    writeMessage("SCHD " + job.jobID + " " + s.serverType + " " + s.serverID);

                    // set the value[0] ++
                    // do error checking too

                    // receive OK from server
                    setServerMessage(receiveMessage()); // OK
                    break; // then break loop
                }
            }
            
            // if all servers are busy
            if (allBusy) {
                Server lowestRunning = capableServers.get(0);
                int lowestEstRuntime = getServerEstRuntime(lowestRunning);
                for(Server s: capableServers) {
                    // send LSTJ
                    int jobTime = getServerEstRuntime(s);
                    if (jobTime < lowestEstRuntime) {
                        lowestEstRuntime = jobTime;
                        lowestRunning = s;
                    }
                }

                // schedule to the lowest running
                writeMessage("SCHD " + job.jobID + " " + lowestRunning.serverType + " " + lowestRunning.serverID);
                setServerMessage(receiveMessage()); // OK
            }

            // guaranteed: job has been scheduled
            writeMessage("REDY");
            setServerMessage(receiveMessage()); // JOBN, JCPL etc.

            jobn = getServerMessage();
        }

        writeMessage("QUIT");
        setServerMessage(receiveMessage()); // QUIT
    }

    public int getNumDataFields() {
        int dataNums = 0;
        String[] serverData = getServerMessage().split(" ");

        String nRecs = serverData[1]; // store the number of servers

        if (!nRecs.equals(".")) {
            dataNums = Integer.parseInt(nRecs); // store number of server records as int
        } else {
            dataNums = -1;
        }

        return dataNums;
    }
    

    public int getServerEstRuntime(Server s) throws IOException {
        writeMessage("LSTJ " + s.serverType + " " + s.serverID);
        // System.out.println("getServerEstRun: before the DATA receive message");
        setServerMessage(receiveMessage()); // DATA
        int numJobs = getNumDataFields();
        // System.out.println("numJobs: "+numJo
        writeMessage("OK"); // sends job info

        int jobTime = 0;
        for(int i = 0; i < numJobs; i++) {
            setServerMessage(receiveMessage());
            // System.out.println("inside loop: " + getServerMessage());
            String[] jobInfo = getServerMessage().split(" ");
            jobTime += Integer.parseInt(jobInfo[4]);
        }

        writeMessage("OK");
        setServerMessage(receiveMessage()); // .

        return jobTime;
    }

    public HashMap<String, int[]> getServerNumbers() throws IOException {
        HashMap<String, int[]> serverNumbers = new HashMap<String, int[]>();

        writeMessage("GETS All");
        setServerMessage(receiveMessage()); // DATA ___

        int numRecs = getNumDataFields();
        writeMessage("OK");

        // store servertypes in a set
        Set<String> serverTypes = new HashSet<String>();

        // store server info in an arraylist
        ArrayList<Server> serverList = new ArrayList<Server>();

        // Loop iterates through all the servers and adds them as Server objects to an
        // ArrayList
        for (int i = 0; i < numRecs; i++) {
            // get the next server info
            setServerMessage(receiveMessage()); // server information

            // add the serverType to the set
            String type = getServerMessage().split(" ")[0];
            serverTypes.add(type);

            // add the server to the array list
            serverList.add(new Server(getServerMessage()));
        }

        // initialise the hashmap to hold the stuff

        for(String s: serverTypes) {
            int[] serverNumber = new int[] {0, findServerCount(serverList, s)};
            serverNumbers.put(s, serverNumber);
        }

        for(String s: serverNumbers.keySet()) {
            System.out.println(s + " " + serverNumbers.get(s)[0] + " " + serverNumbers.get(s)[1]);
        }
        

        writeMessage("OK");
        setServerMessage(receiveMessage()); // .
        return serverNumbers;
    }

        /**
     * 
     * @param serverList List containing all the servers queried by client's GETS request
     * @param server Server object that is used to count the number of servers of the same type
     * @return
     * 
     * Find the number of servers in an ArrayList that have the same serverType as the parameter server
     */
    public static int findServerCount(ArrayList<Server> serverList, String server) {
        int counter = 0;
        for (Server s : serverList) {
            if (s.serverType.equals(server)) {
                counter++;
            }
        }
        // subtract 1 from counter since indexing starts at 0
        counter-=1;
        return counter;
    }

    public int findNextServerIncrement(String s) {
        int nextIncrement = serverNumbers.get(s)[0];
        int maxIncrement = serverNumbers.get(s)[1];

        if (nextIncrement > maxIncrement) {
            nextIncrement = 0;
            serverNumbers.put(s, new int[]{nextIncrement, maxIncrement});
        } else {
            serverNumbers.put(s, new int[]{nextIncrement+1, maxIncrement});
        }

        return nextIncrement;
    }
}
