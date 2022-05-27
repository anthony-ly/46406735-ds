import java.net.*;
import java.io.*;

public class MyClient {

    private static Socket socket;
    private static BufferedReader input;
    private static DataOutputStream output;
    private static int serverPort;
    private static String hostID;
    private static Algorithm schedulingAlgorithm;

    /**
     * 
     * @param hostID IP address 
     * @param serverPort Port number
     * @return true if connection with ds-server has been successful, false otherwise
     * 
     * Creates socket connection with ds-server and opens input and output streams
     */
    public static boolean openConnection(String hostID, int serverPort) {
        try {
            socket = new Socket(hostID, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new DataOutputStream(socket.getOutputStream());

            return true;
            
        }
        catch(Exception e) {
            System.out.println(e);
        }

        return false;
    }

    /**
     * 
     * Closes socket connection with ds-server and closes input and output streams
     */
    public static void closeConnection() {
        try {
            // input.close();
            // output.close();
            Algorithm.closeReaders();
            socket.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * 
     * @param hostID IP address 
     * @param serverPort Port number
     * 
     * Constructor for MyClient
     * Responsible for scheduling jobs in LRR
     */
    public MyClient(String hostID, int serverPort, String schedAlgo) {
        if (!openConnection(hostID, serverPort)) {
            return;
        }

        // TODO remove other algorithms besides op and lrr
        if (schedAlgo.equals("lrr")) {
            schedulingAlgorithm = new LRR(input, output);
        } else if (schedAlgo.equals("s")) {
            schedulingAlgorithm = new RR(input, output);
        } else if (schedAlgo.equals("o")) {
            schedulingAlgorithm = new O(input, output);
        } else if (schedAlgo.equals("mf")) {
            schedulingAlgorithm = new MyFF(input, output);
        } else if (schedAlgo.equals("op")) {
            schedulingAlgorithm = new Optimised(input, output);
        }

        try {
            schedulingAlgorithm.run();

        } catch (Exception e) {
            System.out.println(e);
        }

        closeConnection();
    }

    // args 0 == algo
    public static void main(String args[]) {
        hostID = "127.0.0.1";
        serverPort = 50000;

        MyClient client = new MyClient(hostID, serverPort, args[0]);

    }
}