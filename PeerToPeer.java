class PeerToPeer {
}

class PeerNode {
  
  void reqFile(Server serverId, String fileId) {
    ArrayList<Package> file = new ArrayList<Package>();
    Package pack;
    int packageNum;
    
    packageNum = serverId.getLength(fileId, packageNum);
    while (packageNum > 0) {
      pack = await(serverId.getPack(fileId, packageNum));
      file.add(0, pack);
      packageNum--;
    }
    db.store(fileId, file);
  }
  
  HashMap<Server><Set<String>> availFiles(ArrayList<Server> serverList) {
    Future futFileList;
    Future futServerFileMap;
    Set<String> fileList = new HashSet<String>();
    HashMap<Server><Set<String>> serverFileMap = new HashMap<Server><Set<String>>();
    if (serverList.size() == 0) {
      return serverFileMap; // an empty list
    }
    else {
      Server server = serverList.remove(0);
      futFileList = server.enquire();
      futServerFileMap = this.availFiles(serverList /* need to send a copy*/);
      fileList = await(futFileList);
      serverFileMap = await(futServerFileMap);
      serverFileMap.put(server,fileList);
      return serverFileMap; // may need to return a copy
    }
  }
}
