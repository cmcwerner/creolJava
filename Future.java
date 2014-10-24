import java.util.ArrayList;

class Future {
  boolean ready = false;
  Object value;
  // a list of calls that performed an await on this future
  // they all need to be notified once it becomes ready
  ArrayList<CreoleObject.CreoleCall> waiters = new ArrayList<CreoleObject.CreoleCall>();
  Future() {
    ready = false;
  }
  Future(Object value) {
    this.value = value;
    ready = true;
  }
  synchronized void set(Object val) {
    assert(!ready);
    value = val;
    ready = true;
    notifyAll();
  }
  synchronized Object get() { 
    try {
      if (!ready) {
        wait();
      }
    }
    catch (InterruptedException e) {
      System.out.println(e);
    }
    return value;
  }
}