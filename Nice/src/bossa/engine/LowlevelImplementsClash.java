package bossa.engine;

/**
 * Thrown when the constraint implies that the rigid variable a implement i
 * which is not the case
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
public class LowlevelImplementsClash extends LowlevelUnsatisfiable {
  private int a;
  private int i;
  LowlevelImplementsClash(int a, int i) {
    super(a+" : "+i);
    this.a = a;
    this.i = i;
  }
  public int getA() {
    return a;
  }
  public int getI() {
    return i;
  }
}
