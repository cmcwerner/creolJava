import creole.*;

public class Producer extends CreoleObject{
  private NewsProducer np;
  Producer(NewsProducer np) {
    this.np = np;
  }
  public News detectNews () {
    News news = (News)np.invoke("getNews").get();
    return news;
  }
}