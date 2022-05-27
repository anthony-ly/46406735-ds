import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Algorithm {
    private static BufferedReader input;
    private static DataOutputStream output;
    private static String serverMessage;
    private static String username = System.getProperty("user.name");

    public Algorithm(BufferedReader in, DataOutputStream out) {
        input = in;
        output = out;
    }

    /**
     * Abstract method that each scheduling algorithm implements
     * @throws IOException
     */
    public abstract void run() throws IOException;

    // Getter and setter methods for fields
    public static String getUsername() {
        return username;
    }

    public static void setServerMessage(String message) {
        serverMessage = message;
    }

    public static String getServerMessage() {
        return serverMessage;
    }
    
    /**
     * 
     * @param message This is the message that the client-side simulator will
     *                send to ds-server
     * @throws IOException
     * 
     * Writes message to ds-server
     */
    public static void writeMessage(String message) throws IOException {
        String formattedMessage = message + "\n";
        // System.out.println("CLIENT: "+message);
        output.write((formattedMessage).getBytes());
        output.flush();
    }

    /**
     * 
     * @return The input received from ds-server as a String
     * @throws IOException
     * 
     * Receives message sent from ds-server
     */
    public static String receiveMessage() throws IOException {
        String message = "";
        message = input.readLine();
        // System.out.println("SERVER: "+message);
        return message;
    }

    /**
     * Closes the input and output streams for the algorithm
     */
    public static void closeReaders() {
        try {
			input.close();
            output.close();
		} catch (IOException e) {
            System.out.println(e);
		}
    }

    public static void auth() throws IOException {
        writeMessage("HELO");
        setServerMessage(receiveMessage()); // OK

        writeMessage("AUTH " + getUsername());
        setServerMessage(receiveMessage()); // OK

        writeMessage("REDY");
        setServerMessage(receiveMessage()); // JOBN
    }

    public static void quit() throws IOException {
        writeMessage("QUIT");
        setServerMessage(receiveMessage()); // QUIT
    }
}