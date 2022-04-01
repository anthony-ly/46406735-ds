public class Job {
    int submitTime;
    int jobID;
    int estRunTime;
    int core;
    int memory;
    int disk;

    public Job(int inSubmitTime, int inJobID, int inEstRunTime, int inCore, int inMemory, int inDisk) {
        submitTime = inSubmitTime;
        jobID = inJobID;
        estRunTime = inEstRunTime;
        core = inCore;
        memory = inMemory;
        disk = inDisk;
    }

    public Job(String job) {
        String[] jobData = job.split(" ");
        submitTime = Integer.parseInt(jobData[1]);
        jobID = Integer.parseInt(jobData[2]);
        estRunTime = Integer.parseInt(jobData[3]);
        core = Integer.parseInt(jobData[4]);
        memory = Integer.parseInt(jobData[5]);
        disk = Integer.parseInt(jobData[6]);
    }
}
