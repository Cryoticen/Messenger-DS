package ServerClient;
import java.util.*;

public interface BrokerInterface extends NodeInterface {
    List<ConsumerInterface> RegisteredUsers= null;
    List<PublisherInterface> RegisteredPublishers= null;

    public ConsumerInterface acceptConnection(ConsumerInterface kati);
    public PublisherInterface acceptConnection(PublisherInterface kati);
    public void calculateKeys();
    public void filterConsumers(String kati);
    public void notifyBrokersOnChanges();
    public void notifyPublisher(String kati);
    public LinkedList<Value> pull(String kati);
}
