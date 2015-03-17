import creol.*;

class BBMain extends CreolObject{
  public static void main(String[] args) {
    new BBMain().test();
  }
  void test() {
    BoundedBuffer buf = new BoundedBuffer(3);
    Producer prod = new Producer();
    Consumer cons = new Consumer();
    
    prod.invoke("startProducing",buf);
    cons.invoke("startConsuming",buf);
  }
}