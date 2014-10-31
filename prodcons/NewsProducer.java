import creole.*;
import java.util.*;

public class NewsProducer extends CreoleObject{
  private ArrayList<News> requests = new ArrayList<News>();
  
  public void add(News ns) {
    requests.add(ns);
  }
  public News getNews() {
    News firstNews = requests.remove(0);
    return firstNews;
  }
  public int getRequests() {
    return requests.size();
  }
}

