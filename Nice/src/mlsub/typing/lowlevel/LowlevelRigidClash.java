package mlsub.typing.lowlevel;

/**
 * Thrown when the constraint implies that x1 be a subtype of x2 but x1 and x2
 * are rigid and x1 is not a subtype of x2
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
public class LowlevelRigidClash extends LowlevelUnsatisfiable {
  LowlevelRigidClash(String x1, String x2) {
    super("Clash ("+x1+"<:"+x2+")");
  }
}
