import java.io.*;
import java.net.Socket;

/**
 * Created by p1303674 on 06/03/2017.
 */

public class ThreadServer implements Runnable{

    private Socket connexion;
    private InputStream is;
    private OutputStream os;
    private String userfile;
    private String user;
    private String pass;

    public ThreadServer(Socket connexion, String userfile){
        this.connexion = connexion;
        this.userfile = userfile;
        try {
            this.is = connexion.getInputStream();
            this.os = connexion.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.pass = "";
        this.user = "";
    }

    @Override
    public void run() {
        String message = "+OK Server ready\r\n";
        MailManager mm = null;
        boolean quit = false;
        boolean authorizationOK = false;
        try {
            os.write(message.getBytes());
            // ETAT AUTHORIZATION
            while(!quit && !authorizationOK){
                message = this.read();
                String[] cmd = message.split(" ");
                try {
                    switch (cmd[0].toUpperCase()) {
                        case "USER":
                            if (checkUser(cmd[1])) {
                                message = "+OK\r\n";
                                os.write(message.getBytes());
                                this.user = cmd[1];
                                message = this.read();
                                cmd = message.split(" ");
                                if (cmd[0].toUpperCase().equals("PASS")) {
                                    if (cmd[1].equals(pass)) {
                                        message = "+OK Authentificated\r\n";
                                        os.write(message.getBytes());
                                        authorizationOK = true;
                                        mm = new MailManager(this.user);
                                    } else {
                                        message = "-ERR Wrong password.\r\n";
                                        os.write(message.getBytes());
                                        this.user = "";
                                        this.pass = "";
                                    }
                                } else {
                                    message = "-ERR was waiting for PASS cmd, try again.\r\n";
                                    os.write(message.getBytes());
                                    this.user = "";
                                    this.pass = "";
                                }
                            } else {
                                message = "-ERR user does not exist.\r\n";
                                os.write(message.getBytes());
                            }
                            break;
                        case "APOP":
                            if (checkUser(cmd[1])) {
                                this.user = cmd[1];
                                message = "+OK Authentificated\r\n";
                                os.write(message.getBytes());
                                authorizationOK = true;
                                mm = new MailManager(this.user);
                            } else {
                                message = "-ERR user does not exist.\r\n";
                                os.write(message.getBytes());
                            }
                            break;
                        case "QUIT":
                            quit = true;
                            break;
                        case "QUITNONSAFE":
                            quit = true;
                            break;
                        default:
                            message = "-ERR not a valid command.\r\n";
                            os.write(message.getBytes());
                            break;
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                    message = "-ERR this command need a parameter.\r\n";
                    os.write(message.getBytes());
                }
            }

            while(!quit){
                message = this.read();
                String[] cmd = message.split(" ");
                switch(cmd[0].toUpperCase()){
                    case "STAT" :
                        message = mm.stat();
                        os.write(message.getBytes());
                        break;
                    case "RETR" :
                        try{
                            message = mm.retrieve(Integer.parseInt(cmd[1]));
                        } catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {
                            message = "-ERR RETR must be followed by a number.\r\n";
                        }

                        os.write(message.getBytes());
                        break;
                    case "QUIT" :
                        quit = true;
                        break;
                    case "QUITNONSAFE" :
                        quit = true;
                        break;
                    default:
                        message = "-ERR command does not exist.\r\n";
                        os.write(message.getBytes());
                        break;
                }
            }
            message = "+OK Sign off\r\n";
            os.write(message.getBytes());
            connexion.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String read() throws IOException {
        boolean cr = false;
        boolean lf = false;
        String message = "";

        while(!cr && !lf){
            int data = is.read();
            if(data == -1){
                return "quitnonsafe";
            }
            char c = (char)data;
            message += c;

            if(cr && c == '\n')
                lf = true;
            else
                cr = false;
            if(c == '\r')
                cr = true;
        }
        if(message.equalsIgnoreCase("quitnonsafe")){
            message += ".";
        }
        message = message.replaceAll("[\r,\n]", "");
        return message;
    }

    private boolean checkUser(String username) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(userfile));
            String currentLine;

            while((currentLine = br.readLine()) != null){
                String[] userpass = currentLine.split(" ");
                if(userpass[0].equals(username)){
                    pass = userpass[1];
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
