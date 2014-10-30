import creole.*;

public class Consumer extends CreoleObject {
  public Item consumeOne(BoundedBuffer buf) {
    return (Item)buf.invoke("remove").get();
  }
  public void startConsuming(BoundedBuffer buf) {
    while (true) {
      // it is wrong to use .get() here because the ojbects thread is blocked and can't process the consumeOne() call
//      Item item = (Item)creoleAwait(this.invoke("consumeOne", buf));
      // doesn't seem to be any reason to use invoke() for synchronous self calls
      Item item = consumeOne(buf);
      System.out.println(item);
    }
  }
}