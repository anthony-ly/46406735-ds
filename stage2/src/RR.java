import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.DataOutputStream;

public class RR extends Algorithm {

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

            // arraylsit that stores the capable servers
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
                    writeMessage("SCHD " + job.jobID + " " + s.serverType + " " + s.serverID);

                    // receive OK from server
                    setServerMessage(receiveMessage()); // OK

                    // then break loop
                    break;
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
}
