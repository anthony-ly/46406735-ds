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
}
