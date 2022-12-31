package ServerClient;

import java.io.Serializable;

// Acceptable types: textMessage, Image, Video
public class Value implements Serializable {
    String key;
    String type;
    String sender;
    String text;



    public Value(String key, String type, String sender, String text){
        this.sender = sender;
        this.key = key;
        this.type = type;
        this.text = text;
    }

}
