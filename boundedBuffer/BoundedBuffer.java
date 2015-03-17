import creol.*;

public class BoundedBuffer extends CreolObject {
  private int head, tail, cnt;
  private Item[] buf;
  BoundedBuffer(int size) {
    buf = new Item[size];
  }
  // CreolObject methods that can be invoked by other CreolObjects must be public
  // because they are actually being called by name from the CreolObject class
  // which is in a separate package.
   public void insert(Item item) {
    while (cnt >= buf.length) creolAwait();
    buf[head] = item;
    head = (head+1)%buf.length;
    cnt++; 
  }
   public Item remove() {
    while (cnt <= 0) creolAwait();
    Item result = buf[tail];
    tail = (tail+1)%buf.length;
    cnt--;
    return result;
  }
}