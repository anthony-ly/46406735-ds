import java.net.*;
import java.io.*;

public class MyClient {

    private static Socket s;
    private static BufferedReader input;
    private static DataOutputStream output;
    private static String username = System.getProperty("user.name");
    private static String serverMessage;

    // Writes message to ds-server
    public static void writeMessage(String message) throws IOException {
        String formattedMessage = message + "\n";
        output.write((formattedMessage).getBytes());
        output.flush();
    }

    // Receives message from ds-server
    public static String receiveMessage() throws IOException {
        String message = "";
        message = input.readLine();

        return message;
    }

    public static void main(String args[]) {
        // arguments supply hostname of destination
        try {
            int serverPort = 50000;
            s = new Socket(args[0], serverPort);
            BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream output = new DataOutputStream(s.getOutputStream());

            // HANDSHAKE
            output.write(("HELO\n").getBytes());
            output.flush();
            System.out.println("CLIENT: HELO");
            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage); // OK

            output.write(("AUTH " + username + "\n").getBytes());
            output.flush();
            System.out.println("CLIENT: AUTH "+ username);
            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage); // OK

            output.write(("REDY\n").getBytes());
            output.flush();
            System.out.println("CLIENT: REDY");
            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage); // JOBN

            String jobn = serverMessage; // store the first job

            // Get server information
            output.write(("GETS All\n").getBytes());
            output.flush();
            System.out.println("CLIENT: GETS All");
            serverMessage = input.readLine(); // DATA
            System.out.println("SERVER: " + serverMessage); // DATA

            // Split the DATA response
            int serverNums = 0;
            if (serverMessage.contains("DATA")) {
                String[] serverData = serverMessage.split(" ");

                String nRecs = serverData[1]; // store the number of servers

                if (!nRecs.equals(".")) {
                    serverNums = Integer.parseInt(nRecs); // store number of servers as int
                } else {
                    serverNums = -1;
                }
            }

            // TODO: ERROR HANDLING FOR IF NO RECORDS

            output.write(("OK\n").getBytes());
            output.flush();
            System.out.println("CLIENT: OK");
            // serverMessage = input.readLine();

            // String serverList = serverMessage; // stores a list of all the servers

            // find the largest server type
            String[] serverLargest;
            for (int i = 0; i < serverNums; i++) {
                serverMessage = input.readLine();
                // System.out.println("SERVER: " + serverMessage); // Server info
            }
            serverLargest = serverMessage.split(" ");

            // Respond with OK
            output.write(("OK\n").getBytes());
            output.flush();
            System.out.println("CLIENT: OK");
            serverMessage = input.readLine(); // .
            System.out.println("SERVER: " + serverMessage); // .

            int LRRServerIncrement = 0;
            int serverLargestMax = Integer.parseInt(serverLargest[1]);

            while (true) {
                // Check if the loop has exceeded the maximum number of available servers
                // of largest type
                // If so, reset to 0
                if (serverMessage.contains("JCPL")) {
                    output.write(("REDY\n").getBytes());
                    output.flush();
                    System.out.println("CLIENT: REDY");
                    serverMessage = input.readLine();
                    jobn = serverMessage;
                    System.out.println("SERVER: " + serverMessage); // JOBN

                    continue;
                }

                if (serverMessage.contains("NONE")) {
                    break;
                }

                if (LRRServerIncrement > serverLargestMax) {
                    LRRServerIncrement = 0;
                }

                // Schedule jobs
                String[] jobInfo = jobn.split(" ");

                output.write(("SCHD " + jobInfo[2] + " " + serverLargest[0] + " " + LRRServerIncrement + "\n").getBytes());
                output.flush();
                System.out.println("CLIENT: " + "SCHD " + jobInfo[2] + " " + serverLargest[0] + " " + LRRServerIncrement);
                serverMessage = input.readLine(); // TODO, check if needed
                System.out.println("SERVER: " + serverMessage); // OK

                LRRServerIncrement += 1;

                output.write(("REDY\n").getBytes());
                output.flush();
                System.out.println("CLIENT: REDY");
                serverMessage = input.readLine();
                System.out.println("SERVER: " + serverMessage); // JOBN, JCPL etc.

                jobn = serverMessage;
            }

            output.write(("QUIT\n").getBytes());
            output.flush();
            System.out.println("CLIENT: QUIT");
            serverMessage = input.readLine();
            System.out.println("SERVER: " + serverMessage);

        } catch (Exception e) {
            System.out.println(e);
        }

        finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
            }
        }
    }
}