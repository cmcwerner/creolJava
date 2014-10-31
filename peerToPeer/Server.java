import java.util.Set;
import creole.*;

interface Server extends CreoleObjectI {
  Set<String> enquire() ;
  
  int getLength(String fileId) ;
  
  Package getPack(String fileId, Integer packageNum) ;
}
