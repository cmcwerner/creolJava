import java.util.ArrayList;
import creole.*;

//enum News {E1, E2, E3, E4, E5, None}
class News {
  int item;
  News(int x) { item = x;}
  public String toString() {return item+"";}
}

class ProdCons {
  public static void main(String[] args) {
    NewsProducer np = new NewsProducer();

    Service srv = new Service(4, np);
    
    Client c = new Client();
    srv.subscribe(c);
    c = new Client();
    srv.subscribe(c);
    c = new Client();
    srv.subscribe(c);
    
    int count = 0;
    while(count < 1000) {
      np.add(new News(++count));
      try {
        Thread.currentThread().sleep((int)(Math.random()*100));
      }
      catch (InterruptedException e) {}
    }
  }
}
class Service extends CreoleObject {
  private Producer prod;
  private Proxy proxy;
  private Proxy lastProxy;
  Service(int limit, NewsProducer np) {
    prod = new Producer(np);
    proxy = new Proxy(limit, this);
    lastProxy = proxy;
    this.invokeVoid("produce"); 
  }
  
  public void subscribe(Client cl) {
    lastProxy = lastProxy.add(cl);
  }
  
  public void produce() {
    Future fut = prod.invoke("detectNews");
    proxy.invokeVoid("publish",fut); 
  }
}

class Proxy extends CreoleObject {
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
      c.invokeVoid("signal",ns); 
    }
    if (nextProxy == null) {
      s.invokeVoid("produce");
    }
    else {
      nextProxy.invokeVoid("publish",fut); 
    }
  }
}

class Producer extends CreoleObject{
  private NewsProducer np;
  Producer(NewsProducer np) {
    this.np = np;
  }
  public News detectNews () {
    News news = new News(0);
    int requests = (Integer)np.invoke("getRequests").get();
    while (requests == 0) {
      try {
        sleep(500);
      }
      catch (InterruptedException e) {}
      requests = (Integer)np.invoke("getRequests").get();
    }
    news = (News)np.invoke("getNews").get();
 //   this.creoleSuspend(); // wasteful - just to see if it keeps running - nothing else for it to do
    return news; // put??
  }
}

class NewsProducer extends CreoleObject{
  private ArrayList<News> requests = new ArrayList<News>();
  
  public synchronized void add(News ns) {
    requests.add(ns);
  }
  public synchronized News getNews() {
    News firstNews = requests.remove(0);
    return firstNews; // put??
  }
  public synchronized int getRequests() {
    return requests.size(); // put??
  }
}


class Client extends CreoleObject{
  private News news = new News(0);
  
  public void signal(News ns) { 
    news = ns;
    System.out.println(news);
  }
}