package mlsub.typing.lowlevel;

/**
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
abstract public class Unsatisfiable extends Exception {
  public Unsatisfiable(String msg)
  {
    super(msg);
  }
  
  public boolean isKindError() {
    return (this instanceof KindUnsatisfiable);
  }
}
