package creol;

import java.util.ArrayList;

/**
 * Instances of Future are used to hold values that will eventually be produced by a method in an active CreolObject.
 * It has just one public method, get(), used to extract the value from the future.
 * If the value is not yet available the thread making the call will be suspended with a standard Java wait().
 */
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
  /**
   * If the value of this Future has been produced, return it. Otherwise, wait.
   * The thread will be awakened (notified) when the method associated with this future completes.
   * @return - the value stored in this future.
   */
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