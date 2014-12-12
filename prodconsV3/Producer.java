import creole.*;

public class Producer extends CreoleObject{
  private Future np;
  Producer(Future np) {
    this.np = np;
  }
  public void detectNews () {
    News news = (News)np.get();
  //  return news;
  }
}