package mlsub.typing.lowlevel;

/**
 * Thrown when the constraint implies that a and b have a common subtype or
 * supertype, but a and b are rigid and this is not true
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
public class LowlevelIncompatibleClash extends LowlevelUnsatisfiable {
  public final static int NO_COMMON_SUBTYPE = 1;
  public final static int NO_COMMON_SUPERTYPE = 2;
  private int what;
  private int a;
  private int b;
  private int z;
  LowlevelIncompatibleClash(int what, int a, int b, int z) {
    this.what = what;
    this.a = a;
    this.b = b;
    this.z = z;
  }
  public int getReason() {
    return what;
  }
  public int[] getRigidPair() {
    return new int[] { a, b };
  }
  public int getVar() {
    return z;
  }

  public String getMessage() 
  {
    return "Lowlevel incompatible clash: "+
      a+" and "+b+" are not compatible";
  }
}
