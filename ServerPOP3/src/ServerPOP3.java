import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;

/**
 * Created by p1303674 on 06/03/2017.
 */
public class ServerPOP3{

    public static final String USERFILE = "c:/POP3/users.txt";

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(110);
            while(true){
                Socket connexion = server.accept();
                ThreadServer ts = new ThreadServer(connexion, USERFILE);
                new Thread(ts).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
