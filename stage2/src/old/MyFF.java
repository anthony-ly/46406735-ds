import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.DataOutputStream;

public class MyFF extends Algorithm {

    // HashMap<String, int[]> serverNumbers;

    public MyFF(BufferedReader in, DataOutputStream out) {
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

            // arraylist that stores the capable servers
            ArrayList<Server> capableServers = new ArrayList<Server>();
            writeMessage("GETS Capable " + job.getRequirements());
            setServerMessage(receiveMessage()); // DATA

            int serverNums = -1; // TODO error handling
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
            // check the first server, if the first server is busy or unavailable, THEN
            // iterate through everything else
            boolean allBusy = true;
            for (Server s : capableServers) {

                if (s.noJobs()) { // if no waiting or running jobs
                    allBusy = false;
                    writeMessage("SCHD " + job.jobID + " " + s.serverType + " " + s.serverID);
                    System.out.println("SERVER WITH NO JOBS FOUND: " + s);
                    // receive OK from server
                    setServerMessage(receiveMessage()); // OK
                    break; // then break loop
                }
            }

            // if all servers are busy
            if (allBusy) {
                Server lowestRunning = capableServers.get(0);
                int lowestEstRuntime = getServerEstRuntime(lowestRunning);
                System.out.println("----------------------");
                for (Server s : capableServers) {
                    // System.out.println(s);
                    // send LSTJ
                    int jobTime = getServerEstRuntime(s);
                    if (jobTime < lowestEstRuntime) {
                        lowestEstRuntime = jobTime;
                        lowestRunning = s;
                    }
                }

                // schedule to the lowest running
                System.out.println("LOWEST RUNNING SERVER: "+ lowestRunning);
                System.out.println("----------------------");
                writeMessage("SCHD " + job.jobID + " " + lowestRunning.serverType + " " + lowestRunning.serverID);
                setServerMessage(receiveMessage()); // OK
                // System.out.println("expected OK | got: " + getServerMessage());
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
        // System.out.println("expected DATA | got: " + getServerMessage());
        int numJobs = getNumDataFields();
        // System.out.println("numJobs: "+numJo
        writeMessage("OK"); // sends job info

        int jobTime = 0;

        if (numJobs > 0) {
            for (int i = 0; i < numJobs; i++) {
                setServerMessage(receiveMessage());
                String[] jobInfo = getServerMessage().split(" ");
                jobTime += Integer.parseInt(jobInfo[4]);
                System.out.println(s + " EST RUNTIME: " + jobTime);
            }

            writeMessage("OK");
        }

        setServerMessage(receiveMessage()); // .
        // System.out.println("expected . | got: " + getServerMessage());

        return jobTime;
    }
}
