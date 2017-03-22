import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javafx.scene.control.TextInputDialog;

import javafx.stage.Stage;

import javafx.application.Application;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Quentin on 16/03/2017.
 */
public class Client extends Application {
    private InputStream is;
    private OutputStream os ;
    private SSLSocket connexion;
    private String userName;
    private ArrayList<Mail> userMails ;
    private String timestamp;

    public Client(){
        userMails = new ArrayList<Mail>();
    }

    public  void run() throws IOException {
        String [] empty = new String[0];
        window(empty);
    }
    @Override public void start(Stage stage) throws IOException{
        //Connexion
        String host = "127.0.0.1";
        SSLSocketFactory fabrique= (SSLSocketFactory) SSLSocketFactory.getDefault();
        connexion= (SSLSocket) fabrique. createSocket (host, 110);
        connexion.setEnabledCipherSuites(connexion.getSupportedCipherSuites());
        is = (InputStream) connexion.getInputStream();
        os = (OutputStream) connexion.getOutputStream();
        String message = this.read();
        if(!message.startsWith("+OK Server ready")){
            System.out.println("Could not connect to Server");
            connexion.close();
            return;
        }else{
            String[] arr = message.split(" ");
            timestamp = arr[arr.length-1];
            arr = null;
        }
        System.out.println(timestamp);
        List<String> choices = new ArrayList<>();
        String choice1 = "USER/PASS";
        String choice2 = "APOP";
        choices.add(choice1);
        choices.add(choice2);
        String errorMessage = "";

        while(!message.startsWith("+OK Authentificated")){
            ChoiceDialog<String> dialog1 = new ChoiceDialog<>(choice1, choices);

            dialog1.setTitle("Choix de type de connexion");
            dialog1.setContentText(errorMessage + "\n Choisissez votre moyen de connexion :");
            Optional<String> result = dialog1.showAndWait();
            String connexionType = "none";
            if(result.isPresent()){
                 connexionType= result.get();
            }
            else{
                quit();
                return;
            }

            if(connexionType.equals("APOP")){
                userName = getUserName();
                if(userName == null )
                    continue;
                String password = getPassword();
                String apopMsg = timestamp + password;
                if(password == null)
                    continue;
                try {
                    apopMsg = String.format("%032x",
                                            new BigInteger(1, MessageDigest.getInstance("MD5").digest(apopMsg.getBytes()))
                                            );
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                System.out.println(apopMsg);
                message = "APOP " + userName + " " + apopMsg +"\r\n";
                os.write(message.getBytes());
                message = read();
                if(message.startsWith("-ERR")){
                    errorMessage = "Wrong username/password.";
                }
            }
            else if (connexionType.equals("USER/PASS")){
                userName = getUserName();
                if(userName == null )
                    continue;
                message = "USER " + userName + "\r\n";
                os.write(message.getBytes());
                message = read();
                if (message.startsWith("+OK")) {
                    String password = getPassword();
                    if(password == null)
                        continue;
                    message = "PASS " + password + "\r\n";
                    os.write(message.getBytes());
                    message = read();
                    if (message.startsWith("-ERR")) {
                        errorMessage = "Wrong password";
                    }
                } else {
                    errorMessage = "Unknown user";
                }
            }
        }

        //Authentificated
        message = "STAT\r\n";
        os.write(message.getBytes());
        message = read();
        if(message.startsWith("+OK")){
            int nbMessage = Integer.parseInt(message.split(" ")[1]);
            retrieve(nbMessage);
        }

        Boolean quit = false;
        while (!quit){
            int mailNumber = choseMails();
            if (mailNumber ==-1) {
                quit = true;
                quit();
            }
            else
                displayMail(mailNumber);
        }

    }

    private void displayMail(int mailNumber) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Mail mail = userMails.get(mailNumber);
        alert.setTitle(mail.getSubject());
        alert.setHeaderText(mail.getHeader());
        alert.setContentText(mail.getMessage());

        alert.showAndWait();
    }

    private int choseMails() {
        List<String> choices = new ArrayList<>();
        HashMap<String, Integer> mailNumber = new HashMap<>();
        String choice;
        String firstMail = "Mail "+1+" : "+userMails.get(0).getSubject();
        for (int i = 0; i<userMails.size();i++ ) {
            choice = "Mail "+(i+1)+" : " +userMails.get(i).getSubject();
            choices.add(choice);
            mailNumber.put(choice, i);
        }
        ChoiceDialog<String> dialog1 = new ChoiceDialog<>(firstMail, choices);

        dialog1.setTitle("Choisissez votre mail");
        dialog1.setContentText("Choisissez le mail à lire :");
        Optional<String> result = dialog1.showAndWait();
        if(result.isPresent()){
            String mailChosen = result.get();
            return mailNumber.get(mailChosen);
        }
        return -1;
    }

    private void retrieve(int nbMessage) throws IOException {
        String message;
        for(int i = 1; i<=nbMessage; i++){
            message = "RETR " + i +"\r\n";
            os.write(message.getBytes());
            readMail();
        }
    }


    private String getUserName(){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("User");
        dialog.setHeaderText("Veuillez entrer votre nom d'utilisateur ");
        dialog.setContentText("Nom d'utilisateur :");

        // Traditional way to get the response value.
        Optional<String> result2 = dialog.showAndWait();
        if (result2.isPresent()){
            return result2.get();
        }
        return null;
    }

    private String getPassword(){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Password");
        dialog.setHeaderText("Veuillez entrer votre mot de passe");
        dialog.setContentText("mot de passe :");

        // Traditional way to get the response value.
        Optional<String> result2 = dialog.showAndWait();
        if (result2.isPresent()){
            return result2.get();
        }
        return null;
    }

    private void window(String[] args) {
        Application.launch(args);
    }

    private void readMail() throws IOException {
        String line = read();
        if (line.startsWith("+OK")){
            line ="";
        }
        Boolean endOfMail = false;
        Boolean body = false;
        String headerText ="";
        String bodyText = "";
        String subject ="";
        while (!endOfMail){
            line = read();
            if(line.equals(".")){
                endOfMail = true;
            }
            if(line.equals("")){
                body = true;
            }
            if(body){
                bodyText += line + "\r\n";
            }
            else{
                headerText += line + "\r\n";
            }
            if(line.toLowerCase().startsWith("subject:")){
                subject = line.substring(line.lastIndexOf(':') + 1);
            }
        }
        headerText = cleanText(headerText);
        bodyText = cleanText(bodyText);
        Mail mail = new Mail(headerText, bodyText);
        mail.setSubject(subject);
        userMails.add(mail);
    }

    private String cleanText(String text) {
        return text.replaceAll("\\<.*?>","");
    }

    private  String read() throws IOException {
        boolean cr = false;
        boolean lf = false;
        String message = "";

        while(!cr || !lf){
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

    private void quit() throws IOException {
        String message = "QUIT\r\n";
        os.write(message.getBytes());
        read();
        connexion.close();
    }
}
