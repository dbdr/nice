package mlsub.typing.lowlevel;

/**
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
abstract public class LowlevelSolutionHandler {
  private DomainVector _solution = null;
  //  private BitVector garbage;
  //  final void fillSolution(DomainVector _solution) {
  //    this._solution = _solution;
  //  }

  final void handle(DomainVector _solution) {
    this._solution = _solution;
    handle();
  }
    
  
  final protected int getSolutionOf(int x) {
    return _solution.getDomain(x).getLowestSetBit();
  }
  /**
   * Called when a solution is found. handle can then call getSolutionOf(x) to
   * retrieve the solution.
   **/
  abstract protected void handle();
}
