package creole;

public interface CreoleObjectI {
  
  Future invoke(String method, Object... args) ;
  
  Object creoleAwait(Future fut) ;
  
}