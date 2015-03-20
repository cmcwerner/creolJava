import creol.*;

public class Producer extends CreolObject{
  private NewsProducer np;
  Producer(NewsProducer np) {
    this.np = np;
  }
  public News detectNews () {
    News news = (News)np.invoke("getNews").get();
    return news;
  }
}