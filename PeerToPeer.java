import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("unchecked")
class PeerToPeer extends CreoleObject {
  public static void main(String[] args) {
    new PeerToPeer();
  }
  public void run() {
    // make some nodes in the network
    int numNodes = 10;
    PeerNode[] nodes = new PeerNode[numNodes];
    
    DB[] dbs = new DB[numNodes];
    ArrayList<Package> file;
    String fileName;
    for (int i = 0; i < numNodes; i++) {
      fileName = "file"+i;
      file = new ArrayList<Package>();
      dbs[i] = new DB();
      int size = (int)(Math.random()*10) + 1;
      for (int p = 1; p<= size; p++) {
        file.add(new Package("package " + p + " of " + fileName));
      }
      dbs[i].storeFile(fileName, file);
      PeerNode peer = new PeerNode(dbs[i]);
      nodes[i] = peer;
    }
    // the peers are running, each with one file. Now get them to share their files with each other.
    // convert the array to ArrayList needed by the availFiles() call.
    ArrayList<Server> servers = new ArrayList<Server>(); 
    for (Server srv : nodes) {
      servers.add(srv);
    }
    for (int i = 0; i < numNodes; i++) {
      for (int j = i+1; j < numNodes; j++) {
        nodes[i].invokeVoid("reqFile", nodes[j], "file"+j);
      }
    }

    for(int x = 1; x <= 2; x++) {
      for (int i = 0; i < dbs.length; i++) {
        dbs[i].dump();
      }
      try {
        sleep(1000);
      }
      catch (InterruptedException e) {}    
    }
    // now let's try out availFiles
    HashMap<Server,Set<String>> catalog = (HashMap<Server,Set<String>>)nodes[1].invoke("availFiles", servers).get();
    System.out.println("avail files");
    for (Server srv : catalog.keySet()) {
      System.out.println(srv);
      for (String fn: catalog.get(srv)) {
        System.out.println(fn);
      }
    }
  }
}
@SuppressWarnings("unchecked")
class PeerNode extends CreoleObject implements Server {
  private DB db;
  PeerNode(DB db) { this.db = db;}
  public Set<String> enquire() {
    return (Set<String>)creoleAwait(db.invoke("listFiles"));
  }
  
  public int getLength(String fileId) {
    return (Integer)creoleAwait(db.invoke("getLength",fileId)); // length in units of packages
  }
  
  public Package getPack(String fileId, Integer packageNum) {
    ArrayList<Package> file;
    file = (ArrayList<Package>)creoleAwait(db.invoke("getFile",fileId)); // this seems horribly inefficient, refetching the entire file for each package
    return (Package)file.get(packageNum-1); // switch to 0 indexing just here
  }
  
  public void reqFile(PeerNode serverId, String fileId) {
    ArrayList<Package> file = new ArrayList<Package>();
    Package pack;
    int packageNum;
    
    packageNum = (Integer)creoleAwait(serverId.invoke("getLength",fileId));
    while (packageNum > 0) {
      pack = (Package)creoleAwait(serverId.invoke("getPack",fileId, packageNum));
      file.add(0, pack);
      packageNum--;
    }
    db.invokeVoid("storeFile",fileId, file);
  }
  
  public HashMap<Server,Set<String>> availFiles(ArrayList<Server> serverList) {
    serverList = (ArrayList<Server>)serverList.clone(); // until I think of a better time or place to make the copy
    Future futFileList;
    Future futServerFileMap;
    Set<String> fileList = new HashSet<String>();
    HashMap<Server,Set<String>> serverFileMap = new HashMap<Server,Set<String>>();
    if (serverList.size() == 0) {
      return serverFileMap; // an empty list
    }
    else {
      Server server = serverList.remove(0);
      futFileList = server.invoke("enquire");
      futServerFileMap = this.invoke("availFiles",serverList /* need to send a copy*/);
      fileList = (Set<String>)creoleAwait(futFileList);
      
      serverFileMap = (HashMap<Server,Set<String>>)creoleAwait(futServerFileMap);
      serverFileMap.put(server,fileList);
      return serverFileMap; // may need to return a copy
    }
  }
}

class DB extends CreoleObject {
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

class Package {
  String content;
  Package(String content) {
    this.content = content;
  }
}

interface Server extends CreoleObjectI {
  Set<String> enquire() ;
  
  int getLength(String fileId) ;
  
  Package getPack(String fileId, Integer packageNum) ;
}
