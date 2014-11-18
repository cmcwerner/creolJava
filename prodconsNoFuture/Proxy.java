import creole.*;
import java.util.*;

public class Proxy extends CreoleObject {
  private int limit;
  private Service s;
  private Producer prod;
  private ArrayList<Client> myClients = new ArrayList<Client>();
  private Proxy nextProxy;
  Proxy(int limit, Service s, Producer prod) {
    this.limit = limit;
    this.s = s;
    this.prod = prod;
  }
  
  public Proxy add(Client cl) {
    Proxy lastProxy = this;
    if (myClients.size() < limit) {
      myClients.add(cl);
    }
    else {
      if (nextProxy == null) {
        nextProxy = new Proxy(limit, s, prod);
      }
      lastProxy = nextProxy.add(cl);
    }
    return lastProxy;
  }
  
  public void start_publish() {
    News ns = (News)creoleAwait(prod.invoke("detectNews"));
    this.invoke("publish", ns);
  }
  
  public void publish(News ns) {

    // send the news item to each of this proxies clients
    for (Client c : myClients) {
      c.invoke("signal",ns); 
    }
    // pass this news item on to other proxies in the list
    // note that this passing along the list can happen without waiting for any acknowledgement from the signaled clients
    if (nextProxy != null) {
      nextProxy.invoke("publish",ns); 
    }
    else {
      // when we hit the end of the list of proxies, we tell the news service to check for the next news item
      s.invoke("produce");
    }
  }
}