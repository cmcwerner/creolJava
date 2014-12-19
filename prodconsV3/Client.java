import creole.*;

public class Client extends CreoleObject{
  private News news = new News(0);
  
  public void signal(Future futureNews) { 
    // using await rather than get here allows the client to process the news in the order it
    // becomes available rather than in the order that the futures arrive
    news = (News)creoleAwait(futureNews);
 //   news = (News)futureNews.get();
  //  System.out.println("news: " + news);
  }
}