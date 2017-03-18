import java.io.Serializable;

/**
 * Created by p1303674 on 07/03/2017.
 */
public class Mail implements Serializable{


    private static final long serialVersionUID = 2172519627179020420L;
    private String header;
    private String message;
    private String subject;

    public Mail(String header, String message) {
        this.header = header;
        this.message = message;
    }

    public String getHeader() {
        return header;
    }

    public String getMessage() {
        return message;
    }

    public int getSize(){
        int size = 0;
        size += this.header.length();
        size += this.message.length();
        return size;
    }

    public void setSubject(String subject){
        this.subject = subject;
    }

    public String getSubject(){
        String subject = this.subject == "" ? "No subject": this.subject;
        return subject;
    }

    @Override
    public String toString() {
        return "<mail>" +
                "<header>"+header+"</header>"+
                "<body>"+message+"</body>"+
                "</mail>";
    }
}
