import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TTO extends Algorithm {

    // ArrayList that stores servers
    ArrayList<Server> allServers = new ArrayList<Server>();

    public TTO(BufferedReader in, DataOutputStream out) {
        super(in, out);
    }

    @Override
    public void run() throws IOException {
        // Authentication
        auth();
        String jobn = getServerMessage(); // store the first job
        Job currentJob = new Job(jobn);

        // Job scheduling
        getsAll(); // get all the server information and store it inside allServers

        while(true) {
            if (getServerMessage().contains("JCPL")) {
                // Job Migration

                // Get the completed job's information
                String[] jcplInfo = getServerMessage().split(" ");
                int jobID = Integer.parseInt(jcplInfo[2]);
                String serverType = jcplInfo[3];
                int serverID = Integer.parseInt(jcplInfo[4]);
                
                for(Server s: allServers) {
                    if(s.serverType.equals(serverType) && s.serverID == serverID) {
                        // Remove the completed job from the corresponding server's job queue
                        s.removeJob(jobID);

                        // Attempt to migrate a job to the the server corresponds to serverType and serverID
                        migrateJob(s);
                        break;
                    }
                }

                
                writeMessage("REDY");
                setServerMessage(receiveMessage()); // JOBN
                jobn = getServerMessage();
                continue;
            }

            if (getServerMessage().contains("NONE")) {
                break;
            }

            currentJob = new Job(jobn); // create a new job object based on ds-server JOBN
            scheduleJob(currentJob); // schedule the job

            writeMessage("REDY");
            setServerMessage(receiveMessage()); // JOBN, JCPL etc.

            jobn = getServerMessage();
        }

        quit();
    }

    /**
     * Calls GETS All and stores the result in allServers
     * @throws IOException
     */
    private void getsAll() throws IOException {
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

        // Loop iterates through all the servers and adds them as Server objects to an
        // ArrayList
        for (int i = 0; i < serverNums; i++) {
            // get the next server info
            setServerMessage(receiveMessage()); // server information

            // add the server to the array list
            allServers.add(new Server(getServerMessage()));
        }

        writeMessage("OK");
        setServerMessage(receiveMessage()); // .
    }

    /**
     * 
     * @param job job to be scheduled by TTO algorithm
     * @throws IOException
     */
    private void scheduleJob(Job job) throws IOException {
        // Find the first server that has sufficient resources to run the job NOW
        for(Server s: allServers) {
            if (s.canRunNow(job)) {
                // schedule the job here
                writeMessage("SCHD " + job.jobID + " " + s.serverType + " " + s.serverID);
                s.scheduleJob(job);
                setServerMessage(receiveMessage()); // OK
                return;
            }
        }

        // Schedule job to the first capable server that has the lowest estRunTime
        Server lowest = allServers.get(0);
        for(Server s: allServers) {
            if (s.canRunLater(job) && s.getServerEstRun() < lowest.getServerEstRun()) { // GETS Available
                lowest = s;
            }
        }

        writeMessage("SCHD " + job.jobID + " " + lowest.serverType + " " + lowest.serverID);
        lowest.scheduleJob(job);
        setServerMessage(receiveMessage()); // OK
    }

    /**
     * migrates job to the server
     * @throws IOException
     */
    private void migrateJob(Server target) throws IOException {
        Job longestJob = new Job(); // The current longest waiting job scheduled
        Server sourceServer = null; // Server that longestJob is scheduled to

        // Find their longest waiting job (if any)
        for(Server s: allServers) {
            if (!s.queue.isEmpty()) { // Only check for servers that have jobs waiting/running
                Job serverLongest = largestWaitingJob(s); // longest waiting job for current server s

                // Check if:
                // The serverLongest has a greater estRunTime than the current estRunTime for longestJob AND
                // s has enough resources available to run serverLongest
                // Reassign longestJob to serverLongest and sourceServer to s if true
                if (serverLongest.estRunTime > longestJob.estRunTime && target.canRunLater(serverLongest)) {
                    longestJob = serverLongest;
                    sourceServer = s;
                }
            }
        }

        // check if migration is appropriate
        if(sourceServer != null) {
            // migrate the job
            String migrate = "MIGJ " + longestJob.jobID + " " + sourceServer.serverType + " " + sourceServer.serverID 
            + " " + target.serverType + " " + target.serverID;

            writeMessage(migrate);
            setServerMessage(receiveMessage()); // OK

            sourceServer.removeJob(longestJob.jobID); // remove job from old server
            target.queue.add(longestJob); // add job to new server
        }
        

    }

    /**
     * 
     * @param s server that has at least 1 job in its queue
     * @return the job in s's queue that has the longest estRunTime
     * @throws IOException
     */
    private Job largestWaitingJob(Server s) throws IOException {
        Job longestWaiting = new Job();

        // Query the server to determine the job times
        writeMessage("LSTJ " + s.serverType + " " + s.serverID);
        setServerMessage(receiveMessage()); // DATA
        
        int numJobs = getNumDataFields(); // get number of jobs on the server
        
        writeMessage("OK"); // sends job info
        
        // receive job information from ds-server
        if (numJobs > 0) {
            for(int i = 0; i < numJobs; i++) {
                setServerMessage(receiveMessage()); // job info

                // jobID, state, submitTime, startTime, estRunTime, core, memory, disk
                String[] lstj = getServerMessage().split(" ");

                Job current = s.getJob(Integer.parseInt(lstj[0]));
                if(lstj[1].equals("1") && current != null && current.estRunTime > longestWaiting.estRunTime) { // if the job is currently waiting
                    longestWaiting = s.getJob(Integer.parseInt(lstj[0]));
                }
            }
    
            writeMessage("OK");
        }

        setServerMessage(receiveMessage()); // .
        
        return longestWaiting;
    }

    /**
     * 
     * @return the number of fields from DATA response
     */
    private int getNumDataFields() {
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
}