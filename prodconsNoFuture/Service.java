import creole.*;

public class Service extends CreoleObject {
  private Producer prod;
  private Proxy proxy;
  private Proxy lastProxy;
  Service(int limit, NewsProducer np) {
    prod = new Producer(np);
    proxy = new Proxy(limit, this, prod);
    lastProxy = proxy;
    this.invoke("produce"); 
  }
  
  public void subscribe(Client cl) {
    lastProxy = lastProxy.add(cl);
  }
  
  public void produce() {
    proxy.invoke("start_publish"); 
  }
}