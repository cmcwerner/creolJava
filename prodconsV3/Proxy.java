import creole.*;
import java.util.*;

public class Proxy extends CreoleObject {
  private int limit;
  private Service s;
  private ArrayList<Client> myClients = new ArrayList<Client>();
  private Proxy nextProxy;
  Proxy(int limit, Service s) {
    this.limit = limit;
    this.s = s;
  }
  
  public Proxy add(Client cl) {
    Proxy lastProxy = this;
    if (myClients.size() < limit) {
      myClients.add(cl);
    }
    else {
      if (nextProxy == null) {
        nextProxy = new Proxy(limit, s);
      }
      lastProxy = nextProxy.add(cl);
    }
    return lastProxy; // put??
  }
  
  public int silly() {
    return 0;
  }
  
  public void publish(Future fut) {
    // insert some silly useless call that makes a get to a already filled future
   // Future trivial = this.invoke("silly");
  //  int x = (Integer)creoleAwait(trivial);
    
    // send the news item to each of this proxies clients
    for (Client c : myClients) {
      c.invoke("signal",fut); 
    }
    // pass this news item on to other proxies in the list
    // note that this passing along the list can happen without waiting for any acknowledgement from the signaled clients
    if (nextProxy != null) {
      nextProxy.invoke("publish",fut); 
    }
    else {
      // when we hit the end of the list of proxies, we tell the news service to check for the next news item
      s.invoke("produce");
    }
  }
}