package creole;
import java.util.ArrayList;
import java.lang.reflect.Method;

public class CreoleObject extends Thread {
  static private int nextId = 0;
  final int id = ++nextId;
  private ArrayList<CreoleCall> calls = new ArrayList<CreoleCall>();
  private ArrayList<CreoleCall> suspended = new ArrayList<CreoleCall>(); // tasks that volunarily suspended
  private ArrayList<CreoleCall> futureWait = new ArrayList<CreoleCall>(); // tasks that did await(future) and the future still no ready
  private ArrayList<CreoleCall> condWait = new ArrayList<CreoleCall>(); // tasks that did creoleAwait() - awaken only if something happens 
  private CreoleCall current = null;
  
  protected CreoleObject() {
    this.start();
  }
  
  public synchronized Future invoke(String method, Object... args) {
    Future fut = new Future();
    CreoleCall newCall = new CreoleCall(method, fut, args);
    calls.add(newCall);
    debug("new call added " + this.getClass() + " " +method);
    notify();
    return fut;
  }  
  
  public Object creoleAwait(Future fut) {
    // busy waits only if nothing else to do
//    while(!fut.ready) {
//      creoleSuspend();
//    }
//    return fut.get();
//    
    // put on a queue like with suspend and also put in the list for the future
    // when the futures becomes ready it will tell each in the list to move from waiting to suspended
    if (!fut.ready) {
      assert current != null : id;
      CreoleCall waiter = current;
      boolean ready; // might have become ready since the check just above - will need to check again for sleeping
      synchronized(fut) {
        ready = fut.addWaiter(current); // returns the current value of ready - if true then it was not put in the future queue       
      }
      if (!ready) {
        // this call has been recorded in the waiters list for fut and the future was not ready at that time, although
        // we are out of the atomic section so it could have become ready by now
        debug("moving to futureWait " + current);
        moveToQueue(futureWait); // put in a queue and wait. will also set current to null and notify the dispatcher
        // we have been woken up becuase the future is now ready
        assert (fut.ready);
        // move waiter from futureWait to supsended
        synchronized (this) {
          boolean success = futureWait.remove(waiter);
          assert(success);
          if (current == null) {
            // there was no active call so make this one the active call
            assert(waiter != null);
            debug("waiter immediately activated " + waiter + ":" + this);
            current = waiter;
          }
          else {
            // there is already an active call in this object so put this one in the suspended queue
            debug("waiter suspending " + waiter + ":" + this);
            suspended.add(waiter);
          }
        }
        // if this isn't the active current call then put this thread to sleep (it is in the suspended queue)
        if (current != waiter) {
          assert(suspended.contains(waiter));
          debug("waiter trying to sleep " + waiter + ":" + this);
          tryToSleep(waiter);
          // can I get here without being current?
          // this is it, if I wake up before suspending this one could be running along with current
          // when I get here - mixing up wakingUp from future vs waking up from suspended????
        }
        debug("waiter continuing as current " + waiter + ":" + this);
      }
    }
    return fut.get();           
  }
  
  /* moves the current call to the specified queue (currently either suspended or futureWait
   * and then has the thread(call) wait.
   */
  private void moveToQueue(ArrayList<CreoleObject.CreoleCall> queue) {
    CreoleCall suspendee;
    // first put it in the appropriate queue and mark this object as not busy, waking up the dispatcher
    synchronized(this) {
      assert current!=null : id;
      suspendee = current;
      queue.add(current);
      current = null; // no longer busy
      notify(); // wake up the dispatcher to find something else to do
    }
    
    // now actully put this thread to sleep but make sure it didn't get woken up immediately by the dispatcher
    tryToSleep(suspendee);
    // back from sleeping. This call has been scheduled as the active call in this object
    assert(suspendee == current || queue == futureWait);
  }
  
  private void tryToSleep(CreoleCall suspendee) {
    synchronized(suspendee) {
      if (!suspendee.wakingUp) {
        try {
          //System.out.println("suspending");
          suspendee.wait();
        }
        catch (InterruptedException e) {
          e.printStackTrace(System.out);
        }
      }
      else {
        //System.out.println("wake up before suspending");
      }
      suspendee.wakingUp = false;
    }
  }
  
  public void creoleSuspend() {
    moveToQueue(suspended);
  }
  
  // this is like creoleSuspend() except that it won't be reawakened until some object state has potentially changed
  public void creoleAwait() {
    moveToQueue(condWait);
  }
  
  void wakeUpCondWaiters() {
    synchronized(this) {
      suspended.addAll(condWait);
      condWait.clear();      
    }
  }
  
  public void run() {
    try {
      while (true) {
        // might get notified of a new call while still busy with something else
        // only select a new one if not active already
        if (current == null) {
          // see if any new calls to process
          if(calls.size() > 0) {
            CreoleCall call;
            synchronized (this) {
              call = calls.remove(0);
              assert(call != null);
              current = call; // now busy
              debug("busy: starting " + current + ":" + this);
              wakeUpCondWaiters();
              call.start();
            }
          }
          // no calls, what about any suspended calls
          else if (suspended.size() > 0) {
            CreoleCall call;
            synchronized (this) {
              call = suspended.remove(0);
              assert current==null:id;
              current = call; // now busy
              assert(current != null);
              debug("busy: resuming " + current + ":" + this);
              wakeUpCondWaiters();
            }
            synchronized (call) {
              call.wakingUp = true;
              call.notify();
            }
          }
        }
        
        // If there is an active call or nothing to do, just wait
        // need to recheck the two queue lengths because something could have come in since checked above
        synchronized (this) {
          if (current != null || (calls.size() == 0 && suspended.size() == 0)) {
            if (current == null) {
              debug(this + "waiting no active call");
            }
            else {
              debug(this + " waiting a call is active.");
            }
            wait();
          }
        }
      }
    }
    catch (InterruptedException e) {
      System.out.println(this + " exiting");
    }
  }
  
  // inner class because every call must be associated with some CreoleObject
  class CreoleCall extends Thread {
    String method;
    Future fut;
    Object[] args;
    boolean wakingUp = false;
    CreoleCall(String method, Future fut, Object... args) {
      this.method = method;
      this.fut = fut;
      this.args = args;
    }
    
    final public void run() {
      debug("processing call " + method + ":"+current + ":" + this);
      invoke();
      // call is over - notify the dispatcher that it can schedule another
      synchronized (CreoleObject.this) {
        debug("free: call ended " + method + ":" + current + ":" + this);
        current = null; // no longer busy
        CreoleObject.this.notify();
      }
    }
    private void invoke() {
      Method m = null;
      try {
        // create array of classes representing the types of the args
        Class[] types = new Class[args.length];
        for(int i = 0; i < args.length; i++) {
          types[i] = args[i].getClass();
        }
        m = CreoleObject.this.getClass().getMethod(method,types);
      }
      catch (NoSuchMethodException e) {
        e.printStackTrace(System.out);
      }
      if (m != null) {
        try {
          if (fut != null) {
            Object result = m.invoke(CreoleObject.this,args);
            fut.set(result);
            debug("Setting future " + this + " " + method);
          }
          else {
            m.invoke(CreoleObject.this,args);
            debug("end of void method " + this + " " + method);
          }
        }
        catch (Exception e) {
          e.printStackTrace(System.out);
        }
      }
    }
  }
  
  void debug(String msg) {
    //System.out.println("dbg " +id + msg);
  }
}
