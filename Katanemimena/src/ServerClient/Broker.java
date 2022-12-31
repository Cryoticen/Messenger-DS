package ServerClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;

public class Broker extends Thread implements BrokerInterface {

    String projectDir = Paths.get("").toAbsolutePath().toString();
    private final String PATH  = projectDir + "\\src";
    private final String SERVER_PATH  = "\\DataBase\\Server";

    ServerSocket providerSocket;
    Socket connection = null;
    int brokerID;
    int portNumber;

    public static ArrayList<String> connectedUsers = new ArrayList<>();
    protected LinkedList<Value> data = new LinkedList<>();

    public Broker (int brokerID, int portNumber){

        super();
        this.brokerID = brokerID;
        this.portNumber = portNumber;
    }

    public static void main(String[] args) {

        Broker broker1 = new Broker(1, 4321);
        System.out.println("Broker 1 connected.");
        brokerList.add(broker1);
        broker1.start();

        Broker broker2 = new Broker(2, 4322);
        System.out.println("Broker 2 connected.");
        brokerList.add(broker2);
        broker2.start();

        Broker broker3 = new Broker(3, 4323);
        System.out.println("Broker 3 connected.");
        brokerList.add(broker3);
        broker3.start();

        try{
            broker1.join();
            broker2.join();
            broker3.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {

            /* Create Server Socket */
            providerSocket = new ServerSocket(portNumber);

            while (true) {
                /* Accept the connection */
                connection = providerSocket.accept();
                System.out.println("User connected");

                /* Handle the request */
                Thread t = new ActionsForClients(connection, this);
                t.start();
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public synchronized void addData(Value value){

        this.data.add(value);
        if(value.type.equals("textMessage")){
            String username = value.sender;
            String topic = value.key;
            String text = value.text;


            File txtTopic = new File(PATH + SERVER_PATH + "\\" + topic + "\\" + topic.toLowerCase(Locale.ROOT) +".txt");
            FileWriter w;
            try{
                w = new FileWriter(txtTopic, true);
                if (txtTopic.length() == 0) {
                    w.write(username + ",, " +text);
                    w.close();
                } else {
                    w.write("\n" + "\n" + username + ",, " +text);
                    w.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public synchronized LinkedList<Value> pull(String topic) {

        return null;
    }

    @Override
    public ConsumerInterface acceptConnection(ConsumerInterface kati) {
        return null;
    }

    @Override
    public PublisherInterface acceptConnection(PublisherInterface kati) {
        return null;
    }

    @Override
    public void calculateKeys() {

    }

    @Override
    public void filterConsumers(String kati) {

    }

    @Override
    public void notifyBrokersOnChanges() {

    }

    @Override
    public void notifyPublisher(String kati) {

    }



    /*SUPER*/

    @Override
    public void connect(String ip, int port, boolean onlyGetInfo) {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public synchronized void init(int brokerID) {

    }

    @Override
    public void updateNodes() {

    }
}
