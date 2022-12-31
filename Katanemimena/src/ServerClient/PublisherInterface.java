package ServerClient;

public interface PublisherInterface extends NodeInterface {
    ProfileName profileName = null;

    public void getBrokerList();
    public void notifyBrokersNewMessage(String kati);
    public void notifyFailure(BrokerInterface kati);
    public void push(String kati, Value value);
}
