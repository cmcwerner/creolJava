package creol;

public interface CreolObjectI {
  
  Future invoke(String method, Object... args) ;
  
  Object creolAwait(Future fut) ;
  
}