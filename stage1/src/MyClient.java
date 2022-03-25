import java.net.*;
import java.io.*;
public class MyClient {
    public static void main (String args[]) {
    // arguments supply hostname of destination
        Socket s = null;
        try{
            int serverPort = 50000;
            s = new Socket(args[0], serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream out = new DataOutputStream( s.getOutputStream());
            
            // HANDSHAKE
            out.write(("HELO\n").getBytes()); 
            String data = in.readLine();
            System.out.println("SERVER: "+ data) ;

            // AUTHENTICATION
            String username = System.getProperty("user.name");
            out.write(("AUTH"+username+"\n").getBytes()); 
            out.flush();
            data = in.readLine();
            System.out.println("SERVER: "+ data) ;
    
            out.write(("REDY\n").getBytes());
            out.flush(); 
            data = in.readLine();
            System.out.println("SERVER: "+ data) ;

            // QUIT GRACEFULLY 
            out.write(("QUIT\n").getBytes());
            out.flush(); 
            data = in.readLine();
            System.out.println("SERVER: "+ data) ;
            
        } catch (UnknownHostException e) {
            System.out.println("Sock:"+e.getMessage());
        } catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        } catch (IOException e){
            System.out.println("IO:"+e.getMessage());
        }
        finally {
            if(s!=null) {
                try {
                    s.close();}catch (IOException e){System.out.println("close:"+e.getMessage());
                }
            }
        }
    }
}