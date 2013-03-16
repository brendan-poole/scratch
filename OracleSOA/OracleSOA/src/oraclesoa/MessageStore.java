package oraclesoa;

import java.util.List;

public interface MessageStore {
    public void putMessage(AdapterMessage msg) ;
    public List<AdapterMessage> getMessageFiltered() ;
}
