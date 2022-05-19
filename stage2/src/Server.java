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

    public boolean noJobs() {
        return wJobs == 0 && rJobs == 0;
    }

    public String getState() {
        return state;
    }
}
