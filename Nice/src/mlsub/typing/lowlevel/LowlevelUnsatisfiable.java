package mlsub.typing.lowlevel;

/**
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
public class LowlevelUnsatisfiable extends Unsatisfiable 
{
  LowlevelUnsatisfiable(String msg)
  {
    super(msg);
  }
  
  LowlevelUnsatisfiable()
  {
    this("[NO MESSAGE]");
  }
  
  static boolean refinedReports = true;
  public static void setRefinedReports(boolean flag) {
    refinedReports = flag;
  }
}
