import java.util.ArrayList;
import java.util.Iterator;

public class Server {
    String serverType;
    int serverID;
    String state;
    int curStartTime;
    int core;
    int memory;
    int disk;
    int wJobs;
    int rJobs;

    // queue for jobs
    ArrayList<Job> queue = new ArrayList<Job>();

    /**
     * 
     * @param inServerType
     * @param inServerID
     * @param inState
     * @param inCurStartTime
     * @param inCore
     * @param inMemory
     * @param inDisk
     * @param inWJobs
     * @param inRJobs
     * 
     * Constructor for Server that takes in individual arguments and assigns them to
     * the corresponding object fields
     */
    public Server(String inServerType, int inServerID, String inState, 
                  int inCurStartTime, int inCore, int inMemory, 
                  int inDisk, int inWJobs, int inRJobs) {

        serverType = inServerType;
        serverID = inServerID;
        state = inState;
        curStartTime = inCurStartTime;
        core = inCore;
        memory = inMemory;
        disk = inDisk;
        wJobs = inWJobs;
        rJobs = inRJobs;
    }

    /**
     * 
     * @param server Raw server data that is sent by ds-server
     * 
     * Constructor for Server that parses raw string data into object fields
     */
    public Server(String server) {
        String[] serverData = server.split(" ");
        serverType = serverData[0];
        serverID =  Integer.parseInt(serverData[1]);
        state =  serverData[2];
        curStartTime =  Integer.parseInt(serverData[3]);
        core =  Integer.parseInt(serverData[4]);
        memory =  Integer.parseInt(serverData[5]);
        disk =  Integer.parseInt(serverData[6]);
        wJobs =  Integer.parseInt(serverData[7]);
        rJobs =  Integer.parseInt(serverData[8]);
    }

    /**
     * 
     * @param j - job to add to queue
     * adds j to the job queue
     */
    public void scheduleJob(Job j) {
        queue.add(j);
    }

    /**
     * 
     * @param jobID - ID of the job object to remove from queue
     * removes the job that has the corresponding jobID from the queue (if it exists)
     */
    public void removeJob(int jobID) {
        // remove the job
        Iterator<Job> iter = queue.iterator();
        while (iter.hasNext()) {
            Job j = iter.next();

            if (j.jobID == jobID) {
                iter.remove();
            }
        }
    }

    /**
     * 
     * @return true if the server has no jobs running or waiting, false otherwise
     */
    public boolean noJobs() {
        return wJobs == 0 && rJobs == 0;
    }

    /**
     * 
     * @param j - job to be scheduled
     * @return true if the server has enough resources to run the job NOW, false otherwise.
     */
    public boolean canRunNow(Job j) {
        int availableCore = core;
        int availableMemory = memory;
        int availableDisk = disk;
        // loop through the job queue
        for(Job job : queue) {
            availableCore -= job.core;
            availableMemory -= job.memory;
            availableDisk -= job.disk;
        }
        // return true if all availRequirements >= j.requirements
        // false otherwise
        return availableCore >= j.core && availableMemory >= j.memory && availableDisk >= j.disk;
    }

    /**
     * 
     * @param j - job to be scheduled
     * @return true if the server has enough resources to eventually run the job j, false otherwise
     */
    public boolean canRunLater(Job j) {
        return core >= j.core && memory >= j.memory && disk >= j.disk;
    }

    public int getServerEstRun() {
        int result = 0;
        for(Job j : queue) {
            result += j.estRunTime;
        }
        return result;
    }

    /**
     * 
     * @return the current state of the server
     */
    public String getState() {
        return state;
    }

    /**
     * returns String that is formatted the same way ds-server formats server information
     */
    public String toString() {
        return serverType + " " + serverID + " " + state + " " + curStartTime + " " + core + " " + memory + " " + disk + " " + wJobs + " " + rJobs;
    }
}