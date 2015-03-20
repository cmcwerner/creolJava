import java.util.ArrayList;
import creol.*;

class ProdCons extends CreolObject {
  public static void main(String[] args) {
    new ProdCons().main();
  }
  void main() {
    NewsProducer np = new NewsProducer();

    Service srv = new Service(4, np);
    
    Client c = new Client();
    srv.invoke("subscribe",c);
    c = new Client();
    srv.invoke("subscribe",c);
    c = new Client();
    srv.invoke("subscribe",c);
    
    int count = 0;
    while(count < 100) {
      System.out.println("active calls " + CreolObject.activeCalls);
      np.invoke("add",new News(++count));
      try {
        //Thread.currentThread().sleep((int)(Math.random()*100));
        sleep((int)(Math.random()*100));
      }
      catch (InterruptedException e) {}
    }
  }
}


//enum News {E1, E2, E3, E4, E5, None}
class News {
  int item;
  News(int x) { item = x;}
  public String toString() {return item+"";}
}









