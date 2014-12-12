import creole.*;
import java.util.*;

public class NewsProducer extends CreoleObject{
  private ArrayList<News> requests = new ArrayList<News>();
  
  public void add(News ns) {
    requests.add(ns);
  }
  public News getNews() {
    while (requests.size() <= 0) creoleAwait();
    News firstNews = requests.remove(0);
    return firstNews;
  }
}

