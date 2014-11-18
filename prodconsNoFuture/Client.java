import creole.*;

public class Client extends CreoleObject{
  private News news = new News(0);
  
  public void signal(News ns) { 
    news = ns;
    System.out.println(news);
  }
}