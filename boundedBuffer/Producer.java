import creole.*;

public class Producer extends CreoleObject {
  int num;
  public void produceOne(BoundedBuffer buf) {
    buf.invoke("insert",new Item(++num)).get(); 
  }
  public void startProducing(BoundedBuffer buf) {
    while(true) {
//      creoleAwait(this.invoke("produceOne", buf)); // ignore the result but get it to make this a synchronous call
      this.produceOne(buf);
    }
  }
}