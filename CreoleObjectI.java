interface CreoleObjectI {
  
  Future invoke(String method, Object... args) ;
  
  void invokeVoid(String method, Object... args);
  
  Object creoleAwait(Future fut) ;
  
}