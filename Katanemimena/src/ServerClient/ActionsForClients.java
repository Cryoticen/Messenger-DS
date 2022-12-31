package ServerClient;

import Assets.StringToListHashMap;
import Assets.FileReader;

import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
import java.lang.*;

public class ActionsForClients extends Thread {
    String projectDir = Paths.get("").toAbsolutePath().toString();
    private final String SERVER_PATH  =  projectDir + "\\src\\DataBase\\Server";

    private final String TOPIC_LIST_PATH  = SERVER_PATH + "\\topics.txt";
    private final String CREDENTIALS_PATH  = SERVER_PATH + "\\credentials.txt";


    public static HashMap<String, ActionsForClients> actionsForClientsMap = new HashMap<>();
    protected static StringToListHashMap ActiveTopicSubscribers = new StringToListHashMap();
    Broker Parent;

    public ObjectInputStream in;
    public ObjectOutputStream out;
    String clientUsername;
    Socket connection;
    FileReader topicReader;

    public ActionsForClients(Socket connection, Broker parent) {
        System.out.println("Got a connection from " + connection.getInetAddress().getHostName());

        this.topicReader = new FileReader(TOPIC_LIST_PATH);
        this.Parent = parent;
        this.connection = connection;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void login(){
        FileReader identifyUser = new FileReader(CREDENTIALS_PATH);
        try {
            while (true) {
                boolean flag = false;
                this.clientUsername = in.readUTF();
                if (identifyUser.Authenticate(this.clientUsername)) {
                    for (String user: Broker.connectedUsers) {

                        if (user.equals(this.clientUsername)) {
                            out.writeUTF("Already online");
                            out.flush();
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        out.writeUTF("Pass");
                        out.flush();
                        Broker.connectedUsers.add(clientUsername);
                        break;
                    }
                }
                else {
                    out.writeUTF("YouShallNotPass");
                    out.flush();
                }
            }

            out.writeObject(getBrokerPortMap());
            out.flush();
            out.writeObject(getTopicBroker());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Integer> getTopicBroker() {
        HashMap<String, Integer> topicsBroker = new HashMap<>();
        File topics = new File(TOPIC_LIST_PATH);
        try{
            Scanner scanner = new Scanner(topics);

        while(scanner.hasNextLine()){
            String topic = scanner.nextLine();
            topicsBroker.put(topic, Math.floorMod( topic.hashCode(), 3) + 1); //brokers start from 1
        }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return topicsBroker;
    }

    public void run() {

        boolean isConnected = true;
        while(isConnected) {
            try {
                String action = in.readUTF();

                if (action.equals("start")) {
                    this.clientUsername = in.readUTF();
                    actionsForClientsMap.put(clientUsername, this);
                }
                else if(action.equals("login")){
                    login();
                }
                else if(action.equals("logout")){
                    Broker.connectedUsers.remove(clientUsername);
                    break;
                }
                else if (action.equals("enterChat")) {
                    String topic = in.readUTF();
                    ActiveTopicSubscribers.put(topic, clientUsername);

                    serverPrint(topic, clientUsername + " has entered the chat.");

                    loadHistory(topic);

                    while (isConnected) {
                        Value msg = null;
                        try {
                            msg = (Value) in.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (msg.text.equals("exit")){
                            isConnected = false;
                            try {
                                out.writeObject(new Value(" ", " ", " ", "exit"));
                                out.flush();
                                connection.close();
                                actionsForClientsMap.remove(this.clientUsername);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }

                        if(msg.type.equals("textMessage")){
                            Parent.addData(msg);
                            LinkedList<Value> value = pull(topic);
                            broadcastValue(topic, value);
                        }
                        else if(msg.type.equals("Image")){
                            readImage(topic);
                        }
                        else if (msg.type.equals("Video")){
                            readImage(topic);
                        }

                    }

                    serverPrint(topic, clientUsername + " has left the chat.");


                }
                else if (action.equals("seeProfile")) {
                    String username = in.readUTF();


                }
                else {
                    break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            in.close();
            out.close();
            actionsForClientsMap.remove(this.clientUsername);
            System.out.println("disconnected");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void readImage(String topic){
        try {
            int imageFileNameLength = in.readInt();
            String imageFileName = null;
            if (imageFileNameLength > 0) {
                byte[] imageFileNameBytes = new byte[imageFileNameLength];
                in.readFully(imageFileNameBytes, 0, imageFileNameBytes.length);
                imageFileName = new String(imageFileNameBytes);

                ArrayList<Byte> constructImage = new ArrayList<Byte>();

                for(int i = 1; i <= 10; i++){
                    int chunkLength = in.readInt();
                    if (chunkLength > 0) {
                        byte[] chunkContentBytes = new byte[chunkLength];
                        in.readFully(chunkContentBytes, 0, chunkLength);
                        for(int j = 0; j < chunkContentBytes.length; j++){
                            constructImage.add(chunkContentBytes[j]);
                        }
                    }
                }
                System.out.println(constructImage.size());
                byte[] finishedImage = new byte[constructImage.size()];
                for(int j = 0; j < constructImage.size(); j++){
                    finishedImage[j] = constructImage.get(j);
                }

//                ByteArrayInputStream bis = new ByteArrayInputStream(finishedImage);
//                BufferedImage bImage2 = ImageIO.read(bis);
//                ImageIO.write(bImage2, "jpeg", new File(projectDir + "\\ClientDirectory\\" + clientID + "dir\\" + imageFileName) );
//                System.out.println("image created");

                File imageFile = new File(SERVER_PATH + "\\" + topic + "\\" + imageFileName);
                FileOutputStream fileOutputStream = new FileOutputStream(imageFile);

                fileOutputStream.write(finishedImage);
                fileOutputStream.close();
            }

        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    private void loadHistory(String topic) {
        File history = new File(SERVER_PATH + "\\" + topic + "\\" + topic.toLowerCase(Locale.ROOT) + ".txt");
        try{
            Scanner scanner = new Scanner(history);
            while(scanner.hasNextLine()){
                String[] line = scanner.nextLine().split(",,");

                if(line.equals("")) continue;

                if(line.length == 2){
                    out.writeObject(new Value(topic, "textMessage", line[0], line[1]));
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastValue(String topic, LinkedList<Value> data) {

        ArrayList<String> users = new ArrayList<>(ActiveTopicSubscribers.get(topic));

        for (Value value: data){
            for(String user: users){
                if (!user.equals(clientUsername)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (value.type.equals("textMessage")){
                                    actionsForClientsMap.get(user).out.writeObject(value);
                                    out.flush();
                                }
                                else{
                                    ///////////////////////////////////////////Image
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        }
    }

    public void serverPrint(String topic, String message){
        ArrayList<String> users = new ArrayList<>(ActiveTopicSubscribers.get(topic));

        Value val = new Value(topic, "textMessage", "Server", message);
        LinkedList<Value> kappa = new LinkedList<>();
        kappa.add(val);

        broadcastValue(topic, kappa);
    }

    public synchronized LinkedList<Value> pull(String topic) {
        LinkedList<Value> data = new LinkedList<>();

        if(!Parent.data.isEmpty()){
            for(Value value: Parent.data){
                if (value.key.equals(topic)){
                    data.add(value);
                    Parent.data.remove(value);
                }
            }
        }

        return data;
    }

    public HashMap<Integer, Integer> getBrokerPortMap(){
        HashMap<Integer, Integer> brokerPortMap = new HashMap<>();
        for (Broker broker: Parent.brokerList){
            brokerPortMap.put(broker.brokerID, broker.portNumber);
        }
        return brokerPortMap;
    }

}