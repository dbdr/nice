package bossa.engine;

/**
 * Thrown when the constraint implies that x1 be a subtype of x2 but x1 and x2
 * are rigid and x1 is not a subtype of x2
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
public class LowlevelRigidClash extends LowlevelUnsatisfiable {
  private int x1;
  private int x2;
  LowlevelRigidClash(int x1, int x2) {
    super("Clash ("+x1+"<:"+x2+")");
    this.x1 = x1;
    this.x2 = x2;
  }
  public int getX1() {
    return x1;
  }
  public int getX2() {
    return x2;
  }
}
