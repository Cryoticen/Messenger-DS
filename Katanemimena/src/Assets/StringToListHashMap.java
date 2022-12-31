package Assets;

import javax.imageio.stream.ImageInputStream;
import java.io.Serializable;
import java.util.*;

public class StringToListHashMap implements Serializable {
    Map<String, ArrayList<String>> multiMap = new HashMap<String, ArrayList<String>>();

    public void put(String key, String subscriber) {
        ArrayList<String> list;

        if (multiMap.containsKey(key)) {
            list = multiMap.get(key);
            list.add(subscriber);
        } else {
            list = new ArrayList<String>();
            list.add(subscriber);
            multiMap.put(key, list);
        }
    }

    public ArrayList<String> get(String key){
        return multiMap.get(key);
    }

    public ArrayList<String> getKeys(){
        ArrayList<String> keys = new ArrayList<>();

        keys.addAll(multiMap.keySet());
        return keys;
    }
}
