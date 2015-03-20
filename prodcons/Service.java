import creol.*;

public class Service extends CreolObject {
  private Producer prod;
  private Proxy proxy;
  private Proxy lastProxy;
  Service(int limit, NewsProducer np) {
    prod = new Producer(np);
    proxy = new Proxy(limit, this);
    lastProxy = proxy;
    this.invoke("produce"); 
  }
  
  public void subscribe(Client cl) {
    lastProxy = (Proxy)lastProxy.invoke("add",cl).get();
  }
  
  public void produce() {
    Future fut = prod.invoke("detectNews");
    proxy.invoke("publish",fut); 
  }
}