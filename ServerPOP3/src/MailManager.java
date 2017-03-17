import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by p1303674 on 07/03/2017.
 */
public class MailManager{
    private String mailbox;
    private ArrayList<Mail> mails;

    public MailManager(String mailbox) throws FileNotFoundException {
        this.mailbox = mailbox;
        this.initialize();
    }

    public String stat(){
        int size = 0;
        for(int i = 0; i < mails.size(); i++){
            size += mails.get(i).getSize();
        }
        return "+OK "+mails.size()+" "+size+"\r\n";
    }

    public String list(){
        return "+OK feature not implemented yet.";
    }

    public String retrieve(int n){
        n--;
        String message;
        if(n >= 0 && n < mails.size()){
            Mail mail = mails.get(n);
            message = "+OK "+mail.getSize()+" octets\r\n";
            message += mail.getHeader() + mail.getMessage();
            message += ".\r\n";

        }else{
            message = "-ERR message does not exist.\r\n";
        }

        return message;
    }

    public void initialize() throws FileNotFoundException {
        mails = new ArrayList<>();
        FileInputStream fis = new FileInputStream(new File("c:/POP3/"+mailbox+".txt"));
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        int state = 0;
        boolean headerFound = false;
        boolean messageFound = false;
        String header = "";
        String message = "";
        try {
            while ((line = br.readLine()) != null) {
                if(line.startsWith(".")){
                    line = line.replace(".", "..");
                }
                switch (state){
                    case 0 : // ETAT EN DEHORS MAIL
                        if(line.startsWith("<mail>")){
                            state = 1;
                        }
                        break;
                    case 1 : // ETAT DANS HEADER
                        header += line;
                        if(header.endsWith("</header>")){
                            header = header.replaceAll("(<header>\r\n|<header>|<"+'/'+"header>)", "");
                            state =  2;
                        }
                        header += "\r\n";
                        break;
                    case 2 :
                        message += line;
                        if(message.endsWith("</body>")){
                            message = message.replaceAll("(<body>\r\n|<body>|<"+'/'+"body>)", "");
                            state =  3;
                        }else {
                            message += "\r\n";
                        }
                        break;
                    case 3 :
                        if(line.startsWith("</mail>")){
                            state = 0;
                            mails.add(new Mail(header, message));
                            header = "";
                            message = "";
                        }
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
