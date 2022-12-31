package ServerClient;

public interface ConsumerInterface extends NodeInterface {

    void disconnect(String Name);
    public void register();
    public void showConversationData(String kati, Value  value);

}
