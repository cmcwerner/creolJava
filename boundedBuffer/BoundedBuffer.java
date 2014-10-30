import creole.*;

public class BoundedBuffer extends CreoleObject {
  int head, tail, cnt;
  Item[] buf;
  BoundedBuffer(int size) {
    buf = new Item[size];
  }
  public Object insert(Item item) {
    while (cnt >= buf.length) {
      creoleSuspend();
    }
    buf[head] = item;
    head = (head+1)%buf.length;
    cnt++; 
    return new Object();
  }
  public Item remove() {
    while (cnt <= 0) {
      creoleSuspend();
    }
    Item result = buf[tail];
    tail = (tail+1)%buf.length;
    cnt--;
    return result;
  }
}