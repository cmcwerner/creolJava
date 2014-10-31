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
  
  public void publish(Future fut) {
    News ns = new News(0);
 //   ns = (News)fut.get();
    ns = (News)creoleAwait(fut);
    // invoke(myClients.signal(ns));
    for (Client c : myClients) {
      c.invoke("signal",ns); 
    }
    if (nextProxy == null) {
      s.invoke("produce");
    }
    else {
      nextProxy.invoke("publish",fut); 
    }
  }
}