import creol.*;

public class Client extends CreolObject{
  private News news = new News(0);
  
  public void signal(News ns) { 
    news = ns;
    System.out.println(news);
  }
}