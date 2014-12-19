import creole.*;

public class Service extends CreoleObject {
  private Producer prod;
  private Proxy proxy;
  private Proxy lastProxy;
  private Future globalNews;
  Service(int limit, Future fut) {
    prod = new Producer(fut);
    proxy = new Proxy(limit, this);
    globalNews = fut;
    lastProxy = proxy;
    this.invoke("produce"); 
  }
  
  public void subscribe(Client cl) {
    lastProxy = (Proxy)lastProxy.invoke("add",cl).get();
  }
  
  public void produce() {
    prod.invoke("detectNews");
     try {
        sleep(300);
      }
      catch (InterruptedException e) {}
    proxy.invoke("publish",globalNews); 
  }
}