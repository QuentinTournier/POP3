import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
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
            SSLServerSocket server = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(110);
            server.setEnabledCipherSuites(server.getSupportedCipherSuites());
            while(true){
                SSLSocket connexion = (SSLSocket) server.accept();
                ThreadServer ts = new ThreadServer(connexion, USERFILE);
                new Thread(ts).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
