import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javafx.application.Application;

/**
 * Created by Quentin on 16/03/2017.
 */
public class Client extends Application {
    private InputStream is;
    private OutputStream os ;
    private Socket connexion;
    private String userName;
    private String message;


    public  void run() throws IOException {


        String [] empty = new String[0];
        window(empty);
    }
    @Override public void start(Stage stage) throws IOException {
        //Connexion
        String host = "127.0.0.1";
        connexion = new Socket(host,110);
        is = connexion.getInputStream();
        os = connexion.getOutputStream();
        String message = this.read();
        if(!message.equals("+OK Server ready")){
            System.out.println("Could not connect to Server");
            return;
        }
        List<String> choices = new ArrayList<>();
        String choice1 = "USER/PASS";
        String choice2 = "APOP";
        choices.add(choice1);
        choices.add(choice2);



        // Traditional way to get the response value.
        while(!message.startsWith("+OK Authentificated")){
            ChoiceDialog<String> dialog1 = new ChoiceDialog<>(choice1, choices);

            dialog1.setTitle("Choix de type de connexion");
            dialog1.setContentText("Choisissez votre moyen de connexion :");
            Optional<String> result = dialog1.showAndWait();
            String connexionType = result.get();

            if(connexionType.equals("APOP")){
                userName = getUserName();
                message = "APOP" + userName +"\r\n";
                os.write(message.getBytes());
                message = read();
            }
            else{
                userName = getUserName();
                message = "USER " + userName +"\r\n";
                os.write(message.getBytes());
                message = read();
                if(message.startsWith("+OK")){
                    String password = getPassword();
                    message = "PASS " + password +"\r\n";
                    os.write(message.getBytes());
                    message = read();
                }
            }
        }

        //Authentificated
        System.out.println(message);
    }

    private String getUserName(){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Text Input Dialog");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter your userName:");

        // Traditional way to get the response value.
        Optional<String> result2 = dialog.showAndWait();
        if (result2.isPresent()){
            return result2.get();
        }
        return "NoUserFound";
    }

    private String getPassword(){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Text Input Dialog");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter your password:");

        // Traditional way to get the response value.
        Optional<String> result2 = dialog.showAndWait();
        if (result2.isPresent()){
            return result2.get();
        }
        return "NoPassowrdFound";
    }

    private void window(String[] args) {
        Application.launch(args);
    }


    private  String read() throws IOException {
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
}
