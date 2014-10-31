import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import creole.*;

@SuppressWarnings("unchecked")
class PeerToPeer extends CreoleObject {
  public static void main(String[] args) {
    new PeerToPeer().test();
  }
  public void test() {
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
        nodes[i].invoke("reqFile", nodes[j], "file"+j);
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



