import java.net.*;
import java.io.*;
public class MyClient {

    private static Socket socket;
    private static BufferedReader input;
    private static DataOutputStream output;
    private static String username = System.getProperty("user.name");

    
    // Writes message to ds-server
    public static void writeMessage(String message) throws IOException {
        String formattedMessage = message+"\n";
        output.write((formattedMessage).getBytes());
        output.flush();
    }

    public static void main (String args[]) {
    // arguments supply hostname of destination
        try{
            int serverPort = 50000;
            socket = new Socket(args[0], serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new DataOutputStream( socket.getOutputStream());
            
            // HANDSHAKE
            // output.write(("HELO\n").getBytes()); 
            writeMessage("HELO");
            String data = input.readLine();
            System.out.println("SERVER: "+ data) ; // OK

            // AUTHENTICATION
            writeMessage("AUTH " + username);
            data = input.readLine();
            System.out.println("SERVER: "+ data) ; // OK
    
            // LOOP FOR JOBS
            while (true) {
                writeMessage("REDY");
                data = input.readLine(); // Step 6
                System.out.println("SERVER: "+ data) ; // next event
                                                       // JOBN, JOBP, JCPL,
                                                       // RESF, RESR, NONE
                
                if (!data.equals("NONE")) {
                    while (true) {
                        // Step 7. client send action for event
                        // receive response from server

                        if (data.equals("something")) {
                            while (true) {
                                
                            }
                        }

                        if (data.equals("OK")) {
                            break;
                        }
                    }
                }
                
                if (data.equals("NONE")) { 
                    break;
                }
            }
            

            // QUIT GRACEFULLY 
            writeMessage("QUIT");
            data = input.readLine();
            System.out.println("SERVER: "+ data) ;
            
        } catch (UnknownHostException e) {
            System.out.println("Sock:"+e.getMessage());
        } catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        } catch (IOException e){
            System.out.println("IO:"+e.getMessage());
        }
        finally {
            if(socket!=null) {
                try {
                    socket.close();}catch (IOException e){System.out.println("close:"+e.getMessage());
                }
            }
        }
    }
}