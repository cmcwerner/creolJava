import creole.*;

public class Producer extends CreoleObject {
  int num;
  public Object produceOne(BoundedBuffer buf) {
    System.out.println("inserting " + num);
    return buf.invoke("insert",new Item(++num)).get(); 
  }
  public void startProducing(BoundedBuffer buf) {
    while(true) {
      this.invoke("produceOne", buf); // ignore the result but get it to make this a synchronous call
      creoleSuspend();
    }
  }
}