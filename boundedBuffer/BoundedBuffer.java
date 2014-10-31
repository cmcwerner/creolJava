import creole.*;

public class BoundedBuffer extends CreoleObject {
  private int head, tail, cnt;
  private Item[] buf;
  BoundedBuffer(int size) {
    buf = new Item[size];
  }
  // CreoleObject methods that can be invoked by other CreoleObjects must be public
  // because they are actually being called by name from the CreoleObject class
  // which is in a separate package.
   public void insert(Item item) {
    while (cnt >= buf.length) creoleAwait();
    buf[head] = item;
    head = (head+1)%buf.length;
    cnt++; 
  }
   public Item remove() {
    while (cnt <= 0) creoleAwait();
    Item result = buf[tail];
    tail = (tail+1)%buf.length;
    cnt--;
    return result;
  }
}