package ServerClient;
import java.net.Inet4Address;
import java.util.*;

public interface NodeInterface {
    List<Broker> brokerList = new ArrayList<>();

    public void connect(String ip, int port, boolean onlyGetInfo);
    public void disconnect();
    public void init(int ID);
    public void updateNodes();
}
