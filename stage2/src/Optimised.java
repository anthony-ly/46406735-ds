import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Optimised extends Algorithm {

    // ArrayList that stores servers
    ArrayList<Server> allServers = new ArrayList<Server>();

    public Optimised(BufferedReader in, DataOutputStream out) {
        super(in, out);
    }

    @Override
    public void run() throws IOException {
        // Authentication
        // HELO
        // - OK
        // AUTH name
        // - Welcome name
        // REDY
        // - JOBN
        // GETS All
        auth(); // last message will be JOBN or NONE 
        String jobn = getServerMessage(); // store the first job
        Job currentJob = new Job(jobn); // TODO: error checking

        // Job scheduling
        
        getsAll(); // get all the server information, last message will be .
                   // now that we have the server information stored inside allServers,
                   // there is no need to call GETS every time,

        while(true) {
            if (getServerMessage().contains("JCPL")) {
                // remove the job that has been completed
                // find the server that the job was completed on
                String[] jcplInfo = getServerMessage().split(" ");
                int jobID = Integer.parseInt(jcplInfo[2]);
                String serverType = jcplInfo[3];
                int serverID = Integer.parseInt(jcplInfo[4]);
                
                // remove the completed job from the corresponding server's job queue
                for(Server s: allServers) {
                    if(s.serverType.equals(serverType) && s.serverID == serverID) {
                        s.removeJob(jobID);
                        // TODO: HOW TO HANDLE THE LAST JCPL????
                        // check if the server that the JCPL specifies has no jobs in its job queue
                        // if no servers, try to migrate a job to it
                        // which job to migrate??
                            // job that has <= resource requirements that the server
                            // job that is not currently running
                        // migrate the current longest waiting job that is the server is capable of running
                        System.out.println(getServerMessage());
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

            currentJob = new Job(jobn);
            scheduleJob(currentJob); // schedule the job, last message will be OK

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

    private void scheduleJob(Job job) throws IOException {
        // look at the job core, memory and disk requirements
        // iterate through all servers to find the first appropriate server that:
            // core, memory, disk >= job requirements
            // no running/waiting jobs
            // maybe? not booting either
        // if none found, send to the first capable server

        for(Server s: allServers) {
            if (s.canRunNow(job) && s.noJobs()) { // GETS Available
                // schedule the job here
                writeMessage("SCHD " + job.jobID + " " + s.serverType + " " + s.serverID);
                s.scheduleJob(job);
                setServerMessage(receiveMessage()); // OK
                return;
            }
        }

        // guaranteed : there is no server that has readily available resources AND is free
        // now we have to send to the first capable server that has the lowest estRunTime
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
     * migrates job to the server and handles the ERR bug
     * 
     * MIGJ jobID, srcServerType, srcServerID, tgtServerType, tgtServerID
     * @throws IOException
     */
    private void migrateJob(Server target) throws IOException {
        // guaranteed: serverMessage will be a JCPL
        // JCPL endTime jobID serverType serverID

        // need to find the longestWaitingJob that can be run on target
        // loop through all the servers
            // call LSTJ on each server
            // if the server has a job that is waiting and can be run on target
                // store the job as "largest waiting job"
        

        // do null checks
        Job longestJob = new Job();
        Server sourceServer = new Server();
        // for each server,
        // find their longest waiting job (if any)
        // compare the longest waiting job's est waiting time with longestWaitingJob's estRunTime
        // if > , reassign it
        // find the longest waiting job
        // target must be able to run it later
        for(Server s: allServers) {
            if (s.queue.size() > 0) {
                Job serverLongest = largestWaitingJob(s);
                if (serverLongest.estRunTime > longestJob.estRunTime && target.canRunLater(serverLongest)) {
                    longestJob = serverLongest;
                    sourceServer = s;
                }
            }
        }

        // check if migration is appropriate
        // ONLY MIGRATE THE JOB IF:
            // the new estruntime on target is < the estruntime on source
        if(sourceServer.getServerEstRun() > target.getServerEstRun()+longestJob.estRunTime) {
            // migrate the job
        }
        

    }

    private Job largestWaitingJob(Server s) throws IOException {
        // jobState: 1 for waiting, 2 for running
        // guaranteed: s has at least 1 job in its queue
        Job longestWaiting = new Job();
        // for(Job j: s.queue) {
        //     if (j.estRunTime > longestWaiting.estRunTime) {
        //         longestWaiting = j;
        //     }
        // }

        // LSTJ
        // DATA
        // OK
        // job info
        // OK
        // .
        writeMessage("LSTJ " + s.serverType + " " + s.serverID);
        setServerMessage(receiveMessage()); // DATA
        int numJobs = s.queue.size(); // get number of jobs on the server
        writeMessage("OK"); // sends job info
        
        if (numJobs > 0) {
            for(int i = 0; i < numJobs; i++) {
                setServerMessage(receiveMessage()); // job info
                // jobID, state, submitTime, startTime, estRunTime, core, memory, disk
                String[] lstj = getServerMessage().split(" ");

                if(lstj[1].equals("1")) { // if the job is currently waiting
                    longestWaiting = s.getJob(Integer.parseInt(lstj[0]));
                }
            }
    
            writeMessage("OK");
        }

        setServerMessage(receiveMessage()); // .

        return longestWaiting;
    }
}