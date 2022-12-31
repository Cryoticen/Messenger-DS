package Assets;

import ServerClient.Value;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.*;
import java.util.Scanner;

public class FileReader {
    File file;

    public FileReader(String path){
        this.file = new File(path);
    }

    public ArrayList<String> getTopicList() throws FileNotFoundException {
        Scanner reader = new Scanner(file);
        ArrayList<String> topicList = new ArrayList<>();

        while (reader.hasNextLine()){
            topicList.add(reader.nextLine());
        }

        return topicList;
    }

    public ArrayList<String> searchTopicSubscribers(String topic) throws FileNotFoundException {
        Scanner reader = new Scanner(file);
        ArrayList<String> topicSubscribers = new ArrayList<>();

        String line;
        while (reader.hasNextLine()){
            line = reader.nextLine();
            if( line.split(",,")[0].equals(topic) ){
                String[] str = line.split(",,");
                topicSubscribers = new ArrayList<String>(Arrays.asList(str));
                break;
            }
        }

        topicSubscribers.remove(0);
        return topicSubscribers;
    }

    public LinkedList<Value> getTopicHistory(String topic) throws FileNotFoundException {
        Scanner reader = new Scanner(file);
        LinkedList<Value> topicHistory = new LinkedList<>();

        String line;
        while (reader.hasNextLine()){
            line = reader.nextLine();
            if( !line.equals("") ){

                Value val = null;
                String[] str = line.split(",,");
                if(str.length == 2) {
                    val = new Value(topic, "textMessage", str[0], str[1]);
                }else if(str[1].equals("Image")){
                    //add image to queue
                }else if(str[1].equals("Video")){
                    //add video to queue
                }
                topicHistory.add(val);

            }
        }

        return topicHistory;
    }

    public boolean Authenticate(String username) throws FileNotFoundException {
        Scanner reader = new Scanner(file);
        String line;
        while (reader.hasNextLine()) {
            line = reader.nextLine();
            if (line.equals(username)) {
                return true;
            }
        }

        return false;
    }

}
