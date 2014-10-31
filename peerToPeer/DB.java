import creole.*;
import java.util.*;

public class DB extends CreoleObject {
  private HashMap<String,ArrayList<Package>> store = new HashMap<String,ArrayList<Package>>();
  
  public ArrayList<Package> getFile(String fileId) {
    return store.get(fileId);
  }
  public int getLength(String fileId) {
    if (store.containsKey(fileId)) {
      return store.get(fileId).size();
    }
    else return 0;
  }
  public void storeFile(String fileId, ArrayList<Package> data) {
    store.put(fileId, data); // at some point I should probably copy the data to avoid modification
  }
  public Set<String> listFiles() {
    return new HashSet<String>(store.keySet());    
  }
  
  void dump() {
    System.out.println(this);
    for (String key : store.keySet()) {
      System.out.println("dumping " + key);
      for (Object pkg : store.get(key)) {
        System.out.println(((Package)pkg).content);
      }
    }
  }
}