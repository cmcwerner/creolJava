class Demo extends CreoleObject{
  public static void main(String[] args) {
    new Demo().test();
  }
  void test() {
    BoundedBuffer buf = new BoundedBuffer(3);
    BasicProducer prod = new BasicProducer();
    BasicConsumer cons = new BasicConsumer();
    
    prod.invokeVoid("startProducing",buf);
    cons.invokeVoid("startConsuming",buf);
  }
}

class BoundedBuffer extends CreoleObject {
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

class BasicProducer extends CreoleObject {
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

class BasicConsumer extends CreoleObject {
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

class Item {
  Item(int payload) { this.payload = payload;}
  int payload;
  public String toString() { return payload+"";}
}