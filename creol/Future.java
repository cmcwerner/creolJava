package creol;

import java.util.ArrayList;

public class Future {
  boolean ready = false;
  Object value;
  // a list of calls that performed an await on this future
  // they all need to be notified once it becomes ready
  ArrayList<CreolObject.CreolCall> waiters = new ArrayList<CreolObject.CreolCall>();
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
    notifyAll(); // wake up threads blocked in get()
    // wake up threads blocked via await
    for (CreolObject.CreolCall call : waiters) {
      synchronized(call) {
        call.wakingUp = true; // otherwise might miss this notify - there is a race
        call.notify();
      }
    }
  }
  public synchronized Object get() { 
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
  synchronized boolean addWaiter(CreolObject.CreolCall call) {
//    if (call == null) {
//      System.out.println("trouble");
//    }
    if (ready) {
      return true;
    }
    else {
      waiters.add(call);
      return false;
    }
  }
}