
import gnu.text.Char;

public class Native
{
  public static final boolean pointerEquality(Object o1, Object o2)
  {
    if (o1 == o2) return true;
    if (o1 == null || o2 == null) return false;
    
    /*
    System.out.println(o1 + " (" + o1.getClass() + ") == " + 
		       o2 + " (" + o1.getClass() + ")");
    */
    if (o1 instanceof Number)
      if (o2 instanceof Number)
	return ((Number) o1).doubleValue() == ((Number) o2).doubleValue();
      else
	return false;
    
    if (o1 instanceof Char)
      if (o2 instanceof Char)
	return ((Char) o1).intValue() == ((Char) o2).intValue();
      else
	return false;
    
    return false;
  }
}
