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

    public abstract void run() throws IOException;

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
        
        return message;
    }

    public static void closeReaders() {
        try {
			input.close();
            output.close();
		} catch (IOException e) {
            System.out.println(e);
		}
    }
}