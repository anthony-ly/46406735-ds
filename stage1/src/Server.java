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
}
