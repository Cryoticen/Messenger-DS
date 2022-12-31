package ServerClient;

import Assets.StringToListHashMap;

import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class User implements ConsumerInterface, PublisherInterface {

    Socket connection;
    ObjectInputStream in;
    ObjectOutputStream out;
    ProfileName profile = new ProfileName();
    public HashMap<Integer,Integer> brokerPortMap = new HashMap<>();
    HashMap<String, Integer> TopicsBroker = new HashMap<>();

    String projectDir = Paths.get("").toAbsolutePath().toString();
    final String USER_PATH  = projectDir + "\\src\\DataBase\\Users";


    public static void main(String[] args) throws IOException {


        User user = new User();
        user.profile.setUserName();

        //Get topic list with connect()
        user.connect("127.0.0.1", 4321,true);

        user.userLoop();
    }

    public User() {
        super();
    }

    public void userLoop() throws IOException {
        int action = 0;
        while(true) {
            System.out.println("Enter number for action:   \n1. See topic list    \n2. Enter topic chat    \n3. See your Profile   \n4. See another users profile    \n5. Exit");
            Scanner input = new Scanner(System.in);
            try{
                action = input.nextInt();
            }catch (InputMismatchException e){
                System.err.println("Not a number, try again");
                continue;
            }


            if (action == 1) {
                System.out.println("-Topics: \n");
                for(String s: TopicsBroker.keySet()){
                    System.out.println(" " + s);
                }
            } else if (action == 2) {
                System.out.println("Enter topic: ");
                String topic = input.nextLine();
                topic = input.nextLine();

                if(!TopicsBroker.containsKey(topic)){
                    System.err.println("Topic doesn't exist");
                    continue;
                }

                enterChat(topic, "127.0.0.1");

            } else if (action == 3) {

                seeUserProfile(profile.getUserName());

            }else if (action == 4) {

                System.out.println("Enter topic: ");
                String username = input.nextLine();
                seeUserProfile(username);

            }
            else if(action == 5){
                out.writeUTF("Logout");
                break;
            }
            else {
                System.err.println("Not an option, try again");
            }
        }
    }

    public void seeUserProfile(String username) throws IOException {
        Scanner input = new Scanner(System.in);
        listener();
        out.writeUTF("seeProfile");
        out.flush();
        out.writeUTF(username);
        out.flush();
    }

    public void enterChat(String topic, String ip) throws IOException {
        System.out.println("____________________ "+ topic.toUpperCase() + " ____________________");
        int port = brokerPortMap.get(TopicsBroker.get(topic));
        connect(ip, port,false);
        Scanner input = new Scanner(System.in);
        Thread listen = new Thread(new Runnable() {@Override public void run() { listener(); } });
        listen.start();

        out.writeUTF("enterChat");
        out.flush();
        out.writeUTF(topic);
        out.flush();

        while (true){
            String text = input.nextLine();
            if (text.equals("exit")) {
                try {
                    out.writeObject(new Value(" ", " ", " ", "exit"));
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            else if(text.endsWith(".png")){
                File image = new File(USER_PATH + "\\" + profile.getUserName() + "\\" + text);
                System.out.println(USER_PATH + "\\" + profile.getUserName() + "\\" + text);
                if(!image.exists()) {
                    System.err.println("Image not found.");
                    continue;
                }
                out.writeObject(new Value(" ", "Image", " ", " "));
                sendFile(image);
            }
            else if(text.endsWith(".mp4")){
                File video = new File(USER_PATH + "\\" + profile.getUserName() + "\\" + text);
                System.out.println(USER_PATH + "\\" + profile.getUserName() + "\\" + text);
                if(!video.exists()) {
                    System.err.println("Image not found.");
                    continue;
                }
                out.writeObject(new Value(" ", "Video", " ", " "));
                sendFile(video);
            }
            else{
                Value value = new Value(topic, "textMessage", this.profile.getUserName(),text);
                push(topic, value);
            }
        }

    }

    private void sendFile(File file) {
        File[] imageFile = new File[1];
        imageFile[0] = file;

        try {

            FileInputStream imageFileInputStream = new FileInputStream(imageFile[0].getAbsoluteFile());

            String imageFileName = imageFile[0].getName();
            byte[] imageFileNameBytes = imageFileName.getBytes();

            byte[] imageFileContentBytes = new byte[(int) imageFile[0].length()];
            imageFileInputStream.read(imageFileContentBytes);

            out.writeInt(imageFileNameBytes.length);
            out.flush();
            out.write(imageFileNameBytes);
            out.flush();
            //out.write(imageFileContentBytes.length);
            //out.flush();

            byte[] chunk;
            for (int i = 1; i <= 10; i++) {

                int first = ((i - 1) * imageFileContentBytes.length) / 10;
                System.out.println(first);
                int last = ((i) * imageFileContentBytes.length) / 10;
                System.out.println(last);
                chunk = new byte[(last - first)];
                int counter = 0;
                for (int j = first; j < last; j++) {
                    chunk[counter] = imageFileContentBytes[j];
                    counter++;
                }
                out.writeInt(chunk.length);
                out.flush();
                out.write(chunk);
                out.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void push(String topic, Value value) {
        try {
            out.writeObject(value);
            out.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void listener(){
        Value message;

        while(true){
            try {
                message = (Value)in.readObject();

                if(message.text.equals("exit")){break;}
                else if(message.type.equals("textMessage")) System.out.println(message.sender + ": " + message.text);
                else if(message.type.equals("Image")){

                }


            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public void connect(String ip, int port, boolean onlyGetInfo) {
        try {
            connection = new Socket(ip, port);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

            if (onlyGetInfo){
                out.writeUTF("login");
                out.flush();

                while (true) {
                    out.writeUTF(profile.getUserName());
                    out.flush();

                    String answer = in.readUTF();
                    if (answer.equals("Pass")){
                        break;
                    }
                    else if(answer.equals("YouShallNotPass")){
                        System.out.println("Unregistered User, try again");
                    }
                    else{
                        System.out.println("User online");
                    }
                    profile.setUserName();
                }

                out.writeUTF("exit");
                out.flush();

                brokerPortMap.putAll((HashMap<Integer, Integer>) in.readObject());
                TopicsBroker = (HashMap<String, Integer>) in.readObject();
            }
            else{
                out.writeUTF("start");
                out.flush();
                out.writeUTF(profile.getUserName());
                out.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /*__________SUPER__________*/


    @Override
    public void disconnect() {

    }

    @Override
    public void init(int ID) {

    }

    @Override
    public void updateNodes() {

    }

    /*__________CONSUMER__________*/
    @Override
    public void disconnect(String Name) {

    }

    @Override
    public void register() {

    }

    @Override
    public void showConversationData(String kati, Value katiAllo) {

    }


    /*__________PUBLISHER__________*/


//    @Override
//    public ArrayList<Value> generateChunks(MultiMediaFile kati) {
//        return null;
//    }


//    @Override
//    public BrokerInterface hashTopic() {
//        return null;
//    }

    @Override
    public void getBrokerList() {

    }

    @Override
    public void notifyBrokersNewMessage(String kati) {

    }

    @Override
    public void notifyFailure(BrokerInterface kati) {

    }
}
