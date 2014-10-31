import creole.*;

public class Producer extends CreoleObject{
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